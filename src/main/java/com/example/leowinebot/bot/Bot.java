package com.example.leowinebot.bot;

import com.example.leowinebot.bot.handler.CommandHandler;
import com.example.leowinebot.bot.handler.KeyboardHandler;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.MatchUserService;
import com.example.leowinebot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private static final String TOKEN = System.getenv("TOKEN");
    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");

    @Autowired
    private UserService userService;

    @Autowired
    private MatchUserService matchUserService;

    @Autowired
    private CommandHandler commandHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {

            Message message = update.getMessage();
            String chatId = Long.toString(message.getChatId());
            User user = userService.findByChatId(chatId);
            log.info("Message from user: " + chatId + " username: " + message.getFrom().getUserName() + " Text: " + message.getText());

            if (user != null && message.hasText()
                    && !message.hasPhoto() && !user.getBanned()) {
                commandHandler.handle(message, user, chatId);
            } else if (user != null && message.hasPhoto()
                    && user.getProfileEditStates().equals("7") && !message.hasText() && !user.getBanned()) {
                commandHandler.handle(message, user, chatId);
            } else if (user != null && message.hasPhoto() && !message.hasText() && !user.getBanned()) {
                executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Нет такого варианта ответа"));
            } else if (user != null && user.getBanned()) {
                executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Ой, наверное это бан :)"));
                user.setActive(false);
                userService.save(user);
            } else if (user == null) {
                executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.helloKeyboard()))
                        .setText("Я помогу найти тебе пару или просто друзей. Можно я задам тебе пару вопросов?"));
                User createUser = new User();
                createUser.setChatId(chatId);
                createUser.setUsername("@" + message.getFrom().getUserName());
                createUser.setSearchStates("0");
                createUser.setMessageStates("0");
                createUser.setMatchStates("0");
                createUser.setCountForCity(0);
                createUser.setLikedPerHour(0);
                createUser.setActive(false);
                createUser.setBanned(false);
                createUser.setStates("6");
                userService.save(createUser);
            } else {
                executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Нет такого варианта ответа"));
            }

        }

    }

    public void executePhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
            log.debug("Executed {}", sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Exception while sending message {} to user: {}", sendPhoto, sendPhoto.getChatId());
        }
    }

    public void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
            log.debug("Executed {}", sendMessage);
        } catch (TelegramApiException e) {
            userService.deleteByChatId(sendMessage.getChatId());
            matchUserService.deleteAllByChatId(sendMessage.getChatId());
            log.error("Exception while sending message {} to user: {}", sendMessage, sendMessage.getChatId());
        }
    }


    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }
}
