package com.runwayrule;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-30 15:59
 */

public class ReleaseByInoutflag {

    public String  preinoutflag="";
    public String  nextinoutflag="";
    public String  duretime="";
    public String  discribe="";

    public String getPreinoutflag() {
        return preinoutflag;
    }

    public void setPreinoutflag(String preinoutflag) {
        this.preinoutflag = preinoutflag;
    }

    public String getNextinoutflag() {
        return nextinoutflag;
    }

    public void setNextinoutflag(String nextinoutflag) {
        this.nextinoutflag = nextinoutflag;
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

    public ReleaseByInoutflag(String preinoutflag, String nextinoutflag, String duretime, String discribe) {
        this.preinoutflag = preinoutflag;
        this.nextinoutflag = nextinoutflag;
        this.duretime = duretime;
        this.discribe = discribe;
    }

    public void setDiscribe(String discribe) {
        this.discribe = discribe;
    }
}
