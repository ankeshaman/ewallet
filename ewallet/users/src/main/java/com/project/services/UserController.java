package com.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public void createUser(@RequestBody UserRequestDto userDto){

        userService.createUser(userDto);
    }

    @GetMapping("/user")
    public UserEntity getUserByUserName(@RequestParam("userName") String userName) throws Exception {
        return userService.getUserByUserName(userName);
    }

}
