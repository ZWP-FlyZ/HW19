package com.huawei.app.model;

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

	
}
