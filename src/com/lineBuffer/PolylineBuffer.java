package com.lineBuffer;


import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @description://线缓冲区
 * @author: LiPin
 * @time: 2022-05-18 22:02
 */




public class PolylineBuffer {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		List<Coordinate> datas = new ArrayList<>();
		datas.add(new Coordinate(36.52, 117.33));
		datas.add(new Coordinate(36.71, 118.41));
		datas.add(new Coordinate(37.13, 119.33));
		PolylineBuffer polylineBuffer=new PolylineBuffer();
		String result = polylineBuffer.getLineBufferEdgeCoords(datas, 0.1);
		//System.out.println(result);
		String strs[] = result.split(",");
		for (int i = 0; i < strs.length / 2; i++) {
			//System.out.println(strs[2 * i] + "\t" + strs[2 * i + 1]);
		}
	}
	public  String getLineBufferEdgeCoords(List<Coordinate> coords, double radius) {
		String leftBufferCoords = getLeftBufferEdgeCoords(coords, radius);
		Collections.reverse(coords);
		String rightBufferCoords = getLeftBufferEdgeCoords(coords, radius);
		return leftBufferCoords + "," + rightBufferCoords;
	}


	private  String getLeftBufferEdgeCoords(List<Coordinate> coords, double radius) {

		if (coords.size() < 1)
			return "";
		double alpha = 0.0;//
		double delta = 0.0;//
		double l = 0.0;//

		StringBuilder strCoords = new StringBuilder();
		double startRadian = 0.0;
		double endRadian = 0.0;
		double beta = 0.0;
		double x = 0.0, y = 0.0;
		{
			alpha = getQuadrantAngle(coords.get(0), coords.get(1));
			startRadian = alpha + Math.PI;
			endRadian = alpha + (3 * Math.PI) / 2;
			strCoords.append(getBufferCoordsByRadian(coords.get(0), startRadian, endRadian, radius));
		}
		for (int i = 1; i < coords.size() - 1; i++) {
			alpha = getQuadrantAngle(coords.get(i), coords.get(i + 1));
			delta = getIncludedAngel(coords.get(i - 1), coords.get(i), coords.get(i + 1));
			l = getVectorProduct(coords.get(i - 1), coords.get(i), coords.get(i + 1));
			if (l > 0) {
				startRadian = alpha + (3 * Math.PI) / 2 - delta;
				endRadian = alpha + (3 * Math.PI) / 2;
				if (strCoords.length() > 0)
					strCoords.append(",");
				strCoords.append(getBufferCoordsByRadian(coords.get(i), startRadian, endRadian, radius));
			} else if (l < 0) {
				beta = alpha - (Math.PI - delta) / 2;
				x = coords.get(i).getY() + radius * Math.cos(beta);
				y = coords.get(i).getX() + radius * Math.sin(beta);
				if (strCoords.length() > 0)
					strCoords.append(",");
				strCoords.append(x + "," + y);
			}
		}

		{
			alpha = getQuadrantAngle(coords.get(coords.size() - 2), coords.get(coords.size() - 1));
			startRadian = alpha + (3 * Math.PI) / 2;
			endRadian = alpha + 2 * Math.PI;
			if (strCoords.length() > 0)
				strCoords.append(",");
			strCoords.append(getBufferCoordsByRadian(coords.get(coords.size() - 1), startRadian, endRadian, radius));
		}
		return strCoords.toString();
	}

	private  String getBufferCoordsByRadian(Coordinate center, double startRadian, double endRadian, double radius) {
		double gamma = Math.PI / 6;
		StringBuilder strCoords = new StringBuilder();
		double x = 0.0, y = 0.0;
		for (double phi = startRadian; phi <= endRadian + 0.000000000000001; phi += gamma) {
			x = center.getY() + radius * Math.cos(phi);
			y = center.getX() + radius * Math.sin(phi);
			if (strCoords.length() > 0)
				strCoords.append(",");
			strCoords.append(x + "," + y);
		}
		return strCoords.toString();
	}

	private  double getVectorProduct(Coordinate preCoord, Coordinate midCoord, Coordinate nextCoord) {
		return (midCoord.getY() - preCoord.getY()) * (nextCoord.getX() - midCoord.getX())
				- (nextCoord.getY() - midCoord.getY())
						* (midCoord.getX() - preCoord.getX());
	}
	public  double getQuadrantAngle(Coordinate preCoord, Coordinate nextCoord) {
		return getQuadrantAngle(nextCoord.getY() - preCoord.getY(),
				nextCoord.getX() - preCoord.getX());
	}

	public  double getQuadrantAngle(double x, double y) {
		double theta = Math.atan(y / x);
		if (x > 0 && y > 0)
			return theta;
		if (x > 0 && y < 0)
			return Math.PI * 2 + theta;
		if (x < 0 && y > 0)
			return theta + Math.PI;
		if (x < 0 && y < 0)
			return theta + Math.PI;
		return theta;
	}

	public  double getIncludedAngel(Coordinate preCoord, Coordinate midCoord, Coordinate nextCoord) {
		double innerProduct = (midCoord.getY() - preCoord.getY())
				* (nextCoord.getY() - midCoord.getY())
				+ (midCoord.getX() - preCoord.getX())
				* (nextCoord.getX() - midCoord.getX());
		double mode1 = Math.sqrt(Math.pow((midCoord.getY() - preCoord.getY()), 2.0)
				+ Math.pow((midCoord.getX() - preCoord.getX()), 2.0));
		double mode2 = Math.sqrt(Math.pow((nextCoord.getY() - midCoord.getY()), 2.0)
				+ Math.pow((nextCoord.getX() - midCoord.getX()), 2.0));
		return Math.acos(innerProduct / (mode1 * mode2));
	}


	public  double getDistance(Coordinate preCoord, Coordinate nextCoord) {
		return Math.sqrt(Math.pow((nextCoord.getY() - preCoord.getY()), 2)
				+ Math.pow((nextCoord.getX() - preCoord.getX()), 2));
	}
}
