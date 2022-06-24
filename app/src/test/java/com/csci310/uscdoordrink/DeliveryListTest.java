package com.csci310.uscdoordrink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeliveryListTest{

    @Test
    public void testAnyInProgress() {
        ArrayList<Delivery> initial = new ArrayList<Delivery>();
        List<Drink> baseDrinks = new ArrayList<Drink>();
        DeliveryList testList;

        baseDrinks.add(new Drink(1, "test drink", "", true, 100, 100, 100));
        Order baseOrder = new Order(100, "merchantID", "customerID", baseDrinks, OrderStatus.SENT, 1, "test merchant");
        Delivery baseDelivery = new Delivery(baseOrder, DeliveryStatus.DELIVERED, "Venice Beach", new SerializablePoint(0, 0));

        //one delivery none in progress
        initial.add(baseDelivery);
        testList = new DeliveryList(initial);
        assertEquals(false, testList.anyInProgress());

        //two deliveries none in progress
        baseDelivery.setStatus(DeliveryStatus.ACCEPTED);
        initial.add(baseDelivery);
        testList = new DeliveryList(initial);
        assertEquals(false, testList.anyInProgress());

        //three deliveries one in progress
        baseDelivery.setStatus(DeliveryStatus.IN_PROGRESS);
        initial.add(baseDelivery);
        testList = new DeliveryList(initial);
        assertEquals(true, testList.anyInProgress());
    }

    @Test
    public void verifyDeliveryListOrders(){
        ArrayList<Delivery> initial = new ArrayList<Delivery>();
        List<Drink> baseDrinks = new ArrayList<Drink>();
        List<Drink> secondBaseDrinks = new ArrayList<Drink>();
        DeliveryList testList;

        baseDrinks.add(new Drink(1, "test drink", "", true, 100, 100, 100));
        Order baseOrder = new Order(100, "merchantID", "customerID", baseDrinks, OrderStatus.SENT, 1, "test merchant");
        Delivery baseDelivery = new Delivery(baseOrder, DeliveryStatus.DELIVERED, "Venice Beach", new SerializablePoint(0, 0));

        initial.add(baseDelivery);

        testList = new DeliveryList(initial);

        List<Delivery> res = testList.getDeliveries();

        //test with only one drink
        assertEquals(1, res.get(0).getOrder().getDrinks().size());
        assertEquals(new Integer(1), res.get(0).getOrder().getDrinks().get(0).getUuid());
        assertEquals(100, res.get(0).getOrder().getCost());

        secondBaseDrinks.add(new Drink(1, "test drink", "", true, 100, 100, 100));
        Order secondBaseOrder = new Order(100, "merchantID", "customerID", secondBaseDrinks, OrderStatus.SENT, 1, "test merchant");
        Delivery secondBaseDelivery = new Delivery(baseOrder, DeliveryStatus.DELIVERED, "Venice Beach", new SerializablePoint(0, 0));

        secondBaseOrder.addToOrder(new Drink(2, "second drink", "", false, 100, 100));
        secondBaseDelivery.setOrder(secondBaseOrder);
        initial.add(secondBaseDelivery);
        testList = new DeliveryList(initial);

        res = testList.getDeliveries();

        //test with two orders and two drinks
        assertEquals(2, res.size());

        assertEquals(1, res.get(0).getOrder().getDrinks().size());
        assertEquals(new Integer(1), res.get(0).getOrder().getDrinks().get(0).getUuid());
        assertEquals(100, res.get(0).getOrder().getCost());

        assertEquals(2, res.get(1).getOrder().getDrinks().size());
        assertEquals(new Integer(2), res.get(1).getOrder().getDrinks().get(1).getUuid());
        assertEquals(100, res.get(1).getOrder().getCost());
    }
}