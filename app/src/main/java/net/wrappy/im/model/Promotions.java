package net.wrappy.im.model;

/**
 * Created by ben on 02/01/2018.
 */

public class Promotions {

    String name;
    String time;
    int number;

    public Promotions(String name, String time, int number) {
        this.name = name;
        this.time = time;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
