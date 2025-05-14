package test;

import base.Time;
import com.agent.Flight;
import com.agent.Aircraft;
import com.conflict.*;
import com.evaluate.EvalueMes;
import com.google.gson.JsonArray;
import com.order.*;
import com.position.UpPosition;
import com.evaluate.OrderofEvaluate;
import com.alibaba.fastjson.JSONObject;
import com.runwayrule.RunwayReleaseRule;
import com.runwayrule.RunwayTakeoffandDown;
import fun.FlyAndSpeedNodes;
import fun.RunwayModel;
import fun.StandNodes;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 实时数据处理主类。
 * 会有实时处理框架通过反射：realDataFilter，disposeRealData，disposeRealDataList
 *
 * @author 24560
 */


public class Simulation {



    //！！！！！！！！！！！！！！！！！！！以下六个列表都是要给到宇飞的！！！！！！！！！！！！！！！！！！！！！！
    //！！！！！！！！！！！！！！！！！！！宇飞那边的数据格式是BlockingQueue<String>格式！！！！！！！！！！！！！！！！！！！！！！
    //！！！！！！！！！！！！！！！！！！！以下六个列表都是要给到宇飞的！！！！！！！！！！！！！！！！！！！！！！
    //Long是为了读取秒，为了存时间，然后存入Map<String, List<RunwayFlightHis>>中
    public Map<Long, Map<String, List<RunwayFlightHis>>> timeHisdirectrunwaymap = new HashMap<>();
    public Map<Long, Map<String, List<RunwayFlightHis>>> timeHiscrossrunwaymap = new HashMap<>();

    //positions没往里面存，因为本地跑不动
    BlockingQueue<String> positions = new LinkedBlockingQueue<>();//位置数据集合
    BlockingQueue<String> orders = new LinkedBlockingQueue<>();//指令数据集合
    Map<String, String> ordersmap = new HashMap<>();//指令数据集合
    public Map<String, HisTimeNodes> hisTimeNodesHashMap = new HashMap<>();
    ///////////////////////////////////////////////////////////////////////////////////////////////



    TaxiwayConflict fconflict;
    SimpleDateFormat sdf;
    RunwayConflictByQuene runwayConflictByQuene;
    UpPosition upPosition;
    public Order order;
    public EvalueMes evalueMes;
    int T = 1;
    Map<String, List<ConflictOrderFatherMes>> conflictMaps = new HashMap<String, List<ConflictOrderFatherMes>>();
    RunwayReleaseRule runwayReleaseRule;
    Logger log = LoggerFactory.getLogger(SimulationTest.class);
    Map<String, List<Coordinate>> boundrymap;
    AircraftPushbackConflict a = new AircraftPushbackConflict();

    public Simulation(SimpleDateFormat sdf, Map<String, Aircraft> aircraftMap, Map<String, StandNodes> standMes,
                      Map<String, List<Coordinate>> polygon_runwayocuppys, Map<String, Map<String, List<Coordinate>>> polygon_flypolygonss,
                      Map<String, RunwayModel> runwaymes, int retreatBridgeBefore, EvalueMes evalueMes, int T,
                      RunwayReleaseRule runwayReleaseRule, Map<String, Map<String, WeightArea>> weightlinesmap,
                      Map<String, List<Coordinate>> boundrymap, double[] gearaltAD, Map<String, RunwayTakeoffandDown> flyandspeednodesmap,
                      Map<String, TrailingNumLimit> trailingNumLimitMap, Map<String, String> trailingconflictmaps, AircraftPushbackConflict a) {
        this.sdf = sdf;
        this.boundrymap = boundrymap;
        this.evalueMes = evalueMes;
        this.runwayReleaseRule = runwayReleaseRule;
        this.T = T;
        this.upPosition = new UpPosition(aircraftMap, standMes, flyandspeednodesmap, gearaltAD);
        this.fconflict = new TaxiwayConflict(sdf, aircraftMap, weightlinesmap, trailingNumLimitMap, trailingconflictmaps);
        this.runwayConflictByQuene = new RunwayConflictByQuene(sdf, polygon_runwayocuppys, runwayReleaseRule, aircraftMap);
        this.order = new Order(sdf, retreatBridgeBefore, runwaymes, standMes, aircraftMap);
        this.a = a;
    }


