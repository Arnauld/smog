package org.technbolts.smog.js.test;

public class JPlayer {

    public static final int PLAYER_COUNT = 0;

    private String name;
    public int age;
    private String gender;

    public JPlayer(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setTheAge(int newAge) {
        this.age = newAge;
    }
}
