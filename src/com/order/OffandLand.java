package com.order;


import com.alibaba.fastjson.JSONObject;

/**
 * @description: 起飞和降落指令
 * @author: LiPin
 * @time: 2022-06-22 11:36
 */
public class OffandLand extends OrderFatherMes {
    private String flightId = "CA1203";
    private String flightNo="";
    private String runway = "05L";
    double lon=116.23;
    double lat=40.23;
    double alt=0;

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

    public OffandLand() {

    }


    public OffandLand(String type,
                      String flightId, String flightNo,
                      String time,
                      String runway,
                      double lon, double lat, double alt
    ) {
        this.flightId = flightId;
        this.flightNo = flightNo;
        this.type = type;
        this.runway = runway;
        this.time = time;
        this. lon=lon;
        this.lat=lat;
        this. alt=alt;
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
        msg.put("time",time);
        msg.put("starttime",starttime);
        msg.put("endtime",endtime);
        msg.put("lon",lon);
        msg.put("lat",lat);
        msg.put("alt",alt);
        return msg;
    }

}
