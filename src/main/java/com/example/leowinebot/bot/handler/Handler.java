package com.example.leowinebot.bot.handler;

import com.example.leowinebot.entity.User;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Predicate;

public interface Handler extends Predicate<String> {

    void handle(Message message, User user, String chatId);

}
