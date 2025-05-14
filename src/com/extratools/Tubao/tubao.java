package com.extratools.Tubao;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-03-20 11:36
 */

public class tubao {
    public static double calculateRegularPolygonAngle(int sides) {
        //如果多边形边数小于3，则不是多边形，返回0
        if (sides < 3) return 0;
        //否则，代入公式计算内角和
        double angle = (double) (sides - 2) * 180 / sides;
        return angle;
    }

    public static double calculateBearingToPoint(double currentBearing, int currentX, int currentY,
                                                 int targetX, int targetY) {
        //计算从根部点到目标点的向量的横坐标
        double x = (targetX - currentX);
        //同上，计算向量的纵坐标
        double y = (targetY - currentY);
        //调用Math类下的atan2方法，计算向量所要偏转的正角度
        double degree = 90 - currentBearing - Math.toDegrees(Math.atan2(y, x));
        //如果角度为负，则转为正角
        if (degree < 0) degree += 360;
        return degree;
    }

    public static List<Coordinate> convexHull(List<Coordinate> points) {
        //判断点的总数是否小于3，小于3则不能构成多边形
        if (points.size() < 3) {
            return points;
        }
        //定义新的Set集合，其中不会有重复元素，符合我们的要求
        List<Coordinate> set = new ArrayList<>();
        Coordinate xmin = new Coordinate(Double.MAX_VALUE, Double.MAX_VALUE);
        //运用for-each遍历的方式，在所有点中寻找最左的点
        for (Coordinate item : points) {
            if (item.x < xmin.x || (item.x == xmin.x && item.y < xmin.y))
                xmin = item;
        }
        //设最左的点为初始起点
        Coordinate nowPoint = xmin, tempPoint = xmin;
        //初始化指向角度为0
        double nowAngle = 0, minAngle = 360, tempAngle = 0;
        double distance;
        double maxdistance = 0;
        //无差别地遍历所有的点
        do {
            set.add(tempPoint);
            //  遍历全部点，寻找下一个在凸包上的点
            for (Coordinate item : points) {
                //当某一点不在点集之中或者该点为起始点
                if ((!set.contains(item) || item == xmin)) {
                    //调用判断calculateBearingToPoint方法计算所需要偏转的角度
                    tempAngle = calculateBearingToPoint(nowAngle, (int) nowPoint.x, (int) nowPoint.y, (int) item.x, (int) item.y);
                    //计算目标点与所在点之间的距离
                    distance = (item.x - nowPoint.x) * (item.x - nowPoint.x) + (item.y - nowPoint.y) * (item.y - nowPoint.y);
                    /*如果某一点的偏转角比之前所找到的最小角度还要小
                      则该角度成为了最小偏转角
                      多个点在同一方向上时取距离所在点最远的目标点*/
                    if (tempAngle < minAngle || ((tempAngle == minAngle) && (distance > maxdistance))) {
                        minAngle = tempAngle;
                        tempPoint = item;
                        maxdistance = distance;
                    }
                }
            }
            //遍历完所有点后，初始化判断指标，从刚刚找到的目标点再次出发，重复上述步骤
            nowAngle = minAngle;
            minAngle = 360;
            nowPoint = tempPoint;
        } while (nowPoint != xmin);  // 当下一个点为第一个点时找到了凸包上的全部点，退出循环
        return set;

    }



}
