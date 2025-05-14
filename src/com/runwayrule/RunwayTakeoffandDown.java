package com.runwayrule;

import fun.FlyAndSpeedNodes;

import java.util.ArrayList;
import java.util.List;


/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-07-10 13:11
 */

public class RunwayTakeoffandDown {
    public  String  runway;
    public String inoutflag;
    public String LineName;
    public List<FlyAndSpeedNodes> flyAndSpeedNodesList=new ArrayList<>();

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

    public String getLineName() {
        return LineName;
    }

    public void setLineName(String lineName) {
        LineName = lineName;
    }

    public List<FlyAndSpeedNodes> getFlyAndSpeedNodesList() {
        return flyAndSpeedNodesList;
    }
    public RunwayTakeoffandDown(String runway, String inoutflag, String lineName) {
        this.runway = runway;
        this.inoutflag = inoutflag;
        this.LineName = lineName;
        this.flyAndSpeedNodesList = flyAndSpeedNodesList;
    }
    public RunwayTakeoffandDown(String runway, String inoutflag, String lineName, List<FlyAndSpeedNodes> flyAndSpeedNodesList) {
        this.runway = runway;
        this.inoutflag = inoutflag;
        this.LineName = lineName;
        this.flyAndSpeedNodesList = flyAndSpeedNodesList;
    }

    public void setFlyAndSpeedNodesList(List<FlyAndSpeedNodes> flyAndSpeedNodesList) {
        this.flyAndSpeedNodesList = flyAndSpeedNodesList;
    }
}
