package com.order;


import com.alibaba.fastjson.JSONObject;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2023-12-03 14:56
 */

public class ConflictMes {
    public  String flightId="";
    public  String flightNo="";
    public   RoadLine location;
    public  Coordinate firstlocation;
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public Coordinate getFirstlocation() {
        return firstlocation;
    }

    public void setFirstlocation(Coordinate firstlocation) {
        this.firstlocation = firstlocation;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }
    public RoadLine getLocation() {
        return location;
    }

    public void setLocation(RoadLine location) {
        this.location = location;
    }
    public ConflictMes(){}
    public ConflictMes(String flightId, String flightNo, Coordinate firstlocation, RoadLine location) {
        this.flightId = flightId;
        this.flightNo = flightNo;
        this.location = location;
        this.firstlocation=firstlocation;
    }

    @Override
    public String toString() {
        JSONObject Js=new JSONObject();
        Js.put("flightId",flightId);
        Js.put("flightNo",flightNo);
        Js.put("firstlocation",new double[]{firstlocation.getX(),firstlocation.getY()});
        Js.put("location",location.toString());
        return Js.toJSONString();
    }
    public JSONObject getMes()
    {
        JSONObject Js=new JSONObject();
        Js.put("flightId",flightId);
        Js.put("flightNo",flightNo);
        Js.put("firstlocation",new double[]{firstlocation.getX(),firstlocation.getY()});
        Js.put("location",location.getMes());
        return Js;
    }
}
