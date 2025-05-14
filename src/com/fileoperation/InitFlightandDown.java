package com.fileoperation;
import base.BlhToGauss;
import fileoperation.ReadandWriteFile;
import fun.FlyAndSpeedNodes;
import org.locationtech.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-07-04 16:42
 */

public class InitFlightandDown {

    public List<FlyAndSpeedNodes> getadA(  List<FlyAndSpeedNodes> AA , Coordinate next0,Coordinate jzd) {
        BlhToGauss blhToGauss = new BlhToGauss();
        double angle = blhToGauss.getAngleByBlh(jzd, next0);
        List<FlyAndSpeedNodes> res = new ArrayList<>();
        FlyAndSpeedNodes f = AA.get(AA.size() - 1);
        FlyAndSpeedNodes f2 = AA.get(AA.size() - 2);
        FlyAndSpeedNodes c0 = new FlyAndSpeedNodes(jzd.getX(), jzd.getY(), f.getSpeednum(), f.getSpeed(),
                f.getAltnum(), f.getAlt(), f.getTimenum(), f.getTime(), f.getRadius());
        res.add(c0);

        double fan = blhToGauss.getAngleByBlh(new Coordinate(f.getX(), f.getY()), new Coordinate(f2.getX(), f2.getY()));
        for (int i = AA.size() - 1; i > 0; i--) {
            FlyAndSpeedNodes nextn = AA.get(i - 1);
            Coordinate first = new Coordinate(AA.get(i).getX(), AA.get(i).getY());
            Coordinate next = new Coordinate(nextn.getX(), nextn.getY());
            double disd = getBLHDistanceByVincenty(first, next);
            double fan0 = blhToGauss.getAngleByBlh(first,next);
            double nowangle = fan0 - fan + angle;
            Coordinate nextp = calcLatAndlon(jzd, nowangle, disd);
            FlyAndSpeedNodes c = new FlyAndSpeedNodes(nextp.getX(), nextp.getY(), nextn.getSpeednum(), nextn.getSpeed(),
                    nextn.getAltnum(), nextn.getAlt(), nextn.getTimenum(), nextn.getTime(), nextn.getRadius());
            res.add(0, c);
            jzd=nextp;
        }
        return res;
    }

    public List<FlyAndSpeedNodes> getadD( List<FlyAndSpeedNodes> DD, Coordinate jzd,Coordinate next0 ) {
        List<FlyAndSpeedNodes> res = new ArrayList<>();
        FlyAndSpeedNodes f = DD.get(0);
        FlyAndSpeedNodes f2 =  DD.get(1);
        FlyAndSpeedNodes c0 = new FlyAndSpeedNodes(jzd.getX(), jzd.getY(), f.getSpeednum(), f.getSpeed(),
                f.getAltnum(), f.getAlt(), f.getTimenum(), f.getTime(), f.getRadius());
        res.add(c0);
        BlhToGauss blhToGauss = new BlhToGauss();
        double angle=blhToGauss.getAngleByBlh(jzd, next0);
        double fan = blhToGauss.getAngleByBlh(new Coordinate(f.getX(), f.getY()), new Coordinate(f2.getX(), f2.getY()));
        for (int i = 0; i < DD.size() - 1; i++) {
            FlyAndSpeedNodes nextn = DD.get(i + 1);
            Coordinate first = new Coordinate(DD.get(i).getX(), DD.get(i).getY());
            Coordinate next = new Coordinate(nextn.getX(), nextn.getY());
            double dis= getBLHDistanceByVincenty(first, next);
            double fan0 = blhToGauss.getAngleByBlh(first, next);
            double nowangle = fan0 - fan + angle;
            Coordinate nextp = calcLatAndlon(jzd, nowangle, dis);
            FlyAndSpeedNodes c = new FlyAndSpeedNodes(nextp.getX(), nextp.getY(), nextn.getSpeednum(), nextn.getSpeed(),
                    nextn.getAltnum(), nextn.getAlt(), nextn.getTimenum(), nextn.getTime(), nextn.getRadius());
            res.add(c);
            jzd=nextp;
        }
        return res;
    }


    /**
     * MethodName: calcLatAndlon
     * Description:
     *
     * @date 2021/9/21 20:35
     * @params: [posX 终点位置X轴的位置信息, posY 终点位置Y轴的位置信息, basePointLongitude 基点的GPS经度坐标,
     * basePointLatitude 基点的GPS纬度坐标, azimuth 方位角(弧度), distance 2点之间的直线距离]
     * @author Tianjiao
     */
    static double a = 6378137;
    static double b = 6356752.3142;
    static double f = 1 / 298.257223563;

