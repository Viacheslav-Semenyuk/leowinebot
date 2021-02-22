package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Component
public class ProfileAboutEditHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileHandler profileHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Autowired
    private Bot bot;

    public Message handle(Message message, User user, String chatId) {

        switch (user.getProfileEditStates()) {
            case ("0"):
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.editKeyboard("Пропустить")))
                        .setText("Расскажи о себе и кого хочешь найти, \n" +
                                "чем предлагаешь заняться. \n" +
                                "Это поможет лучше подобрать тебе компанию."));
                user.setProfileEditStates("1");
                userService.save(user);
                break;
            case ("1"):
                if (message.getText().equals("Пропустить")) {
                    user.setAbout(" ");
                } else {
                    user.setAbout("– " + message.getText());
                }
                user.setActive(true);
                user.setStates("2");
                user.setProfileEditStates("0");
                userService.save(user);
                profileHandler.handle(message, user, chatId);
                break;
        }

        return message;
    }

}
