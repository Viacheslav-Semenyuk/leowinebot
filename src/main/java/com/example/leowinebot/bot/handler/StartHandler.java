package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartHandler implements Handler {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileEditHandler profileEditHandler;

    @Autowired
    private Bot bot;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Override
    public void handle(Message message, User user, String chatId) {
        if (message.getText().startsWith("Да") && !message.hasPhoto()) {
            user.setUserStates("profileEdit");
            user.setProfileEditStates("0");
            userService.save(user);
            profileEditHandler.handle(message, user, chatId);
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.helloKeyboard()))
                    .setText("Нажми на кнопку"));
        }
    }

    @Override
    public boolean test(String o) {
        return "start".equals(o);
    }
}
