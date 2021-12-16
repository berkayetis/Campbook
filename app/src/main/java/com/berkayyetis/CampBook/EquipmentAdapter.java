package com.berkayyetis.CampBook;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.berkayyetis.CampBook.databinding.EquipmentlistRowBinding;

import java.util.ArrayList;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentHolder> {
    ArrayList<String> equipmentList;

    public EquipmentAdapter(ArrayList<String> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public void setList(ArrayList<String> equipmentList) {
        this.equipmentList = equipmentList;
    }
    @NonNull
    @Override
    public EquipmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EquipmentlistRowBinding binding = EquipmentlistRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new EquipmentHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentHolder holder, int position) {
        holder.binding.checkBox.setText(equipmentList.get(position));
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }


    public class EquipmentHolder extends RecyclerView.ViewHolder {
        private EquipmentlistRowBinding binding;
        public EquipmentHolder(EquipmentlistRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
