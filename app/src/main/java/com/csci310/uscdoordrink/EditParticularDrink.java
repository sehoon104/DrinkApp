package com.csci310.uscdoordrink;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.NumberFormat;

public class EditParticularDrink extends AppCompatActivity {

    EditText title;
    EditText description;
    EditText caffeineContent;
    Spinner drinkType;
    ImageView drinkImage;
    Bitmap drinkImageBitmapUpdated = null;
    EditText price;
    EditText discount;

    int position;

    TextView totalPriceWithDiscount;
    ActivityResultLauncher<Intent> launcher;

    Button onCancel;
    Button onDelete;
    Button onSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_individual_drink);

        title = findViewById(R.id.particularDrinkTitle);
        description = findViewById(R.id.particularDrinkDescription);
        caffeineContent = findViewById(R.id.particularDrinkCaffeineContent);
        drinkImage = findViewById(R.id.particularDrinkImage);
        drinkType = findViewById(R.id.particularDrinkType);
        price = findViewById(R.id.particularDrinkPrice);
        discount = findViewById(R.id.particularDrinkDiscount);

        totalPriceWithDiscount = findViewById(R.id.particularPriceWithDiscount);


        drinkType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                drinkImage.setVisibility(View.VISIBLE);
                if (drinkType.getSelectedItem().toString().equals("Coffee")) {
                    drinkImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.coffee_mug));
                } else if (drinkType.getSelectedItem().toString().equals("Tea")) {
                    drinkImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.tea_cup));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        price.addTextChangedListener(new TextWatcher() {
            String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            //Credit to https://stackoverflow.com/questions/5107901/better-way-to-format-currency-input-edittext
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    price.removeTextChangedListener(this);

                    String cleanString = charSequence.toString().replaceAll("[$,.]", "");
                    double parsed = Double.parseDouble(cleanString);

                    String formatted = NumberFormat.getCurrencyInstance().format(parsed / 100);
                    current = formatted;
                    price.setText(formatted);
                    price.setSelection(formatted.length());
                    price.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String priceStringParsed = price.getText().toString().replaceAll("[$,.]", "");
                String discountStringParsed = discount.getText().toString().replaceAll("[$,.]", "");

                int priceInt = Integer.parseInt(priceStringParsed);
                int discountInt = Integer.parseInt(discountStringParsed);


                if (discountInt <= priceInt) {
                    totalPriceWithDiscount.setText("Total: " + CheckoutPageActivity.formatPrice(priceInt - discountInt));
                } else {
                    totalPriceWithDiscount.setText("Discount cannot be greater than price");
                }
            }
        });
        discount.addTextChangedListener(new TextWatcher() {
            String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            //Credit to https://stackoverflow.com/questions/5107901/better-way-to-format-currency-input-edittext
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    discount.removeTextChangedListener(this);
                    ;
                    String cleanString = charSequence.toString().replaceAll("[$,.]", "");
                    double parsed = Double.parseDouble(cleanString);

                    String formatted = NumberFormat.getCurrencyInstance().format(parsed / 100);
                    current = formatted;
                    discount.setText(formatted);

                    discount.setSelection(formatted.length());

                    discount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String priceStringParsed = price.getText().toString().replaceAll("[$,.]", "");
                String discountStringParsed = discount.getText().toString().replaceAll("[$,.]", "");

                int priceInt = Integer.parseInt(priceStringParsed);
                int discountInt = Integer.parseInt(discountStringParsed);


                if (discountInt <= priceInt) {
                    totalPriceWithDiscount.setText("Total: " + CheckoutPageActivity.formatPrice(priceInt - discountInt));
                } else {
                    totalPriceWithDiscount.setText("Discount cannot be greater than price");
                }


            }
        });

        Intent intent = getIntent();
        title.setText(intent.getStringExtra("title"));
        description.setText(intent.getStringExtra("description"));
        caffeineContent.setText(intent.getStringExtra("caffeineContent"));
        price.setText(intent.getStringExtra("price"));
        discount.setText(intent.getStringExtra("discount"));
        String drinkTypeData = intent.getStringExtra("drinkTypeData");
        int position = intent.getIntExtra("position", -1);

        boolean isNewDrink = intent.getBooleanExtra("isNewDrink", false);
        if(!isNewDrink) {
            String priceStringParsed = price.getText().toString().replaceAll("[$,.]", "");
            String discountStringParsed = discount.getText().toString().replaceAll("[$,.]", "");

            int priceInt = Integer.parseInt(priceStringParsed);
            int discountInt = Integer.parseInt(discountStringParsed);

            totalPriceWithDiscount.setText("Total: " + CheckoutPageActivity.formatPrice(priceInt - discountInt));
        } else {
            totalPriceWithDiscount.setText("");
        }

        if (drinkTypeData.equals("tea")) {
            drinkType.setSelection(0);
        } else {
            drinkType.setSelection(1);
        }


        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {


                    } else {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(data.getData());
                            //Updates images on screen
                            drinkImageBitmapUpdated = BitmapFactory.decodeStream(inputStream);
                            drinkImage.setImageBitmap(drinkImageBitmapUpdated);

                        } catch (FileNotFoundException e) {
                            //Shouldn't ever occur if an image was found
                            Log.e("", "File not found exception");
                        }
                    }

                }
            }
        });

        onCancel = findViewById(R.id.particularDrinkCancelButton);
        onCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        onSave = findViewById(R.id.particularDrinkSaveButton);
        /**
         * Save Drink Data here
         */
        onSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleString = title.getText().toString();
                String descriptionString = description.getText().toString();
                String caffeineString = caffeineContent.getText().toString().replaceAll("[\\D]", "");


                String priceString = price.getText().toString().replaceAll("[\\D]", "");
                String discountString = discount.getText().toString().replaceAll("[\\D]", "");

                Log.e("", "" + discountString);


                String drinkTypeString = drinkType.getSelectedItem().toString();

                if (drinkImageBitmapUpdated != null) {
                    //TODO save the updated bitmap if it was updated (could use filestreams or something);
                }

                if (titleString.isEmpty() || descriptionString.isEmpty() || caffeineString.isEmpty() || priceString.isEmpty() || drinkTypeString.isEmpty()) {
                    Toast.makeText(EditParticularDrink.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (Integer.parseInt(priceString) < Integer.parseInt(discountString)) {
                    Toast.makeText(EditParticularDrink.this, "Discount can not be greater than price.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int hash;

                if (intent.getBooleanExtra("isNewDrink", true))
                    hash = new Drink(0, titleString, descriptionString, drinkTypeString.equals("Tea"), Integer.parseInt(priceString), Integer.parseInt(caffeineString)).hashCode();
                else
                    hash = Integer.valueOf(intent.getStringExtra("uid").substring(0, intent.getStringExtra("uid").length()));
                Database.addDrink(Database.user.getUid(), new Drink(hash, titleString, descriptionString, drinkTypeString.equals("Tea"), Integer.parseInt(priceString), Integer.parseInt(discountString), Integer.parseInt(caffeineString)));

                finish();
            }
        });


        onDelete = findViewById(R.id.particularDrinkDeleteButton);
        if (intent.getBooleanExtra("isNewDrink", false)) {
            onDelete.setVisibility(View.GONE);
        }
        onDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert();
            }
        });
    }

    private void showAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Drink")
                .setMessage("Are you sure about that?")
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /**
                         * Delete drink here
                         */
                        String numUid = getIntent().getStringExtra("uid").substring(0, getIntent().getStringExtra("uid").length());
                        Database.removeDrink(Database.user.getUid(), Integer.valueOf(numUid));

                        finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


}