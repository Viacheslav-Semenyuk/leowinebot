package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ActiveHandler {


    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileHandler profileHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public Message handle(Message message, User user, String chatId) {

        if (message.getText().equals("1") && user.getActive().equals(false)) {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Мы тебя помним!"));
            user.setStates("2");
            userService.save(user);
            profileHandler.handle(message, user, chatId);
        } else if (user.getActive().equals(false)) {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Нет такого варианта ответа"));
        }

        if (user.getActive().equals(true)) {
            switch (message.getText()) {
                case ("1"):
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.oneKeyboard()))
                            .setText("Надеюсь ты нашел кого-то благодаря мне! Рад был с тобой пообщаться, " +
                                    "будет скучно – пиши, обязательно найдем тебе кого-нибудь\n" +
                                    "\n" +
                                    "1. Смотреть анкеты"));
                    user.setActive(false);
                    userService.save(user);
                    break;
                case ("2"):
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                            .setText("1. Смотреть анкеты.\n" +
                                    "2. Моя анкета.\n" +
                                    "3. Я больше не хочу никого искать."));
                    user.setStates("8");
                    userService.save(user);
                    break;
                default:
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Нет такого варианта ответа"));
                    break;
            }
        }


        return message;
    }
}
