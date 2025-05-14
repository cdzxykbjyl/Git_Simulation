package com.order;


import com.alibaba.fastjson.JSONObject;
import fun.StandNodes;

/**
 * @description:
 * @author: LiPin
 * @time: 2023-02-27 20:59
 */
public class StandOccupyOrderFatherMes extends OrderFatherMes {
    String flightId = "";//航班id
    String flightNo = "";//航班号
    String runway = "";//跑道号
    StandNodes standNodes = new StandNodes();//跑道号
    String standname = "201";//跑道号
    String inoutflag = "";//进港飞机还是离港飞机

    public StandOccupyOrderFatherMes() {
    }

    public StandOccupyOrderFatherMes(String type, String stand, String time, String flightId, String flightNo, String runway, String inoutflag) {
        this.time = time;
        this.type = type;
        this.flightId = flightId;
        this.flightNo = flightNo;
        this.runway = runway;
        this.inoutflag = inoutflag;
        this.standname = stand;
        this.starttime=time;
        this.endtime=time;
        String[] sdk= starttime.split(" ")[1].split(":");
        this.key=flightId+"_"+sdk[0]+sdk[1]+sdk[2];

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public StandNodes getStandNodes() {
        return standNodes;
    }

    public void setStandNodes(StandNodes standNodes) {
        this.standNodes = standNodes;
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

    public String getInoutflag() {
        return inoutflag;
    }

    public void setInoutflag(String inoutflag) {
        this.inoutflag = inoutflag;
    }

    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key", key);
        msg.put("type", type);
        msg.put("time", time);
        msg.put("starttime", starttime);
        msg.put("endtime", endtime);
        msg.put("flightId", flightId);
        msg.put("flightNo", flightNo);
        msg.put("runway", runway);
        msg.put("inoutflag", inoutflag);
        msg.put("direction", 54);
        msg.put("standmes", standNodes.getMsg());
        return msg;
    }
}
