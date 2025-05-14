package com.conflict;

import com.agent.Flight;
import com.extratools.Tubao.aobao;
import com.order.ConflictOrderFatherMes;
import com.order.OrderFatherMes;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: ***重点监控区域
 * @author: LiPin
 * @time: 2024-04-28 10:36
 */

public class KeyMonitoringAreas {

    Map<String, Map<String, Flight>> crossflightsbyhour = new HashMap<>();//每小时出现的飞机的数量
    Map<String, String[]> conflicenums = new HashMap<>();//每小时出现的飞机的数量
    Map<String, Integer> trailingnum = new HashMap<>();//最大排队数量;







    public   Map<String, Double>  getAve(SimpleDateFormat sdf, Map<String, Map<String, AreaMorniter>> priorflightmes, Map<String, Map<String, AreaMorniter>> secondflightmes) {

        Map<String, Double> aves = new HashMap<>();
        for (Map.Entry<String, Map<String, AreaMorniter>> entry : priorflightmes.entrySet()) {
            Map<String, AreaMorniter> morniterPjMap = entry.getValue();
            double sum = 0;
            for (Map.Entry<String, AreaMorniter> entry1 : morniterPjMap.entrySet()) {
                try {
                    long time = (sdf.parse(entry1.getValue().getEndtime()).getTime() - sdf.parse(entry1.getValue().getStarttime()).getTime()) / 1000 / 60;
                    sum = sum + time;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            aves.put(entry.getKey(), sum / morniterPjMap.size());
        }
        for (Map.Entry<String, Map<String, AreaMorniter>> entry : secondflightmes.entrySet()) {
            Map<String, AreaMorniter> morniterPjMap = entry.getValue();
            double sum = 0;
            for (Map.Entry<String, AreaMorniter> entry1 : morniterPjMap.entrySet()) {
                try {
                    long time = (sdf.parse(entry1.getValue().getEndtime()).getTime() - sdf.parse(entry1.getValue().getStarttime()).getTime()) / 1000 / 60;
                    sum = sum + time;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            aves.put(entry.getKey(), sum / morniterPjMap.size());
        }
        return aves;
    }


    private void OneHourFlights(Map<String, Flight> real) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            String key = flight.getTime().substring(0, 16);
            Map<String, Flight> flightMap = crossflightsbyhour.get(key);
            if (flightMap == null) {
                flightMap = new HashMap<>();
            }
            flightMap.put(flight.getFlightId(), flight);
            crossflightsbyhour.put(key, flightMap);
        }
    }


    void ConflictNums(Map<String, Flight> real) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            List<OrderFatherMes> orderFatherMes = flight.getMessages();
            for (int i = 0; i < orderFatherMes.size(); i++) {
                if (orderFatherMes.get(i).type.equals("trailing") ||
                        orderFatherMes.get(i).type.equals("cross") ||
                        orderFatherMes.get(i).type.equals("confrontation")) {
                    ConflictOrderFatherMes conflictMes = (ConflictOrderFatherMes) (orderFatherMes.get(i));
                    String key = conflictMes.glideflightA.flightId + "_" + conflictMes.stopflightB.flightId;
                    String[] times = conflicenums.get(key) == null ? new String[]{flight.getTime(), flight.getTime()} : conflicenums.get(key);
                    times[1] = flight.getTime();
                    conflicenums.put(key, times);
                }
            }
        }
    }

    void trailingnums(Map<String, Flight> real) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            List<OrderFatherMes> orderFatherMes = flight.getMessages();
            for (int i = 0; i < orderFatherMes.size(); i++) {
                if (orderFatherMes.get(i).type.equals("trailing") ||
                        orderFatherMes.get(i).type.equals("cross") ||
                        orderFatherMes.get(i).type.equals("confrontation")) {
                    ConflictOrderFatherMes conflictMes = (ConflictOrderFatherMes) (orderFatherMes.get(i));
                    if (conflictMes.glideflightA.flightId.equals(flight.getFlightId())) {
                        String key = conflictMes.glideflightA.flightId + "_" + conflictMes.stopflightB.flightId;
                        Integer maxtraillingnum = trailingnum.get(key);
                        if (maxtraillingnum == null) {
                            trailingnum.put(key, conflictMes.conflictquene.split(",").length);
                        } else {
                            if (maxtraillingnum < conflictMes.conflictquene.split(",").length) {
                                trailingnum.put(key, conflictMes.conflictquene.split(",").length);
                            }
                        }
//                    if(conflictMes.conflictobjs.length>=2){
//                        String p=conflictMes.conflictobjs.length + "," + flight.getFlightId() + "," + flight.getFlightNo() + "," + flight.getTime()+">>"  ;
//                       for(int k=0;k<conflictMes.conflictobjs.length;k++){
//                           p=p+","+conflictMes.conflictobjs[k];
//                       }
//                       // System.out.println(p);
//                    }
                    }
                }
            }
        }
    }


}
