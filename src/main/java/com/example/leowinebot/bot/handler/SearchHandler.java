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

@Component
@Slf4j
public class SearchHandler implements Handler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    @Autowired
    private ProfileHandler profileHandler;


    public void handle(Message message, User user, String chatId) {

        if (user.getSearchStates().equals("0")) {

            User userSearch = userService.findRandomUserByCriterion(user);

            if (userSearch != null && user.getLikedPerHour() <= 35) {
                bot.executePhoto(new SendPhoto().setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                        .setCaption(userSearch.getName() + ", " + userSearch.getAge() + ", "
                                + userSearch.getCity() + " " + userSearch.getAbout()).setPhoto(userSearch.getPhoto()));
                user.setFoundChatIdUser(userSearch.getChatId());
                user.setProfileEditStates("0");
                user.setSearchStates("1");
                user.setUserStates("search");
                userService.save(user);
            } else if (user.getLikedPerHour() > 35) {
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
                user.setUserStates("afterMatch");
                userService.save(user);
            } else {
                user.setSearchStates("0");
                user.setUserStates("profile");
                userService.save(user);
                bot.executeMessage(new SendMessage().setChatId(chatId)
                        .setText("Анкет по твоему запросу не найдено"));
                profileHandler.handle(message, user, chatId);
            }

        } else if (user.getSearchStates().equals("1")) {
            if (EmojiManager.getByUnicode(message.getText()) != null
                    && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("heart")
                    || message.getText().equals("1")) {

                User foundUser = userService.findByChatId(user.getFoundChatIdUser());
                Match match = new Match();
                match.setChatId(chatId);
                match.setLikeChatId(foundUser.getChatId());

                clickedHeart(user, match, foundUser, chatId, message);

            } else if (EmojiManager.getByUnicode(message.getText()) != null
                    && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("love_letter")
                    || message.getText().equals("2")) {

                user.setMessageStates("0");
                user.setUserStates("message");
                userService.save(user);
                messageHandler.handle(message, user, chatId);
            } else if (EmojiManager.getByUnicode(message.getText()) != null
                    && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("-1")
                    || message.getText().equals("3")) {

                user.setSearchStates("0");
                user.setCountForCity(user.getCountForCity() + 1);
                userService.save(user);
                handle(message, user, chatId);
            } else if (EmojiManager.getByUnicode(message.getText()) != null
                    && EmojiManager.getByUnicode(message.getText()).getAliases().get(0).equals("zzz")
                    || message.getText().equals("4")) {

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
                user.setUserStates("afterMatch");
                userService.save(user);
            } else {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                        .setText("Воспользуйся кнопками."));
            }

        }

    }

    void clickedHeart(User user, Match match, User foundUser,
                      String chatId, Message message) {

        if (!matchService.existsByChatIdAndLikeChatId(foundUser.getChatId(), chatId)
                && !message.getText().equals("Вернуться назад")) {
            int size = matchService.countByLikeChatId(foundUser.getChatId()) + 1;
            foundUser.setUserStates("match");
            foundUser.setProfileEditStates("0");
            foundUser.setMatchStates("0");
            foundUser.setSearchStates("0");

            user.setMessageStates("0");
            user.setSearchStates("0");
            user.setCountForCity(user.getCountForCity() + 1);
            user.setLikedPerHour(user.getLikedPerHour() + 1);
            user.setUserStates("search");
            matchService.save(match);
            userService.save(foundUser);
            userService.save(user);

            checkGender(user, foundUser, size);

            handle(message, user, chatId);
        } else {
            bot.executePhoto(new SendPhoto().setChatId(chatId)
                    .setReplyMarkup(keyboardHandler.handle(keyboardHandler.searchKeyboard()))
                    .setCaption(foundUser.getName() + ", " + foundUser.getAge() + ", "
                            + foundUser.getCity() + " " + foundUser.getAbout()).setPhoto(foundUser.getPhoto()));

            user.setMessageStates("0");
            user.setSearchStates("1");
            user.setUserStates("search");
            userService.save(user);

        }
    }

    private void checkGender(User user, User foundUser, int size) {
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
