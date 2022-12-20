package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate kafkaTemplate;

    public final String CREATE_WALLET_TOPIC = "create_wallet";

    @KafkaListener(topics = {CREATE_WALLET_TOPIC}, groupId = "friends_group")
    public void createWallet(String message) throws JsonProcessingException {

        JSONObject walletRequest = objectMapper.readValue(message,JSONObject.class);

        String userName = String.valueOf(walletRequest.get("username"));

        WalletEntity walletEntity = WalletEntity.builder().userName(userName)
                                                          .balance(0).build();
        walletRepository.save(walletEntity);

    }

    @KafkaListener(topics = {"update_wallet"}, groupId = "friends_group")
    public void updateWallet(String message) throws JsonProcessingException {

        JSONObject walletRequest = objectMapper.readValue(message,JSONObject.class);

        String fromUser = String.valueOf(walletRequest.get("fromUser"));
        String toUser = String.valueOf(walletRequest.get("toUser"));
        int amount = (int) walletRequest.get("amount");
        String transactionId = String.valueOf(walletRequest.get("transactionId"));

        //TODO STEPS :
        // 1st CHECK BALANCE FROM fromUser
        /*
            //IF FAIL (if senders balance is not sufficient)
            //SEND STATUS AS FAILED
            //OTHERWISE
            deduct the senders money
            add the receivers money
            SEND STATUS AS SUCCESS
         */

        WalletEntity sendersWallet = walletRepository.findByUserName(fromUser);

        if(sendersWallet.getBalance()>=amount){

            //UPDATE THE WALLETS
            walletRepository.updateWallet(fromUser,-1*amount);
            walletRepository.updateWallet(toUser,amount);

            //Push TO Kafka:
            JSONObject sendTOTransaction = new JSONObject();

            sendTOTransaction.put("transactionId",transactionId);
            sendTOTransaction.put("TransactionStatus","SUCCESS");

            String sendMessage = sendTOTransaction.toString();

            kafkaTemplate.send("updateTransaction",message);

        }
        else{

            JSONObject sendToTransaction = new JSONObject();

            sendToTransaction.put("transactionId",transactionId);
            sendToTransaction.put("transactionStatus","FAILED");

            String sendMessage = sendToTransaction.toString();

            kafkaTemplate.send("update_transaction",sendMessage);

        }

    }

    /*public WalletEntity incrementWallet(String userName,int amount){

        WalletEntity oldWalletEntity = walletRepository.findByUserName(userName);
        int newAmount = oldWalletEntity.getBalance() + amount;
        int id = oldWalletEntity.getId();

        WalletEntity updatedWallet = WalletEntity.builder().id(id).userName(userName)
                                                           .balance(newAmount).build();

        walletRepository.save(updatedWallet);
        return updatedWallet;
    }

    public WalletEntity decrementWallet(String userName,int amount){

        WalletEntity oldWalletEntity = walletRepository.findByUserName(userName);
        int newAmount = oldWalletEntity.getBalance() - amount;
        int id = oldWalletEntity.getId();

        WalletEntity updatedWallet = WalletEntity.builder().id(id).userName(userName)
                .balance(newAmount).build();

        walletRepository.save(updatedWallet);
        return updatedWallet;
    }*/
}
