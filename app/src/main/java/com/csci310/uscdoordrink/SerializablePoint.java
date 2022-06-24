package com.csci310.uscdoordrink;

public class SerializablePoint implements java.io.Serializable{

    public double lat;
    public double lon;

    SerializablePoint(){
        lat = 0;
        lon = 0;
    };

    SerializablePoint(double latitude, double longitude){
        lat = latitude;
        lon = longitude;
    }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
}
