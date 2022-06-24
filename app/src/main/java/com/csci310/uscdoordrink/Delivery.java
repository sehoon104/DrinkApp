package com.csci310.uscdoordrink;

enum DeliveryStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,
    DELIVERED
}

public class Delivery implements java.io.Serializable {
    private Delivery(){};
    public Delivery(Order order, DeliveryStatus status, String address, SerializablePoint location) {
        this.order = order;
        this.status = status;
        this.address = address;
        this.location = location;
        this.UID = "";
    }
    private Order order;
    private DeliveryStatus status;
    private String address;
    private SerializablePoint location;
    private String UID;
    //Getters and Setters
    public String getAddress() {
        return address;
    }
    public void setAddress(String ad) { this.address = ad; }
    public SerializablePoint getLocation() {
        return location;
    }
    public void setLocation(SerializablePoint loc) { this.location = loc; }
    public Order getOrder() {
        return order;
    }
    public DeliveryStatus getStatus() {
        return status;
    }
    public void setOrder(Order order) { this.order = order; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public void setUid(String key) {
        UID = key;
    }
    public String getUID(){
        return UID;
    }
}
