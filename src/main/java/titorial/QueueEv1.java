package titorial;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;

public class QueueEv1 {

   RandomVariateGen genArr;
   RandomVariateGen genServ;
   LinkedList<Customer> waitList = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits     = new Tally ("Waiting times");
   Accumulate totWait  = new Accumulate ("Size of queue");

   class Customer { double arrivTime, servTime; }

   public QueueEv1 (double lambda, double mu) {
      genArr = new ExponentialGen (new MRG32k3a(), lambda);
      genServ = new ExponentialGen (new MRG32k3a(), mu);
   }

   public void simulate (double timeHorizon) {
      Sim.init();
      new EndOfSim().schedule (timeHorizon);
      new Arrival().schedule (genArr.nextDouble());
      //new initAccumulate().schedule(9700);
      Sim.start();
   }

   public class initAccumulate extends Event{
	   public void actions(){
		   totWait.init();
	   }
   }
 
   class Arrival extends Event {
      public void actions() {
         new Arrival().schedule (genArr.nextDouble()); // Next arrival.
         Customer cust = new Customer();  // Cust just arrived.
         cust.arrivTime = Sim.time();
         cust.servTime = genServ.nextDouble();
         if (servList.size() > 0) {       // Must join the queue.
            waitList.addLast (cust);
            //if(sim.time()>=10000)
              totWait.update (waitList.size());
         } 
         else {                         // Starts service.
        	   // if(sim.time()>=10000)
        	      custWaits.add (0.0);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
      }
   }

   class Departure extends Event {
      public void actions() {
         servList.removeFirst();
         if (waitList.size() > 0) {
            // Starts service for next one in queue.
            Customer cust = waitList.removeFirst();
           // if(sim.time()>=10000)
              totWait.update (waitList.size());
           // if(sim.time()>=10000)
              custWaits.add (Sim.time() - cust.arrivTime);
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

   public static void main (String[] args) {
      QueueEv1 queue = new QueueEv1 (1.0, 2.0);
      queue.simulate (100000.0);
      System.out.println (queue.custWaits.report());
      System.out.println (queue.totWait.report());
   }
}
