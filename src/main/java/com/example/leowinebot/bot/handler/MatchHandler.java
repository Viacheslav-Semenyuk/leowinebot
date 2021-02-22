package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.MatchUser;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.MatchUserService;
import com.example.leowinebot.service.UserService;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class MatchHandler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private MatchUserService matchUserService;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public Message handle(Message message, User user, String chatId) {

        if (matchUserService.existsByLikeChatId(chatId)) {
            MatchUser matchUser = matchUserService.findByLikeChatId(chatId);
            User foundUser = userService.findByChatId(matchUser.getChatId());
            if (foundUser == null) {
                matchUserService.deleteAllByChatId(matchUser.getChatId());
            }
            assert foundUser != null;
            log.info("FROM MATCH USER {} to USER {}", user.getChatId(), foundUser.getChatId());
            switch (user.getMatchStates()) {
                case ("0"):
                    if (message.getText().equals("1")) {

                        sendMessageLike(chatId, foundUser, matchUser);

                        user.setActive(true);
                        user.setStates("7");
                        user.setMatchStates("1");
                        userService.save(user);
                        break;

                    } else if (message.getText().equals("2")) {
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                                .setText("Так ты не узнаешь, что кому-то нравишься... Точно хочешь отключить свою анкету?\n" +
                                        "\n" +
                                        "1. Да, отключить анкету.\n" +
                                        "2. Нет, вернуться назад."));
                        user.setStates("9");
                        userService.save(user);
                        break;
                    } else {
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setText("Нет такого варианта ответа"));
                    }

                case ("1"):
                    Emoji emoji = EmojiManager.getByUnicode(message.getText());
                    if (emoji != null) {
                        switch (emoji.getAliases().get(0)) {
                            case ("heart"):
                                bot.executeMessage(new SendMessage()
                                        .setChatId(chatId)
                                        .setText("Отлично! Надесюь хорошо проведете время ;) добавляй в друзья "
                                                + "[" + foundUser.getName() + "](tg://user?id=" + foundUser.getChatId() + ")")
                                        .enableMarkdown(true));

                                bot.executeMessage(new SendMessage()
                                        .setChatId(foundUser.getChatId())
                                        .setText("Есть взаимная симпатия! Добавляй в друзья "
                                                + "[" + user.getName() + "](tg://user?id=" + user.getChatId() + ")")
                                        .enableMarkdown(true));
                                bot.executePhoto(new SendPhoto().setChatId(foundUser.getChatId())
                                        .setCaption(user.getName() + ", " + user.getAge() + ", "
                                                + user.getCity()  + " " + user.getAbout()).setPhoto(user.getPhoto()));
                                bot.executeMessage(new SendMessage()
                                        .setChatId(foundUser.getChatId())
                                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                                        .setText("1. Смотреть анкеты.\n" +
                                                "2. Моя анкета.\n" +
                                                "3. Я больше не хочу никого искать."));

                                delete(chatId, user, foundUser);
                                foundUser.setStates("8");
                                foundUser.setMatchStates("0");
                                foundUser.setSearchStates("0");
                                userService.save(foundUser);
                                break;

                            case ("-1"):
                                delete(chatId, user, foundUser);
                                break;

                            default:
                                bot.executeMessage(new SendMessage()
                                        .setChatId(chatId)
                                        .setText("Нет такого варианта ответа"));
                                break;
                        }
                    } else {
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardHandler.handle(keyboardHandler.matchKeyboard()))
                                .setText("Воспользуйся кнопками."));

                    }
                    break;
                default:
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Нет такого варианта ответа"));
                    break;
            }
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                    .setText("1. Смотреть анкеты.\n" +
                            "2. Моя анкета.\n" +
                            "3. Я больше не хочу никого искать."));
            user.setStates("8");
            user.setMatchStates("0");
            user.setSearchStates("0");
            userService.save(user);
        }
        return message;
    }

    private void delete(String chatId, User user, User foundUser) {
        matchUserService.deleteByChatIdAndLikeChatId(foundUser.getChatId(), chatId);
        if (matchUserService.countByLikeChatId(chatId) != 0) {
            MatchUser newMatchUser = matchUserService.findByLikeChatId(chatId);
            User newFoundUser = userService.findByChatId(newMatchUser.getChatId());
            sendMessageLike(chatId, newFoundUser, newMatchUser);
            user.setStates("7");
            user.setMatchStates("1");
            user.setSearchStates("0");
            userService.save(user);
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                    .setText("1. Смотреть анкеты.\n" +
                            "2. Моя анкета.\n" +
                            "3. Я больше не хочу никого искать."));
            user.setStates("8");
            user.setMatchStates("0");
            user.setSearchStates("0");
            userService.save(user);
        }
    }

    private void sendMessageLike(String chatId, User foundUser, MatchUser msgUser) {
        if (msgUser.getMessage() != null) {
            bot.executePhoto(new SendPhoto().setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.matchKeyboard()))
                    .setCaption("Кому-то понравилась твоя анкета: \n" + "\n"
                            + foundUser.getName() + ", " + foundUser.getAge() + ", "
                            + foundUser.getCity() + " " + foundUser.getAbout()
                            + "\n\nСообщения для тебя: \n"
                            + msgUser.getMessage()).setPhoto(foundUser.getPhoto())
            );
        } else {
            bot.executePhoto(new SendPhoto().setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.matchKeyboard()))
                    .setCaption("Кому-то понравилась твоя анкета: \n" + "\n"
                            + foundUser.getName() + ", " + foundUser.getAge() + ", "
                            + foundUser.getCity() + " " + foundUser.getAbout())
                    .setPhoto(foundUser.getPhoto())
            );
        }
    }

}
