package com.huawei.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.huawei.app.ana.AnaCarPath;
import com.huawei.app.model.Answer;
import com.huawei.app.model.Car;
import com.huawei.app.model.CarStatus;
import com.huawei.app.model.Cross;
import com.huawei.app.model.Road;

public class FormatUtils {

	
	/**
	 *  >加载文件 并去除文件中的#()字符 
	 * @param path
	 * @return
	 */
	public static List<String> loadAndFormat(String path){
		List<String> res = null;
		try {
			res =Files.readAllLines(Paths.get(path),
					StandardCharsets.UTF_8);
			res = res.stream()
					.filter(v->!v.contains("#")&&v.length()>2)
					.map(v->v.replaceAll("\\(|\\)",""))
					.map(v->v.replaceAll(" ",""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	public static List<String> loadAnarec(String path){
		List<String> res = null;
		try {
			res =Files.readAllLines(Paths.get(path),
					StandardCharsets.UTF_8);
			res = res.stream()
					.filter(v->!v.contains("#")&&v.length()>2)
					.map(v->v.replaceAll(" ",""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	public static Map<Integer,Car> converCars(List<String> lines){
		if(lines==null) return null;
		Map<Integer,Car> res = new HashMap<>();
		lines.stream()
		.map(v->v.split(","))
		.forEach((v)->{
			int[] p = strs2ints(v);
			res.put(p[0], new Car(p));
		});
		return res;
	}
	
	
	public static  Map<Integer,Road> converRoad(List<String> lines){
		if(lines==null) return null;
		Map<Integer,Road> res = new HashMap<>();
		lines.stream()
		.map(v->v.split(","))
		.forEach((v)->{
			int[] p = strs2ints(v);
			res.put(p[0], new Road(p));
		});
		return res;
	}
	
	
	public static Map<Integer,Cross> converCross(List<String> lines){
		if(lines==null) return null;
		Map<Integer,Cross> res = new HashMap<>();
		lines.stream()
		.filter(v->v.length()>1)
		.map(v->v.split(","))
		.forEach((v)->{
			int[] p = strs2ints(v);
			res.put(p[0], new Cross(p));
		});
		return res;
	}
	
	public static Map<Integer,Answer> converAnswer(List<String> lines){
		if(lines==null) return null;
		Map<Integer,Answer> res = new HashMap<>();
		lines.stream()
		.filter(v->v.length()>1)
		.map(v->v.split(","))
		.forEach((v)->{
			int[] p = strs2ints(v);
			res.put(p[0], new Answer(p));
		});
		return res;
	}
	
	
	public static Map<Integer,AnaCarPath> converAnaCarPath(List<String> lines){
		if(lines==null) return null;
		Map<Integer,AnaCarPath> res = new HashMap<>();
		lines.stream()
		.filter(v->v.length()>1)
		.map(v->v.split(","))
		.forEach((v)->{
			int[] p = strs2ints(v);
			res.put(p[0], new AnaCarPath(p));
		});
		return res;
	}
	
	
	
	public static int[] strs2ints(String[] ss) {
		int[] res = new int[ss.length];
		for(int i=0;i<ss.length;i++) 
			res[i]=Integer.parseInt(ss[i]);
		return res;
	}
	
	
	
	/**
	 *  >将结果写回anwser.txt 中
	 */
	private final static String head = "#(carId,StartTime,RoadId...)";
	public static void saveAnswer(String path,Collection<CarStatus> cars) {
		try {
			File f  = Paths.get(path).toFile();
			if(!f.exists()) f.createNewFile();
			try(BufferedWriter bw  
					= new BufferedWriter(new FileWriter(f))){
				bw.write(head+"\n");
				
				for(CarStatus c:cars)bw.write(c.getReport()+"\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> v = loadAndFormat("E:/test2.txt");
		v.forEach(System.out::println);
//		saveAnswer("E:/test2-an.txt",v) ;
	}

}
