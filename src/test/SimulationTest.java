package test;

import base.AngDisUtil;
import base.BlhToGauss;
import com.agent.Aircraft;
import com.conflict.*;
import com.evaluate.EvalueMes;
import com.agent.Flight;
import com.fileoperation.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import com.runwayrule.RunwayReleaseRule;
import com.runwayrule.RunwayTakeoffandDown;
import fileoperation.ReadandWriteFile;
import fun.*;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import road.ShpAttribute;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**  用于首都
 * @author zhangqi
 **/
public class SimulationTest {
    Map<String, RunwayModel> runwaymes;
    Map<String, Aircraft> aircraftMap;
    RunwayReleaseRule runwayReleaseRule;
    Map<Long, List<Flight>> flights;
    Map<String, String[]> standlimitmaps;
    Map<String, Map<String, WeightArea>> weightlinesmap;
    Map<String, List<Coordinate>> boundrymap;
    Map<String, TrailingNumLimit> trailingNumLimitMap;
    //Map<String, List<RoadConflictMes>> roadConflictMesMap;
    Map<String, String> trailingconflictmaps;
    Simulationdatalinit simulationdatalinit;
    Map<String, RunwayTakeoffandDown> runwayTakeoffandDownMap = new HashMap<>();
    AircraftPushbackConflict a = new AircraftPushbackConflict();

    public SimulationTest(List<RoadLine> roadLines, SimpleDateFormat sdf,
                          List<JSONObject> standlimit, List<JSONObject> weightlines, List<JSONObject> roadjson, List<RunwayNodes> runwaynodes,
                          List<JSONObject> releaseByAircraft, List<JSONObject> airlineRelease, List<JSONObject> releaseByInoutflag, List<JSONObject> aircraftS,
                          List<JSONObject> releaseByRunwayno, List<JSONObject> flightslists, Map<String, StandNodes> standMes, String[][] runwayno,
                          List<JSONObject> calareas, List<JSONObject> trailinglines, JSONObject waitmes, Map<String, List<FlyAndSpeedNodes>> initflightspeednodesmap, List<RunwayTakeoffandDown> runwayTakeoffandDowns,
                          Map<String, InsertNodes> insertNodes, Map<String, List<TaxiNodes>> TaxiPoints, List<InoutNodes> inoutNodes) {
        simulationdatalinit = new Simulationdatalinit(sdf);
        simulationdatalinit.updateRoadlines(roadLines);
        Map<String, RoadLine> roadLineMaps = new HashMap<>();
        for (int i = 0; i < roadLines.size(); i++) {
            RoadLine roadLine = roadLines.get(i);
            roadLineMaps.put(roadLine.getId(), roadLine);
        }

        weightlinesmap = simulationdatalinit.getWeightLines(weightlines);
        //   roadConflictMesMap = simulationdatalinit.updateConflict(shpAttributes, weightlinesmap);
        boundrymap = simulationdatalinit.getboundry(roadLineMaps, calareas);
        standlimitmaps = simulationdatalinit.getStandLimit(standlimit);
        runwayReleaseRule = new RunwayReleaseRule(simulationdatalinit.getReleaseByRunway(releaseByRunwayno),
                simulationdatalinit.getAirlineRelease(airlineRelease),
                simulationdatalinit.getReleaseByAircraft(releaseByAircraft),
                simulationdatalinit.getReleaseByInoutflag(releaseByInoutflag));
        Map<String, JSONObject> aircraftMaps = simulationdatalinit.getAircraft(aircraftS);//机型
        aircraftMap = simulationdatalinit.initAircraft(aircraftMaps);
        //new 更新
        a.readAircraftProtectionCover(aircraftMap);
        a.getStandInoutLineNode(standMes, roadLines);
        flights = simulationdatalinit.getFlightsFromFile(flightslists, sdf);//航班计划读取
        runwaymes = simulationdatalinit.initrunway(runwayno, runwaynodes, roadLineMaps);

        for (int i = 0; i < runwayTakeoffandDowns.size(); i++) {
            RunwayTakeoffandDown runwayTakeoffandDown = runwayTakeoffandDowns.get(i);
            runwayTakeoffandDown.flyAndSpeedNodesList = getflyAndSpeedNodesList(runwayTakeoffandDowns.get(i));
            runwayTakeoffandDownMap.put(runwayTakeoffandDown.runway + runwayTakeoffandDown.inoutflag, runwayTakeoffandDown);
        }

        ///////////////////////////////////////////恢复路径规划数据//////////////////////////////////////////
        Map<String, List<ShpAttribute>> roadshpsmaps = simulationdatalinit.updateRoad(roadjson, roadLineMaps, waitmes, insertNodes, TaxiPoints, inoutNodes, standMes, runwaynodes);
        /////////////////////////////////////////////////////////////////////////////////////

        simulationdatalinit.initRoads(flights, roadshpsmaps, standMes, runwayTakeoffandDownMap);//航班计划中路径初始化
        List<ShpAttribute> shpAttributes = new ArrayList<>();
        for (Map.Entry<String, List<ShpAttribute>> entry : roadshpsmaps.entrySet()) {
            shpAttributes.addAll(entry.getValue());
        }

        trailingNumLimitMap = simulationdatalinit.getTrailingNumLimitMap(trailinglines, roadLineMaps);
        trailingconflictmaps = simulationdatalinit.getsd(trailingNumLimitMap, shpAttributes);
    }