    public void disposeRealData(Map<Long, List<Flight>> hisData, Map<String, Flight> init, Map<String, List<RunwayFlightHis>> hisdirectrunwaymap, Map<Long, List<Flight>> plan, String starttime, String endtime, JSONObject runwayflightnum) {
        long smalltime = 0;//初始化最小时间
        long end_time = 0;//初始化最小时间
        try {
            if (starttime == null || starttime == "null" || starttime.equals("")) {
                smalltime = getMinTime(plan);//初始化最小时间
            } else {
                smalltime = sdf.parse(starttime).getTime() / 1000;
            }
            if (endtime == null || endtime == "null" || endtime.equals("")) {
                end_time = getMaxTime(plan);//初始化最小时间
            } else {
                end_time = sdf.parse(endtime).getTime() / 1000;
            }
        } catch (ParseException e) {
            log.error(e.getMessage());
        }

        Map<String, Flight> real = new HashMap<String, Flight>();
        if (init != null) {
            real.putAll(init);
            fconflict.updateConflict(real);
            runwayConflictByQuene.hisdirectrunwaymap = hisdirectrunwaymap;
        }
        Map<String, List<Flight>> stands = order.initStand(plan);
        Map<String, Flight> needDsend = new HashMap<String, Flight>();
        //    needDsend= order.initneedsend(smalltime, stands);
        evalueMes.initEvaluateMap(plan);

        Iterator iter = plan.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            long timekey = (long) entry.getKey();
            if (smalltime > timekey || end_time < timekey) {
                iter.remove();
            }
        }
        UdpSender udpSender = new UdpSender("localhost", 41234);//udp地址,udpSender==null时，不发送udp
//        UdpSender udpSender1 = new UdpSender("localhost", 41234);//udp地址,udpSender==null时，不发送udp localhost
        saveandsend(udpSender, real, needDsend, order);
        boolean iscontinue = true;
        while (iscontinue) {
            order.monitorRemoveBridge(plan, smalltime);
            List<Flight> newflights = order.getNewflightsandUpdatePlan(runwayConflictByQuene, plan, real, smalltime, hisTimeNodesHashMap, runwayflightnum);
            fconflict.updateConflict(newflights, real);
            upPosition.updatePosition(real, sdf.format(smalltime * 1000), T);//更新位置paidui
            fconflict.checkConflict(real, a, smalltime);//检测冲突

            ///////////记录跑道实时的排序状态///////////
            runwayConflictByQuene.updateRunwayConflictByQuene(real, smalltime);
            if(!runwayConflictByQuene.hisdirectrunwaymap.isEmpty()){
                timeHisdirectrunwaymap.put(smalltime, runwayConflictByQuene.hisrunwaymap_copy("hisdirectrunwaymap"));
            }
            if(!runwayConflictByQuene.hiscrossrunwaymap.isEmpty()){
                timeHiscrossrunwaymap.put(smalltime, runwayConflictByQuene.hisrunwaymap_copy("hiscrossrunwaymap"));
            }
            //////////////////////////////////////

            runwayConflictByQuene.updateRunwayConflictByQuene(real, smalltime);
            order.morniterInOutStandAndStandStation(real);//检测机位状态
            order.getTaxiWeit(real, T);//检测移交等待点
            order.updateOutline(real);//更新下线指令
            order.UpdateNeedSend(real, needDsend, stands);//更新需要进行输出的飞机；
            saveandsend(udpSender, real, needDsend, order);

            if (hisData.get(smalltime) != null) {
                List<Flight> hislist = hisData.get(smalltime);
                for (int i = 0; i < hislist.size(); i++) {
                    Flight flight = hislist.get(i).copy();
                    flight.setIfHistory("realdata");
                    //发的历史数据，直接发送，没有做任何处理
                    udpSender.sendDataToUdp(flight.getMes().toString());
                }
            }

            evalueMes.updateEvaluateMap(real);//更新评价

            order.updateHistNodeMapAndDeleteOffline(real, hisTimeNodesHashMap, runwayConflictByQuene.hisdirectrunwaymap, fconflict, conflictMaps);//删除下线的飞机//处理下线飞机的冲突列表
            System.out.println(smalltime + "" + plan.size() + real.size());
            smalltime = smalltime + T;
            if ((plan.size() == 0 && real.size() == 0) || (end_time < smalltime)) {
                iscontinue = false;
            }
            try {
                Thread.sleep(1);//定时发送
            } catch (InterruptedException e) {
                log.info(e.getMessage());
            }

        }//while

