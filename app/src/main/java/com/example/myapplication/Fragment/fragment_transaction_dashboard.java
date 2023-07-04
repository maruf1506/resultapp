package com.example.myapplication.Fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.TransactionAdapter;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.Constant;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;

public class fragment_transaction_dashboard extends Fragment implements AdapterView.OnItemSelectedListener {

    String user_id;
    Spinner spinner;
    Cursor cursor;
    ArrayList<String> transaction_name, transaction_phone_number, transaction_time, transaction_amount, transaction_sender_id, transaction_id;
    ArrayList<Bitmap> transaction_image;
    TransactionAdapter transactionAdapter;
    RecyclerView transactionrecyclerview;
    DatabaseHelper myDB;
    private Date oneWayTripDate;
    LinearLayout download_button;
    private final static int PERMISSION_REQUEST_CODE = 200;
    TextView textView;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_transaction_dashboard, container, false);
        spinner = root.findViewById(R.id.spinner1);
        transactionrecyclerview = root.findViewById(R.id.transactionrecyclerview);
        download_button = root.findViewById(R.id.download_button);
        textView = root.findViewById(R.id.prog);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        sharedPreferences = getActivity().getSharedPreferences("UserDetails", MODE_PRIVATE);
        if (sharedPreferences.getInt("save_4",0)==0){
            Constant.showcase(getActivity(),"Select History","You can manage your history",spinner,12,14,4);
        }
        if (sharedPreferences.getInt("save_5",0)==0){
            Constant.showcase(getActivity(),"Create PDF","CLick here to generate your pdf file",download_button,12,14,5);
        }

        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsTableEmpty(cursor)) {
                    Toast.makeText(getActivity(),"Not Enough Data",Toast.LENGTH_SHORT).show();
                }else {
                    if (checkPermission()) {
                        generatePDF(transactionrecyclerview);
                    } else {
                        requestPermission();
                    }
                }
            }
        });

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        myDB = new DatabaseHelper(getContext());
        user_id = sharedPreferences.getString("Id", "");

        transaction_name = new ArrayList<>();
        transaction_phone_number = new ArrayList<>();
        transaction_sender_id = new ArrayList<>();
        transaction_time = new ArrayList<>();
        transaction_amount = new ArrayList<>();
        transaction_image = new ArrayList<>();
        transaction_id = new ArrayList<>();


        if (i == 0) {
            cursor = myDB.all_transaction(user_id);
        } else if (i == 1) {
            cursor = myDB.credit_transaction(user_id);
        } else if (i == 2) {
            cursor = myDB.debit_transaction(user_id);
        }
        if (IsTableEmpty(cursor)) {
            textView.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                transaction_phone_number.add(sharedPreferences.getString("country_code","0")+cursor.getString(1));
                transaction_name.add(cursor.getString(2));
                if (cursor.getBlob(3) == null) {
                    transaction_image.add(null);
                } else {
                    transaction_image.add(BitmapFactory.decodeByteArray(cursor.getBlob(3), 0, cursor.getBlob(3).length));
                }
                transaction_sender_id.add(cursor.getString(5));
                transaction_amount.add(cursor.getString(7));
                String date = cursor.getString(8);
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy hh:mm a");
                try {
                    oneWayTripDate = input.parse(date);  // parse input
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                transaction_time.add(output.format(oneWayTripDate));
                transaction_id.add(cursor.getString(9));
            }
        }
        transactionAdapter = new TransactionAdapter(getContext(), user_id, transaction_sender_id, transaction_name, transaction_phone_number, transaction_amount, transaction_time, transaction_image, transaction_id);
        transactionrecyclerview.setAdapter(transactionAdapter);
        transactionrecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void generatePDF(RecyclerView view) {

        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }

                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.WHITE);

            com.itextpdf.text.Document document = new Document(PageSize.A4);
            String mfile = new SimpleDateFormat("yMdms", Locale.getDefault()).format(System.currentTimeMillis());
            final File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "_" + mfile + ".pdf");
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < size; i++) {
                try {
                    //Adding the content to the document
                    Bitmap bmp = bitmaCache.get(String.valueOf(i));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    com.itextpdf.text.Image image = Image.getInstance(stream.toByteArray());
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                    image.scalePercent(scaler);
                    image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER | com.itextpdf.text.Image.ALIGN_TOP);
                    if (!document.isOpen()) {
                        document.open();
                    }
                    document.add(image);
                } catch (Exception ex) {
                    Log.e("TAG-ORDER PRINT ERROR", ex.getMessage());
                }
            }
            if (document.isOpen()) {
                document.close();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Success")
                    .setMessage("PDF File Created Successfully.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }

                    }).show();

        }

    }

    public boolean IsTableEmpty(Cursor cursor) {
        return !(cursor.getCount() > 0);
    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (writeStorage && readStorage) {
                    generatePDF(transactionrecyclerview);
                } else {
                    Toast.makeText(getActivity(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
