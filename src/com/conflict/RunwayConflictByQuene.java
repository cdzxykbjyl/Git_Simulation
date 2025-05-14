//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.conflict;

import base.AngDisUtil;
import com.agent.Aircraft;
import com.agent.Flight;
import com.order.RunawayWeit;
import com.runwayrule.AirlineRelease;
import com.runwayrule.ReleaseAfterAircraft;
import com.runwayrule.ReleaseByRunwayNo;
import com.runwayrule.RunwayReleaseRule;
import org.locationtech.jts.geom.Coordinate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
/*
 * 跑道放行规则
 * 放行规则主要分为两种
 * 1.以目标机场为放行规则：西安机场，需要启用updateDirectRunwayMapAndNeedWait函数中的AirlineRelease airlineRelease方法，需要每个离港飞机的目标机场四字码，airlineRelease不为空
 * 2.以机型大小的放行规则：首都机场，需要启用updateDirectRunwayMapAndNeedWait函数中的ReleaseAfterAircraft releaseAfterAircraft方法，需要每个离港飞机的机型大小，具体参照机型配置文件，releaseAfterAircraft不为空
 *
 *
 *
 *
 * */
public class RunwayConflictByQuene {
    public SimpleDateFormat sdf;
    public RunwayReleaseRule runwayReleaseRule;
    Map<String, List<Coordinate>> polygon_runwayocuppysmax = new HashMap();
    public Map<String, List<RunwayFlightHis>> hisdirectrunwaymap = new HashMap();
    public Map<String, List<RunwayFlightHis>> hiscrossrunwaymap = new HashMap();
    Map<String, RunawayWeit> runawayWeitMap = new HashMap<>();
    Map<String, Aircraft> aircraftMap = new HashMap<>();

    ////////////////////////////Wangyi添加对策深度拷贝方法////////////////////////////////////
    public Map<String, List<RunwayFlightHis>> hisrunwaymap_copy(String type){
        Map<String, List<RunwayFlightHis>> hisrunwaymap = new HashMap<>();
        Map<String, List<RunwayFlightHis>> copyed_hisrunwaymap = new HashMap<>();
        if(type.equals("hisdirectrunwaymap")){
            hisrunwaymap = hisdirectrunwaymap;
        }
        if(type.equals("hiscrossrunwaymap")){
            hisrunwaymap = hiscrossrunwaymap;
        }
        for (Entry<String, List<RunwayFlightHis>> entry : hisrunwaymap.entrySet()) {
            String key = entry.getKey();
            List<RunwayFlightHis> value = entry.getValue();
            List<RunwayFlightHis> inner_value = new ArrayList<>();
            for (RunwayFlightHis runwayFlightHis : value) {
                inner_value.add(runwayFlightHis.copy());
            }
            copyed_hisrunwaymap.put(key, inner_value);
        }
        return copyed_hisrunwaymap;
    }
    /////////////////////////////////////////////////////////////////////////////////////

    public RunwayConflictByQuene(){};

    public RunwayConflictByQuene(SimpleDateFormat sdf, Map<String, List<Coordinate>> polygon_runwayocuppysmax, RunwayReleaseRule runwayReleaseRule, Map<String, Aircraft> aircraftMap) {
        this.sdf = sdf;
        this.runwayReleaseRule = runwayReleaseRule;
        this.polygon_runwayocuppysmax = polygon_runwayocuppysmax;
        this.runawayWeitMap = new HashMap<>();
        this.aircraftMap = aircraftMap;
    }

    private boolean isinhisrunway(Flight flight, Map<String, List<RunwayFlightHis>> hismap) {
        boolean isin = false;
        Iterator var3 = hismap.entrySet().iterator();
        while (var3.hasNext()) {
            Entry<String, List<RunwayFlightHis>> entry = (Entry) var3.next();
            List<RunwayFlightHis> flights = entry.getValue();
            for (int i = 0; i < flights.size(); ++i) {
                if (flights.get(i).getFlightId().equals(flight.getFlightId())) {
                    isin = true;
                }
            }
        }
        return isin;
    }

