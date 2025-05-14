package com.evaluate;

import base.Time;
import com.order.OrderFatherMes;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 评价信息
 * 包括飞机的航班号、
 * 飞机的起始滑行时间、
 * 终止滑行时间、
 * 总滑行时间、
 * 遇到的冲突次数、
 * 冲突的具体信息（包括冲突的飞机、冲突的坐标、路段、冲突类型等）
 * @author: LiPin
 * @time: 2022-08-08 17:22
 */
public class OrderofEvaluate {
    private String starttime = "";//滑行开始时间
    private String duretime = "";//滑行时长
    private String endtime = "";//滑行结束时间
    private List<OrderFatherMes> ordermessages = new ArrayList();//指令消息
    private List<OrderFatherMes> conflictmessages = new ArrayList();//冲突消息



    public String getStarttime() {
        return starttime;
    }

    public List<OrderFatherMes> getOrdermessages() {
        return ordermessages;
    }

    public void setOrdermessages(List<OrderFatherMes> ordermessages) {
        this.ordermessages = ordermessages;
    }

    public List<OrderFatherMes> getConflictmessages() {
        return conflictmessages;
    }

    public void setConflictmessages(List<OrderFatherMes> conflictmessages) {
        this.conflictmessages = conflictmessages;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getDuretime() {
        return duretime;
    }

    public void setDuretime(String duretime) {
        this.duretime = duretime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
        this.duretime = Time.getTimeDuration(this.endtime,
                this.starttime);
    }

    public OrderofEvaluate() {
    }

    @Override
    public String toString() {
        return "{" +
                " \"starttime\":" + starttime + "\"" +
                ", \"duretime\":" + duretime + "\"" +
                ", \"endtime\":" + endtime + "\"" +
                ", \"ordermessages\":" + ordermessages +
                ", \"conflictmessages\":" + conflictmessages +
                "}";
    }

    public JSONObject getMes() {
        JSONObject js = new JSONObject();
        js.put("starttime", starttime);
        js.put("duretime", duretime);
        js.put("endtime", endtime);
        JSONArray jsonArray1 = new JSONArray();
        for (int i = 0; i < ordermessages.size(); i++) {
            jsonArray1.add(ordermessages.get(i).getMes());
        }
        JSONArray jsonArray2 = new JSONArray();
        for (int i = 0; i < conflictmessages.size(); i++) {
            jsonArray2.add(conflictmessages.get(i).getMes());
        }
        js.put("ordermessages", jsonArray1);
        js.put("conflictmessages", jsonArray2);
        return js;
    }


}
