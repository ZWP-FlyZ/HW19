package com.huawei.app.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author zwp12
 *
 * >车辆定义
 *
 */
public class Car {

	private Integer carId;//车辆ID
	private int oriCrossId ;//出发地点的CrossId
	private int desCrossId;// 目的地
	private int maxSpeed;// 车辆最高速度
	private int startTime;//出发时刻
	
	private int relStartTime;// 真实出发时间
	// 记录所有路过的RoadId
	private List<Integer> passedRoadRec= new LinkedList<>();
	
	public Car(int carId,int oriCrossId,int desCrossId,
			int maxSpeed,int startTime) {
		this.carId=carId;this.oriCrossId=oriCrossId;
		this.desCrossId=desCrossId;this.maxSpeed=maxSpeed;
		this.startTime=startTime;
	}
	
	public Car(int[] args) {
		this.carId=args[0];this.oriCrossId=args[1];
		this.desCrossId=args[2];this.maxSpeed=args[3];
		this.startTime=args[4];
	}	
	

	// 添加车辆经过的roadId
	public void addPassedRoad(int roadId) {

		passedRoadRec.add(roadId);
	}

	/**
	 *  >生成当前汽车的行程
	 * @return
	 */
	public String getReport() {
		StringBuilder sb =new StringBuilder();
		sb.append("(");
		sb.append(carId);
		sb.append(","+relStartTime);
		passedRoadRec.forEach(v->sb.append(","+v));
		sb.append(")");
		return sb.toString();
	}
	

	public Integer getCarId() {
		return carId;
	}


	public int getOriCrossId() {
		return oriCrossId;
	}


	public int getDesCrossId() {
		return desCrossId;
	}


	public int getMaxSpeed() {
		return maxSpeed;
	}


	public int getStartTime() {
		return startTime;
	}


	public int getRelStartTime() {
		return relStartTime;
	}
	
	public void setRelStartTime(int relStartTime) {
		this.relStartTime = relStartTime;
	}

	public List<Integer> getPassedRoadRec() {
		return passedRoadRec;
	}
	

	
}
