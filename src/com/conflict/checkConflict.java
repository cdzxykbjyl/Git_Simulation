package com.conflict;

import base.BlhToGauss;
import base.Line;
import fun.RoadLine;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-06-03 20:04
 */

public class checkConflict {

    public String[] getroadids(List<RoadLine> roadlines) {
        String[] ids = new String[roadlines.size()];
        for (int i = 0; i < roadlines.size(); i++) {
            ids[i] = roadlines.get(i).getId();
        }
        return ids;
    }

    public String getequlType(RoadLine aroadline, RoadLine broadline) {
        String Type = null;
        //路段或者路口，ID相同，完全重合的路段
        if (aroadline.getIndex() == broadline.getIndex()) {
            if (aroadline.getDirection().equals(broadline.getDirection())) {
                int innum = getinnum(aroadline.geometry.getCoordinates(), broadline.geometry.getCoordinates());
                if (innum >= 2) {
                    Type = "trailing";
                }
            } else {
                int innum = getinnum(aroadline.geometry.reverse().getCoordinates(), broadline.geometry.getCoordinates());
                if (innum >= 2) {
                    Type = "confrontation";
                }
            }
        }
        return Type;
    }

    //获取是两个数据集合是否时重合的点
    private int getinnum(Coordinate[] a1, Coordinate[] b1) {
        int equalnum = 0;

        //完全重合的点
        for (int i = 0; i < a1.length; i++) {
            for (int j = 0; j < b1.length; j++) {
                boolean isequal = a1[i].equals(b1[j]);
                if (isequal) {
                    equalnum = equalnum + 1;
                }
            }
        }

        //点在线上
        int innum = 0;
        for (int i = 0; i < a1.length - 1; i++) {
            Coordinate pre = a1[i];
            Coordinate enr = a1[i + 1];
            for (int j = 0; j < b1.length; j++) {
                Coordinate mi = b1[j];
                boolean is = new Line().isOnLine(mi, pre, enr);
                if (is) {
                    innum = innum + 1;
                }
            }
        }
        //点在线上
        int innum2 = 0;
        for (int i = 0; i < b1.length - 1; i++) {
            Coordinate pre = b1[i];
            Coordinate enr = b1[i + 1];
            for (int j = 0; j < a1.length; j++) {
                Coordinate mi = a1[j];
                boolean is = new Line().isOnLine(mi, pre, enr);
                if (is) {
                    innum2 = innum2 + 1;
                }
            }
        }
        return equalnum + innum + innum2;
    }


