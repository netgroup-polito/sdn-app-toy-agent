package ae;

import java.io.*;
import java.net.*;
import Message.*;
import Utility.*;
import java.util.ArrayList;
import org.w3c.dom.*;


public class MyServerThread extends Thread {


boolean dseConnected = false;

int cont = 0;

Socket dseSocket = new Socket();
Socket aeSocket = new Socket();
private static Socket socket;

     public MyServerThread(Socket sock, int i){

     this.aeSocket = sock;

     this.cont = i;

     }

        String response = "test";

        String client = "";
        org.w3c.dom.Document aeRequestXML = null;
        org.w3c.dom.Document responseRequestXML = null;


      @Override
      public void run()

      {

      	     long startthread = System.currentTimeMillis();

                System.out.println("time thread started"+startthread+" with ID "+cont);
                System.out.println("the value of the thread ID is "+cont);

      	        aeRequestXML = readFromSocketToXmlDoc(cont);

                //aeSocket.close();
                System.out.println("the received doc is");
                XMLUtility.getInstance().printXML(aeRequestXML);

      	     long received = System.currentTimeMillis();

             System.out.println("time request received by AE"+received+" with ID "+cont);
             
             
             if(isElementExists("get-config", aeRequestXML))//receive get-config request
             {
                 
             System.out.println("received a get-config request");
             
          try
           {
            String host = "10.30.2.254";//adress of ESCAPE
            int port = 6789;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);
 
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            
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
        System.out.println("XML to String using BufferedReader : ");
        System.out.println(xml2String);
        
        bufReader.close();

            String sendMessage = xml2String;
            bw.write(sendMessage);
            bw.flush();
            System.out.println("Message sent to the server : "+sendMessage);
 
            //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            System.out.println("Message received from the server : " +message);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
                 
             }//if get-config request is received
             
             else//receive edit-config request
                 
             {       

                if (aeRequestXML != null) {//edit-config file or request for get-config received


                MyDseThread aeContainer = new MyDseThread();

                aeContainer.connectDse();

             //   if (aeContainer.isConnected()) {


                System.out.println("l'AE crée dans le thread est connecté");


                System.out.println("sending the request to the DSE");
                
                
            NorthBoundInterface nbI = new NorthBoundInterface();
            
            ArrayList<org.w3c.dom.Document> documents = nbI.mappingParameters(aeRequestXML);
                   
            for(int i=0; i<documents.size(); i++)
            {

            XMLUtility.getInstance().printXML(documents.get(i));
            
            AEMessage msg = new AEMessage(documents.get(i), "");
            //msg.setValue(doc);
                    
           msg = aeContainer.sendRequest(msg);
            
       
            System.out.println("Message envoyé au DSE");
            
          
                }

             try {


                   aeSocket.close();
                   }

                   catch (java.io.IOException ei)
		{
			//e1.printStackTrace();
		}

                if (aeRequestXML == null) {
                    System.out.println("xml nuull");
                }//if

                    }

             }//else: receive edit-config file
        }

synchronized public org.w3c.dom.Document readFromSocketToXmlDoc(int val) {

        XMLUtility xmUtilities = XMLUtility.getInstance();
        org.w3c.dom.Document doc;

        String s = "";
        try {
            BufferedReader receivedXML = new BufferedReader(new InputStreamReader((aeSocket.getInputStream())));
            String str = "xmlOutput".concat(String.valueOf(val));
            File tmp = File.createTempFile(str, ".tmp");
            System.out.println("The name of the temporary file is "+str);

            BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
            System.out.println("writing to temporary file...");
            while ((s = receivedXML.readLine()) != null) {
            //    System.out.println("we are in the loop of writing the request...");
                System.out.println(s);
                out.write(s);

                //if(s.endsWith(".Bye"))break;
                //System.out.println("the request ends with"+s.)
                if ((s.trim()).endsWith("</virtualizer>")) {
                    //out.write(s);
                    break;
                }
            }//while
            out.flush();
            out.close();
            //receivedXML.close();
            System.out.println("Data Written to the temporary file");
            System.out.println("File Closed");
            doc = xmUtilities.loadXML(tmp.getPath());
            tmp.deleteOnExit();
            return doc;
        } //try
        catch (Exception e) {

            System.out.println("readstring error : can not read from socket to file");
            e.printStackTrace();
            return null;
        }//catch


    }//


synchronized public boolean writeToSocket(BufferedWriter socket, String request) {

        try {
            System.out.println("writing to socket");
            socket.write(request);

            socket.flush();
            socket.close();
            System.out.println("response from ae to ae written to socket");

            return true;
        }//try
        catch (Exception e) {
            System.out.println("cannot write to socket");
            System.out.println("the request is" + request);
            e.printStackTrace();
            return false;
        }//catch
    }//writeToSocket


    synchronized private void connectDse() {
        try {



             dseSocket = new Socket("127.0.0.1", 4048);



            dseSocket.setKeepAlive(true);
            dseConnected = true;


          //  dseConnection.setText("Connected");
        } catch (Exception e) {
            System.out.println("Error! Connection to DSE not available");
            dseConnected = false;

          //  dseConnection.setText("Notconnected");
        }
    }


 public boolean isConnected() {
        return dseConnected;
    }



public boolean isElementExists(String content, Document doc) {
    
    NodeList nodeList = doc.getElementsByTagName(content);
    boolean found = false;
    if(nodeList.getLength() > 0)
    {found = true;}
    return found;
}




}
