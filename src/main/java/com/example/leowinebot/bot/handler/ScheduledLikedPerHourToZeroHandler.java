package com.example.leowinebot.bot.handler;

import com.example.leowinebot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class ScheduledLikedPerHourToZeroHandler {

    @Autowired
    private UserService userService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 3600000)
    public void changeCountCurrentTime() {
        userService.saveAllLikedPerHour();
        log.info("Save liked per hour to zero {}", dateFormat.format(new Date()));
    }
}
