package com.csci310.uscdoordrink;

import java.util.HashMap;
import java.util.Map;

public class User {
    public volatile Boolean isInvalid = false;
    private String name;
    private Boolean isCustomer;
    private String uid;
    private String phoneNumber;
    private String email;
    private int balance;
    private String address;
    private SerializablePoint location;

    public SerializablePoint getLocation() {return location;}
    public void setLocation(SerializablePoint m){location = m;}

    public User() {
        isInvalid = true;
    }

    public User(String name, String UID, String email, int balance, Boolean isCustomer){
        this.name = name;
        this.uid = UID;
        this.email = email;
        this.balance = balance;
        this.isCustomer = isCustomer;
        this.location = new SerializablePoint(0, 0);
        address = "";
    }

    public String getName(){return name;}
    public void setName(String z){name = z;}

    public String getAddress(){return address;}
    public void setAddress(String z){address = z;}

    public String getEmail(){return email;}
    public void setEmail(String z){email = z;}

    public Boolean getIsCustomer(){return isCustomer;}
    public void setIsCustomer(Boolean z){isCustomer = z;}

    public String getPhoneNumber(){return phoneNumber;}
    public void setPhoneNumber(String z){phoneNumber = z;}

    public int getBalance(){return balance;}
    public void setBalance(int m){balance = m;}

    public String getUid() {return uid;}
    public void setUid(String m){return;}

}
