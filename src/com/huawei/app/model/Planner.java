package com.huawei.app.model;

/**
 * 
 * @author zwp12
 *
 * >路径规划器接口
 *
 */
public interface Planner {

	
	/**
	 * >返回carId车接下来要进入的RoadId
	 * @return 如果返回-1表示无效
	 * 
	 */
	public int next(int carId,int curCrossId);
	

	
}
