package com.extratools.Tubao;

import org.locationtech.jts.geom.Coordinate;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-03-20 13:46
 */

public class PointInfo {
    public Coordinate Point;
    public int Index;
    public double DistanceTo;
    public PointInfo(Coordinate p, int i, double dis)
    {
        this.Point = p;
        this.Index = i;
        this.DistanceTo = dis;
    }
    //        public int CompareTo(PointInfo other)
//        {
//            return DistanceTo.CompareTo(other.DistanceTo);
//        }
    public String  ToString()
    {
        return Point+","+Index+","+DistanceTo;
    }
}
