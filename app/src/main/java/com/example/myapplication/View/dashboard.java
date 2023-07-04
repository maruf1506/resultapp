package com.example.myapplication.View;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.Constant;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;
import com.example.myapplication.Fragment.fragment_customer_dashboard;
import com.example.myapplication.Fragment.fragment_home_dashboard;
import com.example.myapplication.Fragment.fragment_cardmaker;
import com.example.myapplication.Fragment.fragment_transaction_dashboard;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    DrawerLayout drawerLayout;
    NavigationView side_navigation;
    ImageView sidebar_icon;
    TextView status_bar_text, transaction_date;
    String user_id;
    SharedPreferences sharedPreferences;

    public void transaction_chat(View view) {
        MaterialCardView card = (MaterialCardView) view;
        String a = card.getTag().toString();
        Intent intent = new Intent(getApplicationContext(), transaction_chat.class);
        intent.putExtra("Friend_id", a);
        startActivity(intent);
    }

    public void transaction_summary(View view) {
        String transaction_id, customer_image_alert_text = null, transaction_sender_id = null, transaction_receiver_id = null, transaction_amount_text = null, transaction_remarks_text = null, transaction_date_text = null, sender_id, customer_phone_number_alert_text = null;
        final ImageView close_alert, share_icon;
        TextView transaction_amount, customer_image_alert, transaction_remarks, transaction_time, customer_phone_number_alert, transactionamountsymbol;
        final MaterialCardView alert_dialog;
        final LinearLayout share_layout;

        MaterialCardView card = (MaterialCardView) view;
        transaction_id = card.getTag().toString();
        DatabaseHelper myDB = new DatabaseHelper(this);
        Cursor cursor = myDB.get_transaction_details(transaction_id);

        while (cursor.moveToNext()) {
            transaction_sender_id = cursor.getString(1);
            transaction_receiver_id = cursor.getString(2);
            transaction_amount_text = cursor.getString(3);
            transaction_remarks_text = cursor.getString(5);
            transaction_date_text = cursor.getString(7);
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_transaction_alert);
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        alert_dialog = dialog.findViewById(R.id.alert_dialog);
        close_alert = dialog.findViewById(R.id.close_alert);
        customer_image_alert = dialog.findViewById(R.id.customer_image);
        customer_phone_number_alert = dialog.findViewById(R.id.customer_contact_number);
        transactionamountsymbol = dialog.findViewById(R.id.transactionamountsymbol);
        transactionamountsymbol.setText(sharedPreferences.getString("currency_symble", "0"));
        transaction_amount = dialog.findViewById(R.id.transaction_amount);
        transaction_remarks = dialog.findViewById(R.id.transaction_remarks);
        transaction_time = dialog.findViewById(R.id.transaction_time);
        share_icon = dialog.findViewById(R.id.share_icon);
        share_layout = dialog.findViewById(R.id.share_layout);

        if (transaction_sender_id.compareTo(user_id) == 0) {
            sender_id = transaction_receiver_id;
            transaction_amount.setText("- " + transaction_amount_text);
            transaction_amount.setTextColor(getApplicationContext().getResources().getColor(R.color.warning));
            transactionamountsymbol.setTextColor(getApplicationContext().getResources().getColor(R.color.warning));
            close_alert.setImageResource(R.drawable.alertbox_cross_icon_debit);
        } else {
            sender_id = transaction_sender_id;
            transaction_amount.setText("+ " + transaction_amount_text);
            transaction_amount.setTextColor(getApplicationContext().getResources().getColor(R.color.sucess));
            transactionamountsymbol.setTextColor(getApplicationContext().getResources().getColor(R.color.sucess));
            close_alert.setImageResource(R.drawable.alertbox_cross_icon_credit);
            share_icon.setImageResource(R.drawable.credit_share_icon);
        }

        Cursor cursor1 = myDB.get_user_details(sender_id);

        while (cursor1.moveToNext()) {
            customer_phone_number_alert_text = cursor1.getString(0);
            customer_image_alert_text = cursor1.getString(1).substring(0, 1);
        }

        transaction_remarks.setText(transaction_remarks_text);
        transaction_time.setText(transaction_date_text);
        customer_phone_number_alert.setText(customer_phone_number_alert_text);
        customer_image_alert.setText(customer_image_alert_text);
        Random random = new Random();
        int random_int = random.nextInt(5);
        switch (random_int) {
            case 0:
                customer_image_alert.setBackgroundResource(R.drawable.circle_bg0);
                break;
            case 1:
                customer_image_alert.setBackgroundResource(R.drawable.circle_bg1);
                break;
            case 2:
                customer_image_alert.setBackgroundResource(R.drawable.circle_bg2);
                break;
            case 3:
                customer_image_alert.setBackgroundResource(R.drawable.circle_bg3);
                break;
            case 4:
                customer_image_alert.setBackgroundResource(R.drawable.circle_bg4);
                break;
        }

        close_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        share_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                share_layout.setVisibility(View.INVISIBLE);
                close_alert.setVisibility(View.INVISIBLE);
                Bitmap bitmap = Bitmap.createBitmap(alert_dialog.getWidth(), alert_dialog.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                alert_dialog.draw(canvas);

                try {
                    String mfile = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
                    File file = new File(getApplicationContext().getExternalCacheDir(), File.separator + getString(R.string.app_name) + "_" + mfile + ".jpg");
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, false);
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("image/jpg");
                    startActivity(Intent.createChooser(intent, "Share image via"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().hide();

        sharedPreferences = getApplicationContext().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("Id", "");
        final DatabaseHelper myDB = new DatabaseHelper(getApplicationContext());
        /*Cursor cursor = myDB.get_user_details(user_id);
        while (cursor.moveToNext()){
            user_name = cursor.getString(1);
            user_business_name = cursor.getString(2);
            user_image = cursor.getBlob(4);
        }*/

        /* Side Navigation */

        drawerLayout = findViewById(R.id.side_drawer);
        side_navigation = findViewById(R.id.side_navigation);
        sidebar_icon = findViewById(R.id.sidebar_icon);
        navigationDrawer();



        /* Status Bar */
        status_bar_text = findViewById(R.id.status_bar_text);

        /* Bottom Navigation Fragment */

        final BottomNavigationView bottom_nav = findViewById(R.id.bottom_navigation);
        bottom_nav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new fragment_home_dashboard()).commit();
        if (sharedPreferences.getInt("save_1", 0) == 0) {
            Constant.showcase(dashboard.this, "Add Transaction", "Add your all transaction here and\n make easier", bottom_nav, 12, 14,1);
        }
    }

    /* Side Navigation */
    private void navigationDrawer() {
        side_navigation.bringToFront();
        side_navigation.setNavigationItemSelectedListener(this);
        side_navigation.setCheckedItem(R.id.nav_share);
        if (sharedPreferences.getInt("save_2", 0) == 0) {
            Constant.showcase(dashboard.this, "Extra Feature", "* Card Maker", sidebar_icon, 12, 14,2);
        }
        sidebar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment selectedFragment = null;
        switch (menuItem.getItemId()) {
            case R.id.nav_qrcode:
                selectedFragment = new fragment_cardmaker();
                status_bar_text.setText(R.string.qrdashboardstatusbar);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, selectedFragment).commit();
                break;
            case R.id.nav_help:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.HELP_URL)));
                break;
            case R.id.nav_privary:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.PRIVACY_URL)));
                break;
            case R.id.nav_rate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                break;
            case R.id.nav_share:
                String message = "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /* Bottom Navigation Fragment */

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            Fragment selectedFragment = null;
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new fragment_home_dashboard();
                    status_bar_text.setText(R.string.homedashboardstatusbar);
                    break;
                case R.id.nav_user:
                    selectedFragment = new fragment_customer_dashboard();
                    status_bar_text.setText(R.string.selectcustomerdashboardstatusbar);
                    break;
                case R.id.nav_book:
                    selectedFragment = new fragment_transaction_dashboard();
                    status_bar_text.setText(R.string.transactionlistdashboardstatusbar);
                    break;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, selectedFragment).commit();
            return true;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
        }
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String month_name = new DateFormatSymbols().getMonths()[month];
        String temp_date = String.valueOf(day) + " - " + month_name.substring(0, 3) + " - " + String.valueOf(year);
        transaction_date.setText(temp_date);

    }
}