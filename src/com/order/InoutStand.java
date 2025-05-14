package com.order;


import com.alibaba.fastjson.JSONObject;

/**
 * @description:
 * @author: LiPin
 * @time: 2022-06-19 22:01
 */
public class InoutStand extends OrderFatherMes {
    private String flightId = "CA1234"; // 机型
    private String flightNo = "CA1234"; // 机型
    private String stand = "321";//机位
    private double lon = 116.23;
    private double lat = 40.23;
    private double alt = 0;
    public InoutStand() {
    }
    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public InoutStand(String type, String flightId, String flightNo, String stand, String time, double lon,
                      double lat,
                      double alt) {

        this.flightId = flightId;
        this.flightNo= flightNo;
        this.type = type;
        this.stand = stand;
        this.time = time;
        this.lon = lon;
        this.lat = lat;
        this.alt = alt;
        this.starttime=time;
        this.endtime=time;
        String[] sdk= starttime.split(" ")[1].split(":");
        this.key=flightId+"_"+sdk[0]+sdk[1]+sdk[2];

    }

    public String getflightId() {
        return flightId;
    }

    public void setflightId(String flightId) {
        this.flightId = flightId;
    }

    public String getStand() {
        return stand;
    }

    public void setStand(String stand) {
        this.stand = stand;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key", key);
        msg.put("type", type);
        msg.put("stand", stand);
        msg.put("flightId", flightId);
        msg.put("flightNo", flightNo);
        msg.put("time", time);
        msg.put("starttime", starttime);
        msg.put("endtime", endtime);
        msg.put("lon", lon);
        msg.put("lat", lat);
        msg.put("alt", alt);
        return msg;
    }
}
