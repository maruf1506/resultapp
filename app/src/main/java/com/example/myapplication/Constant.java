package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

import static android.content.Context.MODE_PRIVATE;

public class Constant {
    public static  String PRIVACY_URL = "";
    public static  String HELP_URL = "";



    public static void showcase(Context context, String title, String description, View view,int font_content,int font_title,int save_point){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        new GuideView.Builder(context)
                .setTitle(title)
                .setContentText(description)
                .setGravity(Gravity.center)
                .setDismissType(DismissType.anywhere)
                .setTargetView(view)
                .setContentTextSize(font_content)
                .setTitleTextSize(font_title)
                .setGuideListener(new GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        switch (save_point){
                            case 1:
                                editor.putInt("save_1",1).apply();
                                break;
                            case 2:
                                editor.putInt("save_2",1).apply();
                                break;
                            case 3:
                                editor.putInt("save_3",1).apply();
                                break;
                            case 4:
                                editor.putInt("save_4",1).apply();
                                break;
                            case 5:
                                editor.putInt("save_5",1).apply();
                                break;
                        }
                    }
                })
                .build()
                .show();
    }

}
