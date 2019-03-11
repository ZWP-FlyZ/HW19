package com.huawei.app;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.huawei.app.Application.Context;
import com.huawei.app.model.Car;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;

public class PathPlanner implements Planner {

	private static final Logger logger = Logger.getLogger(Simulator.class);
	private Context ctx = null;
    private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    
    
	public PathPlanner(Context ctx){
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
	}
	
	
	/**
	 *  初始化将所有车初始方法缓存
	 */
	public void init() {
		
	}
	
	
	@Override
	public int next(int carId, int curCrossId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Integer> nextAll(int carId, int curCrossId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	


}
