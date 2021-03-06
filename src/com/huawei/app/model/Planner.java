package com.huawei.app.model;

import com.huawei.app.Simulator.SimStatus;

/**
 * 
 * @author zwp12
 *
 * >路径规划器接口
 *
 */
public interface Planner {

	
	/**
	 * 
	 * 
	 * 设置所有车辆自定义请求上路最早出发时间，
	 * 
	 * 若返回数值小于原始出发时间时，所有车辆以默认出发时间向规划器请求上路
	 * 
	 * 
	 * @param newStartTime
	 */
	
	public int  onInitCarStartTime(int carId);
	
	
	/**
	 * >返回carId车接下来要进入的RoadId
	 * @return 正常返回一个道路ID,curCrossId已经和目的地相同，则需要返回-1
	 * 如果下一条道路的id和当前车的id相同，将报出异常
	 * 
	 */
	public int onScheduling(int carId,int curCrossId,SimStatus ss);
	

	/**
	 * 当前准备上路的车,根据路况返回是否可以上路
	 * remCars 表示当期模拟器中车辆的数量
	 * @return
	 */
	public boolean onTryStart(int carId,int crossId,SimStatus ss);
	
	/**
	 *  车辆正式上路时通知
	 * @param carId
	 * @param crossId
	 * @param curRemCar
	 * @return
	 */
	public void onStart(int carId,int crossId,SimStatus ss);
	
	/**
	 *  >当某车通过路口时，通知规划器
	 * @param carId
	 * @param curCrossId
	 * @param ss
	 * @return
	 */
	public void onPassedCross(int carId,int curCrossId,SimStatus ss);
	
	
	/**
	 *  >当某车到达变道区准备通过路口时，通知规划器
	 * @param carId
	 * @param curCrossId
	 * @param ss
	 * @return
	 */
	public void onPassedRoad(int carId,int roaId,SimStatus ss);
	
	/**
	 * 表示car在curSAT到达路口crossId,结束行程
	 * remCars 表示当期模拟器中车辆的数量
	 * @return
	 */
	public boolean onStop(int carId,int crossId,SimStatus ss);
	
	
}