    public RunwayFlightHis convertFlighttoRunwayFlightHis(Flight flight) {
        RunwayFlightHis mes = new RunwayFlightHis();
        mes.setX(flight.getX());
        mes.setY(flight.getY());
        mes.setZ(flight.getZ());
        mes.setTime(flight.getTime());
        mes.setRunway(flight.getRunway());
        mes.setInOutFlag(flight.getInOutFlag());
        mes.setAcft(flight.getAcft());
        mes.setAirline(flight.getAirline());
        mes.setFlightId(flight.getFlightId());
        mes.setFlightNo(flight.getFlightNo());
        mes.setIsinRunway(true);
        return mes;
    }
//删除历史直接跑道飞机map中的某一个飞机
    public void clearal(Flight flight) {
        List<RunwayFlightHis> ls = hisdirectrunwaymap.get(flight.getRunway());
        if (ls != null) {
            int size = ls.size();
            for (int i = 0; i < size; i++) {
                if (ls.get(i).getFlightId().equals(flight.getFlightId())) {
                    ls.remove(i);
                    i--;
                    size--;
                    if (size != 0) {
                        hisdirectrunwaymap.put(flight.getRunway(), ls);
                    }
                }
            }
        }
    }

    public Map<String, List<RunwayFlightHis>> CheckNewDirectRunwayOccupyBymax(Map<String, Flight> real) {
        Map<String, List<RunwayFlightHis>> realdirectrunwaymap = new HashMap();
        AngDisUtil angDisUtil = new AngDisUtil();
        for (Entry<String, Flight> flightEntry : real.entrySet()) {
            Flight flight = flightEntry.getValue();
            if (flight.getInOutFlag().equals("A") && flight.getZ() == 0) {
                clearal(flight);
            } else {
                boolean isind = isinhisrunway(flight, hisdirectrunwaymap);
                if (!isind) {
                    String runway = flight.getRunway();
                    String rerunway = getrerunway(runway);
                    List<Coordinate> runbj = this.polygon_runwayocuppysmax.get(runway + "_" + rerunway) == null ? polygon_runwayocuppysmax.get(rerunway + "_" + runway) : polygon_runwayocuppysmax.get(runway + "_" + rerunway);
                    boolean isinPolygon = angDisUtil.IsinPolygon(runbj, new Coordinate(flight.getX(), flight.getY()));
                    if (isinPolygon && flight.getWait() == 0) {
                        List<RunwayFlightHis> nowflights = realdirectrunwaymap.get(flight.getRunway()) == null ? new ArrayList() : realdirectrunwaymap.get(flight.getRunway());
                        (nowflights).add(convertFlighttoRunwayFlightHis(flight));
                        realdirectrunwaymap.put(flight.getRunway(), nowflights);
                    }
                }
            }
        }
        return realdirectrunwaymap;
    }

    public Map<String, List<RunwayFlightHis>> CheckNewCrossRunwayOccupyBymax(Map<String, Flight> real) {
        AngDisUtil angDisUtil = new AngDisUtil();

        Map<String, List<RunwayFlightHis>> realcrossrunwaymap = new HashMap();
        for (Entry<String, Flight> flightEntry : real.entrySet()) {
            Flight flight = flightEntry.getValue();
            Iterator var13 = this.polygon_runwayocuppysmax.entrySet().iterator();
            while (var13.hasNext()) {
                Entry<String, List<Coordinate>> entry = (Entry) var13.next();
                boolean isindir = isinhisrunway(flight, hisdirectrunwaymap);
                boolean isincorss = isinhisrunway(flight, hiscrossrunwaymap);
                if (!isindir && !isincorss && flight.getWait() == 0) {
                    boolean isin = false;
                    String[] runways = (entry.getKey()).split("_");
                    for (int i = 0; i < runways.length; ++i) {
                        if (flight.getRunway().equals(runways[i])) {
                            isin = true;
                        }
                    }
                    boolean isinPolygon = angDisUtil.IsinPolygon(entry.getValue(), new Coordinate(flight.getX(), flight.getY()));
                    if (isinPolygon && !isin) {
                        List<RunwayFlightHis> nowflights = realcrossrunwaymap.get(flight.getRunway()) == null ? new ArrayList() : realcrossrunwaymap.get(flight.getRunway());
                        (nowflights).add(convertFlighttoRunwayFlightHis(flight));
                        realcrossrunwaymap.put(flight.getRunway(), nowflights);
                    }
                }
            }
        }
        return realcrossrunwaymap;
    }

