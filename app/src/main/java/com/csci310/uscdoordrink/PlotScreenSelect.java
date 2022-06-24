package com.csci310.uscdoordrink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class PlotScreenSelect extends Fragment {

    private Spinner timeperiod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_plot_screen_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        timeperiod = view.findViewById(R.id.spinner);
        List<String> TimeP = new ArrayList<String>();
        TimeP.add(0, "Choose Period");
        TimeP.add("3 Weeks");
        TimeP.add("1 Month");
        TimeP.add("6 Months");
        TimeP.add("1 Year");
        TimeP.add("All");

        ArrayAdapter<String> dataAdapter  = new ArrayAdapter(this.getContext(),android.R.layout.simple_spinner_item, TimeP);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timeperiod.setAdapter(dataAdapter);
        timeperiod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!adapterView.getItemAtPosition(i).equals("Choose Period")) {
                    String item = adapterView.getItemAtPosition(i).toString();
                    Intent intent = new Intent(PlotScreenSelect.this.getContext(), PlotScreen.class);
                    Calendar calendar = Calendar.getInstance();
                    Date date = new Date();
                    calendar.setTime(date);
                    Boolean allorders = false;
                    if (item == "3 Weeks") calendar.add(Calendar.DAY_OF_YEAR, -21);
                    else if (item == "1 Month") calendar.add(Calendar.MONTH, -1);
                    else if (item == "6 Months") calendar.add(Calendar.MONTH, -6);
                    else if (item == "1 Year") calendar.add(Calendar.YEAR, -1);
                    else if (item == "All") allorders = true;
                    date = calendar.getTime();

                    String UID = Database.getUID();
                    DatabaseReference dataref = FirebaseDatabase.getInstance().getReference("/users/completedOrdersCustomers/" + UID);
                    Boolean finalAllorders = allorders;
                    Date finalDate = date;
                    dataref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<Order> orderList = new ArrayList<>();
                            if (snapshot.exists()) {//This is not necessary
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    orderList.add(ds.getValue(Order.class));
                                }
                            }
                            //Iterate through orders and parse accordging to timeperiod
                            if (!finalAllorders){
                                Iterator<Order> it = orderList.iterator();
                                while (it.hasNext()) {
                                    Order order = it.next();
                                    if (order.getTimestamp().before(finalDate)) {
                                        it.remove();
                                    }
                                }
                            }
                            if (orderList.isEmpty()) {
                                Toast.makeText(adapterView.getContext(), "No History For Selected", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                intent.putExtra("key", (Serializable) orderList);
                                startActivity(intent);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //nothing
            }
        });
    }
}