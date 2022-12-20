package com.project.services;

public class UserNotFoundException extends Exception{

    public UserNotFoundException() {
        super("User not found");
    }

}
