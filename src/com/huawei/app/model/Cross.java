package com.huawei.app.model;

import java.util.Map;

/**
 * 
 * @author zwp12
 *
 * >交叉路口定义
 * > 存有相接Road的一些冗余形象
 */

public class Cross {

	
	private final Integer crossId;// 当前Croass的id
	
	// 按顺时针排序Road的id,
	private final int[] connRoadIds;
	// 注意这里的ID是指可以从这个路口出去的Road
	// 当无出去的路时，为-1;
	private int[] connOutRoadIds= {-1,-1,-1,-1};
	// 无相连的Cross时，为-1;
	private int[] connCrossIds;// 按顺时针排序Cross的ID
	
	public Cross(int crossId,int[] connRoadIds) {
		this.crossId=crossId;
		this.connRoadIds=connRoadIds;
	}

	/**
	 * 
	 * >根据当前方向车辆进入计算下向前、向左、向右的RoadId
	 * >若放回-1 表示选择的方向上无Road或者无效输入
	 * @param orderId 
	 * @return 
	 */
	public int getNextRoadId(int currentDirect, int needNextDriveDirect) {
		if(currentDirect<Direction.N||currentDirect>Direction.W||
				needNextDriveDirect<DriveDirection.LEFT||needNextDriveDirect<DriveDirection.RIGHT)
			return -1;// 无效方向
		else
			return connOutRoadIds[(currentDirect+4+needNextDriveDirect)%4];
	}
	

	/**
	 *  >通过出路口的roadId计算该Road相对于路口的方向
	 *  >例如知道路在上方，但不能说from到to的绝对方向
	 * @param roadId
	 * @return dirction
	 * 
	 */
	public int getDirectionByRoadId(int outRoadId) {
		int res = 0;
		for(;res<4;res++) 
			if(connRoadIds[res]==outRoadId) break;
		if (res==4) 
			throw new IllegalArgumentException("outRoadId not in connRoadIds");
		return res;
	}
	
	
	
	
	/**
	 * > 通过这个函数、更新connOutRoadIds中的roadId
	 * @param dirction 绝对方向
	 * @param roadId 
	 */
	public void setConnOutRoadIds(int dirction,int roadId) {
		connOutRoadIds[dirction]=roadId;
	}

	/**
	 * 
	 */
	public void updateConnCrossIds(Map<Integer,Road> roads) {

	}
	
	
	public Integer getCrossId() {
		return crossId;
	}

	public int[] getConnRoadIds() {
		return connRoadIds;
	}

	public int[] getConnCrossIds() {
		return connCrossIds;
	}
	
	
	
	
	
}
