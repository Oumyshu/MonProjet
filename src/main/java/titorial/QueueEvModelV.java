package titorial;

import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;

public class QueueEvModelV {

/*
 * Dans ce cas on a un seul serveur pour deux fils d'attente differents
 * chaque fil d'attente a un taux d'arrive et un taux dde service different
 * Chaque client a une heure d'arriv�e et une dur�e de service qui suivent des loi de 
 * poisson de parametres respectif lamda1 et mu1 (pour la fil une) 
 * et  lamda2 et mu2 (pour la fil deux)
 * */	
   RandomVariateGen genArr1,genArr2;
   RandomVariateGen genServ1,genServ2;
   LinkedList<Customer> waitList1 = new LinkedList<Customer> ();
   LinkedList<Customer> waitList2 = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits     = new Tally ("Waiting times");
   Accumulate totWait  = new Accumulate ("Size of queue");

   class Customer { double arrivTime, servTime; }

   public QueueEvModelV (double lambda1,double mu1,double lambda2,  double mu2) {
      genArr1 = new ExponentialGen (new MRG32k3a(), lambda1);
      genServ1 = new ExponentialGen (new MRG32k3a(), mu1);
      genArr2 = new ExponentialGen (new MRG32k3a(), lambda2);
      genServ2 = new ExponentialGen (new MRG32k3a(), mu2);
   }
//	programmer la simulation pour unr dur�e bien d�termin�e
   public void simulate (double timeHorizon) {
//	   on initialise le compteur � 0
      Sim.init();
//      on prevoit l'arret apres "timeHorizon"
      new EndOfSim().schedule (timeHorizon);
//      On programme l'arriv�e du premier client
      new Arrival().schedule (genArr1.nextDouble());
//	   on demarre le compteur 
      Sim.start();
   } 
//   Gestion de l'arriv�e d'un client
   class Arrival extends Event {
      public void actions() {
//    	  on determine les heures des prochaines arrivee
    	  double arr1=genArr1.nextDouble();
    	  double arr2=genArr2.nextDouble();
    	  Customer cust = null;
    	  if(arr1<arr2) {
    		  Customer cust2=new Customer();
    		  cust2.arrivTime=arr2;
    		  cust2.servTime=genServ2.nextDouble();
    		  waitList2.addLast(cust2);
              totWait.update (waitList1.size() + waitList2.size());
    		  
//    		  si le client de la fil 1 est premier
//        	  on prevoit son arrivee dans arr1 unite de temps
              new Arrival().schedule (arr1);
//            d�s l'arriv�e, le client est cr�e avec ses attribut
              cust = new Customer();  // Cust just arrived.
//              son heure d'arriv�e est l'heure actuel
              cust.arrivTime = Sim.time();
//              on genere son temps de service
              cust.servTime = genServ1.nextDouble();
//            on verifie si le serveur est occup�
              if  (servList.size() >2){
           	   		waitList1.addLast (cust);
                 totWait.update (waitList1.size() + waitList2.size());
              }
//            si le serveur est libre
              else{// Starts service.
           	   custWaits.add (0.0);
                 servList.addLast (cust);
                 new Departure().schedule (cust.servTime);
              } 
    		  
    	  }else if(arr1==arr2) {
//    		  si les clients des deux fil sont arrive en meme temps
    		  
    	  }else {
    		  Customer cust1=new Customer();
    		  cust1.arrivTime=arr2;
    		  cust1.servTime=genServ1.nextDouble();
    		  waitList1.addLast(cust1);
              totWait.update (waitList1.size() + waitList2.size());
              
//    		  si le client  de la fil 2 est premier
              new Arrival().schedule (arr2);
//            d�s l'arriv�e, le client est cr�e avec ses attribut
              cust = new Customer();  // Cust just arrived.
//              son heure d'arriv�e est l'heure actuel
              cust.arrivTime = Sim.time();
//              on genere son temps de service
              cust.servTime = genServ2.nextDouble();
              //on verifie si le serveur est occup�
              if  (servList.size() >2){
         	   		waitList2.addLast (cust);
	               totWait.update (waitList1.size() + waitList2.size());
	            }
//            si le serveur est libre
              else{// Starts service.
           	   custWaits.add (0.0);
                 servList.addLast (cust);
                 new Departure().schedule (cust.servTime);
              } 
    		  
    	  }

      }
   }
//	Gestion du depart d'un client
   class Departure extends Event {
      public void actions() {
//    	  le client qui etait en service quitte le serveur
         servList.removeFirst();
         
         if (waitList1.size() >0 && waitList2.size()==0) {
            Customer cust1 = waitList1.removeFirst();
            
             totWait.update (waitList1.size() + waitList2.size());
             custWaits.add (Sim.time() - cust1.arrivTime);
           servList.addLast (cust1);
           
           new Departure().schedule (cust1.servTime);
        }else if (waitList1.size() ==0 && waitList2.size()>0) {
           Customer cust1 = waitList2.removeFirst();
           
            totWait.update (waitList1.size() + waitList2.size());
            custWaits.add (Sim.time() - cust1.arrivTime);
            servList.addLast (cust1);
          
          new Departure().schedule (cust1.servTime);
       }else if (waitList1.size() >0 && waitList2.size()>0) {
           Customer cust1 = waitList1.getFirst();
           Customer cust2 = waitList2.getFirst();
           
           if(cust1.arrivTime>cust2.arrivTime) {
        	   waitList2.removeFirst(); 
        	   totWait.update (waitList1.size() + waitList2.size());
               custWaits.add (Sim.time() - cust2.arrivTime);
               servList.addLast (cust2);
               new Departure().schedule (cust2.servTime);
           }else if(cust1.arrivTime==cust2.arrivTime) {
        	   
           }else{
        	   waitList1.removeFirst(); 
        	   totWait.update (waitList1.size() + waitList2.size());
               custWaits.add (Sim.time() - cust1.arrivTime);
               servList.addLast (cust1);
               new Departure().schedule (cust1.servTime);
           }
          
          
         
      }
   }
}

   class EndOfSim extends Event {
      public void actions() {
         Sim.stop();
      }
   }

   public static void main (String[] args) {
	   System.out.println("L'ex�cution a d�marr�");
      QueueEvModelV queue = new QueueEvModelV (1.0, 2.0,1.0, 2.0);
      queue.simulate (100000.0);
      System.out.println (queue.custWaits.report());
      System.out.println (queue.totWait.report());
      System.out.println("Fin d'execution");
   }
}