    private Coordinate getrotateco(Coordinate basepoint, Coordinate startpoint, double angle) {
        BlhToGauss blhToGauss = new BlhToGauss(basepoint, 3);
        Coordinate startgs = blhToGauss.BLHtoGauss(basepoint);
        Coordinate nextgs = blhToGauss.BLHtoGauss(startpoint);
        Coordinate en = new Coordinate(nextgs.getX() - startgs.getX(), nextgs.getY() - startgs.getY());
        double radians = Math.toRadians(angle); // 将角度转换为弧度
        double newX = en.getX() * Math.cos(radians) - en.getY() * Math.sin(radians);
        double newY = en.getX() * Math.sin(radians) + en.getY() * Math.cos(radians);
        Coordinate enr = new Coordinate(startgs.getX() + newX, startgs.getY() + newY);
        Coordinate enrblh = blhToGauss.GaussToBLH(enr);
        return enrblh;
    }

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //配置文件 三期地址
//        String basepackage = SimulationTest.class.getClassLoader().getResource("").getFile() + "resources/sq";//三期地址
//        String[][] runwayno = new String[][]{{"05L", "23R"}, {"05R", "23L"}, {"06L", "24R"}, {"06R", "24L"}};//三期跑道信息
//        String starttime = "2024-01-05 12:40:00";//仿真起始时间
//        String endtime = "2024-01-05 18:30:00";//仿真终止时间
//        String excelsheetname = "2024-01-05";
//        String flightplan=basepackage + "/" + "flightplan.xls";
//        String flightsroads = basepackage + "/" + "flightsroads.txt";//路径规划结果文件
//        String filepathdata = null;


//        //配置文件 二期地址
//        String basepackage = SimulationTest.class.getClassLoader().getResource("").getFile() + "resources/eq_";//二期地址
//        String[][] runwayno = new String[][]{{"05L", "23R"}, {"05R", "23L"}};//二期跑道信息
//        String starttime = "2024-06-06 13:30:00";//仿真起始时间
//        String endtime = "2024-06-06 16:00:00";//仿真终止时间
//        String excelsheetname = "2024-06-06";
//        String flightplan=basepackage + "/" + "new_flight_plan.xls";
////        String flightplan=basepackage + "/" + "flightplan.xls";
//        String flightsroads = basepackage + "/" + "flightsroads.txt";//路径规划结果文件
//        String filepathdata = null;
////        String filepathdata = basepackage + "/" + "ADSB_多点.xls";


//        //配置文件 二期地址  节点demo样例
//        String basepackage = SimulationTest.class.getClassLoader().getResource("").getFile() + "resources/eq_";//二期验证
//        String[][] runwayno = new String[][]{{"05L", "23R"}, {"05R", "23L"}};//二期跑道信息
//        String starttime = "2024-01-01 08:00:00";//仿真起始时间
//        String endtime = "2024-01-01 13:00:00";//仿真终止时间
//        String  flightplan=basepackage + "/" + "flightplan_20240101_2.xls";
//        String  excelsheetname = "2024-01-01";
//        String flightsroads = basepackage + "/" + "flightsroads.txt";//路径规划结果文件


//        //配置文件 孟锡瑞验证样例

//        String basepackage = SimulationTest.class.getClassLoader().getResource("").getFile() + "resources/eq_";//二期验证
        String basepackage = "./resources/eq_";//二期验证
        String[][] runwayno = new String[][]{{"05L", "23R"}, {"05R", "23L"}};//二期跑道信息
        String starttime = "2024-03-12 14:38:26";//仿真起始时间
        String endtime = "2024-03-12 16:30:00";//仿真终止时间
        String flightplan = basepackage + "/" + "flightplanwcl.xls";
        String excelsheetname = "2024-03-12";

