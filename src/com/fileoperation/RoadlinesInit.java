package com.fileoperation;



import base.AngDisUtil;
import base.BlhToGauss;
import com.agent.Flight;
import com.alibaba.fastjson.JSONArray;

import com.order.Gaid;
import com.order.RoadOrder;
import fun.FlyAndSpeedNodes;
import fun.RoadLine;
import fun.StandNodes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import road.ShpAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-06-21 19:00
 */

public class RoadlinesInit {
 AngDisUtil angDisUtil=new AngDisUtil();
 BlhToGauss blhToGauss=new BlhToGauss();
    Logger log = LoggerFactory.getLogger(this.getClass());
    private double getdis(Coordinate a1, Coordinate a2) {
        return Math.sqrt(Math.pow(a1.getX() - a2.getX(), 2) + Math.pow(a1.getY() - a2.getY(), 2));
    }

    private void updatelines(List<RoadLine> lists) {

        for (int i = 0; i < lists.size() - 1; i++) {
            String predir = lists.get(i).direction;
            String nextdir = lists.get(i + 1).direction;
            Coordinate[] pre = predir.equals("positive") ? lists.get(i).geometry.getCoordinates() : lists.get(i).geometry.reverse().getCoordinates();
            Coordinate[] next = nextdir.equals("positive") ? lists.get(i + 1).geometry.getCoordinates() : lists.get(i + 1).geometry.reverse().getCoordinates();
            double diss = getdis(pre[pre.length - 1], next[0]);
            if (diss > 0.000001) {
                double dis = 999999;
                Coordinate foot = null;
                int coordindex = 0;
                int lineindex = 0;
                for (int ni = 0; ni < next.length - 1; ni++) {
                    Coordinate foot0 = angDisUtil.getFoot(next[ni], pre[pre.length - 1], next[ni + 1]);
                    double dis0 = blhToGauss.getDistace(foot0, pre[pre.length - 1]);
                    if (dis0 < dis) {
                        dis = dis0;
                        foot = foot0;
                        coordindex = ni;
                        lineindex = 0;
                    }
                }
                for (int ni = 0; ni < pre.length - 1; ni++) {
                    Coordinate foot0 = angDisUtil.getFoot(pre[ni], next[0], pre[ni + 1]);
                    double dis0 = blhToGauss.getDistace(foot0, next[0]);
                    if (dis0 < dis) {
                        dis = dis0;
                        foot = foot0;
                        coordindex = ni;
                        lineindex = 1;
                    }
                }
                if (lineindex == 0) {
                    Coordinate[] nexti = new Coordinate[next.length - coordindex];
                    nexti[0] = foot;
                    for (int ni = coordindex + 1; ni < next.length; ni++) {
                        nexti[ni - coordindex] = next[ni];
                    }
                    Geometry geometry = new GeometryFactory().createLineString(nexti);
                    lists.get(i + 1).geometry = nextdir.equals("positive") ? geometry : geometry.reverse();
                } else {
                    Coordinate[] prei = new Coordinate[coordindex + 2];
                    for (int ni = 0; ni < coordindex + 1; ni++) {
                        prei[ni] = pre[ni];
                    }
                    prei[coordindex + 1] = foot;
                    Geometry geometry = new GeometryFactory().createLineString(prei);
                    lists.get(i).geometry = predir.equals("positive") ? geometry : geometry.reverse();
                }
            }
        }
    }

