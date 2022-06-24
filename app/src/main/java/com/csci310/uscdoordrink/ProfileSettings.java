package com.csci310.uscdoordrink;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import java.text.NumberFormat;
import java.util.Locale;

public class ProfileSettings extends Fragment {

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private TextView address;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (Database.user.getIsCustomer())
            return inflater.inflate(R.layout.settings_activity, container, false);
        else return inflater.inflate(R.layout.merchant_settings_activity, container, false);
    }


    /*
    Use "view." to interact with the the settings view
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Database.user.getIsCustomer()) populateCustomerSettings(view);
        else populateMerchantSettings(view);

        return;
    }


    /*
     *
     * Although the populateCustomerSettings is a subset of the populateMerchantSettings function, I have kept them seperate in order to make it v easy to change the settings in the future
     *
     *
     * */
    private void populateCustomerSettings(View view) {

        Customer user = (Customer) Database.user;

        EditText prefName = (EditText) view.findViewById(R.id.profileName);
        prefName.setText(user.getName());
        prefName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setName(prefName.getText().toString());
                Database.updateUserInfo(user);
            }

        });

        EditText balance = (EditText) view.findViewById(R.id.balance);
        balance.addTextChangedListener(new TextWatcher() {
            String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    balance.removeTextChangedListener(this);
                    String cleanString = charSequence.toString().replaceAll("[$,.]", "");
                    double parsed = Double.parseDouble(cleanString);

                    String formatted = NumberFormat.getCurrencyInstance().format(parsed / 100);
                    current = formatted;
                    balance.setText(formatted);
                    balance.setSelection(formatted.length());
                    balance.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                return;
            }
        });
        balance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String x = balance.getText().toString().replaceAll("[\\D]", "");
                if ((x != null) && (!x.equals(""))) {
                    user.setBalance(new Integer(x));
                    Database.updateUserInfo(user);
                }
            }
        });
        balance.setText(String.valueOf(user.getBalance()));

        EditText pNumber = (EditText) view.findViewById(R.id.phoneNumber);
        pNumber.addTextChangedListener(new TextWatcher() {
            String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    pNumber.removeTextChangedListener(this);
                    String cleanString = charSequence.toString().replaceAll("[( )+-]", "");


                    String formatted = PhoneNumberUtils.formatNumber(cleanString, Locale.getDefault().getCountry());
                    if (formatted == null) {
                        formatted = cleanString;
                    }
                    current = formatted;
                    pNumber.setText(formatted);
                    pNumber.setSelection(formatted.length());
                    pNumber.addTextChangedListener(this);
                }

                return;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        pNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setPhoneNumber(pNumber.getText().toString().replaceAll("[( )+-]", ""));
                Database.updateUserInfo(user);
            }
        });
        pNumber.setText(user.getPhoneNumber());


        address = (TextView) view.findViewById(R.id.address);
        if (Database.user.getAddress() != null && !Database.user.getAddress().isEmpty()) {
            address.setText(Database.user.getAddress());
        } else {
            address.setText("Not Set");
        }
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });

        address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setAddress(address.getText().toString());
                Database.updateUserInfo(user);
                return;
            }
        });

        return;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            String placeName = selectedCarmenFeature.placeName();
            double placeLat = ((Point) selectedCarmenFeature.geometry()).latitude();
            double placeLong = ((Point) selectedCarmenFeature.geometry()).longitude();

            Database.user.setLocation(new SerializablePoint(placeLat, placeLong));
            address.setText(placeName);
            Database.user.setAddress(address.getText().toString());
            Database.updateUserInfo(Database.user);
        }


    }

    private void populateMerchantSettings(View view) {

        Merchant user = (Merchant) Database.user;

        EditText prefName = (EditText) view.findViewById(R.id.profileName);
        prefName.setText(user.getName());
        prefName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setName(prefName.getText().toString());
                Database.updateUserInfo(user);
            }
        });
        EditText balance = (EditText) view.findViewById(R.id.balance);
        balance.addTextChangedListener(new TextWatcher() {
            String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    balance.removeTextChangedListener(this);
                    ;
                    String cleanString = charSequence.toString().replaceAll("[$,.]", "");
                    if (cleanString.length() > 9) {
                        return;
                    }
                    double parsed = Double.parseDouble(cleanString);

                    String formatted = NumberFormat.getCurrencyInstance().format(parsed / 100);
                    current = formatted;
                    balance.setText(formatted);
                    ;
                    balance.setSelection(formatted.length());
                    balance.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        balance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setBalance(new Integer(balance.getText().toString().replaceAll("[\\D]", "")));
                Database.updateUserInfo(user);
            }
        });
        balance.setText(String.valueOf(user.getBalance()));

        EditText pNumber = (EditText) view.findViewById(R.id.phoneNumber);
        pNumber.addTextChangedListener(new TextWatcher() {
            String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    pNumber.removeTextChangedListener(this);
                    String cleanString = charSequence.toString().replaceAll("[( )+-]", "");


                    String formatted = PhoneNumberUtils.formatNumber(cleanString, Locale.getDefault().getCountry());
                    if (formatted == null) {
                        formatted = cleanString;
                    }
                    current = formatted;
                    pNumber.setText(formatted);
                    pNumber.setSelection(formatted.length());
                    pNumber.addTextChangedListener(this);
                }

                return;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        pNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setPhoneNumber(pNumber.getText().toString().replaceAll("[( )+-]", ""));
                Database.updateUserInfo(user);
            }
        });
        pNumber.setText(user.getPhoneNumber());


        address = (TextView) view.findViewById(R.id.address);
        if (Database.user.getAddress() != null && !Database.user.getAddress().isEmpty()) {
            address.setText(Database.user.getAddress());
        } else {
            address.setText("Not Set");
        }
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
        address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setAddress(address.getText().toString());
                Database.updateUserInfo(user);
                return;
            }
        });

        EditText merchantDescription = view.findViewById(R.id.merchantPlaceDescription);
        merchantDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                user.setMerchantDescription(merchantDescription.getText().toString());
                Database.updateUserInfo(user);
                return;
            }
        });
        merchantDescription.setText(user.getMerchantDescription());

        /**
         * Access menu data go through here
         */
        Button accessMenuButton = view.findViewById(R.id.accessMenuButton);
        accessMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MerchantMenuEditActivity.class);
                startActivity(intent);

            }
        });

        Spinner transport = view.findViewById(R.id.transport);
        String defaultTransport = ((Merchant) (Database.user)).getModeOfTransport();
        if (defaultTransport != null && !defaultTransport.equals("")) {
            int i = 0;
            if (defaultTransport.charAt(0) == 'C') {
                i = 1;
            } else if (defaultTransport.charAt(0) == 'D') {
                i = 0;
            } else {
                i = 2;
            }
            transport.setSelection(i);
        }
        transport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                user.setModeOfTransport(adapterView.getSelectedItem().toString());
                Database.updateUserInfo(user);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner merchantType = getView().findViewById(R.id.merchantType);
        boolean defaultMerchantType = ((Merchant)(Database.user)).getIsCoffee();
        if(defaultMerchantType) {
            merchantType.setSelection(1);
        } else {
            merchantType.setSelection(0);
        }

        merchantType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().equals("Coffee")) {
                    user.setIsCoffee(true);
                } else {
                    user.setIsCoffee(false);
                }
                Database.updateUserInfo(user);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return;
    }


}