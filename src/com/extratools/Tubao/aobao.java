package com.extratools.Tubao;

import org.locationtech.jts.geom.Coordinate;


import java.util.ArrayList;
import java.util.List;
/*
 * */


public class aobao {
    private boolean[] flags;
    private List<Coordinate> points;
    private double[][] distanceMap;
    private List<List<Integer>> rNeigbourList;

    public aobao(List<Coordinate> list) {
        this.points = list;
        //  points.sort();
        flags = new boolean[points.size()];
        for (int i = 0; i < flags.length; i++)
            flags[i] = false;
        InitDistanceMap();
        InitNearestList();
    }

    private void InitNearestList() {
        rNeigbourList = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            rNeigbourList.add(i, GetSortedNeighbours(i));
        }
    }

    private void InitDistanceMap() {
        distanceMap = new double[points.size()][points.size()];
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                distanceMap[i][j] = GetDistance(points.get(i), points.get(j));
            }
        }
    }

    public double GetRecomandedR() {
        double r = 0;
        for (int i = 0; i < points.size(); i++) {
            if (distanceMap[i][rNeigbourList.get(i).get(1)] > r)
                r = distanceMap[i][rNeigbourList.get(i).get(1)];
        }
        return r;
    }

    public double GetMinEdgeLength() {
        double min = 0;
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                if (i < j) {
                    if (distanceMap[i][j] < min)
                        min = distanceMap[i][j];
                }
            }
        }
        return min;
    }

    public List<Coordinate> GetConcave_Ball(double radius) {
        List<Coordinate> ret = new ArrayList<>();
        List<Integer>[] adjs = GetInRNeighbourList(2 * radius);
        ret.add(points.get(0));
        //flags[0] = true;
        int i = 0, j = -1, prev = -1;
        while (true) {
            j = GetNextPoint_BallPivoting(prev, i, adjs[i], radius);
            if (j == -1)
                break;
            Coordinate p = aobao.GetCircleCenter(points.get(i), points.get(j), radius);
            ret.add(points.get(j));
            flags[j] = true;
            prev = i;
            i = j;
        }
        return ret;
    }

    public List<Coordinate> GetConcave_Edge(double radius) {
        List<Coordinate> ret = new ArrayList<>();
        List<Integer>[] adjs = GetInRNeighbourList(2 * radius);
        ret.add(points.get(0));
        int i = 0, j = -1, prev = -1;
        while (true) {
            j = GetNextPoint_EdgePivoting(prev, i, adjs[i], radius);
            if (j == -1)
                break;
            //Point p = BallConcave.GetCircleCenter(points[i], points[j], radius);
            ret.add(points.get(j));
            flags[j] = true;
            prev = i;
            i = j;
        }
        return ret;
    }

    private boolean CheckValid(List<Integer>[] adjs) {
        for (int i = 0; i < adjs.length; i++) {
            if (adjs[i].size() < 2) {
                return false;
            }
        }
        return true;
    }

    public boolean CompareAngel(Coordinate a, Coordinate b, Coordinate m_origin, Coordinate m_dreference) {

        Coordinate da = new Coordinate(a.x - m_origin.x, a.y - m_origin.y);
        Coordinate db = new Coordinate(b.x - m_origin.x, b.y - m_origin.y);
        double detb = GetCross(m_dreference, db);

        // nothing is less than zero degrees
        if (detb == 0 && db.x * m_dreference.x + db.y * m_dreference.y >= 0) return false;

        double deta = GetCross(m_dreference, da);

        // zero degrees is less than anything else
        if (deta == 0 && da.x * m_dreference.x + da.y * m_dreference.y >= 0) return true;

        if (deta * detb >= 0) {
            // both on same side of reference, compare to each other
            return GetCross(da, db) > 0;
        }

        // vectors "less than" zero degrees are actually large, near 2 pi
        return deta > 0;
    }

    public int GetNextPoint_EdgePivoting(int prev, int current, List<Integer> list, double radius) {
        if (list.size() == 2 && prev != -1) {
            return list.get(0) + list.get(1) - prev;
        }
        Coordinate dp;
        if (prev == -1)
            dp = new Coordinate(1, 0);
        else
            dp = new Coordinate(points.get(prev).x - points.get(current).x, points.get(prev).y - points.get(current).y);
        int min = -1;
        for (int j = 0; j < list.size(); j++) {
            if (!flags[list.get(j)]) {
                if (min == -1) {
                    min = list.get(j);
                } else {
                    Coordinate t = points.get(list.get(j));
                    if (CompareAngel(points.get(min), t, points.get(current), dp) && GetDistance(t, points.get(current)) < radius) {
                        min = list.get(j);
                    }
                }
            }
        }
        return min;
    }

    public int GetNextPoint_BallPivoting(int prev, int current, List<Integer> list, double radius) {
        SortAdjListByAngel(list, prev, current);
        for (int j = 0; j < list.size(); j++) {
            if (flags[list.get(j)])
                continue;
            int adjIndex = list.get(j);
            Coordinate xianp = points.get(adjIndex);
            Coordinate rightCirleCenter = GetCircleCenter(points.get(current), xianp, radius);
            if (!HasPointsInCircle(list, rightCirleCenter, radius, adjIndex)) {
                //  System.out.println(rightCirleCenter+ ""+points.get(current)+""+points.get(adjIndex)+ radius);
                return list.get(j);
            }
        }
        return -1;
    }

    private void SortAdjListByAngel(List<Integer> list, int prev, int current) {
        Coordinate origin = points.get(current);
        Coordinate df;
        if (prev != -1)
            df = new Coordinate(points.get(prev).x - origin.x, points.get(prev).y - origin.y);
        else
            df = new Coordinate(1, 0);
        int temp = 0;
        for (int i = list.size(); i > 0; i--) {
            for (int j = 0; j < i - 1; j++) {
                if (CompareAngel(points.get(list.get(j)), points.get(list.get(j + 1)), origin, df)) {
                    temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    private boolean HasPointsInCircle(List<Integer> adjPoints, Coordinate center, double radius, int adjIndex) {
        for (int k = 0; k < adjPoints.size(); k++) {
            if (adjPoints.get(k) != adjIndex) {
                int index2 = adjPoints.get(k);
                if (IsInCircle(points.get(index2), center, radius))
                    return true;
            }
        }
        return false;
    }

    public static Coordinate GetCircleCenter(Coordinate a, Coordinate b, double r) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double cx = 0.5 * (b.x + a.x);
        double cy = 0.5 * (b.y + a.y);
        if (r * r / (dx * dx + dy * dy) - 0.25 < 0) {
            return new Coordinate(-1, -1);
        }
        double sqrt = Math.sqrt(r * r / (dx * dx + dy * dy) - 0.25);
        return new Coordinate(cx - dy * sqrt, cy + dx * sqrt);
    }

    public static boolean IsInCircle(Coordinate p, Coordinate center, double r) {
        double dis2 = (p.x - center.x) * (p.x - center.x) + (p.y - center.y) * (p.y - center.y);
        return dis2 < r * r;
    }

    public List<Integer>[] GetInRNeighbourList(double radius) {
        List<Integer>[] adjs = new ArrayList[points.size()];
        for (int i = 0; i < points.size(); i++) {
            adjs[i] = new ArrayList<>();
        }
        for (int i = 0; i < points.size(); i++) {

            for (int j = 0; j < points.size(); j++) {
                if (i < j && distanceMap[i][j] < radius) {
                    adjs[i].add(j);
                    adjs[j].add(i);
                }
            }
        }
        return adjs;
    }

    private List<Integer> GetSortedNeighbours(int index) {
        List<PointInfo> infos = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            infos.add(new PointInfo(points.get(i), i, distanceMap[index][i]));
        }
        //infos.sort();
        List<Integer> adj = new ArrayList<>();
        for (int i = 1; i < infos.size(); i++) {
            adj.add(infos.get(i).Index);
        }
        return adj;
    }

    public static double GetDistance(Coordinate p1, Coordinate p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static double GetCross(Coordinate a, Coordinate b) {
        return a.x * b.y - a.y * b.x;
    }
}