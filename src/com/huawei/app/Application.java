package com.huawei.app;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.huawei.app.model.Car;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Road;

public class Application {

	public static void run(String[] args) {
		
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
        
        System.out.println("carPath = " + carPath + "\nroadPath = " + roadPath +
        		"\ncrossPath = " + crossPath + "\nanswerPath = " + answerPath);
        
        Map<Integer,Car> cars = 
        		FormatUtils.converCars(FormatUtils.loadAndFormat(carPath)); 
        Map<Integer,Road> roads = 
        		FormatUtils.converRoad(FormatUtils.loadAndFormat(roadPath)); 
        Map<Integer,Cross> crosses = 
        		FormatUtils.converCross(FormatUtils.loadAndFormat(crossPath));
        
        System.out.println("load finished!\ncars.size="+cars.size()+
        		"\nroads.size="+roads.size()+"\ncrosses.size="+crosses.size());        
        
        Instant now = Instant.now();
        // 创建规划器
        // 创建模拟器
        // 注册规划器
        // 运行模拟器产生运行结果
        
        long runingtime = Duration.between(now, Instant.now()).toMillis();
        
        System.out.println("running time:"+runingtime);
        
        // 记录所有车辆的行程
        FormatUtils.saveAnswer(answerPath, cars.values());
	}
	
}
