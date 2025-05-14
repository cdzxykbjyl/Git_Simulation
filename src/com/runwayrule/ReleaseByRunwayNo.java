package com.runwayrule;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-30 15:51
 */

public class ReleaseByRunwayNo {
    public String  prerunway="";
    public String  nextrunway="";
    public String  duretime="";
    public String  discribe="";

    public String getPrerunway() {
        return prerunway;
    }

    public void setPrerunway(String prerunway) {
        this.prerunway = prerunway;
    }

    public String getNextrunway() {
        return nextrunway;
    }

    public void setNextrunway(String nextrunway) {
        this.nextrunway = nextrunway;
    }

    public String getDuretime() {
        return duretime;
    }

    public void setDuretime(String duretime) {
        this.duretime = duretime;
    }

    public String getDiscribe() {
        return discribe;
    }

    public void setDiscribe(String discribe) {
        this.discribe = discribe;
    }

    public ReleaseByRunwayNo(String prerunway, String nextrunway, String duretime, String discribe) {
        this.prerunway = prerunway;
        this.nextrunway = nextrunway;
        this.duretime = duretime;
        this.discribe = discribe;
    }
}
