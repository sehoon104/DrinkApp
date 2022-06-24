package com.csci310.uscdoordrink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RunWith(JUnit4.class)
public class zDatabaseTest {

    static volatile boolean done = false;
    static volatile boolean fail = false;
    static String merchantUID = "JTyasi55IEgyTmu1IT5SAFMF9cU2";

//    public void beforetest(){
//        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getContext());
//    }

    @Test
    public void sendDeliveryTest() {
        List<Drink> drinkList = new ArrayList<>();
        Drink drink = new Drink(2, "Coffee","This is test drink", false, 30, 100);
        drinkList.add(drink);
        Order order = new Order(30, merchantUID, "testuuid", drinkList, OrderStatus.SENT, 1000, "TestName");
        boolean error = false;
        Database database = new Database();
        database.user.setAddress("user address");
        database.user.setLocation(new SerializablePoint());
        try {
            database.sendDelivery(order);
        }
        catch (Exception e) {
            error = true;
            e.printStackTrace();
        }
        assertEquals(false, error);
    }

    @Test
    public void updateCaffeineDailyTest() {
        boolean error = false;
        Database database = new Database();
        database.user.setAddress("user address");
        database.user.setLocation(new SerializablePoint());
        try {
            Database.updateCaffeineDaily();
        }
        catch (Exception e) {
            error = true;
            e.printStackTrace();
        }
        assertEquals(false, error);
    }

    @Test
    public void castUserTest(){
        //cast merchant
        try {
            final User[] result = new User[1];
            result[0] = null;
            Merchant m = new Merchant("Sup Man", "testUID", "testemail@gmail.com", 11);
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("/users/merchants/" + m.getUid());
            userReference.setValue(m);
            Database.getMerchantReference("testUID").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    result[0] = Database.castUser(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            while (result[0] == null){
                Thread.yield();
            }

            assertEquals(false, (result[0].getIsCustomer()));
        }
        catch (Exception e){
            fail(e.getMessage());
        }


        //cast customer
        try {
            final User[] result = new User[1];
            result[0] = null;
            Customer m = new Customer("Sup Man", "testUID", "testemail@gmail.com", 11);
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("/users/customers/" + m.getUid());
            userReference.setValue(m);
            Database.getCustomerReference("testUID").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    result[0] = Database.castUser(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            while (result[0] == null){
                Thread.yield();
            }

            assertEquals(true, (result[0].getIsCustomer()));
        }
        catch (Exception e){
            fail(e.getMessage());
        }

    }

    @Test
    public void updateUserTest() throws InterruptedException {
        //large change to user
        Database.getUser(true, new SwitchDisplayCallback() {
            @Override
            public void switchToMerchantDisplay() {

            }

            @Override
            public void switchToCustomerDisplay() {

            }
        });

        while (Database.user == null){Thread.yield();}

        User previousUser = Database.user;
        previousUser.setBalance(190);
        previousUser.setName("Sup Man");
        previousUser.setPhoneNumber("0192");
        Database.updateUserInfo(previousUser);
        Thread.sleep(3000);
        zDatabaseTest.continueTest(previousUser);
        while(!done){
            Thread.yield();
        }

        if (fail) fail();
    }

    public static void continueTest(User previousUser){
        try {
            Gson gsonHelper = new GsonBuilder().create();

            assertEquals(gsonHelper.toJson(previousUser), gsonHelper.toJson(Database.user));
        }
        catch (Exception e){
            fail = true;
            done = true;
        }
        fail = false;
        done = true;
    }

    @Test
    public void merchantLocationTest() throws InterruptedException {
        Collection<Merchant> list = Database.getNearbyMerchants(null, null);
        Thread.sleep(3000);
        assertEquals(false, list.size() == 0);
    }
}
