package com.conflict;

import base.AngDisUtil;
import base.BlhToGauss;
import calfun.Dijkstra;
import com.agent.Aircraft;
import com.agent.Flight;
import com.order.ConflictMes;
import com.order.ConflictOrderFatherMes;

import fun.RoadLine;
import org.apache.commons.collections.map.HashedMap;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import road.ShpAttribute;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;


/**
 * @description:
 * @author: LiPin
 * @time: 2022-10-30 1:26
 */
public class TaxiwayConflict {
    Map<String, Aircraft> aircraftMap = new HashMap<>();
    SimpleDateFormat sdf;
    public Map<String, List<ConflictOrderFatherMes>> conflictMaps = new HashedMap();
    ProtectionConflict protectionConflict = new ProtectionConflict();
    AngDisUtil angDisUtil = new AngDisUtil();

    Map<String, List<RoadConflictMes>> roadConflictMesMap = new HashMap<>();
    Map<String, Map<String, WeightArea>> weightlinesmap;
    Map<String, TrailingNumLimit> trailingNumLimitMap;
    Map<String, String> trailingconflictmaps;
    Map<String, Map<String, Flight>> history = new HashMap<>();
    Logger log = LoggerFactory.getLogger(TaxiwayConflict.class);
    BlhToGauss blhToGauss = new BlhToGauss();

    public TaxiwayConflict(SimpleDateFormat sdf, Map<String, Aircraft> aircraftMap, Map<String, Map<String, WeightArea>> weightlinesmap,
                           Map<String, TrailingNumLimit> trailingNumLimitMap, Map<String, String> trailingconflictmaps) {
        this.sdf = sdf;
        this.aircraftMap = aircraftMap;
        this.weightlinesmap = weightlinesmap;
        this.trailingNumLimitMap = trailingNumLimitMap;
        this.trailingconflictmaps = trailingconflictmaps;
    }

    public void updateConflict(Map<String, Flight> real) {
        UptateTaxiConflictMap uptateTaxiConflictMap = new UptateTaxiConflictMap();
        for (Map.Entry<String, Flight> entry1 : real.entrySet()) {
            Flight fa = entry1.getValue();
            for (Map.Entry<String, Flight> entryb : real.entrySet()) {
                Flight fb = entryb.getValue();
                if ((fa.getFlightId()!=fb.getFlightId()) && roadConflictMesMap.get(fa.getFlightId() +"_"+ fb.getFlightId()) == null &&
                        roadConflictMesMap.get(fb.getFlightId() +"_"+ fa.getFlightId()) == null) {
                        ShpAttribute a = new ShpAttribute();
                        a.setInoutflag(fa.getInOutFlag());
                        a.setRoadlines(fa.getRoadlines());
                        a.setOrderRoadNames(fa.getOrderRoadNames());
                        ShpAttribute b = new ShpAttribute();
                        b.setInoutflag(fb.getInOutFlag());
                        b.setRoadlines(fb.getRoadlines());
                        b.setOrderRoadNames(fb.getOrderRoadNames());
                    List<RoadConflictMes> sd = uptateTaxiConflictMap.getconflictmap(a, b, fa.getFlightId(),fb.getFlightId(),weightlinesmap);
                    if (sd.size() != 0) {
                        roadConflictMesMap.put(fa.getFlightId() +"_"+ fb.getFlightId(), sd);
                    }
                }
            }
        }
    }


    //交叉冲突//对头冲突//跟随冲突初步检测，很多的飞机，初始化可能存在的冲突

    public void updateConflict(List<Flight> newflights, Map<String, Flight> real) {
        UptateTaxiConflictMap uptateTaxiConflictMap = new UptateTaxiConflictMap();
        for (Map.Entry<String, Flight> entry1 : real.entrySet()) {
            Flight fa = entry1.getValue();
            for (int ki = 0; ki < newflights.size(); ki++) {
                Flight fb = newflights.get(ki);
                if ((fa.getFlightId()!=fb.getFlightId())&&roadConflictMesMap.get(fa.getFlightId() +"_"+ fb.getFlightId()) == null &&
                        roadConflictMesMap.get(fb.getFlightId() +"_"+ fa.getFlightId()) == null) {
                    ShpAttribute a = new ShpAttribute();
                    a.setInoutflag(fa.getInOutFlag());
                    a.setRoadlines(fa.getRoadlines());
                    a.setOrderRoadNames(fa.getOrderRoadNames());
                    ShpAttribute b = new ShpAttribute();
                    b.setInoutflag(fb.getInOutFlag());
                    b.setRoadlines(fb.getRoadlines());
                    b.setOrderRoadNames(fb.getOrderRoadNames());
                    List<RoadConflictMes> sd = uptateTaxiConflictMap.getconflictmap(a, b, fa.getFlightId(),fb.getFlightId(),weightlinesmap);
                    if (sd.size() != 0) {
                        roadConflictMesMap.put(fa.getFlightId() +"_"+ fb.getFlightId(), sd);
                    }
                }
            }
        }

    }

