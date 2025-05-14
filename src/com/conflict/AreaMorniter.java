package com.conflict;

import base.Time;
import com.agent.Flight;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-08 15:10
 */

public class AreaMorniter {
    private double x = Double.NaN;
    private double y = Double.NaN;
    private double z = Double.NaN;
    private String flightId = "";
    private String flightNo = "";
    private String runway = "05L";
    private String inOutFlag = "A";
    private String acft = "321";
    private String areaname = "";
    String starttime = "";
    String endtime = "";
    String duretime = "00:00";
    String orderroadmes = "";
    boolean isinnow = false;



    public String getOrderroadmes() {
        return orderroadmes;
    }

    public void setOrderroadmes(String orderroadmes) {
        this.orderroadmes = orderroadmes;
    }

    public boolean isIsinnow() {
        return isinnow;
    }

    public void setIsinnow(boolean isinnow) {
        this.isinnow = isinnow;
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

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
        this.duretime =endtime.equals(starttime)?"00:00": Time. getTimeDuration( endtime,  starttime) ;
    }

    public String getDuretime() {
        return duretime;
    }

    public void setDuretime(String duretime) {
        this.duretime = duretime;
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

    public AreaMorniter copy() {
        AreaMorniter runwayFlightHis = new AreaMorniter();
        runwayFlightHis.x = this.getX();
        runwayFlightHis.y = this.getY();
        runwayFlightHis.z = this.getZ();
        runwayFlightHis.starttime = this.getStarttime();
        runwayFlightHis.runway = this.getRunway();
        runwayFlightHis.inOutFlag = this.getInOutFlag();
        runwayFlightHis.acft = this.getAcft();
        runwayFlightHis.endtime = this.getEndtime();
        runwayFlightHis.flightNo = this.getFlightNo();
        runwayFlightHis.flightId = this.getFlightId();
        runwayFlightHis.duretime = this.getDuretime();
        runwayFlightHis.areaname = this.getAreaname();
        runwayFlightHis.orderroadmes = this.orderroadmes;
        runwayFlightHis.isinnow =this. isinnow;

        return runwayFlightHis;
    }

    AreaMorniter() {

    }

    public AreaMorniter(Flight flight, String areaname) {
        this.x = flight.getX();
        this.y = flight.getY();
        this.z = flight.getZ();
        this.starttime = flight.getTime();
        this.endtime = flight.getTime();
        this.duretime = "00:00";
        this.x = flight.getX();
        this.y = flight.getY();
        this.z = flight.getZ();
        this.runway = flight.getRunway();
        this.inOutFlag = flight.getInOutFlag();
        this.acft = flight.getAcft();
        this.areaname = areaname;
        this.flightId = flight.getFlightId();
        this.flightNo = flight.getFlightNo();
        this.isinnow=true;

    }

}
