package com.berkayyetis.CampBook;

import static com.berkayyetis.CampBook.DetailsActivity.artId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.berkayyetis.CampBook.databinding.ActivityDetailsBinding;
import com.berkayyetis.CampBook.databinding.ActivityEquipmentListBinding;

import java.util.ArrayList;

public class EquipmentListActivity extends AppCompatActivity {
    ActivityEquipmentListBinding binding;
    ArrayList<String> equipmentList;
    EquipmentAdapter equipmentAdapter;
    PageViewModel pageViewModel;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        setTitle("Ekipman Listesi");
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityEquipmentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        equipmentList = new ArrayList<String>();
        //binding.recyclerview2.setLayoutManager(new LinearLayoutManager(this));
        binding.equipmentRecycler.setLayoutManager(new LinearLayoutManager(this));
        equipmentAdapter = new EquipmentAdapter(equipmentList);
        binding.equipmentRecycler.setAdapter(equipmentAdapter);
        getData();

        binding.refleshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                binding.refleshLayout.setRefreshing(false);
            }
        });

        pageViewModel= new ViewModelProvider(this).get(PageViewModel.class);
        pageViewModel.getVal().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                final LoadingBar loadingBar = new LoadingBar(EquipmentListActivity.this);
                loadingBar.showDialog();
                handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        getData();
                        loadingBar.dissmissbar();
                        equipmentAdapter.notifyDataSetChanged();
                    }
                };
                handler.postDelayed(r, 1000);

            }
        });
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_equipment, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_equipment_item) {
            Toast.makeText(this, "add_art_item", Toast.LENGTH_SHORT).show();
            EquipmentFragment equipmentFragment = new EquipmentFragment();
            equipmentFragment.show(getSupportFragmentManager(),"EquipmentFragment");
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.reset_allequipment_item:
                SQLiteDatabase database = this.openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
                database.delete("equipments", "campsId=?", new String[]{artId.toString()});
                equipmentList.clear();
                equipmentAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Deleted All Equipments", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}