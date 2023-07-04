package com.example.myapplication.View;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.scrounger.countrycurrencypicker.library.Country;
import com.scrounger.countrycurrencypicker.library.CountryCurrencyPicker;
import com.scrounger.countrycurrencypicker.library.Currency;
import com.scrounger.countrycurrencypicker.library.Listener.CountryCurrencyPickerListener;
import com.scrounger.countrycurrencypicker.library.PickerType;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("currency_symble","0").equals("0")){
            findViewById(R.id.select_country).setVisibility(View.VISIBLE);
            findViewById(R.id.welcome_btn).setVisibility(View.GONE);
            findViewById(R.id.prog).setVisibility(View.GONE);
        }else {
            findViewById(R.id.select_country).setVisibility(View.GONE);
            findViewById(R.id.welcome_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.prog).setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), dashboard.class));
                    finish();
                }
            },500);
        }

        findViewById(R.id.select_country).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountryCurrencyPicker pickerDialog = CountryCurrencyPicker.newInstance(PickerType.COUNTRYandCURRENCY, new CountryCurrencyPickerListener() {
                    @Override
                    public void onSelectCountry(Country country) {
                        SharedPreferences SharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = SharedPreferences.edit();
                        myEdit.putString("Id", "1234");
                        myEdit.putString("currency_symble", country.getCurrency().getSymbol());
                        myEdit.putString("country_code", "");//country.getCode()+
                        myEdit.apply();
                        startActivity(new Intent(getApplicationContext(), dashboard.class));
                        finish();
                    }
                    @Override
                    public void onSelectCurrency(Currency currency) {
                    }
                });
                pickerDialog.show(getSupportFragmentManager(), CountryCurrencyPicker.DIALOG_NAME);
            }
        });
    }

}