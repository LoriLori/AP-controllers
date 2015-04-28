package edu.virginia.dtc.SItracking;


public class KF_processing_new {
	
	LinearDiscModel buff_ins_model;
	LinearDiscModel buff_meal_model;
	LinearDiscModel ins_model;
	LinearDiscModel meal_model;
	KF kf;
	double[] Gest; // 8h
	double[] SI_8h; // 2h warm up + SI_6h
  	double[] SI_6h; // 6h
  	boolean isValid = true;
  	

  public KF_processing_new(Inputs_new inputs, double BW,double Gb) {
    

	//buff model
	double[][] MEAL_BUFF_A_init={{0.188876, 0.0}, 
			                     {0.314793, 0.188876}};
	double[][] MEAL_BUFF_B_init={{0.811124}, 
			                     {0.496332}};
	double[][] INS_BUFF_A_init={{0.188876, 0.0}, 
								{0.314793, 0.188876}};
	double[][] INS_BUFF_B_init={{0.811124}, 
								{0.496332}};
	double[][] BUFF_C_init={{0.0, 1.0}};
	double[][] BUFF_D_init={{0.0}};
	double[][] BUFFstate_ins_ini={{0.0}, {0.0}};
	double[][] BUFFstate_meal_ini={{0.0}, {0.0}};

	//insulin model
	double[][] INS_A_init={{0.902877, 0.0,      0.0},
					       {0.092245, 0.902877, 0.0},
					       {0.003309, 0.054439, 0.281072}};
	double[][] INS_B_init={	{4.753059}, 																	// Now includes BW dependency
							{0.238672}, 
							{0.006173}};
	double[][] INS_C_init={	{1.0, 0.0, 0.0}, 
			                {0.0, 1.0, 0.0}, 
			                {0.0, 0.0, 1.0}};
	double[][] INS_D_init={{0.0}, {0.0}, {0.0}};
	double[][] INSstate_ini={{0.0}, {0.0}, {0.0}};
	
	//meal model
	double[][] MEAL_A_init={{0.832767, 0.0,      0.0}, 
			                {0.152689, 0.832445, 0.0},
			                {0.012995, 0.142033, 0.844143}};
	double[][] MEAL_B_init={{0.167233}, 
			                {0.014864},
			                {0.000829}};
	double[][] MEAL_C_init={{1.0, 0.0, 0.0}, 
			                {0.0, 1.0, 0.0},
			                {0.0, 0.0, 1.0}};
	double[][] MEAL_D_init={{0.0}, {0.0}, {0.0}};
	double[][] MEALstate_ini={{0.0}, {0.0},{0.0}};
	
	//KF model
	double[][] KF_A_init={{0.655357, -0.002268, -0.010844}, 
			              {0.0,    0.925298,  0.0},
			              {1.219438, 0.0,     0.920044}};
	double[][] KF_B_init={{-0.000089, 0.080283, -0.011305, 0.310921},
			              {0.074702,  0.0,      0.0,       0.0},
			              {0.0,       0.0,      0.0,       -1.219438}};
	double[][] KF_C_init={{0.693103, 0.0, 0.0},
			              {0.693103, 0.0, 0.0},
			              {0.0,      1.0, 0.0},
			              {1.325412, 0.0, 1.0}};
	double[][] KF_D_init={{0.0, 0.0, 0.0, 0.306897},
			              {0.0, 0.0, 0.0, 0.306897},
			              {0.0, 0.0, 0.0, 0.0},
			              {0.0, 0.0, 0.0, -1.3254}};
	double[][] KFstate_init={{0.0}, {0.0}, {0.0}};
	
    // other parameters
    double VI = 0.0601;
    double kcl = 0.2538;
    double gamma = 300;
    double KSI = 0.0; // base line of log(SI/SIb) that is equal to 0.
	double SIb = 4.1345e-4;
    double Ib = inputs.basal[0]/(kcl*VI*BW); // use the initial basal value to calculate Ib.
    // double Ib = getmean(inputs.basal)/(kcl*VI*BW); // use the average basal to calculate Ib.
    
	//feed-forward model
	buff_ins_model = new LinearDiscModel(INS_BUFF_A_init,INS_BUFF_B_init,BUFF_C_init,BUFF_D_init, BUFFstate_ins_ini);
	buff_meal_model = new LinearDiscModel(MEAL_BUFF_A_init,MEAL_BUFF_B_init,BUFF_C_init,BUFF_D_init, BUFFstate_meal_ini);
	ins_model = new LinearDiscModel(INS_A_init,INS_B_init,INS_C_init,INS_D_init, INSstate_ini);
	meal_model = new LinearDiscModel(MEAL_A_init,MEAL_B_init,MEAL_C_init,MEAL_D_init, MEALstate_ini);


	buff_ins_model.predict(inputs.J);
	buff_meal_model.predict(inputs.meal);
	
	
	ins_model.predict(buff_ins_model.out);
	meal_model.predict(buff_meal_model.out);
	

	//construct KF inputs
	int i,N; // pay attention to those N's
	N=inputs.J.length;
	double[][] kfinput = new double[3][N];
	for (i=0;i<N;i++){
		kfinput[0][i] = ins_model.out[2][i]/(VI*BW)-Ib;  //I
		kfinput[1][i] = meal_model.out[2][i]/(meal_model.out[2][i]+gamma); //Ra
		kfinput[2][i] = KSI; 
	}

	
	//KF estimation	
	kf = new KF(KF_A_init,KF_B_init,KF_C_init,KF_D_init,KFstate_init);
	kf.estimate(kfinput,inputs.CGM_log); // feed-forward inputs + measurement
	
	Gest = new double[N];
	SI_8h = new double[N];
	SI_6h = new double[72];
	
	for (i=0;i<N;i++) {
		Gest[i] = Math.exp(kf.KFout[0][i])*Gb;
		SI_8h[i] = Math.exp(kf.KFout[2][i]+KSI)*SIb;
		if (i<72) {
			SI_6h[i] = Math.exp(kf.KFout[2][i+N-72]+KSI)*SIb;
		}
	}
  }
  
  public double getmean(double[] input){
	  
	  double sum=0.0, result=0.0;
	  if (input.length < 1) {
		  isValid = false;
	  }
	  else {
		  for (int i=0;i<input.length;i++){
			sum=sum+input[i];  
		  }
		  result = sum/input.length;
	  }
	  return result;
  }

}
