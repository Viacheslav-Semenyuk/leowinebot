package com.example.leowinebot.service.impl;

import com.example.leowinebot.entity.Match;
import com.example.leowinebot.repository.MatchUserRepository;
import com.example.leowinebot.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchUserRepository matchUserRepository;

    @Override
    public Boolean existsByChatIdAndLikeChatId(String chatId, String likeChatId) {
        return matchUserRepository.findAllByChatId(chatId)
                .stream()
                .anyMatch(s -> s.getLikeChatId().equals(likeChatId));
    }

    @Override
    public Match findByLikeChatId(String chatId) {
        return matchUserRepository.findFirstByLikeChatId(chatId).orElse(null);
    }


    @Override
    public void deleteByChatIdAndLikeChatId(String chatId, String likeChatId) {
        matchUserRepository.deleteByChatIdAndLikeChatId(chatId, likeChatId);
    }

    @Override
    public void save(Match match) {
        matchUserRepository.save(match);
    }

    @Override
    public Boolean existsByLikeChatId(String chatId) {
        return matchUserRepository.existsByLikeChatId(chatId);
    }

    @Override
    public Integer countByLikeChatId(String chatId) {
        return matchUserRepository.countByLikeChatId(chatId);
    }

    @Override
    public void deleteAllByChatId(String chatId) {
        matchUserRepository.deleteAllByChatId(chatId);
    }


}
