package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {

    @Autowired
    WalletService walletService;

    @PostMapping("/createWallet")
    public ResponseEntity<String> createWallet(@RequestParam String userName) throws JsonProcessingException {
        walletService.createWallet(userName);
        return new ResponseEntity<>("wallet created successfully", HttpStatus.CREATED);
    }

}
