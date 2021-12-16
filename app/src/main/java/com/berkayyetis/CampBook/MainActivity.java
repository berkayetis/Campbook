package com.berkayyetis.CampBook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.berkayyetis.CampBook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setTitle("Kamp Defterim");
        artList = new ArrayList<Art>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter = new ArtAdapter(artList);
        binding.recyclerView.setAdapter(artAdapter);
        getData();



        if (artList.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        }
        else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    public void getData() {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM camps", null);
            int nameIx = cursor.getColumnIndex("campname");
            int idIx = cursor.getColumnIndex("id");
            int painterIx = cursor.getColumnIndex("date");
            int yearIx = cursor.getColumnIndex("day");
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                String painter = cursor.getString(painterIx);
                String year = cursor.getString(yearIx);
                Art art = new Art(name,id,year,painter);
                artList.add(art);
            }
            artAdapter.notifyDataSetChanged();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goDetail(View vie){
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("info","new");
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_art_item) {
            SQLiteDatabase database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
            database.execSQL("DELETE FROM camps");
            artList.clear();
            artAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Deleted All", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
