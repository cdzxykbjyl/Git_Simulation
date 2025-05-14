package com.runwayrule;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-30 15:54
 */

public class AirlineRelease {



    public String  preairline="";
    public String  pregroup="";
    public String  nextairline="";
    public String  nextgroup="";
    public String  duretime="";

    public String getPreairline() {
        return preairline;
    }

    public void setPreairline(String preairline) {
        this.preairline = preairline;
    }

    public String getPregroup() {
        return pregroup;
    }

    public void setPregroup(String pregroup) {
        this.pregroup = pregroup;
    }

    public String getNextairline() {
        return nextairline;
    }

    public void setNextairline(String nextairline) {
        this.nextairline = nextairline;
    }

    public String getNextgroup() {
        return nextgroup;
    }

    public void setNextgroup(String nextgroup) {
        this.nextgroup = nextgroup;
    }

    public AirlineRelease(String preairline, String pregroup, String nextairline, String nextgroup, String duretime) {
        this.preairline = preairline;
        this.pregroup = pregroup;
        this.nextairline = nextairline;
        this.nextgroup = nextgroup;
        this.duretime = duretime;
    }

    public String getDuretime() {
        return duretime;
    }

    public void setDuretime(String duretime) {
        this.duretime = duretime;
    }
}