    //优化和综合后地预冲突路段信息
    public List<ConflictRoadIndex> getConflictRoadIndex(List<RoadLine> roads_a, List<RoadLine> roads_b) {
        List<ConflictRoadIndex> conflictRoadIndices = new ArrayList<>();
        List<ConflictRoadIndex> conflictRoadIndicescross = new ArrayList<>();
        for (int i = 0; i < roads_a.size(); i++) {
            for (int j = 0; j < roads_b.size(); j++) {
                RoadLine aroadline = roads_a.get(i);
                RoadLine broadline = roads_b.get(j);
                String type = getequlType(aroadline, broadline);
                if (type != null) {
                    ConflictRoadIndex conflictRoadIndex = new ConflictRoadIndex(new int[]{i, i}, new int[]{j, j}, type);
                    conflictRoadIndices.add(conflictRoadIndex);
                }
                String type2 = getCrossType(aroadline, broadline);
                if (type2 != null) {
                    ConflictRoadIndex conflictRoadIndex = new ConflictRoadIndex(new int[]{i, i}, new int[]{j, j}, type2);
                    conflictRoadIndicescross.add(conflictRoadIndex);
                }
            }
        }
        List<List<ConflictRoadIndex>> segConflictRoadIndexs = segConflictRoadIndex(conflictRoadIndices);
        List<ConflictRoadIndex> reconflictRoadIndices = new ArrayList<>();
        for (int i = 0; i < segConflictRoadIndexs.size(); i++) {
            List<ConflictRoadIndex> iniseg = segConflictRoadIndexs.get(i);
            reconflictRoadIndices.addAll(updateConflictRoadIndices(iniseg));
        }
        int size0 = reconflictRoadIndices.size();

        for (int i = 0; i < size0; i++) {
            ConflictRoadIndex iniseg = reconflictRoadIndices.get(i).copy();
            if (iniseg.type.equals("trailing")) {
                iniseg = addCrossByTrailing(iniseg, roads_a, roads_b);
                if (i < size0 - 1) {
                    for (int j = i + 1; j < size0; j++) {
                        ConflictRoadIndex next = reconflictRoadIndices.get(j).copy();
                        List<ConflictRoadIndex> inisegk = new ArrayList<>();
                        inisegk.add(iniseg);
                        inisegk.add(next);
                        List<ConflictRoadIndex> reconflictRoadIndicesk = updateConflictRoadIndices(inisegk);
                        if (reconflictRoadIndicesk.size() == 1) {
                            iniseg = reconflictRoadIndicesk.get(0).copy();
                            reconflictRoadIndices.set(i, iniseg);
                            reconflictRoadIndices.remove(j);
                            size0--;
                            j--;
                        }
                    }
                }
                reconflictRoadIndices.set(i, iniseg);
            } else if (iniseg.type.equals("confrontation")) {
                iniseg = AddCrossByConfrontation(iniseg, roads_a, roads_b);
                if (i < size0 - 1) {
                    for (int j = i + 1; j < size0; j++) {
                        ConflictRoadIndex next = reconflictRoadIndices.get(j).copy();
                        List<ConflictRoadIndex> inisegk = new ArrayList<>();
                        inisegk.add(iniseg);
                        inisegk.add(next);
                        List<ConflictRoadIndex> reconflictRoadIndicesk = updateConflictRoadIndices(inisegk);
                        if (reconflictRoadIndicesk.size() == 1) {
                            iniseg = reconflictRoadIndicesk.get(0).copy();
                            reconflictRoadIndices.set(i, iniseg);
                            reconflictRoadIndices.remove(j);
                            size0--;
                            j--;
                        }
                    }
                }
                reconflictRoadIndices.set(i, iniseg);
            }
        }
        for (int k = 0; k < reconflictRoadIndices.size(); k++) {
            ConflictRoadIndex c = reconflictRoadIndices.get(k);
            int size = conflictRoadIndicescross.size();
            for (int j = 0; j < size; j++) {
                ConflictRoadIndex cross = conflictRoadIndicescross.get(j);
                if (c.aindex[0] <= cross.aindex[0] && cross.aindex[0] <= c.aindex[1]) {
                    conflictRoadIndicescross.remove(j);
                    size--;
                    j--;
                }
            }
        }
        int sizecorss = conflictRoadIndicescross.size();
        for (int i = 0; i < sizecorss; i++) {
            ConflictRoadIndex iniseg = conflictRoadIndicescross.get(i);
            iniseg = addCrossByCross(iniseg, roads_a, roads_b);
            if (i < sizecorss - 1) {
                for (int j = i + 1; j < sizecorss; j++) {
                    ConflictRoadIndex next = conflictRoadIndicescross.get(j).copy();
                    if (iniseg.aindex[0] <= next.aindex[0] && iniseg.aindex[1] <= next.aindex[1] &&
                            iniseg.bindex[0] <= next.bindex[0] && iniseg.bindex[1] <= next.bindex[1]
                    ) {
                        conflictRoadIndicescross.remove(j);
                        sizecorss--;
                        j--;
                    }
                }
            }
            conflictRoadIndicescross.set(i, iniseg);
        }
        int sizes = conflictRoadIndicescross.size() - 1;
        for (int i = 0; i < sizes; i++) {
            ConflictRoadIndex iniseg = conflictRoadIndicescross.get(i);
            ConflictRoadIndex next = conflictRoadIndicescross.get(i + 1);
            if (next.aindex[0] - iniseg.aindex[1] == 1) {
                iniseg.aindex[1] = next.aindex[0];
                if (iniseg.bindex[0] < next.bindex[0]) {
                    //顺；
                    iniseg.bindex[1] = next.bindex[1];
                    iniseg.setType("trailing");
                    sizes--;
                    conflictRoadIndicescross.remove(i + 1);
                    i--;
                } else {
                    if (iniseg.bindex[0] - next.bindex[1] == 1) {
                        //逆时针
                        iniseg.bindex[0] = next.bindex[0];
                        iniseg.setType("confrontation");
                        sizes--;
                        conflictRoadIndicescross.remove(i + 1);
                        i--;
                    }
                }
            }
        }
        if (conflictRoadIndicescross.size() != 0) {
            if (conflictRoadIndicescross.size() == 1) {
                reconflictRoadIndices.addAll(conflictRoadIndicescross);
            } else {
                List<ConflictRoadIndex> upcross = updateCross(conflictRoadIndicescross);
                reconflictRoadIndices.addAll(upcross);
            }
        }

        return reconflictRoadIndices;
    }

