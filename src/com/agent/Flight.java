package com.agent;


import com.alibaba.fastjson.JSONObject;
import com.order.OrderFatherMes;
import fun.NodeFather;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;


/**
 * @description:
 * @author: LiPin
 * @time: 2022-01-27 22:45
 */
public class Flight {
    private double x = Double.NaN;
    private double y = Double.NaN;
    private double z = Double.NaN;
    private double elevation;//俯仰角
    private double direction = 343.5097418104348;
    private double speed = 80;
    private String airline = "";
    private String speedunit = "m/s";
    private int wait = 0;
    private int lineindex = 0;
    private int segindex = 0;
    private double segyu = 0;
    private String flightId = "";
    private String flightNo = "";
    private String runway = "05L";
    private String stand = "201";
    private String inOutFlag = "A";
    private String acft = "321";
    private String online = "N";//是否在几位上
    private String time = "2022-05-26 12:29:03";
    private String orderRoadNames = "";//路由指令
    private String Onstand = "Y";//是否在几位上
    private String networkname;//当前所处的系统
    private double passlength=0;

    //"simulation"显示在前端的左边，"real"显示在前端的右边
    private String ifHistory = "simulation";

    public String getIfHistory() {
        return ifHistory;
    }

    public void setIfHistory(String ifHistory) {
        this.ifHistory = ifHistory;
    }

    private List<RoadLine>roadlines=new ArrayList<>();
    private List<NodeFather> bestnodes = new ArrayList<>();
    private List<OrderFatherMes> messages = new ArrayList<>();//消息指令信息

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public double getPasslength() {
        return passlength;
    }

    public void setPasslength(double passlength) {
        this.passlength = passlength;
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

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getSpeedunit() {
        return speedunit;
    }

    public void setSpeedunit(String speedunit) {
        this.speedunit = speedunit;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public int getLineindex() {
        return lineindex;
    }

    public void setLineindex(int lineindex) {
        this.lineindex = lineindex;
    }

    public int getSegindex() {
        return segindex;
    }

    public void setSegindex(int segindex) {
        this.segindex = segindex;
    }

    public double getSegyu() {
        return segyu;
    }

    public void setSegyu(double segyu) {
        this.segyu = segyu;
    }

    public List<NodeFather> getBestnodes() {
        return bestnodes;
    }

    public void setBestnodes(List<NodeFather> bestnodes) {
        this.bestnodes = bestnodes;
    }

    public String getOnstand() {
        return Onstand;
    }

    public String getAcft() {
        return acft;
    }

    public String getNetworkname() {
        return networkname;
    }

    public void setNetworkname(String networkname) {
        this.networkname = networkname;
    }

    public void setAcft(String acft) {
        this.acft = acft;
    }

    public void setOnstand(String onstand) {
        Onstand = onstand;
    }

    public String getOrderRoadNames() {
        return orderRoadNames;
    }

    public void setOrderRoadNames(String orderRoadNames) {
        this.orderRoadNames = orderRoadNames;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public String getTime() {
        return time;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public List<OrderFatherMes> getMessages() {
        return messages;
    }

    public void setMessages(List<OrderFatherMes> messages) {
        this.messages = messages;
    }

    public String getRunway() {
        return runway;
    }

    public void setRunway(String runway) {
        this.runway = runway;
    }

    public String getStand() {
        return stand;
    }

    public List<RoadLine> getRoadlines() {
        return roadlines;
    }

    public void setRoadlines(List<RoadLine> roadlines) {
        this.roadlines = roadlines;
    }

    public void setStand(String stand) {
        this.stand = stand;
    }

    private double[][] getcoordstr(Coordinate[] coordinates) {
        double[][] coors = new double[coordinates.length][2];
        for (int i = 0; i < coordinates.length; i++) {
            coors[i][0] = coordinates[i].getX();
            coors[i][1] = coordinates[i].getY();
        }
        return coors;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInOutFlag() {
        return inOutFlag;
    }

    public void setInOutFlag(String inOutFlag) {
        this.inOutFlag = inOutFlag;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public Flight(String flightId, String flightNo, String runway, String stand, String inOutFlag, String acft, String time, double lon, double lat, double alt, double direction, double speed, String online ,String orderRoadNames,String airlinename) {
        this.flightId = flightId;
        this.flightNo = flightNo;
        this.runway = runway;
        this.stand = stand;
        this.inOutFlag = inOutFlag;
        this.time = time;
        this.acft = acft;
        this.setX(lon);
        this.setY(lat);
        this.setZ(alt);
        this.setDirection(direction);
        this.setSpeed(speed);
        this.online = online;
        this.airline = airlinename;
        this.orderRoadNames=orderRoadNames;
        this.Onstand=inOutFlag=="A"?"N":"Y";
    }

    public Flight() {
    }

    public JSONObject getMes() {
        JSONObject msg = new JSONObject();
        msg.put("type", "position");
        msg.put("flightNo", flightNo);
        msg.put("flightId", flightId);
        msg.put("time", time);
        msg.put("runway", runway);
        msg.put("stand", stand);
        msg.put("lat", y);
        msg.put("lon", x);
        msg.put("alt", z);
        msg.put("airline", airline);
        ///////////////////////////////////////
        msg.put("ifHistory", ifHistory);
        ///////////////////////////////////////
        msg.put("inOutFlag", inOutFlag);
        msg.put("acft", acft);
        msg.put("online", online);
        msg.put("direction", direction);
        msg.put("elevation", elevation);
        msg.put("passlength", passlength);
        return msg;
    }

    public Flight copy() {
        Flight flight=   new Flight(this.flightId, this.flightNo, this.runway, this.stand, this.inOutFlag, this.acft, this.time,
                this.x, this.y, this.z, this.direction, this.speed, this.online, this.orderRoadNames,this.airline);
        flight.elevation=this.elevation;
        flight.speedunit=this.speedunit;
        flight.wait=this.wait;
        flight.lineindex=this.lineindex;
        flight.segindex=this.segindex;
        flight.segyu=this.segyu;
        flight.online=this.online;
        flight.messages.addAll(this.messages);
        flight.Onstand=this.Onstand;
        flight.networkname=this.networkname;
        flight.passlength=this.passlength;
        flight.bestnodes.addAll(this.bestnodes);
        flight.roadlines.addAll(this.roadlines);
        flight.ifHistory=this.ifHistory;
        return flight;
    }

}
