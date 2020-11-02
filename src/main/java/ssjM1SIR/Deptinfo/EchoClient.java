package ssjM1SIR.Deptinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
	public static void main(String[] args) {
		try {
			Socket s = new Socket("10.153.43.52", 5555);
			BufferedReader in = new BufferedReader (new InputStreamReader(s.getInputStream()));
			PrintWriter out = new PrintWriter(s.getOutputStream(), true /* autoFlush */);
			Scanner clavier = new Scanner(System.in);
			boolean more = true;
			while (more) {
				String line = in.readLine();
				if (line ==null) 
					more = false;
				else
					System.out.println(line);
					line=clavier.nextLine();
					out.println(line);
				}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
