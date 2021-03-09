package com.example.leowinebot.repository;

import com.example.leowinebot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByChatId(String chatId);

    void deleteByChatId(String chatId);

    List<User> findAllByChatIdNotContainingAndCityInAndGenderAndActiveAndAgeBetween(String chatId, List<String> city,
                                                                                    String gender,
                                                                                    Boolean active,
                                                                                    int fromAge, int toAge);

    List<User> findAllByChatIdNotContainingAndCityInAndSearchGenderAndActiveAndAgeBetween(String chatId, List<String> city,
                                                                                          String searchGender,
                                                                                          Boolean active,
                                                                                          int fromAge, int toAge);

    List<User> findAllByChatIdNotContainingAndGenderAndActiveAndAgeBetween(String chatId,
                                                                           String gender,
                                                                           Boolean active,
                                                                           int fromAge, int toAge);

    List<User> findAllByChatIdNotContainingAndSearchGenderAndActiveAndAgeBetween(String chatId,
                                                                                 String searchGender,
                                                                                 Boolean active,
                                                                                 int fromAge, int toAge);

}
