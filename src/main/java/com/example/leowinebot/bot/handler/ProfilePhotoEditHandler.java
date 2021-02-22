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
public class ProfilePhotoEditHandler {

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
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler
                                .editKeyboard("Оставить текущее")))
                        .setText("Теперь пришли свое фото, \n" +
                                " его будут видеть другие пользователи"));
                user.setProfileEditStates("7");
                userService.save(user);
                break;
            case ("7"):
                if (message.getText().equals("Оставить текущее")) {
                    user.setPhoto(user.getPhoto());
                    user.setActive(true);
                    user.setStates("2");
                    user.setProfileEditStates("0");
                    userService.save(user);
                    profileHandler.handle(message, user, chatId);
                } else if (message.hasPhoto() && !message.hasText() && !message.hasAudio()) {
                    user.setActive(true);
                    user.setPhoto(message.getPhoto().get(0).getFileId());
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

}
