package com.berkayyetis.CampBook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.berkayyetis.CampBook.databinding.ActivityDetailsBinding;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity{

    SQLiteDatabase database;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    private ActivityDetailsBinding binding;
     ArrayList<String> equipmentList;
    Boolean firstIn=false;
    EquipmentAdapter equipmentAdapter;
    static Integer artId;
    String info;
    PageViewModel pageViewModel;
    String lati,longi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar

        actionBar.setDisplayHomeAsUpEnabled(true);
        registerLauncher();
        database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);

        Intent intent = getIntent();
        info = intent.getStringExtra("info");

        if (info.matches("new")) {
            binding.artNameText.setText("");
            binding.painterNameText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
        } else {
            artId = intent.getIntExtra("artId",1);
            binding.button.setVisibility(View.INVISIBLE);
            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.updateButton.setVisibility(View.VISIBLE);
            binding.equipmentButton.setVisibility(View.VISIBLE);
            try {
                Cursor cursor = database.rawQuery("SELECT * FROM camps WHERE id = ?",new String[] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("campname");
                int painterNameIx = cursor.getColumnIndex("date");
                int yearIx = cursor.getColumnIndex("day");
                int latiIx = cursor.getColumnIndex("latitude");
                int longiIx = cursor.getColumnIndex("longitude");
                while (cursor.moveToNext()) {
                    binding.artNameText.setText(cursor.getString(artNameIx));
                    binding.painterNameText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));
                    lati =cursor.getString(latiIx);
                    longi =cursor.getString(longiIx);
                    setTitle(cursor.getString(artNameIx));
                }

                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        equipmentList = new ArrayList<String>();
        equipmentList.add("Cadir");
        getData();
        pageViewModel= new ViewModelProvider(this).get(PageViewModel.class);
        pageViewModel.getVal().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                firstIn = true;
                getData();
            }
        });

    }

    public void registerLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intentFromResult = result.getData();
                            if (intentFromResult != null) {
                                Uri imageData = intentFromResult.getData();
                            }
                        }
                    }
                });
    }
    public void dbSave(){
        String artName = binding.artNameText.getText().toString();
        String painterName = binding.painterNameText.getText().toString();
        String year = binding.yearText.getText().toString();
        try {
            database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS camps (id INTEGER PRIMARY KEY,campname VARCHAR, date VARCHAR, day VARCHAR, latitude VARCHAR, longitude VARCHAR)");
            database.execSQL("CREATE TABLE IF NOT EXISTS equipments (id INTEGER PRIMARY KEY,equipmentname VARCHAR,campsId INTEGER,FOREIGN KEY(campsId) REFERENCES camps(id))");
            String sqlString = "INSERT INTO camps (campname, date, day) VALUES (?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.execute();
        } catch (Exception e) {

        }

        Intent intent = new Intent(DetailsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }
    public void save(View view) {
        if(!binding.artNameText.getText().toString().equals("")) {
            dbSave();
        }
        else{
            Toast.makeText(this, "Kamp adi bos gecilemez.", Toast.LENGTH_SHORT).show();
        }

    }



    public void deleteClicked(View view) {
        database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
        String artName = binding.artNameText.getText().toString();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to delete "+artName + "?");
        alertDialogBuilder.setTitle("Delete "+artName + "?");
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String artName = binding.artNameText.getText().toString();
                String painterName = binding.painterNameText.getText().toString();
                String year = binding.yearText.getText().toString();
                database.delete("camps", "id=?", new String[]{artId.toString()});
                database.delete("equipments", "campsId=?", new String[]{artId.toString()});
                Intent intent = new Intent(DetailsActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }


    public void updateClicked(View view) {

        database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
        String artName = binding.artNameText.getText().toString();
        String painterName = binding.painterNameText.getText().toString();
        String year = binding.yearText.getText().toString();

        ContentValues cv = new ContentValues();
        cv.put("campname",artName);
        cv.put("date",painterName);
        cv.put("day",year);

        database.update("camps",cv, "id=?", new String[]{artId.toString()});
        Intent intent = new Intent(DetailsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void equipmentListClicked(View view){
        Intent intent = new Intent(this,EquipmentListActivity.class);
        startActivity(intent);
    }

    public void getData() {
        equipmentList.clear();
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM equipments WHERE campsId =?", new String[] {String.valueOf(artId)});
            int nameIx = cursor.getColumnIndex("equipmentname");
            while (cursor.moveToNext()) {
                String equipmentName = cursor.getString(nameIx);
                equipmentList.add(equipmentName);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(firstIn){
            equipmentAdapter.setList(equipmentList);
            equipmentAdapter.notifyDataSetChanged();
        }
    }

    public void mapClicked(View view){
       if(!binding.artNameText.getText().toString().equals("")){
           Intent intent = new Intent(this,MapsActivity.class);
           if(lati == null){
               intent.putExtra("info","nullupdate");
           }
           else{
               intent.putExtra("info",info);
           }
           startActivity(intent);
       }
       else{
           Toast.makeText(this, "Kamp adi bos gecilemez.", Toast.LENGTH_SHORT).show();
       }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.map_share, menu);

        // first parameter is the file for icon and second one is menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // We are using switch case because multiple icons can be kept
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.shareButton:

                String uri = "http://maps.google.com/maps?saddr=" +lati+","+longi;

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String ShareSub = "Here is my location";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ShareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