        //！！！！！！！！！！这里是从文件里面读取路径规划结果，要改为从Redis 或 Mysql里面读取！！！！！！！！！！！！！！
        String flightsroads = basepackage + "/" + "flightsroadswcl.txt";//路径规划结果文件

        String filepathdata = basepackage + "/" + "ADSB_多点.xls";
        // String filepathdata = null;
//        //配置文件 夜晚速度调试
//        String basepackage = SimulationTest.class.getClassLoader().getResource("").getFile() + "resources/eq_";
//        String[][] runwayno = new String[][]{{"05L", "23R"}, {"05R", "23L"}};//二期跑道信息
//        String starttime = "2024-06-06 00:00:00";//仿真起始时间
//        String endtime = "2024-06-06 15:30:58";//仿真终止时间
//        String flightplan = basepackage + "/" + "flightplan_night.xls";
//        String excelsheetname = "2024-06-06";
//        String flightsroads = basepackage + "/" + "flightsroads.txt";//路径规划结果文件
//        String filepathdata = basepackage + "/" + "ADSB_多点.xls";


        //配置仿真基本信息   //删除了坐标转换模型
        double[] gearaltAD = new double[]{100, 100};//打开起落架和关闭起落架的高度
        int retreatBridgeBefore = 15;//离港飞机提前15s撤侨
        int T = 1;//仿真使劲按步长
        JSONObject waitmes = JSONObject.parseObject("{\"pushout\":60,\"zbd\":10,\"taxi\":0}");//各类型的点等待时长
        JSONObject runwayflightnum = JSONObject.parseObject("{\"05L\":10,\"05R\":10,\"23L\":10,\"23R\":10}");//各个跑道的出港飞机出现在仿真场景中的数量上限，简易流量约束。
        //依赖文件的地址
        String network = basepackage + "/" + "network.shp";//路网数据
        String runwaypoints = basepackage + "/" + "runwaypoints.shp";//跑道点
        String standpoints = basepackage + "/" + "standpoints.shp";//机位点
        String inoutpoints = basepackage + "/" + "inoutpoints.shp";//入离位点文件,可通过CreateInoutPoints方法自动获取
        String insertpoints = basepackage + "/" + "insertpoints.shp";//机位点文件
        String taxipoints = basepackage + "/" + "taxipoints.shp";//移交点文件
        String polygon_runwayocuppy = basepackage + "/" + "polygon_runwayocuppy.shp";//跑道占用区域
        String polygon_flypolygons = basepackage + "/" + "polygon_flypolygons.shp";//起飞面
        String aircrafttypes = basepackage + "/" + "aircrafttypes.xls";//机型信息
        String standlimitfilepath = basepackage + "/" + "stopandglidejw.xls";//机型信息
        String runwayDrulerfilepath = basepackage + "/" + "runwayDruler.xls";//机型信息
        String priorareas = basepackage + "/" + "priorareas.xls";//
        String flightspeednodes = basepackage + "/" + "modifiedspeed.json";
        //机位约束关系文件

