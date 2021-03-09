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
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class ProfileHandler {

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

        switch (user.getProfileEditStates()) {
            case ("0"):
                if(user.getPhoto() != null){
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Так выглядит твоя анкета:"));
                    bot.executePhoto(new SendPhoto().setChatId(chatId)
                            .setCaption(user.getName() + ", " + user.getAge() + ", "
                                    + user.getCity()  + " " + user.getAbout()).setPhoto(user.getPhoto()));

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
                    break;
                }else {
                    user.setActive(false);
                    user.setStates("3");
                    user.setProfileEditStates("0");
                    userService.save(user);
                    profileEditHandler.handle(message, user, chatId);
                    break;
                }
            case ("1"):
                switch (message.getText()) {
                    case ("1"):
                        user.setActive(false);
                        user.setStates("3");
                        user.setProfileEditStates("0");
                        userService.save(user);
                        profileEditHandler.handle(message, user, chatId);
                        break;
                    case ("2"):
                        user.setActive(false);
                        user.setStates("5");
                        user.setProfileEditStates("0");
                        userService.save(user);
                        profilePhotoEditHandler.handle(message, user, chatId);
                        break;
                    case ("3"):
                        user.setActive(false);
                        user.setStates("4");
                        user.setProfileEditStates("0");
                        userService.save(user);
                        profileAboutEditHandler.handle(message, user, chatId);
                        break;
                    case ("4"):
                        user.setStates("1");
                        user.setSearchStates("0");
                        user.setProfileEditStates("0");
                        userService.save(user);
                        searchHandler.handle(message, user, chatId);
                        break;
                    default:
                        bot.executeMessage(new SendMessage()
                                .setChatId(chatId)
                                .setReplyMarkup(keyboardHandler.handle(keyboardHandler.profileKeyboard()))
                                .setText("Нету такого варианта ответа."));
                }
                break;

        }
    }
}