    //判断两个线是否属于交叉类型
    private String getCrossType(RoadLine aroadline, RoadLine broadline) {
        String Type = null;
        if (aroadline.junname.equals(broadline.junname) && (!aroadline.getNetworkname().equals("runway")) && !broadline.junname.equals("") && aroadline.getIndex() != broadline.getIndex()) {
            Type = "cross";
        }
        return Type;
    }


    private List<List<ConflictRoadIndex>> segConflictRoadIndex(List<ConflictRoadIndex> conflictRoadIndices) {
        List<List<ConflictRoadIndex>> conflicts = new ArrayList<>();
        List<ConflictRoadIndex> dk = new ArrayList<>();
        for (int j = 0; j < conflictRoadIndices.size(); j++) {
            if (j == 0) {
                dk.add(conflictRoadIndices.get(0));
            } else {
                ConflictRoadIndex last = dk.get(dk.size() - 1);
                if (last.type.equals(conflictRoadIndices.get(j).type)) {
                    dk.add(conflictRoadIndices.get(j));
                } else {
                    conflicts.add(dk);
                    dk = new ArrayList<>();
                    dk.add(conflictRoadIndices.get(j));
                }
            }
        }
        if (dk.size() != 0) {
            conflicts.add(dk);
        }
        return conflicts;
    }


    public ConflictRoadIndex AddCrossByConfrontation(ConflictRoadIndex conflictRoadIndex, List<RoadLine> roads_a, List<RoadLine> roads_b) {

        if (conflictRoadIndex.aindex[1] < roads_a.size() - 1 && conflictRoadIndex.bindex[0] > 1) {
            int aendindex = conflictRoadIndex.aindex[1] + 1;
            int bstartindex = conflictRoadIndex.bindex[0] - 1;
            ConflictRoadIndex conflictRoadIndexx = new ConflictRoadIndex(new int[]{aendindex, aendindex}, new int[]{bstartindex, bstartindex}, conflictRoadIndex.type);
            if (roads_a.get(aendindex).getJunname().equals(roads_b.get(bstartindex).getJunname())) {
                boolean isin1 = false;
                for (int j = aendindex; j < roads_a.size(); j++) {
                    if (roads_a.get(aendindex).getJunname().equals(roads_a.get(j).getJunname()) && !isin1) {
                        conflictRoadIndexx.aindex[1] = j;
                    } else {
                        isin1 = true;
                    }
                }
                boolean isin2 = false;
                for (int j = bstartindex; j > -1; j--) {
                    if (roads_b.get(bstartindex).getJunname().equals(roads_b.get(j).getJunname()) && !isin2) {
                        conflictRoadIndexx.bindex[0] = j;
                    } else {
                        isin2 = true;
                    }
                }
                conflictRoadIndex.aindex[1] = conflictRoadIndexx.aindex[1];
                conflictRoadIndex.bindex[0] = conflictRoadIndexx.bindex[0];
            }
        }

        if (conflictRoadIndex.aindex[0] > 1 && conflictRoadIndex.bindex[1] < roads_b.size() - 1) {
            int astartindex = conflictRoadIndex.aindex[0] - 1;
            int bendindex = conflictRoadIndex.bindex[1] + 1;
            if (roads_a.get(astartindex).getJunname().equals(roads_b.get(bendindex).getJunname())) {
                ConflictRoadIndex conflictRoadIndexx = new ConflictRoadIndex(new int[]{astartindex, astartindex}, new int[]{bendindex, bendindex}, conflictRoadIndex.type);
                boolean isin1 = false;
                for (int j = astartindex; j > -1; j--) {
                    if (roads_a.get(astartindex).getJunname().equals(roads_a.get(j).getJunname()) && !isin1) {
                        conflictRoadIndexx.aindex[0] = j;
                    } else {
                        isin1 = true;
                    }
                }
                boolean isin2 = false;
                for (int j = bendindex; j < roads_b.size(); j++) {
                    if (roads_b.get(bendindex).getJunname().equals(roads_b.get(j).getJunname()) && !isin2) {
                        conflictRoadIndexx.bindex[1] = j;
                    } else {
                        isin2 = true;
                    }
                }
                conflictRoadIndex.aindex[0] = conflictRoadIndexx.aindex[0];
                conflictRoadIndex.bindex[1] = conflictRoadIndexx.bindex[1];
            }
        }
        return conflictRoadIndex;
    }

