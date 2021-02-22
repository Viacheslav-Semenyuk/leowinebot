package com.example.leowinebot.service;

import com.example.leowinebot.entity.User;

public interface UserService {

    User findByChatId(String chatId);

    User findRandomUserByCriterion(User user);

    void save(User user);

    void saveAllLikedPerHour();

    void deleteByChatId(String chatId);

}
