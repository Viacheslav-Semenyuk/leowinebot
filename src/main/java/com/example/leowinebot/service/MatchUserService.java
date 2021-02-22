package com.example.leowinebot.service;

import com.example.leowinebot.entity.MatchUser;

public interface MatchUserService {

    Boolean existsByChatIdAndLikeChatId(String chatId, String likeChatId);

    MatchUser findByLikeChatId(String chatId);

    void deleteByChatIdAndLikeChatId(String chatId, String likeChatId);

    void save(MatchUser matchUser);

    Boolean existsByLikeChatId(String chatId);

    Integer countByLikeChatId(String chatId);

    void deleteAllByChatId(String chatId);
}
