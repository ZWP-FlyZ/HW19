package com.huawei.app.model;

/**
 * 
 * 
 * @author zwp12
 *
 * >道路的描述类型
 *
 *
 */
public class Road {

	private Integer roadId;// 路ID
	private int roadLength;// Road的 长度
	private int maxSpeed;// Road的 最大速度
	private int chanelCot;// 车道数
	private int fromCrossId;// 起始路口
	private int toCrossId;// 终点路口
	private int isDu ;// 是否为双向通道
	
	//从当前from到to节点推出绝对方向
	//如果时双向通道需要变更
	private int direction;
	
	// 从from 到 to 方向的所有车道
	// 默认车道
	// lazy创建
	private RoadChannel[] fromChannels = null; 
	
	// 如果当前车道是双向通道,则toChannels,表示
	// to 到 from 方向上所有车道，只双向通道时被使用
	private RoadChannel[] toChannels = null; 
	
	
	
	
	/**
	 * >道路通道描述
	 * @author zwp12
	 *
	 */

	
	public Road(int roadId,int roadLength,int maxSpeed,
			int chanelCot,int fromCrossId,int toCrossId,int isDu) {
		this.roadId=roadId;this.roadLength=roadLength;
		this.maxSpeed=maxSpeed;this.chanelCot=chanelCot;
		this.fromCrossId=fromCrossId;this.toCrossId=toCrossId;
		this.isDu=isDu;
	}
	
	public boolean isDuplex() {
		return this.isDu==1;
	}
	
	/**
	 *  > 该路与crossId的路口相连，并且存在车道进入crossId，获得
	 *  >方向指向crossId的所有车道
	 *  
	 * @param crossId
	 * @return
	 */
	public RoadChannel[] getInCrossChannels(int crossId) {
		
		if(isDuplex()&&fromCrossId==crossId) {
			// 双向通道，并且to到from方向
			if(toChannels==null) initToRoadChannels();
			return toChannels;
		}
		if(toCrossId!=crossId) 
			throw new IllegalArgumentException("toCrossId !=crossId err");
		if(fromChannels==null) initFromRoadChannels();
		
		return fromChannels;
	}
	
	/**
	 *  > 该路与crossId的路口相连，并且存在车道从crossId出去，获得
	 *  >方向从crossId的出去所有车道
	 * @param crossId
	 * @return
	 */
	public RoadChannel[] getOutCrossChannels(int crossId) {
		
		if(isDuplex()&&toCrossId==crossId) {
			// 双向通道，并且from到to方向
			if(toChannels==null) initToRoadChannels();
			return toChannels;
		}
		
		if(fromCrossId!=crossId) 
			throw new IllegalArgumentException("fromCrossId !=crossId err");
		if(fromChannels==null) initFromRoadChannels();
		return fromChannels;		
		
	}
	
	
	
	/**
	 *  >由相连crossId路口推出当前路从from到to的绝对方向。
	 * @param roadDirection
	 * @param crossId
	 */
	public void updateDirection(int roadDirection,int crossId) {
		if(isDuplex()&&toCrossId==crossId) 
			// 反向处理
			direction = (roadDirection+2)%4;
		if(fromCrossId!=crossId) 
			throw new IllegalArgumentException("fromCrossId !=crossId err");
		direction = roadDirection;
	}
	
	
	/**
	 * 
	 * >计数以crossId作为进入路口时，当前路相对于路口的方向
	 * 
	 * @param roadDirection
	 * @param crossId
	 */
	
	public int getInDirection(int crossId) {
		if(isDuplex()&&fromCrossId==crossId) 
			// 反向处理
			return (direction+2)%4;
		if(fromCrossId!=crossId) 
			throw new IllegalArgumentException("fromCrossId !=crossId err");
		return direction;
	}
	
	
	
	
	
	private void initFromRoadChannels() {
		fromChannels= new RoadChannel[chanelCot];
		for(int i=0;i<chanelCot;i++)
			fromChannels[i]= new RoadChannel(roadId, i, maxSpeed, roadLength);
	}

	private void initToRoadChannels() {
		toChannels= new RoadChannel[chanelCot];
		for(int i=0;i<chanelCot;i++)
			toChannels[i]= new RoadChannel(roadId, i, maxSpeed, roadLength);
	}
	

}
