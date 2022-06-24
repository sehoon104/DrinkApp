package com.csci310.uscdoordrink;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class BrowseDrinksPage extends AppCompatActivity {

    static String mname;
    static String mUid;
    Menu menuDisplayed;
    public static ArrayList<Drink> order;
    ExtendedFloatingActionButton finalizeOrderButton;
    static boolean isCheckoutSuccessful;
    boolean isCustomer;
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCheckoutSuccessful = false;
        Intent intent = getIntent();

        isCustomer = Database.user.getIsCustomer();


        order = new ArrayList<>();

        setContentView(R.layout.activity_browse_drinks_page);
        finalizeOrderButton = findViewById(R.id.finalizeOrderButton);
        finalizeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (order.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Please add a drink to your order", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), CheckoutPageActivity.class);
                    intent.putExtra("merchant_uid", mUid);
                    intent.putExtra("merchant_name", mname);
                    startActivity(intent);
                }

            }
        });

        if (!isCustomer) {
            finalizeOrderButton.setVisibility(View.GONE);
        }

        Gson gson = new GsonBuilder().create();
        menuDisplayed = gson.fromJson(intent.getStringExtra("data"), Menu.class);
        JsonObject merchantInfo = gson.fromJson(intent.getStringExtra("merchantInfo"), JsonObject.class);

        TextView title = (TextView) findViewById(R.id.merchantTitle);
        title.setText(merchantInfo.get("name").getAsString());
        mname = merchantInfo.get("name").getAsString();
        mUid = merchantInfo.get("uid").getAsString();



        recyclerView = (RecyclerView) findViewById(R.id.listOfDrinks);
        DrinkPageAdapter adapter = new DrinkPageAdapter(this, menuDisplayed);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isCustomer) {
                    // Use hash map as quick way to determine if selected box should be displayed
                    TextView uid_view = view.findViewById(R.id.uid);
                    String uid = uid_view.getText().toString();
                    Drink selectedDrink = menuDisplayed.getDrinks().get(uid + "a");
                    if (!order.contains(selectedDrink)) {
                        order.add(selectedDrink);
                        view.setBackground(getDrawable(R.drawable.drink_border));
                    } else {
                        order.remove(selectedDrink);
                        view.setBackground(getDrawable(R.drawable.drink_border_divider));
                    }
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));


    }

    public static void setCheckoutSuccessful() {
        isCheckoutSuccessful = true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isCheckoutSuccessful) {
            MainPage.returningFromSuccessFulCheckout = true;
            isCheckoutSuccessful = false;
            finish();

        }

        int children = recyclerView.getChildCount();
        for (int i = 0; i < children; i++) {
            View view = recyclerView.findViewHolderForAdapterPosition(i).itemView;
            TextView uid_view = view.findViewById(R.id.uid);
            String uid = uid_view.getText().toString();
            Drink selectedDrink = menuDisplayed.getDrinks().get(uid + "a");
            if (!order.contains(selectedDrink)) {
                view.setBackground(getDrawable(R.drawable.drink_border_divider));
            }
        }

    }
}