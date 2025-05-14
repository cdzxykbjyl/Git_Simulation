package com.order;


import base.Time;
import com.alibaba.fastjson.JSONObject;


/**
 * @description:
 * @author: LiPin
 * @time: 2022-06-22 12:17
 */
public class TaxiWait extends OrderFatherMes {
    private String Name = "PB1";
    private String flightId = "CA1203";
    private  String flightNo="";
    private double lon = 116.232;
    private double lat = 43;
    private double alt = 232;
    private  String duretime;
    public TaxiWait() {
    }

    public TaxiWait(String type, String Name, String time,  String flightId, String flightNo, double lon,
                    double lat,
                    double alt) {
        this.type=type;
        this.Name = Name;
        this.time=time;
        this.flightId = flightId;
        this.flightNo = flightNo;
        this.lon = lon;
        this.lat = lat;
        this.alt = alt;
        this.starttime=time;
        this.endtime=time;
        String[] sdk= starttime.split(" ")[1].split(":");
        this.key=flightId+"_"+sdk[0]+sdk[1]+sdk[2];
        this.duretime="00:00";
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

   public void setEndtime(String endtime){
        this.endtime=endtime;
        this.time=endtime;
       this.duretime = Time.getTimeDuration(this.endtime, starttime);
   }

    public String getflightId() {
        return flightId;
    }

    public void setflightId(String flightId) {
        this.flightId = flightId;
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
    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key",key);
        msg.put("type",type);
        msg.put("Name",Name);
        msg.put("time",time);
        msg.put("starttime",starttime);
        msg.put("endtime",endtime);
        msg.put("flightId",flightId);
        msg.put("flightNo",flightNo);
        msg.put("lon",lon);
        msg.put("lat",lat);
        msg.put("alt",alt);
        return msg;
    }
}
