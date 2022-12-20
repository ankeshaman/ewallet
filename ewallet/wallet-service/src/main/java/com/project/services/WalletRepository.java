package com.project.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WalletRepository extends JpaRepository<WalletEntity,Integer> {

    public WalletEntity findByUserName(String userName);

    @Modifying
    @Query("update WalletEntity w set w.balance = w.balance + :amount where w.userName = :userName")
    public void updateWallet(String userName,int amount);

}
