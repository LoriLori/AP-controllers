package edu.virginia.dtc.SItracking;

import android.content.Context;


public class SItracking_processing {
	
	public long history_length = 28800; //secs -> 8 hours
	public long history_interval = 300;  //secs -> 5 minutes
	public double Gb = 110.0;
	public double SI; // average of SI over last 6h
	
	
	public SItracking_processing(Context callingContext, long time, double BW) {
		

		Inputs_new inputs = new Inputs_new(history_length,history_interval,Gb); //allocation
	 	inputs.reconstruct(time,callingContext); //construct standard array of inputs
		KF_processing_new kf_processing = new KF_processing_new(inputs,BW,Gb);
        SI = kf_processing.SI;
		
	}
	
	
	

}

