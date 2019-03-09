package com.huawei.app;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.huawei.app.Application.Context;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;

/**
 * 
 * >模拟执行系统调配过程
 * 
 * @author zwp12
 *
 */
public class Simulator {

	private Context ctx = null;
    private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    private Map<Integer,CarStatus> statues=null;
    
    // 调度优先队列
    private PriorityQueue<CarStatus> actQue = null;
    
    // 当前时间
    private int curSYT = 0;
    
    
    //规划器
    Planner planner = null;
    
    public Simulator(Context ctx) {
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
    	statues = createStatus();
    	ctx.statues=statues;
    	actQue = new PriorityQueue<>();
    }
    
    
    /**
     * >创建状态集
     * @return
     */
    private Map<Integer,CarStatus> createStatus(){
    	Map<Integer,CarStatus> res = new HashMap<>();
    	Car car = null;
    	for(Integer cId:cars.keySet()) {
    		car = cars.get(cId);
    		res.put(cId,new CarStatus(cId, car, car.getStartTime()));
    	}
    	return res;
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
     */
    public void init() {
    	
    }
    
	
}
