package com.fileoperation;

/**
 * @description: ***
 * @author: LiPin
 * @time: 2024-03-27 15:42
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.conflict.HisTimeNodes;
import com.conflict.RunwayFlightHis;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;

public class ExcelWriter {
    Logger log;

    public ExcelWriter(Logger log) {
        this.log = log;
    }

    public void createRunwayQueneexcel(String filepath, String sheetname, Map<String, HisTimeNodes> hisTimeNodes) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        //创建HSSFSheet对象
        HSSFSheet sheet = workbook.createSheet(sheetname);
        //创建行的单元格，从0开始

        int i = 0;
        for (Map.Entry<String, HisTimeNodes> entry : hisTimeNodes.entrySet()) {
            HisTimeNodes runwayFlightHis1 = entry.getValue();
            JSONObject js = runwayFlightHis1.geMes();
            HSSFRow row = sheet.createRow(i);
            int k = 0;
            if (i == 0) {
                k = 0;
                for (String key : js.keySet()) {
                    row.createCell(k).setCellValue(key);
                    k++;
                }
                i++;
            }
            row = sheet.createRow(i);
            k = 0;
            for (String key : js.keySet()) {
                row.createCell(k).setCellValue(js.getString(key));
                k++;
            }
            i++;
        }
        //创建文档信息
        workbook.createInformationProperties();
        //文档输出
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filepath);
            workbook.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            log.error(filepath + "文件找不到！" + e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void createRunwayQueneexcel2(String filepath, String sheetname, Map<Long,Map<String, List<RunwayFlightHis>>> timeHisdirectrunwaymap ) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        //创建HSSFSheet对象
        HSSFSheet sheet = workbook.createSheet(sheetname);
        //创建行的单元格，从0开始

        ArrayList<String> fightIdRecord = new ArrayList<>();
        int i = 0;
        for ( Map.Entry<Long, Map<String, List<RunwayFlightHis>>> entry : timeHisdirectrunwaymap.entrySet()) {
            Long timeHisdirectrunwaymap_key = entry.getKey();
            Map<String, List<RunwayFlightHis>> timeHisdirectrunwaymap_value = entry.getValue();
            for(Map.Entry<String, List<RunwayFlightHis>> inner_entry : timeHisdirectrunwaymap_value.entrySet()){
                String timeHisdirectrunwaymap_value_key = inner_entry.getKey();
                List<RunwayFlightHis> timeHisdirectrunwaymap_value_value = inner_entry.getValue();
                for(RunwayFlightHis timeHisdirectrunwaymap_value_value_element : timeHisdirectrunwaymap_value_value){
                    JSONObject js = timeHisdirectrunwaymap_value_value_element.getMes();
                    HSSFRow row = sheet.createRow(i);
                    int k = 0;
                    if (i == 0) {
                        k = 0;
                        row.createCell(k).setCellValue("Time");
                        row.createCell(k).setCellValue("Runway");
                        for (String key : js.keySet()) {
                            row.createCell(k).setCellValue(key);
                            k++;
                        }
                        i++;
                    }
                    k = 0;
                    String flightId = js.getString("flightId");
                    if(!fightIdRecord.contains(flightId)){
                        fightIdRecord.add(flightId);
                        row = sheet.createRow(i);
                        row.createCell(k).setCellValue(timeHisdirectrunwaymap_key);
                        row.createCell(k).setCellValue(timeHisdirectrunwaymap_value_key);
                        for (String key : js.keySet()) {
                            row.createCell(k).setCellValue(js.getString(key));
                            k++;
                        }
                        i++;
                    }
                }
            }
        }
        //创建文档信息
        workbook.createInformationProperties();
        //文档输出
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filepath);
            workbook.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            log.error(filepath + "文件找不到！" + e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
