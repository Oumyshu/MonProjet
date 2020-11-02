package titorial;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import umontreal.ssj.util.Chrono;

import java.util.LinkedList;

public class QueueEvModeleV {

   RandomVariateGen genArr1, genArr2;
   RandomVariateGen genServ1, genServ2;
   LinkedList<Customer> waitList1 = new LinkedList<Customer> ();
   LinkedList<Customer> waitList2 = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits1     = new Tally ("Waiting times Type 1");
   Tally custWaits2     = new Tally ("Waiting times Type 2");
   Accumulate totWait1  = new Accumulate ("Size of queue 1");
   Accumulate totWait2  = new Accumulate ("Size of queue 2");
   int s;
   RandomStream stream;
   class Customer { double arrivTime, servTime; int type; }

   public QueueEvModeleV (double lambda1, double lambda2, double mu1, double mu2, int serveurs) {
      genArr1 = new ExponentialGen (new MRG32k3a(), lambda1);
      genArr2 = new ExponentialGen (new MRG32k3a(), lambda2);
      genServ1 = new ExponentialGen (new MRG32k3a(), mu1);
      genServ2 = new ExponentialGen (new MRG32k3a(), mu2);
      stream= new MRG32k3a();
      s=serveurs;
   }

   public void simulate (double timeHorizon) {
      Sim.init();
      new EndOfSim().schedule (timeHorizon);
      new Arrival(1).schedule (genArr1.nextDouble());  
      new Arrival(2).schedule (genArr2.nextDouble()); 
      Sim.start();
   }

   class Arrival extends Event {
	 int type;
	 public Arrival(int t){
		type=t;
	 }
      public void actions() {
          if(type==1)
           { new Arrival(1).schedule (genArr1.nextDouble()); // Next arrival.
             Customer cust = new Customer();  // Cust just arrived.
             cust.arrivTime = Sim.time();  
             cust.servTime = genServ1.nextDouble();
             cust.type=1;
              decision(cust);
          } 
          else
          {   new Arrival(2).schedule (genArr2.nextDouble()); // Next arrival.
              Customer cust = new Customer();  // Cust just arrived.
              cust.arrivTime = Sim.time();  
              cust.servTime = genServ2.nextDouble();
              cust.type=2;
              decision(cust);
          }
          
         

      
      }
      
      public void decision(Customer cust){
    	
    	  if(servList.size()>s-1)
    	  { if(cust.type==1)
    	     {  waitList1.addLast (cust);
                totWait1.update (waitList1.size());
              }
    	    else{waitList2.addLast (cust);
               totWait2.update (waitList2.size());
              }  
    	  }
    	  else
    	  {
    		     if(cust.type==1)
        		   custWaits1.add (0.0);
        		  else
        			 custWaits2.add (0.0); 
                  servList.addLast (cust);
                  new Departure().schedule (cust.servTime);	  
    	  }

      }
    
   
   }

/*   public int selectType(){
	   if(waitList1.size()>waitList2.size())
	    return 1;
	   else if(waitList1.size()<waitList2.size())
		 return 2;
	   else if (waitList1.size()==waitList2.size() && waitList1.size()>0 )
	   {  double val=stream.nextDouble();
		   if(val<=0.5)
			   return 1;
		    else 
		      return 2;
	   }
	   else
		   return 0;
   }*/
   
   class Departure extends Event {
      public void actions() {
         servList.removeFirst();
         int v=selectType();
         if (v== 1) {
            // Starts service for next one in queue.
            Customer cust = waitList1.removeFirst();
            totWait1.update (waitList1.size());
            custWaits1.add (Sim.time() - cust.arrivTime);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
          if (v== 2) {
             // Starts service for next one in queue.
             Customer cust = waitList2.removeFirst();
             totWait2.update (waitList2.size());
             custWaits2.add (Sim.time() - cust.arrivTime);
             servList.addLast (cust);
             new Departure().schedule (cust.servTime);
          }
      }
   }

   class EndOfSim extends Event {
      public void actions() {
         Sim.stop();
      }
   }

  public int selectType(){
	if(waitList1.size()>0 && waitList2.size()>0)
	{ if(waitList1.get(0).arrivTime<waitList2.get(0).arrivTime)
		  return 1;
	   else if( waitList1.get(0).arrivTime==waitList2.get(0).arrivTime)	  
	   { double v=stream.nextDouble();
		  if(v<0.5) 
		    return 1;
		  else
			 return 2;
	   }
	   else
		   return 2;
	}
	else if(waitList1.size()>0 && waitList2.size()==0)
	   return 1;
	else if(waitList1.size()==0 && waitList2.size()>0)
		   return 2;
	else
		return 0;
  } 
   
   
   
   public static void main (String[] args) {
	  double mu1=2.0;
	  double mu2=2.2;
	  double lambda1= 2.1;
	  double lambda2= 2.0;
	  int s=2;
      QueueEvModeleV queue = new QueueEvModeleV (lambda1, lambda2, mu1, mu2, s);
      Chrono timer = new Chrono();
      queue.simulate (1000.0);
      System.out.println("Total CPU time:      " + timer.format() + "\n");
      System.out.println (queue.custWaits1.report());
      System.out.println (queue.totWait1.report());
      
      System.out.println (queue.custWaits2.report());
      System.out.println (queue.totWait2.report());

      
      
   }
}
