package com.csci310.uscdoordrink;

import java.util.ArrayList;
import java.util.List;

public class DeliveryList {
    private List<Delivery> deliveries;
    public DeliveryList() {
        deliveries = new ArrayList<Delivery>();
    }
    public DeliveryList(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }
    public List<Delivery> getDeliveries() {
        return deliveries;
    }
    public boolean anyInProgress() {
        for(Delivery delivery : deliveries) {
            if(delivery.getStatus() == DeliveryStatus.IN_PROGRESS) {
                return true;
            }
        }
        return false;
    }
}
