package com.conflict;


/**
 * @description://用于记录整条路线上的所有可能存在的冲突类型，记录完毕之后，提出出路段信息，用于冲突检测。
 * @author: LiPin
 * @time: 2023-01-03 17:54
 */
public class ConflictRoadIndex {
    public  int[] aindex=new int[]{-9999,-9999};
    public  int []bindex=new int[]{-9999,-9999};
    public  String type;
    public ConflictRoadIndex copy() {
        ConflictRoadIndex copy = new ConflictRoadIndex();
        copy.aindex = this.aindex;
        copy.bindex = this.bindex;
        copy.type = this.type;
        return copy;
    }
    public ConflictRoadIndex() {

    }

    public int[] getAindex() {
        return aindex;
    }

    public void setAindex(int[] aindex) {
        this.aindex = aindex;
    }

    public int[] getBindex() {
        return bindex;
    }

    public void setBindex(int[] bindex) {
        this.bindex = bindex;
    }

    public ConflictRoadIndex(int[] aindex, int []bindex, String type) {
        this.aindex = aindex;
        this.bindex = bindex;
        this.type = type;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
