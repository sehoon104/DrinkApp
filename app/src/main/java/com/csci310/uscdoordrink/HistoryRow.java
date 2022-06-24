package com.csci310.uscdoordrink;

import java.text.SimpleDateFormat;

public class HistoryRow {
    @Override
    public String toString() {
        return "HistoryRow{" +
                "ordertime='" + ordertime + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    private String ordertime;
    private String address;

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    private boolean expandable;

    public String getOrdertime() {
        return ordertime;
    }

    public void setOrdertime(String ordertime) {
        this.ordertime = ordertime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;
    public HistoryRow(Delivery delivery) {
        ordertime = "Order Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(delivery.getOrder().getTimestamp());
        address = "Address: " + delivery.getAddress();
        String descriptions = "Drinks: ";
        for (Drink drink : delivery.getOrder().getDrinks()){
            descriptions += "x" + drink.getQuantity() + " " + drink.getName() + ", ";
        }
        descriptions = descriptions.substring(0, descriptions.length() - 2);
        description = descriptions;
        expandable = false;
    }
}
