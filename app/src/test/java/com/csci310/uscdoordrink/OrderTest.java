package com.csci310.uscdoordrink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderTest{

    @Test
    public void testGetCaffeineIntake() {
        //No drinks and 0 caffeine
        List<Drink> testDrinks = new ArrayList<Drink>();
        Order toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 100, "test merchant");
        assertEquals(0, toTest.getCaffeineIntake());

        //Drinks but 0 caffeine
        testDrinks = new ArrayList<Drink>();
        testDrinks.add(new Drink(1, "0 caffeine drink", "", true, 100, 0));
        testDrinks.add(new Drink(2, "0 caffeine drink", "", true, 100, 0));
        toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 100, "test merchant");
        assertEquals(0, toTest.getCaffeineIntake());

        //One Drink nonzero caffeine
        testDrinks = new ArrayList<Drink>();
        testDrinks.add(new Drink(1, "0 caffeine drink", "", true, 100, 99));
        toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 100, "test merchant");
        assertEquals(99, toTest.getCaffeineIntake());

        //Many Drinks nonzero caffeine
        testDrinks = new ArrayList<Drink>();
        testDrinks.add(new Drink(1, "0 caffeine drink", "", true, 100, 33));
        testDrinks.add(new Drink(2, "0 caffeine drink", "", true, 100, 66));
        toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 100, "test merchant");
        assertEquals(99, toTest.getCaffeineIntake());
    }

    @Test
    public void testGetduration() throws InterruptedException {
        //0 duration
        List<Drink> testDrinks = new ArrayList<Drink>();
        Order toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 0, "test merchant");
        long d = toTest.getduration();
        assertEquals(0, d);

        //nonzero duration
        testDrinks = new ArrayList<Drink>();
        toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 190, "test merchant");
        d = toTest.getduration();
        assertEquals(190, d);

        //altered duration (Used for plotscreen)
        //this can fail during debugging due to the realtime nature
        testDrinks = new ArrayList<Drink>();
        toTest = new Order(100, "a", "a", testDrinks, OrderStatus.SENT, 190, "test merchant");
        Thread.sleep(300, 0);
        toTest.setETA(new Date(System.currentTimeMillis()));
        d = toTest.getduration();
        boolean check = (280 <= d)  && (d <= 320);
        assertEquals(true, check);
    }
}