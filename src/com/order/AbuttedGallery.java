package com.order;
/**
 * @description:
 * @author: LiPin
 * @time: 2022-06-19 22:01
 */

import base.Time;
import com.alibaba.fastjson.JSONObject;


/**
 * @description:
 * @author: LiPin
 * @time: 2022-06-19 22:01
 */
public class AbuttedGallery extends OrderFatherMes {

    String flightId = "";
    String flightNo = "CA1203";
    String stand = "321";//机位
    String acft = "B747"; // 机型
    private double lon = 116.23;
    private double lat = 40.23;
    private String duretime = "";//冲突发生时间
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

    public String getEndtime() {
        return endtime;
    }


    public AbuttedGallery() {
    }

    public AbuttedGallery(String type, String flightId, String flightNo, String stand, String time, String acft) {

        this.flightId = flightId;
        this.flightNo = flightNo;
        this.type = type;
        this.acft = acft;
        this.stand = stand;
        this.time = time;
        this.starttime=time;
        this.endtime=time;
        String[] sdk= starttime.split(" ")[1].split(":");
        this.key=flightId+"_"+sdk[0]+sdk[1]+sdk[2];
        this.duretime="00:00";
    }

public void setEndtime(String endtime){
    this.time=endtime;
    this.endtime = endtime;
    this.duretime = Time.getTimeDuration(this.endtime, starttime);

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

    public String getAcft() {
        return acft;
    }

    public void setAcft(String acft) {
        this.acft = acft;
    }

    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key", key);
        msg.put("type", type);
        msg.put("stand", stand);
        msg.put("flightId", flightId);
        msg.put("flightNo", flightNo);
        msg.put("time", time);
        msg.put("duretime", duretime);
        msg.put("starttime", starttime);
        msg.put("endtime", endtime);
        msg.put("acft", acft);
        return msg;
    }
}
