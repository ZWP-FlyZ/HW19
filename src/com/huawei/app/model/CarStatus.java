package com.huawei.app.model;

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
	public int curDirct;// 当前行驶方向，
						//只是简单指代,当从from到to的车是为0，否则为1
	public int curSpeed;// 当前行驶的速度，由这个数值确定是否进入规划区
	public int curRoadId;//当前行驶的RoadId
	public int curChannelId;//当前行驶的通道Id
	public int curChannelLocal;//当前车处于通道的位置
	// 当前车辆默认处于启动阶段
	public CarActions action=CarActions.START;
	
	// 当前系统的绝对时间
	public int SAT;//
	
	
	// 注意以下两个值可以在实际通过路口时更改，并且重新加入优先队列选择中
	// 下一条路的ID,可以是假设最优的下一条路径，但并不是最优
	// 如果已经是在最后一条道路上了，则为-1;
	public int nextRoadId;
	// 接下来车的转向，该值也为假设值，若不按照原定方向行驶，
	// 可以更改当前值，
	// 如果无下一条路径则设为直行
	public int turnDirected;
	
	
	public enum CarActions{
		START,//当前车辆处于启动行为
		RUNNING,//处于可被调度行为
	}
	
	public CarStatus(int carId,Car car,int SAT) {
		this.carId = carId;this.car=car;
		this.SAT = SAT;
	}
	
	
	@Override
	public int compareTo(CarStatus o) {
		// TODO Auto-generated method stub
		
		// 时间最优先
		if(SAT!=o.SAT) return SAT-o.SAT;
		// 准备上路的车，以车id小的优先，
		if((action==o.action)
				&&action==CarActions.START) 
			return carId-o.carId;
		
		// 优先处理处于可被调度的车
		if(action!=o.action) {
			if(action==CarActions.START) 
				return 1;// 
			else return -1;
		}

		//action = RUNNING
		// 如果都处于可被调度情况下
		
		
		
		
		// 优先处理车道小的车
		if(curChannelId!=o.curChannelId) {
			return curChannelId-o.curChannelId;
		}
		
		//路口根据转向处理
		if(turnDirected!=o.turnDirected) {
			// 优先处理直行的车
			if(turnDirected==DriveDirection.RIGHT) return 1;
			else if(turnDirected==DriveDirection.FOWARD) return -1;
			else if(o.turnDirected == DriveDirection.FOWARD) return 1;
			else return -1;
		}
		
		// 优先处理同一行驶方向from到to方向比to到from的处理优先级更高
		if(curDirct!=o.curDirct)
			return curDirct-curDirct;
		
		
		// 车道相同的车，优先处理同一条路中的所有车
		// 注意这里可能有问题、无法辨别出哪些道路的最小车道会被先调度
		// 因此存在一种情况：下一条道路没有先被调度，后一条道路出现阻塞的情况
		if(curRoadId != o.curRoadId) {
			if(curRoadId==o.nextRoadId) 
				return -1;// 当前道路是o的将要进入的道路，则优先处理curRoadId
			else if(nextRoadId==o.curRoadId)
				return 1;// 
			//这里会优先处理车道大的车
			return o.curRoadId-curRoadId; 
		}
			
		
		return 0;
	}

}
