package edu.virginia.dtc.SItracking;


import edu.virginia.dtc.SysMan.Event;
import edu.virginia.dtc.Tvector.Tvector;
import android.content.Context;
import android.os.Bundle;


public class IOB_tracking {
	
	public long history_length = 21600; //secs -> 6 hours
	public long history_interval = 300;  //secs -> 5 minutes

	public double IOBtotal; // Insulin on Board based on 4h action curves for all injected insulin
	public double IOBbasal; // Insulin on Board based on 4h action curves for basal insulin
	public double IOBrel;// usual IOB
	
	public IOB_tracking(Context callingContext, long time, double BW) {
	double Gb=110;	

		Inputs_new inputs = new Inputs_new(history_length,history_interval,Gb); //allocation
	 	inputs.reconstruct(time,callingContext); //construct standard array of inputs
		IOBbasal=calculate_IOB(inputs.basal_array,time,"BASAL",callingContext);
		IOBrel=calculate_IOB(inputs.insulin_array,time,"REG",callingContext);
		IOBtotal=IOBbasal+IOBrel;
		
	}
	
	
	public double calculate_IOB(double[] insulin_history,long time,String type,Context callingContext) {
		final String FUNC_TAG = "IOB_MCM";
		int insulin_history_length = insulin_history.length;
		int ii;
		double IOB = 0.0;
		double[] IOBswancurve={1.0,0.9936,0.9873,0.9757,0.9642,0.9376,0.9110,0.8856,0.8602,0.8284,0.7966,0.7603,0.7239,0.6880,0.6522,0.6153,0.5783,0.5419,0.5055,0.4697,0.4339,0.4044,0.3749,0.3466,0.3183,0.2903,0.2623,0.2392,0.2161,0.1993,0.1826,0.1641,0.1456,0.1334,0.1213,0.1118,0.1023,0.0867,0.0711,0.0633,0.0555,0.0497,0.0439,0.0378,0.0318,0.0272,0.0225,0.0199,0.0173,0.0144,0.0116,0.0092,0.0069,0.0052,0.0035,0.0023,0.0012,0.0,0.0,0.0,0.0};
		try {
			for (ii=0; ii<61; ii++) { //convolution of insulin history by IOB_actioncurve
				IOB = IOB + 1000*5*insulin_history[ii]*IOBswancurve[60-ii]; //100*5* due to conversion from mU/min to U/interval (5min)
			}
			
		//	debug_message(FUNC_TAG, "IOB > time="+time+",IOB" + type + "="+IOB);
		}
 		catch (Exception e) {
 			Bundle b = new Bundle();
 			b.putString("description", "Error in "+FUNC_TAG+" > "+e.toString());
 			Event.addEvent(callingContext, Event.EVENT_SYSTEM_ERROR, Event.makeJsonString(b), Event.SET_LOG);
		}	
		return IOB;
	
	}

}
