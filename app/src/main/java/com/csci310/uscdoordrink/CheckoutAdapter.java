package com.csci310.uscdoordrink;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutItemHolder> {
    Context currentContext;
    ArrayList<Drink> order;

    public CheckoutAdapter(Context context, ArrayList<Drink> order) {
        currentContext = context;
        this.order = order;
    }


    @NonNull
    @Override
    public CheckoutItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater i = LayoutInflater.from(currentContext);
        View v = i.inflate(R.layout.checkout_drink_row, parent, false);
        return new CheckoutItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutItemHolder holder, int position) {
        //super.onBindViewHolder(holder, position);
        holder.uid.setText(order.get(holder.getAdapterPosition()).getUuid().toString());
        holder.title.setText(order.get(holder.getAdapterPosition()).getName());
        int cost = order.get(holder.getAdapterPosition()).getTotalPrice() - (order.get(holder.getAdapterPosition()).getDiscount() * order.get(holder.getAdapterPosition()).getQuantity());
        holder.cost.setText(CheckoutPageActivity.formatPrice(cost));
        holder.quantity.setText(order.get(holder.getAdapterPosition()).getQuantity() + "");


        holder.increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(order.get(holder.getAdapterPosition()).getQuantity() == 10) {
                    return;
                }

                int pos = holder.getAdapterPosition();
                int oldCost = order.get(pos).getTotalPrice() - (order.get(pos).getDiscount()) * order.get(pos).getQuantity();

                TextView quantity = holder.quantity;
                int quantity_num = Integer.parseInt(quantity.getText().toString());
                quantity_num++;
                quantity.setText(quantity_num + "");
                order.get(pos).increaseQuantity();
                int newCost = order.get(pos).getTotalPrice() - (order.get(pos).getDiscount()) * order.get(pos).getQuantity();
                CheckoutPageActivity.updateTotalPrice(oldCost, newCost);

                holder.cost.setText(CheckoutPageActivity.formatPrice(newCost));


            }
        });

        holder.decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                int oldCost = order.get(pos).getTotalPrice() - (order.get(pos).getDiscount() * order.get(pos).getQuantity());
                TextView quantity = holder.quantity;
                int quantity_num = Integer.parseInt(quantity.getText().toString());
                if (quantity_num > 1) {
                    quantity_num--;
                    quantity.setText(quantity_num + "");
                    order.get(pos).decreaseQuantity();
                    int newCost = order.get(pos).getTotalPrice() - (order.get(pos).getDiscount()) * order.get(pos).getQuantity();
                    CheckoutPageActivity.updateTotalPrice(oldCost, newCost);
                    holder.cost.setText(CheckoutPageActivity.formatPrice(newCost));
                }
            }
        });

        holder.deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                int oldCost = order.get(pos).getTotalPrice() - (order.get(pos).getDiscount() * order.get(pos).getQuantity());
                order.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, order.size());
                CheckoutPageActivity.updateTotalPrice(oldCost, 0);
                //BrowseDrinksPage.isSelected.put(h*older.uid.getText().toString(), false);


            }
        });

    }


    @Override
    public int getItemCount() {
        return order.size();
    }

    public class CheckoutItemHolder extends RecyclerView.ViewHolder {
        TextView title, quantity, cost, uid;
        Button increaseQuantity, decreaseQuantity, deleteItem;

        public CheckoutItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.checkoutDrinkTitle);
            quantity = itemView.findViewById(R.id.checkoutDrinkQuantity);
            cost = itemView.findViewById(R.id.checkoutDrinkPrice);
            uid = itemView.findViewById(R.id.checkoutUID);

            increaseQuantity = itemView.findViewById(R.id.checkoutDrinkQuantityIncrease);
            decreaseQuantity = itemView.findViewById(R.id.checkoutDrinkQuantityDecrease);
            deleteItem = itemView.findViewById(R.id.checkoutDeleteDrink);
        }

    }

}
