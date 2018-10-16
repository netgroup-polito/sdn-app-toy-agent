package ae;

import Message.AEMessage;
import Utility.XMLUtility;
import java.io.*;
import java.net.*;
import java.util.*;


public class HTTPPOSTServer extends Thread {
	
	static final String HTML_START = 
			"<html>" +
			"<body>";
			
    static final String HTML_END = 
			"</body>" +
			"</html>";
    
    	static final String HTML_START_XML = "<?xml version=\"1.0\" ?>";
			
		
	Socket connectedClient = null;	
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
        
        static int num_requests = 1;
	
			
	public HTTPPOSTServer(Socket client) {
		connectedClient = client;
	}			
			
	public void run() {
		
	  String currentLine = null, postBoundary = null, contentength = null, filename = null, contentLength = null;
	  PrintWriter fout = null;
		
	  try {
		
		System.out.println( "The Client "+
        connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");
            
        inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));                  
        outToClient = new DataOutputStream(connectedClient.getOutputStream());
        
        currentLine = inFromClient.readLine();
        String headerLine = currentLine;            	
        StringTokenizer tokenizer = new StringTokenizer(headerLine);
		String httpMethod = tokenizer.nextToken();
		String httpQueryString = tokenizer.nextToken();
		
		System.out.println(currentLine);
				
        if (httpMethod.equals("GET")) {//GET request
            
        	System.out.println("GET request");  
                
                System.out.println("httpQueryString: "+httpQueryString);
                
			if (httpQueryString.equals("/")) {
   				  // The default home page
   				  String responseString = HTTPPOSTServer.HTML_START + 
   				  "<form action=\"http://127.0.0.1:8899\" enctype=\"multipart/form-data\"" +
   				  "method=\"post\">" +
   				  "Enter the name of the File <input name=\"file\" type=\"file\"><br>" +
  				  "<input value=\"Upload\" type=\"submit\"></form>" +
  				  "Upload only text files." +
  				  HTTPPOSTServer.HTML_END;
				  sendResponse(200, responseString , false);				  			  
				} 
                        
                        else if (httpQueryString.endsWith("/ping")) {
                            
                                 // Verify the reachability of the REST API
   				  String responseString = HTTPPOSTServer.HTML_START + 
   				  "OK" +
  				  HTTPPOSTServer.HTML_END;
				  sendResponse(200, responseString , false);				  			  
				 
                        
                        }
                        
                        else if (httpQueryString.endsWith("/get-config")) {
                            
                                  // The topology in a virtualizer format
                                   File xmlFile = new File("unify-get-config.xml");
                                   Reader fileReader = new FileReader(xmlFile);
                                   BufferedReader bufReader = new BufferedReader(fileReader);
        
                                   StringBuilder sb = new StringBuilder();
                                   String line = bufReader.readLine();
                                   while( line != null){
                                        sb.append(line).append("\n");
                                        line = bufReader.readLine();
                                    }
                                    String xml2String = sb.toString();

   				  String responseString = HTTPPOSTServer.HTML_START + xml2String + HTTPPOSTServer.HTML_END;
                                  
				  sendResponse(200, responseString , false);
                        }
                        else {
                  sendResponse(404, "<b>The Requested resource not found ...." +
				  "Usage: http://193.205.83.126:8899</b>", false);				  
				}
		}
        
    /********************************************************************************************************/
        
		else { //POST request
                    
                    org.w3c.dom.Document doc = null;
                    System.out.println("POST request"); 
                    
			do {
			      //  currentLine = inFromClient.readLine();
                                
                               // System.out.println("currentLine: "+currentLine);
                                
                        if (currentLine.contains("/get-config")) {
                                 
                         System.out.println("we are in get-config");
                                 
			 // The topology in a virtualizer format
                                   File xmlFile = new File("unify-get-config.xml");
                                   Reader fileReader = new FileReader(xmlFile);
                                   BufferedReader bufReader = new BufferedReader(fileReader);
        
                                   StringBuilder sb = new StringBuilder();
                                   String line = bufReader.readLine();
                                   while( line != null){
                                        sb.append(line).append("\n");
                                        line = bufReader.readLine();
                                    }
                                    String xml2String = sb.toString();

                                  String responseString = HTTPPOSTServer.HTML_START_XML + xml2String;
                                 // System.out.println("responseString: "+responseString);
                                 String fileName = "unify-get-config.xml";
				 sendResponse(200, fileName , true);
                
                             
				} //if	 
                        
													
                             else if (currentLine.contains("edit-config")) {
                                 
                                 while (true) {
				  	currentLine = inFromClient.readLine();
				  	if (currentLine.contains("Content-Length:")) {
				  		contentLength = currentLine.split(" ")[1];
				  		System.out.println("Content Length = " + contentLength);
				  		break;
				  	}				  	
				  }
                                 
				XMLUtility xmUtilities = XMLUtility.getInstance();
                                

                                String s = "";
                                try {
            
                                String str = "xmlOutput".concat(String.valueOf(0));
                                File tmp = File.createTempFile(str, ".tmp");
                                System.out.println("The name of the temporary file is "+str);

                                BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
                                System.out.println("writing to temporary file...");
                                
                                String Source = "";
                                 //put body into an edit-config.xml file
                                 while(true)
                                  {
                                  //System.out.println(currentLine);
                                  currentLine = inFromClient.readLine();
                                  Source = Source.concat(currentLine);
                                  if(currentLine.equals("</virtualizer>"))
                                  {break;}
                                  }
  
                                 String substr = "<?xml version=\"1.0\" ?>";
                                 
                                 String xmlSource = "<?xml version=\"1.0\" ?>".concat(Source.substring(Source.indexOf(substr) + substr.length()));
                                 java.io.FileWriter fw = new java.io.FileWriter("my-file.xml");
                                 fw.write(xmlSource);
                                 fw.close();
                  
                                 doc = xmUtilities.loadXML("my-file.xml");
                            
  
                                System.out.println("Data Written to the temporary file");
                                System.out.println("File Closed");
                               
                                } //try
        
                                catch (Exception e) {

                            System.out.println("readstring error : can not read from socket to file");
                            e.printStackTrace();

                            }//catch		  			 
				  
                              XMLUtility.getInstance().printXML(doc);  
			      sendResponse(200, " ", false);
			      //fout.close();
                              
                              /*********send the edit-config file to the Application Entity file****************/
                              
                MyDseThread aeContainer = new MyDseThread();

                aeContainer.connectDse();
               
                NorthBoundInterface nbI = new NorthBoundInterface();
            
                ArrayList<org.w3c.dom.Document> documents = nbI.mappingParameters(doc);
                   
                for(int i=0; i<documents.size(); i++)
                    {

                    XMLUtility.getInstance().printXML(documents.get(i));
            
                    AEMessage msg = new AEMessage(documents.get(i), "");

                    msg = aeContainer.sendRequest(msg);
            
                    System.out.println("Message envoyé au DSE");

                  }
				} //else if	
                        
         /******************************************************************************************************/   
                    else if (currentLine.contains("cancel-config")) {
                                 
                                 while (true) {
				  	currentLine = inFromClient.readLine();
				  	if (currentLine.contains("Content-Length:")) {
				  		contentLength = currentLine.split(" ")[1];
				  		System.out.println("Content Length = " + contentLength);
				  		break;
				  	}				  	
				  }
                                 
				XMLUtility xmUtilities = XMLUtility.getInstance();
                                

                                String s = "";
                                try {
            
                                String str = "xmlOutput".concat(String.valueOf(0));
                                File tmp = File.createTempFile(str, ".tmp");
                                System.out.println("The name of the temporary file is "+str);

                                BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
                                System.out.println("writing to temporary file...");
                                
                                String Source = "";
                                 //put body into an edit-config.xml file
                                 while(true)
                                  {
                                  System.out.println(currentLine);
                                  currentLine = inFromClient.readLine();
                                  Source = Source.concat(currentLine);
                                  if(currentLine.equals("</ServiceCancellationRequest>"))
                                  {break;}
                                  }
  
                                 String substr = "<?xml version=\"1.0\" ?>";
                                 
                                 String xmlSource = "<?xml version=\"1.0\" ?>".concat(Source.substring(Source.indexOf(substr) + substr.length()));
                                 java.io.FileWriter fw = new java.io.FileWriter("my-file.xml");
                                 fw.write(xmlSource);
                                 fw.close();
                  
                                 doc = xmUtilities.loadXML("my-file.xml");
                            
  
                                System.out.println("Data Written to the temporary file");
                                System.out.println("File Closed");
                               
                                } //try
        
                                catch (Exception e) {

                            System.out.println("readstring error : can not read from socket to file");
                            e.printStackTrace();

                            }//catch		  			 
				  
                              XMLUtility.getInstance().printXML(doc);  
			      sendResponse(200, " ", false);
			      //fout.close();
                              
                              /*********send the cancel-config file to the Application Entity file****************/
                              
                MyDseThread aeContainer = new MyDseThread();

                aeContainer.connectDse();
               
                AEMessage msg = new AEMessage(doc, "");

                 msg = aeContainer.sendRequest(msg);
            
                    System.out.println("Message envoyé au DSE");

                    } //else if       
                        
        /******************************************************************************************************/                     
                             
			}while (inFromClient.ready()); //End of do-while
	  	}//else
	  } catch (Exception e) {
			e.printStackTrace();
	  }	
	}

	
	public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {
		
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String fileName = null;		
		//String contentTypeLine = "Content-Type: text/html" + "\r\n";
                String contentTypeLine = "Content-Type: xml" + "\r\n";
		FileInputStream fin = null;
		
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";	
			
		if (isFile) {
			fileName = responseString;			
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";	
		}						
		else {
			responseString = HTTPPOSTServer.HTML_START + responseString + HTTPPOSTServer.HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";	
		}			
		 
		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");		
		
		if (isFile) sendFile(fin, outToClient);
		else outToClient.writeBytes(responseString);
		
		outToClient.close();
	}
	
	public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[1024] ;
		int bytesRead;
	
		while ((bytesRead = fin.read(buffer)) != -1 ) {
		out.write(buffer, 0, bytesRead);
	    }
	    fin.close();
	}
        
        			
	public static void main (String args[]) throws Exception {
		
		//ServerSocket Server = new ServerSocket (8899, 10, InetAddress.getByName("192.168.56.102"));         
                ServerSocket Server = new ServerSocket (8899, 10, InetAddress.getByName("127.0.0.1"));         
		System.out.println ("HTTP Server Waiting for client on port 8899");
								
		while(true) {	                	   	      	
		     Socket connected = Server.accept();
	            (new HTTPPOSTServer(connected)).start();
        }      
	}
}