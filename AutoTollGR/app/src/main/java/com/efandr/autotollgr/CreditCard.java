package com.efandr.autotollgr;

public class CreditCard {

    public String cardholder;
    public String cardnumber;
    public String cardcvv;
    public String cardexpireday;
    public String userid;

    public CreditCard() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public CreditCard(String cardholder,String cardnumber,String cardcvv, String cardexpireday, String userid) {
        this.cardholder = cardholder;
        this.cardnumber = cardnumber;
        this.cardcvv = cardcvv;
        this.cardexpireday = cardexpireday;
        this.userid = userid;
    }

    public String getCardholder() {
        return cardholder;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public String getCardcvv() {
        return cardcvv;
    }

    public String getCardexpireday() {
        return cardexpireday;
    }

    public String getUserid() {
        return userid;
    }

    public void setCardholder(String cardholder) {
        this.cardholder = cardholder;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public void setCardcvv(String cardcvv) {
        this.cardcvv = cardcvv;
    }

    public void setCardexpireday(String cardexpireday) {
        this.cardexpireday = cardexpireday;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
