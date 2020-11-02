package monprojet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.LognormalGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.Tally;

public class MonTp {
		RandomVariateGen genArrA,  genArrB,genArrp1,genArrp2,genArrp3;
		LognormalDist genServA,genServB,genServRetard;
		LinkedList<Client> waitListA = new LinkedList<Client> ();
		LinkedList<ClientB> waitListB = new LinkedList<ClientB> ();
		LinkedList<ClientB> rvList   = new LinkedList<ClientB> ();
		LinkedList<Client> servListA = new LinkedList<Client> ();
		LinkedList<ClientB> servListB = new LinkedList<ClientB> ();
		   Tally custWaitsA     = new Tally ("Waiting times Type A");
		   Tally custWaitsB     = new Tally ("Waiting times Type B");
		   Accumulate totWaitA  = new Accumulate ("Size of queue A");
		   Accumulate totWaitB = new Accumulate ("Size of queue B");
	

		   static final double HOUR = 3600.0;// Le temps est en seconde
		   // Les données
		   // Les tarifs d'arrivée sont par heure, les temps de service et de patience sont en secondes.
		   double openingTime; // Heure d'ouverture du centre (en heures).
		   int numPeriods; // Nombre de périodes de travail (heures) dans la journée.
		   int  [] nCaissiersParPeriode; // Nombre de caissiers pour chaque période.
		   int[]  mConseillersParPeriode ;//Nombre de conseillers pour chaque période.
		   double [] lambda; // Taux d'arrivée de base lambda_j pour chaque j.
		   int rv;// nbre de plages de la journee;
		   double timerv;  // Duree de la plage.    
		   double r ; //probabilite d'un rv prevu.
		   double alpha0; // Paramètre de distribution gamma pour B.
		   double p; // Probabilité pour que le client ne se presente pas.
		   Random random = new Random();
		   float R = random.nextFloat();//delai de retard.
		   double s;// Tps d'attente du conseiller.
		  // double occupation; // Valeur actuelle de B.
		   double arrRate = 0.0; // Taux d'arrivée actuel.
		   int nCaissier; // Nombre de caissiers dans la période en cours.
		   int mConseiller;// Nombre de conseillers dans la période en cours.
		   int nBusy; // Nombre d'agents occupés;
		   int nArrivals; // Nombre d'arrivées aujourd'hui;
		   //int nGoodQoS; // Nombre de temps d'attente inférieur à s aujourd'hui.
		  // double nCallsExpected; 
		   	RandomStream stream;
		    double muA,sigmaA,muB,sigmaB,sigmaR,muR,lambaA; // Paramètres de distribution du temps de service gamma.

	
		   	public void readData (String fileName) throws IOException {
		   		Locale loc = Locale.getDefault();
		   		Locale.setDefault(Locale.US); // to read reals as 8.3 instead of 8,3
		   		BufferedReader input = new BufferedReader (new FileReader (fileName));
		   		Scanner scan = new Scanner(input);
		   		openingTime = scan.nextDouble();
		   		scan.nextLine();
		   		numPeriods = scan.nextInt();
		   		scan.nextLine();
		   		nCaissiersParPeriode = new int[numPeriods];
		   		lambda = new double[numPeriods];
		   		//nCallsExpected = 0.0;
		   		for (int j = 1; j < 3; j++) {
		   			nCaissiersParPeriode[j] = scan.nextInt();
		   			mConseillersParPeriode [j] = scan.nextInt();
		   			lambda[j] = scan.nextDouble();
		   		
		   		}
		   		alpha0 = scan.nextDouble();
		   		scan.nextLine();
		   		p = scan.nextDouble();
		   		scan.nextLine();
		   		//nu = scan.nextDouble();
		   		scan.nextLine();
		   		//alpha = scan.nextDouble();
		   		scan.nextLine();
		   		//beta = scan.nextDouble(); 
		   		scan.nextLine();
		   		s = scan.nextDouble();
		   		scan.close();
		   		Locale.setDefault(loc);
		   		}
class Client{ double arrivTime, servTime;String type; }
	 
class ClientB extends  Client{double heureRV,numeroConseiller;}
 
class Agent{ String nom, servTime;String type; }
 
class  Conseiller extends Agent{ double heureRv,numeroConseiller; }
 
class RendezVous{ double heureRV; }
public MonTp (String fileName) throws IOException {
readData (fileName);

// public MonTp(double  lambdaA1,double lambdaA2,double lambdaA3,double muA,double sigmaA,  double muB,double sigmaB,double muR,double sigmaR) {
    // genArrp1 = new ExponentialGen (new MRG32k3a(), lambdaA1);
    // genArrA = new ExponentialGen (new MRG32k3a(), lambdaA);
    // genArrp2 = new ExponentialGen (new MRG32k3a(), lambdaA2);
    // genArrp3 = new ExponentialGen (new MRG32k3a(), lambdaA3);
     genServA= new LognormalDist(muA, sigmaA);
     genServB = new LognormalDist(muB, sigmaB);
     genServRetard =new LognormalDist(muR, sigmaR);
     //genServA = new LognormalGen( stream, muA, sigmaA);
     //  genServB = new LognormalGen(stream, muB, sigmaB);
     
    
 }
 
 
		 
class Arrival extends Event {
	 String type;
	 
