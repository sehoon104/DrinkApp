package com.csci310.uscdoordrink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Menu {
    void createDrink(Drink drink) {
        drinks.put(drink.getUuid().toString() + "a", drink);
    }

    void removeDrink(Integer UUID) {
        drinks.remove(UUID.toString() + "a");
    }

    public Map<String, Drink> getDrinks() {
        return drinks;
    }

    public void setDrinks(Map<String, Drink> i) {
        drinks = i;
    }

    public List<Drink> drinksInMenu() {
        return new ArrayList<Drink>(drinks.values());
    }

    public Map<String, Drink> drinks;

    Menu() {
        drinks = new TreeMap<String, Drink>();
    }
}
