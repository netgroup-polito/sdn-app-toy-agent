
package Manager;

import java.net.*;
import java.util.*;


public class TCPXMLCommunicationManager extends Thread{

      
	private String tcpHost;
	private int tcpPort;
	private String client = "";
		
	public TCPXMLCommunicationManager(String host, int port)
	{	
		tcpHost = host;
		tcpPort = port;
	}
	
        @Override
	public void run()
	{
	 	try
	 	{
		 	ServerSocket ss = new ServerSocket(tcpPort);

			while (true)
			{
				Socket s = ss.accept();
				System.out.println("aperto un socket sulla porta locale " + s.getLocalPort() + " alla porta remota " + s.getPort() + " dell'Host " + (s.getInetAddress()).getHostName());
				InetAddress address = s.getInetAddress();
				client = address.getHostName();
				int porta = s.getPort();
				System.out.println("In chiamata Client: " + client + "porta:" + porta);
				
				String hostAddress = InetAddress.getLocalHost().getHostAddress();   
				
				StringTokenizer str = new StringTokenizer(hostAddress, ".");
				hostAddress = str.nextToken() + str.nextToken() + str.nextToken() + str.nextToken();
				
			}
		}
		catch (Exception e)
		{
		}			
	}	
	
	
    }
