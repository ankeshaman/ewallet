package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

@Service
public class TransactionServices {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    public void createTransaction(TransactionRequestDto transactionRequestDto){

        TransactionEntity transactionEntity = TransactionEntity.builder().fromUser(transactionRequestDto.getFromUser())
                                                               .amount(transactionRequestDto.getAmount()).
                                                                toUser(transactionRequestDto.getToUser()).
                                                                transactionId(String.valueOf(UUID.randomUUID())).
                                                                transactionStatus(TransactionStatus.PENDING.toString()).build();

        transactionRepository.save(transactionEntity);

        JSONObject walletRequest = new JSONObject();

        walletRequest.put("fromUser",transactionRequestDto.getFromUser());
        walletRequest.put("toUser",transactionRequestDto.getToUser());
        walletRequest.put("amount",transactionRequestDto.getAmount());
        walletRequest.put("transactionId",transactionEntity.getTransactionId());

        String message = walletRequest.toString();

        kafkaTemplate.send("update_wallet",message);

    }

    @KafkaListener(topics = {"update_transaction"}, groupId = "friends_group")
    public void updateTransaction(String message) throws JsonProcessingException {

        JSONObject transactionRequest = objectMapper.readValue(message,JSONObject.class);

        String transactionId = String.valueOf(transactionRequest.get("transactionId"));
        String transactionStatus = String.valueOf(transactionRequest.get("TransactionStatus"));

        TransactionEntity transactionEntity = transactionRepository.findByTransactionId(transactionId);
        transactionEntity.setTransactionStatus(transactionStatus);

        transactionRepository.save(transactionEntity);

        callNotificationService(transactionEntity);

    }

    public void callNotificationService(TransactionEntity transactionEntity){

        //FETCH IS EMAIL FROM USER SERVICE

        String fromUser = transactionEntity.getFromUser();
        String toUser = transactionEntity.getToUser();
        String transactionId = transactionEntity.getTransactionId();

        //hitting the user repo to take the userinfo which is in another module and server from this server by using URI
        URI url = URI.create("http://localhost:8076/user?userNmae=" + fromUser);

        HttpEntity httpEntity = new HttpEntity(new HttpHeaders());
        JSONObject fromObject = restTemplate.exchange(url, HttpMethod.GET,httpEntity,JSONObject.class).getBody();

        String senderName = (String) fromObject.get("name");
        String senderMail = (String) fromObject.get("email");

        url = URI.create("http://localhost:8076/user?userNmae=" + toUser);

        JSONObject toObject = restTemplate.exchange(url,HttpMethod.GET,httpEntity,JSONObject.class).getBody();

        String receiverName = (String) toObject.get("name");
        String receiverMail = (String) toObject.get("email");

        //SENDER should always receive email
        JSONObject emailRequest = new JSONObject();

        String senderMessageBody = String.format("Hi %s the transcation with transactionId %s has been %s of Rs %d",
                senderName,transactionId,transactionEntity.getTransactionStatus(),transactionEntity.getAmount());

        emailRequest.put("email",senderMail);
        emailRequest.put("message",senderMessageBody);

        String message = emailRequest.toString();

        //SEND IT TO KAFKA
        kafkaTemplate.send("send_mail",message);

        if(transactionEntity.getTransactionStatus().equals("FAIL")){
            return;
        }

        //SEND an email to the reciever also
        String receiverMessageBody = String.format("Hi %s you have recived money %d from %s",
                receiverName,transactionEntity.getAmount(),senderName);

        emailRequest.put("email",receiverMail);
        emailRequest.put("message",receiverMessageBody);

        message = emailRequest.toString();

        kafkaTemplate.send("send_mail",message);

    }

}