    private String getrerunway(String runway) {
        String dir = runway.length() == 3 ? (runway.substring(2, 3).equals("L") ? "R" : "L") : "";
        int num = Integer.parseInt(runway.substring(0, 2));
        num = num > 18 ? num - 18 : num + 18;
        String numstr = num < 10 ? 0 + String.valueOf(num) : String.valueOf(num);
        String rerunway = numstr + dir;
        return rerunway;
    }


    public void ClearHisRunwayByMax(Map<String, Flight> reals) {
        Iterator var3 = this.hisdirectrunwaymap.entrySet().iterator();
        while (var3.hasNext()) {
            Entry<String, List<RunwayFlightHis>> entry = (Entry) var3.next();
            List<RunwayFlightHis> hiss = entry.getValue();
            int size = hiss.size();
            for (int i = 0; i < size; i++) {
                RunwayFlightHis his = hiss.get(i);
                Flight flight = reals.get(his.getFlightId());
                //对于进港的飞机落地则清除其在跑道上的信息；
                if (flight != null) {
                    if (flight.getInOutFlag().equals("A") && flight.getZ() == 0 && his.isInRunway()) {
                        his.setIsinRunway(false);
                    }
                }
                //跑断飞机是否还在电子围栏里面，无论进出港，不在电子围栏里，则直接清除；
                boolean isinPolygon = false;
                if (flight != null) {
                    String runway = entry.getKey();
                    String rerunway = getrerunway(runway);
                    List<Coordinate> runbj = polygon_runwayocuppysmax.get(runway + "_" + rerunway) == null ? polygon_runwayocuppysmax.get(rerunway + "_" + runway) : polygon_runwayocuppysmax.get(runway + "_" + rerunway);
                    isinPolygon = (new AngDisUtil()).IsinPolygon(runbj, new Coordinate(flight.getX(), flight.getY()));
                }
                if (!isinPolygon) {
                    hiss.remove(i);
                    i--;
                    size--;
                }
            }
            if (hiss.size() == 0) {
                var3.remove();
            }
        }
        Iterator var4 = this.hiscrossrunwaymap.entrySet().iterator();
        while (var4.hasNext()) {
            Entry<String, List<RunwayFlightHis>> entry = (Entry) var4.next();
            String runway = entry.getKey();
            String rerunway = getrerunway(runway);
            List<Coordinate> runbj = this.polygon_runwayocuppysmax.get(runway + "_" + rerunway) == null ? polygon_runwayocuppysmax.get(rerunway + "_" + runway) : polygon_runwayocuppysmax.get(runway + "_" + rerunway);
            List<RunwayFlightHis> hiss = entry.getValue();
            int size = hiss.size();
            for (int i = 0; i < size; i++) {
                RunwayFlightHis his = hiss.get(i);
                Flight flight = reals.get(his.getFlightId());
                boolean isinPolygon = false;
                if (flight != null) {
                    isinPolygon = (new AngDisUtil()).IsinPolygon(runbj, new Coordinate(flight.getX(), flight.getY()));
                }
                if (!isinPolygon) {
                    hiss.remove(i);
                    i--;
                    size--;
                }
            }
            if (hiss.size() == 0) {
                var4.remove();
            }
        }
    }

    public void updateRunwayConflictByQuene(Map<String, Flight> reals, long longnewtime) {
        Map<String, List<RunwayFlightHis>> realdirectrunwaymap = CheckNewDirectRunwayOccupyBymax(reals);
        Map<String, List<RunwayFlightHis>> realcrossrunwaymap = CheckNewCrossRunwayOccupyBymax(reals);
        ClearHisRunwayByMax(reals);
        updateDirectRunwayMapAndNeedWait(reals, realdirectrunwaymap);
        updateCrossRunwayMapAndNeedWait(longnewtime, reals, realcrossrunwaymap);
    }

