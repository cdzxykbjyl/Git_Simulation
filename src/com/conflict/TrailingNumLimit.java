package com.conflict;

import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-07-04 12:24
 */

public class TrailingNumLimit {

    String name = "";
    Map<String, RoadLine> roads = new HashMap<>();
    List<Coordinate>coordinates=new ArrayList<>();
    Map<String, String>roadnames=new HashMap<>();
    int limitnum = 0;

    String[] limitdirs = new String[]{};
    String describe = "";

    public Map<String, String> getRoadnames() {
        return roadnames;
    }

    public void setRoadnames(Map<String, String> roadnames) {
        this.roadnames = roadnames;
    }

    public Map<String, RoadLine> getRoads() {
        return roads;
    }

    public void setRoads(Map<String, RoadLine> roads) {
        this.roads = roads;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public int getLimitnum() {
        return limitnum;
    }

    public void setLimitnum(int limitnum) {
        this.limitnum = limitnum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getLimitdirs() {
        return limitdirs;
    }

    public void setLimitdirs(String[] limitdirs) {
        this.limitdirs = limitdirs;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public TrailingNumLimit(String name,  Map<String, RoadLine> roads, List<Coordinate> coordinates, int num, String[] limitdirs, String describe) {
        this.name = name;
        this.limitnum = num;
        this.roads = roads;
        this.limitdirs = limitdirs;
        this.describe = describe;
        this.coordinates=coordinates;
    }
}
