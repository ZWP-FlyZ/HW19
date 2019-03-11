package com.huawei.app;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import com.huawei.app.Application.Context;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.CarStatus.CarActions;
import com.huawei.app.model.Cross;
import com.huawei.app.model.DriveDirection;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;

/**
 * 
 * >模拟执行系统调配过程
 * 
 * @author zwp12
 *
 * > 
 * > 
 * > 模拟过程是一个实时系统，每一时刻都执行车辆实时位置更新子任务和路口调度任务子任务
 * > 首先全道路实时更新处于RUNNING行为的车的位置，然后处理处于SCHEDULING和START行为的车
 * > 每辆车在当前车道上可行最大车速V1、后一车道最大速度为V2,在路口前v1长度和路口后v2长度为，该车在路口的变道区
 * > 
 * > 1.首先处理位置更新队列中的所有车辆的位置，当更新的后的位置处于变道区时，则让规划器计算下一步的路径,
 * > 更新CarStatus相关信息，设置SCHEDULING行为,并加入到路口调度队列当中
 * > 2.首先车辆检查能否前行至路口，有车辆阻挡或者有残影占位时，将车辆前移。
 * > 如果能够前行到路口，则检查下一道路能否容纳新车辆，如果不能将车辆前移
 * > 如果能顺利通过路口，则检查当前路口是否为车辆的结束地点，若不是则将CarStatus更改相关信息，设置为RUNNING行为。
 *
 */
public class Simulator {

	private static final Logger logger = Logger.getLogger(Simulator.class);
	private Context ctx = null;
    private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    private Map<Integer,CarStatus> statues=null;
    
    // 更新所有车在道路中位置的优先队列，
    // 所有RUNNING行为的车
    private PriorityQueue<CarStatus> runningQue = null;
    
    // 所有需要被路口调度的车,
    // 所有SCHDULING、START的车
    private PriorityQueue<CarStatus> schedulingQue = null;
    
    
    // 当前系统时间
    private int curSAT = 0;
    
    // 记录每一时刻内，位置更新和路口调度的计数
    // 若无任何更新，则可能存在死锁，结束模拟
    private int modCot = 0;
    
    // 在道路上行驶的车辆数量，
    // 用于控制模拟器结束
    private int remCarCot = 0;
    
    //规划器
    Planner planner = null;
    
    public Simulator(Context ctx) {
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
    	runningQue = new PriorityQueue<>();
    	schedulingQue = new PriorityQueue<>();
    }
    
	/**
	 * >注册规划器
	 * @param p
	 */
    
    public void registerPlanner(Planner p) {
    	this.planner = p;
    }
    
    /**
     * >初始化当前规划器
     * >
     */
    public void init() {
    	// 初始化所有CarStatus
    	// 将所有准备上路的车加入的路口调度队列当中
    	statues = new HashMap<>();
    	cars.values().stream().forEach((car)->{
    		CarStatus cs = new CarStatus(car.getCarId(), car,
    					car.getStartTime());
    		// 启动行为
    		cs.action=CarActions.START;
    		cs.curSAT=car.getStartTime();
    		cs.nextRoadId  = planner.next(car.getCarId(), car.getOriCrossId());
    		// 假设准备上路为直走，并不影响路口调度
    		cs.turnDirected=DriveDirection.FOWARD;
    		statues.put(car.getCarId(),cs);
    		// 添加准备上路的车辆
    		schedulingQue.add(cs);
    	}); 
    	ctx.statues=statues;
    	// 获得车辆总数
    	remCarCot = cars.size();
    	// 设置系统时间为车辆最开始上路时间
    	if(remCarCot>0)
    		curSAT = schedulingQue.peek().curSAT;
    	
    	
    	
    }
    
    /**
     * >执行模拟过程，返回模拟总时间
     * 
     * @return
     */
    public int run() {
    	logger.info("Simulator start run,AST="+curSAT+", car.size="+remCarCot);
    	CarStatus cs = null;
    	while(true) {
    		// 当前模拟器中还有车辆在行驶
    		
    		// 重置操作计数
    		modCot=0;
    		
    		// 首先处理道路中处于RUNNING行为的车辆位置更新
    		while(!runningQue.isEmpty()&&
    			(cs=runningQue.peek()).curSAT==curSAT) {
    			// 从running队列中取出当前时刻要更新位置的车辆
    			runningQue.poll();
    			// 更新cs的状态
    			cs = updateRunningCarStatus(cs);
    			if(cs.action==CarActions.RUNNING)
    				// 如果还没有到达变道区，
    				runningQue.add(cs);
    			else if(cs.action==CarActions.SCHEDULING)
    				// 如果到达变道区，加入到running队列中
    				schedulingQue.add(cs);
    			
    		}
    		
    		
    		// 处理路口调度的车
    		while(!schedulingQue.isEmpty()&&
    			(cs=schedulingQue.peek()).curSAT==curSAT) {
    			// 从scheduling中取出要路口调度的车
    			schedulingQue.poll();
    			// 将车进行路口调度
    			cs = schedulingCarStatus(cs);
    			if(cs.action==CarActions.RUNNING)
    				// 路口调度成功，继续道路行驶
    				runningQue.add(cs);
    			else if(cs.action==CarActions.SCHEDULING)
    				// 上一时刻车辆通过路口失败，需要继续等待路口调度
    				schedulingQue.add(cs);
    			else if(cs.action==CarActions.STOP) {
    				// 该车到达终点
        			remCarCot--;
        			logger.info("Car:"+cs.carId+"->Cross:"+
        					cs.car.getDesCrossId()+"->time:"+curSAT);
    			}
    			
    			
    		}
    		
    		logger.info("Simulator modCot="+modCot);
    		if(modCot==0) {logger.info("Simulator may be dead locked!");}
    		if(remCarCot>0) curSAT++;//继续执行模拟
    		else break;// 正常结束
    		
    	}// end while
    	logger.info("Simulator finished,AST="+curSAT);
    	
    	
    	
    	return curSAT;
    }
    

    private CarStatus updateRunningCarStatus(CarStatus curCarStatus) {
    	return null;
    }

    private CarStatus schedulingCarStatus(CarStatus curCarStatus) {
    	return null;
    }
    
    
    
    /*
     * >创建T时间的残影车用于占位
     */
    private CarStatus createNullCar(int t) {
    	return new CarStatus(-1, null, t);
    }
    
    
	
}
