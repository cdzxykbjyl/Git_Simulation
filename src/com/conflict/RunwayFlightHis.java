package com.conflict;

import com.agent.Flight;
import com.alibaba.fastjson.JSONObject;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-04-26 6:30
 */

public class RunwayFlightHis {
    private double x = Double.NaN;
    private double y = Double.NaN;
    private double z = Double.NaN;
    private String airline = "";
    private String flightId = "";
    private String flightNo = "";
    private String runway = "05L";
    private String inOutFlag = "A";
    private boolean inRunway=true;//是否脱离跑道
    private String acft = "321";
    private String time = "2022-05-26 12:29:03";
    public RunwayFlightHis copy(){
        RunwayFlightHis runwayFlightHis=new RunwayFlightHis();
        runwayFlightHis.x=this.getX();
        runwayFlightHis.y=this.getY();
        runwayFlightHis.z=this.getZ();
        runwayFlightHis.time=this.getTime();
        runwayFlightHis.runway=this.getRunway();
        runwayFlightHis.inOutFlag=this.getInOutFlag();
        runwayFlightHis.acft=this.getAcft();
        runwayFlightHis.airline=this.getAirline();
        runwayFlightHis.flightId=this.getFlightId();
        runwayFlightHis.flightNo=this.getFlightNo();
        runwayFlightHis.inRunway=this.inRunway;
        return runwayFlightHis;
    }
    public JSONObject getMes() {
        JSONObject msg = new JSONObject();
        msg.put("x", x);
        msg.put("y", y);
        msg.put("z", z);
        msg.put("time", time);
        msg.put("runway", runway);
        msg.put("inOutFlag", inOutFlag);
        msg.put("acft", acft);
        msg.put("airline", airline);
        msg.put("flightId", flightId);
        msg.put("flightNo", flightNo);
        msg.put("inRunway", inRunway);
        return msg;
    }
    public RunwayFlightHis(){}


    public boolean isInRunway() {
        return inRunway;
    }

    public void setIsinRunway(boolean inRunway) {
        this.inRunway = inRunway;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
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

    public String getRunway() {
        return runway;
    }

    public void setRunway(String runway) {
        this.runway = runway;
    }

    public String getInOutFlag() {
        return inOutFlag;
    }

    public void setInOutFlag(String inOutFlag) {
        this.inOutFlag = inOutFlag;
    }

    public String getAcft() {
        return acft;
    }

    public void setAcft(String acft) {
        this.acft = acft;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
