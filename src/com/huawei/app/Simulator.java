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
import com.huawei.app.model.RoadChannel;

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
    	cars.values().forEach(car->{
    		CarStatus cs = new CarStatus(car.getCarId(), car,
    					car.getStartTime());
    		// 启动行为
    		cs.action=CarActions.START;
    		cs.curSAT=car.getStartTime();
    		cs.nextRoadId  = planner.next(car.getCarId(), car.getOriCrossId());
    		cs.frmCrossId = car.getOriCrossId();
    		// 获取道路出口的CrossId
    		cs.tagCrossId = roads.get(cs.nextRoadId).
    					getAnotherCrossId(car.getOriCrossId());
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
    				// 如果到达变道区，加入到scheduling队列中
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
    

    /**
     * 更新当前汽车位置状态
     * @param cs
     * @return
     */
    private CarStatus updateRunningCarStatus(CarStatus cs) {
    	Road curroad = roads.get(cs.curRoadId);
    	// 获得当前行驶的车道
    	RoadChannel curChannel = curroad.
    			getInCrossChannels(cs.tagCrossId)[cs.curChannelId];
    	CarStatus[] cc = curChannel.getChanel();
    	int cLength = curChannel.getChannelLength();
    	int loc= cs.curChannelLocal;
    	if(loc>=cLength-cs.curRoadSpeed) {
    		// 如果RUNNING行为的车已经在变道区时
    		// 直接更改为SCHEDULING,不修改时间
    		cs.action=CarActions.SCHEDULING;
    	}else {
    		// 可以进行位置更新
    		for(int i=1;i<=cs.curRoadSpeed;i++) {
    			if(!_check(cc[++loc]))
    				{loc--;break;}
    			else
    				// 清除路径上遇到的无效占位
    				cc[loc] = null;// help GC

    		}// end for
    		
    		// 如果车辆位置可以更新
    		if(loc>cs.curChannelLocal) {
    			modCot++;
    			// 创建占位
        		cc[cs.curChannelLocal]=createNullCar(cs.curSAT);
        		// 更新车道
        		cc[loc]=cs;
        		cs.curChannelLocal=loc;
    		} 

    		cs.curSAT++;// 更新时间
    		// 如果车辆进入了变道区，获得下一步行动路径
    		if(loc>=cLength-cs.curRoadSpeed) {
    			cs.action=CarActions.SCHEDULING;
    			// 获得下一步将要往那条路走
    			// 若即将结束行程，nextRoadId为-1，turnDirected为自行
    			cs.nextRoadId = planner.next(cs.carId, cs.tagCrossId);
    			if(cs.nextRoadId<0) 
    				cs.turnDirected = DriveDirection.FOWARD;
    			else
    				cs.turnDirected = crosses.get(cs.tagCrossId)
    					.getTurnDireByRoad(cs.curRoadId, cs.nextRoadId);
    		}
    			
    	}
    	return cs;
    }

    private class CheckedResult{
    	int channelId;// 车道号
    	int channelLocal;// 车道内位置
    	CheckedResult(int cId,int cLoc){
    		this.channelId=cId;
    		this.channelLocal=cLoc;
    	}
    }

    public boolean _check(CarStatus cs) {
    	return cs==null||(cs.carId<0&&cs.curSAT<curSAT);
    }
    private CheckedResult checkNextRoad(RoadChannel[] rcs,int maxRange) {
    	int cId=0,cLoc=0;
    	CarStatus[] cc = null;
    	for(cId=0;cId<rcs.length;cId++) {
    		cc=rcs[cId].getChanel();
    		if(!_check(cc[0])) continue;
			for(cLoc=1;cLoc<maxRange;cLoc++) 
				if(!_check(cc[cLoc]))break;	
			cLoc--;
			break;
    	}// end for;
    	// 车道无法进入
    	if(cId>=rcs.length) 
    		return null;
    	else
    		return new CheckedResult(cId,cLoc);
    }
    
    /**
     * 路口调度
     * @param curCarStatus
     * @return
     */
    private CarStatus schedulingCarStatus(CarStatus cs) {
    	CarStatus[] cc =null;
    	// 如果不是准备上路的车需要检查能否行驶到路口
    	if(cs.action==CarActions.SCHEDULING) {
    		
    		Road road = roads.get(cs.curRoadId);
        	RoadChannel curChannel = road.
        			getInCrossChannels(cs.tagCrossId)[cs.curChannelId];
    		cc = curChannel.getChanel();
    		// 记录第一次遇到占位的位置
    		int recNullCarloc =curChannel.getChannelLength();
    		int loc=cs.curChannelLocal;
    		while(++loc<curChannel.getChannelLength()) {
    			if(cc[loc]!=null) {
    				// 记录第一个遇到的位置
    				if(cc[loc].carId<0&&
    						cc[loc].curSAT==curSAT&&
    						recNullCarloc==curChannel.getChannelLength())
    					recNullCarloc=loc-1;
    				// 在路口遇到阻塞，无法穿过路口
    				if(cc[loc].carId>0) {loc--;break;}
    			}
    		}// end while

    		////////////////////////////////////////////////////////////////
    		// 无法行驶到路口////////////////////////////////////////////////
    		if(loc<curChannel.getChannelLength()) {
    			// 保持当前车行为SCEDULING
    			// 
    			loc=Math.min(recNullCarloc, loc);
    			// 操作计数加一
    			if(loc>cs.curChannelLocal) modCot++;
    			// 设置残影
    			cc[cs.curChannelLocal]=createNullCar(cs.curSAT);
    			cc[loc]=cs;// 设置原来的位置
    			cs.curChannelLocal = loc;
    			// 更改时间
    			cs.curSAT++;
    			// 获得下一步将要往那条路走
    			// 若即将结束行程，nextRoadId为-1，turnDirected为自行
    			cs.nextRoadId = planner.next(cs.carId, cs.tagCrossId);
    			if(cs.nextRoadId<0) 
    				cs.turnDirected = DriveDirection.FOWARD;
    			else
    				cs.turnDirected = crosses.get(cs.tagCrossId)
    					.getTurnDireByRoad(cs.curRoadId, cs.nextRoadId);
    			return cs;// 不继续通过路口
    		}
    		
    		//可以行驶到路口，检查能否到进入下一条路
    		road = roads.get(cs.nextRoadId);
    		int nextRoadMaxSpeed = Math.min(road.getMaxSpeed(), 
    				cs.car.getMaxSpeed());
    		// 计算进入下一条道路最大可行长度
    		int nrage = nextRoadMaxSpeed-
    				(curChannel.getChannelLength()-cs.curChannelLocal-1);
    		
    		RoadChannel[] rcs = road.getOutCrossChannels(cs.tagCrossId);
    		CheckedResult ckres = null;
    		
    		////////////////////////////////////////////////////////////////
    		// 当小于等于0或者下一条道无法进入更多的车时，无法跨越当前路口，
    		// 将车前移到最近一个T时刻生成的残影占位
    		// 如果无残影占位，则前移到车道最前端
    		if(nrage<=0||(ckres=checkNextRoad(rcs,nrage))==null) {
    			loc=recNullCarloc;
    			if(loc==curChannel.getChannelLength()) 
    				loc = curChannel.getChannelLength()-1;
    			// 操作计数加一
    			if(loc>cs.curChannelLocal) modCot++;
    			// 设置占位
    			cc[cs.curChannelLocal]=createNullCar(cs.curSAT);
    			cc[loc]=cs;// 设置原来的位置
    			cs.curChannelLocal = loc;
    			// 更改时间
    			cs.curSAT++;
    			// 获得下一步将要往那条路走
    			// 若即将结束行程，nextRoadId为-1，turnDirected为自行
    			cs.nextRoadId = planner.next(cs.carId, cs.tagCrossId);
    			if(cs.nextRoadId<0) 
    				cs.turnDirected = DriveDirection.FOWARD;
    			else
    				cs.turnDirected = crosses.get(cs.tagCrossId)
    					.getTurnDireByRoad(cs.curRoadId, cs.nextRoadId);
    			return cs;// 不继续通过路口    			
    		}
    		
    		////////////////////////////////////////////////////////////////
    		// 当车可以跨越当前路口，检查是否为终点
    		
    		modCot++;
    		// 生成一个T时刻的NullCar占位
    		// 表明T时刻该位置有车、其他T时刻的车无法行驶到这
			cc[cs.curChannelLocal]=createNullCar(cs.curSAT);
			
			if(cs.tagCrossId==cs.car.getDesCrossId()) {
			// 若车辆已经到达终点
				cs.action=CarActions.STOP;
				// cs.curSAT中保存这结束行程的时刻
				return cs;
			}
			
			cs.action=CarActions.RUNNING;
			// 更新道路中车最大速度
			cs.curRoadSpeed = nextRoadMaxSpeed;
			cs.curRoadId=cs.nextRoadId;
			cs.curChannelId=ckres.channelId;
			cs.curChannelLocal=ckres.channelLocal;
			cs.frmCrossId=cs.tagCrossId;
			cs.tagCrossId = road.getAnotherCrossId(cs.frmCrossId);
			cs.curSAT++;
			// 记录行驶路径
			cs.addPassedRoad(cs.curRoadId);
			//更新新车道中的位置
			cc = rcs[cs.curChannelId].getChanel();
			cc[cs.curChannelLocal]=cs;
			return cs;
   		
    	}// end action=SCEHDULING
    	
    	// 处理准备上路的车
    	else if(cs.action==CarActions.START) {
    		
    		//可以行驶到路口，检查能否到进入下一条路
    		Road nextRoad = roads.get(cs.nextRoadId);
    		int nextRoadMaxSpeed = Math.min(nextRoad.getMaxSpeed(), 
    				cs.car.getMaxSpeed());
    		// 下一条道路最大可行长度
    		int nrage = nextRoadMaxSpeed;
    		
    		RoadChannel[] rcs = nextRoad.getOutCrossChannels(cs.frmCrossId);
    		CheckedResult ckres = checkNextRoad(rcs, nrage);
    		// 准备上路的车无法进入下一条道路
    		if(ckres==null) {
    			cs.curSAT++;
    			// 获得下一步将要往那条路走
    			// 若即将结束行程，nextRoadId为-1，turnDirected为自行
        		cs.nextRoadId  = planner.next(cs.carId, cs.frmCrossId);
        		// 获取道路出口的CrossId
        		cs.tagCrossId = roads.get(cs.nextRoadId).
        					getAnotherCrossId(cs.frmCrossId);
        		// 假设准备上路为直走，并不影响路口调度
        		cs.turnDirected=DriveDirection.FOWARD;
        		return cs;
    		}
    		// 更新计数
    		modCot++;
			cs.action=CarActions.RUNNING;
			// 更新道路中车最大速度
			cs.curRoadSpeed = nextRoadMaxSpeed;
			cs.curRoadId=cs.nextRoadId;
			cs.curChannelId=ckres.channelId;
			cs.curChannelLocal=ckres.channelLocal;
			cs.frmCrossId=cs.tagCrossId;
			cs.tagCrossId = nextRoad.getAnotherCrossId(cs.frmCrossId);
			
			// 记录最开始的时刻，记录行驶路径
			cs.relStartTime=cs.curSAT;
			cs.addPassedRoad(cs.curRoadId);
			cs.curSAT++;
			//更新新车道中的位置
			cc = rcs[cs.curChannelId].getChanel();
			cc[cs.curChannelLocal]=cs;
			return cs;
    		
    	}
    	// 出现无效Action出现在当前处理中
    	else 
    		throw new IllegalArgumentException("illegel Action "+cs.action);
    }
    
    
    
    /*
     * >创建T时间的残影车用于占位
     */
    private CarStatus createNullCar(int t) {
    	return new CarStatus(-1, null, t);
    }
    
    
	
}
