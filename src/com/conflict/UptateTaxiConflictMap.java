package com.conflict;

import base.AngDisUtil;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;
import road.ShpAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-06-16 21:45
 */

public class UptateTaxiConflictMap {

    private  List<RoadConflictMes>  updateRoadConflictMesMap(ShpAttribute a, ShpAttribute b,String ida,String idb) {
        List<RoadConflictMes> conflicts = new ArrayList<>();
        List<RoadLine> aroadLines = a.getRoadlines();
        List<RoadLine> broadLines = b.getRoadlines();
        checkConflict checkconflict = new checkConflict();
        AngDisUtil angDisUtil = new AngDisUtil();
        List<ConflictRoadIndex> conflictRoadIndes = checkconflict.getConflictRoadIndex(aroadLines, broadLines);
        if (conflictRoadIndes.size() > 0) {
            conflicts = new ArrayList<>();
            checkconflict.UpdateStands(a.getInoutflag(), b.getInoutflag(), aroadLines, broadLines, conflictRoadIndes);
            for (int i = 0; i < conflictRoadIndes.size(); i++) {
                RoadConflictMes roadConflictMes = new RoadConflictMes();
                ConflictRoadIndex nowdis = conflictRoadIndes.get(i);
                roadConflictMes.setType(nowdis.type);

                List<RoadLine> aroad = checkconflict.getRoadLinesByStartandEndIndex(aroadLines, nowdis.aindex);
                List<Coordinate> ao = checkconflict.convertRoadlineToPath(aroad);
                double anglea = angDisUtil.getAngleByZB(ao.get(0), ao.get(1));
                String[] roadidsa = checkconflict.getroadids(aroad);
                ConflictRoadMes ames = new ConflictRoadMes(ao, a.getOrderRoadNames(), nowdis.aindex, roadidsa, ao.get(0), 0, 0, anglea);

                List<RoadLine> broad = checkconflict.getRoadLinesByStartandEndIndex(broadLines, nowdis.bindex);
                List<Coordinate> bo = checkconflict.convertRoadlineToPath(broad);
                double angleb = angDisUtil.getAngleByZB(bo.get(0), bo.get(1));
                String[] roadidsb = checkconflict.getroadids(broad);
                ConflictRoadMes bmes = new ConflictRoadMes(bo, b.getOrderRoadNames(), nowdis.bindex, roadidsb, bo.get(0), 0, 0, angleb);

                roadConflictMes.setMaxnum(nowdis.type.equals("trailing") ? 999 : 1);
                roadConflictMes.mesMap.put(ida, ames);
                roadConflictMes.mesMap.put(idb, bmes);
                conflicts.add(roadConflictMes);
            }

        }
        return conflicts;
    }


    public  List<RoadConflictMes> getconflictmap(ShpAttribute a, ShpAttribute b,String ida,String idb, Map<String, Map<String, WeightArea>> weightareamaps) {

        List<RoadConflictMes> conflicts = updateRoadConflictMesMap(a, b,ida,idb);
        conflicts = conflicts == null ? new ArrayList<>() : conflicts;
        //       Map<String, WeightArea> weightlinesmap = weightareamaps.get("runwayinoutflag");
        Map<String, WeightArea> weightlinesmap = weightareamaps.get("default");
        for (Map.Entry<String, WeightArea> entry : weightlinesmap.entrySet()) {
            WeightArea weightArea = entry.getValue();
            ConflictRoadMes priorordermes = null;
            ConflictRoadMes secondordermes = null;
            updatepriroadlinesandnextroadlines(priorordermes, secondordermes, a, weightArea);
            updatepriroadlinesandnextroadlines(priorordermes, secondordermes, b, weightArea);
            if(priorordermes!=null&&secondordermes!=null){
                conflicts = updateconflicts(priorordermes, secondordermes, conflicts, a, b,ida,idb);
            }
        }
        return conflicts;
    }

