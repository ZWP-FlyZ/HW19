package com.huawei.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.huawei.app.Application.Context;
import com.huawei.app.Simulator.SimStatus;
import com.huawei.app.model.Answer;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;

/**
 * 
 * @author zwp12
 *
 * 
 *
 *	测试Answer的规划器
 *	
 *
 */
public class ReRunPathPlanner implements Planner{

	private Context ctx = null;
    private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    @SuppressWarnings("unused")
	private Map<Integer,Integer> crossReIdx=null;
    @SuppressWarnings("unused")
	private List<Integer>  crossIdx =null;

	    
	private Map<Integer,CarPathNode> initCarPath=null;
    
    private class CarPathNode{
    	int curCrossId;
    	int nextRoadId;
    	CarPathNode next;
    	CarPathNode(int crossId,int roadid,CarPathNode next){
    		this.curCrossId=crossId;
    		this.nextRoadId=roadid;
    		this.next =next;
    	}
    }
    
    
    public ReRunPathPlanner(Context ctx) {
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
    	createCrossIdx(crosses.keySet());
    	initCarPath = new HashMap<>();
    }
    
	
    
	/**
	 *  初始化将所有车初始方法缓存
	 */
	public void init(Map<Integer,Answer> answers) {
		// 初始化
		
		answers.values().forEach(ans->{
			initCarPath.put(ans.getCarId(), 
					getCarPathNode(ans.getCarId(),ans.getRoadIds()));
		});
	}
	
	private void createCrossIdx(Collection<Integer> crossIds){
		Map<Integer,Integer> res = new HashMap<>();
		List<Integer> ids = crossIds.stream()
			.sorted((a,b)->Integer.compare(a, b))
			.collect(Collectors.toCollection(ArrayList::new));
		for(int i=0;i<ids.size();i++) {
			res.put(ids.get(i), i);
		}
		crossIdx = ids;
		crossReIdx = res;
	}
		
	

	// 创建路径
	private CarPathNode getCarPathNode(int carId,int[] roadIds) {
		if(roadIds==null||roadIds.length==0)return null;
		Car car = cars.get(carId);
		CarPathNode head = new CarPathNode(car.getOriCrossId(),roadIds[0], null);
		Road road = roads.get(roadIds[0]);
		int anCrossId = road.getAnotherCrossId(car.getOriCrossId());
		CarPathNode p = head,tmp=null;
		for(int i=1;i<roadIds.length;i++) {
			road = roads.get(roadIds[i]);
			tmp = new CarPathNode(anCrossId,roadIds[i], null);
			anCrossId = road.getAnotherCrossId(anCrossId);
			p.next=tmp;
			p = tmp;
		}

//		System.err.println(anCrossId != car.getDesCrossId());
			
		return head;
	}
	
	@Override
	public int onScheduling(int carId, int curCrossId,SimStatus ss) {
		
		CarStatus cs = ctx.statues.get(carId);
		Car car = cs.car;
		// 注意已经到达目的地，返回-1
		if(curCrossId==car.getDesCrossId())
			return -1;
		
		CarPathNode cur=null;
		cur=initCarPath.get(carId);
		while(cur!=null&&cur.curCrossId!=curCrossId)
			cur=cur.next;
		if(cur==null) 
			throw new IllegalArgumentException("CrossId:"+curCrossId+" is not in carpath");
			
		return cur.nextRoadId;
	}
	
	@Override
	public boolean onTryStart(int carId, int crossId, SimStatus ss) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean onStop(int carId, int crossId, SimStatus ss) {
		// TODO Auto-generated method stub
		System.err.println("Car:"+carId+"->Cross:"+crossId+"->time:"+ss.getCurSAT());
		return false;
	}
	
	public String showPath(int carId) {
		StringBuffer sb = new StringBuffer();
		CarPathNode node = initCarPath.get(carId);
		while(node!=null) {
			sb.append("("+node.curCrossId+","+node.nextRoadId+")->");
			node=node.next;
		}
		return sb.toString();
	}



	@Override
	public void onStart(int carId, int crossId, SimStatus ss) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPassedCross(int carId, int curCrossId, SimStatus ss) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPassedRoad(int carId, int roaId, SimStatus ss) {
		// TODO Auto-generated method stub
		
	}





	

}
