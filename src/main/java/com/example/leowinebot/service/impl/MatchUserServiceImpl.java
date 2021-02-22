package com.example.leowinebot.service.impl;

import com.example.leowinebot.entity.MatchUser;
import com.example.leowinebot.repository.MatchUserRepository;
import com.example.leowinebot.service.MatchUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchUserServiceImpl implements MatchUserService {

    @Autowired
    private MatchUserRepository matchUserRepository;

    @Override
    public Boolean existsByChatIdAndLikeChatId(String chatId, String likeChatId) {
        return matchUserRepository.findAllByChatId(chatId)
                .stream()
                .anyMatch(s -> s.getLikeChatId().equals(likeChatId));
    }

    @Override
    public MatchUser findByLikeChatId(String chatId) {
        return matchUserRepository.findFirstByLikeChatId(chatId).orElse(null);
    }


    @Override
    public void deleteByChatIdAndLikeChatId(String chatId, String likeChatId) {
        matchUserRepository.deleteByChatIdAndLikeChatId(chatId, likeChatId);
    }

    @Override
    public void save(MatchUser matchUser) {
        matchUserRepository.save(matchUser);
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
