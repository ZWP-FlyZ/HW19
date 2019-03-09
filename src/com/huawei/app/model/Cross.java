package com.huawei.app.model;

import java.util.Arrays;
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
//	private int[] connCrossIds;// 按顺时针排序Cross的ID
	
	public Cross(int crossId,int[] connRoadIds) {
		this.crossId=crossId;
		this.connRoadIds=connRoadIds;
	}
	
	public Cross(int[] args) {
		this.crossId=args[0];
		this.connRoadIds=Arrays.copyOfRange(args, 1, 5);
	}

	/**
	 * 
	 * >根据当前车辆行驶路径相对于路口的位置，计算下向前、向左、向右的RoadId
	 * > 可以通过getDirectionByRoadId 计算roadDirectInCross
	 * >若放回-1 表示选择的方向上无Road或者无效输入
	 * @param orderId 
	 * @return 
	 */
	public int getNextRoadId(int roadDirectInCross, int needNextDriveDirect) {
		if(roadDirectInCross<Direction.N||roadDirectInCross>Direction.W||
				needNextDriveDirect<DriveDirection.LEFT||needNextDriveDirect<DriveDirection.RIGHT)
			return -1;// 无效方向
		else
			return connOutRoadIds[(roadDirectInCross+6+needNextDriveDirect)%4];
	}
	

	/**
	 *  >通过出路口的roadId计算该Road相对于路口的位置
	 *  >例如知道路在上方
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
	 * >根据进入的Road的相对于路口的位置和出去的RoadId判断，该行驶属于向左、直行、向右中的哪种
	 * >通过 getDirectionByRoadId计算过inRoadDirection
	 * @param inRoadId
	 * @param outRoadId
	 * @return DriveDirection.LEFT RIGHT FORWARD
	 */
	public int getTurnDirection(int inRoadDirection,int outRoadId) {
		if(connOutRoadIds[(inRoadDirection+1)%4]==outRoadId) 
			return DriveDirection.LEFT;
		else if(connOutRoadIds[(inRoadDirection+2)%4]==outRoadId)
			return DriveDirection.FOWARD;
		else if(connOutRoadIds[(inRoadDirection+3)%4]==outRoadId)
			return DriveDirection.RIGHT;
		else 
			throw new IllegalArgumentException("outRoadId not in connOutRoadIds");
	}
	
	
	
	/**
	 * > 这个函数更新connOutRoadIds中的roadId
	 * 
	 */
	public void setConnOutRoadIds(Map<Integer,Road> roads) {
		int rid;
		Road rd = null;
		for(int i=0;i<4;i++) {
			if((rid=connRoadIds[i])<0) continue;
			rd = roads.get(rid);
			if(rd.isDuplex()||rd.getFromCrossId()==crossId) 
				connOutRoadIds[i]=rid;	
		}
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


	
	
	
	
}