    private void updatepriroadlinesandnextroadlines(ConflictRoadMes priorordermes, ConflictRoadMes secondordermes, ShpAttribute b, WeightArea weightArea) {
        ShpAttribute shpAttribute = b;
        List<RoadLine> roadLines = shpAttribute.getRoadlines();
        String[] priordirs = weightArea.getPriordirs();

        String[] crosslines = weightArea.getCrosslines();
        int[] index = null;
        for (int j = 0; j < roadLines.size(); j++) {
            RoadLine roadLine = roadLines.get(j);
            for (int i = 0; i < crosslines.length; i++) {
                if (roadLine.getId().equals(crosslines[i])) {
                    if (index == null) {
                        index = new int[]{j, j};
                    } else {
                        index[1] = j;
                    }
                }
            }
        }

        boolean isinprior = false;
        for (int z = 0; z < priordirs.length; z++) {
            String priordir = priordirs[z];
            if (priordir.equals(shpAttribute.getRunway() + shpAttribute.getInoutflag())) {
                isinprior = true;
            }
        }
        if (index != null) {
            AngDisUtil angDisUtil = new AngDisUtil();
            checkConflict checkconflict = new checkConflict();
            List<RoadLine> aroadlines = checkconflict.getRoadLinesByStartandEndIndex(roadLines, index);
            List<Coordinate> coordinates = checkconflict.convertRoadlineToPath(aroadlines);
            double angle = angDisUtil.getAngleByZB(coordinates.get(0), coordinates.get(1));
            String[] roadids = checkconflict.getroadids(aroadlines);
            if (isinprior) {
                priorordermes = new ConflictRoadMes(coordinates, shpAttribute.getOrderRoadNames(), index, roadids, coordinates.get(0), weightArea.getWaitdis(), weightArea.getPriordis(), angle);
            } else {
                secondordermes = new ConflictRoadMes(coordinates, shpAttribute.getOrderRoadNames(), index, roadids, coordinates.get(0), weightArea.getWaitdis(), weightArea.getPriordis(), angle);
            }
        }

    }

    private List<RoadConflictMes> updateconflicts(ConflictRoadMes p, ConflictRoadMes e, List<RoadConflictMes> cs, ShpAttribute a, ShpAttribute b,String pln,String eln) {
        List<RoadLine> al = a.getRoadlines();
        List<RoadLine> bl = b.getRoadlines();
        boolean isin = false;
        int[] pric = p.getIndexab();
        int[] prie = e.getIndexab();
        AngDisUtil angDisUtil = new AngDisUtil();
        for (int i = 0; i < cs.size(); i++) {
            RoadConflictMes m = cs.get(i);
            Map<String, ConflictRoadMes> mesMaphis = m.mesMap;
            int[] repin = isin(mesMaphis.get(pln).getIndexab(), pric);
            int[] reein = isin(prie, mesMaphis.get(eln).getIndexab());
            if (repin[0] != -999 && reein[0] != -999) {
                checkConflict c = new checkConflict();
                List<RoadLine> aroad = c.getRoadLinesByStartandEndIndex(al, repin);
                List<Coordinate> ao = c.convertRoadlineToPath(aroad);
                double angela = angDisUtil.getAngleByZB(ao.get(0), ao.get(1));
                String[] roadidsa = c.getroadids(aroad);
                ConflictRoadMes ames = new ConflictRoadMes(ao, pln, repin, roadidsa, ao.get(0), p.getWaitdis(), p.getPriordis(), angela);
                List<RoadLine> broad = c.getRoadLinesByStartandEndIndex(bl, reein);
                List<Coordinate> bo = c.convertRoadlineToPath(broad);
                double angelb = angDisUtil.getAngleByZB(bo.get(0), bo.get(1));
                String[] roadidsb = c.getroadids(broad);
                ConflictRoadMes bmes = new ConflictRoadMes(bo, eln, reein, roadidsb, bo.get(0), p.getWaitdis(), e.getPriordis(), angelb);
                m.setMaxnum(m.type.equals("trailing") ? 999 : 1);
                m.mesMap.put(pln, ames);
                m.mesMap.put(eln, bmes);
                cs.set(i, m);
                isin = true;
            }
        }
        if (!isin) {
            Map<String, ConflictRoadMes> mesMap = new HashMap<>();
            mesMap.put(pln, p);
            mesMap.put(eln, e);
            RoadConflictMes rs = new RoadConflictMes();
            rs.setType("cross");
            rs.setMaxnum(1);
            rs.setMesMap(mesMap);
            cs.add(rs);
        }
        return cs;
    }

    private int[] isin(int[] ab1, int[] ab2) {
        int[] re = new int[]{-999, -999};
        boolean ab20 = ab1[0] <= ab2[0] && ab1[1] >= ab2[0];
        boolean ab10 = ab2[0] <= ab1[0] && ab2[1] >= ab1[0];
        boolean ab21 = ab1[0] <= ab2[1] && ab1[1] >= ab2[1];
        boolean ab11 = ab2[0] <= ab1[1] && ab2[1] >= ab1[1];
        if (ab20 && ab11) {
            re[0] = ab1[0];
            re[1] = ab2[1];

        }
        if (ab10 && ab11) {
            re[0] = ab2[0];
            re[1] = ab2[1];

        }
        if (ab21 && ab10) {
            re[0] = ab2[0];
            re[1] = ab1[1];

        }
        if (ab20 && ab21) {
            re[0] = ab1[0];
            re[1] = ab1[1];

        }
        return re;

    }

}
