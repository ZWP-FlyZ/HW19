package com.huawei.app.ana;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.huawei.app.Application;
import com.huawei.app.Simulator;
import com.huawei.app.Application.Context;
import com.huawei.app.Simulator.SimStatus;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Planner;
import com.huawei.app.model.Road;

/**
 * 
 * @author zwp12
 *
 * 动态静态可实现的规划器模板
 *
 *	方法1（静态）：假设当前系统内无任何车辆时，为每辆车进行规划一条路线，
 *		所有车辆之后按照自己路线行驶
 *	方法2（轻微动态，整体静态）：所有车辆在上路前，根据当前系统车况初始化一条路线，之后车辆按照该路线行驶
 *	方法3（局部静态，整体动态）：所有车辆会保留若干时间前计算的一条路线，到达时间有限期后失效重新计算路线
 *	方法4（实时）：所有车辆在路口调度之前必须重新计算当前最优路线
 *	
 *	以上方法可以配合 onTryStart() 来限制在系统中车辆的数量
 *	
 *
 */
public class AnaPathPlanner implements Planner{

	private Context ctx = null;
	private Map<Integer,Car> cars = null;
    private Map<Integer,Road> roads = null;
    private Map<Integer,Cross> crosses = null;
    private Map<Integer,Integer> crossReIdx=null;
    private List<Integer>  crossIdx =null;
    private int[]  crossStart = null;
    private int[] crossStop = null;
    private int[] crossPassed = null;
    private List<String> pathRec  = new ArrayList<>();
    // 道路图
    private Road[][] graph = null;
    // 权重图
    private int[][] G = null;
    

    // 当前系统时间
    private int curSAT = -1;

    // 车辆的路径
	private Map<Integer,CarPathNode> initCarPath=null;
	
    private class CarPathNode{
    	int curCrossId;
    	int nextRoadId;
    	int toEndRoadLength=0;
    	CarPathNode next;
    	CarPathNode(int crossId,int roadid,CarPathNode next){
    		this.curCrossId=crossId;
    		this.nextRoadId=roadid;
    		this.next =next;
    	}
    }
    

     
    
    public AnaPathPlanner(Context ctx) {
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
    	createCrossIdx(crosses.keySet());
    	initCarPath = new HashMap<>(cars.size());
    	crossStart = new int[crosses.size()];
    	crossStop = new int[crosses.size()];
    	crossPassed = new int[crosses.size()];

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
		crosses.values().forEach(cross->{
			
		});
		
		cars.values().forEach(car->{
			crossStart[cIdx(car.getOriCrossId())]++;
			crossStop[cIdx(car.getDesCrossId())]++;
		});
		
	}
	
	private void createCrossIdx(Collection<Integer> crossIds){
		Map<Integer,Integer> res = new HashMap<>(crosses.size());
		List<Integer> ids = crossIds.stream()
			.sorted((a,b)->Integer.compare(a, b))
			.collect(Collectors.toCollection(ArrayList::new));
		for(int i=0;i<ids.size();i++) {
			res.put(ids.get(i), i);
		}
		crossIdx = ids;
		crossReIdx = res;
	}
	
