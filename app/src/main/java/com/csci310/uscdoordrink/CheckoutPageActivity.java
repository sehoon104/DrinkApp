package com.csci310.uscdoordrink;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class CheckoutPageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ExtendedFloatingActionButton checkoutButton;
    static TextView totalCost;
    static int totalCostNum;
    static String merchantuid;
    static String merchantname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_page);
        Bundle passedindata = getIntent().getExtras();
        merchantuid = (String) passedindata.get("merchant_uid");
        merchantname = (String) passedindata.get("merchant_name");

        totalCostNum = 0;

        totalCost = findViewById(R.id.orderCost);

        if (BrowseDrinksPage.order != null) {
            for (Drink dr : BrowseDrinksPage.order) {
                totalCostNum += (dr.getTotalPrice() - dr.getDiscount() * dr.getQuantity());
            }
        }

        totalCost.setText(formatPrice(totalCostNum));


        recyclerView = findViewById(R.id.itemizedOrder);
        recyclerView.setItemAnimator(new FadeInRightAnimator());
        CheckoutAdapter adapter = new CheckoutAdapter(this, BrowseDrinksPage.order);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        checkoutButton = findViewById(R.id.checkoutButton);

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BrowseDrinksPage.order.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Select drinks to make an order", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Make sure address is on file
                if(Database.user.getAddress() == null || Database.user.getAddress().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Error: Please set an address in settings before making orders", Toast.LENGTH_SHORT).show();
                    return;
                }


                int totalAmount = 0;
                int totalCaffeine = 0;
                int totalQuantity = 0;
                if (BrowseDrinksPage.order != null) {
                    for (Drink dr : BrowseDrinksPage.order) {
                        totalQuantity += dr.getQuantity();
                        totalAmount += (dr.getTotalPrice() - (dr.getDiscount() * dr.getQuantity()));
                        totalCaffeine += dr.getCaffeineContent() * dr.getQuantity();
                    }
                }

                String message = "Cost of " + formatPrice(totalCostNum) + " Total items: " + totalQuantity;

                if (((Customer) Database.user).getDeliveries() != null){
                    Toast.makeText(getApplicationContext(), "Error: Please wait for your current Delivery to finish", Toast.LENGTH_SHORT).show();
                    return;
                }

                //check balance {
                if (Database.user.getBalance() >= totalAmount) {
                    //Update Caffeine Daily Intake
                    //Database.updateCaffeineDaily();
                    //Check Caffeine Intake for the day. if more than 400, create alert
                    //TODO allow user to change caffeine limit
                    int databaseCaffeine = ((Customer)Database.user).getCaffeineIntake();
                    if (databaseCaffeine + totalCaffeine > 400) {
                        int finalTotalAmount = totalAmount;
                        int finalCaff = totalCaffeine;
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("High Caffeine Alert")
                                .setMessage("Your caffeine intake is already " + databaseCaffeine + " mg today. Do you wish to continue?")
                                .setPositiveButton("Yes", (dialogInterface, i) -> {
                                    //TODO: Continue with the order
                                    //Test text
                                    //Toast.makeText(getApplicationContext(), merchantuid, Toast.LENGTH_SHORT).show();

                                    sendpendingdelivery(BrowseDrinksPage.order, finalTotalAmount, getApplicationContext(), finalCaff);
                                })
                                .setNegativeButton("No", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    else {
                        /*if(Database.user.getUid().equals(merchantuid)) {
                            Toast.makeText(getApplicationContext(), "Error: Bug detected. Log out & log back in", Toast.LENGTH_LONG).show();
                        }*/
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        sendpendingdelivery(BrowseDrinksPage.order, totalAmount, getApplicationContext(), totalCaffeine);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Insufficient Balance", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public static void updateTotalPrice(int subtractOld, int newAdd) {
        totalCostNum -= subtractOld;
        totalCostNum += newAdd;
        totalCost.setText(formatPrice(totalCostNum));
    }

    public static String formatPrice(int cost) {
        if (cost == 0) {
            return "$0.00";
        }
        return "$" + cost / 100 + "." + String.format("%02d",cost % 100);
    }

    public void sendpendingdelivery(ArrayList<Drink> drinks, int cost, Context context, int caffeine) {
        //Balance and caffeine is updated when the delivery is complete
        Order neworder = new Order(cost, merchantuid, Database.getUID(), drinks, OrderStatus.SENT, MapPage.duration*1000, merchantname);
        Database.sendDelivery(neworder);
        Toast.makeText(getApplicationContext(), "Order Created. Wait for Merchant Approval", Toast.LENGTH_SHORT).show();
        finis();

    }

    public void finis(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BrowseDrinksPage.setCheckoutSuccessful();
        finish();
    }
}