  public Arrival(String t){ type=t; }
	 
    public void actions() 
    {
         if(type.equals("A"))
           { 
        	 new Arrival("A").schedule (genArrA.nextDouble()); // Next arrival.
             Client c = new Client();  // Cust just arrived.
             c.arrivTime = Sim.time();  
             c.servTime = ((RandomStream) genServA).nextDouble();
             c.type="A";
             Agent A=new Agent();
             A.type="Ca";
             
             if(servListA.size()> nCaissier)
			  { 
			     waitListA.addLast (c);
		           totWaitA.update (waitListA.size());
		      }   
			  else
		      {
		   		   custWaitsA.add (0.0);
		             servListA.addLast (c);
		             nBusy++;
		             new Departure("A","Ca").schedule (c.servTime);	  
			  }
           } 
         else
	       {
        	 new Arrival("B").schedule (genArrB.nextDouble()); // Next arrival.
             ClientB cB = new ClientB();  // Cust just arrived.
             cB.arrivTime = Sim.time();  
             cB.servTime = ((RandomStream) genServB).nextDouble();
             cB.type="B";
             Conseiller C=new Conseiller();
             C.type="C";
             if(servListB.size()>mConseiller)
			  { 
			     waitListB.addLast (cB);
		           totWaitB.update (waitListB.size());
		      }  
			  else
		      {
				  if(cB.numeroConseiller==C.numeroConseiller)
	            	 {
	            		 custWaitsB.add (0.0);
			             servListB.addLast (cB);
			             nBusy++;
			             new Departure("B","C").schedule (cB.servTime);
	            	 }
		   		   
			  }
              
	       }
         
      }
 
 }

class Departure extends Event {
 String type1,type2;
 public Departure(String v,String a){ type1=v; type2=a; }
      public void actions() {
    	  
    	  if (type1.equals("A") && type2.equals("C")) 
    	  {
    		  servListA.removeFirst();
    		  nBusy--;
    		 if (waitListA.size() > 0)
    		  {
    			Client c = waitListA.removeFirst();
	            totWaitA.update (waitListA.size());
	            custWaitsA.add (Sim.time() - c.arrivTime);
	            servListA.addLast (c);
	            new Departure("A","C").schedule (c.servTime);
    		  }
    	
    	  }
    	  else if(type1.equals("A") && type2.equals("D"))
    	  {
				servListB.removeFirst();
				nBusy--;
				RendezVous Rv=new RendezVous();
				if(rvList.size()>0)
				{
					for(int i=0;i<rvList.size();i++)
					{
							
						  if(Sim.time()==rvList.get(i).heureRV)
						{
							 ClientB ca = waitListB.removeFirst();
							 totWaitB.update (waitListB.size());
							 custWaitsB.add(Sim.time() - ca.arrivTime); 
							 servListB.addLast (ca);
							new Departure("B","C").schedule (ca.servTime);
						}
						  else if(s>=(rvList.get(i).heureRV))
						  {
							  Client c = waitListA.removeFirst();
					            totWaitA.update (waitListA.size());
					            custWaitsA.add (Sim.time() - c.arrivTime);
					            servListA.addLast (c);
					            new Departure("A","C").schedule (c.servTime);
						  }
				   }	
				}
			
    	  }
    	  else if(type1.equals("B"))
    	  {
    		  servListB.removeFirst();
    	  }
    
      }
   }




}

 
