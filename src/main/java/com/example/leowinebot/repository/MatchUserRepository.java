package com.example.leowinebot.repository;

import com.example.leowinebot.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface MatchUserRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByChatId(String chatId);

    Optional<Match> findFirstByLikeChatId(String chatId);

    Boolean existsByLikeChatId(String chatId);

    void deleteByChatIdAndLikeChatId(String chatId, String likeChatId);

    Integer countByLikeChatId(String chatId);

    void deleteAllByChatId(String chatId);
}
