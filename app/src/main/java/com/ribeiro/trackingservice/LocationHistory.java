package com.ribeiro.trackingservice;

import android.content.Context;
import android.location.Location;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;


public class LocationHistory {
    private static final String TAG = "RIBEIROTRACKING_LocationHistory";
    private String Id;
    private String DeviceId;
    private String DateTime;
    private Double Latitude;
    private Double Longitude;
    private String Location;
    private String Message;

    public LocationHistory() {

    }

    public LocationHistory(Location location, String message, Context context) {
        this.setId(UUID.randomUUID().toString());
        DeviceUuidFactory factory = null;
        factory = new DeviceUuidFactory(context);
        this.setDeviceId(factory.getDeviceUuid().toString());
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String datetime = dateformat.format(c.getTime());
        this.setDateTime(datetime);
        this.setLatitude(location.getLatitude());
        this.setLongitude( location.getLongitude());
        this.setMessage(message);
        this.setLocation();
    }

    public String getJson() {
        return "{ \"LocationHistoryId\": \""+getId()+"\","    +
                "\"LocationHistoryDeviceId\": \""+ getDeviceId() + "\"," +
                "\"LocationHistoryDateTime\": \""+ getDateTime() + "\","  +
                "\"LocationHistoryLocation\": \""+ getLocation() + "\","  +
                "\"LocationHistoryMessage\": \""+ getMessage()   + "\" }";
    }

    //setters
    public void setId(String id) { Id = id; }
    public void setDeviceId(String deviceId) { DeviceId = deviceId; }
    public void setDateTime(String dateTime) { DateTime = dateTime; }
    public void setLatitude(Double latitude) {Latitude = latitude; }
    public void setLongitude(Double longitude) {Longitude = longitude; }
    public void setLocation() {
        Location = this.getLatitude().toString()+","+ this.getLongitude().toString();;
    }
    public void setMessage(String message) { Message = message; }
    //getters
    public Double getLatitude() { return Latitude; }
    public Double getLongitude(){ return Longitude; }
    public String getId(){ return Id;}
    public String getDeviceId(){ return DeviceId;}
    public String getDateTime(){ return DateTime;}
    public String getLocation(){ return Location;}
    public String getMessage() { return Message; }


}
