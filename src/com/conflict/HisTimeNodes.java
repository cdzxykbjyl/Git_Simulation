package com.conflict;

import com.agent.Flight;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-04-26 6:30
 */

public class HisTimeNodes {
    private String airline = "";
    private String flightId = "";
    private String flightNo = "";
    private String runway = "05L";
    private String inOutFlag = "A";
    private String acft = "321";
    private String starttime = "";//计划开始时间
    private String realstarttime = "";//仿真实际开始时间
    private String endtime = "";//仿真结束时间
    private String flytime = "";//出港飞机起飞离地时刻
    private String downtime = "";//进港飞机降落落地时刻
    private String startoccupyrunwaytime = "";//开始占用跑道时间，离港飞机跑道口等待结束，开始上跑道时刻，进港飞机落地前2分钟时刻
    private String stopoccupyrunwaytime = "";//停止占用跑道时间 ，离港飞机尾流结束时刻，进港飞机离开跑道时刻
    private String inofflinetime = "";  //进港飞机下线时间
    private String outofflinetime = "";//出港飞机下线时间
    private String startinstandtime = "";//开始入位时间
    private String endinstandtime = "";//结束入位时间
    private String endoutstandtime = "";//结束离位时间
    private String startoutstandtime = "";//开始离位时间
    private String orderRoadNames = "";
    private String allduretime = "";    //总体长度，包括滑行和12公里以内的飞行时长
    private String taxiduretime = "";   //滑行时长，进港飞机为离开跑道到机位，出港飞机为上跑道之前


    public JSONObject geMes() {
        String str = "{" +
                "\"airline\":\"" + airline + '\"' +
                ", \"flightId\":\"" + flightId + '\"' +
                ", \"flightNo\":\"" + flightNo + '\"' +
                ", \"runway\":\"" + runway + '\"' +
                ", \"inOutFlag\":\"" + inOutFlag + '\"' +
                ", \"acft\":\"" + acft + '\"' +
                ", \"starttime\":\"" + starttime + '\"' +
                ", \"endtime\":\"" + endtime + '\"' +
                ", \"realstarttime\":\"" + realstarttime + '\"' +
                ", \"flytime\":\"" + flytime + '\"' +
                ", \"downtime\":\"" + downtime + '\"' +
                ", \"startoccupyrunwaytime\":\"" + startoccupyrunwaytime + '\"' +
                ", \"stopoccupyrunwaytime\":\"" + stopoccupyrunwaytime + '\"' +
                ", \"inofflinetime\":\"" + inofflinetime + '\"' +
                ", \"outofflinetime\":\"" + outofflinetime + '\"' +
                ", \"startinstandtime\":\"" + startinstandtime + '\"' +
                ", \"endinstandtime\":\"" + endinstandtime + '\"' +
                ", \"endoutstandtime\":\"" + endoutstandtime + '\"' +
                ", \"startoutstandtime\":\"" + startoutstandtime + '\"' +
                ", \"orderRoadNames\":\"" + orderRoadNames + '\"' +
                ", \"allduretime\":\"" + allduretime + '\"' +
                ", \"taxiduretime\":\"" + taxiduretime + '\"' +
                '}';
        JSONObject js = JSON.parseObject(str);
        return js;
    }


    public String getRealstarttime() {
        return realstarttime;
    }

    public void setRealstarttime(String realstarttime) {
        this.realstarttime = realstarttime;
    }

    public String getFlytime() {
        return flytime;
    }

    public void setFlytime(String flytime) {
        this.flytime = flytime;
    }

    public String getDowntime() {
        return downtime;
    }

    public void setDowntime(String downtime) {
        this.downtime = downtime;
    }

    public String getStartoccupyrunwaytime() {
        return startoccupyrunwaytime;
    }

    public void setStartoccupyrunwaytime(String startoccupyrunwaytime) {
        this.startoccupyrunwaytime = startoccupyrunwaytime;
    }

    public String getStopoccupyrunwaytime() {
        return stopoccupyrunwaytime;
    }

