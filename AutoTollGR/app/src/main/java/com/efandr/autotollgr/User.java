package com.efandr.autotollgr;

public class User {

    public String email;
    public String username;
    public String licenceplate;
    public String vehicletype;
    public String userid;
    public String password;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email,String username,String licenceplate, String vehicletype, String userid, String password) {
        this.email = email;
        this.username = username;
        this.licenceplate = licenceplate;
        this.vehicletype = vehicletype;
        this.userid = userid;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLicenceplate(String licenceplate) {
        this.licenceplate = licenceplate;
    }

    public void setVehicletype(String vehicletype) {
        this.vehicletype = vehicletype;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getLicenceplate() {
        return licenceplate;
    }

    public String getVehicletype() {
        return vehicletype;
    }
}
