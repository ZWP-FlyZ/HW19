package com.huawei.app.model;

/**
 * 
 * @author zwp12
 *
 * >车道的类
 *
 */
public class RoadChannel {

	private int roadId=0;
	private int channelId=0;
	private int carCot=0;// 通道内车辆计数
	// 当前车道新车进入的初始速度
	// 当 当前车道无车时，车道速度恢复为 Road的 maxSpeed;
	private int cMaxSpeed;
	// 车道的长度、与道路的长度相同
	private int channelLength;
	private Car[] channel =null;
	
	public RoadChannel(int roadId,int channelId,int maxSpeed,int channelLength) {
		this.roadId=roadId;this.channelId=channelId;
		this.cMaxSpeed=maxSpeed;this.channelLength=channelLength;
	}
	
	public int getRoadId() {
		return roadId;
	}
	public int getChannelId() {
		return channelId;
	}
	public int getCarCot() {
		return carCot;
	}

	public int incAndGetCarCot() {
		return ++carCot;
	}

	public int decAndGetCarCot() {
		return --carCot;
	}
	
	public int getcMaxSpeed() {
		return cMaxSpeed;
	}
	public void setcMaxSpeed(int cMaxSpeed) {
		this.cMaxSpeed = cMaxSpeed;
	}
	public int getChannelLength() {
		return channelLength;
	}

	/**
	 * lazy 生成车道空间
	 * @return
	 */
	public Car[] getChanel() {
		if(channel==null)
			channel = new Car[channelLength]; 
		return channel;
	}

	
	
	
}
