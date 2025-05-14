package com.order;



import base.AngDisUtil;
import base.ClearNoise;
import fun.RoadLine;

import java.util.ArrayList;
import java.util.List;


//滑行引导程序
public class Gaid {

    public List<RoadOrder> getGaids(List<RoadLine> roadlines, String taxilines) {
        double startangle = 0;
        double endangle = 0;
        String[] ss = taxilines.split(",");
        List<RoadOrder> lastRocadiroders = new ArrayList<>();
        for (int i = 0; i < roadlines.size(); i++) {
            RoadLine R = roadlines.get(i);
            if (R.direction.equals("positive")) {
                startangle = R.getFirstangle();
                endangle = R.getEndangle() + 180 > 360 ? R.getEndangle() + 180 - 360 : R.getEndangle() + 180;
            } else {
                startangle = R.getEndangle();
                endangle = R.getFirstangle() + 180 > 360 ? R.getFirstangle() + 180 - 360 : R.getFirstangle() + 180;
            }
            String nowroadnames= R.name.replaceAll("_",",");
            String[] namearray =nowroadnames.split(",");
            if (namearray.length != 1) {//筛选出转弯
                String noworder = new AngDisUtil().getDir(startangle, endangle);
                if (getindex(namearray, ss)) {
                    updatelastRocadiroders(  lastRocadiroders,  noworder, startangle, endangle,new AngDisUtil().getDirD(startangle, endangle), R,  nowroadnames);
                }
            } else {
                String noworder = "Z";
                updatelastRocadiroders(  lastRocadiroders,  noworder, startangle, endangle,0, R,  nowroadnames);
            }
        }
        return lastRocadiroders;
    }
    private  void updatelastRocadiroders( List<RoadOrder> lastRocadiroders, String noworder,double startangle,double endangle,double rotateangle,RoadLine R,String  nowroadnames){
        RoadOrder nowroadOrder = null;
        if (R.direction.equals("positive")) {
            nowroadOrder = new RoadOrder(lastRocadiroders.size(), R.getFirstPoint(), R.length,nowroadnames, noworder, startangle, endangle,rotateangle);
        } else {
            nowroadOrder = new RoadOrder(lastRocadiroders.size(), R.getEndPoint(), R.length,nowroadnames, noworder, startangle, endangle,rotateangle);
        }
        if (lastRocadiroders.size() == 0) {
            lastRocadiroders.add(nowroadOrder);
        } else {
            RoadOrder lastorder = lastRocadiroders.get(lastRocadiroders.size() - 1);
            if (noworder.equals(lastorder.order)) {
                if(!nowroadOrder.name.equals(lastorder.name)){
                    lastorder.name =  getnowroads( lastorder.name, nowroadOrder.name);
                }
                lastorder.endangle= nowroadOrder.endangle;
                lastorder.length = lastorder.length + R.length;
                lastRocadiroders.set(lastRocadiroders.size() - 1, lastorder);
            } else {
                lastRocadiroders.add(nowroadOrder);
            }
        }

    }


    private String getnowroads(String lastnames,String nownames){
        String[] lastmes =lastnames.split(",");
        String[] nowstrs =nownames.split(",");
        for (int ki = 0; ki < lastmes.length; ki++) {
            nowstrs =nownames.split(",");
            boolean isin=false;
            for (int kj = 0; kj < nowstrs.length; kj++) {
                if (lastmes[ki].equals(nowstrs[kj])) {
                    isin=true;
                }
            }
            if(!isin){
                nownames=nownames+","+lastmes[ki];
            }
        }
        return nownames;
    }

    private boolean getindex(String[] Rname, String[] ss) {
        boolean isin = true;
        for (int i = 0; i < Rname.length; i++) {
            boolean now = false;
            for (int j = 0; j < ss.length; j++) {
                if (Rname[i].equals(ss[j])) {
                    now = true;
                }
            }
            if (!(isin && now)) {
                isin = false;
            }
        }
        return isin;
    }


    private  String getnames(List<RoadLine> roadlines) {
        String strs = "";
        for (int i = 0; i < roadlines.size(); i++) {
            RoadLine R = roadlines.get(i);
            String Rname = R.name;
            if (strs == "") {
                strs = Rname;
            } else {
                String[] strarrays = strs.split(",");
                boolean isin = false;
                for (int j = 0; j < strarrays.length; j++) {
                    if (strarrays[j].equals(Rname)) {
                        isin = true;
                    }
                }
                if (!isin) {
                    strs = strs + "," + Rname;
                }
            }
        }
        return strs;
    }

    private String getorder(String roadsnames, String start, String end) {
        String[] roadsname = roadsnames.split(",");
        ClearNoise noiseLayer = new ClearNoise();
        noiseLayer.zuHe(roadsname);
        int startindex = -9999;
        int endindex = -9999;
        startindex = noiseLayer.getIndexByName(noiseLayer.nameslist, start);
        endindex = noiseLayer.getIndexByName(noiseLayer.nameslist, end);
        String str = "";
        if (startindex != -9999 && endindex != -9999) {
            List<Integer> roadIDs = noiseLayer.getstr(roadsname, startindex, endindex);
            str = noiseLayer.convertStr(roadIDs);
        }
        if (str.split(",").length > 2) {
            return str;
        } else {
            return null;
        }
    }

}
