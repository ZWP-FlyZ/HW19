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
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;
import com.huawei.app.model.RoadChannel;


public class DynamicPathPlanner implements Planner{

	private Context ctx = null;
    private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    private Map<Integer,Integer> crossReIdx=null;
    private List<Integer>  crossIdx =null;

	
    // 道路图
    private Road[][] graph = null;
    // 权重图
    private int[][] G = null;
    
    // 更新
    private int UPDATE_DELAY=1;
    
    // 当前系统时间
    private int curSAT = -1;
    
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
    
    
    public DynamicPathPlanner(Context ctx) {
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
	public void init() {
		// 初始化图
		graph = new Road[crosses.size()][crosses.size()];
		G = new int[crosses.size()][crosses.size()];
		roads.values().forEach(road->{
			int i=cIdx(road.getFromCrossId());
			int j=cIdx(road.getToCrossId());
			graph[i][j]=road;
			if(road.isDuplex()) 
				graph[j][i]=road;
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
	
	private void updateG() {
		for(int i=0;i<G.length;i++)
			for(int j=0;j<G.length;j++) {
				if(graph[i][j]==null) G[i][j] = Integer.MAX_VALUE;
				else {
					Road road = graph[i][j];
					G[i][j]=cost(road);
				}
			}
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
	private int cost(Road road) {
		
		int baseTime = (int)Math.ceil(road.getRoadLength()*1.0/road.getMaxSpeed());
		int cout = 0;
		CarStatus[] cc = null;
		RoadChannel[] rcs = road.getOutCrossChannels(road.getFromCrossId());
		for(RoadChannel rc :rcs) {
			cc = rc.getChanel();
			for(CarStatus cs:cc)
				if(cs!=null&&cs.carId>=0)cout++;
		}
		if(road.isDuplex()) {
			rcs = road.getInCrossChannels(road.getToCrossId());
			for(RoadChannel rc :rcs) {
				cc = rc.getChanel();
				for(CarStatus cs:cc)
					if(cs!=null&&cs.carId>=0)cout++;
			}
			cout = cout/2;
		}

		return baseTime+cout;
	}
	
	
	private CarPathNode dij(Car car,int oriCrossId,int desCrossId) {
		
		if(oriCrossId==desCrossId)
			System.err.println(car.getDesCrossId()+" "+car.getOriCrossId());
		if(car==null||oriCrossId==desCrossId) 
			throw new IllegalArgumentException("car==null or oriCrossId==desCrossId");
		int[] dist = new int[crosses.size()];
		int[] path = new int[crosses.size()];

		int ori = cIdx(oriCrossId);
		int des = cIdx(desCrossId);
		
		// 初始化计算
		Set<Integer> set = new HashSet<>(crosses.size());		
		for(int i=0;i<crosses.size();i++)
			if(i!=ori) set.add(i);
		Arrays.fill(path, -1);
		Arrays.fill(dist, Integer.MAX_VALUE);
		for(int i=0;i<G.length;i++) {
			dist[i]=G[ori][i];
			if(graph[ori][i]!=null)path[i]=ori;
		}
			
		out:
		for(int i=1;i<crosses.size();i++) {
			int tmp=Integer.MAX_VALUE;
			int k = -1;
			for(int v:set) {
				if(dist[v]<tmp) {
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
						(tmp=dist[k]+G[k][v])<dist[v]) {
					dist[v]=tmp;
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
	public int next(int carId, int curCrossId) {
		
		
		CarStatus cs = ctx.statues.get(carId);
		Car car = cs.car;
		// 注意已经到达目的地，返回-1
		if(curCrossId==car.getDesCrossId())
			return -1;
		
		int _curSAT = cs.curSAT;
		if(curSAT<0||_curSAT>curSAT+UPDATE_DELAY) {
			curSAT=_curSAT;
			updateG();
			initCarPath.put(carId, null);
		}
		CarPathNode cur;
		if((cur=initCarPath.get(carId))==null) {
			cur = dij(car,curCrossId,car.getDesCrossId());
			initCarPath.put(carId, cur);
		}
		while(cur!=null&&cur.curCrossId!=curCrossId)
			cur=cur.next;
		if(cur==null) 
			throw new IllegalArgumentException("CrossId:"+curCrossId+" is not in carpath");
			
		return cur.nextRoadId;
	}
	
	@Override
	public boolean feed(int carId, int crossId, int remCars) {
		// TODO Auto-generated method stub
		return true;
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

}
