package com.csci310.uscdoordrink;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeliveryListPageAdapter extends RecyclerView.Adapter<DeliveryListPageAdapter.DeliveryListItemHolder> {
    String extraInformationText;
    Context currentContext;
    List<Delivery> deliveries;

    public DeliveryListPageAdapter(Context context, List<Delivery> deliveries) {
        currentContext = context;
        this.deliveries = deliveries;
    }

    @NonNull
    @Override
    public DeliveryListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater i = LayoutInflater.from(currentContext);
        View v = i.inflate(R.layout.delivery_list_row, parent, false);
        return new DeliveryListItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryListItemHolder holder, int position) {
        if (deliveries.get(position) == null) return;
        extraInformationText = formatExtraInformationText(position);
        holder.extraInformationTextView.setText(extraInformationText);

        //Set Text for time sent
        String orderTime = new SimpleDateFormat("HH:mm:ss").
                format(deliveries.get(position).getOrder().getTimestamp());
        String timeMessage = "Sent At: " + orderTime;
        holder.sentAtTimeTextView.setText(timeMessage);

        if(deliveries.get(holder.getAdapterPosition()).getStatus() == DeliveryStatus.PENDING) {
            holder.pendingOrderButtonView.setVisibility(View.VISIBLE);
            holder.deliverOrderButton.setVisibility(View.GONE);
        } else {
            holder.pendingOrderButtonView.setVisibility(View.GONE);
            holder.deliverOrderButton.setVisibility(View.VISIBLE);
        }
    }

    private String formatExtraInformationText(int position) {
        String extraInfo = "Status: ";
        if (deliveries.get(position).getStatus() == null){return " ";}
        switch (deliveries.get(position).getStatus()) {
            case PENDING:
                extraInfo += "Pending";
                break;
            case ACCEPTED:
                extraInfo += "Accepted";
                break;
            case REJECTED:
                extraInfo += "Rejected";
                break;
            case IN_PROGRESS:
                extraInfo += "In-Progress";
                break;
            case DELIVERED:
                extraInfo += "Delivered";
                break;
            default:
                extraInfo += "";
        }

        extraInfo += "\n";

        int order_cost = deliveries.get(position).getOrder().getCost();
        extraInfo += "Total Cost: " + CheckoutPageActivity.formatPrice(order_cost) + "\n";

        extraInfo += "Address: " + deliveries.get(position).getAddress() + "\n";

        if (deliveries.get(position).getOrder().getDrinks() != null) {
            for (Drink drink : deliveries.get(position).getOrder().getDrinks()) {
                extraInfo += drink.getName() + ", " + drink.getQuantity() + "\n";
            }
        }

        return extraInfo;

    }


    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    public class DeliveryListItemHolder extends RecyclerView.ViewHolder {
        MaterialButton expandInformationButton;
        boolean isExtraInfoVisible;
        View extraInformationView;

        TextView sentAtTimeTextView;
        TextView extraInformationTextView;
        View pendingOrderButtonView;
        Button acceptOrderButton;
        Button rejectOrderButton;


        Button deliverOrderButton;

        public DeliveryListItemHolder(@NonNull View view) {
            super(view);
            isExtraInfoVisible = false;

            expandInformationButton = view.findViewById(R.id.deliveryListPageRowExpandButton);
            extraInformationView = view.findViewById(R.id.deliveryListPageRowExtraInformation);
            extraInformationView.setVisibility(View.GONE);
            pendingOrderButtonView = view.findViewById(R.id.deliveryListPageRowDeliveryInitialOptions);
            //Buttons
            acceptOrderButton = view.findViewById(R.id.deliveryListPageRowDeliveryAcceptDeliveryButton);
            rejectOrderButton = view.findViewById(R.id.deliveryListPageRowDeliveryRejectDeliveryButton);
            deliverOrderButton = view.findViewById(R.id.deliveryListPageRowDeliveryDeliverDeliveryButton);

            //TextViews
            sentAtTimeTextView = view.findViewById(R.id.deliveryListPageRowHeaderText);
            extraInformationTextView = view.findViewById(R.id.deliveryListPageRowExtraInformationTextView);


            expandInformationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isExtraInfoVisible) {
                        extraInformationView.setVisibility(View.VISIBLE);
                        expandInformationButton.setIcon(view.getContext().getDrawable(R.drawable.ic_baseline_expand_less_24));

                    } else {
                        extraInformationView.setVisibility(View.GONE);
                        expandInformationButton.setIcon(view.getContext().getDrawable(R.drawable.ic_baseline_expand_more_24));
                    }
                    isExtraInfoVisible = !isExtraInfoVisible;


                }
            });

            acceptOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO mark delivery as in-progress in database
                    pendingOrderButtonView.setVisibility(View.GONE);
                    deliveries.get(getAdapterPosition()).setStatus(DeliveryStatus.IN_PROGRESS);
                    //Database.updateDeliveryStatus(deliveries.get(getAdapterPosition()));

                    String deliveryid = deliveries.get(getAdapterPosition()).getUID();

                    FirebaseDatabase.getInstance().getReference("/users/deliveries/" +deliveryid+"/status").setValue(DeliveryStatus.IN_PROGRESS);



                    extraInformationText = extraInformationText.substring(extraInformationText.indexOf("\n"));
                    extraInformationText = "Status: In-Progress" + extraInformationText;
                    //Todo add order money to balance
                    Database.user.setBalance(Database.user.getBalance() + deliveries.get(getAdapterPosition()).getOrder().getCost());
                    Database.updateUserInfo(Database.user);
                    extraInformationTextView.setText(extraInformationText);
                    deliverOrderButton.setVisibility(View.VISIBLE);
                }
            });

            rejectOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO delete drink and update database
                    //notifyItemRangeChanged(getAdapterPosition(), getItemCount());
                    // Todo mark the the delivery as rejected in the database
                    String deliveryid = deliveries.get(getAdapterPosition()).getUID();
                    Delivery delivery = deliveries.get(getAdapterPosition());
                    List<String> dd = ((Merchant)Database.user).getDeliveries();
                    dd.remove(deliveries.get(getAdapterPosition()).getUID());
                    ((Merchant)Database.user).setDeliveries(dd);
                    FirebaseDatabase.getInstance().getReference("/users/customers/"+delivery.getOrder().getCustomerUUID()+"/deliveries/").removeValue();
                    Database.updateUserInfo(Database.user);
                    FirebaseDatabase.getInstance().getReference("/users/deliveries/" +deliveryid).removeValue();
                    FirebaseDatabase.getInstance().getReference("/users/deliveries/"+deliveryid).removeValue();
                    deliveries.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });
            deliverOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        extraInformationText = extraInformationText.substring(extraInformationText.indexOf("\n"));
                        extraInformationText = "Status: Delivered" + extraInformationText;
                        Delivery compl = deliveries.get(getAdapterPosition());
                        Database.removeDelivery(compl);
                        Database.updateCaffeineforComplete(compl);
                        Database.updateBalanceforComplete(compl);

                        deliveries.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), getItemCount());
                    }catch (Exception e) {
                        Log.e("", "");
                    }
                }
            });


        }

    }
}
