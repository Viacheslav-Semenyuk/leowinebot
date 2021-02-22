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
public class CommandHandler {

    @Autowired
    private Bot bot;

    @Autowired
    private ActiveHandler activeHandler;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private MatchHandler matchHandler;

    @Autowired
    private AfterMatchHandler afterMatchHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Autowired
    private ProfileEditHandler profileEditHandler;

    @Autowired
    private ProfileAboutEditHandler profileAboutEditHandler;

    @Autowired
    private ProfilePhotoEditHandler profilePhotoEditHandler;

    @Autowired
    private ProfileHandler profileHandler;

    @Autowired
    private UserService userService;


    public void handle(Message message, User user, String chatId) {

        if (message.getText() != null && message.getText().equals("/start") && !message.hasPhoto()) {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Мы тебя помним!"));

            user.setStates("2");
            user.setSearchStates("0");
            user.setCountForCity(0);
            user.setMatchStates("0");
            user.setActive(true);
            user.setProfileEditStates("0");
            userService.save(user);
        }

        switch (user.getStates()) {
            case ("1"):
                searchHandler.handle(message, user, chatId);
                break;
            case ("2"):
                profileHandler.handle(message, user, chatId);
                break;
            case ("3"):
                profileEditHandler.handle(message, user, chatId);
                break;
            case ("4"):
                profileAboutEditHandler.handle(message, user, chatId);
                break;
            case ("5"):
                profilePhotoEditHandler.handle(message, user, chatId);
                break;
            case ("6"):
                if (message.getText().startsWith("Да") && !message.hasPhoto()) {
                    user.setStates("3");
                    user.setProfileEditStates("0");
                    userService.save(user);
                    profileEditHandler.handle(message, user, chatId);
                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.helloKeyboard()))
                            .setText("Нажми на кнопку"));
                }
                break;
            case ("7"):
                matchHandler.handle(message, user, chatId);
                break;
            case ("8"):
                afterMatchHandler.handle(message, user, chatId);
                break;
            case ("9"):
                activeHandler.handle(message, user, chatId);
                break;
            case ("10"):
                messageHandler.handle(message, user, chatId);
                break;

        }

    }
}
