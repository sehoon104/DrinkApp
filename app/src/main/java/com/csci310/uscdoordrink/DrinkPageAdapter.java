package com.csci310.uscdoordrink;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Collection;

public class DrinkPageAdapter extends RecyclerView.Adapter<DrinkPageAdapter.DrinkHolder> {

    Context currentContext;
    Menu menu;
    Object[] listOfDrinks;
    DrinkHolder drinkHolder;

    public DrinkPageAdapter(Context context, Menu menuToDisplay) {
        currentContext = context;
        menu = menuToDisplay;
        if (menu != null) listOfDrinks = menu.drinksInMenu().toArray();
    }


    @NonNull
    @Override
    public DrinkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater i = LayoutInflater.from(currentContext);
        View v = i.inflate(R.layout.drink_row, parent, false);
        return new DrinkHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkHolder holder, int position) {
        drinkHolder = holder;
        holder.uid.setText(((Drink) listOfDrinks[position]).getUuid().toString());
        holder.drinkTitle.setText(((Drink) listOfDrinks[position]).getName());
        holder.drinkDescription.setText(((Drink) listOfDrinks[position]).getDescription());
        int caffeine_mg = new Integer(((Drink) listOfDrinks[position]).getCaffeineContent());
        holder.drinkCaffeine.setText("Caffeine: " + caffeine_mg + " mg");
        int drink_price = ((Drink) listOfDrinks[position]).getTotalPrice();
        holder.price.setText(CheckoutPageActivity.formatPrice(drink_price));

        int discount = ((Drink) listOfDrinks[position]).getDiscount();
        holder.discount.setText("Discount: " + CheckoutPageActivity.formatPrice(discount));
        if (discount == 0) {
            holder.discount.setVisibility(View.GONE);
        } else {
            holder.discount.setVisibility(View.VISIBLE);
        }


        int totalPrice = drink_price - discount;
        holder.totalPrice.setText("Total: " + CheckoutPageActivity.formatPrice(totalPrice));

        boolean isTea = ((Drink) listOfDrinks[position]).getIsTea();
        if (isTea) {
            holder.drinkTypeData.setText("tea");
            holder.imageView.setImageBitmap(BitmapFactory.decodeResource(currentContext.getResources(), R.drawable.tea_cup));
        } else {
            holder.drinkTypeData.setText("coffee");
            holder.imageView.setImageBitmap(BitmapFactory.decodeResource(currentContext.getResources(), R.drawable.coffee_mug));
        }
    }

    @Override
    public int getItemCount() {
        return (menu != null) ? menu.drinksInMenu().size() : 0;
    }

    public void notifyChanged() {
        notifyItemRemoved(drinkHolder.getAdapterPosition());
    }


    public class DrinkHolder extends RecyclerView.ViewHolder {

        TextView drinkTitle, drinkDescription, drinkCaffeine, uid, price, discount, totalPrice;
        TextView drinkTypeData;

        ImageView imageView;

        public DrinkHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.drinkImage);
            drinkTitle = (TextView) itemView.findViewById(R.id.drinkTitle);
            drinkDescription = (TextView) itemView.findViewById(R.id.drinkDescription);
            drinkCaffeine = (TextView) itemView.findViewById(R.id.drinkCaffeine);
            price = (TextView) itemView.findViewById(R.id.drinkPrice);
            uid = (TextView) itemView.findViewById(R.id.uid);
            drinkTypeData = itemView.findViewById(R.id.drinkTypeData);
            discount = itemView.findViewById(R.id.drinkDiscount);
            totalPrice = itemView.findViewById(R.id.drinkTotalPrice);

        }
    }
}
