package com.huawei.app.model;

public class WinMean {
	private int sum;
	private long idx;
	private int[] his = null;
	private int winSize=1;
	public WinMean(int size){
		winSize = size>0?size:1;
		sum = 0; idx=0;
		his = new int[size];
	}
	public int add(int v) {
		sum+=v;
		int i =(int) (idx%winSize);
		his[i] += v;
		return his[i];
	}
	public double getMean() {
		if(idx<winSize) 
			return (sum*1.0/(idx+1));
		return sum*1.0/winSize;
	}
	
	public void next() {
		idx++;
		if(idx>=winSize) {
			sum-=his[(int) (idx%winSize)];
			his[(int) (idx%winSize)]=0;
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WinMean wm = new WinMean(3);
		System.out.println(wm.getMean());
		wm.add(1);
		System.out.println(wm.getMean());
		wm.next();
		System.out.println(wm.getMean());
		wm.add(2);
		System.out.println(wm.getMean());
		wm.next();
		System.out.println(wm.getMean());
		wm.add(3);
		System.out.println(wm.getMean());
		wm.next();
		System.out.println(wm.getMean());
		wm.add(4);
		System.out.println(wm.getMean());
		wm.next();
	}

}