    public static Coordinate calcLatAndlon(Coordinate co, double azimuth, double distance) {

        double lon1 = co.getX();
        double lat1 = co.getY();

        double s = distance;
        double alpha1 = rad(azimuth);
        double sinAlpha1 = Math.sin(alpha1);
        double cosAlpha1 = Math.cos(alpha1);

        double tanU1 = (1 - f) * Math.tan(rad(lat1));
        double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1;
        double sigma1 = Math.atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cosSqAlpha = 1 - sinAlpha * sinAlpha;
        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double sinSigma = 0;
        double cosSigma = 0;
        double cos2SigmaM = 0;
        double sigma = s / (b * A), sigmaP = 2 * Math.PI;
        while (Math.abs(sigma - sigmaP) > 1e-12) {
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            sinSigma = Math.sin(sigma);
            cosSigma = Math.cos(sigma);
            double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                    B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = s / (b * A) + deltaSigma;
        }

        double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
        double lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
        double lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
        double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
        double L = lambda - (1 - C) * f * sinAlpha *
                (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        return new Coordinate(lon1 + deg(L), deg(lat2));

    }

    /**
     * 度换成弧度
     *
     * @param {Float} d  度
     * @return {[Float}   弧度
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 弧度换成度
     *
     * @param {Float} x 弧度
     * @return {Float}   度
     */
    private static double deg(double x) {
        return x * 180 / Math.PI;
    }

    public static double getBLHDistanceByVincenty(Coordinate p1, Coordinate p2) {
        double a = 6378137;
        double f = 1 / 298.257223563;
        double b = a * (1.0D - f);
        double lat1Rad = Math.toRadians(p1.getY());
        double lon1Rad = Math.toRadians(p1.getX());
        double lat2Rad = Math.toRadians(p2.getY());
        double lon2Rad = Math.toRadians(p2.getX());
        double L = lon2Rad - lon1Rad;
        double U1 = Math.atan((1.0D - f) * Math.tan(lat1Rad));
        double U2 = Math.atan((1.0D - f) * Math.tan(lat2Rad));
        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);
        double lambda = L;
        int iterLimit = 100;

        while (true) {
            double uSq = Math.sin(lambda);
            double A = Math.cos(lambda);
            double sinSigma = Math.sqrt(cosU2 * uSq * cosU2 * uSq + (cosU1 * sinU2 - sinU1 * cosU2 * A) * (cosU1 * sinU2 - sinU1 * cosU2 * A));
            if (sinSigma == 0.0D) {
                return 0.0D;
            }

            double cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * A;
            double sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * uSq / sinSigma;
            double cosSqAlpha = 1.0D - sinAlpha * sinAlpha;
            double cos2SigmaM = cosSigma - 2.0D * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0.0D;
            }

            double B = f / 16.0D * cosSqAlpha * (4.0D + f * (4.0D - 3.0D * cosSqAlpha));
            double lambdaP = lambda;
            lambda = L + (1.0D - B) * f * sinAlpha * (sigma + B * sinSigma * (cos2SigmaM + B * cosSigma * (-1.0D + 2.0D * cos2SigmaM * cos2SigmaM)));
            if (Math.abs(lambda - lambdaP) > 1.0E-12D) {
                --iterLimit;
                if (iterLimit > 0) {
                    continue;
                }
            }

            if (iterLimit == 0) {
                return 0.0D / 0.0;
            }

            uSq = cosSqAlpha * (a * a - b * b) / (b * b);
            A = 1.0D + uSq / 16384.0D * (4096.0D + uSq * (-768.0D + uSq * (320.0D - 175.0D * uSq)));
            B = uSq / 1024.0D * (256.0D + uSq * (-128.0D + uSq * (74.0D - 47.0D * uSq)));
            double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4.0D * (cosSigma * (-1.0D + 2.0D * cos2SigmaM * cos2SigmaM) - B / 6.0D * cos2SigmaM * (-3.0D + 4.0D * sinSigma * sinSigma) * (-3.0D + 4.0D * cos2SigmaM * cos2SigmaM)));
            return b * A * (sigma - deltaSigma);
        }
    }

}
