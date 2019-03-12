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
	

	/**
	 * 当前准备上路的车,根据路况返回是否可以上路
	 * remCars 表示当期模拟器中车辆的数量
	 * @return
	 */
	public boolean feed(int carId,int crossId,int remCars);
	
	
}
