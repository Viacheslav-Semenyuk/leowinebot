package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class ProfileHandler implements Handler {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileEditHandler profileEditHandler;

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private ProfileAboutEditHandler profileAboutEditHandler;

    @Autowired
    private ProfilePhotoEditHandler profilePhotoEditHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Autowired
    private Bot bot;

    public void handle(Message message, User user, String chatId) {

        if (user.getProfileEditStates().equals("0")) {
            if (user.getPhoto() != null) {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Так выглядит твоя анкета:"));
                bot.executePhoto(new SendPhoto().setChatId(chatId)
                        .setCaption(user.getName() + ", " + user.getAge() + ", "
                                + user.getCity() + " " + user.getAbout()).setPhoto(user.getPhoto()));

                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.profileKeyboard()))
                        .setText("1. Заполнить анкету заново.\n" +
                                "2. Изменить фото.\n" +
                                "3. Изменить текст анкеты.\n" +
                                "4. Смотреть анкеты."));
                user.setActive(true);
                user.setProfileEditStates("1");
                userService.save(user);
            } else {
                user.setActive(false);
                user.setUserStates("profileEdit");
                user.setProfileEditStates("0");
                userService.save(user);
                profileEditHandler.handle(message, user, chatId);
            }
        } else if (user.getProfileEditStates().equals("1")) {
            if (message.getText().equals("1")) {
                user.setActive(false);
                user.setUserStates("profileEdit");
                user.setProfileEditStates("0");
                userService.save(user);
                profileEditHandler.handle(message, user, chatId);
            } else if (message.getText().equals("2")) {
                user.setActive(false);
                user.setUserStates("profilePhotoEdit");
                user.setProfileEditStates("0");
                userService.save(user);
                profilePhotoEditHandler.handle(message, user, chatId);
            } else if (message.getText().equals("3")) {
                user.setActive(false);
                user.setUserStates("profileAboutEdit");
                user.setProfileEditStates("0");
                userService.save(user);
                profileAboutEditHandler.handle(message, user, chatId);
            } else if (message.getText().equals("4")) {
                user.setUserStates("search");
                user.setSearchStates("0");
                user.setProfileEditStates("0");
                userService.save(user);
                searchHandler.handle(message, user, chatId);
            } else {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.profileKeyboard()))
                        .setText("Нету такого варианта ответа."));
            }

        }
    }

    @Override
    public boolean test(String o) {
        return "profile".equals(o);
    }
}
