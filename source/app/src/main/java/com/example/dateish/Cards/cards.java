package com.example.dateish.Cards;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class cards {
    private String userId;
    private String name;
    private String profileImageUrl;
    private long age;

    public cards(String userId, String name, String birthDate, String profileImageUrl){
        this.userId = userId;
        this.name = name;
        this.age = calculateAge(birthDate);
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public long getAge(){
        return age;
    }
    public void setAge(String date){
        this.age = calculateAge(date);
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl){ this.profileImageUrl = profileImageUrl; }

    private long calculateAge(String birthDate){
        return ChronoUnit.YEARS.between(LocalDate.parse((CharSequence) birthDate), LocalDate.now());
    }
}