    public void setStopoccupyrunwaytime(String stopoccupyrunwaytime) {
        this.stopoccupyrunwaytime = stopoccupyrunwaytime;
    }

    public String getInofflinetime() {
        return inofflinetime;
    }

    public void setInofflinetime(String inofflinetime) {
        this.inofflinetime = inofflinetime;
    }

    public String getOutofflinetime() {
        return outofflinetime;
    }

    public void setOutofflinetime(String outofflinetime) {
        this.outofflinetime = outofflinetime;
    }

    public String getStartinstandtime() {
        return startinstandtime;
    }

    public void setStartinstandtime(String startinstandtime) {
        this.startinstandtime = startinstandtime;
    }

    public String getEndinstandtime() {
        return endinstandtime;
    }

    public void setEndinstandtime(String endinstandtime) {
        this.endinstandtime = endinstandtime;
    }

    public String getEndoutstandtime() {
        return endoutstandtime;
    }

    public void setEndoutstandtime(String endoutstandtime) {
        this.endoutstandtime = endoutstandtime;
    }

    public String getStartoutstandtime() {
        return startoutstandtime;
    }

    public void setStartoutstandtime(String startoutstandtime) {
        this.startoutstandtime = startoutstandtime;
    }

    public String getAllduretime() {
        return allduretime;
    }

    public void setAllduretime(String allduretime) {
        this.allduretime = allduretime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;

    }

    public String getTaxiduretime() {
        return taxiduretime;
    }

    public void setTaxiduretime(String taxiduretime) {
        this.taxiduretime = taxiduretime;
    }

    public HisTimeNodes copy() {
        HisTimeNodes runwayFlightHis = new HisTimeNodes();
        runwayFlightHis.inofflinetime = this.getInofflinetime();
        runwayFlightHis.outofflinetime = this.getOutofflinetime();
        runwayFlightHis.startinstandtime = this.getStartinstandtime();//开始入位时间
        runwayFlightHis.endinstandtime = this.getEndinstandtime();//结束入位时间
        runwayFlightHis.endoutstandtime = this.getEndoutstandtime();//结束离位时间
        runwayFlightHis.startoutstandtime = this.getStartoutstandtime();//开始离位时间
        runwayFlightHis.flytime = this.getFlytime();
        runwayFlightHis.downtime = this.getDowntime();
        runwayFlightHis.startoccupyrunwaytime = this.getStartoccupyrunwaytime();
        runwayFlightHis.stopoccupyrunwaytime = this.getStopoccupyrunwaytime();
        runwayFlightHis.runway = this.getRunway();
        runwayFlightHis.inOutFlag = this.getInOutFlag();
        runwayFlightHis.acft = this.getAcft();
        runwayFlightHis.airline = this.getAirline();
        runwayFlightHis.flightId = this.getFlightId();
        runwayFlightHis.starttime = this.getStarttime();
        runwayFlightHis.endtime = this.getEndtime();
        runwayFlightHis.realstarttime = this.getRealstarttime();
        runwayFlightHis.orderRoadNames = this.getOrderRoadNames();
        runwayFlightHis.allduretime = this.getAllduretime();
        runwayFlightHis.taxiduretime = this.getTaxiduretime();
        return runwayFlightHis;
    }

    public HisTimeNodes() {

    }

    public String getOrderRoadNames() {
        return orderRoadNames;
    }

    public void setOrderRoadNames(String orderRoadNames) {
        this.orderRoadNames = orderRoadNames;
    }

    public HisTimeNodes HisTimeNodesByReal(Flight flight) {
        this.starttime = flight.getTime();
        this.runway = flight.getRunway();
        this.inOutFlag = flight.getInOutFlag();
        this.acft = flight.getAcft();
        this.airline = flight.getAirline();
        this.flightId = flight.getFlightId();
        this.flightNo = flight.getFlightNo();
        this.orderRoadNames = flight.getOrderRoadNames();
        return this;
    }


    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }


    public void setEndtime(String endtime, String duretime) {
        this.endtime = endtime;
        this.allduretime = duretime;
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

}
