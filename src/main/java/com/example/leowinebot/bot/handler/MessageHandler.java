package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.MatchUser;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageHandler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public Message handle(Message message, User user, String chatId) {

        switch (user.getMessageStates()) {
            case ("0"):
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.editKeyboard("Вернуться назад")))
                        .setText("Напиши сообщение для этого пользователя"));

                user.setMessageStates("1");
                userService.save(user);
                break;
            case ("1"):
                User foundUser = userService.findByChatId(user.getFoundChatIdUser());
                MatchUser matchUser = new MatchUser();
                matchUser.setChatId(foundUser.getChatId());
                matchUser.setLikeChatId(chatId);
                matchUser.setMessage(message.getText());
                searchHandler.likeUser(user, matchUser, foundUser, chatId, message);

                break;
        }

        return message;
    }
}
