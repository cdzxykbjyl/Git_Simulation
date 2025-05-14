package com.conflict;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-06-03 19:40
 */

public class ConflictRoadMes {
    String orderRoadNames = "";
    List<Coordinate> coordinates = new ArrayList<>();
    Coordinate enterpoints = new Coordinate();
    double  enterangle = 0;
    double waitdis = 200;
    double priordis = 0;
    int[] indexab = new int[]{};
    String[] roadids = new String[]{};

    public double getEnterangle() {
        return enterangle;
    }

    public void setEnterangle(double enterangle) {
        this.enterangle = enterangle;
    }

    public String getOrderRoadNames() {
        return orderRoadNames;
    }

    public void setOrderRoadNames(String orderRoadNames) {
        this.orderRoadNames = orderRoadNames;
    }

    public Coordinate getEnterpoints() {
        return enterpoints;
    }

    public void setEnterpoints(Coordinate enterpoints) {
        this.enterpoints = enterpoints;
    }

    public double getWaitdis() {
        return waitdis;
    }

    public void setWaitdis(double waitdis) {
        this.waitdis = waitdis;
    }

    public double getPriordis() {
        return priordis;
    }

    public void setPriordis(double priordis) {
        this.priordis = priordis;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public String[] getRoadids() {
        return roadids;
    }

    public void setRoadids(String[] roadids) {
        this.roadids = roadids;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public int[] getIndexab() {
        return indexab;
    }

    public void setIndexab(int[] indexab) {
        this.indexab = indexab;
    }

    public ConflictRoadMes(List<Coordinate> coordinates, String orderRoadNames, int[] indexab, String[] roadids, Coordinate enterpoints, double waitdis,  double priordis,double enterangle) {
        this.coordinates = coordinates;
        this.indexab = indexab;
        this.roadids = roadids;
        this.enterpoints = enterpoints;
        this.orderRoadNames = orderRoadNames;
        this.waitdis = waitdis;
        this.priordis=priordis;
        this.enterangle=enterangle;
    }
}
