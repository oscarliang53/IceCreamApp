package edu.shu.icecream;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.shu.icecream.data.IceUserData;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        loginButton = new LoginButton(this);
        loginButton.setReadPermissions("email", "public_profile");

        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("fbCallbackManager", "FB onSuccess " + loginResult.getAccessToken());
//                checkFBToken();
                binding.frameLayout.removeAllViews();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i("fbCallbackManager", "FB Cancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("fbCallbackManager", "onError " + error.toString());
                updateUI(null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(MainApplication.ins().currentUser);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        MainApplication.ins().mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.i("fire", "fire 登入");
                        MainApplication.ins().currentUser = MainApplication.ins().mAuth.getCurrentUser();
                        updateUI(MainApplication.ins().currentUser);
                    } else {
                        Log.i("fire", "fire No登入");
                        Toast.makeText(LoginActivity.this, "認證失敗", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.i("updateUI", "update");
//            binding.btSignOut.setVisibility(View.VISIBLE);
//            String result = user.getDisplayName() + ";\n" + user.getUid();
//            String result = user.getDisplayName();
//            binding.tvLoginResult.setText(result);
            binding.tvLoginResult.setText("登入成功");
            Toast.makeText(this, "進入首頁...", Toast.LENGTH_SHORT).show();
            getUserInfo();
        } else {
            binding.frameLayout.removeAllViews();
            binding.frameLayout.addView(loginButton);
//            binding.btSignOut.setVisibility(View.GONE);
        }
    }

    private void getUserInfo() {//檢查是否有此帳號
        MainApplication.ins().fireDB.collection("UserInfo").document(MainApplication.ins().currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            MainApplication.ins().iceUserData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()), IceUserData.class);
                            gotoMain();
                        } else {
                            Log.d("TAG", "No such document");
                            addUserInfo();//新增帳號
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                });
    }

    private void addUserInfo() {
//        建UserInfo
        IceUserData tempIceUser = new IceUserData(MainApplication.ins().currentUser.getDisplayName(), MainApplication.ins().currentUser.getUid());
        MainApplication.ins().fireDB.collection("UserInfo")
                .document(MainApplication.ins().currentUser.getUid())
                .set(tempIceUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addUserFriend();
                getUserInfo();
            }
        });
    }

    private void addUserFriend() {
        //建完UserInfo建UserFriend
        IceUserFriendData initialIceUserFriendData = new IceUserFriendData();
        initialIceUserFriendData.setUserID(MainApplication.ins().currentUser.getUid());
        List<IceUserFriendData.FriendBean> friendBeanList = new ArrayList<>();
        initialIceUserFriendData.setFriendBeans(friendBeanList);
        MainApplication.ins().fireDB.collection("UserFriend")
                .document(MainApplication.ins().currentUser.getUid()).set(initialIceUserFriendData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