    public ConflictRoadIndex addCrossByTrailing(ConflictRoadIndex conflictRoadIndex, List<RoadLine> roads_a, List<RoadLine> roads_b) {
        if (conflictRoadIndex.aindex[1] < roads_a.size() - 1 && conflictRoadIndex.bindex[1] < roads_b.size() - 1) {
            int aendindex = conflictRoadIndex.aindex[1] + 1;
            int bendindex = conflictRoadIndex.bindex[1] + 1;
            if (roads_a.get(aendindex).getJunname().equals(roads_b.get(bendindex).getJunname())) {
                ConflictRoadIndex conflictRoadIndexx = new ConflictRoadIndex(new int[]{aendindex, aendindex}, new int[]{bendindex, bendindex}, conflictRoadIndex.type);
                for (int j = aendindex; j < roads_a.size(); j++) {
                    if (roads_a.get(aendindex).getJunname().equals(roads_a.get(j).getJunname())) {
                        conflictRoadIndexx.aindex[1] = j;
                    }
                }
                for (int j = bendindex; j < roads_b.size(); j++) {
                    if (roads_b.get(bendindex).getJunname().equals(roads_b.get(j).getJunname())) {
                        conflictRoadIndexx.bindex[1] = j;
                    }
                }
                conflictRoadIndex.aindex[1] = conflictRoadIndexx.aindex[1];
                conflictRoadIndex.bindex[1] = conflictRoadIndexx.bindex[1];
            }
        }
        if (conflictRoadIndex.aindex[0] > 1 && conflictRoadIndex.bindex[0] > 1) {
            int astartindex = conflictRoadIndex.aindex[0] - 1;
            int bstartindex = conflictRoadIndex.bindex[0] - 1;
            if (roads_a.get(astartindex).getJunname().equals(roads_b.get(bstartindex).getJunname())) {
                ConflictRoadIndex conflictRoadIndexx = new ConflictRoadIndex(new int[]{astartindex, astartindex}, new int[]{bstartindex, bstartindex}, conflictRoadIndex.type);
                for (int j = astartindex; j > -1; j--) {
                    if (roads_a.get(astartindex).getJunname().equals(roads_a.get(j).getJunname())) {
                        conflictRoadIndexx.aindex[0] = j;
                    }
                }
                for (int j = bstartindex; j > -1; j--) {
                    if (roads_b.get(bstartindex).getJunname().equals(roads_b.get(j).getJunname())) {
                        conflictRoadIndexx.bindex[0] = j;
                    }
                }
                conflictRoadIndex.aindex[0] = conflictRoadIndexx.aindex[0];
                conflictRoadIndex.bindex[0] = conflictRoadIndexx.bindex[0];
            }
        }
        return conflictRoadIndex;
    }


    public List<ConflictRoadIndex> updateConflictRoadIndices(List<ConflictRoadIndex> conflictRoadIndices) {
        List<ConflictRoadIndex> reconflictRoadIndices = new ArrayList<>();
        if (conflictRoadIndices.size() != 0) {
            ConflictRoadIndex yx = conflictRoadIndices.get(0).copy();
            String type = yx.type;
            for (int i = 1; i < conflictRoadIndices.size(); i++) {
                ConflictRoadIndex conflictRoadIndexi = conflictRoadIndices.get(i);
                boolean isin = false;
                if (conflictRoadIndexi.aindex[0] - yx.aindex[1] == 1) {
                    yx.aindex[1] = conflictRoadIndexi.aindex[1];
                    isin = true;
                }
                if (!isin) {
                    reconflictRoadIndices.add(yx);
                    yx = conflictRoadIndexi.copy();
                }
                if (type.equals("trailing") || type.equals("cross")) {
                    if (conflictRoadIndexi.bindex[0] - yx.bindex[1] == 1) {
                        yx.bindex[1] = conflictRoadIndexi.bindex[1];
                    }
                } else if (type.equals("confrontation")) {
                    if (yx.bindex[0] - conflictRoadIndexi.bindex[1] == 1) {
                        yx.bindex[0] = conflictRoadIndexi.bindex[0];
                    }
                }
            }
            reconflictRoadIndices.add(yx);
        }
        return reconflictRoadIndices;
    }


