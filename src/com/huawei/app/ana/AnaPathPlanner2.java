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
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import com.huawei.app.Application;
import com.huawei.app.Simulator;
import com.huawei.app.Application.Context;
import com.huawei.app.Simulator.SimStatus;
import com.huawei.app.ana.AnaCarPath.CTpair;
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
public class AnaPathPlanner2 implements Planner{

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

    private CrossOrder[] allOrder = null;

    // 当前系统时间
    private int curSAT = -1;


	PriorityQueue<AnaCarPath> outque = new PriorityQueue<>();
	
   class CrossOrder{
	   int curSAT;
	   int cot;
	   CrossOrder next;
	   CrossOrder(int curSAT){
		   this.curSAT=curSAT;
		   cot=1;
	   }
	   
	   int inc() {
		   return ++cot;
	   }
   }
	
	
	
    public AnaPathPlanner2(Context ctx,Map<Integer,AnaCarPath> initPath) {
    	this.ctx= ctx; 
    	cars = ctx.cars;
    	roads=ctx.roads;
    	crosses=ctx.crosses;
    	createCrossIdx(crosses.keySet());

    	crossStart = new int[crosses.size()];
    	crossStop = new int[crosses.size()];
    	crossPassed = new int[crosses.size()];
    	initPath.values().forEach(ana->outque.add(ana));
    	allOrder = new CrossOrder[crosses.size()];
    }
    
	  
	/**
	 *  初始化将所有车初始方法缓存
	 */
	public void init() {
		// 初始化图

		roads.values().forEach(road->{
			
		});
		crosses.values().forEach(cross->{
			
		});
		
		cars.values().forEach(car->{

		});
		
		run();
			
	}
	
	
	private int[] testAddOrder(int curTime,AnaCarPath cp) {
		
		CTpair[] path = cp.getPath();
		int[] res = new int[path.length];
		int cid,sat,tmp;CrossOrder p=null;
		for(int i=0;i<path.length;i++) {
			cid = cIdx(path[i].crossId);
			sat = path[i].sat+curTime;
			p = allOrder[cid];
			tmp=1;
			while(p!=null) {
				if(p.curSAT==sat) {tmp+=p.cot;break;}
				p=p.next;
			}
			res[i] = tmp;
		}
		
		return res;
	}
	
	
	private int[] addCarOrder(int curTime,AnaCarPath cp) {
		
		CTpair[] path = cp.getPath();
		int[] res = new int[path.length];
		int cid,sat,tmp;CrossOrder p=null,q;
		for(int i=0;i<path.length;i++) {
			cid = cIdx(path[i].crossId);
			sat = path[i].sat+curTime;
			p = allOrder[cid];
			tmp=1;
			if(p==null)
				allOrder[cid]=new CrossOrder(sat);
			else if(sat<p.curSAT) {
				allOrder[cid] = new CrossOrder(sat);
				allOrder[cid].next = p;
			}else if(sat==p.curSAT) {
				tmp=p.cot++;
			}else {
				while(p.next!=null&&p.next.curSAT<sat)
					p = p.next;
				if(p.next==null||p.next.curSAT>sat){
					q=new CrossOrder(sat);
					q.next=p.next;
					p.next=q;
				}
				else {
					tmp=++p.next.cot;
				}
			}
			
			res[i] = tmp;
		}
		
		return res;
	}
	
	
	private int checkEnable(int[] ods) {
		int cot=0,edge=8;
		for(int k:ods) {
			if(k>=edge)cot++;
		}
		return cot>(ods.length/3)?-1:0;
	}
	
	Map<Integer,Integer> newStart = new HashMap<>();
	private void run() {
		
		while(!outque.isEmpty()) {
			AnaCarPath aa = outque.poll();
			if(curSAT<0||curSAT<aa.getPlanStartTime())
				curSAT = aa.getPlanStartTime();
			int[] ods = testAddOrder(curSAT, aa);
			if(checkEnable(ods)==0) {
				System.err.println(Arrays.toString(addCarOrder(1, aa)));
				System.err.println(aa.getCarId()+","+aa.getStartTime()+","+aa.getAllTime()+","+aa.getAllLen());
				newStart.put(aa.getCarId(), curSAT);
			}else {
				aa.setPlanStartTime(aa.getPlanStartTime()+1);
				outque.add(aa);
			}
		}
		
		System.out.println(curSAT);
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
	

	
	
	@Override
	public int onScheduling(int carId, int curCrossId,SimStatus ss) {
		

		return 0;
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
