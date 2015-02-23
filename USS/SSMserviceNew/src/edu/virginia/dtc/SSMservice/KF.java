package edu.virginia.dtc.SSMservice;

import Jama.*;

public class KF {
	private int steps;
	private Matrix KF_A;
	private Matrix KF_B;
	private Matrix KF_C;
	private Matrix KF_D;
	private Matrix KFstate;
	private Matrix KFu;	
	private double[][] KFuall;
	
	public double[][] KFout;
	
	public KF(double[][] KF_A_ini,double[][] KF_B_ini,double[][] KF_C_ini,double[][] KF_D_ini, double[][] KFstate_ini) {
		KF_A= new Matrix(KF_A_ini);
		KF_B= new Matrix(KF_B_ini);
		KF_C= new Matrix(KF_C_ini);
		KF_D= new Matrix(KF_D_ini);
		KFstate = new Matrix(KFstate_ini);
	}
	
	public void estimate(double[][] u, double[] measurement) {
		int n=u.length;
		steps = u[0].length; 
		
		double[][] KFu_ini = new double[n+1][1];
		KFu = new Matrix(KFu_ini);
		
		KFuall=new double[n+1][steps];

		double[][] KFout_temp_ini=new double[KF_C.getRowDimension()][1];
		Matrix KFout_temp = new Matrix(KFout_temp_ini);

		KFout = new double[KF_C.getRowDimension()][steps];

		int i,j;
		
		for (i=0;i<steps;i++){
		  	for(j=0;j<n;j++){
		  		KFu.set(j, 0, u[j][i]);
		  	}
		  	KFu.set(n,0,measurement[i]);
		  	
		  	KFuall[0][i]=KFu.get(0,0);
		  	KFuall[1][i]=KFu.get(1,0);
		  	KFuall[2][i]=KFu.get(2,0);
			
		  	KFout_temp = KF_C.times(KFstate).plus(KF_D.times(KFu));
		  	KFstate = KF_A.times(KFstate).plus(KF_B.times(KFu));
		
		  	for (j=0;j<KF_C.getRowDimension();j++){
		  		KFout[j][i]=KFout_temp.get(j,0);
		  	}
	    }
	}	
}