    public Flight updateshpAttributejyshpattribute(Flight flight, Map<String, List<RoadLine>> stringListMap, List<ShpAttribute> shpAttributes, Map<String, StandNodes> standMes,List<FlyAndSpeedNodes> flyAndSpeedNodes) {

        Gaid gaid = new Gaid();
        JSONArray jsonArray = new JSONArray();
        ShpAttribute shpAttribute = null;
        double minlength = 999999;
        int minindex = 0;
        for (int j = 0; j < shpAttributes.size(); j++) {
            if (shpAttributes.get(j).orderRoadNames.equals(flight.getOrderRoadNames())) {
                shpAttribute = shpAttributes.get(j);
            }
            jsonArray.add(shpAttributes.get(j).orderRoadNames);
            if (shpAttributes.get(j).getLength() < minlength) {
                minlength = shpAttributes.get(j).getLength();
                minindex = j;
            }
        }
        if (shpAttribute == null) {
            shpAttribute = shpAttributes.get(minindex);
            if (!(flight.getOrderRoadNames() == null || flight.getOrderRoadNames().equals("") || flight.getOrderRoadNames().equals("null"))) {
                log.info("数据库中的路径信息为:" + jsonArray.toJSONString() + "," + "无滑行指令：" + flight.getOrderRoadNames() +
                        ",已默认最短路线:" + shpAttribute.getOrderRoadNames());
            }
        }
        updatelines(shpAttribute.roadlines);
        if (Double.isNaN(flight.getX()) && Double.isNaN(flight.getY())) {
            Coordinate node = shpAttribute.bestnodes.get(0).getCoordinate();
            flight.setX(node.getX());
            flight.setY(node.getY());
        }
        List<RoadLine> roadLines = new ArrayList<>();
        for (RoadLine roadline : shpAttribute.getRoadlines()) {
            roadLines.add(roadline.copy());
        }
        flight.setRoadlines(roadLines);
        flight.setBestnodes(shpAttribute.getBestnodes());
        flight.setOrderRoadNames(shpAttribute.getOrderRoadNames());
        flight = updateflight(flight, flyAndSpeedNodes, standMes);
        List<RoadOrder> orders = gaid.getGaids(shpAttribute.roadlines, flight.getOrderRoadNames());
        stringListMap.put(shpAttribute.getOrderRoadNames(), shpAttribute.roadlines);
        return flight;
    }

    private Flight updateflight(Flight flight, List<FlyAndSpeedNodes> flyAndSpeedNodesList, Map<String, StandNodes> standMes) {
        List<RoadLine> lines = flight.getRoadlines();
        Coordinate co = new Coordinate(flight.getX(), flight.getY());
        int[] index = new int[]{0, 0};
        double mindis = 99999;
        for (int k = 0; k < lines.size(); k++) {
            RoadLine line = lines.get(k);
            Coordinate[] coordinates = null;
            if (line.direction.equals("positive")) {
                coordinates = line.geometry.getCoordinates();
            } else {
                coordinates = line.geometry.reverse().getCoordinates();
            }
            for (int j = 0; j < coordinates.length - 1; j++) {
                Coordinate foot0 = angDisUtil.getFoot(coordinates[j], co, coordinates[j + 1]);
                double realdis = blhToGauss.getBLHDistanceByHaverSine(foot0, co);
                if (realdis < mindis) {
                    index[0] = k;
                    index[1] = j;
                    mindis = realdis;
                    flight.setX(foot0.getX());
                    flight.setY(foot0.getY());
                }
            }
        }
        flight.setLineindex(index[0]);
        flight.setSegindex(index[1]);
        RoadLine roadLine = flight.getRoadlines().get(index[0]);
        Coordinate[] coordinates = null;
        if (roadLine.direction.equals("reverse")) {
            coordinates = roadLine.geometry.reverse().getCoordinates();
        } else {
            coordinates = roadLine.geometry.getCoordinates();
        }
        double dir = blhToGauss.getAngleByBlh(coordinates[flight.getSegindex()], coordinates[flight.getSegindex() + 1]);
        String type = roadLine.getType();
        if ((type.equals("aproncenter") || type.equals("inoutline") )&&roadLine.getDirection().equals("reverse")) {
            dir = dir + 180 > 360 ? dir - 180 : dir + 180;
        }
        flight.setDirection(dir);
        if (roadLine.getType().equals("runwaycenter") || roadLine.getType().equals("runwayycx")) {
            double[] speedandalt = getspeedoraltByFlyandSpeedModel(flyAndSpeedNodesList,co);
            flight.setSpeed(speedandalt[0]);
            flight.setZ(speedandalt[1]);
        } else {
            if (flight.getInOutFlag().equals("A")) {
                flight.setSpeed(roadLine.getLimitspeedA());
            } else {
                flight.setSpeed(roadLine.getLimitspeedD());
            }
            flight.setZ(0);
        }
        flight.setNetworkname(roadLine.getNetworkname());
        return flight;
    }

