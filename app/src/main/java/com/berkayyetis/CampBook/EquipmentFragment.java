package com.berkayyetis.CampBook;

import static android.content.Context.MODE_PRIVATE;

import static com.berkayyetis.CampBook.DetailsActivity.artId;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.berkayyetis.CampBook.databinding.FragmentEquipmentBinding;
import com.google.android.material.textfield.TextInputEditText;

public class EquipmentFragment extends DialogFragment {

    public static EquipmentFragment newInstance() {
        return new EquipmentFragment();
    }

    SQLiteDatabase database;
    TextInputEditText equipInput;
    PageViewModel pageViewModel;
    Integer count=0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
        equipInput = view.findViewById(R.id.equipmentEditText);
        Button addButton = view.findViewById(R.id.addEquipmentButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                pageViewModel.setNewValue(count);
                saveEquipment();
            }
        });
    }
    public void saveEquipment() {
        String equipmentName = equipInput.getText().toString();
        try {
            database = getActivity().openOrCreateDatabase("CampDatabase",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS equipments (id INTEGER PRIMARY KEY,equipmentname VARCHAR,campsId INTEGER,FOREIGN KEY(campsId) REFERENCES camps(id))");
            String sqlString = "INSERT INTO equipments (equipmentname,campsId) VALUES (?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,equipmentName);
            sqLiteStatement.bindString(2, String.valueOf(artId));
            sqLiteStatement.execute();
            Toast.makeText(getActivity(),  equipmentName+" ekipman listesine eklendi.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
    }

}