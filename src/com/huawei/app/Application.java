package com.huawei.app;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Road;

public class Application {

	/**
	 * 
	 * @author zwp12
	 * >保存全局信息描述
	 */
	public static class Context{
		
	    public Map<Integer,Car> cars = null;
	    public Map<Integer,Road> roads = null;
	    public Map<Integer,Cross> crosses = null;
		public Map<Integer,CarStatus>  statues=null;
	}
	
	public static void run(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        
        System.out.println("carPath = " + carPath + "\nroadPath = " + roadPath +
        		"\ncrossPath = " + crossPath + "\nanswerPath = " + answerPath);
        
        Context ctx = new Context();
        
        ctx.cars = 
        		FormatUtils.converCars(FormatUtils.loadAndFormat(carPath)); 
        ctx.roads = 
        		FormatUtils.converRoad(FormatUtils.loadAndFormat(roadPath)); 
        ctx.crosses = 
        		FormatUtils.converCross(FormatUtils.loadAndFormat(crossPath));
        
        System.out.println("load finished!\ncars.size="+ctx.cars.size()+
        		"\nroads.size="+ctx.roads.size()+"\ncrosses.size="+ctx.crosses.size());        
        
        Instant now = Instant.now();
        
        // 完成cars、roads、crosses的一些基础工作
        preprocess(ctx);
        // 创建规划器
//        StaticPathPlanner planner = new StaticPathPlanner(ctx);
        DynamicPathPlanner planner = new DynamicPathPlanner(ctx);
        // 创建模拟器
        Simulator sim = new Simulator(ctx);
        // 注册规划器
        sim.registerPlanner(planner);
        // 初始化
        planner.init();
        sim.init();
//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+planner.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();

        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        
        // 记录所有车辆的行程
        FormatUtils.saveAnswer(answerPath,  ctx.statues.values());
	}
	
	
	private static void preprocess(Context ctx) {
		
		// 设置所有Cross中可以驶出的roadId
		ctx.crosses.values().stream()
		.forEach(v->v.setConnOutRoadIds(ctx.roads));
	}
	
	
}
