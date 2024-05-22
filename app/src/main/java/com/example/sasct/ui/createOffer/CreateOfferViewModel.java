package com.example.sasct.ui.createOffer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateOfferViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CreateOfferViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is create offer fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
