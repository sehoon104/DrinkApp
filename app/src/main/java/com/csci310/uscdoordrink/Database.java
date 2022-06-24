package com.csci310.uscdoordrink;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    Database() {user = new User();};
    DatabaseReference conn;
    public static volatile User user;
    public static volatile boolean hasSwitched = false;
    public static Map<String, Delivery> deliveries;
    public static DeliveryList inprogresslist;
    public static DeliveryList cleanList;
    public static Map<DatabaseReference, ValueEventListener> valueEventListenerMap;

    public static void initvalueeventmap(){
        valueEventListenerMap = new HashMap<>();
    }

    public static boolean isLoggedin(){
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /*
    *
    *
    * This function solely deals with logging in firebase
    *
    * */
    public static void loginUser(String idToken){
        FirebaseAuth fireDatabase = FirebaseAuth.getInstance();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fireDatabase.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = fireDatabase.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });



    }
    public static void testOrdersAdd(){

    }

    /*
    * Assumes the user is logged in
    * */
    public static String getUID(){
        if(FirebaseAuth.getInstance() != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "";
    }

    /*

        Continuously updates the user object and related strings

     * Assumes the user is logged in, will return null otherwise.
     * */

    public static void getUser(Boolean isCustomer, SwitchDisplayCallback callback){
        try{
            String UID = getUID();
            FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference userReference;



            if (isCustomer) userReference = getCustomerReference(UID);
            else userReference = getMerchantReference(UID);

            if(!isCustomer) {
                fetchAllDeliveries();
            }
            ValueEventListener userrefval = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.getChildrenCount() == 0) {
                            Database.user = (isCustomer) ? freshCustomer(fuser) : freshMerchant(fuser);
                            userReference.setValue((isCustomer) ? (Customer) Database.user : (Merchant) Database.user);
                        } else {
                            Database.user = castUser(snapshot);
                            //if its a customer and there is a delivery (not already in user variable), make a new intent and go to the delivery status page (this will call getdeliveryStatus)
                            //if its a merchant and there are deliveries that are not already in user variable, notify the user/go to the delivery list.
                        }

                        if (!hasSwitched) {
                            hasSwitched = true;
                            //Taken Out. May Change later
                            if (Database.user.getIsCustomer()) {
                                callback.switchToCustomerDisplay();
                            } else {
                                callback.switchToMerchantDisplay();
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        Log.w("w", "Cast user problem");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Firebase", "error in updating user");
                    return;
                }
            };
            userReference.addValueEventListener(userrefval);
            valueEventListenerMap.put(userReference, userrefval);
            return;
        }
        catch(Exception e){
            Log.w("Database Class", e.getMessage());
            return;
        }
    }

    public static Merchant getMerchantByUID(String UID){
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("/users/merchants/" + UID);
        final Merchant[] toBeReturned = {null};
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                toBeReturned[0] = (Merchant) castUser(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "error in retrieving Merchant user");
            }
        });
        while(toBeReturned[0] == null){Thread.yield();}
        return toBeReturned[0];
    }

    private static Customer freshCustomer(FirebaseUser fuser){return new Customer(fuser.getDisplayName(), fuser.getUid(), fuser.getEmail(), 0);}

    private static Merchant freshMerchant(FirebaseUser fuser){return new Merchant(fuser.getDisplayName(), fuser.getUid(), fuser.getEmail(), 0);}

    public static User castUser(DataSnapshot snapshot){
        Gson gson = new GsonBuilder().create();
        JsonObject temp = gson.fromJson(gson.toJson(snapshot.getValue(Object.class)), JsonObject.class);
        if (temp.get("isCustomer").getAsBoolean()) return gson.fromJson(temp, Customer.class);
        else return gson.fromJson(temp, Merchant.class);
    }

    private static Object getObject(DataSnapshot snapshot){
        Gson gson = new GsonBuilder().create();
        return snapshot.getValue(Object.class);
    }

    public static void sendDelivery(Order orderToSend){

        Delivery newDelivery = new Delivery(orderToSend,DeliveryStatus.PENDING, Database.user.getAddress(), Database.user.getLocation());

        DatabaseReference dataref = FirebaseDatabase.getInstance().getReference("/users/deliveries");
        DatabaseReference customerref = FirebaseDatabase.getInstance().getReference("/users/customers/"+orderToSend.getCustomerUUID()+"/deliveries");
        DatabaseReference merchantref = FirebaseDatabase.getInstance().getReference("/users/merchants/"+orderToSend.getMerchantUUID());
        dataref.push().setValue(newDelivery, (error, ref) -> {
            String deliveryuid = ref.getKey();

            merchantref.child("deliveries").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    ArrayList<String> deliveries = (ArrayList<String>)snapshot.getValue();
                    if (deliveries == null) {
                        deliveries = new ArrayList<>();
                    }
                    deliveries.add(deliveryuid);
                    merchantref.child("deliveries").setValue(deliveries);
                    merchantref.child("hasDeliveryinProgress").setValue(true);

                    DeliveryStatusPage.uid = deliveryuid;
                    DeliveryStatusPage.delivery = newDelivery;
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            customerref.setValue(deliveryuid);
            //context.finis();
        });
    }


    public static void receiveDeliveryStatus(String deliveryUID, CustomerDisplayRunnable callback){
        //get snapshot of the delivery
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/users/deliveries/"+deliveryUID);
        ValueEventListener vale = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Delivery delivery = snapshot.getValue(Delivery.class);
                    if (delivery == null) {
                        callback.run(null, new SerializablePoint(0, 0));
                        return;
                    }
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("/users/merchants/" + delivery.getOrder().getMerchantUUID()).child("merchantLocation");
                    ValueEventListener vakk = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get location of merchant, it will be nested so we have the merchantID
                            SerializablePoint merchantlocation = snapshot.getValue(SerializablePoint.class);
                            //when you get snapshot, do callback.run(delivery, SerializablePoint)
                            if (delivery.getLocation() != null)
                                MapPage.updateMerchantLocation(delivery.getLocation().getLat(), delivery.getLocation().getLon());
                            callback.run(delivery, merchantlocation);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    databaseReference1.addValueEventListener(vakk);
                    valueEventListenerMap.put(databaseReference1, vakk);
                }
                catch(Exception e){
                    Log.w("W", "Error in receive Delivery Status");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(vale);
        valueEventListenerMap.put(databaseReference, vale);

    }



    public static Collection<Merchant> getNearbyMerchants(MapPage map, SymbolManager symbol) {
        Collection<Merchant> nearbymer = new ArrayList<>();
        DatabaseReference userReference = getMerchantReference("");
        ValueEventListener vale = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.getChildren()){
                    nearbymer.add((Merchant) castUser(i));
                }
                if (map != null) map.positionIconsOnMap(symbol, nearbymer);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "error in retrieving all merchants");
            }
        };
        userReference.addValueEventListener(vale);
        if (valueEventListenerMap != null) valueEventListenerMap.put(userReference, vale);

        return nearbymer;
    }

    public static DatabaseReference getMerchantReference(String merchantUUID){
        return FirebaseDatabase.getInstance().getReference("/users/merchants/" + merchantUUID);
    }

    public static DatabaseReference getCustomerReference(String UUID) {
        return FirebaseDatabase.getInstance().getReference("/users/customers/" + UUID);
    }

    public static void getMenu(String merchantUUID, MenuRunnable runnable){
            DatabaseReference Drinkref = getMerchantReference(merchantUUID).child("menu");
            Drinkref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String temp = new GsonBuilder().create().toJson(getObject(snapshot));
                    Menu m = new GsonBuilder().create().fromJson(temp, Menu.class);
                    runnable.run(m);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Firebase", "Could not get menu in getMenu");
                }
            });
            return;
        }

    public static void addDrink(String merchantUUID, Drink newDrink){
        try{
            DatabaseReference merchant = getMerchantReference(merchantUUID);
            merchant.child("menu").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Menu merchantMenu;
                    String test = new GsonBuilder().create().toJson(getObject(snapshot));
                    merchantMenu = new GsonBuilder().create().fromJson(test, Menu.class);
                    if (merchantMenu == null) merchantMenu = new Menu();
                    merchantMenu.createDrink(newDrink);
                    merchant.child("menu").setValue(merchantMenu);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Firebase Error: ", "Could not add drink");
                }
            });

        }catch (Exception e){
            Log.e("Error: ", e.getMessage());
        }
    }


    public static void removeDrink(String merchantUUID, Integer drinkUUID){
        DatabaseReference menuRef;

        try{
            menuRef = getMerchantReference(merchantUUID).child("menu");
            menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Menu m;
                    String test = new GsonBuilder().create().toJson(getObject(snapshot));
                    m = new GsonBuilder().create().fromJson(test, Menu.class);
                    Log.w("H", test);
                    m.removeDrink(drinkUUID);
                    menuRef.setValue(m);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception e){
            Log.e("Error: ", e.getMessage());
        }

    }

    public static void removeDelivery(Delivery delivery){//Remove function for merchant
        ((Merchant)Database.user).getDeliveries().remove(delivery.getUID());
        FirebaseDatabase.getInstance().getReference("/users/deliveries/" +delivery.getUID()).removeValue();

        DatabaseReference merchref = FirebaseDatabase.getInstance().getReference("/users/merchants/" + delivery.getOrder().getMerchantUUID() +"/deliveries");
        DatabaseReference customerref = FirebaseDatabase.getInstance().getReference("/users/customers/" +delivery.getOrder().getCustomerUUID() +"/deliveries");

        customerref.removeValue();
        merchref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> deliver = (List<String>) snapshot.getValue();
                if (deliver != null) {
                    deliver.remove(delivery.getUID());
                    merchref.setValue(deliver);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //add to completedOrders
        delivery.getOrder().setETA(new Date(System.currentTimeMillis()));
        FirebaseDatabase.getInstance().getReference("/users/completedOrdersCustomers/"+delivery.getOrder().getCustomerUUID()+"/"+
                delivery.getOrder().getUuid()).setValue(delivery.getOrder());
        FirebaseDatabase.getInstance().getReference("/users/completedOrdersMerchants/"+delivery.getOrder().getMerchantUUID()+"/"+
                delivery.getUID()).setValue(delivery);


    }

    public static void updateUserInfo(User user) {
        String UID = getUID();
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userReference;
        if (user.getIsCustomer()) userReference = FirebaseDatabase.getInstance().getReference("/users/customers/" + UID);
        else userReference = FirebaseDatabase.getInstance().getReference("/users/merchants/" + UID);

        userReference.setValue((user.getIsCustomer())? (Customer) user : (Merchant) user);

        return;
    }

    public static void  receiveDeliveryStatus(Delivery delivery) {
        //TODO
    }

    public static void updateCaffeineDaily() {//for customer use
        //retrieve completed order data
        DatabaseReference completedorders = FirebaseDatabase.getInstance().getReference("/users/completedOrdersCustomers/" + user.getUid());
        completedorders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Order> completed = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        completed.add(ds.getValue(Order.class));
                    }
                }
                //get the most recent order. if null, reset.
                DatabaseReference caffeine = FirebaseDatabase.getInstance().getReference("/users/customers/" + user.getUid() + "/caffeineIntake");
                if (completed.size() != 0) {
                    Order recent = completed.get(completed.size() - 1);
                    //Check if date change. If yes, reset the caffeine intake of the user.
                    Date today = new Date(System.currentTimeMillis());
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    String today_date = format.format(today);
                    String recentorder = format.format((recent.getTimestamp()));
                    if (!today_date.equals(recentorder)) {
                        resetcaf(caffeine);
                    }
                }
                else {
                    if (user.getIsCustomer())
                        resetcaf(caffeine);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private static void resetcaf(DatabaseReference databaseReference){
        databaseReference.setValue(0);
        if (Database.user.getIsCustomer()) {
            ((Customer)user).setCaffeineIntake(0);
        }
    }
    public static void fetchAllDeliveries(){
            DatabaseReference deliveryref = FirebaseDatabase.getInstance().getReference("/users/deliveries");
            ValueEventListener val = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (Database.user == null) deliveryref.removeEventListener(this);
                        deliveries = new HashMap<>();
                        List<Delivery> deliveriesinp = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Delivery delivery = ds.getValue(Delivery.class);
                            delivery.setUid(ds.getKey());
                            deliveries.put(ds.getKey(), delivery);
                            deliveriesinp.add(delivery);
                        }
                        inprogresslist = new DeliveryList(deliveriesinp);
                        deliveryparse();
                    } catch (Exception e) {
                        Log.w("w", "Fetch all deliveries problem");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            deliveryref.addValueEventListener(val);
            valueEventListenerMap.put(deliveryref, val);
    }

    public static void deliveryparse() {
        if (Database.user == null || inprogresslist == null) return;
        List<Delivery> alldeliveries = inprogresslist.getDeliveries();
        List<Delivery> merchant_delivery = new ArrayList<>();
        for (Delivery delivery : alldeliveries){
            if(getUID().equals("")) {
                return;
            }

            if (delivery != null &&  delivery.getOrder().getMerchantUUID().equals(getUID()))
                merchant_delivery.add(delivery);
        }
        inprogresslist = new DeliveryList(merchant_delivery);
        cleanList = inprogresslist;

        if (DeliveryListPage.handler == null){
            DeliveryListPage.setHandler();
        }
    }

    public static void updateCaffeineforComplete(Delivery delivery) {
        DatabaseReference caffeineintake = FirebaseDatabase.getInstance().getReference("/users/customers/"+delivery.getOrder().getCustomerUUID()+"/caffeineIntake");
        caffeineintake.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentcaff = snapshot.getValue(Integer.class);
                Integer newcaff = currentcaff + delivery.getOrder().getCaffeineIntake();
                caffeineintake.setValue(newcaff);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void updateBalanceforComplete(Delivery delivery) {
        DatabaseReference bb = FirebaseDatabase.getInstance().getReference("/users/customers/"+delivery.getOrder().getCustomerUUID()+"/balance");
        bb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer balanceCurr = snapshot.getValue(Integer.class);
                Integer newb = balanceCurr - delivery.getOrder().getCost();
                bb.setValue(newb);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void sendLocation(Delivery i) {
        LatLng lng = MapPage.getMerchantCurrentLocation();
        if (lng == null) return;
        i.setLocation(new SerializablePoint(lng.getLatitude(), lng.getLongitude()));
        DatabaseReference deliveryref = FirebaseDatabase.getInstance().getReference("/users/deliveries/" + i.getUID());
        deliveryref.setValue(i);
    }

    public static void removeeventlisteners() {
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerMap.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
    }
}