	private void updateG(CarStatus cs) {
		for(int i=0;i<G.length;i++)
			for(int j=0;j<G.length;j++) {
				if(graph[i][j]==null) G[i][j] = Integer.MAX_VALUE;
				else {
					Road road = graph[i][j];
					if(road.getRoadId()==cs.curRoadId)
						G[i][j]=Integer.MAX_VALUE;
					else{
						G[i][j]=cost(road,cs);	
					}
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
	private int cost(Road road,CarStatus cs) {
		
		int spd = Math.min(road.getMaxSpeed(),cs.car.getMaxSpeed());
		int baseTime = (int)Math.ceil(road.getRoadLength()*1.0/spd);
//		System.out.println(spd);
		return baseTime;
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
					tmp= dist[v];
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
		int lencot = 0;
		CarPathNode next = new CarPathNode(cReId(des),-1,null);
		next.toEndRoadLength=lencot;
		int par =-1,son=des;
		Road road;
		while((par=path[son])!=ori) {
			road = graph[par][son];
			next = new CarPathNode(cReId(par),
					road.getRoadId(),next);
			lencot+=road.getRoadLength();
			next.toEndRoadLength = lencot;
			son=par;
		}
		road = graph[par][son];
		next = new CarPathNode(cReId(par),
				road.getRoadId(),next);
		lencot+=road.getRoadLength();
		next.toEndRoadLength = lencot;
		return next;	
	}
	
	
	@Override
	public int onScheduling(int carId, int curCrossId,SimStatus ss) {
		
		if(curSAT<0||curSAT<ss.getCurSAT()) curSAT = ss.getCurSAT();
		crossPassed[cIdx(curCrossId)]++;
		
		CarStatus cs = ctx.statues.get(carId);
		Car car = cs.car;
		// 注意已经到达目的地，返回-1
		if(curCrossId==car.getDesCrossId())
			return -1;
		
		CarPathNode cur;
		if((cur=initCarPath.get(carId))==null) {
			updateG(cs);
			cur = dij(car,curCrossId,car.getDesCrossId());
			initCarPath.put(carId, cur);
		}
		while(cur!=null&&cur.curCrossId!=curCrossId)
			cur=cur.next;
		if(cur==null) 
			throw new IllegalArgumentException("CrossId:"+curCrossId+" is not in carpath");
			
		return cur.nextRoadId;
	}
	
	int incot=0;
	boolean swit = true; 
	StringBuilder sb = new StringBuilder();
	@Override
	public boolean onTryStart(int carId, int crossId, SimStatus ss) {
		// TODO Auto-generated method stub
		
		return ss.getRemCarCot()<1;
	}
	

	int ttt=0;
	@Override
	public boolean onStop(int carId, int crossId, SimStatus ss) {
		// TODO Auto-generated method stub
		int runt = ss.getCurSAT()-ttt;
		Car car = cars.get(carId);
		System.err.println("Car:"+carId+"->Cross:"+crossId+"->time:"+ss.getCurSAT()+"->runt:"+runt);
		sb.append(crossId+","+(ss.getCurSAT()-ttt));
		CarPathNode node = initCarPath.get(carId);
		sb.insert(0, car.getCarId()+","+car.getStartTime()+","+node.toEndRoadLength+",");
		System.err.println(sb.toString());
		pathRec.add(sb.toString());
		sb = new StringBuilder(); 
//		System.out.println(incot);
//		crossDesCarRemCot[cIdx(cars.get(carId).getDesCrossId())]--;
		return false;
	}
	
	@Override
	public void onStart(int carId, int crossId, SimStatus ss) {
		// TODO Auto-generated method stub
		ttt =ss.getCurSAT();
		sb.append(crossId+","+(ss.getCurSAT()-ttt)+",");
//		crossDesCarRemCot[cIdx(cars.get(carId).getDesCrossId())]++;
	}


	@Override
	public void onPassedCross(int carId, int curCrossId, SimStatus ss) {
		// TODO Auto-generated method stub
		sb.append(curCrossId+","+(ss.getCurSAT()-ttt)+",");
	}

	@Override
	public void onPassedRoad(int carId, int roadId, SimStatus ss) {
		// TODO Auto-generated method stub
				
//		System.out.println(ss.getCurSAT()+","+basetime+","+realtime);

		
	}
	
	
	public void dumpPathRec(String filepath) {
		try {
			Files.write(Paths.get(filepath), pathRec, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public void showCal() {
		System.err.println(Arrays.toString(crossStart));
		System.err.println(Arrays.toString(crossStop));
		System.err.println(Arrays.toString(crossPassed));
		double sum = 0.0;
		for(int a:crossPassed) {
			sum+=a*1.0/crossPassed.length;
		}
		System.err.println(sum);
	}


	@Override
	public int onInitCarStartTime(int carId) {
		// TODO Auto-generated method stub
		return 0;
	}

}
