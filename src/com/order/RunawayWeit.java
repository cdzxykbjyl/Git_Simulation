package com.order;


import base.Time;
import com.alibaba.fastjson.JSONObject;

/**
 * @description:
 * @author: LiPin
 * @time: 2022-08-04 15:10
 */
public class RunawayWeit extends OrderFatherMes {
    private String glideflightId = "FSDF4342";//优先级高的飞机航班号
    private String glideflightNo= "FSDF4342";//优先级高的飞机航班号
    private String glideinoutFlag = "D";//优先级高的飞机进出港标识
    private String stopflightId = "1CA1332";
    private String stopflightNo = "1CA1332";
    private String stopinoutFlag = "A";
    private String runaway = "05L";//跑道号
    private String duretime = "05L";//跑道号
    private String waitqueue="";
    public RunawayWeit() {
    }

    public String getDuretime() {
        return duretime;
    }

    public void setDuretime(String duretime) {
        this.duretime = duretime;
    }

    public String getWaitqueue() {
        return waitqueue;
    }

    public void setWaitqueue(String waitqueue) {
        this.waitqueue = waitqueue;
    }

    public String getGlideflightNo() {
        return glideflightNo;
    }

    public void setGlideflightNo(String glideflightNo) {
        this.glideflightNo = glideflightNo;
    }

    public String getStopflightNo() {
        return stopflightNo;
    }

    public void setStopflightNo(String stopflightNo) {
        this.stopflightNo = stopflightNo;
    }

    public  void setEndtime(String endtime){
        this.endtime=endtime;
        this.time=endtime;
        this.duretime = Time.getTimeDuration(this.endtime, starttime);
    }
    public  String getTime(){
        return  time;
    }


    public String getGlideflightId() {
        return glideflightId;
    }

    public void setGlideflightId(String glideflightId) {
        this.glideflightId = glideflightId;
    }

    public String getGlideinoutFlag() {
        return glideinoutFlag;
    }

    public void setGlideinoutFlag(String glideinoutFlag) {
        this.glideinoutFlag = glideinoutFlag;
    }

    public String getStopflightId() {
        return stopflightId;
    }

    public void setStopflightId(String stopflightId) {
        this.stopflightId = stopflightId;
    }

    public String getStopinoutFlag() {
        return stopinoutFlag;
    }

    public void setStopinoutFlag(String stopinoutFlag) {
        this.stopinoutFlag = stopinoutFlag;
    }

    public String getRunaway() {
        return runaway;
    }

    public void setRunaway(String runaway) {
        this.runaway = runaway;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getsTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }



    public RunawayWeit(String type,
                       String glideflightId,//优先级高的飞机航班号
                       String glideflightNo,//优先级高的飞机航班号
                       String glideinoutFlag,//优先级高的飞机进出港标识
                       String stopflightId,
                       String stopflightNo,
                       String stopinoutFlag,
                       String runaway,//跑道号
                       String time
    ) {
        this.type = type;
        this.glideflightId = glideflightId;//优先级高的飞机航班号
        this.glideflightNo = glideflightNo;//优先级高的飞机航班号
        this.glideinoutFlag = glideinoutFlag;//优先级高的飞机进出港标识
        this.stopflightId = stopflightId;
        this.stopflightNo = stopflightNo;
        this.stopinoutFlag = stopinoutFlag;
        this.runaway = runaway;//跑道号
        this.time=time;
        this.starttime=time;
        this.endtime=time;
        this.duretime="00:00";
        String[] sdk= starttime.split(" ")[1].split(":");
        this.key=glideflightId+"_"+stopflightId+"_"+sdk[0]+sdk[1]+sdk[2];
        this.waitqueue=stopflightId;
    }

    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key",key);
        msg.put("type",type);
        msg.put("glideflightId",glideflightId);
        msg.put("glideflightNo",glideflightNo);
        msg.put("glideinoutFlag",glideinoutFlag);
        msg.put("stopflightId",stopflightId);
        msg.put("stopflightNo",stopflightNo);
        msg.put("stopinoutFlag",stopinoutFlag);
        msg.put("runaway",runaway);
        msg.put("time",time);
        msg.put("duretime", duretime);
        msg.put("starttime",starttime);
        msg.put("endtime",endtime);
        msg.put("waitqueue",waitqueue);
        return msg;
    }
}
