package com.example.leowinebot.service.impl;

import com.example.leowinebot.entity.User;
import com.example.leowinebot.repository.UserRepository;
import com.example.leowinebot.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Integer FROM_AGE = 2;

    private static final Integer TO_AGE = 4;

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByChatId(String chatId) {
        return userRepository.findByChatId(chatId).orElse(null);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void saveAllLikedPerHour() {
        List<User> users = userRepository.findAll()
                .stream()
                .peek(u -> u.setLikedPerHour(0))
                .peek(u -> u.setCountForCity(0))
                .collect(Collectors.toList());
        userRepository.saveAll(users);
    }

    @Override
    public void deleteByChatId(String chatId) {
        userRepository.deleteByChatId(chatId);
    }

    private User findUserByAllCity(User user) {
        if (user.getSearchGender().equals("bi")) {
            List<User> usersList = userRepository
                    .findAllByChatIdNotContainingAndSearchGenderAndActiveAndAgeBetween(user.getChatId(),
                            user.getSearchGender(),
                            true, user.getAge() - FROM_AGE, user.getAge() + TO_AGE);
            if (checkListUsersIsEmpty(user, usersList)) {
                return usersList.get(user.getCountForCity());
            }

        } else {
            List<User> usersList = userRepository
                    .findAllByChatIdNotContainingAndGenderAndActiveAndAgeBetween(user.getChatId(),
                            user.getSearchGender(),
                            true, user.getAge() - FROM_AGE, user.getAge() + TO_AGE);
            if (checkListUsersIsEmpty(user, usersList)) {
                return usersList.get(user.getCountForCity());
            }
        }
        return null;
    }

    @Override
    public User findRandomUserByCriterion(User user) {

        ArrayList<String> citiesList = new ArrayList<>();
        if (!user.getAdminNameCity().equals("not admin city")) {
            try {
                RestTemplate restForCityName = new RestTemplate();
                ResponseEntity<String> responseInCityName = restForCityName
                        .getForEntity("http://api.geonames.org/searchJSON?q=" +
                                user.getAdminNameCity() + "&maxRows=15&lang=ru&style=full&cities=cities15000&username=leowinebot", String.class);
                ObjectMapper mapperForCityName = new ObjectMapper();
                JsonNode bodyForCityName = mapperForCityName.readTree(Objects.requireNonNull(responseInCityName.getBody()));
                if (!bodyForCityName.findPath("status").isContainerNode() && bodyForCityName.findPath("geonames").isContainerNode()) {
                    for (int i = 0; i < bodyForCityName.get("geonames").size(); i++) {
                        citiesList.add(bodyForCityName.path("geonames").get(i).path("name").asText());
                    }
                } else {
                    RestTemplate restForCityName2 = new RestTemplate();
                    ResponseEntity<String> responseInCityName2 = restForCityName2
                            .getForEntity("http://api.geonames.org/searchJSON?q=" +
                                    user.getAdminNameCity() + "&maxRows=15&lang=ru&style=full&cities=cities15000&username=leowinebot2", String.class);
                    ObjectMapper mapperForCityName2 = new ObjectMapper();
                    JsonNode bodyForCityName2 = mapperForCityName2.readTree(Objects.requireNonNull(responseInCityName2.getBody()));
                    if (!bodyForCityName2.findPath("status").isContainerNode() && bodyForCityName2.findPath("geonames").isContainerNode()) {
                        for (int i = 0; i < bodyForCityName2.get("geonames").size(); i++) {
                            citiesList.add(bodyForCityName2.path("geonames").get(i).path("name").asText());
                        }
                    } else {
                        return findUserByAllCity(user);
                    }
                }

            } catch (JsonProcessingException e) {
                return findUserByAllCity(user);
            }
        } else {
            return findUserByAllCity(user);
        }

        if (user.getSearchGender().equals("bi")) {
            List<User> usersList = userRepository.findAllByChatIdNotContainingAndCityInAndSearchGenderAndActiveAndAgeBetween(user.getChatId(),
                    citiesList, user.getSearchGender(),
                    true, user.getAge() - FROM_AGE, user.getAge() + TO_AGE);
            if (checkListUsersIsEmpty(user, usersList)) {
                return usersList.get(user.getCountForCity());
            }
        } else {
            List<User> usersList = userRepository
                    .findAllByChatIdNotContainingAndCityInAndGenderAndActiveAndAgeBetween(user.getChatId(),
                            citiesList, user.getSearchGender(),
                            true, user.getAge() - FROM_AGE, user.getAge() + TO_AGE);
            if (checkListUsersIsEmpty(user, usersList)) {
                return usersList.get(user.getCountForCity());
            }
        }

        return null;
    }

    private boolean checkListUsersIsEmpty(User user, List<User> listUsers) {
        if (!listUsers.isEmpty()) {
            if (user.getCountForCity() < listUsers.size()) {
                return true;
            } else {
                user.setCountForCity(0);
                save(user);
                return true;
            }
        }
        return false;
    }
}
