package edu.shu.icecream.route;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import edu.shu.icecream.MainApplication;
import edu.shu.icecream.R;
import edu.shu.icecream.data.LocationData;
import edu.shu.icecream.databinding.ActivityRouteDetailBinding;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    List<LocationData> locationDataList = new ArrayList<>();
    String targetID = "";
    ActivityRouteDetailBinding binding;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    Polyline polyline;
    FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_route_detail);
        try {
            targetID = getIntent().getStringExtra("targetID");
        } catch (Exception ignore) {
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);

        if (mMap == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);//藍點點
            mMap.getUiSettings().setMyLocationButtonEnabled(true);//定位紐

            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude()), 17))).addOnFailureListener(e -> {
            });
        }
    }

    @Override
    public void onMapLoaded() {
        getLocations();
    }

    public void getLocations() {
        MainApplication.ins().fireDB.collection("UserLocation")
                .document(targetID)
                .collection("locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot documentList = task.getResult();
                    if (documentList != null && documentList.size() > 0) {
                        Log.d("TAG", "QuerySnapshot data count: " + documentList.size());
                        for (DocumentSnapshot document : documentList.getDocuments()) {
                            locationDataList.add(MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()),
                                    LocationData.class));
                        }
                        if (locationDataList.size() > 0) {
                            updatePolyLine();
                        }
                    } else {
                        Log.d("TAG", "No such document");
                        Toast.makeText(RouteDetailActivity.this, "該好友無定位紀錄", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    private void updatePolyLine() {
        Collections.sort(locationDataList, (o1, o2) -> o1.getTimeStamp().compareTo(o2.getTimeStamp()));
        PolylineOptions polylineOptions = new PolylineOptions();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        //前一天凌晨
        Calendar yesterdayCalendar = Calendar.getInstance();
        yesterdayCalendar.set(yesterdayCalendar.get(Calendar.YEAR),
                yesterdayCalendar.get(Calendar.MONTH),
                yesterdayCalendar.get(Calendar.DATE) - 1, 0, 0, 0);

        for (LocationData locationData : locationDataList) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(locationData.getTimeStamp());
            if (calendar.after(yesterdayCalendar)) {
                LatLng latLng = new LatLng(locationData.getLat(), locationData.getLng());
                polylineOptions.add(latLng);
                boundsBuilder.include(latLng);
            }
        }

        if (polyline != null) {
            polyline.remove();
        }
        polyline = mMap.addPolyline(polylineOptions);
        polyline.setWidth(12f);
        polyline.setColor(ContextCompat.getColor(this, R.color.orange));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 20));

    }
}
