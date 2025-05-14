package com.runwayrule;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-30 15:57
 */

public class ReleaseAfterAircraft {
    public String  preaircraft="";
    public String  nextaircraft="";
    public String  duretime="";
    public String getPreaircraft() {
        return preaircraft;
    }

    public void setPreaircraft(String preaircraft) {
        this.preaircraft = preaircraft;
    }

    public String getNextaircraft() {
        return nextaircraft;
    }

    public void setNextaircraft(String nextaircraft) {
        this.nextaircraft = nextaircraft;
    }
    public String getDuretime() {
        return duretime;
    }
    public void setDuretime(String duretime) {
        this.duretime = duretime;
    }
    public ReleaseAfterAircraft(String preaircraft, String nextaircraft, String duretime) {
        this.preaircraft = preaircraft;
        this.nextaircraft = nextaircraft;
        this.duretime = duretime;
    }
}