        evalueMes.update();
        for (Map.Entry<String, String> entry : ordersmap.entrySet()) {
            try {
                orders.put(entry.getValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public long getMinTime(Map<Long, List<Flight>> plan) {
        long mintime = -1;
        for (Map.Entry<Long, List<Flight>> entry : plan.entrySet()) {
            long flighttime = entry.getKey();
            if (mintime == -1) {
                mintime = flighttime;
            } else {
                if (flighttime < mintime) {
                    mintime = flighttime;
                }
            }
        }
        return mintime;
    }

    public long getMaxTime(Map<Long, List<Flight>> plan) {
        long maxtime = -1;
        for (Map.Entry<Long, List<Flight>> entry : plan.entrySet()) {
            long flighttime = entry.getKey();
            if (maxtime == -1) {
                maxtime = flighttime;
            } else {
                if (flighttime > maxtime) {
                    maxtime = flighttime;
                }
            }
        }
        return maxtime + 7200;
    }

    private JSONObject gettrallingByRunwait(Map<String, Flight> real, JSONObject runwayjs) {
        String stopflightId = runwayjs.getString("stopflightId");
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            List<OrderFatherMes> orderFatherMes = entry.getValue().getMessages();
            for (int i = 0; i < orderFatherMes.size(); i++) {
                OrderFatherMes fatherMes = orderFatherMes.get(i);
                if (fatherMes.getType().equals("trailing") | fatherMes.getType().equals("cross") |
                        fatherMes.getType().equals("confrontation")) {
                    ConflictOrderFatherMes conflictMes = (ConflictOrderFatherMes) fatherMes;
                    if (conflictMes.getGlideflightA().getFlightId().equals(stopflightId)) {
                        runwayjs.put("waitqueue", conflictMes.getConflictquene());
                    }
                }
            }
        }
        return runwayjs;
    }


    //void saveandsend( Map<String, Flight> real, Map<String, Flight> needDsend, Order order) {
    void saveandsend(UdpSender udpSender, Map<String, Flight> real, Map<String, Flight> needDsend, Order order) {

        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            List<OrderFatherMes> orderFatherMes = entry.getValue().getMessages();
            for (int i = 0; i < orderFatherMes.size(); i++) {
                JSONObject js = orderFatherMes.get(i).getMes();
                if (js.getString("type").equals("runwayoccupy")) {
                    if (js.getString("stopinoutFlag").equals("D")) {
                        js= gettrallingByRunwait(real, js);
                    }
                }
                ordersmap.put(js.getString("key"), js.toJSONString());
            }
        }

        for (Map.Entry<String, AbuttedGallery> entry : order.abuttedGalleries.entrySet()) {
            JSONObject js = entry.getValue().getMsg();
            ordersmap.put(js.getString("key"), js.toJSONString());
            if (js.getString("key").equals("")) {
                System.out.println(js);
            }
        }
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue().copy();
//            flight.setDirection(flight.getDirection() + 40 > 360 ? flight.getDirection() + 40 - 360 : flight.getDirection() + 40);
            try {
                positions.put(flight.getMes().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            flight.setIfHistory("simulation");
            udpSender.sendDataToUdp(flight.getMes().toString());
        }
        Iterator iter = needDsend.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter.next();
            Flight flight = (Flight) Flightmap.getValue();
            Flight mes = flight.copy();
            mes.setOnline("Y");
            mes.setDirection(mes.getDirection() + 40 > 360 ? mes.getDirection() + 40 - 360 : mes.getDirection() + 40);
            try {
                positions.put(mes.getMes().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
              udpSender.sendDataToUdp(mes.getMes().toString());
            iter.remove();
        }
    }
    // public void sendOrders( Map<String, Flight> real, Map<String, Flight> needDsend, Order order) {

    public void sendOrders(UdpSender udpSender, Map<String, Flight> real, Map<String, Flight> needDsend, Order order) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            List<OrderFatherMes> orderFatherMes = entry.getValue().getMessages();
            for (int i = 0; i < orderFatherMes.size(); i++) {
                // KafkaProducer.send("order", order.getOrderMes(orderFatherMes.get(i)), null);
            }
        }
        for (int i = 0; i < order.abuttedGalleries.size(); i++) {
            //KafkaProducer.send("order", order.abuttedGalleries.get(i).getMsg().toString(), null);
        }
        List<String> positionMes = getPositionMes(real, needDsend);//1-2
        for (int i = 0; i < positionMes.size(); i++) {
            //  udpSender.sendDataToUdp(positionMes.get(i));
            // KafkaProducer.send("simulation_aircraft", positionMes.get(i), null);
        }
    }

    public List<String> getPositionMes(Map<String, Flight> real, Map<String, Flight> needDsend) {
        List<String> returnstr = new ArrayList<>();
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            JSONObject flight = entry.getValue().getMes();
            double dir = flight.getDouble("direction") + 40 > 360 ? flight.getDouble("direction") + 40 - 360 : flight.getDouble("direction") + 40;
            flight.put("direction", dir);
            returnstr.add(flight.toString());
        }
        Iterator iter = needDsend.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry Flightmap = (Map.Entry) iter.next();
            Flight flight = (Flight) Flightmap.getValue();
            JSONObject mes = flight.getMes();
            mes.put("online", "Y");
            mes.put("direction", flight.getDirection() + 40 > 360 ? flight.getDirection() + 40 - 360 : flight.getDirection() + 40);
            returnstr.add(mes.toString());
            iter.remove();
        }
        return returnstr;
    }


}
