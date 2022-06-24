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

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeliveryStatusPageAdapter extends RecyclerView.Adapter<DeliveryStatusPageAdapter.DeliveryStatusPageItemHolder> {
    String extraInformationText;
    Context currentContext;
    List<Delivery> deliveries;

    public DeliveryStatusPageAdapter(Context context, List<Delivery> deliveries) {
        currentContext = context;
        this.deliveries = deliveries;
    }

    @NonNull
    @Override
    public DeliveryStatusPageItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater i = LayoutInflater.from(currentContext);
        View v = i.inflate(R.layout.delivery_status_row, parent, false);
        return new DeliveryStatusPageItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryStatusPageItemHolder holder, int position) {
        extraInformationText = formatExtraInformationText(position);
        holder.extraInformationTextView.setText(extraInformationText);
    }

    private String formatExtraInformationText(int position) {
        String extraInfo = "\n";
        /*String extraInfo = "Status: ";
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
        */

        int order_cost = (deliveries.get(position).getOrder().getCost());
        extraInfo += "Total Cost: " + CheckoutPageActivity.formatPrice(order_cost) + "\n";

        extraInfo += "Deliver To: " + deliveries.get(position).getAddress() + "\n";

        if ( deliveries.get(position).getOrder().getDrinks() != null) {
            for (Drink drink : deliveries.get(position).getOrder().getDrinks()) {
                extraInfo += "Drink: ";
                extraInfo += drink.getQuantity() + " " + drink.getName()  + "\n";
            }
        }

        return extraInfo;

    }


    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    public class DeliveryStatusPageItemHolder extends RecyclerView.ViewHolder {
        MaterialButton expandInformationButton;
        boolean isExtraInfoVisible;
        View extraInformationView;

        TextView sentAtTimeTextView;
        TextView expected;
        TextView extraInformationTextView;

        View pendingOrderButtonView;
        //Button acceptOrderButton;
        //Button rejectOrderButton;


        Button deliverOrderButton;

        public DeliveryStatusPageItemHolder(@NonNull View view) {
            super(view);
            //TextViews
            sentAtTimeTextView = view.findViewById(R.id.deliveryStatusPageRowHeaderText);
            expected = view.findViewById(R.id.deliveryStatusPageRowHeaderText2);
            extraInformationTextView = view.findViewById(R.id.deliveryStatusPageRowExtraInformationTextView);
            extraInformationTextView.setVisibility(View.VISIBLE);

            if(deliveries != null) {

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");

                Date orderSentAt = deliveries.get(0).getOrder().getTimestamp();

                String timeMessage = "Sent At: " + sdf.format(orderSentAt);
                sentAtTimeTextView.setText(timeMessage);

                long plus = deliveries.get(0).getOrder().getETA().getTime();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(orderSentAt);

                long seconds = deliveries.get(0).getOrder().getduration() / 1000;
                calendar.add(Calendar.SECOND, (int)(seconds));

                timeMessage = "Time of Arrival: " + sdf.format(calendar.getTime());

                expected.setText(timeMessage);

            }

        }

    }
}
