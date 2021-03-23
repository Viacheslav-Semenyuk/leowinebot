package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.MatchUser;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.MatchUserService;
import com.example.leowinebot.service.UserService;
import com.vdurmont.emoji.EmojiManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Slf4j
public class SearchHandler implements Handler {

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


    public void handle(Message message, User user, String chatId) {

        if (user.getSearchStates().equals("0")) {

            User userSearch = userService.findRandomUserByCriterion(user);

            if (userSearch != null && user.getLikedPerHour() <= 50) {
                bot.executePhoto(new SendPhoto().setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                        .setCaption(userSearch.getName() + ", " + userSearch.getAge() + ", "
                                + userSearch.getCity() + " " + userSearch.getAbout()).setPhoto(userSearch.getPhoto()));
                user.setFoundChatIdUser(userSearch.getChatId());
                user.setProfileEditStates("0");
                user.setSearchStates("1");
                user.setStates("search");
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
                user.setStates("afterMatch");
                userService.save(user);
            } else {
                user.setSearchStates("0");
                user.setStates("profile");
                userService.save(user);
                bot.executeMessage(new SendMessage().setChatId(chatId)
                        .setText("Анкету по твоему запросу не найдено"));
                profileHandler.handle(message, user, chatId);
            }

        } else if (user.getSearchStates().equals("1")) {
            if (message.getText().equals("1") ||
                    EmojiManager.getByUnicode(message.getText()) != null
                            && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("heart")) {

                User foundUser = userService.findByChatId(user.getFoundChatIdUser());
                MatchUser matchUser = new MatchUser();
                matchUser.setChatId(chatId);
                matchUser.setLikeChatId(foundUser.getChatId());

                likeUser(user, matchUser, foundUser, chatId, message);

            } else if (message.getText().equals("2") ||
                    EmojiManager.getByUnicode(message.getText()) != null
                            && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("love_letter")) {

                user.setMessageStates("0");
                user.setStates("message");
                userService.save(user);
                messageHandler.handle(message, user, chatId);
            } else if (message.getText().equals("3") ||
                    EmojiManager.getByUnicode(message.getText()) != null
                            && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("-1")) {

                user.setSearchStates("0");
                user.setCountForCity(user.getCountForCity() + 1);
                userService.save(user);
                handle(message, user, chatId);
            } else if (message.getText().equals("4") ||
                    EmojiManager.getByUnicode(message.getText()) != null
                            && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("zzz")) {

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
                user.setStates("afterMatch");
                userService.save(user);
            } else {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                        .setText("Воспользуйся кнопками."));
            }

        }

    }

    void likeUser(User user, MatchUser matchUser, User foundUser,
                  String chatId, Message message) {

        if (!matchUserService.existsByChatIdAndLikeChatId(foundUser.getChatId(), chatId)
                && !message.getText().equals("Вернуться назад")) {
            int size = matchUserService.countByLikeChatId(foundUser.getChatId()) + 1;
            foundUser.setStates("match");
            foundUser.setProfileEditStates("0");
            foundUser.setMatchStates("0");
            foundUser.setSearchStates("0");

            user.setMessageStates("0");
            user.setSearchStates("0");
            user.setCountForCity(user.getCountForCity() + 1);
            user.setLikedPerHour(user.getLikedPerHour() + 1);
            user.setStates("search");
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
            user.setStates("search");
            userService.save(user);

        }
    }

    private void checkGenderSendMessage(User user, User foundUser, int size) {
        if (user.getGender().equals("male")) {
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

    @Override
    public boolean test(String o) {
        return "search".equals(o);
    }
}
