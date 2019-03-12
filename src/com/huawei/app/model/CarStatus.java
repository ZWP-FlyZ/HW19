package com.huawei.app.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author zwp12
 *
 * >汽车所有状态描述
 *
 */
public class CarStatus implements Comparable<CarStatus>{

	public int carId;//车辆ID
	public Car car;
	
	public int relStartTime;// 真实出发时间
	// 记录所有路过的RoadId
	private List<Integer> passedRoadRec= new LinkedList<>(); 
	
	//在当前道路上该车最大可行的速度
	//由该速度确定是否切换RUNNING为SCHEDULING
	// min(car.maxspeed,road.maxspeed)
	public int curRoadSpeed;

	// 最近一个路过的路口
	public int frmCrossId;
	// 将要驶向的路口ID
	public int tagCrossId;
	public int curRoadId;//当前行驶的RoadId
	public int curChannelId;//当前行驶的通道Id
	
	// 最近一次状态被更新绝对时间
	public int curSAT;
	// 最近一次状态更新车处于通道的位置
	public int curChannelLocal;
	
	// 当前车辆处于行为，
	// 默认处于启动阶段
	public CarActions action=CarActions.START;
	
	
	// 注意以下两个值可以在实际通过路口时更改，并且重新加入优先队列选择中
	// 下一条路的ID,可以是假设最优的下一条路径，但并不是最优
	// 如果已经是在最后一条道路上了，则为-1;
	public int nextRoadId;
	// 接下来车的转向，该值也为假设值，若不按照原定方向行驶，
	// 可以更改当前值，
	// 如果无下一条路径则设为直行 DriveDirection.FORWARD
	public int turnDirected;
	
	
	public enum CarActions{
		/*
		 *  >当前车辆处于准备上路行为中，
		 *  >与处于SCHEDULING行为的车一样处于路口调度中
		 */
		START,
		/*
		 * >处于正常行驶，
		 * >处于该行为的车，会被车辆位置实时更新任务更新
		 * >当车进入变道区内，状态被改为SCHEDULING
		 */
		RUNNING,
		/*
		 * >处于路口调度行为中
		 * >当被正确路口调度后，状态会被改为RUNNING
		 */
		SCHEDULING,
		/*
		 * >提前处于路口调度行为中
		 * >当被正确路口调度后，状态会被改为SCHEDULING
		 */
		BLOCK_SCHEDULING,		
		
		/* 
		 * >车辆将要结束行程
		 * >参与到路口调度当中
		 */
		STOP;

		
	}
	
	public CarStatus(int carId,Car car,int SAT) {
		this.carId = carId;this.car=car;
		this.curSAT = SAT;
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
	
	
	
	@Override
	public int compareTo(CarStatus o) {
		// TODO Auto-generated method stub
		// 时间最优先
		if(curSAT!=o.curSAT) return curSAT-o.curSAT;
		
		/**
		 * >通过以下优先调度， 这里最终优先顺序，
		 * >在路、路口、车道、行驶方向中随机执行
		 * 
		 */
		
//		// 优先处理crossId小的路口
//		if(tagCrossId!=o.tagCrossId) 
//			return tagCrossId-o.tagCrossId;
		
		if(action==CarActions.RUNNING) {
			// 处理处于RUNNING的节点	
			// 位置在前的车优先更新位置
			return o.curChannelLocal-curChannelLocal;
		}else if(action==CarActions.RUNNING){
			// 处理SCHEDULING

			// 优先处理处于可被调度的车
			// 优先调度位置在前的车
			if(o.curChannelId!=curChannelLocal)
				return o.curChannelLocal-curChannelLocal;
			
			// 优先调度车道小的车
			if(curChannelId!=o.curChannelId)
				return curChannelId-o.curChannelId;	
			
			//路口根据转向
			if(turnDirected!=o.turnDirected) {
				// 优先处理直行的车
				if(turnDirected==DriveDirection.RIGHT) return 1;
				else if(turnDirected==DriveDirection.FOWARD) return -1;
				else if(o.turnDirected == DriveDirection.FOWARD) return 1;
				else return -1;
			}
						
			
			// 车道相同的车，优先处理同一条路中的所有车
			// 注意这里可能有问题、无法辨别出哪些道路的最小车道会被先调度
			// 因此存在一种情况：下一条道路没有先被调度，后一条道路出现阻塞的情况
//			if(curRoadId != o.curRoadId) {
//				if(curRoadId==o.nextRoadId) 
//					return -1;// 当前道路是o的将要进入的道路，则优先处理curRoadId
//				else if(nextRoadId==o.curRoadId)
//					return 1;// 
//				//这里会优先处理道路ID大的车
////				return o.curRoadId-curRoadId; 
//			}
			
			return 0;
		}else if(action==CarActions.START) {
			
			// 准备上路的车，以车id小的优先，
			return carId-o.carId;			
		}
		
	return 0;

	}

}
