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
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.ChatAdapter;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class transaction_chat extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    LinearLayout share_layout;
    ImageView sidebar_icon;
    Intent intent;
    String Friend_id, user_id,symble;
    ArrayList<String> transaction_amount, transaction_time, transaction_remarks, transaction_sender_id, transaction_id;
    Cursor cursor;
    RecyclerView transaction_chat_recycle;
    ChatAdapter chatAdapter;
    TextView transaction_debit, transaction_credit;
    TextView transaction_date, save_debit, transaction_name, transaction_balance, save_credit, friend_name, error_msg_transaction_amount, error_msg_transaction_date, error_msg_transaction_name;
    String transaction_date_text, transaction_name_text, transaction_balance_text, Friend_name;
    TextInputLayout transaction_amount_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_chat);
        getSupportActionBar().hide();

        sidebar_icon = findViewById(R.id.sidebar_icon);
        transaction_chat_recycle = findViewById(R.id.transaction_chat_recycle);
        transaction_debit = findViewById(R.id.transaction_debit);
        transaction_credit = findViewById(R.id.transaction_credit);
        friend_name = findViewById(R.id.friend_name);
        share_layout = findViewById(R.id.share_layout);


        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("Id", "");
        symble = sharedPreferences.getString("currency_symble","0");
        intent = getIntent();
        Friend_id = intent.getStringExtra("Friend_id");

        DatabaseHelper myDB = new DatabaseHelper(this);
        Cursor cursor = myDB.get_user_details(Friend_id);
        while (cursor.moveToNext()) {
            Friend_name = cursor.getString(1);
        }

        friend_name.setText(Friend_name);

        transaction_sender_id = new ArrayList<>();
        transaction_amount = new ArrayList<>();
        transaction_remarks = new ArrayList<>();
        transaction_time = new ArrayList<>();
        transaction_id = new ArrayList<>();

        transaction_debit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(transaction_chat.this);
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_debit_dialog);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                transaction_balance = bottomSheetDialog.findViewById(R.id.transaction_amount);
                transaction_amount_layout = bottomSheetDialog.findViewById(R.id.transaction_amount_layout);
                transaction_amount_layout.setHint("Transaction Amount. Ex. "+symble);
                transaction_name = bottomSheetDialog.findViewById(R.id.transaction_name);
                transaction_date = bottomSheetDialog.findViewById(R.id.transaction_date);
                error_msg_transaction_amount = bottomSheetDialog.findViewById(R.id.error_msg_transaction_amount);
                error_msg_transaction_date = bottomSheetDialog.findViewById(R.id.error_msg_transaction_date);
                error_msg_transaction_name = bottomSheetDialog.findViewById(R.id.error_msg_transaction_name);
                save_debit = bottomSheetDialog.findViewById(R.id.save_debit);

                transaction_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(transaction_chat.this, transaction_chat.this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePickerDialog.show();
                    }
                });

                save_debit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transaction_balance_text = transaction_balance.getText().toString();
                        transaction_name_text = transaction_name.getText().toString();
                        transaction_date_text = transaction_date.getText().toString();

                        int count1 = 1, count2 = 1, count3 = 1;

                        error_msg_transaction_name.setVisibility(View.GONE);
                        error_msg_transaction_amount.setVisibility(View.GONE);
                        error_msg_transaction_date.setVisibility(View.GONE);

                        if (transaction_name_text.isEmpty()) {
                            count1 = 0;
                            error_msg_transaction_name.setVisibility(View.VISIBLE);
                            error_msg_transaction_name.setText("This is required");
                        }
                        if (transaction_balance_text.isEmpty()) {
                            count2 = 0;
                            error_msg_transaction_amount.setVisibility(View.VISIBLE);
                            error_msg_transaction_amount.setText("This is required");
                        }
                        if (transaction_date_text.isEmpty()) {
                            count3 = 0;
                            error_msg_transaction_date.setVisibility(View.VISIBLE);
                            error_msg_transaction_date.setText("This is required");
                        }
                        if (count1 == 1 && count2 == 1 && count3 == 1) {
                            DatabaseHelper myDB = new DatabaseHelper(getApplicationContext());
                            if (!myDB.storeNewDebitTransaction(user_id, Friend_id, transaction_balance_text, transaction_name_text, transaction_date_text)) {
                                Toast.makeText(transaction_chat.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                            transaction_sender_id = new ArrayList<>();
                            transaction_amount = new ArrayList<>();
                            transaction_remarks = new ArrayList<>();
                            transaction_time = new ArrayList<>();
                            transaction_id = new ArrayList<>();
                            Fetch_Transaction(user_id, Friend_id);
                            chatAdapter = new ChatAdapter(getApplicationContext(), user_id, transaction_sender_id, transaction_amount, transaction_remarks, transaction_time, transaction_id);
                            transaction_chat_recycle.setAdapter(chatAdapter);
                            transaction_chat_recycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            transaction_chat_recycle.smoothScrollToPosition(transaction_amount.size());
                            bottomSheetDialog.hide();
                        }
                    }
                });
                bottomSheetDialog.show();
            }
        });


        transaction_credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(transaction_chat.this);
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_credit_dialog);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                transaction_balance = bottomSheetDialog.findViewById(R.id.transaction_amount);
                transaction_amount_layout = bottomSheetDialog.findViewById(R.id.transaction_amount_layout);
                transaction_amount_layout.setHint("Transaction Amount. Ex. "+symble);
                transaction_name = bottomSheetDialog.findViewById(R.id.transaction_name);
                transaction_date = bottomSheetDialog.findViewById(R.id.transaction_date);
                error_msg_transaction_amount = bottomSheetDialog.findViewById(R.id.error_msg_transaction_amount);
                error_msg_transaction_date = bottomSheetDialog.findViewById(R.id.error_msg_transaction_date);
                error_msg_transaction_name = bottomSheetDialog.findViewById(R.id.error_msg_transaction_name);
                save_credit = bottomSheetDialog.findViewById(R.id.save_credit);

                transaction_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(transaction_chat.this, transaction_chat.this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePickerDialog.show();
                    }
                });

                save_credit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transaction_balance_text = transaction_balance.getText().toString();
                        transaction_name_text = transaction_name.getText().toString();
                        transaction_date_text = transaction_date.getText().toString();

                        int count1 = 1, count2 = 1, count3 = 1;

                        error_msg_transaction_name.setText("");
                        error_msg_transaction_amount.setText("");
                        error_msg_transaction_date.setText("");

                        if (transaction_name_text.isEmpty()) {
                            count1 = 0;
                            error_msg_transaction_name.setText("This is required");
                        }
                        if (transaction_balance_text.isEmpty()) {
                            count2 = 0;
                            error_msg_transaction_amount.setText("This is required");
                        }
                        if (transaction_date_text.isEmpty()) {
                            count3 = 0;
                            error_msg_transaction_date.setText("This is required");
                        }
                        if (count1 == 1 && count2 == 1 && count3 == 1) {
                            DatabaseHelper myDB = new DatabaseHelper(getApplicationContext());
                            if (!myDB.storeNewCreditTransaction(user_id, Friend_id, transaction_balance_text, transaction_name_text, transaction_date_text)) {
                                Toast.makeText(transaction_chat.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                            transaction_sender_id = new ArrayList<>();
                            transaction_amount = new ArrayList<>();
                            transaction_remarks = new ArrayList<>();
                            transaction_time = new ArrayList<>();
                            transaction_id = new ArrayList<>();
                            Fetch_Transaction(user_id, Friend_id);
                            chatAdapter = new ChatAdapter(getApplicationContext(), user_id, transaction_sender_id, transaction_amount, transaction_remarks, transaction_time, transaction_id);
                            transaction_chat_recycle.setAdapter(chatAdapter);
                            transaction_chat_recycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            transaction_chat_recycle.smoothScrollToPosition(transaction_amount.size());
                            bottomSheetDialog.hide();
                        }
                    }
                });
                bottomSheetDialog.show();
            }
        });


        Fetch_Transaction(user_id, Friend_id);
        chatAdapter = new ChatAdapter(getApplicationContext(), user_id, transaction_sender_id, transaction_amount, transaction_remarks, transaction_time, transaction_id);
        transaction_chat_recycle.setAdapter(chatAdapter);
        transaction_chat_recycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        transaction_chat_recycle.smoothScrollToPosition(transaction_amount.size());

    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    public void Fetch_Transaction(String userid, String friendid) {
        Date oneWayTripDate = null;
        DatabaseHelper myDB = new DatabaseHelper(this);
        cursor = myDB.user_friend_transaction(userid, friendid);
        while (cursor.moveToNext()) {
            transaction_sender_id.add(cursor.getString(0));
            transaction_amount.add(cursor.getString(1));
            transaction_remarks.add(cursor.getString(2));
            String date = cursor.getString(3);
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy hh:mm a");
            try {
                oneWayTripDate = input.parse(date);  // parse input
            } catch (ParseException e) {
                e.printStackTrace();
            }
            transaction_time.add(output.format(oneWayTripDate));
            transaction_id.add(cursor.getString(4));
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String month_name = new DateFormatSymbols().getMonths()[month];
        String temp_date = String.valueOf(day) + " - " + month_name.substring(0, 3) + " - " + String.valueOf(year);
        transaction_date.setText(temp_date);

    }

    public void share_transaction(View view) {
        String transaction_id;
        String transaction_sender_id = null;
        String transaction_receiver_id = null;
        String transaction_amount_text = null;
        String transaction_remarks_text = null;
        String transaction_date_text = null;
        String sender_id;
        String customer_phone_number_alert_text = null;
        final String[] user_name = new String[1];
        final String[] user_business_name = new String[1];
        String customer_image_alert_text = null;
        final ImageView close_alert, share_icon;
        TextView transaction_amount, transaction_remarks, transaction_time, customer_phone_number_alert,transactionamountsymbol,customer_image_alert;
        final MaterialCardView alert_dialog;
        final LinearLayout share_layout;

        LinearLayout card = (LinearLayout) view;
        transaction_id = card.getTag().toString();
        final DatabaseHelper myDB = new DatabaseHelper(this);
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
        customer_phone_number_alert.setText(sharedPreferences.getString("country_code","0"));
        transactionamountsymbol = dialog.findViewById(R.id.transactionamountsymbol);
        transactionamountsymbol.setText(sharedPreferences.getString("currency_symble","0"));
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
            customer_image_alert_text = cursor1.getString(1).substring(0,1).toUpperCase();
        }

        transaction_remarks.setText(transaction_remarks_text);
        transaction_time.setText(transaction_date_text);
        customer_phone_number_alert.setText(customer_phone_number_alert_text);
        customer_image_alert.setText(customer_image_alert_text);
        Random random = new Random();
        int random_int = random.nextInt(5);
        switch (random_int){
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
                Cursor cursor2 = myDB.get_user_details(user_id);
                while (cursor2.moveToNext()) {
                    user_name[0] = cursor2.getString(1);
                    user_business_name[0] = cursor2.getString(2);
                }

                try {
                    String mfile=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
                    File file = new File(getApplicationContext().getExternalCacheDir(), File.separator +getString(R.string.app_name)+"_"+mfile+ ".jpg");
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

}