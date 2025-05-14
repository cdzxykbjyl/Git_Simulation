package com.evaluate;

import com.agent.Flight;
import com.alibaba.fastjson.JSONObject;
import com.evaluate.HeatMap;
import com.evaluate.OrderofEvaluate;
import com.order.ConflictOrderFatherMes;
import com.order.OrderFatherMes;
import com.order.RunawayWeit;
import org.locationtech.jts.geom.Coordinate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-04-28 11:31
 */

public class EvalueMes {
    BlockingQueue<String> heaps = new LinkedBlockingQueue<>();//热力图信息
    BlockingQueue<String> evaluemap = new LinkedBlockingQueue<>();//评价信息
    HeatMap heatMap;
    int score = 99999;
    SimpleDateFormat sdf ;
    public Map<String, OrderofEvaluate> evaluateMap = new HashMap<>();
    public EvalueMes( SimpleDateFormat sdf,Coordinate  botomleft,Coordinate upright,double stepx,double stepy){
        this. heatMap = new HeatMap(botomleft, upright, stepx, stepy);//初始化热力图网格
        this.sdf=sdf;

    }



    public Map<String, OrderofEvaluate> getEvaluateMap() {
        return evaluateMap;
    }

    public void setEvaluateMap(Map<String, OrderofEvaluate> evaluateMap) {
        this.evaluateMap = evaluateMap;
    }

    public void initEvaluateMap(Map<Long, List<Flight>> plan) {
        for (Map.Entry<Long, List<Flight>> entry : plan.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                Flight flight = entry.getValue().get(i);
                OrderofEvaluate orderofEvaluate = new OrderofEvaluate();
                orderofEvaluate.setStarttime(flight.getTime());
                orderofEvaluate.setEndtime(flight.getTime());
                evaluateMap.put(flight.getFlightId(), orderofEvaluate);
            }
        }
    }

    public int getScore() {
        int score = 0;//时间总和   仿真时间综合，所有航班运行的时间总和
        for (Map.Entry<String, OrderofEvaluate> entry : evaluateMap.entrySet()) {
            OrderofEvaluate orderofEvaluate = entry.getValue();
            String[] flightduretime = orderofEvaluate.getDuretime().split(":");
            int flightduretimemiao = 0;
            if (flightduretime.length == 2) {
                flightduretimemiao = Integer.parseInt(flightduretime[0]) * 60 + Integer.parseInt(flightduretime[1]);
            } else if (flightduretime.length == 3) {
                flightduretimemiao = Integer.parseInt(flightduretime[0]) * 3600 + Integer.parseInt(flightduretime[1]) * 60 + Integer.parseInt(flightduretime[2]);
            }
            score = score + flightduretimemiao;
        }
        return score;
    }

    public void update(){
        score = getScore();//获取评分
        //评价热力图输出
        for (int i = 0; i < heatMap.heatmap.length; i++) {
            String str = "";
            for (int jk = 0; jk < heatMap.heatmap[0].length; jk++) {
                str = str + heatMap.heatmap[i][jk] + "\t";
            }
            try {
                heaps.put(str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        //最终评价结果输出
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, OrderofEvaluate> entry : evaluateMap.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue().getMes());
        }
        try {
            evaluemap.put(jsonObject.toJSONString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void updateEvaluateMap(Map<String, Flight> real) {
        Iterator iter = real.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter.next();
            Flight flight = (Flight) Flightmap.getValue();
            OrderofEvaluate orderofEvaluate = evaluateMap.get(flight.getFlightId());
            if (flight.getMessages().size() != 0) {
                updateOrderofEvaluateMes(flight.getMessages(), orderofEvaluate);
                flight.setMessages(new ArrayList<OrderFatherMes>());
            }
            orderofEvaluate.setEndtime(flight.getTime());
            evaluateMap.put(flight.getFlightId(), orderofEvaluate);
        }
    }
    public void updateOrderofEvaluateMes(List<OrderFatherMes> nowlist, OrderofEvaluate
            orderofEvaluate) {
        List<OrderFatherMes> lastconflictmessages = orderofEvaluate.getConflictmessages();
        for (int i = 0; i < nowlist.size(); i++) {
            OrderFatherMes now = nowlist.get(i);
            boolean isin = false;
            switch (now.type) {
                case "conflict":
                    ConflictOrderFatherMes nowmes = (ConflictOrderFatherMes) now;
                    for (int j = lastconflictmessages.size() - 1; j > -1; j--) {
                        OrderFatherMes last = lastconflictmessages.get(j);
                        if (last.type.equals("conflict")) {
                            ConflictOrderFatherMes lastmes = (ConflictOrderFatherMes) last;
                            if (lastmes.glideflightA.getFlightId().equals(nowmes.glideflightA.getFlightId()) && lastmes.stopflightB.getFlightId().equals(nowmes.stopflightB.getFlightId())) {

                                try {
                                    long time = (sdf.parse(nowmes.getEndtime()).getTime() - sdf.parse(lastmes.getEndtime()).getTime()) / 1000;
                                    if (time <= 1) {
                                        isin = true;
                                        lastconflictmessages.set(j, nowlist.get(i));
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (isin) {
                        orderofEvaluate.setConflictmessages(lastconflictmessages);
                    } else {
                        orderofEvaluate.getConflictmessages().add(nowlist.get(i));
                    }
                    break;
                case "runwayoccupy":
                    RunawayWeit runwaywait = (RunawayWeit) now;
                    for (int j = lastconflictmessages.size() - 1; j > -1; j--) {
                        OrderFatherMes last = lastconflictmessages.get(j);
                        if (last.type.equals("runwayoccupy")) {
                            RunawayWeit lastmes = (RunawayWeit) last;
                            if (lastmes.getGlideflightId().equals(runwaywait.getGlideflightId()) && lastmes.getStopflightId().equals(runwaywait.getStopflightId())) {
                                try {
                                    long time = (sdf.parse(runwaywait.getTime()).getTime() - sdf.parse(lastmes.getTime()).getTime()) / 1000;
                                    if (time <= 1) {
                                        isin = true;
                                        lastconflictmessages.set(j, nowlist.get(i));
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (isin) {
                        orderofEvaluate.setConflictmessages(lastconflictmessages);
                    } else {
                        orderofEvaluate.getConflictmessages().add(nowlist.get(i));
                    }
                    break;
                default:
                    orderofEvaluate.getOrdermessages().add(nowlist.get(i));
                    break;
            }
        }
    }

}
