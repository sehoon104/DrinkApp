package com.csci310.uscdoordrink;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;

@RunWith(JUnit4.class)
public class PlotScreenTest {

    @Test
    public void testRecommendedDrink(){
        HashMap<String, Integer> Drink_Data = new HashMap<>();
        Drink_Data.put("Americano", 3);
        Drink_Data.put("Green Tea", 4);
        Drink_Data.put("Espresso", 5);
        String testval = PlotScreen.RecommendedDrink(Drink_Data);
        String expected = "Espresso";
        assertEquals("Espresso", testval);
        Drink_Data.clear();
        Drink_Data.put("Hewwoo", 0);
        testval = PlotScreen.RecommendedDrink(Drink_Data);
        assertEquals("Hewwoo",testval);
        Drink_Data.clear();
        testval = PlotScreen.RecommendedDrink(Drink_Data);
        assertEquals("", testval);
    }

    @Test
    public void testRecommendedMerchant() {
        HashMap<String, Integer> Merchant_Data = new HashMap<>();
        Merchant_Data.put("USC", 3);
        Merchant_Data.put("UCLA", 4);
        Merchant_Data.put("SCP-294", 5);
        String testval = PlotScreen.RecommendedMerchant(Merchant_Data);
        assertEquals("SCP-294",testval);
        Merchant_Data.clear();
        Merchant_Data.put("Hello", 0);
        testval = PlotScreen.RecommendedDrink(Merchant_Data);
        assertEquals("Hello",testval);
        Merchant_Data.clear();
        testval = PlotScreen.RecommendedDrink(Merchant_Data);
        assertEquals("", testval);
    }

    @Test
    public void testAverageCaffeine(){
        HashMap<String, Integer> Order = new HashMap<>();
        Order.put("Day1", 300);
        Order.put("Day2", 400);
        Order.put("Day3", 500);
        int testval = PlotScreen.AverageCaffeine(Order);
        assertEquals(400, testval);
        Order.clear();
        Order.put("Day1", 0);
        testval = PlotScreen.AverageCaffeine(Order);
        assertEquals(0, testval);
        Order.clear();
        testval = PlotScreen.AverageCaffeine(Order);
        assertEquals(0, testval);
        Order.clear();
        Order.put("Day1", 1234);
        Order.put("Day2", 1256);
        Order.put("Day3", 777);
        Order.put("Day4", 211);
        Order.put("Day5", 666);
        testval = PlotScreen.AverageCaffeine(Order);
        assertEquals(828, testval);
    }
}