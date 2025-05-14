package com.fileoperation;

import com.agent.Flight;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: ***检测进入的航班
 * @author: LiPin
 * @time: 2024-01-18 12:02
 */

public class CheckIn {
    public   Map<String , Flight> A_runway=new HashMap<>();//跑道上进港优于出港
    public Map<String , Flight> D_apron=new HashMap<>();//机位上出港优于进港
    public int apron_D_from=20;//推出时检测20s后，是否有对头冲突的进港航班，出现在对头的路上，如果有，则等待对头冲突的航班结束对头冲突，
    // 如果为正，则飞机进入一段冲突路段之前，需要检测20s后是否有对头冲突的航班推出，如果有，则先等待推出，然后进入对头冲突路段，否则直接进入，限制推出航班;
    public  int runway_A_from= 10;//上跑道之前检测10s后是否有待进港航班，如果没有，则上跑道，如果有，则等待最长10s，
    // 如果为负值则进港航班进港之前，检测是10s内有上跑道的飞机，如果有，则等待最长10s，待出港飞机离开之后，再落跑道；

    public Map<String, Flight> getA_runway() {
        return A_runway;
    }

    public void setA_runway(Map<String, Flight> a_runway) {
        A_runway = a_runway;
    }

    public Map<String, Flight> getD_apron() {
        return D_apron;
    }

    public void setD_apron(Map<String, Flight> d_apron) {
        D_apron = d_apron;
    }

    public int getApron_D_from() {
        return apron_D_from;
    }

    public void setApron_D_from(int apron_D_from) {
        this.apron_D_from = apron_D_from;
    }

    public int getRunway_A_from() {
        return runway_A_from;
    }

    public void setRunway_A_from(int runway_A_from) {
        this.runway_A_from = runway_A_from;
    }

    public CheckIn( int runway_A_from,int apron_D_from) {
        this.apron_D_from = apron_D_from;
        this.runway_A_from = runway_A_from;
    }
}