        //读取数据
        ReadandWriteFile readandWriteFile = new ReadandWriteFile();
        Map<String, StandNodes> standMes = readandWriteFile.initStandPoints(readandWriteFile.readFile(standpoints, "UTF-8"));
        List<RoadLine> roadLines = readandWriteFile.initFloydDataFromShapfle(readandWriteFile.readFile(network, "UTF-8"));

        List<RunwayNodes> runwaynodes = readandWriteFile.updateRunwayPoints(readandWriteFile.readFile(runwaypoints, "UTF-8"));
        Map<String, Map<String, List<Coordinate>>> polygon_runwayocuppys = readandWriteFile.getRunwayoccupy(polygon_runwayocuppy, "UTF-8");
        Map<String, Map<String, List<Coordinate>>> polygon_flypolygonss = readandWriteFile.getRunwayoccupy(polygon_flypolygons, "UTF-8");

        //新增，无文件
        List<InoutNodes> inoutNodes = readandWriteFile.readOutPoints(readandWriteFile.readFile(inoutpoints, "UTF-8"));
        Map<String, InsertNodes> insertNodes = readandWriteFile.initInsertPoints(readandWriteFile.readFile(insertpoints, "UTF-8"));
        Map<String, List<TaxiNodes>> TaxiPoints = readandWriteFile.initTaxiPoints(readandWriteFile.readFile(taxipoints, "UTF-8"));

        List<JSONObject> standlimit = readandWriteFile.getJSONByExcel(standlimitfilepath, "Sheet1");
        List<JSONObject> aircraftS = readandWriteFile.getJSONByExcel(aircrafttypes, "aircrafttype");
        List<JSONObject> releaseByRunwayno = readandWriteFile.getJSONByExcel(runwayDrulerfilepath, "releaseByRunwayno");
        List<JSONObject> airlineRelease = readandWriteFile.getJSONByExcel(runwayDrulerfilepath, "airlineRelease");
        List<JSONObject> releaseByAircraft = readandWriteFile.getJSONByExcel(runwayDrulerfilepath, "releaseByAircraft");
        List<JSONObject> releaseByInoutflag = readandWriteFile.getJSONByExcel(runwayDrulerfilepath, "releaseByInoutflag");
        Map<String, List<FlyAndSpeedNodes>> initflightspeednodesmap = readandWriteFile.readJsonRunwaySpeedAndAlt(flightspeednodes);

