package com.huawei.app.ana;

public class AnaCarPath implements Comparable<AnaCarPath>{

	private int carId;
	private int startTime;
	private int allLen;
	private int allTime;
	private int oriCrossId;
	private int desCrossId;
	private int pathCot;
	private CTpair[] path;
	private int planStartTime;
	
	public AnaCarPath(int[] args) {
		carId = args[0];
		startTime=args[1];
		planStartTime = startTime;
		allLen =args[2];
		oriCrossId = args[3];
		desCrossId=args[args.length-2];
		allTime = args[args.length-1]+1;
		path = new CTpair[(args.length-3)/2];
		for(int i=3,j=0;i<args.length;i+=2) {
			path[j++] = new CTpair(args[i],args[i+1]);
		}
	}
	
	public static class CTpair{
		public int crossId;
		public int sat;
		public CTpair(int crossId,int sat) {
			this.crossId=crossId;
			this.sat = sat;
		}
	}

	public int getCarId() {
		return carId;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getAllLen() {
		return allLen;
	}

	public int getAllTime() {
		return allTime;
	}

	public CTpair[] getPath() {
		return path;
	}

	public int getOriCrossId() {
		return oriCrossId;
	}

	public int getDesCrossId() {
		return desCrossId;
	}

	public int getPathCot() {
		return pathCot;
	}

	@Override
	public int compareTo(AnaCarPath o) {
		// TODO Auto-generated method stub

		if(planStartTime!=o.planStartTime)
			return planStartTime-o.planStartTime;
		
		if(allTime!=o.allTime)
			return allTime-o.allTime;
		
		if(allLen!=o.allLen)
			return allLen-o.allLen;


		
		return 0;
	}

	public int getPlanStartTime() {
		return planStartTime;
	}

	public void setPlanStartTime(int planStartTime) {
		this.planStartTime = planStartTime;
	}
	
	
	
}
