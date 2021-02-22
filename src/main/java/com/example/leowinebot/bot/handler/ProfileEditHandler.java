package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Objects;

@Component
public class ProfileEditHandler {

    @Autowired
    private Bot bot;

    @Autowired
    private ProfileHandler profileHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public Message handle(Message message, User user, String chatId) {

        switch (user.getProfileEditStates()) {
            case ("0"):
                user.setProfileEditStates("1");
                userService.save(user);
                if (user.getAge() != null) {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler
                                    .editKeyboard(Integer.toString(user.getAge()))))
                            .setText("Сколько тебе лет?"));
                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(new ReplyKeyboardRemove())
                            .setText("Сколько тебе лет?"));
                }

                break;
            case ("1"):
                if (NumberUtils.isDigits(message.getText()) && Integer.parseInt(message.getText()) > 9
                        && Integer.parseInt(message.getText()) < 90) {
                    user.setAge(Integer.parseInt(message.getText()));
                    user.setProfileEditStates("2");
                    userService.save(user);
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                            .setText("Теперь определимся с полом \n" + "\n" +
                                    "1. Я девушка. \n" +
                                    "2. Я парень."));
                } else {
                    if (user.getAge() != null) {
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardHandler.handle(keyboardHandler
                                        .editKeyboard(Integer.toString(user.getAge()))))
                                .setText("Укажи правильный возраст, только цифры"));
                    } else {
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText("Укажи правильный возраст, только цифры"));
                    }
                }
                break;
            case ("2"):
                switch (message.getText()) {
                    case ("1"):
                        user.setGender("female");
                        user.setProfileEditStates("3");
                        userService.save(user);
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchGenderKeyboard()))
                                .setText("Кто тебе интересен? \n" + "\n" +
                                        "1. Девушки. \n" +
                                        "2. Парни. \n" +
                                        "3. Все равно."));
                        break;
                    case ("2"):
                        user.setGender("male");
                        user.setProfileEditStates("3");
                        userService.save(user);
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchGenderKeyboard()))
                                .setText("Кто тебе интересен? \n" + "\n" +
                                        "1. Девушки. \n" +
                                        "2. Парни. \n" +
                                        "3. Все равно."));
                        break;
                    default:
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText("Нет такого варианта ответа."));
                        break;
                }
                break;
            case ("3"):
                switch (message.getText()) {
                    case ("Девушки"):
                        user.setSearchGender("female");
                        user.setProfileEditStates("4");
                        userService.save(user);
                        whatCity(user);
                        break;
                    case ("Парни"):
                        user.setSearchGender("male");
                        user.setProfileEditStates("4");
                        userService.save(user);
                        whatCity(user);
                        break;
                    case ("Все равно"):
                        user.setSearchGender("bi");
                        user.setProfileEditStates("4");
                        userService.save(user);
                        whatCity(user);
                        break;
                    default:
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText("Нет такого варианта ответа."));
                        break;

                }
                break;
            case ("4"):
                try {
                    RestTemplate restForAdminName = new RestTemplate();
                    ResponseEntity<String> responseInAdminName = restForAdminName
                            .getForEntity("http://api.geonames.org/searchJSON?q=" +
                                    message.getText() + "&lang=ru&style=full&cities=cities1000&username=leowinebot1", String.class);
                    ObjectMapper mapperForAdminName = new ObjectMapper();
                    JsonNode bodyForAdminName = mapperForAdminName.readTree(Objects.requireNonNull(responseInAdminName.getBody()));
                    if (!bodyForAdminName.findPath("status").isContainerNode() && !bodyForAdminName.findPath("geonames").isEmpty()) {
                        user.setAdminNameCity(bodyForAdminName.findPath("adminName1").asText());
                    } else {
                        user.setAdminNameCity("not admin city");
                    }

                } catch (JsonProcessingException e) {
                    user.setAdminNameCity("not admin city");
                }

                user.setCity(message.getText());
                user.setProfileEditStates("5");
                userService.save(user);
                if (user.getName() != null) {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler
                                    .editKeyboard(user.getName())))
                            .setText("Как мне тебя называть?"));
                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Как мне тебя называть?"));
                }

                break;
            case ("5"):
                user.setName(message.getText());
                user.setProfileEditStates("6");
                userService.save(user);
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.editKeyboard("Пропустить")))
                        .setText("Расскажи о себе и кого хочешь найти, \n" +
                                "чем предлагаешь заняться. \n" +
                                "Это поможет лучше подобрать тебе компанию."));
                break;
            case ("6"):
                if (message.getText().equals("Пропустить")) {
                    user.setAbout(" ");
                    user.setProfileEditStates("7");
                    userService.save(user);
                    whatPhoto(user);
                } else {
                    user.setAbout("– " + message.getText());
                    user.setProfileEditStates("7");
                    userService.save(user);
                    whatPhoto(user);

                }

                break;
            case ("7"):
                if (message.hasPhoto() && !message.hasText() && !message.hasAudio()) {
                    user.setPhoto(message.getPhoto().get(0).getFileId());
                    user.setActive(true);
                    user.setStates("2");
                    user.setProfileEditStates("0");
                    userService.save(user);
                    profileHandler.handle(message, user, chatId);
                } else if (message.getText().equals("Оставить текущее")) {
                    user.setPhoto(user.getPhoto());
                    user.setActive(true);
                    user.setStates("2");
                    user.setProfileEditStates("0");
                    userService.save(user);
                    profileHandler.handle(message, user, chatId);
                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Прикрепи к сообщению фото"));
                }
                break;
        }

        return message;
    }

    private void whatCity(User user) {
        if (user.getCity() != null) {
            bot.executeMessage(new SendMessage()
                    .setChatId(user.getChatId())
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler
                            .editKeyboard(user.getCity())))
                    .setText("Из какого ты города?"));
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(user.getChatId())
                    .setReplyMarkup(new ReplyKeyboardRemove())
                    .setText("Из какого ты города?"));
        }

    }

    private void whatPhoto(User user) {
        if (user.getPhoto() != null) {
            bot.executeMessage(new SendMessage()
                    .setChatId(user.getChatId())
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler
                            .editKeyboard("Оставить текущее")))
                    .setText("Теперь пришли свое фото, \n" +
                            "его будут видеть другие пользователи"));
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(user.getChatId())
                    .setText("Теперь пришли свое фото, \n" +
                            "его будут видеть другие пользователи"));
        }

    }
}

