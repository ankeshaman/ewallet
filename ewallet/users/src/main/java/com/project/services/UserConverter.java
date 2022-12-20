package com.project.services;


public class UserConverter {

    public static UserRequestDto convertEntityToDto(UserEntity userEntity){
        return UserRequestDto.builder().userName(userEntity.getUserName())
                                .name(userEntity.getName()).email(userEntity.getEmail())
                                .age(userEntity.getAge()).build();
    }

    public static UserEntity convertDtoToEntity(UserRequestDto userDto){
        return UserEntity.builder().userName(userDto.getUserName())
                                   .name(userDto.getName()).email(userDto.getEmail())
                                   .age(userDto.getAge()).build();
    }

}
