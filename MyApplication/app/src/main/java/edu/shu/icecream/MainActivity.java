package edu.shu.icecream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.shu.icecream.data.IceUserData;
import edu.shu.icecream.data.IceUserFriendData;
import edu.shu.icecream.data.LocationData;
import edu.shu.icecream.databinding.ActivityMainBinding;
import edu.shu.icecream.message.MessageActivity;
import edu.shu.icecream.navigation.NavigationActivity;
import edu.shu.icecream.profile.ProfileActivity;
import edu.shu.icecream.rollcall.RollCallActivity;
import edu.shu.icecream.route.RouteActivity;
import edu.shu.icecream.sos.SosActivity;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    ActivityMainBinding binding;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap = null;
    FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3478;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation = new LatLng(24.08326, 121.1703);//合歡山

    private LocationUpdatesService mService = null;
    private boolean mBound = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mService.requestLocationUpdates();//啟動Service
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    IceUserFriendData iceUserFriendData;
    List<IceUserFriendData.FriendBean> friendList = new ArrayList<>();
    Map<String, Bitmap> friendPhotoMap = new HashMap<>();
    List<LocationData> locationDataList = new ArrayList<>();
    List<Marker> friendMarkerList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        setListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocationUpdatesService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        getFriend();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainApplication.ins().currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setListener() {
        binding.btRoute.setOnClickListener(v ->
                startActivity(new Intent(this, RouteActivity.class)));
        binding.btSos.setOnClickListener(v ->
                startActivity(new Intent(this, SosActivity.class)));
        binding.btCompass.setOnClickListener(v ->
                startActivity(new Intent(this, NavigationActivity.class)));
        binding.btMessage.setOnClickListener(v ->
                startActivity(new Intent(this, MessageActivity.class)));
        binding.btRollCall.setOnClickListener(v ->
                startActivity(new Intent(this, RollCallActivity.class)));
        binding.btProfile.setOnClickListener(view ->
                startActivityForResult(new Intent(this, ProfileActivity.class), 666));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                    getDeviceLocation();
                }
            }
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
//            Log.i("TTT", "mMap == null");
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);//藍點點
                mMap.getUiSettings().setMyLocationButtonEnabled(true);//定位紐
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
//            Log.i("TTT Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    mLastKnownLocation = location;
                    if (mLastKnownLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 12));
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 6));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        if (!((LocationManager) this.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(this, "請打開系統定位功能", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 6));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                });
            }
        } catch (SecurityException e) {
//            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 666 && resultCode == RESULT_OK) {
//            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.removeLocationUpdates();
            mService.stopSelf();
        }
    }

    private void getFriend() {
        if (MainApplication.ins().currentUser == null) return;
        MainApplication.ins().fireDB.collection("UserFriend").document(MainApplication.ins().currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                iceUserFriendData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()), IceUserFriendData.class);
                                Log.d("TAG", "friend size : " + iceUserFriendData.getFriendBeans().size());
                                if (iceUserFriendData.getFriendBeans().size() > 0) {
                                    friendList = iceUserFriendData.getFriendBeans();
                                    getFriendData();
                                }
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void getFriendData() {
        friendPhotoMap.clear();
        for (IceUserFriendData.FriendBean friendBean : friendList) {
            //抓照片
            MainApplication.ins().fireDB.collection("UserInfo")
                    .document(friendBean.getFriendID()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                IceUserData friendData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()), IceUserData.class);
                                downloadFriendPhoto(friendBean.getFriendID(), friendData.getPhotoUrl());
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    });
            //定位監聽
            MainApplication.ins().fireDB.collection("UserLocation")
                    .document(friendBean.getFriendID())
                    .collection("locations")
                    .orderBy("timeStamp")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) Log.i("TTT", "snapshot error " + error.getMessage());
                            if (value != null) {
                                if (value.size() > 0) {
                                    updateFriendLocation(value);
                                }
                            }
                        }
                    });
        }
    }

    private void downloadFriendPhoto(String friendID, String photoUrl) {
        Glide.with(this).asBitmap().load(photoUrl)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        int height = 100;
                        int width = 100;
                        Bitmap newMarker = Bitmap.createScaledBitmap(resource, width, height, false);
                        friendPhotoMap.put(friendID, newMarker);
                        Log.i("TTT", "friendPhotoMap = " + friendPhotoMap.size());
                        drawMapMarker();
                    }
                });
    }

    private void updateFriendLocation(QuerySnapshot value) {
        if (value.getDocuments().size() > 0) {
            DocumentSnapshot snapshot = value.getDocuments().get(value.getDocuments().size() - 1);
            LocationData new_locationData = new LocationData(
                    (String) snapshot.get("id"),
                    (Double) snapshot.get("lat"),
                    (Double) snapshot.get("lng"),
                    (Long) snapshot.get("timeStamp"));

            Log.i("TTT", "last time = " + new_locationData.getTimeStamp());
            if (locationDataList.size() > 0) {
                for (int i = 0; i < locationDataList.size(); i++) {
                    Log.i("TTT", "current id : " + locationDataList.get(i).getId());
                    Log.i("TTT", "target id : " + new_locationData.getId());
                    if (locationDataList.get(i).getId().equals(new_locationData.getId())) {
                        locationDataList.remove(i--);
                    }
                }
            }
            locationDataList.add(new_locationData);
            Log.i("TTT", "locationDataList = " + locationDataList.size());
            drawMapMarker();
        }
    }

    private void drawMapMarker() {
        if (mMap != null) {
            mMap.clear();
            friendMarkerList.clear();
            for (LocationData locationData : locationDataList) {
                Bitmap bitmap = friendPhotoMap.get(locationData.getId());
                if (bitmap == null) {
                    break;
                }
                friendMarkerList.add(mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(locationData.getLat(), locationData.getLng()))
                        .icon(BitmapDescriptorFactory.fromBitmap(friendPhotoMap.get(locationData.getId())))));
            }
        }
    }
}
