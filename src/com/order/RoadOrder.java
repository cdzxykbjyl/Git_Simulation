package com.order;

import com.alibaba.fastjson.JSONObject;
import org.locationtech.jts.geom.Coordinate;

public class RoadOrder {
    int id = 0;
    double length = 0;
    String name = "";
    String order = "";
    Coordinate monitorlocation = new Coordinate();
    double startangle = 0;
    double endangle = 0;
    double rotateangle = 0;
    String discribe = "";

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinate getMonitorlocation() {
        return monitorlocation;
    }

    public void setMonitorlocation(Coordinate monitorlocation) {
        this.monitorlocation = monitorlocation;
    }

    public int getId() {
        return id;
    }

    public double getStartangle() {
        return startangle;
    }

    public void setStartangle(double startangle) {
        this.startangle = startangle;
    }

    public double getEndangle() {
        return endangle;
    }

    public void setEndangle(double endangle) {
        this.endangle = endangle;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RoadOrder(int id, Coordinate monitorlocation, double length, String name, String order, double startangle, double endangle, double rotateangle) {
        this.id = id;
        this.length = length;
        this.name = name;
        this.monitorlocation = monitorlocation;
        this.order = order;
        this.startangle = startangle;
        this.endangle = endangle;
        this.rotateangle = rotateangle;
        if (order == "直行") {
            this.discribe = "前方沿着" + name + "滑行道直行" + (int) (length) + "米";
        } else {
            if (order == "R") {
                this.discribe = "前方在" + name + "滑行道路口右转,行驶约" + (int) (length) + "米";
            } else {
                this.discribe = "前方在" + name + "滑行道路口左转,行驶约" + (int) (length) + "米";
            }
        }
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", length=" + length +
                ", name='" + name + '\'' +
                ", order='" + order + '\'' +
                ", monitorlocation:" + monitorlocation +
                ", startangle=" + startangle +
                ", endangle=" + endangle +
                ", discribe='" + discribe + '\'' +
                '}';
    }

    public JSONObject getJson() {

        if (order == "Z") {
            this.discribe = "前方沿着" + name + "滑行道直行" + (int) (length) + "米";
        } else {
            double l = endangle - startangle > 0 ? endangle - startangle : endangle - startangle + 360;//先计算顺时针需要转多少度
            l = l > 180 ? 360 - l : l;//如果度数超过180，则计算逆时针需要转多少度
            this.rotateangle = l;
            if (order == "R") {
                this.discribe = "前方在" + name + "滑行道路口右转约" + (int) l + "度,行驶约" + (int) (length) + "米";
            } else {
                this.discribe = "前方在" + name + "滑行道路口左转" + (int) l + "度,行驶约" + (int) (length) + "米";
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id + 1);
        jsonObject.put("length", length);
        jsonObject.put("name", name);
        jsonObject.put("order", order);
        jsonObject.put("monitorlocation", monitorlocation);
        jsonObject.put("startangle", startangle);
        jsonObject.put("endangle", endangle);
        jsonObject.put("rotateangle", rotateangle);
        jsonObject.put("discribe", discribe);
        return jsonObject;
    }
}
