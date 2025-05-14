package com.order;


import com.alibaba.fastjson.JSONObject;


/**
 * @description:
 * @author: LiPin
 * @time: 2022-08-05 20:46
 */
public class Offline extends OrderFatherMes {

    private String flightId = "CA1234"; // 机型
    private String flightNo = "CA1234"; // 机型
    private String inoutflag="A";
    private String stand = "321";//机位
    private String runway = "05L";//机位
    private double  lon=116.23;
    private  double lat=40.23;
    private  double alt=0;


    public int getNums() {
        return nums;
    }

    public void setNums(int nums) {
        this.nums = nums;
    }

    private  int nums;

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

    public String getInoutflag() {
        return inoutflag;
    }

    public void setInoutflag(String inoutflag) {
        this.inoutflag = inoutflag;
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

    public Offline(String type,String flightId,String inoutflag,String flightNo, String stand, String runway, String time, double lon, double lat, double alt) {
        this.type = type;
        this.flightId = flightId; // 机型
        this.inoutflag=inoutflag;
        this.flightNo = flightNo; // 机型
        this.stand = stand;//机位
        this.runway = runway;//机位
        this.time = time;//靠桥开始时间
        this.starttime=time;
        this.endtime=time;
        this.lon=lon;
        this.lat=lat;
        this.alt=alt;
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

    public String getRunway() {
        return runway;
    }

    public void setRunway(String runway) {
        this.runway = runway;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String Time) {
        this.time = time;
    }
    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key",key);
        msg.put("type",type);
        msg.put("stand",stand);
        msg.put("runway",runway);
        msg.put("flightId",flightId);
        msg.put("inoutflag",inoutflag);
        msg.put("flightNo",flightNo);
        msg.put("time",time);
        msg.put("starttime",starttime);
        msg.put("endtime",endtime);
        msg.put("lon",lon);
        msg.put("lat",lat);
        msg.put("alt",alt);
        return msg;
    }
}
