package com.huawei.app.model;

public class Answer {

	private int carId;
	private int startTime;
	private int[] roadIds ;
	
	public Answer(int carId,int startTime,int[] roadIds) {
		this.carId = carId;
		this.startTime=startTime;
		this.roadIds = roadIds;
	}
	
	public Answer(int[] args) {
		this.carId = args[0];
		this.startTime=args[1];
		roadIds = new int[args.length-2];
		for(int i=2;i<args.length;i++)
			roadIds[i-2]=args[i];
	}
	
	public int getCarId() {
		return carId;
	}
	public int getStartTime() {
		return startTime;
	}

	public int[] getRoadIds() {
		return roadIds;
	}

	
	
	
	
}
