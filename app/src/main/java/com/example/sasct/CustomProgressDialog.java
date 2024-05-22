package com.example.sasct;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog {
    ProgressDialog progressDialog;
    String title,msg;
    Context mContext;

    public CustomProgressDialog(String title, String msg, Context mContext) {
        this.title = title;
        this.msg = msg;
        this.mContext=mContext;
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMax(100);
        progressDialog.setMessage(msg);
        progressDialog.setTitle(title);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }
    public void showDialog(){
        progressDialog.show();
    }
    public void dismisDialog(){
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

}
