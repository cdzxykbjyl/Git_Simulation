package com.fileoperation;

import base.AngDisUtil;
import base.BlhToGauss;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-07-03 15:13
 */

public class ConvertMesToGeom {

    public List<Coordinate> getcoordinate(RoadLine roadLine) {
        List<Coordinate> coordinates = new ArrayList();
        if (roadLine.direction.equals("reverse")) {
//            System.out.println(roadLine.getId());
            coordinates = Arrays.asList(roadLine.geometry.reverse().getCoordinates());
        } else {
            coordinates = Arrays.asList(roadLine.geometry.getCoordinates());
        }
        return coordinates;
    }

    private Coordinate getfoot(Coordinate co, List<Coordinate> coordinates) {
        AngDisUtil angDisUtil = new AngDisUtil();
        BlhToGauss blhToGauss = new BlhToGauss();
        double dis = 99999;
        Coordinate f = null;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate f0 = angDisUtil.getFoot(coordinates.get(i), co, coordinates.get(i + 1));
            double dis0 = blhToGauss.getBLHDistanceByHaverSine(co, f0);
            if (dis > dis0) {
                dis = dis0;
                f = f0;
            }
        }
        return f;
    }

    //获取
    private List<Coordinate> getcosf(Coordinate cof, List<Coordinate> coordinates) {
        AngDisUtil angDisUtil = new AngDisUtil();
        BlhToGauss blhToGauss = new BlhToGauss();
        double dis = 99999;
        List<Coordinate> coordinates1 = new ArrayList<>();
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate f0 = angDisUtil.getFoot(coordinates.get(i), cof, coordinates.get(i + 1));
            double dis0 = blhToGauss.getBLHDistanceByHaverSine(cof, f0);
            if (dis > dis0) {
                dis = dis0;
                coordinates1.add(coordinates.get(i));
            }
        }
        coordinates1.add(cof);
        return coordinates;
    }

    private List<Coordinate> getcose(Coordinate coe, List<Coordinate> coordinates) {
        AngDisUtil angDisUtil = new AngDisUtil();
        BlhToGauss blhToGauss = new BlhToGauss();
        double dis = 99999;
        List<Coordinate> coordinates1 = new ArrayList<>();
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate f0 = angDisUtil.getFoot(coordinates.get(i), coe, coordinates.get(i + 1));
            double dis0 = blhToGauss.getBLHDistanceByHaverSine(coe, f0);
            if (dis > dis0) {
                dis = dis0;
            } else {
                coordinates1.add(coordinates.get(i + 1));
            }
        }
        coordinates1.add(0, coe);
        return coordinates;
    }


    public Geometry getgeometry(List<RoadLine> roadlines, double sampointdis) {
        BlhToGauss blhToGauss = new BlhToGauss();
        List<List<Coordinate>> coordinateList = new ArrayList();
        for (int i = 0; i < roadlines.size() - 1; ++i) {
            List<Coordinate> coordinates0 = getcoordinate(roadlines.get(i));
            coordinateList.add(coordinates0);
        }
        for (int i = 0; i < coordinateList.size() - 1; ++i) {
            List<Coordinate> coordinates0 = coordinateList.get(i);
            List<Coordinate> coordinates1 = coordinateList.get(i + 1);
            Coordinate aend = coordinates0.get(coordinates0.size() - 1);
            Coordinate bf = coordinates1.get(0);
            double dis = blhToGauss.getBLHDistanceByHaverSine(aend, bf);
            if (dis > sampointdis) {
                Coordinate a0 = getfoot(aend, coordinates1);
                Coordinate b0 = getfoot(bf, coordinates0);
                double disa0 = blhToGauss.getBLHDistanceByHaverSine(aend, a0);
                double disb0 = blhToGauss.getBLHDistanceByHaverSine(bf, b0);
                if (disa0 > disb0) {
                    coordinates0 = getcosf(bf, coordinates0);
                    coordinateList.set(i, coordinates0);
                } else {
                    coordinates1 = getcose(aend, coordinates1);
                    coordinateList.set(i, coordinates1);
                }
            }
        }
        List<Coordinate> coordinatesall = new ArrayList();
        for (int i = 0; i < coordinateList.size(); i++) {
            List<Coordinate> coordinates = coordinateList.get(i);
            if (i != coordinateList.size() - 1) {
                coordinatesall.addAll(coordinates);
                coordinatesall.remove(coordinatesall.size() - 1);
            } else {
                coordinatesall.addAll(coordinates);
            }
        }
        Geometry geometry = (new GeometryFactory()).createLineString(coordinatesall.toArray(new Coordinate[coordinatesall.size()]));
        return geometry;
    }
}
