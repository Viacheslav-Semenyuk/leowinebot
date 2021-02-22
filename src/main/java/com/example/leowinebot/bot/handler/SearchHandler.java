package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.MatchUser;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.MatchUserService;
import com.example.leowinebot.service.UserService;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class SearchHandler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private MatchUserService matchUserService;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Autowired
    private ProfileHandler profileHandler;


    public Message handle(Message message, User user, String chatId) {

        switch (user.getSearchStates()) {
            case ("0"):
                User userSearch = userService.findRandomUserByCriterion(user);

                if (userSearch != null && user.getLikedPerHour() <= 50) {
                    bot.executePhoto(new SendPhoto().setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                            .setCaption(userSearch.getName() + ", " + userSearch.getAge() + ", "
                                    + userSearch.getCity() + " " + userSearch.getAbout()).setPhoto(userSearch.getPhoto()));
                    user.setFoundChatIdUser(userSearch.getChatId());
                    user.setProfileEditStates("0");
                    user.setSearchStates("1");
                    user.setStates("1");
                    userService.save(user);
                } else if (user.getLikedPerHour() > 50) {
                    bot.executeMessage(new SendMessage().setChatId(chatId)
                            .setText("Слишком много лайков за последнее время – " +
                                    "ставь Мне нравится только тем, кто тебе действительно" +
                                    " нравится. Загляни к нам попозже"));
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                            .setText("1. Смотреть анкеты.\n" +
                                    "2. Моя анкета.\n" +
                                    "3. Я больше не хочу никого искать."));
                    user.setSearchStates("0");
                    user.setStates("8");
                    userService.save(user);
                } else {
                    user.setSearchStates("0");
                    user.setStates("2");
                    userService.save(user);
                    bot.executeMessage(new SendMessage().setChatId(chatId)
                            .setText("Анкету по твоему запросу не найдено"));
                    profileHandler.handle(message, user, chatId);
                }
                break;
            case ("1"):
                Emoji emoji = EmojiManager.getByUnicode(message.getText());
                if (emoji != null) {
                    switch (emoji.getAliases().get(0)) {
                        case ("heart"):
                            User foundUser = userService.findByChatId(user.getFoundChatIdUser());
                            MatchUser matchUser = new MatchUser();
                            matchUser.setChatId(chatId);
                            matchUser.setLikeChatId(foundUser.getChatId());

                            likeUser(user, matchUser, foundUser, chatId, message);

                            break;
                        case ("love_letter"):
                            user.setMessageStates("0");
                            user.setStates("10");
                            userService.save(user);
                            messageHandler.handle(message, user, chatId);
                            break;
                        case ("-1"):
                            user.setSearchStates("0");
                            user.setCountForCity(user.getCountForCity() + 1);
                            userService.save(user);
                            handle(message, user, chatId);
                            break;
                        case ("zzz"):
                            bot.executeMessage(new SendMessage()
                                    .setChatId(chatId)
                                    .setText("Подождем пока кто-то увидит твою анкету"));
                            bot.executeMessage(new SendMessage()
                                    .setChatId(chatId)
                                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                                    .setText("1. Смотреть анкеты.\n" +
                                            "2. Моя анкета.\n" +
                                            "3. Я больше не хочу никого искать."));
                            user.setSearchStates("0");
                            user.setStates("8");
                            userService.save(user);
                            break;
                        default:
                            bot.executeMessage(new SendMessage()
                                    .setChatId(chatId)
                                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                                    .setText("Нету такого варианта ответа."));

                    }
                } else {
                    bot.executeMessage(new SendMessage()
                            .setChatId(chatId)
                            .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                            .setText("Воспользуйся кнопками."));
                }
                break;
        }
        return message;
    }

    void likeUser(User user, MatchUser matchUser, User foundUser,
                  String chatId, Message message) {

        if (!matchUserService.existsByChatIdAndLikeChatId(foundUser.getChatId(), chatId)
                && !message.getText().equals("Вернуться назад")) {
            int size = matchUserService.countByLikeChatId(foundUser.getChatId()) + 1;
            foundUser.setStates("7");
            foundUser.setProfileEditStates("0");
            foundUser.setMatchStates("0");
            foundUser.setSearchStates("0");

            user.setMessageStates("0");
            user.setSearchStates("0");
            user.setCountForCity(user.getCountForCity() + 1);
            user.setLikedPerHour(user.getLikedPerHour() + 1);
            user.setStates("1");
            matchUserService.save(matchUser);
            userService.save(foundUser);
            userService.save(user);

            checkGenderSendMessage(user, foundUser, size);

            handle(message, user, chatId);
        } else {
            bot.executePhoto(new SendPhoto().setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                    .setCaption(foundUser.getName() + ", " + foundUser.getAge() + ", "
                            + foundUser.getCity() + " " + foundUser.getAbout()).setPhoto(foundUser.getPhoto()));

            user.setMessageStates("0");
            user.setSearchStates("1");
            user.setStates("1");
            userService.save(user);

        }
    }

    private void checkGenderSendMessage(User user, User foundUser, int size) {
        if (user.getGender().equals("male")) {
            log.info("CHECK GENDER From user {} to user {} ", user.getChatId(), foundUser.getChatId());
            if (foundUser.getGender().equals("female") && size == 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравилась " + size + " парню, показать его? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            } else if (foundUser.getGender().equals("male") && size == 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравился " + size + " парню, показать его? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            } else if (foundUser.getGender().equals("female") && size > 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравилась " + size + " парням, показать их? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            } else if (foundUser.getGender().equals("male") && size > 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравился " + size + " парням, показать их? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            }
        } else if (user.getGender().equals("female")) {
            if (foundUser.getGender().equals("female") && size == 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравилась " + size + " девушке, показать её? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            } else if (foundUser.getGender().equals("male") && size == 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравился " + size + " девушке, показать ее? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            } else if (foundUser.getGender().equals("female") && size > 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравилась " + size + " девушкам, показать их? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            } else if (foundUser.getGender().equals("male") && size > 1) {
                bot.executeMessage(new SendMessage()
                        .setChatId(user.getFoundChatIdUser())
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Ты понравился " + size + " девушкам, показать их? \n" + "\n" +
                                "1. Показать." + "\n" +
                                "2. Не хочу больше никого смотреть."));
            }
        }

    }

}
