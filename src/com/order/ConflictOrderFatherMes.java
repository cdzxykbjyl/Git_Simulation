package com.order;


import base.Time;
import com.alibaba.fastjson.JSONObject;
import com.conflict.ConflictRoadMes;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: LiPin
 * @time: 2022-12-24 15:42
 */
public class ConflictOrderFatherMes extends OrderFatherMes {
    private boolean bisin = false;
    private String level = "";  //冲突等级
    private double score = 0;//冲突发生时间
    private String duretime = "";//冲突发生时间
    public String conflictquene = "";
    public ConflictMes glideflightA = new ConflictMes();//主动飞机
    public ConflictMes stopflightB = new ConflictMes();//词飞机
    public int maxnum = 999;//最大可占据数量
    public Map<String, ConflictRoadMes> mesMap = new HashMap<>();

    public String getConflictquene() {
        return conflictquene;
    }
    public  String getTime(){
        return time;
    }
    public void setConflictquene(String conflictobjs) {
        this.conflictquene = conflictobjs;
    }

    public int getMaxnum() {
        return maxnum;
    }

    public void setMaxnum(int maxnum) {
        this.maxnum = maxnum;
    }

    public Map<String, ConflictRoadMes> getMesMap() {
        return mesMap;
    }

    public void setMesMap(Map<String, ConflictRoadMes> mesMap) {
        this.mesMap = mesMap;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public ConflictMes getGlideflightA() {
        return glideflightA;
    }

    public void setGlideflightA(ConflictMes glideflightA) {
        this.glideflightA = glideflightA;
    }

    public ConflictMes getStopflightB() {
        return stopflightB;
    }

    public void setStopflightB(ConflictMes stopflightB) {
        this.stopflightB = stopflightB;
    }



    @Override
    public String toString() {
        String str = "";

        str = str + "]";
        return "{\"type\":\"" + type + "\",\"mes\":{" +
                //  ",\"roads\":" + str +
                ", \"bisin\":" + bisin +
                ", \"level\":\"" + level + "\"" +
                ", \"key\":\"" + key + "\"" +
                ", \"time\":\"" + time + "\"" +
                ", \"starttime\":\"" + starttime + "\"" +
                ", \"endtime\":\"" + endtime + "\"" +
                ", \"duretime\":\"" + duretime + "\"" +
                ", \"conflictquene\":" + conflictquene +
                ", \"glideflight\":" + glideflightA.toString() +
                ", \"stopflight\":" + stopflightB.toString() +
                "}" +
                "}";
    }

    public JSONObject getMsg() {
        JSONObject msg = new JSONObject();
        msg.put("key", key);
        msg.put("type", type);
        msg.put("glideflight", glideflightA.getMes());
        msg.put("flightIdB", stopflightB.getMes());
        msg.put("bisin", bisin);
        msg.put("level", level);
        msg.put("time", time);
        msg.put("starttime", starttime);
        msg.put("endtime", endtime);
        msg.put("duretime", duretime);
        msg.put("conflictquene", conflictquene);
        return msg;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBisin() {
        return bisin;
    }

    public void setBisin(boolean bisin) {
        this.bisin = bisin;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
        this.time=starttime;
        String[] sdk= starttime.split(" ")[1].split(":");;
        this.key=  glideflightA.flightId+"_"+stopflightB.getFlightId()+"_"+sdk[0]+sdk[1]+sdk[2];
        this.duretime="00:00";
        this.endtime=starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.time=endtime;
        this.endtime = endtime;
        this.duretime = Time.getTimeDuration(this.endtime, starttime);
    }

    public String getDuretime() {
        return duretime;
    }

    public ConflictOrderFatherMes() {
    }

    public ConflictOrderFatherMes(String flightIdA, String conflictquene, String type, String flightIdB, String flightNoA, String flightNoB, boolean bisin, String level, String time, String duretime, RoadLine locationA, RoadLine locationB, Coordinate glide, Coordinate stop) {
        this.time = time;
        this.starttime = time;
        this.endtime = time;
        this.type = type;
        this.bisin = bisin;
        this.level = level;
        this.duretime = duretime;
        this.conflictquene = conflictquene;
        this.glideflightA.location = locationA;
        this.glideflightA.flightId = flightIdA;
        this.glideflightA.flightNo = flightNoA;
        this.glideflightA.firstlocation = glide;
        this.stopflightB.location = locationB;
        this.stopflightB.firstlocation = stop;
        this.stopflightB.flightId = flightIdB;
        this.stopflightB.flightNo = flightNoB;
        String[] sdk= starttime.split(" ")[1].split(":");;
        this.key=  glideflightA.flightId+stopflightB.getFlightId()+sdk[0]+sdk[1]+sdk[2];
    }

    public void setDuretime(String duretime) {
        this.duretime = duretime;
    }


}
