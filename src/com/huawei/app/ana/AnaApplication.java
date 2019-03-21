package com.huawei.app.ana;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.huawei.app.Application.Context;
import com.huawei.app.DynamicPathPlanner;
import com.huawei.app.FormatUtils;
import com.huawei.app.ReRunPathPlanner;
import com.huawei.app.Simulator;
import com.huawei.app.ana.AnaCarPath;
import com.huawei.app.ana.AnaPathPlanner;
import com.huawei.app.ana.AnaPathPlanner3;
import com.huawei.app.ana.AnaRecDynamicPathPlanner;
import com.huawei.app.model.Answer;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Road;

public class AnaApplication {

	/**
	 * 
	 * @author zwp12
	 * >保存全局信息描述
	 */

	
	public static void runtest(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        String anaPath = "E:/huawei2019/train/anarec.txt";
        System.out.println("carPath = " + carPath + "\nroadPath = " + roadPath +
        		"\ncrossPath = " + crossPath + "\nanswerPath = " + answerPath);
        
        Context ctx = new Context();
        
        ctx.cars = 
        		FormatUtils.converCars(FormatUtils.loadAndFormat(carPath)); 
        ctx.roads = 
        		FormatUtils.converRoad(FormatUtils.loadAndFormat(roadPath)); 
        ctx.crosses = 
        		FormatUtils.converCross(FormatUtils.loadAndFormat(crossPath));
        Map<Integer,AnaCarPath> initPaths = 
				FormatUtils.converAnaCarPath(FormatUtils.loadAnarec(anaPath));
        
        System.out.println("load finished!\ncars.size="+ctx.cars.size()+
        		"\nroads.size="+ctx.roads.size()+"\ncrosses.size="+ctx.crosses.size());        
        
        Instant now = Instant.now();
        
        // 完成cars、roads、crosses的一些基础工作
        preprocess(ctx);
        // 创建规划器
//        StaticPathPlanner planner = new StaticPathPlanner(ctx);
        AnaRecDynamicPathPlanner planner = new AnaRecDynamicPathPlanner(ctx,initPaths);
        // 创建模拟器
        Simulator sim = new Simulator(ctx);
//        BlockSimulator sim = new BlockSimulator(ctx);
        // 注册规划器
        sim.registerPlanner(planner);
        // 初始化
        planner.init();

//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+planner.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();

        planner.showCal();
        
        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        
        // 记录所有车辆的行程
        FormatUtils.saveAnswer(answerPath,  ctx.statues.values());
	}
	
	
	public static void rerun(String[] args) {
		
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
        
        Map<Integer,Answer> answers = 
        		FormatUtils.converAnswer(FormatUtils.loadAndFormat(answerPath));
        
        System.out.println("load finished!\ncars.size="+ctx.cars.size()+
        		"\nroads.size="+ctx.roads.size()+"\ncrosses.size="+ctx.crosses.size());        
        
        // 重新设置上路时间
        ctx.cars.values().forEach(car->{
        	car.setStartTime(answers.get(car.getCarId()).getStartTime());
        });
        
        
        Instant now = Instant.now();
        
        // 完成cars、roads、crosses的一些基础工作
        preprocess(ctx);
        // 创建规划器
//        StaticPathPlanner planner = new StaticPathPlanner(ctx);
        ReRunPathPlanner planner = new ReRunPathPlanner(ctx);
        // 创建模拟器
        Simulator sim = new Simulator(ctx);
//        BlockSimulator sim = new BlockSimulator(ctx);
        // 注册规划器
        sim.registerPlanner(planner);
        // 初始化
        planner.init(answers);

//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+planner.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();

        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        

	}
	
	
	public static void anarun(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        String anaPath = "E:/huawei2019/train/anarec.txt";
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
        AnaPathPlanner planner = new AnaPathPlanner(ctx);
        // 创建模拟器
        Simulator sim = new Simulator(ctx);
//        BlockSimulator sim = new BlockSimulator(ctx);
        // 注册规划器
        sim.registerPlanner(planner);
        // 初始化
        planner.init();

//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+planner.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();

        planner.dumpPathRec(anaPath);
        
        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        
        // 记录所有车辆的行程
        FormatUtils.saveAnswer(answerPath,  ctx.statues.values());
	}
	
	public static void anarun2(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        String anaPath = "E:/huawei2019/train/anarec.txt";
        System.out.println("carPath = " + carPath + "\nroadPath = " + roadPath +
        		"\ncrossPath = " + crossPath + "\nanswerPath = " + answerPath);
        
        Context ctx = new Context();
        
        ctx.cars = 
        		FormatUtils.converCars(FormatUtils.loadAndFormat(carPath)); 
        ctx.roads = 
        		FormatUtils.converRoad(FormatUtils.loadAndFormat(roadPath)); 
        ctx.crosses = 
        		FormatUtils.converCross(FormatUtils.loadAndFormat(crossPath));
        
		Map<Integer,AnaCarPath> initPaths = 
				FormatUtils.converAnaCarPath(FormatUtils.loadAnarec(anaPath));
        
        System.out.println("load finished!\ncars.size="+ctx.cars.size()+
        		"\nroads.size="+ctx.roads.size()+"\ncrosses.size="+ctx.crosses.size());        
        
        Instant now = Instant.now();
        
        // 完成cars、roads、crosses的一些基础工作
        preprocess(ctx);
        // 创建规划器
//        StaticPathPlanner planner = new StaticPathPlanner(ctx);
        AnaPathPlanner3 planner = new AnaPathPlanner3(ctx,initPaths);
        // 创建模拟器
        Simulator sim = new Simulator(ctx);
//        BlockSimulator sim = new BlockSimulator(ctx);
        // 注册规划器
        sim.registerPlanner(planner);
        // 初始化
        planner.init();

//        ctx.cars.keySet().forEach(v->{
//        	System.out.println(v+":"+planner.showPath(v));
//        });
        // 运行模拟器产生运行结果
        sim.run();
        
        
        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        
        // 记录所有车辆的行程
//        FormatUtils.saveAnswer(answerPath,  ctx.statues.values());
	}
	
	private static void preprocess(Context ctx) {
		
		// 设置所有Cross中可以驶出的roadId
		ctx.crosses.values().stream()
		.forEach(v->v.setConnOutRoadIds(ctx.roads));
	}
	
	
	
	
	
}
