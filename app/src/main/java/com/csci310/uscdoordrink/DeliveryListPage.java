package com.csci310.uscdoordrink;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeliveryListPage extends Fragment {

    RecyclerView recyclerView;
    DeliveryList deliveryList;
    public static Handler handler;
    public static Boolean isCompleted;
    public static volatile Boolean isShown = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_delivery_list_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button t = (Button) getView().findViewById(R.id.deliveryListPageTitle);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetch();
            }
        });
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isShown) {
                    fetch();
                    //h.postDelayed(this, 1000);
                }
            }
        }, 100);
    }

    public void fetch(){
        try {

            Database.fetchAllDeliveries();
            deliveryList = Database.inprogresslist;

            for (int i = 0; i < deliveryList.getDeliveries().size(); i++) {
                if (deliveryList.getDeliveries().get(i) == null) {
                    deliveryList.getDeliveries().remove(i);
                }
            }


            recyclerView = getView().findViewById(R.id.recycler_viewCurrentDeliveries);
            DeliveryListPageAdapter adapter = new DeliveryListPageAdapter(this.getContext(), deliveryList.getDeliveries());
            recyclerView.setAdapter(adapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
        catch (Exception e){
            Log.w("w", "Exception in delivery list page");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        this.onHiddenChanged(false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isCompleted = false;
        if (!hidden){
            isShown = true;
            fetch();
//            Handler h = new Handler();
//            h.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (isShown) {
//                        fetch();
//                        h.postDelayed(this, 1000);
//                    }
//                }
//            }, 1000);
        }
        else{
            isShown = false;
        }
    }


    public static void setHandler()
    {
        int delay = 5000;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (Database.cleanList != null){
                    for (Delivery i : Database.cleanList.getDeliveries()){
                        Database.sendLocation(i);
                    }
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
}