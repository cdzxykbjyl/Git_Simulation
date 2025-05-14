package com.conflict;

import base.AngDisUtil;
import com.agent.Flight;
import com.evaluate.Runwaybusy;
import com.order.RunawayWeit;

import com.runwayrule.RunwayReleaseRule;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-04-02 13:35
 */

public class RunwayConflict {
    Map<String, List<Coordinate>> runwayoccupy = new HashMap<>();
    Map<String, LastRunwayOccupy> lastrunwaymes = new HashMap<>();
    Map<String, Runwaybusy> runwaybusyMap = new HashMap<>();//跑道占用时间统计
    RunwayReleaseRule runwayReleaseRuler;
    SimpleDateFormat sdf;


    Map<String,RunawayWeit>runawayWeitMap=new HashMap<>();
    public RunwayConflict( SimpleDateFormat sdf , Map<String, List<Coordinate>> runwayoccupy,   RunwayReleaseRule runwayReleaseRuler) {
        this.runwayoccupy = runwayoccupy;
        this.runwayReleaseRuler=runwayReleaseRuler;
        this.sdf=sdf;
    }

    public RunwayConflict() {

    }


    public Map<String, Map<String, List<Flight>>> getRunwayMaps(Map<String, Flight> real) {
        Map<String, Map<String, List<Flight>>> ocupyeds = new HashMap<>();
        //获取到已经上跑到的飞机，正在或准备起飞和降落的飞机，目标跑道与飞机所处跑道一致
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            List<Coordinate> runbj = runwayoccupy.get(flight.getRunway() + flight.getInOutFlag());
            boolean isinPolygon = new AngDisUtil().IsinPolygon(runbj, new Coordinate(flight.getX(), flight.getY()));
            if (isinPolygon) {
                Map<String, List<Flight>> flights = ocupyeds.get(flight.getRunway()) != null ? ocupyeds.get(flight.getRunway()) : new HashMap<>();
                List<Flight> flights1 = flights.get(flight.getInOutFlag()) != null ? flights.get(flight.getInOutFlag()) : new ArrayList<>();
                flights1.add(flight);
                flights.put(flight.getInOutFlag(), flights1);
                ocupyeds.put(flight.getRunway(), flights);
            } else {
                if (ocupyeds.get(flight.getRunway()) != null) {
                    Map<String, List<Flight>> flights = ocupyeds.get(flight.getRunway());
                    if (flights.get(flight.getInOutFlag()) != null) {
                        List<Flight> flights1 = flights.get(flight.getInOutFlag());
                        int size = flights1.size();
                        for (int i = 0; i < size; i++) {
                            if (flights1.get(i).getFlightId().equals(flight.getFlightId())) {
                                flights1.remove(i);
                                size--;
                                i--;
                            }
                        }
                    }
                    ocupyeds.put(flight.getRunway(), flights);
                }
            }
        }


//检测穿越跑道的飞机，飞机的目标跑道与飞机现处跑道不一致。
        for (Map.Entry<String, Flight> flightEntry : real.entrySet()) {
            Flight flight = flightEntry.getValue();
            for (Map.Entry<String, List<Coordinate>> entry : runwayoccupy.entrySet()) {
                String runway = entry.getKey().substring(0, 3);
                int num = Integer.parseInt(runway.substring(0, 2));
                int re = num > 18 ? num - 18 : num + 18;
                String rerunway = (re < 10 ? 0 + String.valueOf(re) : String.valueOf(re)) + (runway.substring(2, 3).equals("L") ? "R" : "L");
                if (!flight.getRunway().equals(runway) && !flight.getRunway().equals(rerunway)) {
                    boolean isinPolygon = new AngDisUtil().IsinPolygon(entry.getValue(), new Coordinate(flight.getX(), flight.getY()));
                    if (isinPolygon) {
                        Map<String, List<Flight>> flights = ocupyeds.get(runway) != null ? ocupyeds.get(runway) : new HashMap<>();
                        List<Flight> flights1 = flights.get("C") != null ? flights.get("C") : new ArrayList<>();
                        boolean isin = false;
                        for (int i = 0; i < flights1.size(); i++) {
                            if (flights1.get(i).getFlightId() == flight.getFlightId()) {
                                isin = true;
                            }
                        }
                        if (!isin) {
                            flights1.add(flight);
                            flights.put("C", flights1);
                        }
                        ocupyeds.put(runway, flights);
                    }
                }
            }
        }
        return ocupyeds;
    }

    //更新跑道占用
    public void updateRunbesy(Map<String, Flight> real) {
        Map<String, Map<String, List<Flight>>> ocupyedbyoneflight = getRunwayMaps(real);
        String[] sx = new String[]{"A", "C", "D"};
        removeWaitflight(ocupyedbyoneflight, sx);//
        chuliConflict(real, ocupyedbyoneflight, sx);
        calRunocupy(ocupyedbyoneflight);
    }

    //查询历史的是否在新的里面
    private Flight getFF(List<Flight> flightsA, List<Flight> flightsD, Flight flight) {
        Flight FF = null;
        if (flightsD != null) {
            for (int i = 0; i < flightsD.size(); i++) {
                if (flightsD.get(i).getFlightId().equals(flight.getFlightId())) {
                    FF = flightsD.get(i);
                }
            }
        }
        if (flightsA != null) {
            for (int i = 0; i < flightsA.size(); i++) {
                if (flightsA.get(i).getFlightId().equals(flight.getFlightId())) {
                    FF = flightsA.get(i);
                }
            }
        }
        return FF;
    }


    public LastRunwayOccupy getandRemoveold(List<Flight> flightsA, List<Flight> flightsD, String runwayname) {
        LastRunwayOccupy lastRunwayOccupy = lastrunwaymes.get(runwayname);
        if (lastRunwayOccupy == null) {
            return null;
        } else {
            Flight FFa = null;
            if (lastRunwayOccupy.flighta != null) {
                FFa = getFF(flightsA, flightsD, lastRunwayOccupy.flighta);
            }
            Flight FFb = null;
            if (lastRunwayOccupy.flightb != null) {
                FFb = getFF(flightsA, flightsD, lastRunwayOccupy.flightb);
            }
            if (FFa != null) {
                FFa.setWait(0);
                lastRunwayOccupy.flighta = FFa.copy();
                if (FFb != null) {
                    FFb.setWait(0);
                    lastRunwayOccupy.flightb = FFb.copy();
                } else {
                    lastRunwayOccupy.flightb = null;
                    lastRunwayOccupy.bstart = "";
                }
            } else {
                if (FFb != null) {
                    FFb.setWait(0);
                    lastRunwayOccupy.flighta = FFb.copy();
                    lastRunwayOccupy.astart = lastRunwayOccupy.bstart;
                    lastRunwayOccupy.flightb = null;
                    lastRunwayOccupy.bstart = "";
                } else {
                    lastRunwayOccupy = null;
                }
            }
            return lastRunwayOccupy;
        }
    }


    public Map<String, Map<String, List<Flight>>> chuliConflict(Map<String, Flight> real, Map<String, Map<String, List<Flight>>> ocupyedbyoneflight, String[] sx) {

        for (Map.Entry<String, Map<String, List<Flight>>> entry : ocupyedbyoneflight.entrySet()) {
            Map<String, List<Flight>> flightmap = entry.getValue();
            List<Flight> flightsA = flightmap.get(sx[0]);
            List<Flight> flightsC = flightmap.get(sx[1]);
            List<Flight> flightsD = flightmap.get(sx[2]);
            if (flightsA == null) {
                LastRunwayOccupy lastrunwayoccupy = getandRemoveold(flightsC, flightsD, entry.getKey());//查询是否有历史，历史是否在该范围内
                if (lastrunwayoccupy == null) {
                    if (flightsC != null && flightsD == null) {
                        A(flightsC, entry.getKey(), real);
                    } else if (flightsC == null && flightsD != null) {
                        D(flightsD, entry.getKey(), real);
                    } else if (flightsC != null && flightsD != null) {
                        CD(flightsC, flightsD, entry.getKey(), real);
                    }
                } else {
                    if (flightsC != null) {
                        hasold(lastrunwayoccupy, flightsC, real);
                    }
                    if (flightsD != null) {
                        hasold(lastrunwayoccupy, flightsD, real);
                    }
                }
            }
            if (flightsA != null && flightsC == null) {
                LastRunwayOccupy lastrunwayoccupy = getandRemoveold(flightsA, flightsD, entry.getKey());//查询是否有历史，历史是否在该范围内
                if (lastrunwayoccupy == null) {
                    if (flightsD == null) {
                        A(flightsA, entry.getKey(), real);
                    } else if (flightsD != null) {
                        AD(flightsA, flightsD, entry.getKey(), real);
                    }
                } else {
                    if (flightsD != null) {
                        hasold(lastrunwayoccupy, flightsD, real);
                    }
                    if (flightsA != null) {
                        hasoldA(lastrunwayoccupy, flightsA, real);
                    }
                }
            }
            if (flightsA != null && flightsC != null) {
                LastRunwayOccupy lastrunwayoccupy = getandRemoveold(flightsA, flightsC, entry.getKey());//查询是否有历史，历史是否在该范围内
                if (lastrunwayoccupy == null) {
                    AC(flightsA, flightsC, entry.getKey(), real);
                } else {
                    if (flightsA != null) {
                        hasoldA(lastrunwayoccupy, flightsA, real);
                    }
                    if (flightsC != null) {
                        hasold(lastrunwayoccupy, flightsC, real);
                    }
                }
                lastrunwayoccupy = getandRemoveold(flightsA, flightsD, entry.getKey());//查询是否有历史，历史是否在该范围内
                if (lastrunwayoccupy == null) {
                    if (flightsD == null) {
                        A(flightsA, entry.getKey(), real);
                    } else if (flightsD != null) {
                        AD(flightsA, flightsD, entry.getKey(), real);
                    }
                } else {
                    if (flightsD != null) {
                        hasold(lastrunwayoccupy, flightsD, real);
                    }
                    if (flightsA != null) {
                        hasoldA(lastrunwayoccupy, flightsA, real);
                    }
                }
            }
        }
        return ocupyedbyoneflight;
    }


    private void hasold(LastRunwayOccupy oldflights, List<Flight> flights, Map<String, Flight> real) {
        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);
            boolean isequal = false;
            if (oldflights.flighta != null) {
                if (!isequal) {
                    isequal = flight.getFlightId().equals(oldflights.flighta.getFlightId());
                }
            }
            if (oldflights.flightb != null) {
                if (!isequal) {
                    isequal = flight.getFlightId().equals(oldflights.flightb.getFlightId());
                }
            }
            if (!isequal) {
                flight.setSpeed(0);
                flight.setWait(1);//待监测飞机等待+1
                RunawayWeit runawayWeit =   runawayWeitMap.get(oldflights.flighta.getFlightId()+"_"+flight.getFlightId());
                if(runawayWeit==null){
                    runawayWeit = new RunawayWeit("runwayoccupy", oldflights.flighta.getFlightId(), oldflights.flighta.getFlightNo(),
                            oldflights.flighta.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                }
                runawayWeit.setEndtime(flight.getTime());
                flight.getMessages().add(runawayWeit);
                real.put(flight.getFlightId(), flight);
            }
        }
    }

    private void hasoldA(LastRunwayOccupy lastrunwayoccupy, List<Flight> flightsA, Map<String, Flight> real) {
        if (lastrunwayoccupy.flightb != null) {
            for (int i = 0; i < flightsA.size(); i++) {
                Flight flight = flightsA.get(i);
                boolean isequal = flight.getFlightId().equals(lastrunwayoccupy.flighta.getFlightId());
                if (!isequal) {
                    isequal = flight.getFlightId().equals(lastrunwayoccupy.flightb.getFlightId());
                }
                if (!isequal) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(lastrunwayoccupy.flighta.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null){


                     runawayWeit = new RunawayWeit("runwayoccupy", lastrunwayoccupy.flighta.getFlightId(), lastrunwayoccupy.flighta.getFlightNo(),
                            lastrunwayoccupy.flighta.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());

                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        } else {
            for (int i = 0; i < flightsA.size(); i++) {
                Flight flight = flightsA.get(i);
                boolean isequal = flight.getFlightId().equals(lastrunwayoccupy.flighta.getFlightId());
                if (!isequal && lastrunwayoccupy.flightb == null) {

                    long time = 999;
                    try {
                        time = sdf.parse(flight.getTime()).getTime() - sdf.parse(lastrunwayoccupy.astart).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (time > 120000) {
                        isequal = true;
                        lastrunwayoccupy.flightb = flight.copy();
                        lastrunwayoccupy.bstart = flight.getTime();
                    }
                }
                if (!isequal) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(lastrunwayoccupy.flighta.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                         runawayWeit = new RunawayWeit("runwayoccupy", lastrunwayoccupy.flighta.getFlightId(), lastrunwayoccupy.flighta.getFlightNo(),
                                lastrunwayoccupy.flighta.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }



    public void hasold(LastRunwayOccupy oldflights, List<Flight> flightsA, List<Flight> flightsD, Map<String, Flight> real) {
        if (flightsA != null) {
            for (int i = 0; i < flightsA.size(); i++) {
                Flight flight = flightsA.get(i);
                boolean isequal = false;
                if (oldflights.flighta != null) {
                    if (!isequal) {
                        isequal = flight.getFlightId().equals(oldflights.flighta.getFlightId());
                    }
                }
                if (oldflights.flightb != null) {
                    if (!isequal) {
                        isequal = flight.getFlightId().equals(oldflights.flightb.getFlightId());
                    }
                }
                if (!isequal) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(oldflights.flighta.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                         runawayWeit = new RunawayWeit("runwayoccupy", oldflights.flighta.getFlightId(), oldflights.flighta.getFlightNo(),
                                oldflights.flighta.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());

                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
        if (flightsD != null) {
            for (int i = 0; i < flightsD.size(); i++) {
                Flight flight = flightsD.get(i);

                boolean isequal = false;
                if (oldflights.flighta != null) {
                    if (!isequal) {
                        isequal = flight.getFlightId().equals(oldflights.flighta.getFlightId());
                    }
                }
                if (oldflights.flightb != null) {
                    if (!isequal) {
                        isequal = flight.getFlightId().equals(oldflights.flightb.getFlightId());
                    }
                }
                if (!isequal) {
                    flight.setWait(1);//待监测飞机等待+1
                    flight.setSpeed(0);
                    //输出待监测飞机的跑道冲突信息
                    RunawayWeit runawayWeit = runawayWeitMap.get(oldflights.flighta.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", oldflights.flighta.getFlightId(), oldflights.flighta.getFlightNo(),
                            oldflights.flighta.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(),
                            flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }

    //更新占用跑道的时间长度
    public void calRunocupy(Map<String, Map<String, List<Flight>>> ocupyedbyoneflight) {
        for (Map.Entry<String, Map<String, List<Flight>>> entry : ocupyedbyoneflight.entrySet()) {
            Map<String, List<Flight>> flights = entry.getValue();
            for (Map.Entry<String, List<Flight>> en : flights.entrySet()) {
                List<Flight> list = en.getValue();
                for (int i = 0; i < list.size(); i++) {
                    Flight flight = list.get(i);
                    Runwaybusy runwaybusy = new Runwaybusy();
                    if (runwaybusyMap.get(flight.getFlightId()) == null) {
                        runwaybusy = new Runwaybusy("runwaybusy",flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getTime());
                    } else {
                        runwaybusy = runwaybusyMap.get(flight.getFlightId());
                        runwaybusy.setEndtime(flight.getTime());
                        try {
                            runwaybusy.setDuretime((sdf.parse(runwaybusy.getEndtime()).getTime()
                                    - sdf.parse(runwaybusy.getStarttime()).getTime()) / 1000);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    runwaybusyMap.put(flight.getFlightId(), runwaybusy);
                }
            }
        }
    }

    //移去等待的飞机
    public void removeWaitflight(Map<String, Map<String, List<Flight>>> ocupyedbyoneflight, String[] sx) {
        for (Map.Entry<String, Map<String, List<Flight>>> entry : ocupyedbyoneflight.entrySet()) {
            Map<String, List<Flight>> flightmap = entry.getValue();
            for (int ij = 0; ij < sx.length; ij++) {
                List<Flight> flightsi = flightmap.get(sx[ij]);
                if (flightsi != null) {
                    int asize = flightsi.size();
                    for (int i = 0; i < asize; i++) {
                        if (flightsi.get(i).getWait() != 0) {
                            if (lastrunwaymes.get(flightsi.get(i).getRunway()) == null) {
                                flightsi.remove(i);//该飞机不占用该跑道，占用该跑道的是其他飞机，则该飞机删除，暂时不参与跑道权重分配
                                asize--;
                                i--;
                            }
                        }
                    }
                    if (flightsi.size() == 0) {
                        flightmap.remove(sx[ij]);
                    }
                }
            }
        }
    }

    private Flight getAflight_ByCD(List<Flight> flightsC, List<Flight> flightsD) {
        Flight inoutline_f = null;
        Flight centerF = null;
        for (int i = 0; i < flightsD.size(); i++) {
            Flight flight = flightsD.get(i);
            List<RoadLine> rs = flight.getRoadlines();
            String roadtype = rs.get(flight.getLineindex()).getType();
            if (roadtype.equals("runwaycenter") || roadtype.equals("runwayycx")) {
                centerF = flight;
            }
            if ((roadtype.equals("inoutline") && flight.getNetworkname().equals("runway"))) {
                inoutline_f = flight;
            }
        }
        Flight flightDD = centerF != null ? centerF : inoutline_f;
        Flight flightAA_ = null;
        double az = 99999;
        for (int i = 0; i < flightsC.size(); i++) {
            if (flightsC.get(i).getZ() < az) {
                az = flightsC.get(i).getZ();
                flightAA_ = flightsC.get(i);
            }
        }
        Flight flightAA = flightAA_ != null ? flightAA_ : flightDD;
        return flightAA;
    }



    private Flight getAflight_ByAD(List<Flight> flightsA, List<Flight> flightsD) {
        Flight inoutline_f = null;
        Flight centerF = null;
        for (int i = 0; i < flightsD.size(); i++) {
            Flight flight = flightsD.get(i);
            List<RoadLine> rs = flight.getRoadlines();
            String roadtype = rs.get(flight.getLineindex()).getType();
            if (roadtype.equals("runwaycenter") || roadtype.equals("runwayycx")) {
                centerF = flight;
            }
            if ((roadtype.equals("inoutline") && flight.getNetworkname().equals("runway"))) {
                inoutline_f = flight;
            }
        }
        Flight flightDD = centerF != null ? centerF : inoutline_f;
        Flight flightAA_ = null;
        double az = 99999;
        for (int i = 0; i < flightsA.size(); i++) {
            if (flightsA.get(i).getZ() < az) {
                az = flightsA.get(i).getZ();
                flightAA_ = flightsA.get(i);
            }
        }
        Flight flightAA = flightAA_ != null ? flightAA_ : flightDD;
        return flightAA;
    }
    public void CD(List<Flight> flightsC, List<Flight> flightsD, String runwayname, Map<String, Flight> real) {
        Flight flightAA = getAflight_ByCD(flightsC, flightsD);
        if (flightAA != null) {
            flightAA.setWait(0);
            real.put(flightAA.getFlightId(), flightAA);
            LastRunwayOccupy lastRunwayOccupy=new LastRunwayOccupy();
            lastRunwayOccupy.flighta=flightAA.copy();
            lastRunwayOccupy.astart=flightAA.getTime();
            lastrunwaymes.put(runwayname, lastRunwayOccupy);
            for (int i = 0; i < flightsC.size(); i++) {
                Flight flight = flightsC.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                         runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                                flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
            for (int i = 0; i < flightsD.size(); i++) {
                Flight flight = flightsD.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    flight.setSpeed(0);
                    //输出待监测飞机的跑道冲突信息
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                            flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(),
                            flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }

    public void AC(List<Flight> flightsA, List<Flight> flightsC, String runwayname, Map<String, Flight> real) {
        Flight inoutline_f = null;
        Flight centerF = null;
        for (int i = 0; i < flightsC.size(); i++) {
            Flight flight = flightsC.get(i);
            List<RoadLine> rs = flight.getRoadlines();
            String roadtype = rs.get(flight.getLineindex()).getType();
            if (roadtype.equals("runwaycenter") || roadtype.equals("runwayycx")) {
                centerF = flight;
            }
            if ((roadtype.equals("inoutline") && flight.getNetworkname().equals("runway"))) {
                inoutline_f = flight;
            }
        }
        Flight flightDD = centerF != null ? centerF : inoutline_f;
        Flight flightAA_ = null;
        double az = 99999;
        for (int i = 0; i < flightsA.size(); i++) {
            if (flightsA.get(i).getZ() < az) {
                az = flightsA.get(i).getZ();
                flightAA_ = flightsA.get(i);
            }
        }
        Flight flightAA = flightAA_ != null ? flightAA_ : flightDD;
        if (flightAA != null) {
            flightAA.setWait(0);
            real.put(flightAA.getFlightId(), flightAA);
            LastRunwayOccupy lastRunwayOccupy=new LastRunwayOccupy();
            lastRunwayOccupy.flighta=flightAA.copy();
            lastRunwayOccupy.astart=flightAA.getTime();
            lastrunwaymes.put(runwayname, lastRunwayOccupy);
            for (int i = 0; i < flightsA.size(); i++) {
                Flight flight = flightsA.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                            flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
            for (int i = 0; i < flightsC.size(); i++) {
                Flight flight = flightsC.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    flight.setSpeed(0);
                    //输出待监测飞机的跑道冲突信息
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                            flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(),
                            flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }


    public void AD(List<Flight> flightsA, List<Flight> flightsD, String runwayname, Map<String, Flight> real) {
        Flight flightAA = getAflight_ByAD(flightsA, flightsD);
        if (flightAA != null) {
            flightAA.setWait(0);
            real.put(flightAA.getFlightId(), flightAA);
            LastRunwayOccupy lastRunwayOccupy=new LastRunwayOccupy();
            lastRunwayOccupy.flighta=flightAA.copy();
            lastRunwayOccupy.astart=flightAA.getTime();
            lastrunwaymes.put(runwayname, lastRunwayOccupy);
            for (int i = 0; i < flightsA.size(); i++) {
                Flight flight = flightsA.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                        runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                                flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
            for (int i = 0; i < flightsD.size(); i++) {
                Flight flight = flightsD.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    flight.setSpeed(0);
                    //输出待监测飞机的跑道冲突信息
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                            flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(),
                            flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }

    public void A(List<Flight> flightsA, String runwayname, Map<String, Flight> real) {
        Flight flightAA = null;//位置越低的飞机权重越高
        double az = 99999;
        for (int i = 0; i < flightsA.size(); i++) {
            if (flightsA.get(i).getZ() < az) {
                az = flightsA.get(i).getZ();
                flightAA = flightsA.get(i);
                flightAA.setWait(0);
            }
        }
        if (flightAA != null) {
            flightAA.setWait(0);
            real.put(flightAA.getFlightId(), flightAA);
            LastRunwayOccupy lastRunwayOccupy=new LastRunwayOccupy();
            lastRunwayOccupy.flighta=flightAA.copy();
            lastRunwayOccupy.astart=flightAA.getTime();
            lastrunwaymes.put(runwayname, lastRunwayOccupy);

            for (int i = 0; i < flightsA.size(); i++) {
                Flight flight = flightsA.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                            flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(), flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }

    public void D(List<Flight> flightsD, String runwayname, Map<String, Flight> real) {
        Flight inoutline_f = null;
        Flight centerF = null;
        for (int i = 0; i < flightsD.size(); i++) {
            Flight flight = flightsD.get(i);
            List<RoadLine> rs = flight.getRoadlines();
            String roadtype = rs.get(flight.getLineindex()).getType();
            if (roadtype.equals("runwaycenter") || roadtype.equals("runwayycx")) {
                centerF = flight;
            }
            if ((roadtype.equals("inoutline") && flight.getRoadlines().get(flight.getLineindex()).getNetworkname().equals("runway"))) {
                inoutline_f = flight;
            }
        }
        Flight flightAA = centerF != null ? centerF : inoutline_f;
        if (flightAA != null) {
            flightAA.setWait(0);
            LastRunwayOccupy lastRunwayOccupy=new LastRunwayOccupy();
            lastRunwayOccupy.flighta=flightAA.copy();
            lastRunwayOccupy.astart=flightAA.getTime();
            lastrunwaymes.put(runwayname, lastRunwayOccupy);
            for (int i = 0; i < flightsD.size(); i++) {
                Flight flight = flightsD.get(i);
                if (!flight.getFlightId().equals(flightAA.getFlightId())) {
                    flight.setWait(1);//待监测飞机等待+1
                    flight.setSpeed(0);
                    //输出待监测飞机的跑道冲突信息
                    RunawayWeit runawayWeit = runawayWeitMap.get(flightAA.getFlightId()+"_"+flight.getFlightId());
                    if(runawayWeit!=null) {
                     runawayWeit = new RunawayWeit("runwayoccupy", flightAA.getFlightId(), flightAA.getFlightNo(),
                            flightAA.getInOutFlag(), flight.getFlightId(), flight.getFlightNo(), flight.getInOutFlag(), flight.getRunway(),
                            flight.getTime());
                    }
                    runawayWeit.setEndtime(flight.getTime());
                    flight.getMessages().add(runawayWeit);
                    real.put(flight.getFlightId(), flight);
                }
            }
        }
    }

}
