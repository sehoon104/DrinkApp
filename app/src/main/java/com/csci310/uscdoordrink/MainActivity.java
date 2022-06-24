package com.csci310.uscdoordrink;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onChangeToMapView(View view) {
        Intent intent = new Intent(this, MapPage.class);
        startActivity(intent);
    }

    public void onChangeToLoginView(View view){
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
    }

    public void onChangeToDrinkView(View view) throws IOException {
        String exampleMenu = "";
        exampleMenu = this.createTestFile("examplemenu.txt");//used to create a test menu, replace with call to database
        Intent intent = new Intent(this, BrowseDrinksPage.class);
        intent.putExtra("header", exampleMenu);
        startActivity(intent);
    }

    private String createTestFile(String filename) throws IOException {
        Drink drink1 = new Drink(1, "First", "This the first one buddy", true, 111, 11);
        Drink drink2 = new Drink(2, "Second", "This the Second one buddy", true, 222, 22);
        Drink drink3 = new Drink(3, "Third", "This the Third one buddy", false, 333, 33);
        Drink drink4 = new Drink(2, "Fourth", "This the Fourth one buddy", false, 444, 44);

        Menu testMenu = new Menu();
        testMenu.createDrink(drink1);
        testMenu.createDrink(drink2);
        testMenu.createDrink(drink3);
        testMenu.createDrink(drink4);

        Gson gson = new GsonBuilder().create();
        return gson.toJson(testMenu);
    }

}