        List<RunwayTakeoffandDown> runwayTakeoffandDowns = new ArrayList<>();
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("23L", "D", "D"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("23L", "A", "A"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("05R", "D", "D"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("05R", "A", "A"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("23R", "D", "DR15"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("23R", "A", "A"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("05L", "D", "DL15"));
        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("05L", "A", "A"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("19", "A", "A"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("01", "A", "A"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("24L", "D", "D"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("24L", "A", "A"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("06R", "D", "D"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("06R", "A", "A"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("24R", "D", "D"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("24R", "A", "A"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("06L", "D", "D"));
//        runwayTakeoffandDowns.add(new RunwayTakeoffandDown("06L", "A", "A"));

        List<JSONObject> weightlines = readandWriteFile.getJSONByExcel(priorareas, "weightlines");//增加权重文件参数
        List<JSONObject> trailinglines = readandWriteFile.getJSONByExcel(priorareas, "trailinglines");
        List<JSONObject> calareas = readandWriteFile.getJSONByExcel(priorareas, "calareas");
        List<JSONObject> flightslists = readandWriteFile.getJSONByExcel(flightplan, excelsheetname);

        //！！！！！！！！！！！读取路径规划文件！！！！！！！！！！！！
        List<JSONObject> roadjson = readandWriteFile.getJsonByTxt(flightsroads);
        //我需要在这里做的事情就是类似List<JSONObject> roadjson2 = Connection.queryRoads(flightsroads);

        //热力图左下角，右上角坐标及x,y方向上的步长
        EvalueMes evalueMes = new EvalueMes(sdf, new Coordinate(108.742, 34.420), new Coordinate(108.784, 34.469), 0.0001, 0.0001);

        //实例化simulationTest
        SimulationTest simulationTest = new SimulationTest(roadLines, sdf, standlimit, weightlines, roadjson, runwaynodes, releaseByAircraft, airlineRelease, releaseByInoutflag, aircraftS, releaseByRunwayno, flightslists,
                standMes, runwayno, calareas, trailinglines, waitmes, initflightspeednodesmap, runwayTakeoffandDowns, insertNodes, TaxiPoints, inoutNodes);

        //实例化simulation
        Simulation realDataHandle = new Simulation(sdf, simulationTest.aircraftMap, standMes,
                polygon_runwayocuppys.get("maxrunwayarea"), polygon_flypolygonss,
                simulationTest.runwaymes, retreatBridgeBefore, evalueMes, T, simulationTest.runwayReleaseRule,
                simulationTest.weightlinesmap, simulationTest.boundrymap, gearaltAD, simulationTest.runwayTakeoffandDownMap, simulationTest.trailingNumLimitMap, simulationTest.trailingconflictmaps, simulationTest.a);

        if (filepathdata != null) {
            //实时数据接入
            List<Coordinate> boundrys = new ArrayList<>();
            boundrys.add(new Coordinate(108.82075320444551, 34.57361038896545));
            boundrys.add(new Coordinate(108.89313170640395, 34.51002672940024));
            boundrys.add(new Coordinate(108.64538692831582, 34.333604071728249));
            boundrys.add(new Coordinate(108.59341949994848, 34.40593968630537));
            boundrys.add(new Coordinate(108.82075320444551, 34.57361038896545));
            ////////////////////////////////////////////////////////////////////////////
            Map<Long, List<Flight>> hisdata = simulationTest.readhistorydata(filepathdata, "ADSB_多点", sdf, boundrys);//读取历史位置数据excel

            ///////////////////////////////////////////////////////////////////////////
            long miao = 0;
            try {
                miao = sdf.parse(starttime).getTime() / 1000;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Map<String, Flight> init = simulationTest.simulationdatalinit.getRealFlightByTime(hisdata, miao);//提取到初始时间的所有位置，
            init = simulationTest.simulationdatalinit.matchRoadlines(init, simulationTest.flights, standMes, simulationTest.runwayTakeoffandDownMap);//并对其进行信息匹配，匹配路径信息，跑道，机位
            //hisdirectrunwaymap0里面是当前的电子围栏框住的所有的进出港飞机，包含了：“在电子围栏之内且上了跑到的” + “在电子围栏之内还没有上跑道的”
            Map<String, List<RunwayFlightHis>> hisdirectrunwaymap0 = simulationTest.simulationdatalinit.getRunwayQueneMap(hisdata, init, miao, polygon_runwayocuppys.get("maxrunwayarea"));//初始化跑道排队信息
            System.out.println("仿真开始时间：" + new Date());

            String timeHisdirectrunway_xls_path = basepackage + "/" + "timeHisdirectrunway.xls";//恢复跑道冲突关系文件
            //timeHisdirectrunway_xls_json里面是当前时刻之前，所有占据跑道的进出港飞机，包含了：“在电子围栏之内且上了跑道的” + “已经离开跑道的历史的飞机”
            List<JSONObject> timeHisdirectrunway_xls_json = readandWriteFile.getJSONByExcel(timeHisdirectrunway_xls_path, "timeHisdirectrunway");
            //要求的是“在电子围栏之内且上了跑道的”,这里要实现去重方法，把List<JSONObject> 转成 Map<String, List<RunwayFlightHis>>，如果hisdirectrunwaymap中的元素在
            // hisdirectrunwaymap0中存在，以如果hisdirectrunwaymap中的时间为准，因为hisdirectrunwaymap0存的是ADSB文件里面的时间
            Map<String, List<RunwayFlightHis>> hisdirectrunwaymap = hisdirectrunwaymap0;

            ////////////////////////读取timeHisdirectrunway.xls在这里！！！！！！！//////////////////////////////////
            ////////////////////////返回Map<String, List<RunwayFlightHis>>类型！！！！！！！！！！！！ ////////////////////////
            //////Map<String, List<RunwayFlightHis>> hisdirectrunwaymap = simulationTest.simulationdatalinit.getRunwayQueneMap(hisdata, init, miao, polygon_runwayocuppys.get("maxrunwayarea"));

            realDataHandle.disposeRealData(hisdata, init, hisdirectrunwaymap, simulationTest.flights, starttime, endtime, runwayflightnum);//调用仿真接口

        } else {
            Map<Long, List<Flight>> hisdata = new HashMap<>();
            Map<String, Flight> init = null;
            Map<String, List<RunwayFlightHis>> hisdirectrunwaymap = null;
            System.out.println("仿真开始时间：" + new Date());
            realDataHandle.disposeRealData(hisdata, init, hisdirectrunwaymap, simulationTest.flights, starttime, endtime, runwayflightnum);//调用仿真接口
        }

        Logger log = LoggerFactory.getLogger(SimulationTest.class);
        ExcelWriter excelWriter = new ExcelWriter(log);

        excelWriter.createRunwayQueneexcel(basepackage + "/" + "runwayConflictByQuene.xls", "runwayConflictByQuene", realDataHandle.hisTimeNodesHashMap);
        excelWriter.createRunwayQueneexcel2(basepackage + "/" + "timeHisdirectrunway.xls", "timeHisdirectrunway", realDataHandle.timeHisdirectrunwaymap);

        System.out.println("仿真结束时间：" + new Date());

        //文件关闭，清洗冲突
        Map<String, JSONObject> map = new HashMap<>();
        for (String item : realDataHandle.orders) {
            if (item.equals("")) {
                continue;
            }
            JSONObject jsonObject = JSONObject.parseObject(item);
            map.put(jsonObject.getString("key"), jsonObject);
        }
        for (Map.Entry<String, JSONObject> entry : map.entrySet()) {
            String time = entry.getValue().getString("duretime");
            String type = entry.getValue().getString("type");
            if ((type.equals("trailing") || type.equals("cross") || type.equals("confrontation")) && (time.equals("00:00") || time.equals("00:01") || time.equals("00:02"))) {
                System.out.println("噪声" + ":" + entry.getValue());
            } else {
                if (time == null || time.equals("")) {
                    System.out.println("没有时间：" + entry.getValue());
                    map.remove(entry);
                } else {
                    System.out.println(entry.getValue());
                }
            }
        }
        System.out.println("清洗结束时间：" + new Date());
    }//main

    public static void createFile(File fileName) {
        try {
            if (!fileName.exists()) {
                fileName.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  List<FlyAndSpeedNodes> getflyAndSpeedNodesList( RunwayTakeoffandDown runwayTakeoffandDown ){
        RunwayNodes dds = runwaymes.get(runwayTakeoffandDown.runway).getAllsides().get("dds").get(0);
        RunwayNodes dde = runwaymes.get(runwayTakeoffandDown.runway).getAllsides().get("dde").get(0);
        Coordinate center = new Coordinate((dds.getCoordinate().getX() + dde.getCoordinate().getX()) / 2, (dds.getCoordinate().getY() + dde.getCoordinate().getY()) / 2);
        RunwayNodes jgd = runwaymes.get(runwayTakeoffandDown.runway).getAllsides().get("jgd").get(0);
        RunwayNodes lgd = runwaymes.get(runwayTakeoffandDown.runway).getAllsides().get("lgd").get(0);
        RunwayNodes mzd = runwaymes.get(runwayTakeoffandDown.runway).getAllsides().get("mzd").get(0);
        List<FlyAndSpeedNodes> flyAndSpeedNodesList = new ArrayList<>();
        if (runwayTakeoffandDown.inoutflag.equals("A")) {
            BlhToGauss blhToGauss = new BlhToGauss();
            double dis = blhToGauss.getBLHDistanceByHaverSine(jgd.getCoordinate(), mzd.getCoordinate());
            double ave = dis / (3 * 60);
            double mzdspeed = 66.7;
            double jgdspeed = 2 * ave - 66.7;
            FlyAndSpeedNodes jgdnodes = new FlyAndSpeedNodes(jgd.getCoordinate().getX(), jgd.getCoordinate().getY(),
                    0, jgdspeed, 0, 670, 0, 0, 100);
            FlyAndSpeedNodes mzdnode = new FlyAndSpeedNodes(mzd.getCoordinate().getX(), mzd.getCoordinate().getY(),
                    0, mzdspeed, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddcenternode = new FlyAndSpeedNodes(center.getX(), center.getY(),
                    0, 17, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddenode = new FlyAndSpeedNodes(dde.getCoordinate().getX(), dde.getCoordinate().getY(),
                    0, 12, 0, 0, 0, 0, 30);
            flyAndSpeedNodesList.add(jgdnodes);
            flyAndSpeedNodesList.add(mzdnode);
            flyAndSpeedNodesList.add(ddcenternode);
            flyAndSpeedNodesList.add(ddenode);
        } else if (runwayTakeoffandDown.inoutflag.equals("D")) {
            FlyAndSpeedNodes ddsnode = new FlyAndSpeedNodes(dds.getCoordinate().getX(), dds.getCoordinate().getY(),
                    0, 3, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddcenternode = new FlyAndSpeedNodes(center.getX(), center.getY(),
                    0, 67.7, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddenode = new FlyAndSpeedNodes(dde.getCoordinate().getX(), dde.getCoordinate().getY(),
                    0, 80, 0, 300, 0, 0, 30);
            FlyAndSpeedNodes lgdnodes = new FlyAndSpeedNodes(lgd.getCoordinate().getX(), lgd.getCoordinate().getY(),
                    0, 120, 0, 1500, 0, 0, 100);
            flyAndSpeedNodesList.add(ddsnode);
            flyAndSpeedNodesList.add(ddcenternode);
            flyAndSpeedNodesList.add(ddenode);
            flyAndSpeedNodesList.add(lgdnodes);
        } else if (runwayTakeoffandDown.inoutflag.equals("DL15")) {
            FlyAndSpeedNodes ddsnode = new FlyAndSpeedNodes(dds.getCoordinate().getX(), dds.getCoordinate().getY(),
                    0, 3, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddcenternode = new FlyAndSpeedNodes(center.getX(), center.getY(),
                    0, 67.7, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddenode = new FlyAndSpeedNodes(dde.getCoordinate().getX(), dde.getCoordinate().getY(),
                    0, 80, 0, 300, 0, 0, 30);
            Coordinate enrblh = getrotateco(dde.getCoordinate(), lgd.getCoordinate(), -15);
            FlyAndSpeedNodes lgdnodes = new FlyAndSpeedNodes(enrblh.getX(), enrblh.getY(),
                    0, 120, 0, 1500, 0, 0, 100);
            flyAndSpeedNodesList.add(ddsnode);
            flyAndSpeedNodesList.add(ddcenternode);
            flyAndSpeedNodesList.add(ddenode);
            flyAndSpeedNodesList.add(lgdnodes);

        } else if (runwayTakeoffandDown.inoutflag.equals("DR15")) {
            FlyAndSpeedNodes ddsnode = new FlyAndSpeedNodes(dds.getCoordinate().getX(), dds.getCoordinate().getY(),
                    0, 3, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddcenternode = new FlyAndSpeedNodes(center.getX(), center.getY(),
                    0, 67.7, 0, 0, 0, 0, 30);
            FlyAndSpeedNodes ddenode = new FlyAndSpeedNodes(dde.getCoordinate().getX(), dde.getCoordinate().getY(),
                    0, 80, 0, 300, 0, 0, 30);
            Coordinate enrblh = getrotateco(dde.getCoordinate(), lgd.getCoordinate(), 15);
            FlyAndSpeedNodes lgdnodes = new FlyAndSpeedNodes(enrblh.getX(), enrblh.getY(),
                    0, 120, 0, 1500, 0, 0, 100);
            flyAndSpeedNodesList.add(ddsnode);
            flyAndSpeedNodesList.add(ddcenternode);
            flyAndSpeedNodesList.add(ddenode);
            flyAndSpeedNodesList.add(lgdnodes);
        }
        return flyAndSpeedNodesList;
    }
    public static JSONObject readJson(String filePathOrder, String FilePathPositon) {
        JSONArray orders = new JSONArray();
        BufferedReader br = null;
        int i = 0;
        try {
            br = new BufferedReader(new FileReader(filePathOrder));
            String str = br.readLine();
            while (str != null) {
                try {
                    orders.add(JSONObject.parseObject(str));
                    try {
                        str = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    try {
                        throw new Exception("非Json格式数据异常:" + str + "\n");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                i++;
            }
        } catch (IOException e) {
            try {
                throw new Exception("文件异常！");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        JSONArray position = new JSONArray();
        BufferedReader brp = null;
        try {
            brp = new BufferedReader(new FileReader(FilePathPositon));
            String str = brp.readLine();
            while (str != null) {
                try {
                    position.add(JSONObject.parseObject(str));
                    try {
                        str = brp.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    try {
                        throw new Exception("非Json格式数据异常:" + str + "\n");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            try {
                throw new Exception("文件异常！");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("order", orders);
        jsonObject.put("position", position);
        return jsonObject;
    }

    public Map<Long, List<Flight>> readhistorydata(String filepath, String sheetname, SimpleDateFormat sdf, List<Coordinate> boundrys) {
        Map<Long, List<Flight>> hisdata = new HashMap<>();

        AngDisUtil angDisUtil = new AngDisUtil();
        Workbook mesbook = null;
        try {
            mesbook = Workbook.getWorkbook(new File(filepath));
        } catch (IOException var12) {
            try {
                throw new Exception("读取excel失败");
            } catch (Exception e) {
                e.printStackTrace();
            }
            var12.printStackTrace();
        } catch (BiffException var13) {
            try {
                throw new Exception("读取excel失败");
            } catch (Exception e) {
                e.printStackTrace();
            }
            var13.printStackTrace();
        }
        Sheet sheet = mesbook.getSheet(sheetname);
        for (int i = 1; i < sheet.getRows(); ++i) {
            Flight flight = revertToJson(sheet.getRow(i), sheet.getRow(0));
            boolean isinpolygon = angDisUtil.IsinPolygon(boundrys, new Coordinate(flight.getX(), flight.getY()));
            if (isinpolygon) {
                long flighttime = 0;
                try {
                    flighttime = sdf.parse(flight.getTime()).getTime() / 1000;
                } catch (ParseException var12) {
                    var12.printStackTrace();
                }
                List<Flight> flights = hisdata.get(flighttime) == null ? new ArrayList<>() : hisdata.get(flighttime);
                boolean isin = false;
                for (int j = 0; j < flights.size(); j++) {
                    if (flight.getFlightId().equals(flights.get(j).getFlightId())) {
                        isin = true;
                    }
                }
                if (!isin) {
                    flights.add(flight);
                    hisdata.put(flighttime, flights);
                }
            }
        }
        return hisdata;
    }


    private Flight revertToJson(Cell[] cells0, Cell[] cells) {
        JSONObject jsonObject = new JSONObject();
        if (cells.length == cells0.length) {
            for (int j = 0; j < cells.length; j++) {
                jsonObject.put(cells[j].getContents(), cells0[j].getContents());
            }
        } else {
            for (int j = 0; j < cells0.length; j++) {
                jsonObject.put(cells[j].getContents(), cells0[j].getContents());
            }
            for (int j = cells0.length; j < cells.length; j++) {
                jsonObject.put(cells[j].getContents(), "");
            }
        }
        Flight flight = new Flight(jsonObject.getString("DFID"),
                jsonObject.getString("FLIGHTNO"),
                jsonObject.getString("RUNWAYNAME"),
                jsonObject.getString("STAND"),
                jsonObject.getString("STATE"),

                jsonObject.getString("AIRCRAFT") == "" ? "320" : jsonObject.getString("AIRCRAFT"),
                jsonObject.getString("ADSBTIME"),
                jsonObject.getDouble("GEOX"),
                jsonObject.getDouble("GEOY"),
                jsonObject.getDouble("GEOH"),
                jsonObject.getDouble("DIRECTION"),
                jsonObject.getDouble("SPEED"),
                "Y",
                "",
                "");
        return flight;
    }
}
