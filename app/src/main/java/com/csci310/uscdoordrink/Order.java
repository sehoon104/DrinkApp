package com.csci310.uscdoordrink;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

enum OrderStatus {
    BUILDING,
    SENT,
    FAILED
}


public class Order implements java.io.Serializable {
    public Order(){}

    public Order(int cost, String merchantUUID, String customerUUID, List<Drink> drinks, OrderStatus status, long ETAmillis, String nameofmerchant) {
        this.cost = cost;
        this.merchantUUID = merchantUUID;
        this.customerUUID = customerUUID;
        this.drinks = drinks;
        this.status = status;
        Date date = new Date();
        this.timestamp = new Date(System.currentTimeMillis());
        this.ETA = date;
        this.ETA.setTime(timestamp.getTime()+ETAmillis);
        this.uuid = new UUID(Calendar.getInstance().getTimeInMillis(), Calendar.getInstance().getTimeInMillis()).toString();
        MerchantName = nameofmerchant;
    }

    private String uuid;
    private int cost;
    private String merchantUUID;
    private String customerUUID;
    private List<Drink> drinks;
    private OrderStatus status;
    private Date timestamp;
    private Date ETA;
    private String MerchantName;

    //Getters and Setters
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public void setCost(int cost) {
        this.cost = cost;
    }
    public String getMerchantUUID() {
        return merchantUUID;
    }
    public void setMerchantUUID(String merchantUUID) {
        this.merchantUUID = merchantUUID;
    }
    public String getCustomerUUID() {
        return customerUUID;
    }
    public void setCustomerUUID(String customerUUID) {
        this.customerUUID = customerUUID;
    }
    public List<Drink> getDrinks() {
        return drinks;
    }
    public void setDrinks(List<Drink> drinks) {
        this.drinks = drinks;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public void setETA(Date ETA) {
        this.ETA = ETA;
    }
    public Date getETA() {
        return ETA;
    }
    public String getMerchantName() { return MerchantName; }
    public void setMerchantName(String merchantName) { MerchantName = merchantName; }

    public int getCost() {
        return cost;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void addToOrder(Drink drink) {
        drinks.add(drink);
    }

    public void removeFromOrder(Drink drink) {
        drinks.remove(drink);
    }

    public int getCaffeineIntake() {
        int totalcaffeine = 0;
        for (Drink drink : drinks) {
            totalcaffeine += (drink.getCaffeineContent()*drink.getQuantity());
        }
        return totalcaffeine;
    }

    public long getduration() {
        long diff = getETA().getTime() - getTimestamp().getTime();
        return TimeUnit.MILLISECONDS.convert(diff, TimeUnit.MILLISECONDS);
    }

}
