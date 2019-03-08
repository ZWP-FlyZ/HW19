package com.huawei.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

	
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
					.filter(v->!v.contains("#"))
					.map(v->v.replaceAll("\\(|\\)",""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 *  >将结果写回anwser.txt 中
	 */
	private final static String head = "#(carId,StartTime,RoadId...)";
	public static void saveAnswer(String path,List<String> cars) {
		try {
			File f  = Paths.get(path).toFile();
			if(!f.exists()) f.createNewFile();
			try(BufferedWriter bw  
					= new BufferedWriter(new FileWriter(f))){
				bw.write(head+"\n");
				for(String v:cars)bw.write(v+"\n");
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
		saveAnswer("E:/test2-an.txt",v) ;
	}

}
