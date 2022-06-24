package com.csci310.uscdoordrink;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class MenuTest extends TestCase {

    public void testGetDrinks() {
        Menu toTest = new Menu();

        toTest.setDrinks(new TreeMap<String, Drink>());
        assertEquals(0, toTest.getDrinks().size());

        toTest.createDrink(new Drink(1, "First drink","", true, 100, 100, 100));
        assertEquals(1, toTest.getDrinks().size());

        toTest.createDrink(new Drink(2, "Second drink","", true, 100, 100, 100));
        assertEquals(2, toTest.getDrinks().size());

        //test duplicate

        toTest.createDrink(new Drink(1, "Duplicate first drink","", true, 100, 100, 100));
        assertEquals(2, toTest.getDrinks().size());

        toTest.createDrink(new Drink(2, "Duplicate second drink","", true, 100, 100, 100));
        assertEquals(2, toTest.getDrinks().size());

        //test many drinks in numerical order

        for (int i = 3; i < 30; i++){
            toTest.createDrink(new Drink(i, "drink","", true, 100, 100, 100));
        }

        for (int i = 0; i < 29; i++){
            Integer toCheck = toTest.drinksInMenu().get(i).getUuid();
            assertEquals(true, toTest.getDrinks().containsKey(toCheck.toString() + "a"));
        }

        //test removing all drinks

        toTest.setDrinks(new HashMap<String, Drink>());
        assertEquals(0, toTest.getDrinks().size());
    }

    public void testDrinksInMenu() {
        Menu toTest = new Menu();

        toTest.setDrinks(new TreeMap<String, Drink>());
        assertEquals(0, toTest.drinksInMenu().size());

        toTest.createDrink(new Drink(1, "First drink","", true, 100, 100, 100));
        assertEquals(1, toTest.drinksInMenu().size());

        toTest.createDrink(new Drink(2, "Second drink","", true, 100, 100, 100));
        assertEquals(2, toTest.drinksInMenu().size());

        //test duplicate

        toTest.createDrink(new Drink(1, "Duplicate first drink","", true, 100, 100, 100));
        assertEquals(2, toTest.drinksInMenu().size());

        toTest.createDrink(new Drink(2, "Duplicate second drink","", true, 100, 100, 100));
        assertEquals(2, toTest.drinksInMenu().size());

        //test many drinks in numerical order

        for (int i = 3; i < 30; i++){
            toTest.createDrink(new Drink(i, "drink","", true, 100, 100, 100));
        }

        for (int i = 0; i < 29; i++){
            Integer toCheck = toTest.drinksInMenu().get(i).getUuid();
            assertEquals(toTest.drinksInMenu().get(i).getUuid(), toCheck);
        }

        //test removing all drinks

        toTest.setDrinks(new HashMap<String, Drink>());
        assertEquals(0, toTest.getDrinks().size());

    }
}