package com.conflict;
/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-08 14:28
 */

public class WeightArea {
    public String name;
    public String type;
    String[] priordirs;
    double priordis;
    double  waitdis;
    public String[] crosslines;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String[] getPriordirs() {
        return priordirs;
    }
    public void setPriordirs(String[] priordirs) {
        this.priordirs = priordirs;
    }
    public double getPriordis() {
        return priordis;
    }

    public double getWaitdis() {
        return waitdis;
    }

    public void setWaitdis(double waitdis) {
        this.waitdis = waitdis;
    }

    public void setPriordis(double priordis) {
        this.priordis = priordis;
    }

    public String[] getCrosslines() {
        return crosslines;
    }

    public void setCrosslines(String[] crosslines) {
        this.crosslines = crosslines;
    }

    public WeightArea(String type,String name, String[] priordirs, double priordis,double  waitdis, String[] crosslines) {
        this.name = name;
        this.priordirs = priordirs;
        this.priordis = priordis;
        this.crosslines = crosslines;
        this.waitdis=waitdis;
        this.type=type;
    }
}
