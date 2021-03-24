package com.example.leowinebot.service;

import com.example.leowinebot.entity.Match;

public interface MatchService {

    Boolean existsByChatIdAndLikeChatId(String chatId, String likeChatId);

    Match findByLikeChatId(String chatId);

    void deleteByChatIdAndLikeChatId(String chatId, String likeChatId);

    void save(Match match);

    Boolean existsByLikeChatId(String chatId);

    Integer countByLikeChatId(String chatId);

    void deleteAllByChatId(String chatId);
}
