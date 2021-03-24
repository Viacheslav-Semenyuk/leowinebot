package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.Match;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MessageHandler implements Handler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public void handle(Message message, User user, String chatId) {

        if (user.getMessageStates().equals("0")) {

            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.editKeyboard("Вернуться назад")))
                    .setText("Напиши сообщение для этого пользователя"));

            user.setMessageStates("1");
            userService.save(user);
        } else if (user.getMessageStates().equals("1")) {

            User foundUser = userService.findByChatId(user.getFoundChatIdUser());
            Match match = new Match();
            match.setChatId(foundUser.getChatId());
            match.setLikeChatId(chatId);
            match.setMessage(message.getText());
            searchHandler.clickedHeart(user, match, foundUser, chatId, message);


        }

    }

    @Override
    public boolean test(String o) {
        return "message".equals(o);
    }
}
