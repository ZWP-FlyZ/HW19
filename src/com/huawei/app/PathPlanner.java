package com.huawei.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private Map<Integer,Integer> crossReIdx=null;
    private List<Integer>  crossIdx =null;
    private Road[][] graph = null;
    
	public PathPlanner(Context ctx){
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
    	createCrossIdx(crosses.keySet());
	}
	
	
	/**
	 *  初始化将所有车初始方法缓存
	 */
	public void init() {
		// 初始化图
		graph = new Road[crosses.size()][crosses.size()];
		roads.values().forEach(road->{
			int i=cIdx(road.getFromCrossId());
			int j=cIdx(road.getToCrossId());
			graph[i][j]=road;
			if(road.isDuplex()) 
				graph[j][i]=road;
		});
		System.err.println("");
	}
	
	
	private void createCrossIdx(Collection<Integer> crossIds){
		Map<Integer,Integer> res = new HashMap<>();
		List<Integer> ids = crossIds.stream()
			.sorted((a,b)->Integer.compare(a, b))
			.collect(Collectors.toList());
		for(int i=0;i<ids.size();i++) {
			res.put(ids.get(i), i);
		}
		crossIdx = ids;
		crossReIdx = res;
	}
	
	/**
	 * idx
	 * @param crossId
	 * @return
	 */
	private int cIdx(int crossId) {
		return crossReIdx.get(crossId);
	}
	
	/**
	 * 返回利用当前耗费
	 * @param road
	 * @param car
	 * @return
	 */
	private int cost(Road road,Car car) {
		int mxspd = Math.min(road.getMaxSpeed(), car.getMaxSpeed());
		return (int)Math.ceil(road.getRoadLength()*1.0/mxspd);
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
