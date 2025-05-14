package com.position;


import base.AngDisUtil;
import base.BlhToGauss;
import com.agent.Flight;
import com.agent.Aircraft;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.order.LandingGear;
import com.order.OffandLand;
import com.runwayrule.RunwayTakeoffandDown;
import fun.FlyAndSpeedNodes;
import fun.NodeFather;
import fun.RoadLine;
import fun.StandNodes;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;

/**
 * @description: //更新位置
 * @author: LiPin
 * @time: 2022-10-31 20:16
 */
public class UpPosition {

    public Map<String, Aircraft> aircraftMap = new HashMap<>();//飞机信息
    public Map<String, StandNodes> standMes = new HashMap<>();
    public Map<String, RunwayTakeoffandDown> flyandspeednodesmap = new HashMap<>();
    double[] gearaltAD;

    public UpPosition(Map<String, Aircraft> aircraftMap, Map<String, StandNodes> standMes, Map<String, RunwayTakeoffandDown> flyandspeednodesmap, double[] gearaltAD) {
        this.aircraftMap = aircraftMap;
        this.standMes = standMes;
        this.flyandspeednodesmap = flyandspeednodesmap;
        this.gearaltAD = gearaltAD;
    }

    public void updatePosition(Map<String, Flight> real, String nexttime, int T) {
        Iterator iter = real.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Flight flight = (Flight) entry.getValue();
            flight.setTime(nexttime);
            if (flight.getWait() > 0) {
                flight.setWait(flight.getWait() - 1);
            } else {
                update(flight, T);
            }
        }
    }


    public boolean isJsonString(String str) {
        try {
            JSONObject.parseObject(str);
        } catch (Exception ex) {
            // 尝试解析为JSONArray
            try {
                JSONArray.parseArray(str);
            } catch (Exception ex2) {
                return false; // 不是有效的JSON
            }
        }
        return true; // 是有效的JSON
    }

    public double getLimitspeedByNext(RoadLine roadLine, List<FlyAndSpeedNodes> flyAndSpeedNodes, String runway, String inoutflag) {
        if (roadLine.getType().equals("runwaycenter") || roadLine.getType().equals("runwayycx")) {
            return flyAndSpeedNodes.get(0).getSpeed();
        } else {
            if (inoutflag.equals("A")) {
                return roadLine.getLimitspeedA();
            } else {
                return roadLine.getLimitspeedD();
            }
        }
    }

    public double[] getLimitspeedBySeg(RoadLine roadLine, String runway, String acft,double flightspeed, List<FlyAndSpeedNodes> flyAndSpeedNodes, Coordinate lo, String inoutflag) {
        double[] altorspeed = new double[]{0, 0,0};
        if (roadLine.getType().equals("runwaycenter") || roadLine.getType().equals("runwayycx")) {
            AngDisUtil angDisUtil = new AngDisUtil();
            BlhToGauss blhToGauss = new BlhToGauss();
            double dis = 9999;
            int index = 0;
            Coordinate foot = null;
            for (int i = 0; i < flyAndSpeedNodes.size() - 1; i++) {
                FlyAndSpeedNodes nodesi = flyAndSpeedNodes.get(i);
                FlyAndSpeedNodes nodesi1 = flyAndSpeedNodes.get(i + 1);
                Coordinate f = angDisUtil.getFoot(new Coordinate(nodesi.getX(), nodesi.getY()), lo, new Coordinate(nodesi1.getX(), nodesi1.getY()));
                double dis0 = blhToGauss.getBLHDistanceByHaverSine(f, lo);
                if (dis0 < dis) {
                    dis = dis0;
                    index = i;
                    foot = f;
                }
            }

            if (dis > 500||foot==null) {
                //1.jgd  配置错误   2.实时的航班计划中的跑道号错误    3，跑道的起飞降落线配置错误
                throw new IllegalArgumentException("距离跑道距离过远，可能跑道点配置有问题"+"可能存在的问题：1）"+runway+"的进港点可能配置错误;2）实时数据传入的跑道号错误，不是正确的;3）"+runway+inoutflag+"的跑道起飞降降落线有问题，出现了错误");
            }

            double bili =  (foot.getX() - flyAndSpeedNodes.get(index).getX()) / (flyAndSpeedNodes.get(index + 1).getX() - flyAndSpeedNodes.get(index).getX());

            double speed = (flyAndSpeedNodes.get(index + 1).getSpeed() - flyAndSpeedNodes.get(index).getSpeed()) * bili + flyAndSpeedNodes.get(index).getSpeed();
            double alt = (flyAndSpeedNodes.get(index + 1).getAlt() - flyAndSpeedNodes.get(index).getAlt()) * bili + flyAndSpeedNodes.get(index).getAlt();
            altorspeed = new double[]{speed, alt,speed-flightspeed};
        } else {
            String ac =inoutflag.equals("A") ?
                    String.valueOf(aircraftMap.get(acft).getAdij().get(roadLine.getNetworkname())) :
                    String.valueOf(aircraftMap.get(acft).getDdij().get(roadLine.getNetworkname()));
            altorspeed[2]=Double.parseDouble(ac);
            if (inoutflag.equals("A")) {
                altorspeed[0] = roadLine.getLimitspeedA();
            } else {
                altorspeed[0] = roadLine.getLimitspeedD();
            }
        }
        return altorspeed;
    }

    public void update(Flight flight, int T) {
        Coordinate A = new Coordinate(flight.getX(), flight.getY());
        List<RoadLine> mapLines = flight.getRoadlines();
        Move move = new Move();
        move.segindex = flight.getSegindex();
        move.lineindex = flight.getLineindex();
        move.isend = flight.getOnline();
        move.speed = flight.getSpeed();
        move.segyu = flight.getSegyu();
        if (aircraftMap.get(flight.getAcft()) == null) {
            try {
                throw new Exception("机型文件中没有" + flight.getAcft() + "的相关配置，请补充配置");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        RoadLine nowRoadlines = mapLines.get(flight.getLineindex());

        List<FlyAndSpeedNodes> flyAndSpeedNodes = flyandspeednodesmap.get(flight.getRunway() + flight.getInOutFlag()).getFlyAndSpeedNodesList();
        if (flyAndSpeedNodes == null) {
            try {
                throw new Exception("未配置跑道" + flight.getRunway() + "的,进出港类型为" + flight.getInOutFlag() + "的跑道起飞降落速度模型");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double[] nowspeed = getLimitspeedBySeg(nowRoadlines, flight.getRunway(), flight.getAcft(),flight.getSpeed(), flyAndSpeedNodes, A, flight.getInOutFlag());
        move.go(flight.getSpeed(), nowspeed[2], nowspeed[0], T);
        move.perlimistspeed = nowspeed[0];
        if (flight.getLineindex() < mapLines.size() - 1) {
            RoadLine nextroadline = mapLines.get(flight.getLineindex() + 1);
            move.nextlimistspeed = getLimitspeedByNext(nextroadline, flyAndSpeedNodes, flight.getRunway(), flight.getInOutFlag());
        } else {
            move.nextlimistspeed = move.perlimistspeed;
        }

        List<NodeFather> nodeFathers = flight.getBestnodes();
        Coordinate endnodes = nodeFathers.get(nodeFathers.size() - 1).getCoordinate();
        Coordinate segpoint = move.getSegCoordinate(A, mapLines, endnodes);
        flight.setSpeed(nowspeed[0]);
        flight.setOnline(move.isend);

        flight.setSegindex(move.segindex);
        flight.setLineindex(move.lineindex);
        flight.setSegyu(move.segyu);
        flight.setX(segpoint.getX());
        flight.setY(segpoint.getY());
        flight= upateelevation(flight, nowspeed[1], gearaltAD);
        flight.setZ(nowspeed[1]);
        flight.setOnline(move.isend);
        flight.setPasslength(flight.getPasslength() + move.vs);
        flight.setNetworkname(mapLines.get(flight.getLineindex()).getNetworkname());
        flight.setDirection(updateDir(flight, mapLines));
    }

    private Flight upateelevation(Flight flight, double alt, double[] gearaltAD) {
        if (flight.getInOutFlag().equals("A")) {
            if (flight.getZ() >= gearaltAD[0]) {
                if (alt < gearaltAD[0]) {
                    flight.setElevation(flight.getZ() / 10);
                    LandingGear landingGear = new LandingGear("opengear", flight.getFlightId(), flight.getFlightNo(),
                            flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(landingGear);
                }else{
                    flight.setElevation(10);
                }
            } else if (flight.getZ() > 0 && flight.getZ() < gearaltAD[0]) {

                if (alt == 0) {
                    flight.setElevation(0);
                    OffandLand offandLand = new OffandLand("landing", flight.getFlightId(), flight.getFlightNo(), flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(offandLand);
                }else{
                    flight.setElevation(flight.getZ() / 10);
                }
            } else {
                flight.setElevation(0);
            }
        } else if (flight.getInOutFlag().equals("D")) {
            if(flight.getZ() > gearaltAD[1]){
                flight.setElevation(10);
            }else if(0<flight.getZ()&&flight.getZ() <= gearaltAD[1]){
                if (alt > gearaltAD[1] ) {
                    flight.setElevation(10);
                    LandingGear landingGear = new LandingGear("closegear",
                            flight.getFlightId(), flight.getFlightNo(), flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    //收起起落架指令
                    flight.getMessages().add(landingGear);
                }else{
                    flight.setElevation(flight.getZ() / 10);
                }
            }else{
                if ( alt > 0) {
                    flight.setElevation(flight.getZ()/10);
                    OffandLand offandLand = new OffandLand("takeoff", flight.getFlightId(), flight.getFlightNo(), flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(offandLand);
                }else{
                    flight.setElevation(0);
                }
            }
        }
        return flight;
    }


    /**
     * @param
     * @description: 更新评价信息
     * @return:
     */

    //检测走到关键节点的飞机
    private double updateDir(Flight flight, List<RoadLine> roadLines) {
        //先判断工作空间，机位上特殊，其他的
        RoadLine roadLine = roadLines.get(flight.getLineindex());
        Coordinate[] coordinates = null;
        if (roadLines.get(flight.getLineindex()).direction.equals("reverse")) {
            coordinates = roadLines.get(flight.getLineindex()).geometry.reverse().getCoordinates();
        } else {
            coordinates = roadLine.geometry.getCoordinates();
        }
        int dk = flight.getSegindex();
        double dir = new BlhToGauss().getAngleByBlh(coordinates[dk], coordinates[dk + 1]);
        String type = roadLine.getType();
        if(roadLine.getNetworkname().equals("apron")){
            if((type.equals("inoutline") || type.equals("aproncenter")) && roadLines.get(flight.getLineindex()).getDirection().equals("reverse")){
                dir = dir + 180 > 360 ? dir - 180 : dir + 180;
            }
        }
        else {
            return dir;
        }

        //按照机位的正反向
        return dir;
    }

    public double getDistace(Coordinate A, Coordinate B) {
        return Math.sqrt(Math.pow(A.getX() - B.getX(), 2.0D) + Math.pow(A.getY() - B.getY(), 2.0D));
    }


}