    public ConflictRoadIndex addCrossByCross(ConflictRoadIndex conflictRoadIndex, List<RoadLine> roads_a, List<RoadLine> roads_b) {
        String junctionname = roads_a.get(conflictRoadIndex.aindex[1]).junname;
        if (conflictRoadIndex.aindex[1] < roads_a.size() - 1) {
            boolean isend = false;
            for (int i = conflictRoadIndex.aindex[1] + 1; i < roads_a.size(); i++) {
                String name2 = roads_a.get(i).junname;
                if (junctionname.equals(name2) && !isend) {
                    conflictRoadIndex.aindex[1] = i;
                } else {
                    isend = true;
                }
            }
        }

        if (conflictRoadIndex.bindex[1] < roads_b.size() - 1) {
            boolean isend = false;
            for (int i = conflictRoadIndex.bindex[1] + 1; i < roads_b.size(); i++) {
                String name2 = roads_b.get(i).junname;
                if (junctionname.equals(name2) && !isend) {
                    conflictRoadIndex.bindex[1] = i;
                } else {
                    isend = true;
                }
            }
        }
        if (conflictRoadIndex.aindex[0] > 0) {
            boolean isend = false;
            for (int i = conflictRoadIndex.aindex[0] - 1; i > -1; i--) {
                String name2 = roads_a.get(i).junname;
                if (junctionname.equals(name2) && !isend) {
                    conflictRoadIndex.aindex[0] = i;
                } else {
                    isend = true;
                }
            }
        }

        if (conflictRoadIndex.bindex[0] > 0) {
            boolean isend = false;
            for (int i = conflictRoadIndex.bindex[0] - 1; i > -1; i--) {
                String name2 = roads_b.get(i).junname;
                if (junctionname.equals(name2) && !isend) {
                    conflictRoadIndex.bindex[0] = i;
                } else {
                    isend = true;
                }
            }
        }
        return conflictRoadIndex;
    }

    private List<ConflictRoadIndex> updateCross(List<ConflictRoadIndex> conflictRoadIndicescross) {
        ConflictRoadIndex last = null;
        List<ConflictRoadIndex> endclearByend = new ArrayList<>();
        for (int i = 0; i < conflictRoadIndicescross.size(); i++) {
            if (i == 0) {
                last = conflictRoadIndicescross.get(i).copy();
            } else {
                ConflictRoadIndex now = conflictRoadIndicescross.get(i);
                if (last.aindex[0] == now.aindex[0] && last.aindex[1] == now.aindex[1]) {
                    if (now.bindex[1] == last.bindex[1] + 1) {
                        last.bindex[1] = now.bindex[1];
                    }
                } else {
                    endclearByend.add(last);
                    last = conflictRoadIndicescross.get(i).copy();
                }
            }
        }
        if (last != null) {
            endclearByend.add(last);
        }
        List<ConflictRoadIndex> endclearByfir = new ArrayList<>();
        for (int i = 0; i < endclearByend.size(); i++) {
            if (i == 0) {
                last = endclearByend.get(i).copy();
            } else {
                ConflictRoadIndex now = endclearByend.get(i);
                if (last.bindex[0] == now.bindex[0] && last.bindex[1] == now.bindex[1]) {
                    if (now.aindex[1] == last.aindex[1] + 1) {
                        last.aindex[1] = now.aindex[1];
                    }
                } else {
                    endclearByfir.add(last);
                    last = endclearByend.get(i).copy();
                }
            }
        }
        if (last != null) {
            endclearByfir.add(last);
        }
        return endclearByfir;
    }

