package com.csci310.uscdoordrink;

import java.util.ArrayList;
import java.util.List;

public class Merchant extends User{
    private String merchantDescription;
    private DeliveryList deliveryList;
    private List<String> deliveries;
    private Menu menu;
    private Boolean hasDeliveryinProgress;
    private String modeOfTransport;
    private Boolean isCoffee = true;
//list of strings
    public Menu getMenu() {return menu;}
    public void setMenu(Menu m){menu = m;}

    public DeliveryList getDeliveryList() {return deliveryList;}
    public void setDeliveryList(DeliveryList d){deliveryList = d;}

    public String getMerchantDescription() {return merchantDescription;}
    public void setMerchantDescription(String m){merchantDescription = m;}

    public Boolean getHasDeliveryinProgress() {return hasDeliveryinProgress;}
    public void setHasDeliveryinProgress(Boolean m){hasDeliveryinProgress = m;}

    public Boolean getIsCoffee() {return isCoffee;}
    public void setIsCoffee(Boolean m){isCoffee = m;}

    public String getModeOfTransport() {return modeOfTransport;}
    public void setModeOfTransport(String m){modeOfTransport = m;}

    public List<String> getDeliveries() { return deliveries; }
    public void setDeliveries(List<String> deliveries) { this.deliveries = deliveries; }


    Merchant(){ super(); }

    Merchant(String name, String UID, String email, int balance){
        super(name, UID, email, balance, false);
        merchantDescription = "";
        deliveryList = new DeliveryList();
        hasDeliveryinProgress = false;
        modeOfTransport = "Cycling";//to change
        menu = new Menu();
        deliveries = new ArrayList<>();

    }

}
