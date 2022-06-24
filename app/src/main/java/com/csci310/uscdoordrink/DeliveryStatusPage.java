package com.csci310.uscdoordrink;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class DeliveryStatusPage extends Fragment {

    static String uid;
    static Delivery delivery;
    static Delivery prevDelivery;
    static boolean isCompleted = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_delivery_status_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fetch();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void fetch(){
        try {
            if (prevDelivery != null && delivery == null) {
                if (prevDelivery.getStatus() == DeliveryStatus.PENDING) {
                    displayRejectedStatus();
                } else {
                    displayCompletedStatus();
                }
            } else if (((Customer) Database.user).getDeliveries() != null && !((Customer) Database.user).getDeliveries().equals("")) {
                Database.receiveDeliveryStatus(((Customer) Database.user).getDeliveries(), new CustomerDisplayRunnable() {
                    @Override
                    public void run(Delivery delivery, SerializablePoint merchantLocation) {
                        try {
                            DeliveryStatusPage.delivery = delivery;

                            if (prevDelivery != null && delivery == null) {
                                if (prevDelivery.getStatus() == DeliveryStatus.PENDING) {
                                    displayRejectedStatus();
                                } else {
                                    isCompleted = true;
                                    displayCompletedStatus();
                                }
                            } else if (delivery == null && prevDelivery == null) {
                                displayNoStatus();
                            } else {
                                updateDisplay(delivery, merchantLocation);
                            }

                            if(prevDelivery == null || delivery != null) {
                                prevDelivery = DeliveryStatusPage.delivery;
                            }
                        } catch (Exception e) {
                            Log.w("W", "Exception in callback run delivery status page");
                        }
                    }
                });
            }
        }
        catch (Exception e){
            Log.w("W", "Status page casting exception");
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isCompleted = false;
        if (!hidden) fetch();
    }

    private void displayRejectedStatus() {
        TextSwitcher mname = (TextSwitcher) getView().findViewById(R.id.merchantTitleForStatus);
        mname.setText("Delivery Rejected");
        MapPage.removeMerchantLocationOnMap();
    }

    private void displayCompletedStatus() {
        TextSwitcher mname = (TextSwitcher) getView().findViewById(R.id.merchantTitleForStatus);
        mname.setText("Delivery Complete");
        MapPage.removeMerchantLocationOnMap();
    }

    private void displayNoStatus() {
        MapPage.removeMerchantLocationOnMap();

        TextSwitcher mname = (TextSwitcher) getView().findViewById(R.id.merchantTitleForStatus);
        mname.setText("No Delivery in Progress");
        TextView nn = (TextView) getView().findViewById(R.id.userHelpMessage);
        nn.setText("Please make an order by tapping a merchant's title card on the map");

        RecyclerView cc = (RecyclerView) getView().findViewById(R.id.drinkForStatus);
        cc.setVisibility(View.GONE);

        GifImageView gif = (GifImageView) getView().findViewById(R.id.gifTea);
        gif.setVisibility(View.GONE);

        prevDelivery = null;
        delivery = null;
    }


    public void updateDisplay(Delivery delivery, SerializablePoint merchantLocation){
        try {

            List<Delivery> list = new ArrayList<Delivery>();
            list.add(delivery);

            RecyclerView recyclerView = getView().findViewById(R.id.drinkForStatus);
            recyclerView.setVisibility(View.VISIBLE);
            DeliveryStatusPageAdapter adapter = new DeliveryStatusPageAdapter(this.getContext(), list);
            recyclerView.setAdapter(adapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            
        }
        catch (Exception e){
            Log.w("W", "Error in update display for delivery");
        }
        setDeliveryText(delivery);
    }

    private void setDeliveryText(Delivery delivery) {
        TextSwitcher mstatus = (TextSwitcher) getView().findViewById(R.id.merchantTitleForStatus);
        String status = "";
        if (delivery != null) {
            switch (delivery.getStatus()) {
                case PENDING:
                    status = "Pending";
                    break;
                case ACCEPTED:
                    status = "Accepted";
                    break;
                case REJECTED:
                    status = "Rejected";
                    break;
                case IN_PROGRESS:
                    status = "In-Progress";
                    break;
                case DELIVERED:
                    status = "Delivered";
                    break;
                default:
                    status = "";
            }
        }

        if (prevDelivery == null || delivery == null || (prevDelivery.getStatus() != delivery.getStatus()) || delivery.getStatus() == DeliveryStatus.PENDING || delivery.getStatus() == DeliveryStatus.IN_PROGRESS){
            mstatus.setInAnimation(getContext(), android.R.anim.fade_in);
            mstatus.setOutAnimation(getContext(), android.R.anim.fade_out);
            GifImageView gif = (GifImageView) getView().findViewById(R.id.gifTea);
            gif.setVisibility(View.VISIBLE);
            //TextView t = new TextView(getContext());
            //mstatus.addView(t, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mstatus.setText(status);
        }

        TextView mname = (TextView) getView().findViewById(R.id.userHelpMessage);
        mname.setText(delivery.getOrder().getMerchantName());
    }



}