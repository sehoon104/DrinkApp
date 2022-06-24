package com.csci310.uscdoordrink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity {

    FragmentManager fragmentManager;
    static boolean returningFromSuccessFulCheckout;
    public static List<String> pendingOrdersString;
    public static List<Delivery> pendingOrderDeliveries;

    BottomNavigationView bottomNavigationView;
    Fragment activeFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        returningFromSuccessFulCheckout = false;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        //inprogresslist = new DeliveryList();
        if (Database.user.getIsCustomer()) Database.updateCaffeineDaily();

        fragmentManager = getSupportFragmentManager();
        if (!Database.user.getIsCustomer()) {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_merchant);
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_customer);
        }

        if (savedInstanceState != null) {
            Fragment restoreFragment = fragmentManager.getFragment(savedInstanceState, savedInstanceState.getString("PreviousFragmentStr"));
            fragmentManager.beginTransaction().show(restoreFragment);
        } else {
            fragmentManager.beginTransaction().add(R.id.rlContainer, new MapPage(), "MapPage").commit();
        }


        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment activeFragment = null;
            switch (item.getItemId()) {
                case R.id.navMapPage:
                    if (fragmentManager.findFragmentByTag("MapPage") != null) {
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("MapPage")).commit();
                    } else {
                        fragmentManager.beginTransaction().add(R.id.rlContainer, new MapPage(), "MapPage").commit();
                    }
                    activeFragment = fragmentManager.findFragmentByTag("MapPage");
                    break;
                case R.id.navPlotScreen:
                    if (fragmentManager.findFragmentByTag("PlotScreen") != null) {
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("PlotScreen")).commit();
                    } else {
                        fragmentManager.beginTransaction().add(R.id.rlContainer, new PlotScreenSelect(), "PlotScreen").commit();
                    }
                    activeFragment = fragmentManager.findFragmentByTag("PlotScreen");

                    break;
                case R.id.historyscreen:
                    if (fragmentManager.findFragmentByTag("History") != null) {
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("History")).commit();
                    } else {
                        fragmentManager.beginTransaction().add(R.id.rlContainer, new MerchantHistorySelect(), "History").commit();
                    }
                    activeFragment = fragmentManager.findFragmentByTag("History");
                    break;
                case R.id.navDeliveryList:
                    if (fragmentManager.findFragmentByTag("DeliveryList") != null) {
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("DeliveryList")).commit();
                    } else {
                        fragmentManager.beginTransaction().add(R.id.rlContainer, new DeliveryListPage(), "DeliveryList").commit();
                    }
                    activeFragment = fragmentManager.findFragmentByTag("DeliveryList");

                    break;

                case R.id.navDeliveryStatus:
                    if (fragmentManager.findFragmentByTag("DeliveryStatus") != null) {
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("DeliveryStatus")).commit();
                    } else {
                        fragmentManager.beginTransaction().add(R.id.rlContainer, new DeliveryStatusPage(), "DeliveryStatus").commit();
                    }
                    activeFragment = fragmentManager.findFragmentByTag("DeliveryStatus");

                    break;

                case R.id.navSettingsPage:
                    if (fragmentManager.findFragmentByTag("SettingsPage") != null) {
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("SettingsPage")).commit();
                    } else {
                        fragmentManager.beginTransaction().add(R.id.rlContainer, new ProfileSettings(), "SettingsPage").commit();
                    }
                    activeFragment = fragmentManager.findFragmentByTag("SettingsPage");
                    break;
                case R.id.navLogout:
                    FirebaseAuth.getInstance().signOut();
                    LoginPage.mGoogleSignInClient.signOut();
                    Database.hasSwitched = false;
                    Database.removeeventlisteners();
                    SharedPreferences preferences = getSharedPreferences("info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    Toast.makeText(this.getApplicationContext(), "Thank you for using the app", Toast.LENGTH_SHORT).show();
                    editor.putString("accountType", "");
                    editor.commit();
                    finish();
                    break;
                default:
                    //activeFragment = mapPage;
                    break;

            }

            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment != activeFragment) {
                    fragmentManager.beginTransaction().hide(fragment).commit();
                }
            }


            return true;
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (returningFromSuccessFulCheckout) {
            bottomNavigationView.setSelectedItemId(R.id.navDeliveryStatus);
        }
        returningFromSuccessFulCheckout = false;
    }

    private void saveAccountType() {
        SharedPreferences preferences = getSharedPreferences("info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (Database.user.getIsCustomer()) {
            editor.putString("accountType", "customer");
        } else {
            editor.putString("accountType", "merchant");
        }
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        saveAccountType();
        Database.deliveryparse();
        DeliveryHandler();
    }

    public void DeliveryHandler() {
        if (!Database.user.getIsCustomer()) {
            DatabaseReference haspendingref = FirebaseDatabase.getInstance().getReference("/users/merchants/" + Database.getUID() + "/hasDeliveryinProgress");
            ValueEventListener vale = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (Database.user == null) haspendingref.removeEventListener(this);
                    else {
                        if (!Database.user.getIsCustomer()) {
                            boolean hasinp = snapshot.getValue(Boolean.class);
                            if (hasinp) {
                                pendingOrdersString = new ArrayList<>();

                                pendingOrderDeliveries = new ArrayList<>();
                                List<String> deliverystrings = ((Merchant) Database.user).getDeliveries();
                                if (deliverystrings != null) {
                                    for (String deliverystring : deliverystrings) {
                                        if (deliverystring != null) {
                                            Delivery del = Database.deliveries.get(deliverystring);//May have to change since this depends on ondatachange func run after database updates
                                            if (del != null && del.getStatus() == DeliveryStatus.PENDING) {
                                                pendingOrderDeliveries.add(del);
                                                pendingOrdersString.add(deliverystring);
                                            }
                                        }
                                    }
                                }
                                //buildmuitialerts(0,pendingOrderDeliveries.size(),pendingOrderDeliveries);

                            }
                        } else {
                            haspendingref.removeEventListener(this);
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            haspendingref.addValueEventListener(vale);
            Database.valueEventListenerMap.put(haspendingref, vale);
        } else {
            //check deliveries string. If not null, then fetch data from /users/deliveries and check if declined.
            //String deliverystring = ((Customer)Database.user).getDeliveries();
            String customeruid = Database.getUID();
            DatabaseReference currentdelivery = FirebaseDatabase.getInstance().getReference("/users/customers/" + customeruid + "/deliveries");
            ValueEventListener vall = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (Database.user == null) currentdelivery.removeEventListener(this);
                    else {
                        String newdelivery = snapshot.getValue(String.class);
                        if (newdelivery != null) {
                            DatabaseReference pendingdelivery = FirebaseDatabase.getInstance().getReference("/users/deliveries/" + newdelivery);
                            pendingdelivery.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Delivery delivery = snapshot.getValue(Delivery.class);
                                    if (Database.user == null)
                                        pendingdelivery.removeEventListener(this);
                                    else if (Database.user.getIsCustomer() && delivery != null) {
                                        if (delivery.getStatus() == DeliveryStatus.REJECTED) {
                                            ((Customer) Database.user).setDeliveries(null);
                                            Toast.makeText(getApplicationContext(), "Your Order is Rejected.", Toast.LENGTH_SHORT).show();
                                            FirebaseDatabase.getInstance().getReference("/users/customers/" + customeruid + "/deliveries").removeValue();
                                            FirebaseDatabase.getInstance().getReference("/users/deliveries/" + newdelivery).removeValue();

                                        } else if (delivery.getStatus() == DeliveryStatus.IN_PROGRESS) {
                                            //Delivery Status Page intent. Pass in delivery info for further tracking

                                        }
                                        pendingdelivery.removeEventListener(this);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            currentdelivery.addValueEventListener(vall);
            Database.valueEventListenerMap.put(currentdelivery, vall);
        }
    }

    private void buildmuitialerts(int index, int size, List<Delivery> deliveryList) {
        if (index < size) {
            AlertDialog.Builder orderalert = new AlertDialog.Builder(MainPage.this);
            Order order = deliveryList.get(index).getOrder();
            String message = "Order: " + order.getUuid() + "\n";//Edit to Show brief order stats
            for (Drink drink : order.getDrinks()) {
                String addingstr = "x" + drink.getQuantity() + " ";
                addingstr += drink.getName() + "\n";
                message += addingstr;
            }
            orderalert.setMessage(message);
            orderalert.setCancelable(false);
            orderalert.setPositiveButton("Accept",
                    (dialogInterface, i) -> {
                        //TODO: set delivery to in progress
                        //AcceptOrder(index);
                        buildmuitialerts(index + 1, size, deliveryList);
                    });
            orderalert.setNegativeButton("Decline",
                    (dialogInterface, i) -> {
                        //TODO: Delete the delivery string from merchant => this can also be done customer-end if messy
                        //RejectOrder(index);
                        buildmuitialerts(index + 1, size, deliveryList);
                    });
            if (!isFinishing()) {
                orderalert.create().show();
            }
        }
        if (index == size) {
            FirebaseDatabase.getInstance().getReference("/users/merchants/" + Database.getUID() + "/hasDeliveryinProgress").setValue(false);
            RejectOrdersInList();
        }
    }

    private void RejectOrdersInList() {
        FirebaseDatabase.getInstance().getReference("/users/merchants/" + Database.getUID() + "/deliveries").setValue(((Merchant) Database.user).getDeliveries());

    }

    private void RejectOrder(int index) {
        String deliveryid = pendingOrdersString.get(index);
        //Get List and remove.
        ((Merchant) Database.user).getDeliveries().remove(deliveryid);
        FirebaseDatabase.getInstance().getReference("/users/deliveries/" + deliveryid + "/status").setValue(DeliveryStatus.REJECTED);
    }

    private void AcceptOrder(int index) {
        String deliveryid = pendingOrdersString.get(index);
        FirebaseDatabase.getInstance().getReference("/users/deliveries/" + deliveryid + "/status").setValue(DeliveryStatus.IN_PROGRESS);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
        if (fragmentManager != null) {
            Fragment mapPage = fragmentManager.findFragmentByTag("MapPage");
            Fragment settingsPage = fragmentManager.findFragmentByTag("SettingsPage");
            Fragment plotScreen = fragmentManager.findFragmentByTag("PlotScreen");
            Fragment deliveryList = fragmentManager.findFragmentByTag("DeliveryList");
            Fragment deliveryStatus = fragmentManager.findFragmentByTag("DeliveryStatus");
            Fragment merchanthistory = fragmentManager.findFragmentByTag("History");
            if (mapPage != null && mapPage.isVisible()) {
                fragmentManager.putFragment(outState, "MapPage", mapPage);
                outState.putString("PreviousFragmentStr", "MapPage");
            } else if (plotScreen != null && plotScreen.isVisible()) {
                fragmentManager.putFragment(outState, "PlotScreen", plotScreen);
                outState.putString("PreviousFragmentStr", "PlotScreen");
            } else if (deliveryList != null && deliveryList.isVisible()) {
                fragmentManager.putFragment(outState, "DeliveryList", deliveryList);
                outState.putString("PreviousFragmentStr", "DeliveryList");
            } else if (deliveryStatus != null && deliveryStatus.isVisible()) {
                fragmentManager.putFragment(outState, "DeliveryStatus", deliveryStatus);
                outState.putString("PreviousFragmentStr", "DeliveryStatus");
            } else if (settingsPage != null && settingsPage.isVisible()) {
                fragmentManager.putFragment(outState, "SettingsPage", settingsPage);
                outState.putString("PreviousFragmentStr", "SettingsPage");
            } else if (merchanthistory != null && merchanthistory.isVisible()) {
                fragmentManager.putFragment(outState, "History", merchanthistory);
                outState.putString("PreviousFragmentStr", "History");
            }
        }


    }


    public void onClickMerchantBubble(View view) {
        Gson gson = new GsonBuilder().create();
        String j = gson.toJson(view.getTag());
        Merchant jj = gson.fromJson(j, Merchant.class);
        Intent drinksPageStarter = new Intent(this, BrowseDrinksPage.class);
        drinksPageStarter.putExtra("data", gson.toJson((jj).getMenu()));
        drinksPageStarter.putExtra("merchantInfo", j);
        startActivity(drinksPageStarter);

    }

    @Override
    public void onBackPressed(){
        //do nothing
        return;
    }
}