    public Flight matchRoadlines(Flight flight, Map<Long, List<Flight>> flights, Map<String, StandNodes> standMes,  List<FlyAndSpeedNodes> flyAndSpeedNodesList) {
        Flight plan = null;
        for (Map.Entry<Long, List<Flight>> entry1 : flights.entrySet()) {
            List<Flight> flights1 = entry1.getValue();
            for (int i = 0; i < flights1.size(); i++) {
                Flight p = flights1.get(i);
                if (flight.getFlightId().equals(p.getFlightId())) {//当
                    plan = p;
                }
            }
        }








        if (plan != null) {



            flight.setOrderRoadNames(plan.getOrderRoadNames());
            flight.setRoadlines(plan.getRoadlines());//匹配路线
            flight.setAirline(plan.getAirline());//匹配路线
            flight.setBestnodes(plan.getBestnodes());//匹配最佳节点
            flight.setRunway(plan.getRunway());//匹配跑道
            flight.setStand(plan.getStand());//匹配机位
            flight.setAcft(plan.getAcft());//匹配机型
            flight = updateflight(flight, flyAndSpeedNodesList, standMes);
            return flight;
        } else {
            return null;
        }

    }
    public double[] getspeedoraltByFlyandSpeedModel(List<FlyAndSpeedNodes> flyAndSpeedNodes, Coordinate co) {
        double[] re = new double[]{0, 0};
        double mindis = 99999;

        for (int j = 0; j < flyAndSpeedNodes.size() - 1; j++) {
            FlyAndSpeedNodes flyAndSpeedNodes1 = flyAndSpeedNodes.get(j);
            Coordinate co1 = new Coordinate(flyAndSpeedNodes1.getX(), flyAndSpeedNodes1.getY());
            FlyAndSpeedNodes flyAndSpeedNodes2 = flyAndSpeedNodes.get(j + 1);
            Coordinate co2 = new Coordinate(flyAndSpeedNodes2.getX(), flyAndSpeedNodes2.getY());
            Coordinate foot0 = angDisUtil.getFoot(co1, co, co2);
            double realdis = blhToGauss.getBLHDistanceByHaverSine(foot0, co);
            if (realdis < mindis) {
                if (co2.getX() - co1.getX() == 0) {
                    re[0] = flyAndSpeedNodes2.getSpeed();
                    re[1] = flyAndSpeedNodes2.getAlt();
                } else {
                    double key = (foot0.getX() - co1.getX()) / (co2.getX() - co1.getX());
                    re[0] = (flyAndSpeedNodes2.getSpeed() - flyAndSpeedNodes1.getSpeed()) * key + flyAndSpeedNodes1.getSpeed();
                    re[1] = (flyAndSpeedNodes2.getAlt() - flyAndSpeedNodes1.getAlt()) * key + flyAndSpeedNodes1.getAlt();
                }
                mindis = realdis;
            }
        }
        return re;
    }


    public int[] getIndex(List<RoadLine> lines, Coordinate co) {

        int[] index = new int[]{0, 0};
        double mindis = 99999;
        for (int k = 0; k < lines.size(); k++) {
            RoadLine line = lines.get(k);
            Coordinate[] coordinates = new Coordinate[]{};
            if (line.direction.equals("positive")) {
                coordinates = line.geometry.getCoordinates();
            } else {
                coordinates = line.geometry.reverse().getCoordinates();
            }
            for (int j = 0; j < coordinates.length - 1; j++) {
                Coordinate foot0 = angDisUtil.getFoot(coordinates[j], co, coordinates[j + 1]);
                double realdis = blhToGauss.getBLHDistanceByHaverSine(foot0, co);
                if (realdis < mindis) {
                    index[0] = k;
                    index[1] = j;
                    mindis = realdis;
                }
            }
        }
        return index;
    }


    public double[] getLimitspeedBySeg(RoadLine roadLine, List<FlyAndSpeedNodes> flyAndSpeedNodes, Coordinate lo, String runway, String inoutflag) {
        double[] altorspeed = new double[]{0, 0};
        if (roadLine.getType().equals("runwaycenter") || roadLine.getType().equals("runwayycx")) {

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
            double bili = (foot.getX() - flyAndSpeedNodes.get(index).getX()) / (flyAndSpeedNodes.get(index + 1).getX() - flyAndSpeedNodes.get(index).getX());
            double speed = (flyAndSpeedNodes.get(index + 1).getSpeed() - flyAndSpeedNodes.get(index).getSpeed()) * bili + flyAndSpeedNodes.get(index).getSpeed();
            double alt = (flyAndSpeedNodes.get(index + 1).getAlt() - flyAndSpeedNodes.get(index).getAlt()) * bili + flyAndSpeedNodes.get(index).getAlt();
            altorspeed = new double[]{speed, alt};
        } else {
            if (inoutflag.equals("A")) {
                altorspeed[0] = roadLine.getLimitspeedA();
            } else {
                altorspeed[0] = roadLine.getLimitspeedD();
            }
        }
        return altorspeed;
    }

}
