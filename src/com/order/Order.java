package com.order;


import base.AngDisUtil;
import base.BlhToGauss;
import base.Time;
import com.alibaba.fastjson.JSONObject;
import com.agent.Flight;
import com.agent.Aircraft;

import com.conflict.HisTimeNodes;
import com.conflict.RunwayConflictByQuene;
import com.conflict.RunwayFlightHis;
import com.conflict.TaxiwayConflict;
import com.position.Move;
import fun.NodeFather;
import fun.RunwayModel;
import fun.RunwayNodes;
import fun.StandNodes;
import org.locationtech.jts.geom.Coordinate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Order {
    SimpleDateFormat sdf;
    public Map<String, AbuttedGallery> abuttedGalleries = new HashMap<>();

    int retreatBridgeBefore;
    Map<String, Aircraft> aircraftMap = new HashMap<>();
    public Map<String, RunwayModel> runwayMes = new HashMap<>();
    public Map<String, StandNodes> standMes = new HashMap<>();

    public Map<String, TaxiWait> taxiwaitmaps = new HashMap<>();

    public Order(SimpleDateFormat sdf, int retreatBridgeBefore, Map<String, RunwayModel> runwayMes,
                 Map<String, StandNodes> standMes, Map<String, Aircraft> aircraftMap) {
        this.sdf = sdf;
        this.retreatBridgeBefore = retreatBridgeBefore;
        this.runwayMes = runwayMes;
        this.standMes = standMes;
        this.aircraftMap = aircraftMap;
    }


    public void monitorRemoveBridge(Map<Long, List<Flight>> plan, long realtime) {
        //撤侨检测
        String nowtimestr = sdf.format(realtime * 1000);
        for (int j = 0; j < retreatBridgeBefore; j++) {
            List<Flight> cheqiaoflights = plan.get(realtime + j);
            if (cheqiaoflights != null) {
                for (int i = 0; i < cheqiaoflights.size(); i++) {
                    Flight flight = cheqiaoflights.get(i);
                    if (flight.getInOutFlag().equals("D") && standMes.get(flight.getStand()).isHavebridge()) {
                        AbuttedGallery abuttedGallery = abuttedGalleries.get(flight.getFlightId()) == null ?
                                new AbuttedGallery("removebridge", flight.getFlightId(), flight.getFlightNo(), flight.getStand(), nowtimestr, flight.getAcft()) :
                                abuttedGalleries.get(flight.getFlightId());
                        abuttedGallery.setEndtime(nowtimestr);
                        abuttedGalleries.put(flight.getFlightId(), abuttedGallery);
                    }
                }
            }
        }
        Iterator iter = abuttedGalleries.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, AbuttedGallery> e = (Map.Entry) iter.next();
            try {
                long histime = sdf.parse(e.getValue().getEndtime()).getTime() / 1000;
                if (realtime - histime > retreatBridgeBefore) {
                    iter.remove();
                }
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
    }


    public void updateOfflineConflictMes(Map<String, Flight> real, TaxiwayConflict fconflict) {
        Iterator iter = real.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter.next();
            Flight flight = (Flight) Flightmap.getValue();
            if (flight.getOnline().equals("N")) {
                Map<String, List<ConflictOrderFatherMes>> a = parseMapForFilter2(fconflict.conflictMaps, flight.getFlightId());
                List<String> keys = new ArrayList<>();
                for (Map.Entry<String, List<ConflictOrderFatherMes>> entry : a.entrySet()) {
                    List<ConflictOrderFatherMes> conflictmes = entry.getValue();
                    int size = conflictmes.size();
                    for (int i = 0; i < size; i++) {
                        if (!conflictmes.get(i).isBisin()) {
                            conflictmes.remove(i);
                            size--;
                            i--;
                        }
                    }
                    if (size == 0) {
                        keys.add(entry.getKey());
                    }
                }
                for (int i = 0; i < keys.size(); i++) {
                    fconflict.conflictMaps.remove(keys.get(i));
                }
            }
        }
    }

    public Map<String, List<ConflictOrderFatherMes>> parseMapForFilter2
            (Map<String, List<ConflictOrderFatherMes>> map, String filters) {
        if (map == null) {
            return null;
        } else {
            map = map.entrySet().stream()
                    .filter((e) -> checkKey(e.getKey(), filters))
                    .collect(Collectors.toMap(
                            (e) -> (String) e.getKey(),
                            (e) -> e.getValue()
                    ));
        }
        return map;
    }

    /**
     * 通过indexof匹配想要查询的字符
     */
    private static boolean checkKey(String key, String filters) {
        if (key.indexOf(filters) > -1) {
            return true;
        } else {
            return false;
        }
    }

    public void updateHistNodeMapAndDeleteOffline(Map<String, Flight> real, Map<String, HisTimeNodes> hisTimeNodesHashMap, Map<String, List<RunwayFlightHis>> hisdirectrunwaymap, TaxiwayConflict fconflict, Map<String, List<ConflictOrderFatherMes>> conflictMaps) {

        Iterator iter = real.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter.next();
            Flight flight = (Flight) Flightmap.getValue();
            HisTimeNodes hisTimeNodes = hisTimeNodesHashMap.get(flight.getFlightId()) == null ? new HisTimeNodes().HisTimeNodesByReal(flight) : hisTimeNodesHashMap.get(flight.getFlightId());
            if (hisTimeNodes.getRealstarttime().equals("")) {
                hisTimeNodes.setRealstarttime(flight.getTime());
                hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
            }
            if (flight.getInOutFlag().equals("A")) {
                if (hisTimeNodes.getDowntime().equals("") && flight.getZ() == 0) {//落地时间
                    hisTimeNodes.setDowntime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }

                if (flight.getOnline().equals("N")) {
                    hisTimeNodes.setInofflinetime(flight.getTime());
                    hisTimeNodes.setEndinstandtime(flight.getTime());
                    hisTimeNodes.setEndtime(flight.getTime());
                    if (!hisTimeNodes.getStopoccupyrunwaytime().equals("")) {
                        hisTimeNodes.setTaxiduretime(Time.getTimeDuration(flight.getTime(), hisTimeNodes.getStopoccupyrunwaytime()));
                    }
                    if (!hisTimeNodes.getRealstarttime().equals("")) {
                        hisTimeNodes.setTaxiduretime(Time.getTimeDuration(flight.getTime(), hisTimeNodes.getRealstarttime()));
                    }
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                if (flight.getNetworkname().equals("apron") && hisTimeNodes.getStartinstandtime().equals("")) {
                    hisTimeNodes.setStartinstandtime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }

                if (flight.getNetworkname().equals("taxiway") && hisTimeNodes.getStopoccupyrunwaytime().equals("")) {
                    hisTimeNodes.setStopoccupyrunwaytime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                boolean is = isin(hisdirectrunwaymap, flight);
                if (hisTimeNodes.getStartoccupyrunwaytime().equals("") && is) {
                    hisTimeNodes.setStartoccupyrunwaytime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
            } else {
                boolean is = isin(hisdirectrunwaymap, flight);
                if (hisTimeNodes.getStartoccupyrunwaytime().equals("") && is) {
                    hisTimeNodes.setStartoccupyrunwaytime(flight.getTime());
                    if (!hisTimeNodes.getRealstarttime().equals("")) {
                        hisTimeNodes.setTaxiduretime(Time.getTimeDuration(flight.getTime(), hisTimeNodes.getRealstarttime()));
                    }
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                if (hisTimeNodes.getStopoccupyrunwaytime().equals("") && (!is) & flight.getZ() > 0) {
                    hisTimeNodes.setStopoccupyrunwaytime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                if (hisTimeNodes.getFlytime().equals("") && flight.getZ() > 0) {
                    hisTimeNodes.setFlytime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                if (hisTimeNodes.getStartoutstandtime().equals("") && flight.getNetworkname().equals("apron")) {
                    hisTimeNodes.setStartoutstandtime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                if (hisTimeNodes.getEndoutstandtime().equals("") && flight.getNetworkname().equals("taxiway")) {
                    hisTimeNodes.setEndoutstandtime(flight.getTime());
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
                if (flight.getOnline().equals("N")) {
                    hisTimeNodes.setOutofflinetime(flight.getTime());
                    if (hisTimeNodes.getStopoccupyrunwaytime().equals("")) {
                        hisTimeNodes.setStopoccupyrunwaytime(flight.getTime());
                    }
                    hisTimeNodes.setEndtime(flight.getTime());
                    if (!hisTimeNodes.getRealstarttime().equals("")) {
                        hisTimeNodes.setAllduretime(Time.getTimeDuration(flight.getTime(), hisTimeNodes.getRealstarttime()));
                    }
                    hisTimeNodesHashMap.put(flight.getFlightId(), hisTimeNodes);
                }
            }
        }


        Iterator iter2 = real.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter2.next();
            Flight flight = (Flight) Flightmap.getValue();
            if (flight.getOnline().equals("N")) {
                System.out.println(flight.getFlightId());
                List<String> keys = new ArrayList<>();
                for (Map.Entry<String, List<ConflictOrderFatherMes>> entry : fconflict.conflictMaps.entrySet()) {
                    String[] strs = entry.getKey().split("_");
                    if (flight.getFlightId().equals(strs[0]) || flight.getFlightId().equals(strs[1])) {
                        List<ConflictOrderFatherMes> conflictmes = entry.getValue();
                        for (int i = 0; i < conflictmes.size(); i++) {
                            if (!conflictmes.get(i).isBisin()) {
                                conflictmes.remove(i);
                                i--;
                            }
                        }
                        if (conflictmes.size() != 0) {
                            keys.add(entry.getKey());
                        }
                    }
                }
                for (int i = 0; i < keys.size(); i++) {
                    conflictMaps.put(keys.get(i), fconflict.conflictMaps.get(keys.get(i)));
                    fconflict.conflictMaps.remove(keys.get(i));
                }
                iter2.remove();
            }
        }


    }

    boolean isin(Map<String, List<RunwayFlightHis>> hisdirectrunwaymap, Flight flight) {
        boolean isin = false;
        List<RunwayFlightHis> hiss = hisdirectrunwaymap.get(flight.getRunway()) == null ? new ArrayList<>() : hisdirectrunwaymap.get(flight.getRunway());
        for (int i = 0; i < hiss.size(); i++) {
            if (hiss.get(i).getFlightId().equals(flight.getFlightId())) {
                isin = true;
            }
        }

        return isin;
    }


    public Map<String, List<Flight>> initStand(Map<Long, List<Flight>> real) {
        Map<String, List<Flight>> stands = new HashMap<>();
        for (Map.Entry<Long, List<Flight>> entry : real.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                Flight flight = entry.getValue().get(i);
                List<Flight> flights = new ArrayList<>();
                if (stands.get(flight.getStand()) != null) {
                    flights = stands.get(flight.getStand());
                    try {
                        long lasttime = sdf.parse(flights.get(0).getTime()).getTime();
                        long nowtime = sdf.parse(flight.getTime()).getTime();
                        if (nowtime <= lasttime) {
                            flights.add(0, flight);
                        }
                        long endtime = sdf.parse(flights.get(flights.size() - 1).getTime()).getTime();
                        if (nowtime >= endtime) {
                            flights.add(flight);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    flights.add(flight);
                }
                stands.put(flight.getStand(), flights);
            }
        }
        return stands;
    }

    public Map<String, Flight> initneedsend(long smalltime, Map<String, List<Flight>> stands) {
        Map<String, Flight> needDsend = new HashMap<>();
        for (Map.Entry<String, List<Flight>> entry : stands.entrySet()) {
            if (entry.getValue().get(0).getInOutFlag().equals("D")) {
                Flight flight1 = entry.getValue().get(0);
                flight1.setTime(sdf.format(smalltime));
                needDsend.put(flight1.getFlightId(), flight1);
            }
        }
        return needDsend;
    }


    public void UpdateNeedSend(Map<String, Flight> real, Map<String, Flight> needDsend, Map<String, List<Flight>> stands) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight0 = entry.getValue();
            if (flight0.getOnline().equals("N")) {
                List<Flight> flights = stands.get(flight0.getStand());
                boolean isget = false;
                for (int i = 0; i < flights.size(); i++) {
                    Flight flight = flights.get(i);
                    try {
                        if (!isget && sdf.parse(flight.getTime()).getTime() > sdf.parse(flight0.getTime()).getTime()) {
                            isget = true;
                            if (flight.getInOutFlag().equals("D")) {
                                needDsend.put(flight.getFlightId(), flight);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public List<String> getPositionMes(Map<String, Flight> real, Map<String, Flight> needDsend) {
        List<String> returnstr = new ArrayList<>();
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            returnstr.add(flight.getMes().toString());
        }
        Iterator iter = needDsend.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter.next();
            Flight flight = (Flight) Flightmap.getValue();
            JSONObject mes = flight.getMes();
            mes.put("online", "Y");
            mes.put("direction", flight.getDirection());
            returnstr.add(mes.toString());
            returnstr.add(mes.toString());
            iter.remove();
        }
        return returnstr;
    }

    public List<Flight> getNewFlights(Map<Long, List<Flight>> plan, long realtime) {
        List<Flight> flights = plan.get(realtime);
        List<Flight> newflights = new ArrayList<>();
        if (flights != null) {
            newflights.addAll(flights);
            plan.remove(realtime);
        }
        return newflights;
    }

    public List<Flight> getNewflightsandUpdatePlan(RunwayConflictByQuene runwayConflictByQuene, Map<Long, List<Flight>> plan, Map<String, Flight> real, long smalltime, Map<String, HisTimeNodes> hisTimeNodesHashMap, JSONObject limitnum) {
        //每个跑道的统计量进行初始化

        Map<String, Integer> realrunwaymnum = new HashMap<>();
        for (Object o : limitnum.keySet().toArray()) {
            realrunwaymnum.put(o.toString(), 0);
        }
        //当前出港飞机的量的统计
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            if (entry.getValue().getInOutFlag().equals("D")) {
                realrunwaymnum.put(entry.getValue().getRunway(), Integer.parseInt(realrunwaymnum.get(entry.getValue().getRunway()).toString()) + 1);
            }
        }

        List<Flight> flightsk = plan.get(smalltime) == null ? new ArrayList<>() : plan.get(smalltime);

        if (flightsk.size() != 0) {
            plan.remove(smalltime);
        }
        //获取到新的待加入队列的航班

        Map<String, List<Flight>> newqueryA = new HashMap<>();
        Map<String, List<Flight>> newqueryD = new HashMap<>();
        //新的即将出港的飞机历史节点保存
        //当前即将进港的飞机中出港和进港的量
        for (int i = 0; i < flightsk.size(); i++) {
            Flight flight = flightsk.get(i).copy();
            HisTimeNodes hisTimeNodes = hisTimeNodesHashMap.get(flight.getFlightId());
            if (hisTimeNodes == null) {
                hisTimeNodesHashMap.put(flight.getFlightId(), new HisTimeNodes().HisTimeNodesByReal(flight));
            } else {
                flight.setTime(hisTimeNodes.getStarttime());
            }
            if (flight.getInOutFlag().equals("A")) {
                List<Flight> flights1 = newqueryA.get(flight.getRunway()) == null ? new ArrayList<>() : newqueryA.get(flight.getRunway());
                flights1.add(flight);
                newqueryA.put(flight.getRunway(), flights1);
            } else {
                List<Flight> flights1 = newqueryD.get(flight.getRunway()) == null ? new ArrayList<>() : newqueryD.get(flight.getRunway());
                flights1.add(flight);
                newqueryD.put(flight.getRunway(), flights1);
            }
        }


        Iterator var3 = newqueryD.entrySet().iterator();
        while (var3.hasNext()) {
            Map.Entry<String, List<Flight>> entry = (Map.Entry) var3.next();
            String runway = entry.getKey();
            List<Flight> flights1 = bubbleSortsmalltobig(entry.getValue());
            int realnum = realrunwaymnum.get(runway);
            int newnum = flights1.size();
            int limitnusm = limitnum.getInteger(runway);
            boolean isbigthan = (realnum + newnum) > limitnusm;
            if (isbigthan) {
                int neeadd = limitnusm - realnum;
                if (neeadd>=0){
                    for (int i = neeadd; i < newnum; i++) {
                        List<Flight> l = plan.get(smalltime + 1) == null ? new ArrayList<>() : plan.get(smalltime + 1);
                        Flight n = flights1.get(i);
                        n.setTime(sdf.format((smalltime + 1) * 1000));
                        l.add(n);
                        plan.put(smalltime + 1, l);
                        flights1.remove(i);
                        newnum--;
                        i--;
                    }
                }else{
                    flights1=new ArrayList<>();
                }
                if (flights1.size() != 0) {
                    newqueryD.put(runway, flights1);
                } else {
                    var3.remove();
                }
            }
        }
        Map<String, List<RunwayFlightHis>> hisdirectrunwaymap = runwayConflictByQuene.hisdirectrunwaymap;
        Iterator var4 = newqueryA.entrySet().iterator();
        while (var4.hasNext()) {
            Map.Entry<String, List<Flight>> entry = (Map.Entry) var4.next();
            String runway = entry.getKey();
            List<Flight> flights1 = bubbleSortsmalltobig(entry.getValue());
            List<RunwayFlightHis> his = hisdirectrunwaymap.get(runway);
            int start = 0;
            if (his == null) {
                start = 1;
            } else {
                boolean isgeta = false;
                for (int i = 0; i < his.size(); i++) {
                    if (!isgeta && his.get(i).getInOutFlag().equals("A")) {
                        isgeta = true;
                    }
                }
                start = isgeta ? 0 : 1;
            }
            int numsize = flights1.size();
            for (int i = start; i < numsize; i++) {
                List<Flight> l = plan.get(smalltime + 1) == null ? new ArrayList<>() : plan.get(smalltime + 1);
                Flight n = flights1.get(i);
                n.setTime(sdf.format((smalltime + 1) * 1000));
                l.add(n);
                plan.put(smalltime + 1, l);
                flights1.remove(i);
                numsize--;
                i--;
            }
            if (flights1.size() != 0) {
                newqueryA.put(runway, flights1);
            } else {
                var4.remove();
            }
        }
        List<Flight> newflights = getnewflightA(newqueryA, runwayConflictByQuene, real, plan, smalltime);
        for (Map.Entry<String, List<Flight>> entry : newqueryD.entrySet()) {
            List<Flight> lsd = entry.getValue();
            for (int i = 0; i < lsd.size(); i++) {
                newflights.add(lsd.get(i));
                real.put(lsd.get(i).getFlightId(), lsd.get(i));
            }
        }
        return newflights;
    }

    public List<Flight> getnewflightA(Map<String, List<Flight>> newqueryA, RunwayConflictByQuene runwayConflictByQuene, Map<String, Flight> real, Map<Long, List<Flight>> plan, long smalltime) {
        List<Flight> newflights = new ArrayList<>();
        for (Map.Entry<String, List<Flight>> entry : newqueryA.entrySet()) {
            newflights.addAll(entry.getValue());
        }
        for (int i = 0; i < newflights.size(); i++) {
            real.put(newflights.get(i).getFlightId(), newflights.get(i));
        }
        Map<String, List<RunwayFlightHis>> directrunwaymapA = new HashMap<>();
        for (Map.Entry<String, List<Flight>> entry : newqueryA.entrySet()) {
            List<Flight> flights = entry.getValue();
            List<RunwayFlightHis> runwayFlightHiss = new ArrayList<>();
            for (int i = 0; i < flights.size(); i++) {
                RunwayFlightHis runwayFlightHis = runwayConflictByQuene.convertFlighttoRunwayFlightHis(flights.get(i));
                runwayFlightHiss.add(runwayFlightHis);
            }
            directrunwaymapA.put(entry.getKey(), runwayFlightHiss);
        }
        runwayConflictByQuene.updateDirectRunwayMapAndNeedWait(real, directrunwaymapA);
        Iterator iter = newflights.iterator();
        while (iter.hasNext()) {
            Flight flight = (Flight) iter.next();

            if (real.get(flight.getFlightId()).getWait() != 0) {
                iter.remove();
                real.remove(flight.getFlightId());
                List<Flight> l = plan.get(smalltime + 1) == null ? new ArrayList<>() : plan.get(smalltime + 1);
                Flight n = flight.copy();
                n.setTime(sdf.format((smalltime + 1) * 1000));
                l.add(n);
                plan.put(smalltime + 1, l);
            }
        }
        return newflights;
    }


    public List<Flight> bubbleSortsmalltobig(List<Flight> flights) {
        int n = flights.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                Flight flighti = flights.get(j);
                Flight flighti1 = flights.get(j + 1);
                try {
                    long fitime = sdf.parse(flighti.getTime()).getTime();
                    long fi1time = sdf.parse(flighti1.getTime()).getTime();
                    if (fitime > fi1time) {
                        Flight temp = flighti;
                        flights.set(j, flighti1);
                        flights.set(j + 1, temp);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        }
        return flights;
    }

    public void bubbleSortbigtosmall(List<Flight> flights) {
        int n = flights.size();

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                Flight flighti = flights.get(j);
                Flight flighti1 = flights.get(j + 1);
                try {
                    long fitime = sdf.parse(flighti.getTime()).getTime();
                    long fi1time = sdf.parse(flighti1.getTime()).getTime();
                    if (fitime > fi1time) {
                        Flight temp = flighti;
                        flights.set(j, flighti1);
                        flights.set(j + 1, temp);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateReal(List<Flight> newflights, Map<String, Flight> real) {
        for (int i = 0; i < newflights.size(); i++) {
            real.put(newflights.get(i).getFlightId(), newflights.get(i));
        }
    }

    public void morniterInOutStandAndStandStation(Map<String, Flight> real) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            String nowname = flight.getRoadlines().get(flight.getLineindex()).name;
            String nowtype = flight.getRoadlines().get(flight.getLineindex()).getNetworkname();
            boolean insin = false;
            if (nowtype.equals("apron")) {
                String[] nownames = nowname.split("_");
                for (int i = 0; i < nownames.length; i++) {
                    if (nownames[i].equals(flight.getStand())) {
                        insin = true;
                    }
                }
            }
            if (insin && flight.getOnstand().equals("N")) {
                InoutStand inoutStand = new InoutStand("instand", flight.getFlightId(), flight.getFlightNo(), flight.getStand(), flight.getTime(), flight.getX(), flight.getY(), flight.getZ());
                flight.getMessages().add(inoutStand);
                StandOccupyOrderFatherMes standOccupyMes = new StandOccupyOrderFatherMes("solidstand", flight.getStand(), flight.getTime(), flight.getFlightId(), flight.getFlightNo(), flight.getRunway(), flight.getInOutFlag());
                flight.getMessages().add(standOccupyMes);
                flight.setOnstand("Y");
            } else if (!insin && flight.getOnstand().equals("Y")) {
                InoutStand inoutStand = new InoutStand("outstand", flight.getFlightId(), flight.getFlightNo(), flight.getStand(), flight.getTime(), flight.getX(), flight.getY(), flight.getZ());
                flight.getMessages().add(inoutStand);
                StandOccupyOrderFatherMes standOccupyMes = new StandOccupyOrderFatherMes("dashstand", flight.getStand(), flight.getTime(), flight.getFlightId(), flight.getFlightNo(), flight.getRunway(), flight.getInOutFlag());
                flight.getMessages().add(standOccupyMes);
                flight.setOnstand("N");
            }
        }
    }

    public void updateTime(Map<String, Flight> real, long smalltime) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            entry.getValue().setTime(sdf.format(smalltime));
        }
    }


    private NodeFather getNearstandWeitNode(List<NodeFather> nodeFathers, Coordinate location) {
        NodeFather nearstnode = new NodeFather();
        double todis = 9999;
        for (int i = 0; i < nodeFathers.size(); i++) {
            double nextdistance = new AngDisUtil().getDistace(nodeFathers.get(i).getCoordinate(), location);
            if (nextdistance < todis) {
                todis = nextdistance;
                nearstnode = nodeFathers.get(i);
            }
        }
        return nearstnode;
    }

    public boolean isnear(Flight flight, Coordinate coordinate, int T) {
        Move move = new Move();
        double dis = new BlhToGauss().getBLHDistanceByHaverSine(new Coordinate(flight.getX(), flight.getY()), coordinate);
        if (flight.getInOutFlag().equals("A")) {
            String ac = String.valueOf(aircraftMap.get(flight.getAcft()).getAdij().get(flight.getNetworkname()));
            move.go(flight.getSpeed(), Double.valueOf(ac), flight.getRoadlines().get(flight.getLineindex()).getLimitspeedA(), T);
        } else {
            String ac = String.valueOf(aircraftMap.get(flight.getAcft()).getDdij().get(flight.getNetworkname()));
            move.go(flight.getSpeed(), Double.valueOf(ac), flight.getRoadlines().get(flight.getLineindex()).getLimitspeedD(), T);
        }
        if (dis < (move.vs - flight.getSegyu())) {
            return true;
        } else {
            return false;
        }
    }


    public void getTaxiWeit(Map<String, Flight> real, int T) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            NodeFather nearstnode = getNearstandWeitNode(flight.getBestnodes(), new Coordinate(flight.getX(), flight.getY()));
            boolean isnear = isnear(flight, nearstnode.getCoordinate(), T);
            int waittime = nearstnode.getWait();
            if (waittime != 0 && isnear) {
                TaxiWait taxwait = taxiwaitmaps.get(nearstnode.getType() + "_" + nearstnode.getName() + "_" + flight.getFlightId());
                switch (nearstnode.getType()) {
                    case "zbd":
                        if (taxwait == null) {
                            taxwait = new TaxiWait("zbdwait", nearstnode.getName(), flight.getTime(),
                                    flight.getFlightId(), flight.getFlightNo(), flight.getX(), flight.getY(), flight.getZ());
                        }
                        taxwait.setEndtime(flight.getTime());
                        //是否进行路线的重新规划，当启动实时重新规划时，进行路径的重新规划，和冲突的检测更新
                        break;
                    case "taxi":
                        if (taxwait == null) {
                            taxwait = new TaxiWait("taxiwait", nearstnode.getName(), flight.getTime(), flight.getFlightId(), flight.getFlightNo(), flight.getX(), flight.getY(), flight.getZ());
                        }
                        taxwait.setEndtime(flight.getTime());
                        break;
                    case "pushout":
                        if (taxwait == null) {
                            taxwait = new TaxiWait("pushoutwait", nearstnode.getName(), flight.getTime(), flight.getFlightId(), flight.getFlightNo(), flight.getX(), flight.getY(), flight.getZ());
                        }
                        taxwait.setEndtime(flight.getTime());
                        break;
                    case "ect":
                        if (taxwait == null) {
                            taxwait = new TaxiWait("ectwait", nearstnode.getName(), flight.getTime(), flight.getFlightId(), flight.getFlightNo(), flight.getX(), flight.getY(), flight.getZ());
                        }
                        taxwait.setEndtime(flight.getTime());
                        break;
                    case "in":
                    default:
                        break;
                }
                if (taxwait != null) {

                    flight.setWait(1);
                    flight.getMessages().add(taxwait);
                    waittime = waittime - 1;
                    nearstnode.setWait(waittime);
                    flight.getBestnodes().get(nearstnode.getIndex()).setWait(waittime);
                    taxiwaitmaps.put(nearstnode.getType() + "_" + nearstnode.getName() + "_" + flight.getFlightId(), taxwait);
                }
            }
        }
    }

    public void updateOutline(Map<String, Flight> real) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            if (flight.getOnline().equals("N")) {
                if (flight.getInOutFlag().equals("A")) {
                    InoutStand inoutStand = new InoutStand("instand", flight.getFlightId(), flight.getFlightNo(), flight.getStand(), flight.getTime(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(inoutStand);
                    //靠桥
                    if (standMes.get(flight.getStand()).isHavebridge()) {
                        AbuttedGallery abuttedGallery = new AbuttedGallery("buttbridge", flight.getFlightId(), flight.getFlightNo(), flight.getStand(), flight.getTime(), flight.getAcft());
                        flight.getMessages().add(abuttedGallery);
                    }
                }
                Offline offline = new Offline("offline", flight.getFlightId(), flight.getInOutFlag(), flight.getFlightNo(), flight.getStand(), flight.getRunway(), flight.getTime(), flight.getX(), flight.getY(), flight.getZ());
                flight.getMessages().add(offline);
            }
        }
    }


    public NodeFather getPointByRunway(String name, Map<String, RunwayModel> runwayMes) {
        for (Map.Entry<String, RunwayModel> entry : runwayMes.entrySet()) {
            for (Map.Entry<String, List<RunwayNodes>> entry3 : entry.getValue().allsides.entrySet()) {
                List<RunwayNodes> codes = entry3.getValue();
                for (int i = 0; i < codes.size(); i++) {
                    if (name.equals(codes.get(i).getName())) {
                        return codes.get(i);
                    }
                }
            }
        }
        try {
            throw new Exception("矢量点数据文件中没有该点信息");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
