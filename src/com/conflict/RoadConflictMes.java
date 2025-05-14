package com.conflict;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-06-03 15:21
 */

public class RoadConflictMes {
    public String type = "cross";
    public int maxnum = 999;//最大可占据数量
    public Map<String, ConflictRoadMes> mesMap = new HashMap<>();
    public Map<String, ConflictRoadMes> getMesMap() {
        return mesMap;
    }
    public void setMesMap(Map<String, ConflictRoadMes> mesMap) {
        this.mesMap = mesMap;
    }
    public int getMaxnum() {
        return maxnum;
    }
    public void setMaxnum(int maxnum) {
        this.maxnum = maxnum;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
