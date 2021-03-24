package com.example.leowinebot.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Setter
@Getter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userId_generator")
    @SequenceGenerator(name = "userId_generator", allocationSize = 1, initialValue = 63601)
    private Long id;

    @Column
    @Unique
    private String chatId;

    @Column
    private String username;

    @Column
    private String photo;

    @Column
    private String name;

    @Column
    private Integer age;

    @Column
    private String gender;

    @Column
    private String searchGender;

    @Column
    private String city;

    @Column
    private String adminNameCity;

    @Column
    @Size(max = 4000)
    private String about;

    @Column
    private String userStates;

    @Column
    private String profileEditStates;

    @Column
    private String searchStates;

    @Column
    private String messageStates;

    @Column
    private String foundChatIdUser;

    @Column
    private String matchStates;

    @Column
    private Integer likedPerHour;

    @Column
    private Integer countForCity;

    @Column
    private Boolean active;

    @Column
    private Boolean banned;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
