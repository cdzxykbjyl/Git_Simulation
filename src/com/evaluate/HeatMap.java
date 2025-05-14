package com.evaluate;


import base.AngDisUtil;
import base.MatTrix;
import com.agent.Flight;
import org.apache.commons.collections.map.HashedMap;
import org.locationtech.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description://热度图
 * @author: LiPin
 * @time: 2022-08-05 20:56
 */
public class HeatMap {
    AngDisUtil angDisUtil=  new AngDisUtil();
    List<Coordinate> coordinates = new ArrayList<>();
    public int[][] heatmap;
    public Map<String, Map<Integer, Coordinate>> hisflightsmap = new HashedMap();//历史的位置，前一个点和后一点跨越栅格，处理，需要记录历史的位置
    //初始化热力图
    public HeatMap(  Coordinate botomleft, Coordinate upright ,double stepx , double stepy) {
        coordinates.add(new Coordinate(botomleft));
        coordinates.add(new Coordinate(upright.getX(), botomleft.getY()));
        coordinates.add(new Coordinate(upright));
        coordinates.add(new Coordinate(botomleft.getX(), upright.getY()));
        int row = (int) (upright.getX() / stepx) - (int) (botomleft.getX() / stepx);
        int path = (int) (upright.getY() / stepy) - (int) (botomleft.getY() / stepy);
        heatmap = new MatTrix().intzeros(row, path);
        hisflightsmap = new HashedMap();//历史的位置，前一个点和后一点跨越栅格，处理，需要记录历史的位置
    }
    public void hisloca(Map<String, Flight> reals, Coordinate botomleft, double stepx , double stepy) {
        for (Map.Entry<String, Flight> entry : reals.entrySet()) {
            Map<Integer, Coordinate> coordinates = new HashedMap();
            Flight flight = entry.getValue();
            if (hisflightsmap.get(entry.getKey()) == null) {
                coordinates.put(0, new Coordinate(flight.getX(), flight.getY(), flight.getZ()));
            } else {
                coordinates = hisflightsmap.get(entry.getKey());
                coordinates.put(0, coordinates.get(1));
            }
            coordinates.put(1, new Coordinate(flight.getX(), flight.getY(), flight.getZ()));
            hisflightsmap.put(entry.getKey(), coordinates);
        }
        for (Map.Entry<String, Map<Integer, Coordinate>> hisflight : hisflightsmap.entrySet()) {
            Coordinate startco = hisflight.getValue().get(0);
            Coordinate endco = hisflight.getValue().get(1);
            int prii = (int) ((startco.getX() -botomleft.getX()) / stepx);
            int priy = (int) ((startco.getY() - botomleft.getY()) / stepy);
            int nexti = (int) ((endco.getX()  -botomleft.getX()) / stepx);
            int nexty = (int) ((endco.getY()  -botomleft.getY()) / stepy);
            if(nexti - prii!=0){
                double xielv = (nexty - priy) / (nexti - prii);
                if (prii < nexti) {
                    for (int kjk = 0; kjk < nexti - prii; kjk++) {
                        double xkk = xielv * kjk + priy;
                        heatmap[prii+kjk][(int)xkk] = heatmap[prii+kjk][(int)xkk] +1;
                    }
                }
                else if(prii >nexti){
                    for (int kjk =nexti - prii; kjk <1 ; kjk++) {
                        double xkk = xielv * kjk + priy;
                        heatmap[prii+kjk][(int)xkk] =heatmap[prii+kjk][(int)xkk] +1;
                    }
                }
            }else{
                if(priy>nexty){
                    for(int ij=nexty;ij<priy;ij++){
                        heatmap[nexti][ij]= heatmap[nexti][ij]+1;
                    }
                }else{
                    for(int ij=priy;ij<nexty;ij++){
                        heatmap[nexti][ij]=heatmap[nexti][ij]+1;
                    }
                }
            }
        }
    }
}
