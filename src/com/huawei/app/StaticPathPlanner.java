package com.huawei.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.huawei.app.Application.Context;
import com.huawei.app.Simulator.SimStatus;
import com.huawei.app.model.Car;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;

public class StaticPathPlanner implements Planner {

    private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    private Map<Integer,Integer> crossReIdx=null;
    private List<Integer>  crossIdx =null;
    private Road[][] graph = null;
    
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
    
    
    public StaticPathPlanner(Context ctx){

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
		
		
		
		initCarPath = new HashMap<>();
		cars.values().forEach(car->{
			CarPathNode initpath=dij(car,car.getOriCrossId(),car.getDesCrossId());
			initCarPath.put(car.getCarId(), initpath);
		});
		
		
		System.err.println("");
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
	
	/**
	 * idx
	 * @param crossId
	 * @return
	 */
	private int cIdx(int crossId) {
		return crossReIdx.get(crossId);
	}
	
	private int cReId(int cidx) {
		return crossIdx.get(cidx);
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
	
	
	private CarPathNode dij(Car car,int oriCrossId,int desCrossId) {
		
		if(car==null||oriCrossId==desCrossId) 
			throw new IllegalArgumentException("car==null or oriCrossId==desCrossId");
		
		int[][] G =  new int[crosses.size()][crosses.size()];
		int[] path = new int[crosses.size()];

		
		int ori = cIdx(oriCrossId);
		int des = cIdx(desCrossId);
		
		Set<Integer> set = new HashSet<>(crosses.size());		
		for(int i=0;i<crosses.size();i++)
			if(i!=ori) set.add(i);
		Arrays.fill(path, -1);
		
		
		// 获得当前路况下的权图
		for(int i=0;i<G.length;i++)
			for(int j=0;j<G.length;j++) {
				if(graph[i][j]==null) G[i][j] = Integer.MAX_VALUE;
				else {
					Road road = graph[i][j];
					G[i][j]=cost(road,car);
					// 设置父节点
					if(i==ori)path[j]=ori;
				}
			}
		
		out:
		for(int i=1;i<crosses.size();i++) {
			int tmp=Integer.MAX_VALUE;
			int k = -1;
			for(int v:set) {
				if(G[ori][v]<tmp) {
					tmp= G[ori][v];
					k=v;
				}
			}
			///// 如果这里k<-1 表示遇到死路口 //////// 
			if(k<0) throw new IllegalArgumentException("k<0!");
			if(k==des) break out;// 已经寻找到结尾
			set.remove(k);
			for(int v:set) {
				if(G[k][v]<Integer.MAX_VALUE&&
						G[ori][k]+G[k][v]<G[ori][v]) {
					G[ori][v]=G[ori][k]+G[k][v];
					path[v]=k;//更新父节点
				}
			}
		}// end 
		
		// 路径恢复
		// 创建一个尾节点
		CarPathNode next = new CarPathNode(cReId(des),-1,null);
		int par =-1,son=des;
		while((par=path[son])!=ori) {
			next = new CarPathNode(cReId(par),
					graph[par][son].getRoadId(),next);
			son=par;
		}
		next = new CarPathNode(cReId(par),
				graph[par][son].getRoadId(),next);
		return next;	
	}
	
	
	
	@Override
	public int onScheduling(int carId, int curCrossId,SimStatus ss) {
		// TODO Auto-generated method stub
		CarPathNode cur = initCarPath.get(carId);
		CarPathNode p = cur;
		while(p!=null&&p.curCrossId!=curCrossId)
			p=p.next;
		if(p==null) 
			throw new IllegalArgumentException("CrossId:"+curCrossId+" is not in carpath");
		return p.nextRoadId;
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
	public boolean onTryStart(int carId, int crossId,SimStatus ss) {
		// TODO Auto-generated method stub
		return ss.getRemCarCot()<300;//当前系统限制限制300辆车
	}

	@Override
	public boolean onStop(int carId, int crossId, SimStatus ss) {
		System.err.println("Car:"+carId+"->Cross:"+crossId+"->time:"+ss.getCurSAT());
		return false;
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
