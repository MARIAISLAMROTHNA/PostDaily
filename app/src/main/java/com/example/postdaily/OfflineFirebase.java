package com.example.postdaily;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class OfflineFirebase extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
