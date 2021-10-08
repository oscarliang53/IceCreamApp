package edu.shu.icecream;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import edu.shu.icecream.data.IceUserData;

public class MainApplication extends Application {
    private static MainApplication sIns;

    public static MainApplication ins() {
        return sIns;
    }

    public Gson mGson = new Gson();

    public FirebaseAuth mAuth;
    public FirebaseUser currentUser;
    public FirebaseFirestore fireDB;

    public IceUserData iceUserData;
    @Override
    public void onCreate() {
        super.onCreate();
        sIns = this;
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fireDB = FirebaseFirestore.getInstance();
    }
}
