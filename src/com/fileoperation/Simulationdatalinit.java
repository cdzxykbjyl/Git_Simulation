package com.fileoperation;



import base.AngDisUtil;
import base.BlhToGauss;
import com.agent.Aircraft;
import com.agent.Flight;
import com.conflict.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.extratools.Tubao.aobao;
import com.runwayrule.AirlineRelease;
import com.runwayrule.ReleaseAfterAircraft;
import com.runwayrule.ReleaseByInoutflag;
import com.runwayrule.ReleaseByRunwayNo;
import com.runwayrule.RunwayTakeoffandDown;
import fun.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import road.ShpAttribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Simulationdatalinit {
    Logger log = LoggerFactory.getLogger(this.getClass());
    SimpleDateFormat sdf;

    BlhToGauss blhToGauss=new BlhToGauss();
    AngDisUtil angDisUtil=new AngDisUtil();
    public Simulationdatalinit(SimpleDateFormat sdf) {
        this.sdf = sdf;

    }

    public List<Coordinate> getcoordinate(String[] strings, Map<String, RoadLine> maps) {

        List<Coordinate> coordinate = new ArrayList<>();
        List<Coordinate> zx0 = new ArrayList<>();
        List<Coordinate> re0 = new ArrayList<>();

        for (int j = 0; j < strings.length; j++) {
            RoadLine roadLine = maps.get(strings[j]);
            List<Coordinate> zx1 = convertArrayToList(roadLine.geometry.getCoordinates());
            List<Coordinate> re1 = convertArrayToList(roadLine.geometry.reverse().getCoordinates());
            if (j == 0) {
                zx0 = zx1;
                re0 = re1;
            } else if (j == 1) {
                double[] array = new double[]{0, 0, 0, 0};
                array[0] = blhToGauss.getBLHDistanceByHaverSine(zx0.get(0), zx1.get(0));
                array[1] = blhToGauss.getBLHDistanceByHaverSine(zx0.get(0), zx1.get(zx1.size() - 1));
                array[2] = blhToGauss.getBLHDistanceByHaverSine(zx0.get(zx0.size() - 1), zx1.get(0));
                array[3] = blhToGauss.getBLHDistanceByHaverSine(zx0.get(zx0.size() - 1), zx1.get(zx1.size() - 1));
                int min = getindex(array);
                switch (min) {
                    case 0:
                        coordinate.addAll(re0);//前反后整
                        coordinate.addAll(zx1);
                        break;
                    case 1:
                        coordinate.addAll(re0);
                        coordinate.addAll(re1);
                        break;
                    case 2:
                        coordinate.addAll(zx0);
                        coordinate.addAll(zx1);
                        break;
                    case 3:
                        coordinate.addAll(zx0);
                        coordinate.addAll(re1);
                        break;
                    default:
                        break;
                }
            } else {
                Coordinate last = coordinate.get(coordinate.size() - 1);
                double array1 = blhToGauss.getBLHDistanceByHaverSine(zx0.get(0), last);
                double array2 = blhToGauss.getBLHDistanceByHaverSine(zx0.get(zx0.size() - 1), last);
                if (array1 > array2) {
                    coordinate.addAll(re1);
                } else {
                    coordinate.addAll(zx1);
                }
            }

        }
        return coordinate;
    }


    public Map<String, TrailingNumLimit> getTrailingNumLimitMap(List<JSONObject> trailinglines, Map<String, RoadLine> roadLineMaps) {

        Map<String, TrailingNumLimit> trailingNumLimitMap = new HashMap<>();
        for (int i = 0; i < trailinglines.size(); i++) {
            JSONObject jsonObject = trailinglines.get(i);
            String name = jsonObject.getString("name");
            String[] strings = jsonObject.getString("trailinglines").split(",");
            Map<String, RoadLine> maps = new HashMap<>();
            for (int j = 0; j < strings.length; j++) {
                RoadLine roadLine = roadLineMaps.get(strings[j]);
                maps.put(strings[j], roadLine);
            }
            List<Coordinate> coordinate = getcoordinate(strings, maps);
            TrailingNumLimit trailingNumLimit = new TrailingNumLimit(
                    name, maps, coordinate, jsonObject.getInteger("num"),
                    jsonObject.getString("limitdirs").split(","),
                    jsonObject.getString("describe") == null ? "" : jsonObject.getString("describe")
            );
            trailingNumLimitMap.put(name, trailingNumLimit);
        }

        return trailingNumLimitMap;
    }

    private String getrerunway(String runway) {
        String dir = runway.length() == 3 ? (runway.substring(2, 3).equals("L") ? "R" : "L") : "";
        int num = Integer.parseInt(runway.substring(0, 2));
        num = num > 18 ? num - 18 : num + 18;
        String numstr = num < 10 ? 0 + String.valueOf(num) : String.valueOf(num);
        String rerunway = numstr + dir;
        return rerunway;
    }

    public Map<String, List<RunwayFlightHis>> getRunwayQueneMap(Map<Long, List<Flight>> hisdata, Map<String, Flight> reals, long starttime, Map<String, List<Coordinate>> polygon_runwayocuppysmax) {
        Map<String, List<RunwayFlightHis>> directrunwaymap = new HashMap();
        for (Map.Entry<String, Flight> flightEntry : reals.entrySet()) {
            Flight flight = flightEntry.getValue();
            String runway = flight.getRunway();
            String rerunway = getrerunway(runway);
            List<Coordinate> runbj = polygon_runwayocuppysmax.get(runway + "_" + rerunway) == null ? polygon_runwayocuppysmax.get(rerunway + "_" + runway) : polygon_runwayocuppysmax.get(runway + "_" + rerunway);
            boolean isinPolygon = angDisUtil.IsinPolygon(runbj, new Coordinate(flight.getX(), flight.getY()));
            if (isinPolygon) {
                List<RunwayFlightHis> nowflights = directrunwaymap.get(flight.getRunway()) == null ? new ArrayList() : directrunwaymap.get(flight.getRunway());
                (nowflights).add(convertFlighttoRunwayFlightHis(flight));
                directrunwaymap.put(flight.getRunway(), nowflights);
            }
        }

        Map<String, List<RunwayFlightHis>> hisdirectrunwaymap = new HashMap();
        for (int j = 0; j < 600; j++) {
            long flighttime = starttime + j - 600;
            List<Flight> flights = hisdata.get(flighttime) == null ? new ArrayList<>() : hisdata.get(flighttime);
            for (int i = 0; i < flights.size(); i++) {
                Flight his = flights.get(i).copy();
                boolean isin = isin(his, directrunwaymap);
                if (isin) {
                    String runway = his.getRunway();
                    String rerunway = getrerunway(runway);
                    List<Coordinate> runbj = polygon_runwayocuppysmax.get(runway + "_" + rerunway) == null ? polygon_runwayocuppysmax.get(rerunway + "_" + runway) : polygon_runwayocuppysmax.get(runway + "_" + rerunway);
                    boolean isinPolygon =angDisUtil.IsinPolygon(runbj, new Coordinate(his.getX(), his.getY()));
                    if (isinPolygon) {
                        List<RunwayFlightHis> runwayFlightHis = hisdirectrunwaymap.get(his.getRunway()) == null ? new ArrayList<>() : hisdirectrunwaymap.get(his.getRunway());
                        boolean isins = false;
                        for (int k = 0; k < runwayFlightHis.size(); k++) {
                            if (runwayFlightHis.get(k).getFlightId().equals(his.getFlightId())) {
                                isins = true;
                            }
                        }
                        if (!isins) {
                            runwayFlightHis.add(convertFlighttoRunwayFlightHis(his));
                            hisdirectrunwaymap.put(his.getRunway(), runwayFlightHis);
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, List<RunwayFlightHis>> entry : hisdirectrunwaymap.entrySet()) {
            List<RunwayFlightHis> value = bubble_sortMinToMax(entry.getValue());
            hisdirectrunwaymap.put(entry.getKey(), value);
        }
        return hisdirectrunwaymap;
    }


    public Map<String, Flight> getRealFlightByTime(Map<Long, List<Flight>> hisdata, long start) {

        Map<String, Flight> allname = new HashMap<>();
        Map<String, Flight> last30big = new HashMap<>();
        Map<String, Flight> next30small = new HashMap<>();
        Map<String, Flight> equals = new HashMap<>();
        for (Map.Entry<Long, List<Flight>> entry : hisdata.entrySet()) {
            long flighttime = entry.getKey();
            List<Flight> flights = entry.getValue();
            if (Math.abs(flighttime - start) <= 30) {
                for (int i = 0; i < flights.size(); i++) {
                    Flight flight = flights.get(i);
                    allname.put(flight.getFlightId(), flight);
                    if (flighttime - start == 0) {
                        equals.put(flight.getFlightId(), flight);
                    } else {
                        if (flighttime < start) {
                            updatePreFlights(flight, last30big);
                        } else {
                            updateNextFlights(flight, next30small);
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, Flight> entry : allname.entrySet()) {
            Flight flight = entry.getValue();
            Flight startf = last30big.get(flight.getFlightId());
            Flight endf = next30small.get(flight.getFlightId());
            Flight equal = equals.get(flight.getFlightId());
            if (startf != null && endf != null && equal == null) {
                double angle = blhToGauss.getAngleByBlh(new Coordinate(startf.getX(), startf.getY()), new Coordinate(endf.getX(), endf.getY()));
                try {
                    long tostart = start - sdf.parse(startf.getTime()).getTime() / 1000;
                    long istoend = sdf.parse(endf.getTime()).getTime() / 1000 - start;
                    Coordinate insertco = getInsertPoint(new Coordinate(startf.getX(), startf.getY()), new Coordinate(endf.getX(), endf.getY()), tostart, istoend);
                    flight.setX(insertco.getX());
                    flight.setY(insertco.getY());
                    flight.setDirection(angle);
                    flight.setTime(sdf.format(start));
                } catch (ParseException var12) {
                    var12.printStackTrace();
                }
                equals.put(flight.getFlightId(), flight);
            }
        }
        return equals;
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


    private List<RunwayFlightHis> bubble_sortMinToMax(List<RunwayFlightHis> value) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i, j;
        RunwayFlightHis temp;
        int n = value.size();
        for (i = 0; i < n - 1; i++) { // 外层循环控制排序趟数
            RunwayFlightHis f = value.get(i).copy();
            String lasttime = f.getTime();
            int minindex = i;
            for (j = i; j < n; j++) { // 内层循环控制每一趟排序多少次
                RunwayFlightHis end1 = value.get(j);
                try {
                    boolean ismin = sdf.parse(end1.getTime()).getTime() < sdf.parse(lasttime).getTime();
                    if (ismin) { // 如果前一个元素大于后一个元素，交换这两个元素
                        lasttime = end1.getTime();
                        minindex = j;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            RunwayFlightHis minflight = value.get(minindex).copy();
            try {
                boolean ismin = sdf.parse(minflight.getTime()).getTime() < sdf.parse(f.getTime()).getTime();
                if (ismin) {
                    temp = f;
                    value.set(i, minflight);
                    value.set(minindex, temp);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    private boolean isin(Flight flight, Map<String, List<RunwayFlightHis>> directrunwaymap) {
        boolean isin = false;
        for (Map.Entry<String, List<RunwayFlightHis>> entry : directrunwaymap.entrySet()) {
            List<RunwayFlightHis> his = entry.getValue();
            for (int i = 0; i < his.size(); i++) {
                if (flight.getFlightId().equals(his.get(i).getFlightId())) {
                    return true;
                }
            }
        }
        return isin;
    }


    private boolean isinline(Coordinate[] coordinates, Coordinate co) {
        boolean isin = false;

        for (int i = 0; i < coordinates.length - 1; i++) {
            if (!isin) {
                boolean isis = angDisUtil.GetisOnlineSegment(coordinates[i], coordinates[i + 1], co);
                if (isis) {
                    isin = isis;
                }
            }
        }
        return isin;
    }

    private double getCrossPointsnext(List<Coordinate> polygon, Coordinate[] coordinates) {

        double dis = -9999;
        Coordinate last = null;
        for (int i = 0; i < coordinates.length - 1; i++) {
            for (int j = 0; j < polygon.size() - 1; j++) {
                Coordinate node = cross(polygon.get(j), polygon.get(j + 1), coordinates[i], coordinates[i + 1]);
                if (node != null && last == null) {
                    last = node;
                }
            }
            if (last != null) {
                if (dis == -9999) {
                    dis = 0;
                }
                dis = dis + blhToGauss.getDistaceByBlh(last, coordinates[i + 1]);
                last = coordinates[i + 1];
            }
        }
        return dis;
    }

    public Coordinate cross(Coordinate point1, Coordinate point2, Coordinate point3, Coordinate point4) {
        if ((point2.getY() - point1.getY()) * (point4.getX() - point3.getX()) == (point4.getY() - point3.getY()) * (point2.getX() - point1.getX()))
            return null;
        if (point2.getX() - point1.getX() == 0) {
            double k2 = (point4.getY() - point3.getY()) / (point4.getX() - point3.getX());
            double b2 = point3.getY() - point3.getX() * k2;
            double cross_x = point1.getX();
            double cross_y = k2 * cross_x + b2;
            Coordinate re = new Coordinate(cross_x, cross_y);
            if (((point1.getX() <= re.getX() && point2.getX() >= re.getX()) || (point1.getX() >= re.getX() && point2.getX() <= re.getX()))
                    && ((point3.getX() <= re.getX() && point4.getX() >= re.getX()) || (point3.getX() >= re.getX() && point4.getX() <= re.getX()))
            ) {
                return re;//线段相交点
            } else {
                if ((point1.getX() == point3.getX() && point1.getY() == point3.getY()) || (point1.getX() == point4.getX() && point1.getY() == point4.getY())) {
                    return point1;
                }
                if ((point2.getX() == point4.getX() && point2.getY() == point4.getY()) || (point2.getX() == point3.getX() && point2.getY() == point3.getY())) {
                    return point2;
                }
                return null;
            }

        }
        if (point4.getX() - point3.getX() == 0) {
            double k1 = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
            double b1 = point1.getY() - point1.getX() * k1;
            double cross_x = point3.getX();
            double cross_y = k1 * cross_x + b1;
            Coordinate re = new Coordinate(cross_x, cross_y);
            if (((point1.getX() <= re.getX() && point2.getX() >= re.getX()) || (point1.getX() >= re.getX() && point2.getX() <= re.getX()))
                    && ((point3.getX() <= re.getX() && point4.getX() >= re.getX()) || (point3.getX() >= re.getX() && point4.getX() <= re.getX()))
            ) {
                return re;//线段相交点
            } else {
                if ((point1.getX() == point3.getX() && point1.getY() == point3.getY()) || (point1.getX() == point4.getX() && point1.getY() == point4.getY())) {
                    return point1;
                }
                if ((point2.getX() == point4.getX() && point2.getY() == point4.getY()) || (point2.getX() == point3.getX() && point2.getY() == point3.getY())) {
                    return point2;
                }
                return null;
            }
        }
        double k1 = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
        double b1 = point1.getY() - point1.getX() * k1;
        double k2 = (point4.getY() - point3.getY()) / (point4.getX() - point3.getX());
        double b2 = point3.getY() - point3.getX() * k2;
        double cross_x = (b1 - b2) / (k2 - k1);
        double cross_y = k1 * cross_x + b1;
        Coordinate re = new Coordinate(cross_x, cross_y);//直线相交点

        if (((point1.getX() <= re.getX() && point2.getX() >= re.getX()) || (point1.getX() >= re.getX() && point2.getX() <= re.getX()))
                && ((point3.getX() <= re.getX() && point4.getX() >= re.getX()) || (point3.getX() >= re.getX() && point4.getX() <= re.getX()))
        ) {
            return re;//线段相交点
        } else {
            if ((point1.getX() == point3.getX() && point1.getY() == point3.getY()) || (point1.getX() == point4.getX() && point1.getY() == point4.getY())) {
                return point1;
            }
            if ((point2.getX() == point4.getX() && point2.getY() == point4.getY()) || (point2.getX() == point3.getX() && point2.getY() == point3.getY())) {
                return point2;
            }
            return null;
        }
    }


    public Map<String, Flight> matchRoadlines(Map<String, Flight> init, Map<Long, List<Flight>> flights, Map<String, StandNodes> standMes, Map<String, RunwayTakeoffandDown> flightspeednodesmap) {
      RoadlinesInit roadlinesInit=new RoadlinesInit();
        Iterator iter = init.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Flight> entry = (Map.Entry) iter.next();
            Flight flight = entry.getValue().copy();
            List<FlyAndSpeedNodes> flyAndSpeedNodes = flightspeednodesmap.get(flight.getRunway() + flight.getInOutFlag()).getFlyAndSpeedNodesList();
            flight = roadlinesInit.matchRoadlines(entry.getValue(), flights, standMes, flyAndSpeedNodes);
            if (flight==null){
                log.info("实时航班" + entry.getValue().getMes().toJSONString() + "缺少航班计划信息，"+"，已经将该航班删除！或请补充航班计划！");
                iter.remove();
            }else{
                init.put(entry.getKey(), flight);
            }
        }
        return init;
    }


    Coordinate getInsertPoint(Coordinate a, Coordinate b, double toa, double tob) {
        double bi = toa / (toa + tob);
        double x = (b.getX() - a.getX()) * bi + a.getX();
        double y = (b.getY() - a.getY()) * bi + a.getY();
        return new Coordinate(x, y);
    }


    private void updatePreFlights(Flight flight, Map<String, Flight> last30big) {
        long flighttime = 0;
        try {
            flighttime = sdf.parse(flight.getTime()).getTime() / 1000;
        } catch (ParseException var12) {
            var12.printStackTrace();
        }

        if (last30big.get(flight.getFlightId()) == null) {
            last30big.put(flight.getFlightId(), flight);
        } else {
            Flight start = last30big.get(flight.getFlightId());
            long starttime = 0;
            try {
                starttime = sdf.parse(start.getTime()).getTime() / 1000;
            } catch (ParseException var12) {
                var12.printStackTrace();
            }
            if (flighttime > starttime) {
                last30big.put(flight.getFlightId(), flight);
            }
        }

    }

    private void updateNextFlights(Flight flight, Map<String, Flight> next30small) {

        long flighttime = 0;
        try {
            flighttime = sdf.parse(flight.getTime()).getTime() / 1000;
        } catch (ParseException var12) {
            var12.printStackTrace();
        }
        if (next30small.get(flight.getFlightId()) == null) {
            next30small.put(flight.getFlightId(), flight);
        } else {
            Flight next = next30small.get(flight.getFlightId());
            long nexttime = 0;
            try {
                nexttime = sdf.parse(next.getTime()).getTime() / 1000;
            } catch (ParseException var12) {
                var12.printStackTrace();
            }
            if (flighttime < nexttime) {
                next30small.put(flight.getFlightId(), flight);
            }
        }
    }

    public Map<String, String> getsd(Map<String, TrailingNumLimit> trailingNumLimitMap, List<ShpAttribute> shpAttributes) {
        Map<String, String> remap = new HashMap<>();
        for (int i = 0; i < shpAttributes.size(); i++) {
            ShpAttribute shpAttribute = shpAttributes.get(i);
            List<RoadLine> roadLines = shpAttribute.roadlines;
            for (Map.Entry<String, TrailingNumLimit> entry : trailingNumLimitMap.entrySet()) {
                TrailingNumLimit trailingNumLimit = entry.getValue();
                String[] limitdirection = trailingNumLimit.getLimitdirs();
                boolean isin = false;
                for (int j = 0; j < limitdirection.length; j++) {
                    if ((shpAttribute.getRunway() + shpAttribute.getInoutflag()).equals(limitdirection[j])) {
                        isin = true;
                    }
                }
                if (isin) {
                    isin = isin(roadLines, trailingNumLimit.getRoads());
                    if (isin) {
                        remap.put(shpAttribute.getOrderRoadNames(), trailingNumLimit.getName());
                    }
                }
            }
        }
        return remap;

    }


    public List<Coordinate> convertArrayToList(Coordinate[] cos) {
        List<Coordinate> co = new ArrayList<>();
        for (int i = 0; i < cos.length; i++) {
            co.add(cos[i]);
        }
        return co;
    }


    public int getindex(double[] lengthArray) {
        double length = 99999;
        int minindex = 0;
        for (int j = 1; j < lengthArray.length; j++) {
            if (lengthArray[j] < length) {
                minindex = j;
                length = lengthArray[j];
            }
        }
        return minindex;
    }


    public boolean isin(List<RoadLine> roadLines, Map<String, RoadLine> roadLineMap) {
        for (int i = 0; i < roadLines.size(); i++) {
            for (Map.Entry<String, RoadLine> entry : roadLineMap.entrySet()) {
                if (entry.getKey().equals(roadLines.get(i).getId())) {
                    return true;
                }
            }
        }
        return false;
    }


    public Map<String, List<ShpAttribute>> updateRoad(List<JSONObject> jsonObjects, Map<String, RoadLine> roadLineMap, JSONObject waitJson,
                                                      Map<String, InsertNodes> insertNodes, Map<String, List<TaxiNodes>> TaxiPoints, List<InoutNodes> inoutNodes, Map<String, StandNodes> standMes, List<RunwayNodes> runwaynodes) {
        Map<String, List<ShpAttribute>> roadMaps = new HashMap<>();
        for (int j = 0; j < jsonObjects.size(); j++) {
            JSONObject js = jsonObjects.get(j);
            String key = js.getString("runway") + "_" + js.getString("inoutflag") + "_" + js.getString("stand");
            List<ShpAttribute> shpAttributes = roadMaps.get(key) == null ? new ArrayList<>() : roadMaps.get(key);
            String ordermes = js.getString("ordermes");
            boolean isin = false;
            for (int i = 0; i < shpAttributes.size(); i++) {
                if (shpAttributes.get(i).getOrderRoadNames().equals(ordermes)) {
                    isin = true;
                }
            }
            if (!isin) {
                ShpAttribute shpAttribute = new ShpAttribute();
                shpAttribute.runway = js.getString("runway");
                shpAttribute.inoutflag = js.getString("inoutflag");
                shpAttribute.stand = js.getString("stand");
                shpAttribute.orderRoadNames = js.getString("ordermes");
                JSONArray jsonArray = js.getJSONArray("roadlines");
                List<RoadLine> roadLines = new ArrayList<>();
                for (int k = 0; k < jsonArray.size(); k++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(k);
                    String id = jsonObject1.getString("id");
                    RoadLine roadLine = roadLineMap.get(id).copy();
                    roadLine.direction = jsonObject1.getString("direction");
                    roadLines.add(roadLine);
                }
                shpAttribute.roadlines = roadLines;
                List<NodeFather> nodeFathers = getbestnodes(js.getJSONArray("bestnodes"), insertNodes, TaxiPoints, inoutNodes, standMes, runwaynodes);
                for (int k = 0; k < nodeFathers.size(); k++) {
                    String wait = waitJson.getString(nodeFathers.get(k).getType());
                    if (wait != null) {
                        nodeFathers.get(k).setWait(Integer.valueOf(wait));
                    }
                }
                ConvertMesToGeom convertMesToGeom = new ConvertMesToGeom();
                Geometry geometry = convertMesToGeom.getgeometry(roadLines, 2);
                shpAttribute.setGeometry(geometry);
                shpAttribute.bestnodes = nodeFathers;
                shpAttribute.length = js.getDouble("length");
                shpAttribute.rotatenum = js.getInteger("rotatenum");
                shpAttribute.time = js.get("time") == null ? 0 : js.getDouble("time");
                shpAttributes.add(shpAttribute);
                roadMaps.put(key, shpAttributes);
            }
        }
        return roadMaps;
    }


    private List<NodeFather> getbestnodes(JSONArray jsonArray, Map<String, InsertNodes> insertNodes, Map<String, List<TaxiNodes>> taxipoints, List<InoutNodes> inoutNodes, Map<String, StandNodes> standMes, List<RunwayNodes> runwaynodes) {
        List<NodeFather> nodeFathers = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String type = jsonObject.getString("type");
            String name = jsonObject.getString("name");
            switch (type) {
                case "rkd":
                case "jgd":
                case "tld":
                case "zbd":
                    boolean isin = false;
                    for (int j = 0; j < runwaynodes.size(); j++) {
                        RunwayNodes node = runwaynodes.get(j).copy();
                        if (node.getName().equals(name)) {
                            node.setIndex(nodeFathers.size());
                            nodeFathers.add(node);
                            isin = true;
                        }
                    }
                    if (!isin) {
                        log.info("未传入跑道点:" + name + "," + "目标数据类型为" + type + ",请检查跑道点文件！");
                    }
                    break;
                case "taxi":
                    isin = false;
                    for (Map.Entry<String, List<TaxiNodes>> entry : taxipoints.entrySet()) {
                        List<TaxiNodes> nodes = entry.getValue();
                        for (int j = 0; j < nodes.size(); j++) {
                            TaxiNodes node = nodes.get(j).copy();
                            if (node.getName().equals(name)) {
                                node.setIndex(nodeFathers.size());
                                nodeFathers.add(node);
                                isin = true;
                            }
                        }
                    }
                    if (!isin) {
                        log.info("未传入移交点" + name + "," + "目标数据类型为" + type + ",请检查移交点文件！");
                    }
                    break;
                case "pushout":
                case "in":
                case "out":
                    isin = false;
                    for (int j = 0; j < inoutNodes.size(); j++) {
                        InoutNodes inoutNodes1 = inoutNodes.get(j).copy();
                        if (inoutNodes1.getName().equals(name)) {
                            inoutNodes1.setIndex(nodeFathers.size());
                            nodeFathers.add(inoutNodes1);
                            isin = true;
                        }
                    }
                    if (!isin) {
                        log.info("未传入入离位点" + name + "," + "目标数据类型为" + type + ",请检查入离位点文件！");
                    }
                    break;
                case "stand":
                    StandNodes stand = standMes.get(name).copy();
                    if (stand == null) {
                        log.info("未传入机位点" + name + "," + "目标数据类型为" + type + ",请检查机位点文件！");
                    } else {
                        stand.setIndex(nodeFathers.size());
                        nodeFathers.add(stand);
                    }
                    break;
                case "insert":
                    InsertNodes in = insertNodes.get(name).copy();
                    if (in == null) {
                        log.info("未传入必经点" + name + "," + "目标数据类型为" + type + ",请检查必经点文件！");
                    } else {
                        in.setIndex(nodeFathers.size());
                        nodeFathers.add(in);
                    }
                    break;
                default:
                    break;
            }
        }
        return nodeFathers;
    }

    public Map<String, ReleaseByRunwayNo> getReleaseByRunway(List<JSONObject> jsonObjects) {
        Map<String, ReleaseByRunwayNo> runwayReleaseRuler = new HashMap<>();
        for (int i = 0; i < jsonObjects.size(); i++)//行数字
        {
            JSONObject jsonObject = jsonObjects.get(i);
            String key = jsonObject.getString("nextrunway") + "_" + jsonObject.getString("prerunway");
            runwayReleaseRuler.put(key, new ReleaseByRunwayNo(jsonObject.getString("prerunway"), jsonObject.getString("nextrunway"), jsonObject.getString("duretime"), jsonObject.getString("discribe")));
        }
        return runwayReleaseRuler;
    }

    public Map<String, AirlineRelease> getAirlineRelease(List<JSONObject> jsonObjects) {
        Map<String, AirlineRelease> airlineReleaseMap = new HashMap<>();
        for (int i = 0; i < jsonObjects.size(); i++)//行数字
        {
            JSONObject jsonObject = jsonObjects.get(i);
            String[] nextrunway = jsonObject.getString("nextairline").split(",");
            String[] preairline = jsonObject.getString("preairline").split(",");
            String nextgroup = jsonObject.getString("nextgroup");
            String pregroup = jsonObject.getString("pregroup");
            String duretime = jsonObject.getString("duretime");
            for (int j = 0; j < nextrunway.length; j++) {
                for (int k = 0; k < preairline.length; k++) {
                    String key = nextrunway[j] + "_" + preairline[k];
                    AirlineRelease airlineRelease = new AirlineRelease(nextrunway[j], pregroup, preairline[k], nextgroup, duretime);
                    airlineReleaseMap.put(key, airlineRelease);
                }
            }
        }
        return airlineReleaseMap;
    }

    public Map<String, ReleaseAfterAircraft> getReleaseByAircraft(List<JSONObject> jsonObjects) {
        Map<String, ReleaseAfterAircraft> aircraftHashMap = new HashMap<>();
        for (int i = 0; i < jsonObjects.size(); i++)//行数字
        {
            JSONObject jsonObject = jsonObjects.get(i);
            ReleaseAfterAircraft releaseAfterAircraft = new ReleaseAfterAircraft(jsonObject.getString("preaircraft"), jsonObject.getString("nextaircraft"), jsonObject.getString("duretime"));
            aircraftHashMap.put(releaseAfterAircraft.preaircraft + "_" + releaseAfterAircraft.nextaircraft, releaseAfterAircraft);
        }
        return aircraftHashMap;
    }

    public Map<String, ReleaseByInoutflag> getReleaseByInoutflag(List<JSONObject> jsonObjects) {
        Map<String, ReleaseByInoutflag> releaseByInoutflagHashMap = new HashMap<>();
        for (int i = 0; i < jsonObjects.size(); i++)//行数字
        {
            JSONObject jsonObject = jsonObjects.get(i);
            String key = jsonObject.getString("nextinoutflag") + "_" + jsonObject.getString("preinoutflag");
            ReleaseByInoutflag releaseByInoutflag = new ReleaseByInoutflag(jsonObject.getString("preinoutflag"), jsonObject.getString("nextinoutflag"), jsonObject.getString("duretime"), jsonObject.getString("discribe"));
            releaseByInoutflagHashMap.put(key, releaseByInoutflag);
        }
        return releaseByInoutflagHashMap;
    }

    public Map<String, String[]> getStandLimit(List<JSONObject> jsonObjects) {
        Map<String, String[]> standlimitmap = new HashMap<>();
        for (int i = 1; i < jsonObjects.size(); i++)//行数字
        {
            JSONObject jsonObject = jsonObjects.get(i);
            String glide = jsonObject.getString("glide");
            String[] stops = jsonObject.getString("stop").split(",");
            standlimitmap.put(glide, stops);
        }
        return standlimitmap;
    }

    public Map<Long, List<Flight>> getFlightsFromFile(List<JSONObject> jsonObjects, SimpleDateFormat sdf) {
        Map<Long, List<Flight>> flights = new HashMap<>();
        int rows = jsonObjects.size();//行
        for (int i = 0; i < rows; i++)//行数字
        {
            JSONObject jsonObject = jsonObjects.get(i);
            String stand = jsonObject.getString("stand");
            String standstr = stand.substring(0, 1);
            //是否为临时机位
            if (!standstr.equals("L")) {
                String runway = jsonObject.getString("runway");
                String inoutflag = jsonObject.getString("inoutflag");
                String airroadline = jsonObject.getString("airroadline");
                Flight flight = new Flight(
                        jsonObject.getString("flightid"),//AN
                        jsonObject.getString("flightNo"),//GATE_WAY_TIME
                        runway,//AN
                        jsonObject.getString("stand"),//AN
                        inoutflag,
                        jsonObject.getString("acft"),//VEC
                        jsonObject.getString("time"),//ALT
                        jsonObject.getString("lon").equals("") ?
                                Double.NaN : Double.parseDouble(jsonObject.getString("lon")),//INOUTTYPE
                        jsonObject.getString("lat").equals("") ?
                                Double.NaN : Double.parseDouble(jsonObject.getString("lat")),//INOUTTYPE
                        jsonObject.getString("alt").equals("") ?
                                (inoutflag.equals("A") ? 1500 : 0) : Double.parseDouble(jsonObject.getString("alt")),//INOUTTYPE
                        jsonObject.getString("direction").equals("") ?
                                Double.NaN : Double.parseDouble(jsonObject.getString("direction")),//INOUTTYPE
                        jsonObject.getString("speed").equals("") ?
                                (inoutflag.equals("A") ? 130 : 0) : Double.parseDouble(jsonObject.getString("speed")),//INOUTTYPE
                        "Y",
                        jsonObject.getString("orderRoadNames"),
                        airroadline
                );
                try {
                    long flighttime = sdf.parse(flight.getTime()).getTime() / 1000;
                    List<Flight> flightslist = flights.get(flighttime) == null ? new ArrayList<>() : flights.get(flighttime);
                    flightslist.add(flight);
                    flights.put(flighttime, flightslist);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return flights;
    }

    public void initRoads(Map<Long, List<Flight>> flightsmap, Map<String, List<ShpAttribute>> roadshpsmaps, Map<String, StandNodes> standMes, Map<String, RunwayTakeoffandDown> runwayTakeoffandDownMap) {
        Map<String, List<RoadLine>> stringListMap = new HashMap<>();
        RoadlinesInit roadlinesInit = new RoadlinesInit();
        for (Map.Entry<Long, List<Flight>> entry : flightsmap.entrySet()) {
            List<Flight> flights = entry.getValue();
            int size = flights.size();
            for (int i = 0; i < size; i++) {
                Flight flight = flights.get(i);
                List<ShpAttribute> shpAttributes = roadshpsmaps.get(flight.getRunway() + "_" + flight.getInOutFlag() + "_" + flight.getStand());
                if (shpAttributes != null) {
                    RunwayTakeoffandDown runwayTakeoffandDown = runwayTakeoffandDownMap.get(flight.getRunway() + flight.getInOutFlag());
                    if (runwayTakeoffandDown == null) {
                        try {
                            throw new Exception("未配置跑道" + flight.getRunway() + "的进出港类型为" + flight.getInOutFlag() + "的起飞降落速度和高度模型");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        List<FlyAndSpeedNodes> flyAndSpeedNodes = runwayTakeoffandDown.getFlyAndSpeedNodesList();
                        flight = roadlinesInit.updateshpAttributejyshpattribute(flight, stringListMap, shpAttributes, standMes, flyAndSpeedNodes);
                        flights.set(i, flight);
                    }
                } else {
                    flights.remove(i);
                    size--;
                    i--;
                    log.info(flight.getRunway() + "_" + flight.getInOutFlag() + "_" + flight.getStand() + "缺少规划路径,为继续执行仿真,已将航班id为" + flight.getFlightId() + ",航班号为" + flight.getFlightNo() + "的航班计划" + "进行删除处理");
                }
            }
        }
    }

    public void updateRoadlines(List<RoadLine> roadLines) {

        for (int i = 0; i < roadLines.size(); ++i) {
            RoadLine roadLine = roadLines.get(i);
            Coordinate[] g = roadLine.geometry.getCoordinates();
            roadLine.islimitspeed = true;
            roadLine.islimitnum = true;
            roadLine.firstPoint = g[0];
            roadLine.endPoint = g[g.length - 1];
            roadLine.firstangle = blhToGauss.getAngleByBlh(g[0], g[1]);
            roadLine.endangle = blhToGauss.getAngleByBlh(g[g.length - 1], g[g.length - 2]);
            roadLine.length = blhToGauss.getRoadlengthByBlh(g);
        }
    }


    public Map<String, JSONObject> getAircraft(List<JSONObject> jsonObjects) {
        Map<String, JSONObject> aircraftMap = new HashMap<>();
        int rows = jsonObjects.size();//行
        //循环读取数据
        for (int i = 1; i < rows; i++)//行数字
        {
            JSONObject j = jsonObjects.get(i);
            j.put("x0", j.getDouble("x0"));
            j.put("y0", j.getDouble("y0"));
            j.put("wingspan", j.getDouble("wingspan"));
            j.put("aircraftlength", j.getDouble("aircraftlength"));
            aircraftMap.put(j.getString("Name"), j);
        }
        return aircraftMap;
    }


    private ConflictRoadMes getisinByQuene(List<RoadLine> roadLines, String road, String[] priorlines, WeightArea weightArea) {
        int[] index = null;
        boolean isisall = true;
        int starti = 9999;
        int endi = -9999;

        checkConflict checkconflict = new checkConflict();
        for (int i = 0; i < priorlines.length; i++) {
            boolean isis = false;
            for (int j = 0; j < roadLines.size(); j++) {
                RoadLine roadLine = roadLines.get(j);
                if (roadLine.getId().equals(priorlines[i])) {
                    isis = true;
                    if (starti > j) {
                        starti = j;
                    }
                    if (endi < j) {
                        endi = j;
                    }
                }
            }
            if (!isis) {
                isisall = false;
            }
        }
        if (isisall) {
            if (starti <= endi) {
                index = new int[]{starti, endi};
            }
        }
        if (index != null) {

            List<RoadLine> aroadlines = checkconflict.getRoadLinesByStartandEndIndex(roadLines, index);
            List<Coordinate> cos = checkconflict.convertRoadlineToPath(aroadlines);
            double enterangle = angDisUtil.getAngleByZB(cos.get(0), cos.get(1));
            String[] ids = checkconflict.getroadids(aroadlines);
            ConflictRoadMes ames = new ConflictRoadMes(cos, road, index, ids, cos.get(0), weightArea.getWaitdis(), weightArea.getPriordis(), enterangle);
            return ames;
        } else {
            return null;
        }
    }

    public Map<String, List<Coordinate>> getboundry(Map<String, RoadLine> namemaps, List<JSONObject> callines) {
        Map<String, List<Coordinate>> boundrymap = new HashMap<>();

        for (int m = 0; m < callines.size(); m++) {
            JSONObject js = callines.get(m);
            String key = js.getString("name");
            String[] ministerlinename = js.getString("arealines").split(",");
            List<Coordinate> coordinateLis = new ArrayList<>();
            for (int jk = 0; jk < ministerlinename.length; jk++) {
                RoadLine roadLine = namemaps.get(ministerlinename[jk].trim());
                Coordinate[] coordinates = roadLine.geometry.getCoordinates();
                for (int k = 0; k < coordinates.length; k++) {
                    coordinateLis.add(coordinates[k]);
                }
            }
            int size = coordinateLis.size();
            for (int i = 0; i < size - 1; i++) {
                Coordinate pre = coordinateLis.get(i);
                for (int jk = i + 1; jk < size; jk++) {
                    Coordinate next = coordinateLis.get(jk);
                    if ((pre.x == next.x) && (pre.y == next.y)) {
                        coordinateLis.remove(jk);
                        jk--;
                        size--;
                    }
                }
            }
            boundrymap.put(key, new aobao(coordinateLis).GetConcave_Ball(3));
        }
        return boundrymap;
    }


    public Map<String, Map<String, WeightArea>> getWeightLines(List<JSONObject> weightlines) {
        Map<String, Map<String, WeightArea>> weightlinesmap = new HashMap<>();
        for (int j = 0; j < weightlines.size(); j++) {
            JSONObject jsonObject = weightlines.get(j);
            String name = jsonObject.getString("name");
            String[] priordirs = jsonObject.getString("priordirs").split(",");
            String type = jsonObject.getString("type");
            double priordis = jsonObject.getDouble("priordis");
            double waitdis = jsonObject.getDouble("waitdis");
            String[] crosslines = jsonObject.getString("crosslines").split(",");
            WeightArea weightArea = new WeightArea(type, name, priordirs, priordis, waitdis, crosslines);
            Map<String, WeightArea> AreaMap = weightlinesmap.get(type) == null ? new HashMap<>() : weightlinesmap.get(type);
            AreaMap.put(name, weightArea);
            weightlinesmap.put(type, AreaMap);
        }
        return weightlinesmap;
    }

    public void initRunway(Map<String, RoadLine> roadLines, Map<String, RunwayModel> runwaymes) {
        for (Map.Entry<String, RoadLine> entry : roadLines.entrySet()) {
            RoadLine roadLine = entry.getValue();
            if (roadLine.getNetworkname().equals("runway")) {
                int ID = roadLine.getIndex();
                String name = roadLine.getName();
                String type = roadLine.getType();
                String[] namestrs = name.split("_");
                for (int i = 0; i < namestrs.length; ++i) {
                    RunwayModel rk0 = runwaymes.get(namestrs[i]);
                    if (rk0 != null) {
                        List<Integer> lineids = rk0.getLines().get(type) == null ? new ArrayList() : rk0.getLines().get(type);
                        (lineids).add(ID);
                        rk0.getLines().put(type, lineids);
                        runwaymes.put(namestrs[i], rk0);
                    }
                }
            }
        }
    }

    public Map<String, RunwayModel> initrunway(String[][] runwayno, List<RunwayNodes> runwaynodes, Map<String, RoadLine> roadLinemaps) {
        Map<String, RunwayModel> runwaymes = new HashMap<>();
        for (int i = 0; i < runwayno.length; i++) {
            String name0 = runwayno[i][0];
            String name1 = runwayno[i][1];
            RunwayModel runwayModel0 = new RunwayModel();
            runwayModel0.setName(name0);
            runwayModel0.setOthername(name1);
            runwaymes.put(name0, runwayModel0);
            RunwayModel runwayModel1 = new RunwayModel();
            runwayModel1.setName(name1);
            runwayModel1.setOthername(name0);
            runwaymes.put(name1, runwayModel1);
        }
        initRunway(roadLinemaps, runwaymes);
        updateRunwayPointsBylist(runwaynodes, runwaymes);
        return runwaymes;
    }

    public Map<String, RunwayModel> updateRunwayPointsBylist(List<RunwayNodes> nodes, Map<String, RunwayModel> runwaymes) {
        for (int i = 0; i < nodes.size(); ++i) {
            RunwayNodes node = nodes.get(i);
            RunwayModel runwayMes = runwaymes.get(node.getRunway()) == null ? new RunwayModel() : runwaymes.get(node.getRunway());
            if (node.getType().equals("tld")) {
                node.getForbidlines().addAll(runwayMes.getLines().get("runwaycenter"));
            }
            List<RunwayNodes> runwayNodes = runwayMes.allsides.get(node.getType()) == null ? new ArrayList() : runwayMes.allsides.get(node.getType());
            (runwayNodes).add(node);
            runwayMes.allsides.put(node.getType(), runwayNodes);
            runwaymes.put(node.getRunway(), runwayMes);
        }
        return runwaymes;
    }

    private List<Coordinate> getforpointsbundry(double ce, double hou, double qian, double wingspan, double aircraftlength, double x0, double y0) {
        List<Coordinate> bundry = new ArrayList<>();
        bundry.add(new Coordinate(-(ce + wingspan - y0), -(hou + aircraftlength / 2 - (-x0))));
        bundry.add(new Coordinate(-(ce + wingspan - y0), qian + aircraftlength / 2 - x0));
        bundry.add(new Coordinate(ce + wingspan + y0, qian + aircraftlength / 2 - x0));
        bundry.add(new Coordinate(ce + wingspan + y0, -hou + aircraftlength / 2 - (-x0)));
        return bundry;
    }

    public Map<String, Aircraft> initAircraft(Map<String, JSONObject> aircraftMapjs) {
        Map<String, Aircraft> aircraftMap = new HashMap<>();
        String[] systems = new String[]{"runway", "taxiway", "apron"};
        for (Map.Entry<String, JSONObject> entry : aircraftMapjs.entrySet())//行数字
        {
            JSONObject js = entry.getValue();
            Aircraft aircraft = new Aircraft();
            aircraft.setCategory(js.getString("category"));
            aircraft.setName(js.getString("Name"));
            aircraft.setWingspan(js.getDouble("wingspan"));
            aircraft.setAircraftlength(js.getDouble("aircraftlength"));
            aircraft.setLoadCapacity(js.getInteger("loadCapacity"));
            aircraft.setX0(js.getDouble("x0"));
            aircraft.setY0(js.getDouble("y0"));
            aircraft.setProducer(js.getString("producer"));
            //注释掉下边保护罩会按照机翼和机身长度计算，否则按照中心点计算。
            aircraft = getprotectarea(aircraft, systems, js);
            aircraftMap.put(entry.getKey(), aircraft);
        }
        return aircraftMap;
    }

    private Aircraft getprotectarea(Aircraft aircraft, String[] systems, JSONObject js) {
        Map<String, List<Coordinate>> protects = new HashMap<>();
        Map<String, Double> Aa = new HashMap<>();
        Map<String, Double> Da = new HashMap<>();
        for (int i = 0; i < systems.length; i++) {
            String systemname = systems[i];
            JSONObject allmes = JSONObject.parseObject(js.getString(systemname));
            List<Coordinate> protect = getforpointsbundry(allmes.getDouble("ce"), allmes.getDouble("hou"), allmes.getDouble("qian"), aircraft.getWingspan(), aircraft.getAircraftlength(), aircraft.getX0(), aircraft.getY0());
            protects.put(systemname, protect);
            Aa.put(systemname, allmes.getDouble("Aa"));
            Da.put(systemname, allmes.getDouble("Da"));
        }
        aircraft.setAdij(Aa);
        aircraft.setDdij(Da);
        aircraft.setProtects(protects);
        return aircraft;
    }

}
