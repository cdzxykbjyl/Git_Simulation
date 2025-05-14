package com.order;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @author: LiPin
 * @time: 2023-02-06 14:34
 */
public class OrderFatherMes {
    public  String key="";
    public String type = "";
    public String time = "";
    public String starttime = "";
    public String endtime = "";
    Logger log = LoggerFactory.getLogger(this.getClass());

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getMes() {
        JSONObject jsonObject = new JSONObject();
        switch (this.type) {
            case "instand":
            case "outstand":
                InoutStand inoutStand = (InoutStand) this;
                jsonObject = inoutStand.getMsg();
                break;
            case "removebridge"://撤销桥梁
            case "buttbridge"://对接桥梁
                AbuttedGallery abuttedGallery = (AbuttedGallery) this;
                jsonObject = abuttedGallery.getMsg();
                break;
            case "closegear":
            case "opengear":
                LandingGear landingGear = (LandingGear) this;
                jsonObject = landingGear.getMsg();
                break;
            case "landing":
            case "takeoff":
                OffandLand offandLand = (OffandLand) this;
                jsonObject = offandLand.getMsg();
                break;
            case "offline":
                Offline offline = (Offline) this;
                jsonObject = offline.getMsg();
                break;
            case "taxiwait":
            case "zbdwait":
            case "pushoutwait":
                TaxiWait taxWait = (TaxiWait) this;
                jsonObject = taxWait.getMsg();
                break;
            case "trailing":
            case "cross":
            case "confrontation":
                ConflictOrderFatherMes conflictMes = (ConflictOrderFatherMes) this;
                jsonObject = conflictMes.getMsg();
                break;
            case "runwayoccupy":
                RunawayWeit runawayWeit = (RunawayWeit) this;
                jsonObject = runawayWeit.getMsg();
                break;
            case "solidstand":
                StandOccupyOrderFatherMes standOccupyMes = (StandOccupyOrderFatherMes) this;
                jsonObject = standOccupyMes.getMsg();
                break;
            case "dashstand":
                standOccupyMes = (StandOccupyOrderFatherMes) this;
                jsonObject = standOccupyMes.getMsg();
                break;
            default:
                log.info("新增加的消息类型，Class :Simulation,function:getOrderMes()\t" + this.type);
                break;
        }
        return jsonObject;
    }

}
