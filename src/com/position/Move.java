package com.position;

import base.BlhToGauss;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;
import java.util.List;

/**
 * @description:
 * @author: LiPin
 * @time: 2022-10-18 21:42
 */
public class Move {
    public double vs = 0;
    public double speed = 1;
    int lineindex = 0;
    int segindex = 0;
    String isend = "Y";
    double segyu = 0;
    double perlimistspeed = 0;
    double nextlimistspeed = 0;
    public Move() {
    }
    public void go(double speed, double a, double limitspeed,int T) {
        if (speed > limitspeed) {
            if (speed - a * T > limitspeed) {
                this.vs = speed * T - 0.5 * a * T * T;
                this.speed = speed - a * T;
            } else if (speed - a * T == limitspeed) {
                this.vs = speed * T - 0.5 * a * T * T;
                this.speed = speed - a * T;
            } else if (speed - a * T < limitspeed) {
                double tt = (limitspeed - speed) / -a;
                this.vs = speed * tt + 0.5 * -a * tt * tt + limitspeed * (T - tt);
                this.speed = limitspeed;
            }
        } else if (speed == limitspeed) {
            this.vs = limitspeed * T;
            this.speed = speed;
        } else {
            if (speed + a * T < limitspeed) {
                this.vs = speed * T + 0.5 * a * T * T;
                this.speed = speed + a * T;
            } else if (speed * T + a * T == limitspeed) {
                this.vs = speed * T + 0.5 * a * T * T;
                this.speed = speed + a * T;
            } else if (speed + a * T > limitspeed) {
                double tt = (limitspeed - speed) / a;
                this.vs = speed * tt + 0.5 * a * tt * tt + limitspeed * (T - tt);
                this.speed = limitspeed;
            }
        }
    }

    public Coordinate[] getCoordByindex(List<RoadLine> mapLines, int lineindex) {
        Coordinate[] coordinates = mapLines.get(lineindex).direction.equals("reverse")
                ? mapLines.get(lineindex).geometry.reverse().getCoordinates()
                : mapLines.get(lineindex).geometry.getCoordinates();
        return coordinates;
    }

    public Coordinate getSegCoordinate( Coordinate A, List<RoadLine> mapLines, Coordinate endnode) {
        BlhToGauss blhToGauss=new BlhToGauss();
        Coordinate segpoint = null;
        Coordinate[] coordinates = getCoordByindex(mapLines, lineindex);//当前线的坐标点
        Coordinate B = coordinates[segindex + 1];//下一个节点
        if (lineindex < mapLines.size() - 1) {
            double segdis = blhToGauss.getBLHDistanceByHaverSine(A, B);
                if (segdis < vs - segyu) {
                    if (segindex >= coordinates.length - 2) {
                        lineindex = lineindex + 1;
                        segindex = 0;
                    } else {
                        segindex = segindex + 1;
                    }
                    segyu = segdis + segyu;
                    segpoint = getSegCoordinate( B, mapLines, endnode);
                } else if (segdis > vs - segyu) {
                    segpoint = getSegmetPoint(A, B, vs - segyu);
                    segyu = 0;
                } else if (segdis == vs - segyu) {
                    if (segindex >= coordinates.length - 2) {
                        lineindex = lineindex + 1;
                        segindex = 0;
                    } else {
                        segindex = segindex + 1;
                    }
                    segyu = 0;
                    segpoint = B;
                }
        } else if (lineindex == mapLines.size() - 1) {
            double segdis = blhToGauss.getBLHDistanceByHaverSine(A, B);
            Coordinate foot = getFoot(A, endnode, B);//最后一个点的垂足
            if (foot != null) {//如果垂据小
                B = foot;
                segdis = blhToGauss.getBLHDistanceByHaverSine(A, B);
                if (segdis <= vs - segyu) {
                    segpoint = B;
                    isend = "N";
                } else if (segdis > vs - segyu) {
                    segpoint = getSegmetPoint(A, B, vs - segyu);
                    segyu = 0;
                }
            } else {
                if (segdis < vs - segyu) {
                    segindex = segindex + 1;
                    if (segindex >= coordinates.length - 1) {
                        segindex--;
                        segpoint = B;
                        isend = "N";
                    } else {
                        segyu = segdis + segyu;
                        segpoint = getSegCoordinate( B, mapLines, endnode);
                    }
                } else if (segdis > vs - segyu) {
                    segpoint = getSegmetPoint(A, B, vs - segyu);
                    segyu = 0;
                } else if (segdis == vs - segyu) {
                    segindex = segindex + 1;
                    segyu = 0;
                    segpoint = B;
                }
            }
        }
        return segpoint;
    }
    public Coordinate getFoot(Coordinate firstp, Coordinate midp, Coordinate lastp) {
        Coordinate foot = null;
        double dx = firstp.x - lastp.x;
        double dy = firstp.y - lastp.y;
        double u = (midp.x - firstp.x) * dx + (midp.y - firstp.y) * dy;
        u /= dx * dx + dy * dy;
        double footx = firstp.x + u * dx;
        double footy = firstp.y + u * dy;
        double d = Math.abs((firstp.x - lastp.x) * (firstp.x - lastp.x) + (firstp.y - lastp.y) * (firstp.y - lastp.y));
        double d1 = Math.abs((firstp.x - footx) * (firstp.x - footx) + (firstp.y - footy) * (firstp.y - footy));
        double d2 = Math.abs((lastp.x - footx) * (lastp.x - footx) + (lastp.y - footy) * (lastp.y - footy));
        if (d1 <= d && d2 <= d) {
            foot = new Coordinate(footx, footy);
        } else if (d1 > d2) {
            Double distod = Math.sqrt(Math.pow(lastp.x - footx, 2.0D) + Math.pow(lastp.y - footy, 2.0D));
            if (distod < 1.0E-5D) {
                foot = lastp;
            }
        } else {
            Double distod = Math.sqrt(Math.pow(firstp.x - footx, 2.0D) + Math.pow(firstp.y - footy, 2.0D));
            if (distod < 1.0E-5D) {
                foot = firstp;
            }
        }
        return foot;
    }
    public static boolean between(double a, double b, double target) {
        // 允许的误差值
        double offset = 0.0000001;
        if (target >= a - offset && target <= b + offset || target <= a + offset && target >= b - offset)
            return true;
        else
            return false;
    }
    private Coordinate getSegmetPoint(Coordinate start, Coordinate end, double l) {
        double L0 = new BlhToGauss().getBLHDistanceByHaverSine(start, end);
        Coordinate coordinate = new Coordinate(start.getX() + l / L0* (end.getX() - start.getX()) ,
                start.getY() + l / L0* (end.getY() - start.getY()) );
        return coordinate;
    }
}
