package com.example.leowinebot.bot.handler;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardHandler {

    private ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    private ArrayList<KeyboardRow> keyboard = new ArrayList<>();
    private KeyboardRow keyboardFirstRow = new KeyboardRow();


    public ReplyKeyboardMarkup handle(List<String> params) {

        keyboard.clear();
        keyboardFirstRow.clear();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        keyboardFirstRow.addAll(params);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    List<String> profileKeyboard() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        return list;
    }

    List<String> searchKeyboard() {
        List<String> list = new ArrayList<>();
        list.add(EmojiParser.parseToUnicode(":heart:"));
        list.add(EmojiParser.parseToUnicode(":love_letter:"));
        list.add(EmojiParser.parseToUnicode(":-1:"));
        list.add(EmojiParser.parseToUnicode(":zzz:"));
        return list;
    }

    List<String> threeKeyboard() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        return list;
    }
    List<String> searchGenderKeyboard() {
        List<String> list = new ArrayList<>();
        list.add("Девушки");
        list.add("Парни");
        list.add("Все равно");
        return list;
    }

    List<String> twoKeyboard() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        return list;
    }

    List<String> oneKeyboard() {
        List<String> list = new ArrayList<>();
        list.add("1");
        return list;
    }

    List<String> matchKeyboard() {
        List<String> list = new ArrayList<>();
        list.add(EmojiParser.parseToUnicode(":heart:"));
        list.add(EmojiParser.parseToUnicode(":-1:"));
        return list;
    }

    List<String> editKeyboard(String text) {
        List<String> list = new ArrayList<>();
        list.add(text);
        return list;
    }

    public List<String> helloKeyboard() {
        List<String> list = new ArrayList<>();
        list.add("Да");
        return list;
    }

}
