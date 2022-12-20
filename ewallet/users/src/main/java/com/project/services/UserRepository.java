package com.project.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity,Integer> {

    //@Query(value = "select * from users where user_name = :username",nativeQuery = true)
    @Query(value = "select u from UserEntity u where u.userName = :username")
    UserEntity findByUser_Name(@Param("username") String userName);

    //UserEntity findByUserName(String userName);

    List<UserEntity> findAllByUserNameAndAge(String userName,int age);

    boolean existsByUserName(String userName);



}
