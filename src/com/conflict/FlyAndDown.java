package com.conflict;

import base.getAlt;
import com.agent.Flight;
import com.order.LandingGear;
import com.order.OffandLand;
import org.locationtech.jts.geom.Coordinate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 该类是为了改变飞机高度，并发出打开起落架，收起起落架、降落，起飞的指令信息
 * @author: LiPin
 * @time: 2024-04-28 14:06
 */

public class FlyAndDown {
    Map<String, double[][]> polygon_down = new HashMap<>();
    Map<String, double[][]> polygon_fly= new HashMap<>();
    double []gearaltAD=new double[]{100,100};

public  FlyAndDown(Map<String,Map<String, List<Coordinate>>> polygon_onland_takeoff,  double []gearaltAD){
    Map<String, Map<String, double[][]>>polygon_onland_takeoffs = converttoarray(polygon_onland_takeoff);
    polygon_down=polygon_onland_takeoffs.get("down");
    polygon_fly=polygon_onland_takeoffs.get("fly");
    this.gearaltAD=gearaltAD;
}
    //检测飞机是否落地// //检测飞机起落架，并更新高度
    public void updateHeightandElevation(Map<String, Flight> real) {
        for (Map.Entry<String, Flight> entry : real.entrySet()) {
            Flight flight = entry.getValue();
            double[] lonlatalt=new double[]{0,0};
            if (flight.getInOutFlag().equals("A")) {
                lonlatalt = getAltInit(polygon_down.get(flight.getRunway()+flight.getInOutFlag()), new double[]{flight.getX(), flight.getY()});
                if (lonlatalt[2] < gearaltAD[0] && flight.getZ() > gearaltAD[0]) {
                    flight.setZ(lonlatalt[2]);
                    flight.setElevation(flight.getZ() / 10);
                    LandingGear landingGear = new LandingGear("opengear", flight.getFlightId(), flight.getFlightNo(),
                            flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(landingGear);
                }
                if (flight.getZ() > 0 && lonlatalt[2] == 0) {
                    flight.setZ(0);
                    flight.setElevation(0);
                    OffandLand offandLand = new OffandLand("landing", flight.getFlightId(), flight.getFlightNo(), flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(offandLand);
                }
            } else if (flight.getInOutFlag().equals("D")) {
                lonlatalt = getAltInit(polygon_fly.get(flight.getRunway()+flight.getInOutFlag()), new double[]{flight.getX(), flight.getY()});
                if (lonlatalt[2] >  gearaltAD[1] && flight.getZ() <  gearaltAD[1]) {
                    LandingGear landingGear = new LandingGear("closegear",
                            flight.getFlightId(), flight.getFlightNo(), flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    //收起起落架指令
                    flight.getMessages().add(landingGear);
                }
                if (flight.getZ() == 0 && lonlatalt[2] > 0) {
                    flight.setElevation(10);
                    OffandLand offandLand = new OffandLand("takeoff", flight.getFlightId(), flight.getFlightNo(), flight.getTime(), flight.getRunway(), flight.getX(), flight.getY(), flight.getZ());
                    flight.getMessages().add(offandLand);
                }
            }
            flight.setZ(lonlatalt[2]);
        }
    }

    public double[] getAltInit( double[][] dk, double[] co) {

        getAlt alt = new getAlt();
        double[][] dk2 = alt.buq(dk);
        boolean isin = alt.IsinPolygon(dk2, co);
        double[] ales = null;
        if (isin) {
            ales = alt.getAltInPolygon(dk2, co);
        } else {
            ales = new double[]{co[0], co[1], 0};
        }
        return ales;
    }
    public Map<String, Map<String, double[][]>> converttoarray(Map<String, Map<String, List<Coordinate>>> flyanddownpolygons) {
        Map<String, Map<String, double[][]>> re = new HashMap<>();
        for (Map.Entry<String, Map<String, List<Coordinate>>> entry0 : flyanddownpolygons.entrySet()) {
            Map<String, List<Coordinate>> flypolygons = entry0.getValue();
            Map<String, double[][]> flypolygonsarry = new HashMap<>();
            for (Map.Entry<String, List<Coordinate>> entry : flypolygons.entrySet()) {
                double[][] item = new double[entry.getValue().size()][3];
                for (int i = 0; i < entry.getValue().size(); i++) {
                    item[i][0] = entry.getValue().get(i).getX();
                    item[i][1] = entry.getValue().get(i).getY();
                    item[i][2] = entry.getValue().get(i).getZ();
                }
                flypolygonsarry.put(entry.getKey(), item);
            }
            re.put(entry0.getKey(), flypolygonsarry);
        }
        return re;
    }

}
