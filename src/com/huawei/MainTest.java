package com.huawei;

import com.huawei.app.Application;

public class MainTest {

//	static String basePath = "/home/zwp/work/huawei2019/train";
	static String basePath = "E:/huawei2019/train";
    static String carPath = basePath+"/car.txt";
    static String roadPath = basePath+"/road.txt";
    static String crossPath = basePath+"/cross.txt";
    static String answerPath = basePath+"/answer.txt";
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 正常调度运行
		Application.run(new String[]{carPath,roadPath,crossPath,answerPath});
		//由answer恢复 获得运行结果
//		Application.rerun(new String[]{carPath,roadPath,crossPath,answerPath});
		
	}

}
