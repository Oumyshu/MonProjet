package titorial;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;

public class QueueEvM {

   RandomVariateGen genArr;
   RandomVariateGen genServ;
   RandomVariateGen genPatience;
   LinkedList<Customer> waitList = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits     = new Tally ("Waiting times");
   Accumulate totWait  = new Accumulate ("Size of queue");
   static int nbAban=0;
   class Customer { double arrivTime, servTime, patienceTime; }

   public QueueEvM (double lambda, double mu, double nu) {
      genArr = new ExponentialGen (new MRG32k3a(), lambda);
      genServ = new ExponentialGen (new MRG32k3a(), mu);
      genPatience = new ExponentialGen (new MRG32k3a(), nu);
   }

   public void simulate (double timeHorizon) {
      Sim.init();
      new EndOfSim().schedule (timeHorizon);
      new Arrival().schedule (genArr.nextDouble());
      Sim.start();
   }

   
   class Abandon extends Event {
	   private Customer cust;
	   public Abandon(Customer cust){this.cust=cust;}
	   public void actions() {
		  waitList.remove(cust) ; 
		  nbAban++;
		  totWait.update (waitList.size());
	   }
   }
   class Arrival extends Event {
      public void actions() {
         new Arrival().schedule (genArr.nextDouble()); // Next arrival.
         Customer cust = new Customer();  // Cust just arrived.
         cust.arrivTime = Sim.time();  
         cust.servTime = genServ.nextDouble();
         cust.patienceTime=genPatience.nextDouble();
         if (servList.size() > 0) {       // Must join the queue.
            waitList.addLast (cust);
            new Abandon(cust).schedule(cust.patienceTime);
            totWait.update (waitList.size());
            
         } else {                         // Starts service.
            custWaits.add (0.0);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
      }
   }

   class Departure extends Event {
      public void actions() {
         servList.removeFirst();
        //  updateWaitList();
         if (waitList.size() > 0) {
          	 Customer cust = waitList.removeFirst();
        	 new Abandon(cust).cancel();
        	 totWait.update (waitList.size());
             custWaits.add (Sim.time() - cust.arrivTime);
             servList.addLast (cust);
             new Departure().schedule (cust.servTime);
        	
        	 // Starts service for next one in queue.
/*        	 Customer cust=null;
             do{
        	    cust = waitList.removeFirst();
               }
             while(cust.arrivTime+cust.patienceTime<sim.time() && waitList.size()>0);
           
            if(cust.arrivTime+cust.patienceTime>sim.time())
             {
            	 totWait.update (waitList.size());
                 custWaits.add (Sim.time() - cust.arrivTime);
                 servList.addLast (cust);
                 new Departure().schedule (cust.servTime);
             }*/
         }
      }
      

      
   }

   class EndOfSim extends Event {
      public void actions() {
         Sim.stop();
      }
   }

   public static void main (String[] args) {
	  double mu=2.0;
	  double lambda= 1.0;
	  double nu=0.5;
      QueueEvM queue = new QueueEvM (lambda, mu, nu);
      queue.simulate (100000.0);
      System.out.println (queue.custWaits.report());
      System.out.println (queue.totWait.report());
    
      System.out.println ("Abandon="+nbAban);
    
   }
}
