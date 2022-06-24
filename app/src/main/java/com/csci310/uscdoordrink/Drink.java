package com.csci310.uscdoordrink;

import com.google.gson.JsonObject;

import java.io.*;
import java.util.UUID;

public class Drink implements java.io.Serializable {
    private Integer uuid;
    private String name;
    private String description;
    private boolean isTea;
    private int price;
    private int caffeineContent;
    private int quantity;
    private int discount;

    //Getters and Setters For serialization. DO NOT MODIFY
    public Integer getUuid() {
        return uuid;
    }
    public void setUuid(Integer uuid) {
        this.uuid = uuid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isTea() {
        return isTea;
    }
    public void setTea(boolean tea) {
        isTea = tea;
    }
    public boolean getIsTea() {
        return isTea;
    }
    public void setIsTea(boolean tea) {
        isTea = tea;
    }
    public int getPrice() { return price; }
    public void setPrice(int price) {
        this.price = price;
    }
    public int getCaffeineContent() {
        return caffeineContent;
    }
    public void setCaffeineContent(int caffeineContent) {
        this.caffeineContent = caffeineContent;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quant) { quantity = quant;}

    public int getDiscount() {
        return discount;
    }
    public void setDiscount(int d) { discount = d;}

    private Drink(){}

    void updateName(String newName) {
        name = newName;
    }

    void updateDescription(String newDescription) {
        description = newDescription;
    }

    void updateDrinkType(boolean tea) {
        isTea = tea;
    }//argument is true if drink is a tea

    void updatePrice(int newPrice) {
        price = newPrice;
    }

    void updateCaffeine(int newCaffeine) {
        caffeineContent = newCaffeine;
    }

    void increaseQuantity() {
        quantity++;
    }

    void decreaseQuantity() {
        quantity--;
    }

    //USE THIS FOR SETTING NEW QUANTITY FOR DRINKS
    public void setnewquantity(int quantity) {
        int oldprice = this.price/this.quantity;
        this.quantity = quantity;
        this.price = oldprice * quantity;
    }
    //USED FOR CALCULATING CAFFEINE ALERT
    public int getTotalCaffeineIntake() {
        return quantity*caffeineContent;
    }
    public int getTotalPrice() {return quantity*price;}

    Drink(Integer uui, String name, String description, boolean tea, int price, int caffeine) {
        uuid = uui;
        this.name = name;
        this.description = description;
        this.isTea = tea;
        this.price = price;
        caffeineContent = caffeine;
        this.quantity = 1;
    }

    Drink(Integer uui, String name, String description, boolean tea, int price, int discount, int caffeine) {
        uuid = uui;
        this.name = name;
        this.description = description;
        this.isTea = tea;
        this.price = price;
        this.discount = discount;
        caffeineContent = caffeine;
        this.quantity = 1;
    }

    Drink(Integer uui, String name, String description, boolean tea, int price, int discount, int caffeine, int quantity) {
        uuid = uui;
        this.name = name;
        this.description = description;
        this.isTea = tea;
        this.price = price;
        this.discount = discount;
        this.caffeineContent = caffeine;
        this.quantity = quantity;
    }

    Drink(JsonObject data) {
        this(data.get("UUID").getAsInt(), data.get("name").getAsString(), data.get("description").getAsString(), data.get("isTea").getAsBoolean(), data.get("price").getAsInt(), data.get("discount").getAsInt(), data.get("caffeineContent").getAsInt(), data.get("quantity").getAsInt());
    }
}
