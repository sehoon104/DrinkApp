package com.csci310.uscdoordrink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.annotation.Annotation;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MerchantMenuEditActivity extends AppCompatActivity {

    FloatingActionButton createNewDrink;
    static public RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_menu_edit);
        Database.getMenu(Database.user.getUid(), new MenuRunnable() {
            @Override
            public void run(Menu m) {
                buildDrinkDisplay(m);
            }
        });
    }

    public void buildDrinkDisplay(Menu menuToDisplay) {
        createNewDrink = findViewById(R.id.merchantAddNewDrink);

        createNewDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeActivity();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.merchantEditAllDrinks);

        DrinkPageAdapter adapter = new DrinkPageAdapter(this, menuToDisplay);

        if (menuToDisplay == null) {
            recyclerView.setVisibility(View.GONE);
            return;
        }
        recyclerView.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView title = view.findViewById(R.id.drinkTitle);
                //ImageView img = view.findViewById(R.id.drinkImage);
                TextView description = view.findViewById(R.id.drinkDescription);
                TextView caffeineContent = view.findViewById(R.id.drinkCaffeine);
                TextView price = view.findViewById(R.id.drinkPrice);
                TextView uid = view.findViewById(R.id.uid);
                TextView discount = view.findViewById(R.id.drinkDiscount);
                TextView drinkTypeData = view.findViewById(R.id.drinkTypeData);


                //Image will have to be bitmap more complicated,see https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
                //Image imgSource = img.get

                Intent intent = new Intent(getApplicationContext(), EditParticularDrink.class);

                String titleString = title.getText().toString();
                String descriptionString = description.getText().toString();
                String caffeienString = caffeineContent.getText().toString().replaceAll("[\\D]", "");
                String priceString = price.getText().toString().substring(1);
                String uidString = uid.getText().toString();
                String discountString = discount.getText().toString().replaceAll("[\\D]", "");
                Log.e("", "" + discountString);
                String drinkTypeDataString = drinkTypeData.getText().toString();

                intent.putExtra("uid", uidString);
                intent.putExtra("title", titleString);
                intent.putExtra("description", descriptionString);
                intent.putExtra("caffeineContent", caffeienString);
                intent.putExtra("price", priceString);
                intent.putExtra("isNewDrink", false);
                intent.putExtra("discount", discountString);
                intent.putExtra("drinkTypeData", drinkTypeDataString);
                intent.putExtra("position", position);

                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    public static void notifyItemChangeInRecycler(int index) {
        recyclerView.getAdapter().notifyItemRemoved(index);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Database.getMenu(Database.user.getUid(), new MenuRunnable() {
            @Override
            public void run(Menu m) {
                buildDrinkDisplay(m);
            }
        });
    }


    private void changeActivity() {
        Intent intent = new Intent(this, EditParticularDrink.class);
        intent.putExtra("isNewDrink", true);
        intent.putExtra("title", "");
        intent.putExtra("description", "");
        intent.putExtra("caffeineContent", "0");
        intent.putExtra("price", "$0.00");
        intent.putExtra("discount", "$0.00");
        intent.putExtra("drinkTypeData", "tea");
        startActivity(intent);
    }
}