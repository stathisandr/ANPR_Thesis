package com.efandr.autotollgr;

public class Purchase {

    public String day;
    public float cost;
    public String time;

    public Purchase() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Purchase(String day,float cost,String time) {
        this.day = day;
        this.cost = cost;
        this.time = time;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public float getCost() {
        return cost;
    }

    public String getTime() {
        return time;
    }
}
