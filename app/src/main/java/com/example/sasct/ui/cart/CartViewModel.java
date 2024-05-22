package com.example.sasct.ui.cart;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CartViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CartViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Cart fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
