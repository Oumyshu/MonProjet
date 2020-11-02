package ssjM1SIR.Deptinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Exemple {

	public static void main(String[] args) {
		InetAddress localAddress;
		InetAddress serverAddress;
		try {
			localAddress=InetAddress.getLocalHost();
			System.out.println("L'adresse locale est : " +localAddress);
			serverAddress=InetAddress.getByName("www.ucad.sn");	
			//serverAddress=InetAddress.getAllByName("www.ucad.sn");
			System.out.println("L'adresse du serveur web de l'ucad est : " +serverAddress);
			
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