    public List<RoadLine> getRoadLinesByStartandEndIndex(List<RoadLine> roads, int[] index) {
        int start = index[0];
        int end = index[1];
        List<RoadLine> roadLines = new ArrayList<>();
        if (start < end) {
            for (int i = start; i <= end; i++) {
                roadLines.add(roads.get(i));
            }
        }
        if (start > end) {
            int temp = end;
            end = start;
            start = temp;
            for (int i = start; i <= end; i++) {
                roadLines.add(roads.get(i));
            }
        }
        if (start == end) {
            roadLines.add(roads.get(start));
        }
        return roadLines;
    }

    public List<ConflictRoadIndex> UpdateStands(String inoutflgA, String inoutflagB, List<RoadLine> aroadLines, List<RoadLine> broadLines, List<ConflictRoadIndex> conflictRoadIndes) {


        if (inoutflgA.equals("A")) {
            ConflictRoadIndex conflictRoadIndex = conflictRoadIndes.get(conflictRoadIndes.size() - 1);
            int[] s = new int[]{conflictRoadIndex.aindex[0], conflictRoadIndex.aindex[1]};
            s = updateStand(aroadLines, s);
            conflictRoadIndex.aindex[0] = s[0];
            conflictRoadIndex.aindex[1] = s[1];
            conflictRoadIndes.set(conflictRoadIndes.size() - 1, conflictRoadIndex);
        } else {
            ConflictRoadIndex conflictRoadIndex = conflictRoadIndes.get(0);
            int[] s = new int[]{conflictRoadIndex.aindex[0], conflictRoadIndex.aindex[1]};
            s = updateStand(aroadLines, s);
            conflictRoadIndex.aindex[0] = s[0];
            conflictRoadIndex.aindex[1] = s[1];
            conflictRoadIndes.set(0, conflictRoadIndex);
        }
        if (inoutflagB.equals("A")) {
            ConflictRoadIndex conflictRoadIndex = conflictRoadIndes.get(conflictRoadIndes.size() - 1);
            int[] s = new int[]{conflictRoadIndex.bindex[0], conflictRoadIndex.bindex[1]};
            s = updateStand(broadLines, s);
            conflictRoadIndex.bindex[0] = s[0];
            conflictRoadIndex.bindex[1] = s[1];
            conflictRoadIndes.set(conflictRoadIndes.size() - 1, conflictRoadIndex);
        } else {
            ConflictRoadIndex conflictRoadIndex = conflictRoadIndes.get(0);
            int[] s = new int[]{conflictRoadIndex.bindex[0], conflictRoadIndex.bindex[1]};
            s = updateStand(broadLines, s);
            conflictRoadIndex.bindex[0] = s[0];
            conflictRoadIndex.bindex[1] = s[1];
            conflictRoadIndes.set(0, conflictRoadIndex);
        }
        return conflictRoadIndes;
    }

    private int[] updateStand(List<RoadLine> roadLines, int[] s) {
        int start = s[0];
        int end = s[1];
        if (start > 0) {
            boolean isend = false;
            for (int i = start - 1; i > -1; i--) {
                if (roadLines.get(i).getNetworkname().equals("apron") && !isend) {
                    start = i;

                } else {
                    isend = true;

                }
            }
        }
        if (end < roadLines.size() - 1) {
            boolean isend = false;
            for (int i = end + 1; i < roadLines.size(); i++) {
                if (roadLines.get(i).getNetworkname().equals("apron") && !isend) {
                    end = i;
                } else {
                    isend = true;

                }
            }
        }
        return new int[]{start, end};
    }


    public List<Coordinate> convertRoadlineToPath(List<RoadLine> roadLines) {
        List<Coordinate> path = new ArrayList<>();
        for (int i = 0; i < roadLines.size(); i++) {
            Coordinate[] coordinates = new Coordinate[]{};
            if (roadLines.get(i).direction.equals("positive")) {
                coordinates = roadLines.get(i).geometry.getCoordinates();
            } else {
                coordinates = roadLines.get(i).geometry.reverse().getCoordinates();
            }
            List<Coordinate> coordinateslist = new ArrayList<>();
            if (i > 0) {
                for (int j = 1; j < coordinates.length; j++) {
                    coordinateslist.add(coordinates[j]);
                }
            } else {
                for (int j = 0; j < coordinates.length; j++) {
                    coordinateslist.add(coordinates[j]);
                }
            }
            path.addAll(coordinateslist);
        }
        return path;
    }
}
