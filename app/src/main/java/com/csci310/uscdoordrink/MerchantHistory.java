package com.csci310.uscdoordrink;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MerchantHistory extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_history);
        recyclerView = findViewById(R.id.histories);
        Bundle passedin = getIntent().getExtras();
        ArrayList<Delivery> orderlist = (ArrayList<Delivery>) passedin.get("keym");
        List<HistoryRow> historyRows = new ArrayList<>();
        for (Delivery del : orderlist) {
            historyRows.add(new HistoryRow(del));
        }
        HistoryAdapter adapter = new HistoryAdapter(historyRows);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

}