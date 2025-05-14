package com.order;


import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description://标准指令语言
 * @author: LiPin
 * @time: 2022-05-18 22:02
 */
public class StandardWords {
    String parkingChinese;
    String parkingEnglish;
    String[] roadIDs;
    String inoutflag;
    List<Coordinate> coordinates;
    String discribeChinese;
    String discribeEnglish;

    public StandardWords(
            String parkingChinese,
            String parkingEnglish,
            String [] roadIDs,
            String discribeChinese,
            String discribeEnglish,
            String inoutflag,
            List<Coordinate> coordinates) {

        this.parkingChinese = parkingChinese;
        this.parkingEnglish = parkingEnglish;
        this.roadIDs=roadIDs;
        this. discribeEnglish=discribeEnglish;
        this. discribeChinese=discribeChinese;
        this.inoutflag = inoutflag;
        this.coordinates = coordinates;

    }

    public static void main(String[] args) {

        Map<String,StandardWords> standardWordsMap = new HashMap<>();
        standardWordsMap.put("西机坪Y-P2-CrossRWY02L-P1-T4-E",new StandardWords("西机坪","West Apron", new String[]{"Y","P2","CrossRWY02L","P1","T4 ","E"}, "沿Y滑行道向北滑行，左转加入P2滑行道，在02L跑道外等待，得到穿越跑道许可后沿P1穿越02L跑道，继续沿T4滑行道，左转沿E滑行道滑行，根据管制员指令进位。","Taxi northbound on taxiway D, and turn right on taxiway T3, then turn left on taxiway A and turn right on taxiway A1, hold short of runway20R.",
                "A",new ArrayList<>()));


    }

    public String getParkingChinese() {
        return parkingChinese;
    }

    public void setParkingChinese(String parkingChinese) {
        this.parkingChinese = parkingChinese;
    }

    public String getParkingEnglish() {
        return parkingEnglish;
    }

    public void setParkingEnglish(String parkingEnglish) {
        this.parkingEnglish = parkingEnglish;
    }

    public String[] getRoadIDs() {
        return roadIDs;
    }

    public void setRoadIDs(String[] roadIDs) {
        this.roadIDs = roadIDs;
    }

    public String getInoutflag() {
        return inoutflag;
    }

    public void setInoutflag(String inoutflag) {
        this.inoutflag = inoutflag;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public String getDiscribeChinese() {
        return discribeChinese;
    }

    public void setDiscribeChinese(String discribeChinese) {
        this.discribeChinese = discribeChinese;
    }

    public String getDiscribeEnglish() {
        return discribeEnglish;
    }

    public void setDiscribeEnglish(String discribeEnglish) {
        this.discribeEnglish = discribeEnglish;
    }

}
