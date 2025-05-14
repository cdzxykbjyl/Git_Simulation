package com.evaluate;

import com.order.OrderFatherMes;

/**
 * @description://跑道占用
 * @author: LiPin
 * @time: 2022-12-14 18:45
 */
public class Runwaybusy extends OrderFatherMes {
    private   String flightId;
    private   String flightNo;
    private  String inoutflag;
    private  long duretime;

    public String getflightId() {
        return flightId;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public Runwaybusy(){}
    public Runwaybusy(String type,String flightId, String flightNo, String inoutflag, String time){
        this.type=type;
        this.flightNo=flightNo;
        this.flightId = flightId;
        this.inoutflag = inoutflag;
        this.time=time;
        this.starttime=time;
        this.endtime=time;
    }
    public void setflightId(String flightId) {
        this.flightId = flightId;
    }

    public String getInoutflag() {
        return inoutflag;
    }

    public void setInoutflag(String inoutflag) {
        this.inoutflag = inoutflag;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public long getDuretime() {
        return duretime;
    }

    public void setDuretime(long duretime) {
        this.duretime = duretime;
    }
}
