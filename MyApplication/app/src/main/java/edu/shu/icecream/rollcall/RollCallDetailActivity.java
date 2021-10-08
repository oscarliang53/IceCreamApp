package edu.shu.icecream.rollcall;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.shu.icecream.MainApplication;
import edu.shu.icecream.R;
import edu.shu.icecream.data.LocationData;
import edu.shu.icecream.data.RollCallData;
import edu.shu.icecream.databinding.ActivityRollCallDetailBinding;
import edu.shu.icecream.route.RouteDetailActivity;

public class RollCallDetailActivity extends AppCompatActivity {
    ActivityRollCallDetailBinding binding;
    List<RollCallData> rollCallDataList = new ArrayList<>();
    RollCallAdapter rollCallAdapter;
    String targetID;
    double lat_school = 24.9886642;
    double lng_school = 121.5439837;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_roll_call_detail);
        try {
            targetID = getIntent().getStringExtra("targetID");
        } catch (Exception ignore) {
        }
        Calendar calendar = Calendar.getInstance();
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            RollCallData rollCallData = new RollCallData();
            rollCallData.setDate(String.format("%02d/%02d", calendar.get(Calendar.MONTH) + 1, i));
            rollCallDataList.add(rollCallData);
        }


        rollCallAdapter = new RollCallAdapter(null, null, this);
        binding.rvRollCall.setAdapter(rollCallAdapter);
        rollCallAdapter.updateDatas(rollCallDataList);

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
                            LocationData locationData = MainApplication.ins().mGson.fromJson(MainApplication.ins().mGson.toJson(document.getData()),
                                    LocationData.class);
                            float[] results = new float[1];

                            Location.distanceBetween(
                                    locationData.getLat(), locationData.getLng(),
                                    lat_school, lng_school, results
                            );
                            Log.i("TTT", "date : " + locationData.getDate());
                            Log.i("TTT", "distance : " + results[0]);
                            if (results[0] <= 1000) {
                                //該紀錄在學校範圍
                                for (RollCallData data : rollCallDataList) {
                                    if (data.getDate().equals(locationData.getDate())) {
                                        data.setCheck(true);
                                    }
                                }
                            }
                        }
                        rollCallAdapter.updateDatas(rollCallDataList);
                    } else {
                        Log.d("TAG", "No such document");
                        Toast.makeText(RollCallDetailActivity.this, "該好友無定位紀錄", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

}
