package com.example.leowinebot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Setter
@Getter
@Table(name = "match")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column()
    private String chatId;

    @Column()
    private String likeChatId;

    @Column()
    private String message;

}