    private void get(Map<String, Flight> flights) {
        Map<String, Map<String, Flight>> newhis = new HashMap<>();
        for (Map.Entry<String, Flight> entry : flights.entrySet()) {
            Flight flight = entry.getValue();
            String trname = trailingconflictmaps.get(flight.getFlightId());
            if (trname != null) {
                TrailingNumLimit trailingNumLimit = trailingNumLimitMap.get(trname);
                Map<String, Flight> his = history.get(trailingNumLimit.getName()) == null ? new HashMap<>() : history.get(trailingNumLimit.getName());
                List<Coordinate> proa = blhToGauss.getRealprotectByGauss(aircraftMap.get(flight.getAcft()).getProtects().get(getnetworkname(flight)), new Coordinate(flight.getX(), flight.getY()), flight.getDirection());//飞机保护罩所在位置几何信息
                boolean isain = protectionConflict.intersectionJudgment2(trailingNumLimit.coordinates, proa);//飞机保护罩是否与线有交叉
                if (his.get(flight.getFlightId()) == null) {
                    if (isain) {
                        Map<String, Flight> newnew = newhis.get(trname) == null ? new HashMap<>() : newhis.get(trname);
                        newnew.put(flight.getFlightId(), his.get(flight.getFlightId()));
                        newhis.put(trname, newnew);
                    }
                } else {
                    if (!isain) {
                        his.remove(flight.getFlightId());
                        if (his.size() == 0) {
                            history.remove(trailingNumLimit.getName());
                        } else {
                            history.put(trailingNumLimit.getName(), his);
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, Map<String, Flight>> entry : newhis.entrySet()) {
            TrailingNumLimit trailingNumLimit = trailingNumLimitMap.get(entry.getKey());
            Map<String, Flight> his = history.get(trailingNumLimit.getName()) == null ? new HashMap<>() : history.get(trailingNumLimit.getName());
            Map<String, Flight> newnew = entry.getValue();
            for (Map.Entry<String, Flight> entry1 : newnew.entrySet()) {
                Flight fa = flights.get(entry1.getKey());
                int size = his.size();
                if (size >= trailingNumLimit.limitnum) {

                    boolean isbihuan = false;
                    for (Map.Entry<String, Flight> entry2 : his.entrySet()) {
                        String[] re = LoopDection(entry2.getValue(), fa);
                        if (!re[0].equals("false")) {//有一个闭环，则闭环
                            isbihuan = true;
                        }
                    }
                    if (!isbihuan) {
                        flights.get(entry1.getKey()).setWait(1);
                        log.info(trailingNumLimit.name + "超出容量预警,等待");
                    } else {
                        log.info(trailingNumLimit.name + "超出容量预警" + "禁止超容，会造成闭环," + fa.getFlightNo());
                    }

                } else {
                    his.put(entry1.getKey(), flights.get(entry1.getKey()));
                    history.put(trailingNumLimit.getName(), his);
                }
            }
        }
    }

    public void checkConflict(Map<String, Flight> flights, AircraftPushbackConflict aircraftPushbackConflict, long smalltime) {
        List<Map<String, List<Coordinate>>> pushflightslist = aircraftPushbackConflict.DrawPolygonBynodes(flights);
        for (Map.Entry<String, Flight> entry : flights.entrySet()) {
            for (Map.Entry<String, Flight> entry1 : flights.entrySet()) {
                Flight fa = entry.getValue();
                Flight fb = entry1.getValue();
                if (!entry.getKey().equals(entry1.getKey())) {
                    List<RoadConflictMes> roadConflictMes = roadConflictMesMap.get(fa.getFlightId() + "_" + fb.getFlightId());
                    if (roadConflictMes != null) {
                        List<ConflictOrderFatherMes> mesMap = conflictMaps.get(fa.getFlightId() + "_" + fb.getFlightId());
                        if (mesMap == null) {
                            mesMap = new ArrayList<>();
                            for (int i = 0; i < roadConflictMes.size(); i++) {
                                RoadConflictMes roadConflictMes1 = roadConflictMes.get(i);
                                ConflictOrderFatherMes conflictOrderFatherMes = new ConflictOrderFatherMes();
                                conflictOrderFatherMes.setType(roadConflictMes1.type);
                                conflictOrderFatherMes.setMaxnum(roadConflictMes1.maxnum);
                                conflictOrderFatherMes.setMesMap(roadConflictMes1.mesMap);
                                mesMap.add(conflictOrderFatherMes);
                                conflictMaps.put(fa.getFlightId() + "_" + fb.getFlightId(), mesMap);
                            }
                        }
                        for (int i = 0; i < mesMap.size(); i++) {
                            ConflictOrderFatherMes conflict = mesMap.get(i);
                            initConflictABs(fa, fb, conflict, pushflightslist, smalltime);
                        }
                    }
                }
            }
        }
        get(flights);
    }

    private String getnetworkname(Flight fa) {
        String anet = fa.getNetworkname();
        if (anet.equals("runway")) {
            String type = fa.getRoadlines().get(fa.getLineindex()).getType();
            if (!type.equals("runwaycenter") && !type.equals("runwayycx")) {
                anet = "taxiway";
            }
        }
        return anet;
    }

    private double getnextConflictdis(Flight fa, ConflictRoadMes conflictRoadMes) {
        Coordinate co = new Coordinate(fa.getX(), fa.getY());
        int[] index = conflictRoadMes.getIndexab();
        if (fa.getLineindex() >= index[1]) {
            return Double.NaN;//已经出去
        }
        if (fa.getLineindex() >= index[0] && fa.getLineindex() < index[1]) {
            return 0;//在内部
        }
        double length = 0;
        if (fa.getLineindex() < index[0]) {
            List<RoadLine> roadLine = fa.getRoadlines();
            Coordinate[] coordinates = roadLine.get(fa.getLineindex()).geometry.getCoordinates();
            length = blhToGauss.getBLHDistanceByHaverSine(co, coordinates[fa.getSegindex() + 1]);
            for (int i = fa.getSegindex() + 1; i < coordinates.length - 1; i++) {
                length = length + blhToGauss.getBLHDistanceByHaverSine(coordinates[i], coordinates[i + 1]);
            }
            for (int j = fa.getLineindex() + 1; j < index[0]; j++) {
                length = length + roadLine.get(j).length;
            }
        }
        return length;
    }

    private boolean[] getpriisin(Flight fa, Flight fb, ConflictRoadMes conflictRoadMesa, ConflictRoadMes conflictRoadMesb) {
        boolean[] isinpre = new boolean[]{false, false};
        double disa = getnextConflictdis(fa, conflictRoadMesa);
        double waitdisa = conflictRoadMesa.getWaitdis();
        double pridisa = conflictRoadMesa.getPriordis();
        if (!Double.isNaN(disa)) {
            if (waitdisa >= disa) {
                isinpre[0] = true;//提前占用的过程
            } else {
                if (disa <= waitdisa + pridisa) {
                    isinpre[0] = true;//提前占用的过程
                }
            }
        } else {
            isinpre[0] = false;//提前占用的过程
        }

        double disb = getnextConflictdis(fb, conflictRoadMesb);
        double pridisb = conflictRoadMesb.getPriordis();
        double waitdisb = conflictRoadMesb.getWaitdis();

        if (!Double.isNaN(disb)) {
            if (waitdisb >= disb) {
                isinpre[1] = true;//提前占用的过程
            } else {
                if (disb <= waitdisb + pridisb) {
                    isinpre[1] = true;//提前占用的过程
                }
            }
        } else {
            isinpre[1] = false;//提前占用的过程
        }
        return isinpre;
    }

    private void initConflictABs(Flight fa, Flight fb, ConflictOrderFatherMes conflict, List<Map<String, List<Coordinate>>> pushflightslist, long smalltime) {
        ConflictRoadMes conflictRoadMesa = conflict.mesMap.get(fa.getFlightId());
        ConflictRoadMes conflictRoadMesb = conflict.mesMap.get(fb.getFlightId());
        List<Coordinate> fapath = conflictRoadMesa.coordinates;
        List<Coordinate> proa = new ArrayList<>();
        if (fa.getNetworkname().equals("apron") && fa.getInOutFlag().equals("D")) {
            for (Map<String, List<Coordinate>> listMap : pushflightslist) {
                if (listMap.containsKey(fa.getFlightId())) {
                    proa = listMap.get(fa.getFlightId());
                }
            }
        } else {
            proa = blhToGauss.getRealprotectByGauss(aircraftMap.get(fa.getAcft()).getProtects().get(getnetworkname(fa)), new Coordinate(fa.getX(), fa.getY()), fa.getDirection());//飞机保护罩所在位置几何信息
        }
        boolean isain = protectionConflict.isin(fapath, new Coordinate(fa.getX(), fa.getY()), proa);
        //boolean isain = protectionConflict.intersectionJudgment2(fapath, proa);//飞机保护罩是否与线有交叉
        List<Coordinate> fbpath = conflictRoadMesb.coordinates;
        List<Coordinate> prob = new ArrayList<>();
        if (fb.getNetworkname().equals("apron") && fb.getInOutFlag().equals("D")) {
            for (Map<String, List<Coordinate>> listMap : pushflightslist) {
                if (listMap.containsKey(fb.getFlightId())) {
                    prob = listMap.get(fb.getFlightId());
                }
            }
        } else {
            prob = blhToGauss.getRealprotectByGauss(aircraftMap.get(fb.getAcft()).getProtects().get(getnetworkname(fb)), new Coordinate(fb.getX(), fb.getY()), fb.getDirection());//飞机保护罩所在位置几何信息
        }

        boolean isbin = protectionConflict.isin(fbpath, new Coordinate(fb.getX(), fb.getY()), prob);
        //boolean isbin = protectionConflict.intersectionJudgment2(fbpath, prob);//飞机保护罩是否与线有交叉
        boolean[] isinpre = getpriisin(fa, fb, conflictRoadMesa, conflictRoadMesb);
        double apridis = conflictRoadMesa.getPriordis() + conflictRoadMesa.getWaitdis();
        double bpridis = conflictRoadMesb.getPriordis() + conflictRoadMesb.getWaitdis();

        if (conflict.getType().equals("trailing")) {
            //如果配置中没有提前冲突没有提前约束条件，则按照先后顺序优先进行冲突识别
            boolean isintravlling = protectionConflict.intersectionJudgment(proa, prob);//如果是跟随同时出现，则先判断二者是否出现保护范围冲突
            if (conflictRoadMesa.getPriordis() == 0.0D && conflictRoadMesb.getPriordis() == 0.0D) {

                if (isain && isbin && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {
                    this.inittrailingConflict(fa, fb, conflict);
                }
                if (isain && isbin && isintravlling) {
                    this.stoptrailingFlight(fa, fb, conflict, smalltime);
                }
            } else if (conflictRoadMesa.getPriordis() != 0.0D && conflictRoadMesb.getWaitdis() == 0.0D) {
                if (isinpre[0] && isbin && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {
                    this.inittrailingConflict(fa, fb, conflict);
                }
                if (isintravlling) {
                    if (isinpre[0] && !isain && isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }
                    if (isain && isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }
                }
            } else if (conflictRoadMesb.getPriordis() != 0.0D && conflictRoadMesa.getPriordis() == 0.0D) {
                if (isain && isinpre[1] && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {
                    this.inittrailingConflict(fa, fb, conflict);
                }
                if (isintravlling) {
                    if (isinpre[1] && isain && !isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }
                    if (isain && isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }
                }
            } else if (conflictRoadMesb.getPriordis() != 0.0D && conflictRoadMesa.getPriordis() != 0.0D) {
                if (isinpre[0] && isinpre[1] && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {
                    this.inittrailingConflict(fa, fb, conflict);
                }
                if (isintravlling) {
                    if (isinpre[0] && !isain && isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }

                    if (isinpre[1] && isain && !isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }

                    if (isain && isbin) {
                        this.stoptrailingFlight(fa, fb, conflict, smalltime);
                    }
                }
            }
        } else {
            if (conflictRoadMesa.getPriordis() == 0 && conflictRoadMesb.getPriordis() == 0) {
                //如果配置中没有提前冲突没有提前约束条件，则按照先后顺序优先进行冲突识别
                if (isain && isbin && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {//初始化
                    initConorCrossConflict(fa, fb, fapath, fbpath, apridis, bpridis, conflict);
                }
                if (isain && isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
            } else if (conflictRoadMesa.getPriordis() != 0 && conflictRoadMesb.getWaitdis() == 0) {
                //如果a飞机有提前占用约束条件，而B没有，则A提前占用了之后，B飞机即需要在冲突出现时即停止等待
                if (isinpre[0] && isbin && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {//初始化
                    initConorCrossConflict(fa, fb, fapath, fbpath, apridis, bpridis, conflict);
                }
                if (isinpre[0] && !isain && isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
                if (isain && isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
            } else if (conflictRoadMesb.getPriordis() != 0 && conflictRoadMesa.getPriordis() == 0) {
                if (isain && isinpre[1] && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {//初始化
                    initConorCrossConflict(fa, fb, fapath, fbpath, apridis, bpridis, conflict);
                }
                if (isinpre[1] && isain && !isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
                if (isain && isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
            } else if (conflictRoadMesb.getPriordis() != 0 && conflictRoadMesa.getPriordis() != 0) {
                if (isinpre[0] && isinpre[1] && conflict.glideflightA.getFlightId().equals("") && conflict.stopflightB.getFlightId().equals("")) {//初始化
                    initConorCrossConflict(fa, fb, fapath, fbpath, apridis, bpridis, conflict);
                }
                if (isinpre[0] && !isain && isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
                if (isinpre[1] && isain && !isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
                if (isain && isbin) {
                    stopConOrCrossFlight(fa, fb, conflict, smalltime);
                }
            }
        }
    }

    private void stoptrailingFlight(Flight fa, Flight fb, ConflictOrderFatherMes conflict, long smalltime) {
        ConflictMes conflictMesA = conflict.glideflightA;
        ConflictMes conflictMesB = conflict.stopflightB;
        Flight a = fa.getFlightId().equals(conflictMesA.getFlightId()) ? fa : fb;
        Flight b = fb.getFlightId().equals(conflictMesB.getFlightId()) ? fb : fa;
        String[] re = LoopDection(a, b);
        if (re[0].equals("false")) {
            updatejunction(a, b, conflict, re[1], smalltime);
        } else {
            conflictMesA.setFlightId(b.getFlightId());
            conflictMesA.setFlightNo(b.getFlightNo());
            conflictMesB.setFlightId(a.getFlightId());
            conflictMesB.setFlightNo(a.getFlightNo());
            updatejunction(b, a, conflict, re[1], smalltime);
        }
    }


    private void stopConOrCrossFlight(Flight fa, Flight fb, ConflictOrderFatherMes conflict, long smalltime) {
        ConflictMes conflictMesA = conflict.glideflightA;
        ConflictMes conflictMesB = conflict.stopflightB;
        Flight a = fa.getFlightId().equals(conflictMesA.getFlightId()) ? fa : fb;
        Flight b = fb.getFlightId().equals(conflictMesB.getFlightId()) ? fb : fa;
        String[] re = LoopDection(a, b);
        if (re[0].equals("false")) {
            updatejunction(a, b, conflict, re[1], smalltime);
        } else {
            conflictMesA.setFlightId(b.getFlightId());
            conflictMesA.setFlightNo(b.getFlightNo());
            conflictMesB.setFlightId(a.getFlightId());
            conflictMesB.setFlightNo(a.getFlightNo());
            updatejunction(b, a, conflict, re[1], smalltime);
        }
    }

    private void initConorCrossConflict(Flight fa, Flight fb, List<Coordinate> fapath, List<Coordinate> fbpath, double apridis, double bpridis, ConflictOrderFatherMes conflict) {
        ConflictMes conflictMesA = conflict.glideflightA;
        ConflictMes conflictMesB = conflict.stopflightB;

        double dija = getdis(fapath, new Coordinate(fa.getX(), fa.getY()), apridis);
        double dijb = getdis(fbpath, new Coordinate(fb.getX(), fb.getY()), bpridis);
        Flight a = null;
        Flight b = null;
        if (dija < dijb) {
            a = fb;
            b = fa;
        } else if (dija >= dijb) {
            a = fa;
            b = fb;
        }
        conflictMesA.setFlightId(a.getFlightId());
        conflictMesB.setFlightId(b.getFlightId());
        conflictMesA.setFlightNo(a.getFlightNo());
        conflictMesB.setFlightNo(b.getFlightNo());
    }


    public Coordinate getFoot(Coordinate firstp, Coordinate midp, Coordinate lastp) {
        Coordinate foot = midp.copy();
        double dx = firstp.x - lastp.x;
        double dy = firstp.y - lastp.y;
        double u = (midp.x - firstp.x) * dx + (midp.y - firstp.y) * dy;
        u /= dx * dx + dy * dy;
        double footx = firstp.x + u * dx;
        double footy = firstp.y + u * dy;
        double d = Math.abs((firstp.x - lastp.x) * (firstp.x - lastp.x) + (firstp.y - lastp.y) * (firstp.y - lastp.y));
        double d1 = Math.abs((firstp.x - footx) * (firstp.x - footx) + (firstp.y - footy) * (firstp.y - footy));
        double d2 = Math.abs((lastp.x - footx) * (lastp.x - footx) + (lastp.y - footy) * (lastp.y - footy));
        if (d1 <= d && d2 <= d) {
            foot.setX(footx);
            foot.setY(footy);
        } else if (d1 > d2) {
            foot = lastp;
        } else {
            foot = firstp;
        }

        return foot;
    }


    private double getdis(List<Coordinate> path, Coordinate location, double wtdis) {
        double alllength = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            alllength = alllength + blhToGauss.getBLHDistanceByHaverSine(path.get(i), path.get(i + 1));
        }
        int index = 0;
        double dis = 9999;
        Coordinate foot = null;
        for (int i = 0; i < path.size() - 1; i++) {
            Coordinate foot0 = getFoot(path.get(i), location, path.get(i + 1));
            if (foot0 != null) {
                double dis0 = blhToGauss.getBLHDistanceByHaverSine(location, foot0);
                if (dis > dis0) {
                    dis = dis0;
                    index = i;
                    foot = foot0;
                }
            }
        }
        double passlength = 0;
        boolean isieuqalf = isequal(foot, path.get(0));
        boolean isieuqale = isequal(foot, path.get(path.size() - 1));
        if (!isieuqalf && isieuqale) {
            double tofirst = blhToGauss.getBLHDistanceByHaverSine(path.get(0), location);
            double toend = blhToGauss.getBLHDistanceByHaverSine(path.get(path.size() - 1), location);
            passlength = alllength + tofirst + toend;
        } else if (isieuqalf && !isieuqale) {
            double tofirst = blhToGauss.getBLHDistanceByHaverSine(path.get(0), location);
            passlength = wtdis - tofirst;
        } else if (!isieuqalf && !isieuqale) {
            for (int i = 0; i < index; i++) {
                double dis0 = blhToGauss.getBLHDistanceByHaverSine(path.get(i), path.get(i + 1));
                passlength = passlength + dis0;
            }
            double now = blhToGauss.getBLHDistanceByHaverSine(path.get(index), foot);
            passlength = passlength + now + wtdis;
        }
        return passlength;
    }


    public boolean isequal(Coordinate co, Coordinate co1) {

        return (co.getX() == co1.getX()) && (co.getY() == co1.getY());

    }

    private void inittrailingConflict(Flight fa, Flight fb, ConflictOrderFatherMes conflict) {
        ConflictMes conflictMesA = conflict.glideflightA;
        ConflictMes conflictMesB = conflict.stopflightB;
        Coordinate a = new Coordinate(fa.getX(), fa.getY());
        Coordinate b = new Coordinate(fb.getX(), fb.getY());
        double ain = getdis(conflict.mesMap.get(fa.getFlightId()).coordinates, a, 0);
        double bin = getdis(conflict.mesMap.get(fb.getFlightId()).coordinates, b, 0);
        if (ain > bin) {
            conflictMesA.setFlightId(fa.getFlightId());
            conflictMesB.setFlightId(fb.getFlightId());
            conflictMesA.setFlightNo(fa.getFlightNo());
            conflictMesB.setFlightNo(fb.getFlightNo());
        } else {
            conflictMesA.setFlightId(fb.getFlightId());
            conflictMesB.setFlightId(fa.getFlightId());
            conflictMesA.setFlightNo(fb.getFlightNo());
            conflictMesB.setFlightNo(fa.getFlightNo());
        }

    }

    //通过角度判断

    private void init2(Flight fa, Flight fb, ConflictOrderFatherMes conflict) {
        ConflictMes conflictMesA = conflict.glideflightA;
        ConflictMes conflictMesB = conflict.stopflightB;
        //先判断对方是否在未来的路上，如果在
        double btoa = blhToGauss.getAngleByBlh(new Coordinate(fb.getX(), fb.getY()), new Coordinate(fa.getX(), fa.getY()));
        double adk = angDisUtil.calrotate2(fa.getDirection(), btoa);
        if (0 <= adk && adk < 90) {
            conflictMesA.setFlightId(fa.getFlightId());
            conflictMesB.setFlightId(fb.getFlightId());
            conflictMesA.setFlightNo(fa.getFlightNo());
            conflictMesB.setFlightNo(fb.getFlightNo());
        } else if (adk > 90) {
            conflictMesA.setFlightId(fb.getFlightId());
            conflictMesB.setFlightId(fa.getFlightId());
            conflictMesA.setFlightNo(fb.getFlightNo());
            conflictMesB.setFlightNo(fa.getFlightNo());
        } else if (adk == 90) {
            conflictMesA.setFlightId(fa.getFlightId());
            conflictMesB.setFlightId(fb.getFlightId());
            conflictMesA.setFlightNo(fa.getFlightNo());
            conflictMesB.setFlightNo(fb.getFlightNo());
        }
    }


    public String[] LoopDection(Flight a, Flight b) {
        String[] re = new String[]{"false", ""};
        String flightIdA = a.getFlightId();
        String flightIdB = b.getFlightId();
        String time = a.getTime();
        long timelong = 0;
        List<String> nameslist = new ArrayList();
        try {
            timelong = sdf.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<String, Integer> names = new HashMap<>();
        List<String> roads = new ArrayList<>();
        if (flightIdA != flightIdB) {
            names.put(flightIdB, 0);
            names.put(flightIdA, 1);
            nameslist.add(flightIdB);
            nameslist.add(flightIdA);
            roads.add(flightIdA + "_" + flightIdB);
        }
        for (Map.Entry<String, List<ConflictOrderFatherMes>> entry : conflictMaps.entrySet()) {
            List<ConflictOrderFatherMes> ens = entry.getValue();
            for (int i = 0; i < ens.size(); i++) {
                ConflictOrderFatherMes conflictMes = ens.get(i);
                ConflictMes conflictMesA1 = conflictMes.glideflightA;
                ConflictMes conflictMesB1 = conflictMes.stopflightB;
                if (!conflictMesA1.getFlightId().equals("") && !conflictMesB1.getFlightId().equals("") && !conflictMes.getTime().equals("")) {
                    try {
                        long nowtime = Math.abs((sdf.parse(conflictMes.getEndtime()).getTime() - timelong) / 1000);
                        if (nowtime <= 1) {
                            if (names.get(conflictMesA1.getFlightId()) == null) {
                                names.put(conflictMesA1.getFlightId(), names.size());
                                nameslist.add(conflictMesA1.getFlightId());
                            }
                            if (names.get(conflictMesB1.getFlightId()) == null) {
                                names.put(conflictMesB1.getFlightId(), names.size());
                                nameslist.add(conflictMesB1.getFlightId());
                            }
                            roads.add(conflictMesA1.getFlightId() + "_" + conflictMesB1.getFlightId());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (names.size() >= 2) {
            List<Integer> roadIDs = getstr(roads, 0, 1, names);
            if (roadIDs.size() > 2) {
                re[0] = "true";
                re[1] = convertStr(roadIDs, nameslist);
            } else {
                if (roadIDs.size() == 2) {
                    boolean isin = false;
                    for (int i = 0; i < roads.size(); i++) {
                        if (roads.get(i).equals(flightIdB + "_" + flightIdA)) {
                            isin = true;
                        }
                    }
                    if (isin) {
                        re[0] = "true";
                        re[1] = flightIdB + "," + flightIdA;
                    } else {
                        re[0] = "false";
                        re[1] = flightIdA + "," + flightIdB;
                    }
                } else {
                    re[0] = "false";
                    re[1] = flightIdA + "," + flightIdB;
                }
            }
        } else {
            re[0] = "false";
            re[1] = flightIdA + "," + flightIdB;
        }
        return re;
    }

    public String convertStr(List<Integer> roadIDs, List<String> nameslist) {
        String str = "";

        for (int i = roadIDs.size() - 1; i > -1; --i) {
            if (str.equals("")) {
                str = nameslist.get(roadIDs.get(i));
            } else {
                str = str + "," + nameslist.get(roadIDs.get(i));
            }
        }

        return str;
    }


    public List<Integer> getstr(List<String> roads, int startindex, int endindex, Map<String, Integer> names) {
        double[][] nearst = getNearst(names, roads);
        Dijkstra dijkstra = new Dijkstra();
        dijkstra.setNearst(nearst);
        dijkstra.calPathMatrix(startindex);
        List<Integer> roadIDs = dijkstra.getNodes(endindex);
        return roadIDs;
    }

    private double[][] getNearst(Map<String, Integer> nodename, List<String> roads4) {
        double[][] nearst = new double[nodename.size()][nodename.size()];
        Dijkstra dijkstra = new Dijkstra();
        int i;
        for (i = 0; i < nearst.length; ++i) {
            for (int j = 0; j < nearst[0].length; ++j) {
                if (i == j) {
                    nearst[i][j] = 0.0D;
                } else {
                    nearst[i][j] = dijkstra.getNIF();
                }
            }
        }
        for (i = 0; i < roads4.size(); ++i) {
            String[] str = roads4.get(i).split("_");
            nearst[nodename.get(str[0])][nodename.get(str[1])] = 1.0D;
        }
        return nearst;
    }

    private void updatejunction(Flight fa, Flight fb, ConflictOrderFatherMes junction, String strs, long smalltime) {
        //如果飞机依然在里面
        List<RoadLine> aroadlines = fa.getRoadlines();
        List<RoadLine> broadlines = fb.getRoadlines();
        if (!junction.isBisin()) {
            junction.setBisin(true);
            junction.setStarttime(fa.getTime());
        } else {
            try {
                long time = (sdf.parse(fa.getTime()).getTime() - sdf.parse(junction.getEndtime()).getTime()) / 1000;
                if (time > 1) {
                    junction.setStarttime(fa.getTime());
                }
                junction.setEndtime(fa.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        junction.setLevel("E");
        junction.setEndtime(fa.getTime());
        junction.setConflictquene(strs);
        junction.stopflightB.setLocation(broadlines.get(fb.getLineindex()));
        junction.glideflightA.setLocation(aroadlines.get(fa.getLineindex()));
        junction.glideflightA.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
        junction.stopflightB.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
        fb.setWait(1);
        fb.setSpeed(0);
        fb.getMessages().add(junction);

//        if(junction.getType().equals("cross")){
//            LocalDateTime fatime = LocalDateTime.parse(fa.getTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//            long fatimestamp = fatime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
//            LocalDateTime fbtime = LocalDateTime.parse(fa.getTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//            long fbtimestamp = fbtime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
//            if((smalltime-fatimestamp)<=1800&&(smalltime-fbtimestamp)<=1800&&(smalltime-fatimestamp)>(smalltime-fbtimestamp)){
//                junction.stopflightB.setLocation(broadlines.get(fb.getLineindex()));
//                junction.glideflightA.setLocation(aroadlines.get(fa.getLineindex()));
//                junction.glideflightA.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//                junction.stopflightB.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//                fb.setWait(1);
//                fb.setSpeed(0);
//                fb.getMessages().add(junction);
//            }
//            else if ((smalltime-fatimestamp)<=1800&&(smalltime-fbtimestamp)<=1800){
//                junction.stopflightB.setLocation(aroadlines.get(fa.getLineindex()));
//                junction.glideflightA.setLocation(broadlines.get(fb.getLineindex()));
//                junction.glideflightA.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//                junction.stopflightB.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//                fa.setWait(1);
//                fa.setSpeed(0);
//                fa.getMessages().add(junction);
//            }
//            else if ((smalltime-fatimestamp)<=1800&&(smalltime-fbtimestamp)>1800){
//                junction.stopflightB.setLocation(broadlines.get(fb.getLineindex()));
//                junction.glideflightA.setLocation(aroadlines.get(fa.getLineindex()));
//                junction.glideflightA.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//                junction.stopflightB.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//                fb.setWait(1);
//                fb.setSpeed(0);
//                fb.getMessages().add(junction);
//            }
//            else if((smalltime-fatimestamp)>1800&&(smalltime-fbtimestamp)<=1800){
//                junction.stopflightB.setLocation(aroadlines.get(fa.getLineindex()));
//                junction.glideflightA.setLocation(broadlines.get(fb.getLineindex()));
//                junction.glideflightA.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//                junction.stopflightB.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//                fa.setWait(1);
//                fa.setSpeed(0);
//                fa.getMessages().add(junction);
//            }
//            else if((smalltime-fatimestamp)>(smalltime-fbtimestamp)){
//                junction.stopflightB.setLocation(broadlines.get(fb.getLineindex()));
//                junction.glideflightA.setLocation(aroadlines.get(fa.getLineindex()));
//                junction.glideflightA.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//                junction.stopflightB.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//                fb.setWait(1);
//                fb.setSpeed(0);
//                fb.getMessages().add(junction);
//            }
//            else {
//                junction.stopflightB.setLocation(aroadlines.get(fa.getLineindex()));
//                junction.glideflightA.setLocation(broadlines.get(fb.getLineindex()));
//                junction.glideflightA.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//                junction.stopflightB.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//                fa.setWait(1);
//                fa.setSpeed(0);
//                fa.getMessages().add(junction);
//            }
//        }
//        else {
//            junction.stopflightB.setLocation(broadlines.get(fb.getLineindex()));
//            junction.glideflightA.setLocation(aroadlines.get(fa.getLineindex()));
//            junction.glideflightA.setFirstlocation(new Coordinate(fa.getX(), fa.getY()));
//            junction.stopflightB.setFirstlocation(new Coordinate(fb.getX(), fb.getY()));
//            fb.setWait(1);
//            fb.setSpeed(0);
//            fb.getMessages().add(junction);
//        }
    }
}
