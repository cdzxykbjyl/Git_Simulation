package com.order;


import com.alibaba.fastjson.JSONObject;

/**
 * @description: 收起和降下起落架指令
 * @author: LiPin
 * @time: 2022-06-22 11:52
 */
public class LandingGear extends OrderFatherMes {
    private String flightId = "CA1203";
    private String flightNo= "CA1203";
    private String time = "2022-03-23 13:23:43";
    private String runway = "05L";
    double lon=116.23;
    double lat=40.23;
    double alt=0;

    public LandingGear() {

    }

    public double getAlt() {
        return alt;
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

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public LandingGear(String type,
                       String flightId,
                       String flightNo,
                       String time,
                       String runway,
                       double lon, double lat, double alt) {
        this.flightId= flightId;
        this.flightNo= flightNo;
        this.type = type;
        this.runway = runway;
        this.time = time;
        this.starttime = time;
        this. lon=lon;
        this.lat=lat;
        this. alt=alt;
        String[] sdk= starttime.split(" ")[1].split(":");
        this.key=flightId+"_"+sdk[0]+sdk[1]+sdk[2];
    }

    public String getflightId() {
        return flightId;
    }

    public void setflightId(String flightId) {
        this.flightId = flightId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getRunway() {
        return runway;
    }

    public void setRunway(String runway) {
        this.runway = runway;
    }


    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key",key);
        msg.put("type",type);
        msg.put("runway",runway);
        msg.put("flightId",flightId);
        msg.put("flightNo", flightNo);
        msg.put("time", time);
        msg.put("lon",lon);
        msg.put("lat",lat);
        msg.put("alt",alt);
        msg.put("starttime", starttime);
        msg.put("endtime", endtime);
        return msg;
    }

}