    public void updateDirectRunwayMapAndNeedWait(Map<String, Flight> reals, Map<String, List<RunwayFlightHis>> realdirectrunwaymap) {
        Map<String, List<RunwayFlightHis>> dontneedwaitmap = new HashMap();
        Map<String, List<RunwayFlightHis>> needwaitmap = new HashMap();
        Iterator var3 = realdirectrunwaymap.entrySet().iterator();//新的跑道上的信息;36R
        while (var3.hasNext()) {
            Entry<String, List<RunwayFlightHis>> entry = (Entry) var3.next();
            String realrunway = entry.getKey();
            List<RunwayFlightHis> newlist = entry.getValue();
            List<RunwayFlightHis> realdontneedwaits = hisdirectrunwaymap.get(realrunway);//跑道上历史的数据,如果历史上没有，
            if (realdontneedwaits == null) {
                RunwayFlightHis realdontneedwait = getOneByInoutflag(newlist, "A");
                if (realdontneedwait != null) {
                    removeAbyList(newlist, realdontneedwait.copy());
                    List<RunwayFlightHis> dontneewaits = dontneedwaitmap.get(realrunway) == null ? new ArrayList() : dontneedwaitmap.get(realrunway);
                    (dontneewaits).add(realdontneedwait.copy());
                    dontneedwaitmap.put(realrunway, dontneewaits);
                } else {
                    RunwayFlightHis flight = getOneByInoutflag(newlist, "D");
                    if (flight != null) {
                        removeAbyList(newlist, flight.copy());
                        List<RunwayFlightHis> dontneewaits = dontneedwaitmap.get(realrunway) == null ? new ArrayList() : dontneedwaitmap.get(realrunway);
                        (dontneewaits).add(flight.copy());
                        dontneedwaitmap.put(realrunway, dontneewaits);
                    }
                }
            } else {
                List<RunwayFlightHis> runwayFlightHisDs = getAllByInoutflag(realdontneedwaits, "D");//具有历史出港的航班
                RunwayFlightHis runwayFlightHisD = runwayFlightHisDs.size() != 0 ? runwayFlightHisDs.get(runwayFlightHisDs.size() - 1) : null;//历史出港航班
                List<RunwayFlightHis> runwayFlightHisAs = getAllByInoutflag(realdontneedwaits, "A");
                RunwayFlightHis runwayFlightHisA = runwayFlightHisAs.size() != 0 ? runwayFlightHisAs.get(runwayFlightHisAs.size() - 1) : null;//
                RunwayFlightHis realD = getOneByInoutflag(newlist, "D");
                if (realD != null) {
                    boolean isfang = true;
                    //D_D
                    if (runwayFlightHisD != null) {
                        long duretime = 999L;
                        try {
                            duretime = (sdf.parse(realD.getTime()).getTime() - sdf.parse(runwayFlightHisD.getTime()).getTime()) / 1000L;
                        } catch (ParseException var21) {
                            var21.printStackTrace();
                        }
                        double DDtime = 0;

                        //西安机场航线尾流方法
                        AirlineRelease airlineRelease = runwayReleaseRule.getAirlineReleaseMap().get(realD.getAirline() + "_" + runwayFlightHisD.getAirline());
                        ReleaseAfterAircraft releaseAfterAircraft = null;

                        //首都机场航线尾流方法
//                        AirlineRelease airlineRelease=null;
//                        ReleaseAfterAircraft releaseAfterAircraft = runwayReleaseRule.getReleaseAfterAircraftMap().get(aircraftMap.get(realD.getAcft()).getCategory() + "_" + aircraftMap.get(runwayFlightHisD.getAcft()).getCategory());

                        if (airlineRelease == null && releaseAfterAircraft == null) {
                            DDtime = 120.0D;
                        } else if (airlineRelease == null && releaseAfterAircraft != null) {
                            DDtime = Double.parseDouble(releaseAfterAircraft.duretime) * 60.0D;
                        } else if (airlineRelease != null && releaseAfterAircraft == null) {
                            String time = airlineRelease.duretime;
                            if (time.equals("尾流")) {
                                DDtime = 120.0D;
                            } else {
                                DDtime = Double.parseDouble(time) * 60.0D;
                            }
                        } else if (airlineRelease != null && releaseAfterAircraft != null) {
                            String time = airlineRelease.duretime;
                            if (time.equals("尾流")) {
                                DDtime = 120.0D;
                            } else {
                                DDtime = Double.parseDouble(time) * 60.0D;
                            }
                            double DDtime1 = Double.parseDouble(releaseAfterAircraft.duretime) * 60.0D;
                            DDtime = DDtime < DDtime1 ? DDtime : DDtime1;

                        }
                        if ((double) duretime <= DDtime) {
                            isfang = false;//不放行
                        }
                    }
                    //历史中有A，新的有D ,D_A,落地可上跑道
                    boolean isfang2 = true;
                    if (runwayFlightHisA != null) {
                        if (reals.get(runwayFlightHisA.getFlightId()).getZ() > 0) {
                            double duretime = 99999;
                            try {
                                duretime = (sdf.parse(realD.getTime()).getTime() - sdf.parse(runwayFlightHisA.getTime()).getTime()) / 1000L;
                            } catch (ParseException var21) {
                                var21.printStackTrace();
                            }
                            String D_A = runwayReleaseRule.getReleaseByInoutflagHashMap().get("D_A").duretime;
                            String A_A = runwayReleaseRule.getReleaseByInoutflagHashMap().get("A_A").duretime;
                            double DDtime = D_A == null || A_A == null ? 1 * 60 : (Double.parseDouble(A_A) - Double.parseDouble(D_A)) * 60.0D;
                            if (duretime >= DDtime) {  //大于1分钟，不可放行
                                isfang2 = false;
                            }
                        }
                    }
                    //放行
                    if (isfang && isfang2) {
                        removeAbyList(newlist, realD.copy());
                        List<RunwayFlightHis> dontneewaits = dontneedwaitmap.get(realrunway) == null ? new ArrayList() : dontneedwaitmap.get(realrunway);
                        (dontneewaits).add(realD.copy());
                        dontneedwaitmap.put(realrunway, dontneewaits);
                    }
                }
                RunwayFlightHis runwayFlightrealA = this.getOneByInoutflag(newlist, "A");
                if (runwayFlightrealA != null) {
                    //"A_A";两个进港之间需要相隔3分钟及其以上的间距
                    boolean isadd = true;
                    if (runwayFlightHisA != null) {
                        long duretime = 999L;
                        try {
                            duretime = (sdf.parse(runwayFlightrealA.getTime()).getTime() - sdf.parse(runwayFlightHisA.getTime()).getTime()) / 1000L;
                        } catch (ParseException var22) {
                            var22.printStackTrace();
                        }
                        String A_A = runwayReleaseRule.getReleaseByInoutflagHashMap().get("A_A").duretime;
                        double DDtime = A_A == null ? 2 * 60 : Double.parseDouble(A_A) * 60.0D;
                        if ((double) duretime < DDtime) {
                            isadd = false;
                        }
                    }
                    if (runwayFlightHisD != null) {
                        long duretime = 999L;
                        try {
                            duretime = (sdf.parse(runwayFlightrealA.getTime()).getTime() - sdf.parse(runwayFlightHisD.getTime()).getTime()) / 1000L;
                        } catch (ParseException var22) {
                            var22.printStackTrace();
                        }
                        String A_A = runwayReleaseRule.getReleaseByInoutflagHashMap().get("A_A").duretime;
                        String A_D = runwayReleaseRule.getReleaseByInoutflagHashMap().get("A_D").duretime;
                        double AAtime = A_A == null ? 2 * 60 : Double.parseDouble(A_A) * 60.0D;
                        double DDtime = A_D == null ? 2 * 60 : Double.parseDouble(A_D) * 60.0D;
                        if (AAtime < DDtime) {
                            if (duretime < DDtime - AAtime) {
                                isadd = false;
                            }
                        }
                    }
                    if (isadd) {
                        removeAbyList(newlist, runwayFlightrealA.copy());
                        List<RunwayFlightHis> dontneewaits = dontneedwaitmap.get(realrunway) == null ? new ArrayList() : dontneedwaitmap.get(realrunway);
                        (dontneewaits).add(runwayFlightrealA.copy());
                        dontneedwaitmap.put(realrunway, dontneewaits);
                    }
                }
            }
            if (newlist.size() != 0) {
                needwaitmap.put(realrunway, newlist);
            }
        }


        //无需等待的飞机

        //新的需要等待的飞机设置等待1s
        for (Entry<String, List<RunwayFlightHis>> entry : needwaitmap.entrySet()) {
            List<RunwayFlightHis> waitflight = entry.getValue();
            for (int ik = 0; ik < waitflight.size(); ik++) {
                Flight flight = reals.get(waitflight.get(ik).getFlightId());
                List<RunwayFlightHis> hisrunways = hisdirectrunwaymap.get(flight.getRunway());
                if (hisrunways != null) {
                    RunwayFlightHis his = hisrunways.get(hisrunways.size() - 1);
                    flight.setWait(1);
                    RunawayWeit runawayWeit = runawayWeitMap.get(his.getFlightId() + "_" + flight.getFlightId());
                    if (runawayWeit == null) {
                        runawayWeit = new RunawayWeit("runwayoccupy", his.getFlightId(), his.getFlightNo(),
                                his.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    runawayWeit.setTime(flight.getTime());
                    runawayWeitMap.put(his.getFlightId() + "_" + flight.getFlightId(), runawayWeit);
                    flight.getMessages().add(runawayWeit);
                } else {
                    List<RunwayFlightHis> newrunways = dontneedwaitmap.get(flight.getRunway());
                    if (newrunways != null) {
                        RunwayFlightHis his = newrunways.get(newrunways.size() - 1);
                        flight.setWait(1);
                        RunawayWeit runawayWeit = runawayWeitMap.get(his.getFlightId() + "_" + flight.getFlightId());
                        if (runawayWeit == null) {
                            runawayWeit = new RunawayWeit("runwayoccupy", his.getFlightId(), his.getFlightNo(),
                                    his.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                        }
                        runawayWeit.setEndtime(flight.getTime());
                        runawayWeit.setTime(flight.getTime());
                        runawayWeitMap.put(his.getFlightId() + "_" + flight.getFlightId(), runawayWeit);
                        flight.getMessages().add(runawayWeit);
                    }
                }
            }
        }
        Iterator var4 = dontneedwaitmap.entrySet().iterator();
        //不同跑道之间放飞间隔
        while (var4.hasNext()) {
            Entry<String, List<RunwayFlightHis>> entry = (Entry) var4.next();
            String realrunway = entry.getKey();
            List<RunwayFlightHis> realdontneedwaits = entry.getValue();
            RunwayFlightHis realdontneedwait = realdontneedwaits.get(0);
            Iterator var32 = hisdirectrunwaymap.entrySet().iterator();

            while (var32.hasNext()) {
                Entry<String, List<RunwayFlightHis>> entry1 = (Entry) var32.next();
                String hisrunway = entry1.getKey();
                List<RunwayFlightHis> hislist = entry1.getValue();
                List<RunwayFlightHis> hisflightds = this.getAllByInoutflag(hislist, "D");
                if (!realrunway.equals(hisrunway) && hisflightds.size() != 0) {
                    RunwayFlightHis his = hisflightds.get(hisflightds.size() - 1);
                    long realduretime = 999L;
                    try {
                        realduretime = (sdf.parse(realdontneedwait.getTime()).getTime() - sdf.parse(his.getTime()).getTime()) / 1000L;
                    } catch (ParseException var20) {
                        var20.printStackTrace();
                    }

                    ReleaseByRunwayNo releaseByRunwayNo = runwayReleaseRule.getReleaseByRunwayNoMap().get(realdontneedwait.getRunway() + "_" + his.getRunway());
                    int duretime = releaseByRunwayNo == null ? 0 : Integer.parseInt(releaseByRunwayNo.duretime) * 60;

                    if (realdontneedwait.getInOutFlag().equals("D") && his.getInOutFlag().equals("D") && realduretime < (long) duretime) {
                        this.removeAbyList(realdontneedwaits, realdontneedwait);
                        List<RunwayFlightHis> neewaits = realdirectrunwaymap.get(realrunway) == null ? new ArrayList() : realdirectrunwaymap.get(realrunway);
                        RunwayFlightHis r = realdontneedwait.copy();
                        (neewaits).add(r);

                        Flight flight = reals.get(realdontneedwait.getFlightId());
                        flight.setWait(1);
                        RunawayWeit runawayWeit = runawayWeitMap.get(his.getFlightId() + "_" + flight.getFlightId());
                        if (runawayWeit == null) {
                            runawayWeit = new RunawayWeit("runwayoccupy", his.getFlightId(), his.getFlightNo(),
                                    his.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());

                        }
                        runawayWeit.setEndtime(flight.getTime());
                        runawayWeit.setTime(flight.getTime());
                        runawayWeitMap.put(his.getFlightId() + "_" + flight.getFlightId(), runawayWeit);
                        flight.getMessages().add(runawayWeit);
                        reals.put(realdontneedwait.getFlightId(), flight);
                        needwaitmap.put(realrunway, neewaits);
                    }
                }
            }
            if (realdontneedwaits.size() == 0) {
                var4.remove();
            }
        }

        //权重高的无需等待的飞机加入历史跑道占用Map
        for (Entry<String, List<RunwayFlightHis>> entry : dontneedwaitmap.entrySet()) {
            String realrunway = entry.getKey();
            List<RunwayFlightHis> dontneewaits = hisdirectrunwaymap.get(realrunway) == null ? new ArrayList<>() : hisdirectrunwaymap.get(realrunway);
            dontneewaits.addAll(entry.getValue());
            hisdirectrunwaymap.put(realrunway, dontneewaits);
        }
    }

    //更新穿越跑道的飞机

    public void updateCrossRunwayMapAndNeedWait(long longnewtime, Map<String, Flight> reals, Map<String, List<RunwayFlightHis>> realcrossrunwaymap) {

        for (Entry<String, List<RunwayFlightHis>> entry : hisdirectrunwaymap.entrySet()) {
            String key = entry.getKey();
            List<RunwayFlightHis> clist = realcrossrunwaymap.get(key);
            if (clist != null) {//同一跑道
                if ((key.equals("06R") || key.equals("24L")) && (hisdirectrunwaymap.containsKey("06L") || hisdirectrunwaymap.containsKey("24R"))) {
                    List<RunwayFlightHis> f = hisdirectrunwaymap.get("06L");
                    List<RunwayFlightHis> flights1 = f;
                    RunwayFlightHis his = flights1.get(flights1.size() - 1);//上一个占用跑道的飞机
                    long duretime = 999;
                    try {
                        duretime = longnewtime - sdf.parse(his.getTime()).getTime() / 1000;//上一个飞机已经占用跑道时长
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if ((his.getInOutFlag().equals("A") && duretime < 100) || (his.getInOutFlag().equals("D") && duretime < 30)) {
                        //进港飞机占用跑道时长小于2分钟时，可放行,//出港飞机占用跑道时长小于30秒时，可放行
                        hiscrossrunwaymap.put(key, clist);//放入穿越放行列表
                    } else {
                        for (int i = 0; i < clist.size(); i++) {
                            Flight flight = reals.get(clist.get(i).getFlightId());
                            if (flight != null) {
                                RunawayWeit runawayWeit = runawayWeitMap.get(his.getFlightId() + "_" + flight.getFlightId());
                                if (runawayWeit == null) {
                                    runawayWeit = new RunawayWeit("runwayoccupy", his.getFlightId(), his.getFlightNo(),
                                            his.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                                }
                                runawayWeit.setEndtime(flight.getTime());
                                runawayWeit.setTime(flight.getTime());
                                runawayWeitMap.put(his.getFlightId() + "_" + flight.getFlightId(), runawayWeit);
                                flight.getMessages().add(runawayWeit);
                                flight.setWait(1);
                                reals.put(flight.getFlightId(), flight);
                            }
                        }
                    }
                }
                if ((key.equals("05L") || key.equals("23R")) && (hisdirectrunwaymap.containsKey("05R") || hisdirectrunwaymap.containsKey("23L"))) {
                    List<RunwayFlightHis> f = hisdirectrunwaymap.get("05R");
                    List<RunwayFlightHis> flights1 = f;
                    RunwayFlightHis his = flights1.get(flights1.size() - 1);//上一个占用跑道的飞机
                    long duretime = 999;
                    try {
                        duretime = longnewtime - sdf.parse(his.getTime()).getTime() / 1000;//上一个飞机已经占用跑道时长
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if ((his.getInOutFlag().equals("A") && duretime < 100) || (his.getInOutFlag().equals("D") && duretime < 30)) {
                        //进港飞机占用跑道时长小于2分钟时，可放行,//出港飞机占用跑道时长小于30秒时，可放行
                        hiscrossrunwaymap.put(key, clist);//放入穿越放行列表
                    } else {
                        for (int i = 0; i < clist.size(); i++) {
                            Flight flight = reals.get(clist.get(i).getFlightId());
                            if (flight != null) {
                                RunawayWeit runawayWeit = runawayWeitMap.get(his.getFlightId() + "_" + flight.getFlightId());
                                if (runawayWeit == null) {
                                    runawayWeit = new RunawayWeit("runwayoccupy", his.getFlightId(), his.getFlightNo(),
                                            his.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                                }
                                runawayWeit.setEndtime(flight.getTime());
                                runawayWeit.setTime(flight.getTime());
                                runawayWeitMap.put(his.getFlightId() + "_" + flight.getFlightId(), runawayWeit);
                                flight.getMessages().add(runawayWeit);
                                flight.setWait(1);
                                reals.put(flight.getFlightId(), flight);
                            }
                        }
                    }
                }
            }
        }
    }

    private void removeAbyList(List<RunwayFlightHis> reallist, RunwayFlightHis A) {
        int si = reallist.size();
        for (int i = 0; i < si; i++) {
            if ((reallist.get(i)).getFlightId().equals(A.getFlightId())) {
                reallist.remove(i);
                i--;
                si--;
            }
        }

    }

    public List<RunwayFlightHis> getAllByInoutflag(List<RunwayFlightHis> runwayFlightHis, String inoutflag) {
        List<RunwayFlightHis> flights = new ArrayList<>();

        for (int i = 0; i < runwayFlightHis.size(); ++i) {
            if ((runwayFlightHis.get(i)).getInOutFlag().equals(inoutflag) && runwayFlightHis.get(i).isInRunway()) {
                RunwayFlightHis flight1 = (runwayFlightHis.get(i)).copy();
                flights.add(flight1.copy());
            }
        }
        return flights;
    }

    public RunwayFlightHis getOneByInoutflag(List<RunwayFlightHis> runwayFlightHis, String inoutflag) {
        RunwayFlightHis flight = null;

        for (int i = 0; i < runwayFlightHis.size(); ++i) {
            if ((runwayFlightHis.get(i)).getInOutFlag().equals(inoutflag) && runwayFlightHis.get(i).isInRunway()) {
                RunwayFlightHis flight1 = (runwayFlightHis.get(i)).copy();
                flight = flight1.copy();
            }
        }
        return flight;
    }


    public RunwayFlightHis getfirst(List<RunwayFlightHis> flights) {
        RunwayFlightHis flight = flights.get(0);
        for (int i = 0; i < flights.size(); i++) {
            RunwayFlightHis flight1 = flights.get(i);
            long duretime = 999L;
            try {
                duretime = sdf.parse(flight1.getTime()).getTime() - sdf.parse(flight.getTime()).getTime();
            } catch (ParseException var9) {
                var9.printStackTrace();
            }
            if (duretime < 0L) {
                flight = flight1;
            }

        }


        return flight;
    }

    public RunwayFlightHis getend(List<RunwayFlightHis> flights) {
        RunwayFlightHis flight = flights.get(0);
        for (int i = 0; i < flights.size(); i++) {
            RunwayFlightHis flight1 = flights.get(i);
            long duretime = 999L;
            try {
                duretime = sdf.parse(flight1.getTime()).getTime() - sdf.parse(flight.getTime()).getTime();
            } catch (ParseException var9) {
                var9.printStackTrace();
            }
            if (duretime > 0L) {
                flight = flight1;
            }

        }


        return flight;
    }
}
