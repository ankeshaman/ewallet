package com.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    public final String REDIS_PREFIX_KEY = "user::";
    public final String CREATE_WALLET_TOPIC = "create_wallet";

    public void createUser(UserRequestDto userRequestDto){
        UserEntity userEntity = UserConverter.convertDtoToEntity(userRequestDto);
        userRepository.save(userEntity);

        saveInCache(userEntity);

        //Sending a message through Kafka :

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("username",userEntity.getUserName());
        jsonObject.put("name",userEntity.getName());

        //Converting jsonObject to String bcz message is in string format
        String message = jsonObject.toString();

        kafkaTemplate.send(CREATE_WALLET_TOPIC, userEntity.getUserName(),message);

    }

    public void saveInCache(UserEntity userEntity){
        //saving in cache
        Map map = objectMapper.convertValue(userEntity,Map.class);
        redisTemplate.opsForHash().putAll(REDIS_PREFIX_KEY+userEntity.getUserName(),map);

        //setting duration of hours upto which data will be present inside redis database coz its duration is Infinity
        redisTemplate.expire(REDIS_PREFIX_KEY+userEntity.getUserName(), Duration.ofHours(12));
    }

    public UserEntity getUserByUserName(String userName) throws Exception {

        //Find in Cache
        Map map = redisTemplate.opsForHash().entries(REDIS_PREFIX_KEY+userName);
        if(map==null || map.size()==0){

            //In cache not present so Finding in DB
            UserEntity userEntity = userRepository.findByUser_Name(userName);
            if(userEntity != null){
                saveInCache(userEntity);
            }
            else{
                throw new UserNotFoundException();
            }
            return userEntity;
        }
        else{
            ////Found in the cache --> return from the cache
            UserEntity userEntity = objectMapper.convertValue(map,UserEntity.class);
            return userEntity;
        }

    }

}
