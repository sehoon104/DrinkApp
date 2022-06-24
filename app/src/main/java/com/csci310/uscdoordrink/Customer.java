package com.csci310.uscdoordrink;

public class Customer extends User{
    private int caffeineIntake;
    private String deliveries;
//one string for a delivery in progress
    Customer(){//makes an invalid user
        super();
    }

    Customer(String name, String UID, String email, int balance){
        super(name, UID, email, balance, true);
        caffeineIntake = 0;
    }

    public int getCaffeineIntake() { return caffeineIntake; }
    public void resetCaffeineIntake(){ caffeineIntake = 0; }
    public void setCaffeineIntake(int c){ caffeineIntake = c; }
    public String getDeliveries() { return deliveries; }
    public void setDeliveries(String deliveries) { this.deliveries = deliveries; }
}
