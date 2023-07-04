package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Random;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    Context context;
    ArrayList friend_id,transaction_name,transaction_phone_number,transaction_amount,transaction_image;
    int lastPosition = -1;

    public ContactAdapter(Context context,ArrayList friend_id, ArrayList transaction_name, ArrayList transaction_phone_number, ArrayList transaction_amount,ArrayList transaction_image) {
        this.context = context;
        this.friend_id = friend_id;
        this.transaction_name = transaction_name;
        this.transaction_phone_number = transaction_phone_number;
        this.transaction_amount = transaction_amount;
        this.transaction_image = transaction_image;
    }

    @NonNull
    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.contact_row,parent,false);
        return new ContactAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.MyViewHolder holder, int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        holder.friend_id_text.setTag(String.valueOf(friend_id.get(position)));
        holder.transaction_name_text.setText(String.valueOf(transaction_name.get(position)));
        holder.customer_image.setText(String.valueOf(transaction_name.get(position)).substring(0,1).toUpperCase());
        Random random = new Random();
        int random_int = random.nextInt(5);
        switch (random_int){
            case 0:
                holder.customer_image.setBackgroundResource(R.drawable.circle_bg0);
                break;
            case 1:
                holder.customer_image.setBackgroundResource(R.drawable.circle_bg1);
                break;
            case 2:
                holder.customer_image.setBackgroundResource(R.drawable.circle_bg2);
                break;
            case 3:
                holder.customer_image.setBackgroundResource(R.drawable.circle_bg3);
                break;
            case 4:
                holder.customer_image.setBackgroundResource(R.drawable.circle_bg4);
                break;
        }
        holder.transaction_phone_number_text.setText(String.valueOf(transaction_phone_number.get(position)));
        holder.transactionamountsymbol.setText(sharedPreferences.getString("currency_symble","0"));
        if((Integer) transaction_amount.get(position) >= 0){
            holder.transaction_amount_text.setText("+ "+String.valueOf(transaction_amount.get(position)));
            holder.transaction_amount_text.setTextColor(context.getResources().getColor(R.color.sucess));
            holder.transactionamountsymbol.setTextColor(context.getResources().getColor(R.color.sucess));
        }else{
            holder.transaction_amount_text.setText("- "+String.valueOf(Math.abs((Integer) transaction_amount.get(position))));
            holder.transaction_amount_text.setTextColor(context.getResources().getColor(R.color.warning));
            holder.transactionamountsymbol.setTextColor(context.getResources().getColor(R.color.warning));
        }
        setAnimation(holder.itemView,position);
    }

    private void setAnimation(View itemView, int position) {
        if(position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context,android.R.anim.slide_in_left);
            itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return transaction_name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView transaction_name_text,transaction_phone_number_text,transaction_amount_text,transactionamountsymbol;
        MaterialCardView friend_id_text;
        TextView customer_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            friend_id_text = itemView.findViewById(R.id.transction);
            transaction_name_text = itemView.findViewById(R.id.customername);
            transaction_phone_number_text = itemView.findViewById(R.id.customerphonenumber);
            transaction_amount_text = itemView.findViewById(R.id.transactionamount);
            transactionamountsymbol = itemView.findViewById(R.id.transactionamountsymbol);
            customer_image = itemView.findViewById(R.id.customer_image);
        }
    }

}
