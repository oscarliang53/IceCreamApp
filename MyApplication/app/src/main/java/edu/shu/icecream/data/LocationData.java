package edu.shu.icecream.data;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocationData {
    final static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");

    private String id;
    private Double lat;
    private Double lng;
    private Long timeStamp;

    public String getDate() {
        if (timeStamp > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timeStamp);
            return sdf.format(c.getTime());
        } else {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public LocationData(String id, Double lat, Double lng, Long timeStamp) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.timeStamp = timeStamp;
    }
}
