package com.conflict;



import base.AngDisUtil;
import base.BlhToGauss;
import com.agent.Aircraft;
import com.agent.Flight;
import fun.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.*;


import org.geotools.geometry.jts.JTSFactoryFinder;



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @description: ***
 * @author: Meng Xirui
 * @time: 2024-07-12 14:16
 */
/*
    1、读取保护罩信息存入List<Coordinate>的
    2、读取飞机机位信息和推出滑行道的点信息（机位中线、in/out/inout line）
    3、沿着线上的点对保护罩形状的多边形进行重建，获取多个保护罩多边形信息。
    4、合并多边形构建新的保护罩。
    5、positive沿路的方向，路网方向为点的顺序。reverse为相反的方向

 */
public class AircraftPushbackConflict {
  AngDisUtil angDisUtil=new AngDisUtil();
  BlhToGauss blhToGauss=new BlhToGauss();
    Map<String,List<Coordinate>> ProtectionCoverPolygon = new HashMap<>();//机型-保护罩坐标
    Map<String,List<List<Coordinate>>> StandNearPoints = new HashMap<>();//机位-周围线的点坐标
//做两个点之间的中点，角度用这两个点的的角度
    public AircraftPushbackConflict() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftPushbackConflict that = (AircraftPushbackConflict) o;
        return Objects.equals(ProtectionCoverPolygon, that.ProtectionCoverPolygon);
    }
    public Map<String, List<Coordinate>> getProtectionCoverPolygon() {
        return ProtectionCoverPolygon;
    }

    public void setProtectionCoverPolygon(Map<String, List<Coordinate>> protectionCoverPolygon) {
        ProtectionCoverPolygon = protectionCoverPolygon;
    }

    public  void writeCoordinateincsv(Coordinate[] Coordinates,int i){
        String filePath = "C:\\Users\\mxr\\Desktop\\新建文件夹1\\"+i+"coordinates.csv";

        // 使用 try-with-resources 语句确保资源正确关闭
        try (FileWriter fileWriter = new FileWriter(filePath);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println("X,Y");
            // 遍历 Coordinate 数组
            for (Coordinate coordinate : Coordinates) {
                // 将每个坐标写入 CSV 文件，格式为 x,y
                printWriter.println(coordinate.x + "," + coordinate.y);
            }

            System.out.println("CSV 文件已生成: " + filePath);
        } catch (IOException e) {
            System.err.println("写入 CSV 文件时发生错误: " + e.getMessage());
        }
    }//Corrdinate[]格式数据写入csv文件
    public void readAircraftProtectionCover(Map<String, Aircraft> aircraftMap){//获取机位处的机型保护罩信息
        for(Map.Entry<String, Aircraft> entry : aircraftMap.entrySet()){
            String key = entry.getKey();
            List<Coordinate> coordinate = entry.getValue().getProtects().get("apron");
            ProtectionCoverPolygon.put(key,coordinate);
        }
    }
    public void getStandInoutLineNode(Map<String, StandNodes> standMes,List<RoadLine> roadLines){//获取机位点周围的线的点坐标
        Map<String,List<RoadLine>> StandNearLines = new HashMap<>();//获取机位附近的线路
        for(Map.Entry<String,StandNodes> entry : standMes.entrySet()){
            String key = entry.getKey();
            StandNearLines.put(key,null);
        }
        for (Map.Entry<String,List<RoadLine>> entry : StandNearLines.entrySet()){//获取机位点及其周围的线
            List<RoadLine> Lines = new ArrayList<>();
            for(int i=0;i<roadLines.size();i++){
                String name = roadLines.get(i).getName();
                 if(roadLines.get(i).getNetworkname().equals("apron") && (!roadLines.get(i).getType().equals("inline"))){
                    if(name.contains("_")){
                        String[] n =name.split("_");
                        if(n[0].equals(entry.getKey())||n[1].equals(entry.getKey())){
//                            RoadLine r = roadLines.get(i);
                            Lines.add(roadLines.get(i));
                        }
                    }
                    else{
                        if(name.equals(entry.getKey())){
//                            RoadLine r = roadLines.get(i);
                            Lines.add(roadLines.get(i));
                        }
                    }
                }
            }
            StandNearLines.put(entry.getKey(),Lines);
        }
        for(Map.Entry<String,List<RoadLine>> entry : StandNearLines.entrySet()){
            List<RoadLine> RoadLines = entry.getValue();
            List<List<Coordinate>> Coordinateslist = new ArrayList<>();

            for (int i=0;i<RoadLines.size();i++){
                List<Coordinate> coorlist = new ArrayList<>();
                RoadLine roadLine=RoadLines.get(i);
                Coordinate[] coordinate= roadLine.getGeometry().getCoordinates();;
                for (int j=0;j<coordinate.length;j++){
                    coorlist.add(coordinate[j]);
                }
                Coordinateslist.add(coorlist);
            }
            StandNearPoints.put(entry.getKey(),Coordinateslist);
        }
    }

    public Map<String ,List<List<Coordinate>>> getNearStandRoadNode(Flight flight){

        List<RoadLine> roadLines = flight.getRoadlines();
        List<RoadLine> Lines = new ArrayList<>();
        Map<String ,List<List<Coordinate>>> roadnamelinenode = new HashMap<>();
        List<List<Coordinate>> listlist = new ArrayList<>();
        for(int i =0 ;i<roadLines.size();i++){
            String name = roadLines.get(i).getName();
            if(roadLines.get(i).getNetworkname().equals("apron") && (!roadLines.get(i).getType().equals("inline"))){
                if(name.contains("_")){
                    String[] n =name.split("_");
//                            RoadLine r = roadLines.get(i);
                    Lines.add(roadLines.get(i));
                }
                else{
//                            RoadLine r = roadLines.get(i);
                    Lines.add(roadLines.get(i));
                }
            }
            else {
                Lines.add(roadLines.get(i));
                break;
            }
        }

        for(int i=0;i<Lines.size();i++){
            RoadLine line = Lines.get(i);
            Coordinate[] linenodes = line.getGeometry().getCoordinates();
            List<Coordinate>coordinates=new ArrayList<>();
            if(line.getNetworkname().equals("apron")){
                if(line.getDirection().equals("reverse")){
                    if(linenodes.length==2){
                        coordinates.add(linenodes[0]);
                        coordinates.add(new Coordinate((linenodes[0].getX()+linenodes[1].getX())/2,(linenodes[0].getY()+linenodes[1].getY())/2));
                        coordinates.add(linenodes[1]);
                    }
                    else {
                        for(int j=linenodes.length-1;j>=0;j--){
                            coordinates.add(linenodes[j]);
                        }
                    }
                }
                else {
                    for(int j = 0;j<linenodes.length;j++){
                        coordinates.add(linenodes[j]);
                    }
                }
            }
            else {
                if(line.getDirection().equals("reverse")){
                    listlist.get(listlist.size()-1).add(linenodes[linenodes.length-1]);
                    break;
                }
                else {
                    listlist.get(listlist.size()-1).add(linenodes[0]);
                    break;
                }
            }
            listlist.add(coordinates);
        }
        roadnamelinenode.put(flight.getStand(),listlist);
        return roadnamelinenode;
    }
    public Map<String,List<Coordinate>> DrawPolygonShape(String key,Flight flight){
        Map<String,List<Coordinate>> NewPolygons = new HashMap<>();//最终结果多边形的多边形。
        Geometry n = null;
        Polygon newpo = null;
        List<List<Coordinate>> drawpolygons = new ArrayList<>();//新画的多个多边形
        String aircrafttype =flight.getAcft();
        String stand = flight.getStand();
        for(Map.Entry<String,List<Coordinate>> entry1: ProtectionCoverPolygon.entrySet()){
            if(entry1.getKey().equals(aircrafttype)){
                List<Coordinate> protectionpolygon = entry1.getValue();
                List<List<Coordinate>> standnearpoint = new ArrayList<>();
                for (Map.Entry<String,List<List<Coordinate>>> entry2:getNearStandRoadNode(flight).entrySet()){//StandNearPoints可以换getNearStandRoadNode
                    if(entry2.getKey().equals(stand)){
                        standnearpoint = entry2.getValue();//获取到了飞机机位附近的点坐标。中心点
                        for(int i=0;i<standnearpoint.size();i++){
                            if(standnearpoint.get(i).size()==2){
                                List<Coordinate> coordinateList = standnearpoint.get(i);
                                Coordinate coordinate12 = new Coordinate((standnearpoint.get(0).get(0).x+standnearpoint.get(0).get(1).x)/2,(standnearpoint.get(0).get(0).y+standnearpoint.get(0).get(1).y)/2);
                                Coordinate coordinate14 = new Coordinate((standnearpoint.get(0).get(0).x+coordinate12.x)/2,(standnearpoint.get(0).get(0).y+coordinate12.y)/2);
                                Coordinate coordinate34 = new Coordinate((coordinate12.x+standnearpoint.get(0).get(1).x)/2,(coordinate12.y+standnearpoint.get(0).get(1).y)/2);
                                coordinateList.add(standnearpoint.get(0).get(0));
                                coordinateList.add(coordinate14);
                                coordinateList.add(coordinate12);
                                coordinateList.add(coordinate34);
                                coordinateList.add(standnearpoint.get(0).get(1));
                                standnearpoint.set(i,coordinateList);
                            }
                            for(int j = 1;j<standnearpoint.get(i).size();j++){
                                List<Coordinate> xylist =new ArrayList<>();//根据两个点确定中心点和方向
                                Coordinate middlenode = new Coordinate((standnearpoint.get(i).get(j-1).x+standnearpoint.get(i).get(j).x)/2,(standnearpoint.get(i).get(j-1).y+standnearpoint.get(i).get(j).y)/2);
                                double dirction =blhToGauss.getAngleByBlh(standnearpoint.get(i).get(j-1),standnearpoint.get(i).get(j));
                                Coordinate centergs=blhToGauss.BLHtoGauss(middlenode);
                                for(int k=0;k< protectionpolygon.size();k++){
                                    Coordinate bundry = protectionpolygon.get(k);
                                    double xgs = centergs.getX() + bundry.getX() * Math.cos(dirction) - bundry.getY() * Math.sin(dirction);
                                    double ygs = centergs.getY() + bundry.getX() * Math.sin(dirction) + bundry.getY() * Math.cos(dirction);
                                    xylist.add(blhToGauss.GaussToBLH(new Coordinate(xgs, ygs)));
                                }
                                drawpolygons.add(xylist);
                            }

                        }
                    }
                }
            }
        }
        GeometryFactory gf=JTSFactoryFinder.getGeometryFactory(null);
        List<Polygon> g = new ArrayList<>();
        for(int i=0;i<drawpolygons.size();i++){
            Coordinate[] array = new Coordinate[drawpolygons.get(i).size()+1];
            for (int j = 0; j < drawpolygons.get(i).size()+1; j++) {
                if(j>=drawpolygons.get(i).size()){
                    array[j]=drawpolygons.get(i).get(0);
                }else {
                    array[j] = drawpolygons.get(i).get(j);
                }
            }
            g.add(gf.createPolygon(array));
        }

        List<Polygon> temlist = new ArrayList<>();
//        for(int i=1;i<g.size();i++){
//            n = newpo.union(g.get(i));
//            if(n instanceof Polygon){
//                newpo = (Polygon) n;
//            }
//            else{
//                temlist.add(g.get(i));
//            }
////            Coordinate[] aaa=newpo.getCoordinates();
////            writeCoordinateincsv(aaa,i);
//        }
        int count=0;
        temlist = g;
        boolean flag=false;
        while (!temlist.isEmpty()){
            if(newpo==null){
                newpo = temlist.get(0);
            }
            if(flag==true){
                temlist.add(newpo);
                newpo = temlist.get(0);
                temlist.remove(temlist.get(0));
                if(temlist.size()==1)break;

            }
            n=newpo.union(temlist.get(0));
            if(n instanceof Polygon){
                newpo = (Polygon) n;
//                Coordinate[] aaa=newpo.getCoordinates();
//                writeCoordinate1incsv(aaa,g.size()-temlist.size());
                temlist.remove(temlist.get(0));
            }
            else {
                if(count<temlist.size()){
                    count++;
                    Polygon a = temlist.get(0);
                    temlist.remove(temlist.get(0));
                    temlist.add(a);
                }
                else {
                    count=0;
                    flag=true;
                }

            }
        }

        Coordinate[] outpol = newpo.convexHull().getCoordinates();
        List<Coordinate> cl = new ArrayList<>();
        for(int i=0;i<outpol.length;i++){
            cl.add(outpol[i]);
        }
        NewPolygons.put(key,cl);
        return NewPolygons;
    }


    //改了Simulation 方法的参数
    public List<Map<String,List<Coordinate>>> DrawPolygonBynodes(Map<String, Flight> real){
        List <Map<String,List<Coordinate>>> list = new ArrayList<>();//最终结果多边形的多边形。
        for(Map.Entry<String,Flight> entry: real.entrySet()){
            if(entry.getValue().getInOutFlag().equals("D")&&entry.getValue().getNetworkname().equals("apron")){
              // list.add(DrawPolygonShape(entry.getKey(),entry.getValue()));
                list.add(DrawRectangleWithProtection(entry.getKey(),entry.getValue()));
            }
        }

        return list;
    }
    public Map<String, List<Coordinate>> DrawRectangleWithProtection(String key, Flight flight) {
//        Map<String, List<Coordinate>> rectanglesWithProtection = new HashMap<>();
        String aircrafttype =flight.getAcft();
        String stand = flight.getStand();
        List<List<Coordinate>> rectangleCoordsList = new ArrayList<>();
        List<Polygon> polygons = new ArrayList<>();
        Map<String,List<Coordinate>> NewPolygons = new HashMap<>();//最终结果多边形的多边形。
        Geometry n = null;
        Polygon newpo = null;
        // 遍历保护覆盖多边形相关数据（这里暂时保留原逻辑的结构，先匹配飞机类型）
        for (Map.Entry<String, List<Coordinate>> entry1 : ProtectionCoverPolygon.entrySet()) {
            if (entry1.getKey().equals(aircrafttype)) {
                List<Coordinate> protectionpolygon = entry1.getValue();
                List<List<Coordinate>> standnearpoint = new ArrayList<>();
                for (Map.Entry<String, List<List<Coordinate>>> entry2 : getNearStandRoadNode(flight).entrySet()) {
                    if (entry2.getKey().equals(stand)) {
                        standnearpoint = entry2.getValue();
                        for (int i = 0; i < standnearpoint.size(); i++) {
                            List<Coordinate> coordinateList = standnearpoint.get(i);
                            for (int i1 = 0; i1 < coordinateList.size()-1; i1++) {
                                boolean flat = false;
                                if(i==standnearpoint.size()-1&&i1==coordinateList.size()-2){//找到最后两个点
                                    flat = true;
                                }
                                Coordinate point1 = coordinateList.get(i1);
                                Coordinate point2 = coordinateList.get(i1+1);
                                List<Coordinate> rectangleCoords = new ArrayList<>();
                                // 计算矩形相关信息
                                rectangleCoords = calculateRectangleCoords(point1, point2, protectionpolygon,flat);
                                rectangleCoordsList.add(rectangleCoords);


                                // 以下代码用于基于矩形坐标计算外接多边形保护罩，参考原方法中的逻辑进行调整

                                GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
                                Coordinate[] rectangleArray = rectangleCoords.toArray(new Coordinate[0]);
                                Polygon rectanglePolygon = gf.createPolygon(rectangleArray);
                                polygons.add(rectanglePolygon);

                            }
//                                // 尝试合并所有多边形（这里简化处理，实际可能需要更精细的合并逻辑优化）
//                                Geometry unionGeometry = UnaryUnionOp.union(polygons);
//                                if (unionGeometry instanceof Polygon) {
//                                    Polygon finalPolygon = (Polygon) unionGeometry;
//                                    Coordinate[] outpol = finalPolygon.convexHull().getCoordinates();
//                                    List<Coordinate> cl = new ArrayList<>();
//                                    for (int j = 0; j < outpol.length; j++) {
//                                        cl.add(outpol[j]);
//                                    }
//                                    rectanglesWithProtection.put(entry.getKey(), cl);
//                                }
                        }
                    }
                }
            }
        }

//        GeometryFactory gf=JTSFactoryFinder.getGeometryFactory(null);
        List<Polygon> g = polygons;
        List<Polygon> temlist = new ArrayList<>();
        int count=0;
        temlist = g;
        boolean flag=false;
        while (!temlist.isEmpty()){
            if(newpo==null){
                newpo = temlist.get(0);
            }
            if(flag==true){
                temlist.add(newpo);
                newpo = temlist.get(0);
                temlist.remove(temlist.get(0));
                if(temlist.size()==1)break;

            }
            n=newpo.union(temlist.get(0));
            if(n instanceof Polygon){
                newpo = (Polygon) n;
                temlist.remove(temlist.get(0));
            }
            else {
                if(count<temlist.size()){
                    count++;
                    Polygon a = temlist.get(0);
                    temlist.remove(temlist.get(0));
                    temlist.add(a);
                }
                else {
                    count=0;
                    flag=true;
                }

            }
        }
        Coordinate[] outpol = newpo.convexHull().getCoordinates();
        List<Coordinate> cl = new ArrayList<>();
        for(int i=0;i<outpol.length;i++){
            cl.add(outpol[i]);
        }
        NewPolygons.put(key,cl);
        return NewPolygons;
    }

    private List<Coordinate> calculateRectangleCoords(Coordinate point1, Coordinate point2, List<Coordinate> k,boolean flat) {
        List<Coordinate> rectangleCoords = new ArrayList<>();
        // 计算中点坐标（矩形长的中点）
        Coordinate midpoint = new Coordinate((point1.x + point2.x) / 2, (point1.y + point2.y) / 2);

        Coordinate point1gauss=blhToGauss.BLHtoGauss(point1);
        Coordinate point2gauss=blhToGauss.BLHtoGauss(point2);
        // 计算两点连线的方向角（用于后续确定矩形其他顶点的相对位置）
        double direction =blhToGauss.getAngleByBlh(point1, point2);
        double distance = blhToGauss.getRoadlengthByBlh(new Coordinate[]{point1, point2});
        // 将角度转换为弧度，因为Java中三角函数使用弧度制
//        double radianDirection = Math.toRadians(direction);
        double radianDirection = direction ;
        // 计算矩形宽在x和y方向上的投影长度（假设地球近似为平面做简单投影计算，实际可能更复杂需精确大地测量计算）
        double halfWidthX = Math.abs(k.get(0).x * Math.cos(radianDirection));
        double halfWidthY = Math.abs(k.get(0).x * Math.sin(radianDirection)) ;
        if (flat ==true){
            // 计算矩形四个顶点坐标最后两个点需要多计算一点长
            for(int i=0;i< k.size();i++){
                Coordinate bundry = k.get(i);
                Coordinate centergs = new Coordinate((point1gauss.x+point2gauss.x)/2,(point1gauss.y+point2gauss.y)/2);
                double xgs = centergs.getX() + bundry.getX() * Math.cos(direction) - bundry.getY() * Math.sin(direction);
                double ygs = centergs.getY() + bundry.getX() * Math.sin(direction) + bundry.getY() * Math.cos(direction);
                rectangleCoords.add(blhToGauss.GaussToBLH(new Coordinate(xgs, ygs)));
            }
            rectangleCoords.add(rectangleCoords.get(0));
        }
        else {
            // 计算矩形四个顶点坐标
            Coordinate topLeft = blhToGauss.GaussToBLH(new Coordinate(point1gauss.x - halfWidthX, point1gauss.y + halfWidthY));
            Coordinate topRight = blhToGauss.GaussToBLH(new Coordinate(point1gauss.x + halfWidthX, point1gauss.y - halfWidthY));
            Coordinate bottomLeft = blhToGauss.GaussToBLH(new Coordinate(point2gauss.x - halfWidthX, point2gauss.y + halfWidthY));
            Coordinate bottomRight = blhToGauss.GaussToBLH(new Coordinate(point2gauss.x + halfWidthX, point2gauss.y - halfWidthY));
            // 将四个顶点坐标添加到矩形坐标列表
            rectangleCoords.add(topLeft);
            rectangleCoords.add(topRight);
            rectangleCoords.add(bottomRight);
            rectangleCoords.add(bottomLeft);
            rectangleCoords.add(topLeft); // 闭合多边形，再次添加第一个点
        }
        return rectangleCoords;
    }



}
