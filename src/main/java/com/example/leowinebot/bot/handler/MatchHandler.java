package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.Match;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.MatchService;
import com.example.leowinebot.service.UserService;
import com.vdurmont.emoji.EmojiManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class MatchHandler implements Handler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public void handle(Message message, User user, String chatId) {

        if (matchService.existsByLikeChatId(chatId)) {
            Match match = matchService.findByLikeChatId(chatId);
            User foundUser = userService.findByChatId(match.getChatId());
            if (foundUser == null) {
                matchService.deleteAllByChatId(match.getChatId());
            }
            assert foundUser != null;
            if (user.getMatchStates().equals("0")) {
                if (message.getText().equals("1")) {

                    sendProfile(chatId, foundUser, match);

                    user.setActive(true);
                    user.setUserStates("match");
                    user.setMatchStates("1");
                    userService.save(user);

                } else if (message.getText().equals("2")) {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                            .setText("Так ты не узнаешь, что кому-то нравишься... Точно хочешь отключить свою анкету?\n" +
                                    "\n" +
                                    "1. Да, отключить анкету.\n" +
                                    "2. Нет, вернуться назад."));
                    user.setUserStates("active");
                    userService.save(user);

                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Нет такого варианта ответа"));
                }
            } else if (user.getMatchStates().equals("1")) {

                if (EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("heart")
                        && EmojiManager.getByUnicode(message.getText()) != null
                        || message.getText().equals("1")) {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText("Отлично! Надесюь хорошо проведете время ;) добавляй в друзья "
                                    + "[" + foundUser.getName() + "](tg://user?id=" + foundUser.getChatId() + ")")
                            .enableMarkdown(true));

                    bot.executeMessage(new SendMessage()
                            .setChatId(foundUser.getChatId())
                            .setText("Есть взаимная симпатия! Добавляй в друзья "
                                    + "[" + user.getName() + "](tg://user?id=" + user.getChatId() + ")")
                            .enableMarkdown(true));
                    bot.executePhoto(new SendPhoto().setChatId(foundUser.getChatId())
                            .setCaption(user.getName() + ", " + user.getAge() + ", "
                                    + user.getCity() + " " + user.getAbout()).setPhoto(user.getPhoto()));
                    bot.executeMessage(new SendMessage()
                            .setChatId(foundUser.getChatId())
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                            .setText("1. Смотреть анкеты.\n" +
                                    "2. Моя анкета.\n" +
                                    "3. Я больше не хочу никого искать."));

                    deleteMatch(chatId, user, foundUser);
                    foundUser.setUserStates("afterMatch");
                    foundUser.setMatchStates("0");
                    foundUser.setSearchStates("0");
                    userService.save(foundUser);

                } else if (EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("-1")
                        && EmojiManager.getByUnicode(message.getText()) != null
                        || message.getText().equals("2")) {

                    deleteMatch(chatId, user, foundUser);

                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.matchKeyboard()))
                            .setText("Воспользуйся кнопками."));

                }
            } else {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Нет такого варианта ответа"));
            }
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                    .setText("1. Смотреть анкеты.\n" +
                            "2. Моя анкета.\n" +
                            "3. Я больше не хочу никого искать."));
            user.setUserStates("afterMatch");
            user.setMatchStates("0");
            user.setSearchStates("0");
            userService.save(user);
        }
    }

    private void deleteMatch(String chatId, User user, User foundUser) {
        matchService.deleteByChatIdAndLikeChatId(foundUser.getChatId(), chatId);
        if (matchService.countByLikeChatId(chatId) != 0) {
            Match newMatch = matchService.findByLikeChatId(chatId);
            User newFoundUser = userService.findByChatId(newMatch.getChatId());
            sendProfile(chatId, newFoundUser, newMatch);
            user.setUserStates("match");
            user.setMatchStates("1");
            user.setSearchStates("0");
            userService.save(user);
        } else {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                    .setText("1. Смотреть анкеты.\n" +
                            "2. Моя анкета.\n" +
                            "3. Я больше не хочу никого искать."));
            user.setUserStates("afterMatch");
            user.setMatchStates("0");
            user.setSearchStates("0");
            userService.save(user);
        }
    }

    private void sendProfile(String chatId, User foundUser, Match msgUser) {
        if (msgUser.getMessage() != null) {
            bot.executePhoto(new SendPhoto().setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.matchKeyboard()))
                    .setCaption("Кому-то понравилась твоя анкета: \n" + "\n"
                            + foundUser.getName() + ", " + foundUser.getAge() + ", "
                            + foundUser.getCity() + " " + foundUser.getAbout()
                            + "\n\nСообщения для тебя: \n"
                            + msgUser.getMessage()).setPhoto(foundUser.getPhoto())
            );
        } else {
            bot.executePhoto(new SendPhoto().setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.matchKeyboard()))
                    .setCaption("Кому-то понравилась твоя анкета: \n" + "\n"
                            + foundUser.getName() + ", " + foundUser.getAge() + ", "
                            + foundUser.getCity() + " " + foundUser.getAbout())
                    .setPhoto(foundUser.getPhoto())
            );
        }
    }

    @Override
    public boolean test(String o) {
        return "match".equals(o);
    }
}
