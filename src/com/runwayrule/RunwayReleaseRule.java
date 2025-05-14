package com.runwayrule;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-05-30 15:49
 */

public class RunwayReleaseRule {
    Map<String, ReleaseByRunwayNo> releaseByRunwayNoMap = new HashMap<>();
    Map<String, AirlineRelease> airlineReleaseMap = new HashMap<>();
    Map<String, ReleaseAfterAircraft> releaseAfterAircraftMap = new HashMap<>();
    Map<String, ReleaseByInoutflag> releaseByInoutflagHashMap = new HashMap<>();

    public RunwayReleaseRule(Map<String, ReleaseByRunwayNo> releaseByRunwayNoMap,
                             Map<String, AirlineRelease> airlineReleaseMap,
                             Map<String, ReleaseAfterAircraft> releaseAfterAircraftMap,
                             Map<String, ReleaseByInoutflag> releaseByInoutflagHashMap) {
        this.releaseByRunwayNoMap = releaseByRunwayNoMap;
        this.airlineReleaseMap = airlineReleaseMap;
        this.releaseAfterAircraftMap = releaseAfterAircraftMap;
        this.releaseByInoutflagHashMap = releaseByInoutflagHashMap;
    }

    public Map<String, ReleaseByRunwayNo> getReleaseByRunwayNoMap() {
        return releaseByRunwayNoMap;
    }

    public void setReleaseByRunwayNoMap(Map<String, ReleaseByRunwayNo> releaseByRunwayNoMap) {
        this.releaseByRunwayNoMap = releaseByRunwayNoMap;
    }

    public Map<String, AirlineRelease> getAirlineReleaseMap() {
        return airlineReleaseMap;
    }

    public void setAirlineReleaseMap(Map<String, AirlineRelease> airlineReleaseMap) {
        this.airlineReleaseMap = airlineReleaseMap;
    }

    public Map<String, ReleaseAfterAircraft> getReleaseAfterAircraftMap() {
        return releaseAfterAircraftMap;
    }

    public void setReleaseAfterAircraftMap(Map<String, ReleaseAfterAircraft> releaseAfterAircraftMap) {
        this.releaseAfterAircraftMap = releaseAfterAircraftMap;
    }

    public Map<String, ReleaseByInoutflag> getReleaseByInoutflagHashMap() {
        return releaseByInoutflagHashMap;
    }

    public void setReleaseByInoutflagHashMap(Map<String, ReleaseByInoutflag> releaseByInoutflagHashMap) {
        this.releaseByInoutflagHashMap = releaseByInoutflagHashMap;
    }
}

