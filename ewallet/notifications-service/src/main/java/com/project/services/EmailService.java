package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SimpleMailMessage simpleMailMessage;

    @Autowired
    JavaMailSender javaMailSender;

    @KafkaListener(topics = {"send_mail"}, groupId = "friends_group")
    public void sendEmailMessage(String message) throws JsonProcessingException {

        //DECODING THE MESSAGE TO JSONObject
        //User email ....message
        JSONObject emailRequest = objectMapper.readValue(message,JSONObject.class);

        //Get the email and message from JSONObject
        String email = (String)emailRequest.get("email");
        String messageBody = (String)emailRequest.get("message");

        simpleMailMessage.setFrom("noreply@ewallet.com");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Transaction Information");
        simpleMailMessage.setText(messageBody);

        javaMailSender.send(simpleMailMessage);

    }

}
