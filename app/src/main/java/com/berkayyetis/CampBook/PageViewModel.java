package com.berkayyetis.CampBook;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {


    private MutableLiveData<Integer> myInput = new MutableLiveData<>();

    public void setNewValue(Integer val) {
        myInput.setValue(val);
    }

    public LiveData<Integer> getVal() {
        return myInput;
    }


}
