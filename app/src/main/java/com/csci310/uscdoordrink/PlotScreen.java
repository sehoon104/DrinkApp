package com.csci310.uscdoordrink;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlotScreen extends AppCompatActivity {

    PieChart pie_chart;
    PieChart merchant_chart;
    //LineChart caffeine_chart;
    ListView orderlist;
    TextView rec_merchant;
    TextView rec_drink;
    TextView avg_caffeine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot_screen);

        HashMap<String, Integer> Drink_Data = new HashMap<String, Integer>();
        HashMap<String, Integer> Merchant_Data = new HashMap<String, Integer>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ArrayList<Order> orders = (ArrayList<Order>) extras.get("key");
            pie_chart = findViewById(R.id.piechart);
            merchant_chart = findViewById(R.id.merchantchart);
            orderlist = (ListView) findViewById(R.id.listoforders);
            rec_drink = findViewById(R.id.recdri);
            rec_merchant = findViewById(R.id.recmer);
            avg_caffeine = findViewById(R.id.avgcaf);

            ArrayList<String> orderList = new ArrayList<>();
            HashMap<String,Integer> caffeineintakes = new HashMap<>();
            DateFormat dateparse = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat minuteparse = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Integer AverageCaffeineIntake = 0;

            //Parse orders to Drink_Data/Merchant_Data/OrderList
            for (Order order : orders) {
                //Merchant Parse
                String MerchantName = order.getMerchantName();
                if (Merchant_Data.containsKey(MerchantName)) {
                    Integer count = Merchant_Data.get(MerchantName);
                    Merchant_Data.put(MerchantName,count+1);
                }
                else {
                    Merchant_Data.put(MerchantName,1);
                }
                //Drink Parse
                Integer caffeineintake = 0;
                for (Drink drink : order.getDrinks()) {
                    Integer quantity = drink.getQuantity();
                    String drinkname = drink.getName();
                    if (Drink_Data.containsKey(drinkname)) {
                        Integer count = Drink_Data.get(drinkname);
                        Drink_Data.put(drinkname,count+quantity);
                    }
                    else {
                        Drink_Data.put(drinkname,quantity);
                    }
                    String quantity_string = "x" + quantity;
                    String ordertime = minuteparse.format(order.getTimestamp());
                    long millis = order.getduration();
                    String duration = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                            TimeUnit.MILLISECONDS.toSeconds(millis) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                    String listentry = String.format("%-10s %-5s %-15s %s \n Delivery Duration: %s",drinkname, quantity_string, MerchantName, ordertime, duration);
                    orderList.add(listentry);
                    caffeineintake += drink.getCaffeineContent() * quantity;
                }
                //Caffeine Parse
                String dateoforder = dateparse.format(order.getTimestamp());
                if (caffeineintakes.containsKey(dateoforder)) {
                    Integer count = caffeineintakes.get(dateoforder);
                    caffeineintakes.put(dateoforder,count+caffeineintake);
                }
                else {
                    caffeineintakes.put(dateoforder, caffeineintake);
                }
            }

            //Array Adapter
            ArrayAdapter arrayAdapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1, orderList);
            orderlist.setAdapter(arrayAdapter);
            ArrayList<PieEntry> drinkentries = new ArrayList<>();
            ArrayList<PieEntry> merchantentries = new ArrayList<>();


            //Iterate HashMap
            for (Map.Entry<String, Integer> entry : Drink_Data.entrySet()) {
                String drink = entry.getKey();
                Integer freq = entry.getValue();
                PieEntry pieEntry = new PieEntry(freq, drink);
                drinkentries.add(pieEntry);
            }
            for (Map.Entry<String, Integer> entry : Merchant_Data.entrySet()) {
                String merchant = entry.getKey();
                Integer freq = entry.getValue();
                PieEntry pieEntry = new PieEntry(freq, merchant);
                merchantentries.add(pieEntry);
            }

            AverageCaffeineIntake = AverageCaffeine(caffeineintakes);

            //PieChart for Drinks
            PieDataSet pieDataSet = new PieDataSet(drinkentries, "(Drinks)");
            pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            pieDataSet.setDrawValues(true);
            pie_chart.setData(new PieData(pieDataSet));
            pie_chart.animateXY(1000,1000);
            pie_chart.getDescription().setText("Drinks Chart");
            pie_chart.getDescription().setTextColor(Color.RED);
            pie_chart.setCenterText("Drinks Chart");
            pie_chart.setCenterTextColor(Color.BLUE);
            pie_chart.getLegend().setTextColor(Color.RED);

            //PieChart for Merchants
            PieDataSet pieDataSet1 = new PieDataSet(merchantentries, "(Merchants)");
            pieDataSet1.setColors(ColorTemplate.LIBERTY_COLORS);
            pieDataSet1.setDrawValues(true);
            merchant_chart.setData(new PieData(pieDataSet1));
            merchant_chart.setEntryLabelColor(Color.BLUE);
            merchant_chart.animateXY(1000,1000);
            merchant_chart.getDescription().setText("Merchants Chart");
            merchant_chart.getDescription().setTextColor(Color.GREEN);
            merchant_chart.setCenterText("Merchants Chart");
            merchant_chart.setCenterTextColor(Color.RED);
            merchant_chart.getLegend().setTextColor(Color.GREEN);

            //Recommendation
            String recommended_drink = RecommendedDrink(Drink_Data);
            String recd = "Recommended Drink: " + recommended_drink;
            String recommended_mer = RecommendedMerchant(Merchant_Data);
            String recm = "Recommended Merchant: " + recommended_mer;
            String avgmessage = "Your Average Caffeine Intake: " + AverageCaffeineIntake.toString() + "mg/day";
            if (AverageCaffeineIntake > 400) {
                Integer dif = AverageCaffeineIntake - 400;
                avgmessage += ", which is " + dif.toString() + "mg higher than the recommended intake.";
            }
            rec_merchant.setText(recm);
            rec_drink.setText(recd);
            avg_caffeine.setText(avgmessage);
            rec_drink.setTextColor(Color.RED);
            rec_merchant.setTextColor(Color.GREEN);
            avg_caffeine.setTextColor(Color.MAGENTA);
            rec_drink.setTextSize(17f);
            rec_merchant.setTextSize(17f);
            avg_caffeine.setTextSize(17f);

        }
    }

    public static int AverageCaffeine(HashMap<String,Integer> caffeineintakes) {
        Integer AverageCaffeineIntake = 0;
        for (Map.Entry<String, Integer> entry : caffeineintakes.entrySet()) {
            Integer caffeine = entry.getValue();
            AverageCaffeineIntake += caffeine;
        }
        if (caffeineintakes.size() != 0) AverageCaffeineIntake /= caffeineintakes.size();
        return AverageCaffeineIntake;
    }

    public static String RecommendedDrink(HashMap<String, Integer> Drink_Data) {
        if (!Drink_Data.isEmpty())
            return Collections.max(Drink_Data.entrySet(), Map.Entry.comparingByValue()).getKey();
        else
            return "";
    }

    public static String RecommendedMerchant(HashMap<String, Integer> Merchant_Data) {
        if (!Merchant_Data.isEmpty())
            return Collections.max(Merchant_Data.entrySet(), Map.Entry.comparingByValue()).getKey();
        else
            return "";
    }
}