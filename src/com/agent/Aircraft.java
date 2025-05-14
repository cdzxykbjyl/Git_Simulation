package com.agent;


import org.locationtech.jts.geom.Coordinate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:飞机机型要求
 * @author: LiPin
 * @time: 2022-06-08 14:31
 */
public class Aircraft {
    String Name;
    String category;
    String producer;
    int  loadCapacity;
    double wingspan;
    double aircraftlength;
    double x0;
    double y0;
    private Map<String, Double> adij = new HashMap<>();//加减速
    private Map<String, Double> ddij = new HashMap<>();//加减速
    private Map<String, List<Coordinate>> protects = new HashMap<>();

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public int getLoadCapacity() {
        return loadCapacity;
    }

    public void setLoadCapacity(int  loadCapacity) {
        this.loadCapacity = loadCapacity;
    }

    public double getWingspan() {
        return wingspan;
    }

    public void setWingspan(double wingspan) {
        this.wingspan = wingspan;
    }

    public double getAircraftlength() {
        return aircraftlength;
    }

    public void setAircraftlength(double aircraftlength) {
        this.aircraftlength = aircraftlength;
    }

    public double getX0() {
        return x0;
    }

    public void setX0(double x0) {
        this.x0 = x0;
    }

    public double getY0() {
        return y0;
    }

    public void setY0(double y0) {
        this.y0 = y0;
    }

    public Map<String, Double> getAdij() {
        return adij;
    }

    public void setAdij(Map<String, Double> adij) {
        this.adij = adij;
    }

    public Map<String, Double> getDdij() {
        return ddij;
    }

    public void setDdij(Map<String, Double> ddij) {
        this.ddij = ddij;
    }

    public Map<String, List<Coordinate>> getProtects() {
        return protects;
    }

    public void setProtects(Map<String, List<Coordinate>> protects) {
        this.protects = protects;
    }


}
