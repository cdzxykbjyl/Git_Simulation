package com.conflict;

import com.agent.Flight;

/**
 * @description: ***上一次怕掉飞机占用情况，包括上一次跑道占用的起始时间和当前时间，尾随飞机的起始时间和终止时间；
 * @author: LiPin
 * @time: 2024-04-02 11:23
 */

public class LastRunwayOccupy {

     Flight flighta=null;
     String astart="";
     Flight flightb=null;
     String bstart="";

    public Flight getFlighta() {
        return flighta;
    }

    public void setFlighta(Flight flighta) {
        this.flighta = flighta;
    }

    public String getAstart() {
        return astart;
    }

    public void setAstart(String astart) {
        this.astart = astart;
    }

    public Flight getFlightb() {
        return flightb;
    }

    public void setFlightb(Flight flightb) {
        this.flightb = flightb;
    }

    public String getBstart() {
        return bstart;
    }

    public void setBstart(String bstart) {
        this.bstart = bstart;
    }
}
