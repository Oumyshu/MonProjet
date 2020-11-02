package ssjM1SIR.Deptinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class PremierClient {

	public static void main(String[] args) {
		
		try {
			Socket s=new Socket("time-a.nist.gov",13);
			BufferedReader in =new BufferedReader( new InputStreamReader(s.getInputStream()));
			boolean more= true;
			while(more) {
				String line =in.readLine();
				if(line==null)
					more=false;
				else
					System.out.println(line);
			}
			
		}
	
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
