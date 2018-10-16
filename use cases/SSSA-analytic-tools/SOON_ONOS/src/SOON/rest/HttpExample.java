package rest;

import static DSE.Hex2Decimal.DecHex;

import Utility.XMLUtility;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static DSE.Hex2Decimal.hexDec;
import java.util.Iterator;


import org.json.simple.parser.JSONParser;

import Controller.Route;
import Controller.RouteId;
import Controller.NodePortTuple;
import DSE.Constants;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class HttpExample {
    

	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {

	HttpExample http = new HttpExample();
             //http.getRateperLink();
          //http.getFlowIdFromIntent("0x2b5");
          http.sendCancelConfig();
           // http.getDisjointRoutes("of:0000000000000001","1", "of:000000000000000a", "1");
	}
        
        /*****************************************************************************/
        
        public void parseXML()
        {
        
                 for(int ind=1; ind<=1; ind++)
                        {
                    Document overloaded_paths = XMLUtility.getInstance().loadXML("Database/".concat(String.valueOf(ind).concat("-forward_switches.xml")));
                    XMLUtility.getInstance().printXML(overloaded_paths);
                   
                    NodeList switches = overloaded_paths.getElementsByTagName("Switch");
        
                    for (int i = 0; i < switches.getLength(); i++) 
                    {
             
                    Element value = (Element) switches.item(i);    

                    String id = value.getAttribute("id");
         
                    String key = value.getAttribute("key");

                    System.out.println("sw=" + id + " key = " + key);
  
                    }  
                        }
        
}
        
   /***********************************************************************************/
        public void parseJSON()
        {
      /*  String test = " {\"treatment\":{\"instructions\":[{\"subtype\":\"ETH_SRC\",\"type\":\"L2MODIFICATION\",\"mac\":\"00:00:00:00:00:01\"},{\"port\":\"6\",\"type\":\"OUTPUT\"}],\"deferred\":[]},\"groupId\":0,\"priority\":1,\"deviceId\":\"of:0000000000000004\",\"timeout\":0,\"life\":108,\"packets\":8066,\"isPermanent\":true,\"lastSeen\":1490198107552,\"bytes\":12065704,\"appId\":\"org.onosproject.net.intent\",\"tableId\":0,\"selector\":{\"criteria\":[{\"port\":4,\"type\":\"IN_PORT\"},{\"type\":\"ETH_DST\",\"mac\":\"00:00:00:00:00:0B\"}]},\"id\":\"26458651902350877\",\"state\":\"ADDED\"}";
           String requestID = "";
          Pattern pattern3 = Pattern.compile("\"priority\":(.*?)\"");
           Matcher matcher3 = pattern3.matcher(test);
                if (matcher3.find())
                   {
                      requestID = matcher3.group(1);

                    
                    }
                
                System.out.println("requestID: "+matcher3.group(1));
                
                System.out.println("paths/path" + "1" + "-_" + "1" + ".xml");*/
            
            String test = "http://localhost:8181/onos/v1/links?device=of%3A000000000000000a&port=7";
            String sw = "";
            String port = "";
            
          /*  Pattern pat1 = Pattern.compile("http://localhost:8181/onos/v1/links?device=\\w+");
             Matcher mat1 = pat1.matcher(test);
                if (mat1.find())
                   {
                    sw = mat1.group(1);
                    }
         
                
             Pattern pat2 = Pattern.compile(sw.concat("port=(.*?)"));
             Matcher mat2 = pat2.matcher(test);
                if (mat2.find())
                   {
                      port = mat2.group(1);
                    }*/
            
            sw = test.substring(43, 64);
           port = test.substring(70, 71);
         
                System.out.println("sw: "+sw);
                System.out.println("port: "+port);
                sw = test.substring(43, 64);
                port = test.substring(70, 71);
                 sw = sw.replace("%3A", ":");
                 
                 String couple = sw.concat("-").concat(port);
                 System.out.println("couple: "+couple);
        
        }
        
        /*****************************************************************************/
        
        public void testparse()
        {
        String srcSW = "1";String destSW = "4";
        
              String OFsrcSW = "";
                          String OFdestSW = "";
                          Document mappingOF = XMLUtility.getInstance().loadXML("OFSwitches.xml");
                          NodeList switches = mappingOF.getElementsByTagName("Switch");
                          for (int ind = 0; ind < switches.getLength(); ind++) {
                            Node node = switches.item(ind);
                            Element value = (Element) node;    
                            String id = value.getAttribute("id");
                          //  System.out.println("id: "+id);
                            String of = value.getAttribute("of"); 
                          //  System.out.println("of: "+of);
                            
                            if(srcSW.equals(id))
                            {OFsrcSW = of;}
                            else if(String.valueOf(destSW).equals(id))
                            {OFdestSW = of;}    
                           }
         //  System.out.println("OFdestSW: "+OFdestSW);
       //    System.out.println("OFsrcSW: "+OFsrcSW);
            
        }
        
        
     /**
     * @return **********************************************************************************************/     
        
        public ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> getLinksStatus() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
            ConcurrentHashMap<String, String> RateStatus = new ConcurrentHashMap<String, String>();
            
            ConcurrentHashMap<String, String> ByteCounterStatus = new ConcurrentHashMap<String, String>();
            
            ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> LinksStatus = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>>();
            
          //  System.out.println("getFlowStatsPerPort: - switchID - "+switchID+" - portID - "+portID);
            
            ArrayList<String> flowsIentifiers = new ArrayList<String>();

                String url = "http://localhost:8181/onos/v1/statistics/flows/link";
   	
	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                             
                String result = response.toString();
              //  System.out.println("result: "+result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray loads = jsonResponse.getJSONArray("loads");
                
                 for(int ind=0; ind<loads.length(); ind++)
                {
                    
                 int rate = loads.getJSONObject(ind).getInt("rate"); 
              //   System.out.println("rate: "+rate);
                 
                 double byte_counter = loads.getJSONObject(ind).getDouble("latest"); 
              //   System.out.println("byte_counter: "+byte_counter);
                 
                 String link = loads.getJSONObject(ind).getString("link"); 
             //    System.out.println("link: "+link);
                 
                 String sw = link.substring(43, 64);
                 String port = link.substring(70, 71);
             //    sw = sw.replace("%3A", ":");
                 
                 String couple = sw.concat("-").concat(port);
          //       System.out.println("couple: "+couple);
                 
                 RateStatus.put(couple, String.valueOf(rate));
                 ByteCounterStatus.put(couple, String.valueOf(byte_counter));
                 
                }
                 
                
                LinksStatus.put(0, RateStatus);
                LinksStatus.put(1, ByteCounterStatus);
                
                return LinksStatus;
           
	}
        
     /**
     * @return **********************************************************************************************/  
        
        public String getFlowIdFromIntent(String currentIntent) throws IOException, JSONException{
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
            String FlowId = "";
            
            String url = "http://localhost:8181/onos/v1/intents/relatedflows/org.onosproject.net.intent/".concat(currentIntent);
   	
	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                     
                String result = response.toString();
                System.out.println("result: "+result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray paths = jsonResponse.getJSONArray("paths");                
                                   
                FlowId = paths.getJSONArray(0).getJSONObject(0).getString("id");   
                System.out.println("flow id: "+FlowId);
                 
            return FlowId;
        }
        
        /**
     * @return **********************************************************************************************/     
        
        public ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> getSwitchesStatusPerPort() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
            ConcurrentHashMap<String, String> packetsReceived = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> packetsSent = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> bytesReceived = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> bytesSent = new ConcurrentHashMap<String, String>();
            
            ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> SwitchesStatus = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>>();
            
          //  System.out.println("getFlowStatsPerPort: - switchID - "+switchID+" - portID - "+portID);
            
            ArrayList<String> flowsIentifiers = new ArrayList<String>();

                String url = "http://localhost:8181/onos/v1/statistics/ports";
   	
	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                     
                String result = response.toString();
               // System.out.println("result: "+result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray statistics = jsonResponse.getJSONArray("statistics");
                
                 for(int ind=0; ind<statistics.length(); ind++)
                {
                    
                  String device = statistics.getJSONObject(ind).getString("device");   
                 // System.out.println("device: "+device);
                   
                  JSONArray ports = statistics.getJSONObject(ind).getJSONArray("ports");
                  
                  int total_pack_received = 0; int total_pack_sent = 0;
                  int total_bytes_received = 0; int total_bytes_sent = 0;
                  
                  for(int i=0; i<ports.length(); i++)
                {
                 
                    int port_id = ports.getJSONObject(i).getInt("port"); 
                  //  System.out.println("port id: "+port_id);
                    
                    int packReceived = ports.getJSONObject(i).getInt("packetsReceived"); 
                 //   System.out.println("packetsReceived: "+packReceived);
                    total_pack_received = total_pack_received + packReceived;
                    
                    int packSent = ports.getJSONObject(i).getInt("packetsSent"); 
                 //   System.out.println("packetsSent: "+packSent);
                    total_pack_sent = total_pack_sent + packSent;
                    
                    int byReceived = ports.getJSONObject(i).getInt("bytesReceived"); 
                 //   System.out.println("bytesReceived: "+byReceived);
                    total_bytes_received = total_bytes_received + byReceived;
                    
                    int bySent= ports.getJSONObject(i).getInt("bytesSent"); 
                //    System.out.println("bytesSent: "+bySent);
                    total_bytes_sent = total_bytes_sent + bySent;
                }
                  
                  packetsReceived.put(device, String.valueOf(total_pack_received));
                  packetsSent.put(device, String.valueOf(total_pack_sent));
                  bytesReceived.put(device, String.valueOf(total_bytes_received));
                  bytesSent.put(device, String.valueOf(total_bytes_sent));
                  
                }
                
                SwitchesStatus.put(0, packetsReceived);
                SwitchesStatus.put(1, packetsSent);
                SwitchesStatus.put(2, bytesReceived);
                SwitchesStatus.put(3, bytesSent);
                
                return SwitchesStatus;
           
	}
        
     /************************************************************************************************/      
        
        /**
     * @return **********************************************************************************************/     
        
        public ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> getPathLossRate() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
            ConcurrentHashMap<String, String> packetsReceived = new ConcurrentHashMap<String, String>();
            ConcurrentHashMap<String, String> packetsSent = new ConcurrentHashMap<String, String>();
            
            
            ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> PathsStatus = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>>();
            
          //  System.out.println("getFlowStatsPerPort: - switchID - "+switchID+" - portID - "+portID);
            
            ArrayList<String> flowsIentifiers = new ArrayList<String>();

                String url = "http://localhost:8181/onos/v1/statistics/ports";
   	
	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                     
                String result = response.toString();
                //System.out.println("result: "+result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray statistics = jsonResponse.getJSONArray("statistics");
                
                 for(int ind=0; ind<statistics.length(); ind++)
                {
                    
                  String device = statistics.getJSONObject(ind).getString("device");   
                 // System.out.println("device: "+device);
                   
                  JSONArray ports = statistics.getJSONObject(ind).getJSONArray("ports");
                  
                  for(int i=0; i<ports.length(); i++)
                {
                 
                    int port_id = ports.getJSONObject(i).getInt("port"); 
                  //  System.out.println("port id: "+port_id);
                    
                    int packReceived = ports.getJSONObject(i).getInt("packetsReceived"); 
                 //   System.out.println("packetsReceived: "+packReceived);

                    int packSent = ports.getJSONObject(i).getInt("packetsSent"); 
                 //   System.out.println("packetsSent: "+packSent);

                  packetsReceived.put(device.concat("-").concat(String.valueOf(port_id)), String.valueOf(packReceived));
                  packetsSent.put(device.concat("-").concat(String.valueOf(port_id)), String.valueOf(packSent));
                 
                }  
                }
                
                PathsStatus.put(0, packetsReceived);
                PathsStatus.put(1, packetsSent);
                
                
                return PathsStatus;
           
	}
        
     /************************************************************************************************/   
        
    /************************************************************************************************/
    /**
     * @param key*
     * @throws java.lang.Exception*********************************************************************************************/
        
        
        
         public void deleteIntent(String key) throws Exception {
             
               DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
               writeFile();
                
               // System.out.println("Delete intent - key: "+key);
               
           //     System.out.println("DSE.DSE.currentIntents.size()-before delete: "+DSE.DSE.currentIntents.size());
                   
                  Iterator iter = DSE.DSE.currentIntents.keySet().iterator();
                while (iter.hasNext())
                    {
            
                    String mykey = iter.next().toString();
            //        System.out.println("mykey "+mykey);
                    String value = "";
                    value = DSE.DSE.currentIntents.get(mykey);
             //       System.out.println("value "+value);
                          
                if((key.equals(mykey)) && (!(value.equals(""))))
                    
                {
                    
                String intentID = DSE.DSE.currentIntents.get(key);
                
                DSE.DSE.currentIntents.remove(key);
                
             //   System.out.println("DSE.DSE.currentIntents.size()-after delete: "+DSE.DSE.currentIntents.size());
                
                
             //   System.out.println("intentID to delete: "+intentID);
                
                String myurl = "http://localhost:8181/onos/v1/intents/org.onosproject.net.intent/";
	
                String url = myurl.concat(intentID).concat("?api_key=delete");
                
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");

	con.setRequestMethod("DELETE");
                
                con.connect();

                StringBuffer response;
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                            String inputLine;
                            response = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }               }

	//print result
	//System.out.println(response.toString());
                
                }
                
                        }
                
	}
        
     /************************************************************************************************/
    /************************************************************************************************/
           public void deleteIntentbyID(String intentID) throws Exception {
             
               DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
               writeFile();
                
                System.out.println("remove intent by ID"+intentID);
               
           //    System.out.println("DSE.DSE.currentIntents.size() "+DSE.DSE.currentIntents.size());
               
               /*    Iterator iter = DSE.DSE.currentIntents.keySet().iterator();
                while (iter.hasNext())
                    {
            
                    String key = iter.next().toString();
                //    System.out.println("key "+key);
                    String value = DSE.DSE.currentIntents.get(key);
                //    System.out.println("value "+value);
                   
            if (value.equals(intentID)) {
                
                DSE.DSE.currentIntents.remove(key);
            }
               
               }*/  
               
                
             //   System.out.println("intentID to delete: "+intentID);
                
                String myurl = "http://localhost:8181/onos/v1/intents/org.onosproject.net.intent/";
	
                String url = myurl.concat(intentID).concat("?api_key=delete");
                
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");

	con.setRequestMethod("DELETE");
                
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//print result
	//System.out.println(response.toString());
                
                      //  }
                
	}
    /************************************************************************************************/
    /************************************************************************************************/
        
         public void deleteFlow(String flowID, String switchID) throws Exception {
             
               DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
               writeFile();
                
              //  System.out.println("Delete flow - ID: "+flowID +" on switch: "+switchID);
                
        String swUrl = "";
                
        if(switchID.contains("of"))
            {  
        swUrl = switchID.replaceAll(":", "%3A");
            }
        else
           {
           switch (Integer.parseInt(switchID))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    swUrl = "of%3A000000000000000".concat(switchID);
                    break;
                case 10:
                    swUrl = "of%3A000000000000000a";
                    break;
                case 11:
                    swUrl = "of%3A000000000000000b";
                case 12:
                    swUrl = "of%3A000000000000000c";
                    break;
                case 13:
                    swUrl = "of%3A000000000000000d";
                case 14:
                    swUrl = "of%3A000000000000000e";
                    break;
            }       
           }
        
        ArrayList<Object> flowentries = getFlowStats(Long.valueOf(switchID));
        
        if(flowentries.contains(flowID))
        {
                
                String myurl = "http://localhost:8181/onos/v1/flows/";
                
                String url = myurl.concat(swUrl).concat("/").concat(flowID);
                
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");

	con.setRequestMethod("DELETE");
                
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//print result
	//System.out.println(response.toString());
                
        }
        
        else
        {
        System.out.println("flow ID already deleted");
        }
                
	}
        
     /************************************************************************************************/
    /************************************************************************************************/     
         
         public int getFlowBytes(String flowID) throws Exception
         {
         
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
            
         int numBytes = 0;
         
          String url = "http://127.0.0.1:8181/onos/v1/flows";
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                
                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                Set<Long> switchDpids = new HashSet<Long>();
                
                String result = response.toString();
               // System.out.println("list of all active flows");
               // System.out.println(result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray flows = jsonResponse.getJSONArray("flows");
                String flow = "";
                int bytes = 0;
                
                for(int ind=0; ind<flows.length(); ind++)
                {
                flow = flows.getJSONObject(ind).getString("id");
               // System.out.println("flow ID: "+flow);
                bytes = (flows.getJSONObject(ind).getInt("bytes"));
               //System.out.println("bytes: "+bytes);
                
                if(flow.equals(flowID))
                {
                numBytes = bytes;
                }
                
                }
   
                         
         
         return numBytes;
         }
         
    /***************************************************************************************************/     
    /**************************************************************************************************/
         
     /************************************************************************************************/     
        
        public ArrayList<String> getFlowStatsPerPort(Long switchID, int portID) throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
          //  System.out.println("getFlowStatsPerPort: - switchID - "+switchID+" - portID - "+portID);
            
            ArrayList<String> flowsIentifiers = new ArrayList<String>();

                String myurl = "http://localhost:8181/onos/v1/flows/";
                
                String swId = DecHex(switchID);
                
                String url = myurl.concat("of%3A000000000000000").concat(swId);
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                ArrayList<Object> flowEntries = new ArrayList<>();
                                
                String result = response.toString();
              //  System.out.println("result: "+result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray flows = jsonResponse.getJSONArray("flows");
                
                 for(int ind=0; ind<flows.length(); ind++)
                {
                    
                 String appId = flows.getJSONObject(ind).getString("appId");   
                 if((appId.equals("org.onosproject.net.intent")))// || (appId.equals("org.onosproject.core")))
                 {flowEntries.add(flows.get(ind).toString());}
                }
                 
                  for(Object d:flowEntries) {
                      
                  String input = d.toString();
               //   System.out.println("flow entry: ");
               //   System.out.println(input);
                  
                  Pattern p = Pattern.compile("\\\"id\\\":\\\"(.*?)\\\"");
                  Matcher m = p.matcher(input);
               //   System.out.println("ID of the installed flow");
                  String flowID = "";
                  while(m.find())
                  {
                    flowID = m.group(1); 
                 //   System.out.println(flowID);
                   }
                  
                  //Pattern p1 = Pattern.compile("\"port\":\"(.*?)\",\"type\":\"OUTPUT\"");
                 // Matcher m1 = p1.matcher(input);
                  
               //   String flowPort = "";
              //    while(m1.find())
              //    {
              //      flowPort = m1.group(1); 
              //     }
                  
            //      System.out.println("flowPort: "+flowPort);
                  
            //      if(flowPort.equals(String.valueOf(portID)))
             //     {
            //      System.out.println("Port of the installed flow");
            //      System.out.println(flowPort);
                  flowsIentifiers.add(flowID);
                  
        FileWriter fwMsg = new FileWriter("results/flowsIdentifiers.txt", true);
        BufferedWriter restMsg = new BufferedWriter(fwMsg);
        restMsg.append(flowID);
        restMsg.newLine();
        restMsg.flush();
            //      }
                  
                  int numbytes = getFlowBytes(flowID);
                //  System.out.println("http numBytes: "+numbytes);
                }
                  
             //     System.out.println("we are in http - flowsIentifiers.size()"+flowsIentifiers.size());
                
                return flowsIentifiers;
           
	}
        
     /************************************************************************************************/    
         
     /************************************************************************************************/
    /************************************************************************************************/     
        
        public ArrayList<Object> getFlowStats(Long switchID) throws Exception {
            
            //System.out.println("getFlowStats of switch: "+switchID);
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();

                String myurl = "http://localhost:8181/onos/v1/flows/";
                
                String swId = DecHex(switchID);
                
                String url = myurl.concat("of%3A000000000000000").concat(swId);
                
            //    System.out.println("url: "+url);
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                ArrayList<Object> flowEntries = new ArrayList<>();
                                
                String result = response.toString();
               // System.out.println("result: "+result);
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray flows = jsonResponse.getJSONArray("flows");
                
                 for(int ind=0; ind<flows.length(); ind++)
                {
                    
                 String appId = flows.getJSONObject(ind).getString("appId");   
                 if(appId.equals("org.onosproject.net.intent"))
                 {
                     flowEntries.add(flows.get(ind).toString());
                 }
                }
                 
                  for(Object d:flowEntries) {
                  String input = d.toString();    
                 // System.out.println(input);
                  
                  Pattern p = Pattern.compile("\\\"id\\\":\\\"(.*?)\\\"");
                  Matcher m = p.matcher(input);
                 // System.out.println("ID of the installed flow");
                  String flowID = "";
                  while(m.find())
                  {
                    flowID = m.group(1); 
                    //System.out.println("flowID "+flowID);
                   }
                  
                  int numbytes = getFlowBytes(flowID);
                  //System.out.println("numBytes: "+numbytes);
                }
                
                return flowEntries;
           
	}
        
     /************************************************************************************************/
         /************************************************************************************************/
       
         public String PointToPointIntent_VLAN(String identifier, String SourceSw, String DestinationSw, String Source, String Destination, String srcMAC, String dstMAC, String VLAN_ID, String srcPort, String dstPort) throws MalformedURLException, ProtocolException, IOException, JSONException, Exception {
             
        DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
        writeFile();
        
       // System.out.println("pointtopoint intent");
      //  System.out.println("sw source/dest "+SourceSw+" / "+DestinationSw);
      //  System.out.println("port source/dest "+srcPort+" / "+dstPort);
        
        String installedIntent = "";
               
        String key = identifier.concat("-").concat(SourceSw).concat("-").concat(srcPort).concat("-").concat(dstPort).concat("-").concat(Source).concat("-").concat(Destination).concat("-").concat(VLAN_ID);
                
      //  System.out.println("key: "+key);
        
       String Fl_identifier = identifier.concat("-").concat(DestinationSw).concat("-").concat(dstPort);
       
     //  System.out.println("Fl_identifier in http: "+Fl_identifier);
        
       // if(DSE.DSE.currentIntents.containsKey(key))
      //  {
      //  System.out.println("Intent already installed");
      //  }
          
        
    //    else
        
      //  {
        //  System.out.println("we are setting Point to POint intent");
          
     
          ArrayList<String> previous_flows = getFlowStatsPerPort(Long.parseLong(DestinationSw), Integer.parseInt(dstPort));
          
        /*   for(int ind=0; ind<previous_flows.size(); ind++)
          {
          System.out.println("flow> "+previous_flows.get(ind));
          }
           
          System.out.println("number of flows on this switch before installing intent: "+previous_flows.size());*/
          
       
        String url="http://127.0.0.1:8181/onos/v1/intents";
        String username="onos";
        String password="rocks";
        String dest = ""; String src = "";
        
     //   System.out.println("DestinationSw: "+DestinationSw);
     //   System.out.println("SourceSw: "+SourceSw);
        
        if (!((SourceSw.contains("of")) && (DestinationSw.contains("of"))))
         {  
            switch (Integer.parseInt(DestinationSw))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    dest = "of:000000000000000".concat(DestinationSw);
                    break;
                case 10:
                    dest = "of:000000000000000a";
                    break;
                case 11:
                    dest = "of:000000000000000b";
                    break;
                case 12:
                    dest = "of:000000000000000c";
                    break;
                case 13:
                    dest = "of:000000000000000d";
                    break;
                case 14:
                    dest = "of:000000000000000e";
                    break;
            }
            switch (Integer.parseInt(SourceSw))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    src = "of:000000000000000".concat(SourceSw);
                    break;
                case 10:
                    src = "of:000000000000000a";
                    break;
                case 11:
                    src = "of:000000000000000b";
                    break;
                case 12:
                    src = "of:000000000000000c";
                    break;
                case 13:
                    src = "of:000000000000000d";
                    break;
                case 14:
                    src = "of:000000000000000e";
                    break;

            }    
       
           }
        
        
        JSONObject user=new JSONObject();
        user.put("type", "PointToPointIntent");
        user.put("appId", "org.onosproject.net.intent");
         user.put("priority", "55000");
        //user.put("priority", VLAN_ID);
        
        JSONObject criteria = new JSONObject();
        
        JSONArray crita = new JSONArray();
        
        JSONObject crit2 = new JSONObject();
        crit2.put("type", "ETH_DST");
        crit2.put("mac", dstMAC);
        
     //   JSONObject crit2 = new JSONObject();
     //   crit2.put("type", "ETH_TYPE");
     //   crit2.put("ethType", "0x806");
        
    //    JSONObject crit1 = new JSONObject();
   //     crit1.put("type", "VLAN_VID");
   //     crit1.put("vlanId", VLAN_ID);

        crita.put(0, crit2);
   //     crita.put(1, crit1);
        
        criteria.put("criteria", crita);
        
        user.put("selector", criteria);
        
        JSONObject instructions = new JSONObject();
        
        JSONArray instructa = new JSONArray();
        
        JSONObject inst1 = new JSONObject();
      inst1.put("type", "L2MODIFICATION");
        inst1.put("subtype", "ETH_SRC");
        inst1.put("mac", srcMAC);

        instructa.put(0, inst1);
        
        instructions.put("instructions", instructa);
        
        user.put("treatment", instructions);
        
        JSONArray constraint=new JSONArray();
        
        JSONObject first=new JSONObject();
        JSONObject second=new JSONObject();
        JSONObject third=new JSONObject();
                
        first.put("inclusive", false);
        first.put("type", "LinkTypeConstraint");
        JSONArray optical = new JSONArray();
        optical.put(0, "OPTICAL");
        first.put("types", optical);
        
        second.put("port", srcPort);
        second.put("device", src);
             
      //  System.out.println("rest-src: "+src);
               
        third.put("port", dstPort);
        third.put("device", dest);
        
     //   System.out.println("rest-dest: "+dest);
        
        constraint.put(0, first);
        
        user.put("constraints", constraint);
        user.put("ingressPoint", second);
        user.put("egressPoint", third);
        
        String jsonData=user.toString();
        
      //  System.out.println("jsonData");
     //   System.out.println(jsonData);
        
          
       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       
       String userpass = "onos" + ":" + "rocks";
       String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
       con.setRequestProperty ("Authorization", basicAuth);

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                 con.setRequestProperty("Content-Type", "application/json");
                
                        
	String urlParameters = jsonData;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
	// Send post request
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	int responseCode = con.getResponseCode();

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//System.out.println(response.toString());
                
                Thread.sleep(50);
                
                ArrayList<String> IntentID = getIntents();
                
                String lastIntent = "";
                
                Iterator iter = IntentID.iterator();
                 while (iter.hasNext())
                     {
                    // System.out.println((String)iter.next());
                         String value = (String)iter.next();
                         if(!(DSE.DSE.currentIntents.containsValue(value)))
                    
                                 {lastIntent = value;}
                     }
                
                
             //   System.out.println("last intent: "+lastIntent);
           
                DSE.DSE.currentIntents.put(key, lastIntent);
                
                installedIntent = lastIntent;
                
              //  System.out.println("DestinationSw: "+DestinationSw);
                
                
          ArrayList<String> current_flows = getFlowStatsPerPort(Long.parseLong(DestinationSw), Integer.parseInt(dstPort)); 
          
         /* for(int ind=0; ind<current_flows.size(); ind++)
          {
          System.out.println("flow> "+current_flows.get(ind));
          }*/
          
          
        //  System.out.println("number of flows on this switch after installing intent VLAN: "+current_flows.size());
          
          
          if(current_flows.size() > 0)
          	{

          DSE.BNServer.intent_gives_flow.put(lastIntent, current_flows.get(current_flows.size() - 1));
          
          DSE.BNServer.intent_corresponding_flow.put(Fl_identifier, current_flows.get(current_flows.size() - 1));
                  }
          
          
            
        
        return installedIntent;
              
         }    
           
    /************************************************************************************************/
    /************************************************************************************************/    

	public Set<Long> getSwitchDpids() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();

                String url = "http://127.0.0.1:8181/onos/v1/devices";
                
              //  System.out.println("print getSwitchDpids()");
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
                con.setRequestMethod("GET");
                             
                con.connect();

	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
                
                Set<Long> switchDpids = new HashSet<Long>();
                
                String result = response.toString();
        
                JSONObject jsonResponse = new JSONObject(result);
               
                JSONArray devices = jsonResponse.getJSONArray("devices");
                String device = "";
                
                for(int ind=0; ind<devices.length(); ind++)
                {
                device = devices.getJSONObject(ind).getString("id");
                //System.out.println("device: "+device);
                if((device.contains("of:00000000"))) //&& (!(device.contains("1"))))
                switchDpids.add(hexDec(device));
                }
   
                System.out.println("devices size(): "+switchDpids.size());
                
                return switchDpids;
           
                
	}
        
     /************************************************************************************************/
    /************************************************************************************************
        
         public void addFlow(String Switch, String srcPort, String dstPort, String Source, String Destination) throws MalformedURLException, ProtocolException, IOException, JSONException {
             
             DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
             writeFile();
             
             System.out.println("we are adding a flow");          
             
             String sw = "";
             switch (Integer.parseInt(Switch))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    sw = "000000000000000".concat(Switch);
                    break;
                case 10:
                    sw = "000000000000000a";
                    break;
                case 11:
                    sw = "000000000000000b";
                    break;
            }     
        
        String url="http://localhost:8181/onos/v1/flows/of%3A".concat(sw);
        String username="onos";
        String password="rocks";
        
        JSONObject user=new JSONObject();
        user.put("isPermanent", true);
        user.put("appId", "org.onosproject.cli");
        user.put("priority", "55");
        user.put("deviceId", "of:".concat(sw));
        
        JSONObject treatment=new JSONObject();
        JSONArray instructions =new JSONArray();
        JSONObject first=new JSONObject();
        first.put("type", "OUTPUT");
        first.put("port", dstPort);

        
        instructions.put(0, first);
        
        treatment.put("instructions", instructions);
        
        user.put("treatment", treatment);
       
        
        String jsonData=user.toString();
        
        System.out.println(jsonData);
        
                  
       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       
       String userpass = "onos" + ":" + "rocks";
       String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
       con.setRequestProperty ("Authorization", basicAuth);

	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
                 con.setRequestProperty("Content-Type", "application/json");
                
                        
	String urlParameters = jsonData;
	
	
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	int responseCode = con.getResponseCode();
	
	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
	
                
                System.out.println("result: "+response.toString());
             
         }    
    
     /************************************************************************************************/
    /************************************************************************************************/    
        
      //public String sendPointToPointIntent(String direction, String SourceSw, String DestinationSw, String Source, String Destination, String srcMAC, String dstMAC, String srcPort, String dstPort) throws MalformedURLException, ProtocolException, IOException, JSONException, Exception {
       public String sendPointToPointIntent(String identifier, String SourceSw, String DestinationSw, String Source, String Destination, String srcMAC, String dstMAC, String srcPort, String dstPort) throws MalformedURLException, ProtocolException, IOException, JSONException, Exception {
             
        DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
        writeFile();
        
        String installedIntent = "";
               
        String key = identifier.concat("-").concat(SourceSw).concat("-").concat(srcPort).concat("-").concat(dstPort).concat("-").concat(Source).concat("-").concat(Destination);
                
      //  System.out.println("key: "+key);
        
       String Fl_identifier = identifier.concat("-").concat(SourceSw).concat("-").concat(dstPort);
       
       
        
       // if(DSE.DSE.currentIntents.containsKey(key))
      //  {
      //  System.out.println("Intent already installed");
      //  }
          
        
    //    else
        
      //  {
        //  System.out.println("we are setting Point to POint intent");
          
     
          ArrayList<String> previous_flows = getFlowStatsPerPort(Long.parseLong(DestinationSw), Integer.parseInt(dstPort));    
          System.out.println("number of flows on this switch before installing intent: "+previous_flows.size());
          
       
        String url="http://127.0.0.1:8181/onos/v1/intents";
        String username="onos";
        String password="rocks";
        String dest = ""; String src = "";
        
     //   System.out.println("DestinationSw: "+DestinationSw);
      //  System.out.println("SourceSw: "+SourceSw);
        
        if (!((SourceSw.contains("of")) && (DestinationSw.contains("of"))))
         {  
            switch (Integer.parseInt(DestinationSw))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    dest = "of:000000000000000".concat(DestinationSw);
                    break;
                case 10:
                    dest = "of:000000000000000a";
                    break;
                case 11:
                    dest = "of:000000000000000b";
                    break;
                case 12:
                    dest = "of:000000000000000c";
                    break;
                case 13:
                    dest = "of:000000000000000d";
                    break;
                case 14:
                    dest = "of:000000000000000e";
                    break;
            }
            switch (Integer.parseInt(SourceSw))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    src = "of:000000000000000".concat(SourceSw);
                    break;
                case 10:
                    src = "of:000000000000000a";
                    break;
                case 11:
                    src = "of:000000000000000b";
                    break;
                case 12:
                    src = "of:000000000000000c";
                    break;
                case 13:
                    src = "of:000000000000000d";
                    break;
                case 14:
                    src = "of:000000000000000e";
                    break;
            }    
       
           }
        
        
        JSONObject user=new JSONObject();
        user.put("type", "PointToPointIntent");
        user.put("appId", "org.onosproject.net.intent");
        user.put("priority", "55");
        
        JSONObject criteria = new JSONObject();
        
        JSONArray crita = new JSONArray();
        
        JSONObject crit1 = new JSONObject();
        crit1.put("type", "ETH_DST");
        crit1.put("mac", dstMAC);

        crita.put(0, crit1);
        
        criteria.put("criteria", crita);
        
        user.put("selector", criteria);
        
      
        JSONObject instructions = new JSONObject();
        
        JSONArray instructa = new JSONArray();
        
        JSONObject inst1 = new JSONObject();
      inst1.put("type", "L2MODIFICATION");
        inst1.put("subtype", "ETH_SRC");
        inst1.put("mac", srcMAC);

        instructa.put(0, inst1);
        
        instructions.put("instructions", instructa);
        
        user.put("treatment", instructions);
        
        
        JSONArray constraint=new JSONArray();
        
        JSONObject first=new JSONObject();
        JSONObject second=new JSONObject();
        JSONObject third=new JSONObject();
                
        first.put("inclusive", false);
        first.put("type", "LinkTypeConstraint");
        JSONArray optical = new JSONArray();
        optical.put(0, "OPTICAL");
        first.put("types", optical);
        
        second.put("port", srcPort);
        second.put("device", src);
             
     //   System.out.println("rest-src: "+src);
               
        third.put("port", dstPort);
        third.put("device", dest);
        
      //  System.out.println("rest-dest: "+dest);
        
        constraint.put(0, first);
        
        user.put("constraints", constraint);
        user.put("ingressPoint", second);
        user.put("egressPoint", third);
        
        String jsonData=user.toString();
        
    //    System.out.println("jsonData");
     //   System.out.println(jsonData);
        
          
       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       
       String userpass = "onos" + ":" + "rocks";
       String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
       con.setRequestProperty ("Authorization", basicAuth);

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                 con.setRequestProperty("Content-Type", "application/json");
                
                        
	String urlParameters = jsonData;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
	// Send post request
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	int responseCode = con.getResponseCode();

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//System.out.println(response.toString());
                
                Thread.sleep(50);
                
                ArrayList<String> IntentID = getIntents();
                
                String lastIntent = "";
                
                Iterator iter = IntentID.iterator();
                 while (iter.hasNext())
                     {
                    // System.out.println((String)iter.next());
                         String value = (String)iter.next();
                         if(!(DSE.DSE.currentIntents.containsValue(value)))
                    
                                 {lastIntent = value;}
                     }
                
                
           //     System.out.println("last intent: "+lastIntent);
           
                DSE.DSE.currentIntents.put(key, lastIntent);
                
                installedIntent = lastIntent;
                
                
          ArrayList<String> current_flows = getFlowStatsPerPort(Long.parseLong(DestinationSw), Integer.parseInt(dstPort));    
        //  System.out.println("number of flows on this switch after installing intent: "+current_flows.size());
          
          DSE.BNServer.intent_corresponding_flow.put(Fl_identifier, current_flows.get(current_flows.size() - 1));
      //  }
        
        return installedIntent;
              
         }    
           
    /************************************************************************************************/
    /************************************************************************************************/      

	private ArrayList<String> getIntents() throws Exception {

            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
           // System.out.println("getIntents: ");
	
                String url = "http://127.0.0.1:8181/onos/v1/intents";
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                
                ArrayList<String> IntentID = new ArrayList<String>();

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
             
	con.setRequestMethod("GET");
            
                
                con.connect();
                
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//print result
	System.out.println(response.toString());
                
                JSONObject jsonResponse = new JSONObject(response.toString());
               
                JSONArray intents = jsonResponse.getJSONArray("intents");
                
                 for(int ind=0; ind<intents.length(); ind++)
                {
                    
                 String Id = intents.getJSONObject(ind).getString("id");  
                // System.out.println(Id);
                 IntentID.add(Id);
                }
                
                return IntentID;
           
                
	}
    
    /************************************************************************************************/
        
        public void getAllFlows() throws Exception {

            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
	
                String url = "http://127.0.0.1:8181/onos/v1/flows";
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                
                ArrayList<String> IntentID = new ArrayList<String>();

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
             
	con.setRequestMethod("GET");
            
                
                con.connect();
                
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//print result
	//System.out.println(response.toString());
                
                JSONObject jsonResponse = new JSONObject(response.toString());
               
                JSONArray flows = jsonResponse.getJSONArray("flows");
                
                 for(int ind=0; ind<flows.length(); ind++)
                {
                    
                 String Id = flows.getJSONObject(ind).getString("id");  
                 
                 String appId = flows.getJSONObject(ind).getString("appId");   
                 if((appId.equals("org.onosproject.net.intent")))
                 {
                int bytes = flows.getJSONObject(ind).getInt("bytes");
                
                FileWriter fwMsg = new FileWriter("results/flows.txt", true);
        BufferedWriter restMsg = new BufferedWriter(fwMsg);
        restMsg.append(Id.concat(": "));restMsg.append(String.valueOf(bytes));
        restMsg.newLine();
        restMsg.flush();
                 
                 }
                 
                }
                
                 
           FileWriter fwMsg = new FileWriter("results/flows.txt", true);
        BufferedWriter restMsg = new BufferedWriter(fwMsg);
        restMsg.append("New iteration");
        restMsg.newLine();
        restMsg.flush();
                          
                
	}
    /************************************************************************************************/
    
        public void sendHostToHostIntent(String Source, String Destination) throws MalformedURLException, ProtocolException, IOException, JSONException {
            
        DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
        writeFile();

        String url="http://127.0.0.1:8181/onos/v1/intents";
        String username="onos";
        String password="rocks";
        
        JSONObject user=new JSONObject();
        user.put("type", "HostToHostIntent");
        user.put("appId", "org.onosproject.cli");
        //user.put("one", "00:00:00:00:00:01/None");
        //user.put("two", "00:00:00:00:00:05/None");
        user.put("one", Source);//Mac address
        user.put("two", Destination);//Mac address
        user.put("priority", "55");
    
        String jsonData=user.toString();
        
       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       
       String userpass = "onos" + ":" + "rocks";
       String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
       con.setRequestProperty ("Authorization", basicAuth);

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                 con.setRequestProperty("Content-Type", "application/json");
                
                        
	String urlParameters = jsonData;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
	// Send post request
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	int responseCode = con.getResponseCode();
	//System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
	//System.out.println("Response Code : " + responseCode);

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
	
	//print result
	//System.out.println(response.toString());
         }    

        
    /************************************************************************************************/
    /************************************************************************************************/
        
       // public Route getRoute(String Source, String srcPort, String Destination, String dstPort) throws MalformedURLException, ProtocolException, IOException {
            
    public ArrayList<Route> getRoute(String Source, String srcPort, String Destination, String dstPort) throws MalformedURLException, ProtocolException, IOException {
        
          ArrayList<Route> Routes = new ArrayList<>();  
            
    //    System.out.println("getRoute: source: "+Source+" - srcPort: "+srcPort+" - destination: "+Destination+" - dstPort: "+dstPort);
         
         DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
         writeFile();

        Route returned_route = null;
    //    System.out.println("G Source: "+Source);
     //   System.out.println("G Destination: "+Destination);
        String srcUrl = "";
        String dstUrl = "";
        
        if((Source.contains("of")) && (Destination.contains("of")))
            {  
       //    System.out.println("we are in if");
        srcUrl = Source.replaceAll(":", "%3A");
        dstUrl = Destination.replaceAll(":", "%3A");
            }
        else
           {
             //  System.out.println("we are in else");
           switch (Integer.parseInt(Destination))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    dstUrl = "of%3A000000000000000".concat(Destination);
                    break;
                case 10:
                    dstUrl = "of%3A000000000000000a";
                    break;
                case 11:
                    dstUrl = "of%3A000000000000000b";
                    break;
                case 12:
                    dstUrl = "of%3A000000000000000c";
                    break;
                case 13:
                    dstUrl = "of%3A000000000000000d";
                    break;
                case 14:
                    dstUrl = "of%3A000000000000000e";
                    break;
            }
            String oldsrc="";String newsrcString="";
            switch (Integer.parseInt(Source))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    srcUrl = "of%3A000000000000000".concat(Source);
                    break;
                case 10:
                    srcUrl = "of%3A000000000000000a";
                    break;
                case 11:
                    srcUrl = "of%3A000000000000000b";
                    break;
                case 12:
                    srcUrl = "of%3A000000000000000c";
                    break;
                case 13:
                    srcUrl = "of%3A000000000000000d";
                    break;
                case 14:
                    srcUrl = "of%3A000000000000000e";
                    break;
            }    
       
           }
        String simpleUrl = "http://localhost:8181/onos/v1/paths/";
        String url=simpleUrl.concat(srcUrl).concat("/").concat(dstUrl);
        String username="onos";
        String password="rocks";
        
         System.out.println("url: "+url);
        //System.out.println("Destination: "+Destination);
        
         JSONParser parser = new JSONParser();
 
        try {
 
            String modif = parser.parse(new FileReader("getPaths.txt")).toString();
            
        List<NodePortTuple> switchPorts = new ArrayList<NodePortTuple>();
        
        int cost = 0;
            
            if(Source.equals(Destination))
            {
            // List<NodePortTuple> switchPorts = new ArrayList<NodePortTuple>() ;
          
        
        
        String mysrc="";String mydst="";
   
            if(Source.contains("of"))
            {            
         mysrc = Source.substring(3);
        
        if(mysrc.equals("000000000000000a"))
        {mysrc="10";}
        else if(mysrc.equals("000000000000000b"))
        {mysrc="11";}
        else if(mysrc.equals("000000000000000c"))
        {mysrc="12";}
        else if(mysrc.equals("000000000000000d"))
        {mysrc="13";}
        else if(mysrc.equals("000000000000000e"))
        {mysrc="14";}

            }
            else
            {
                mysrc = Source;
                
            }
        
        NodePortTuple node = new NodePortTuple(Long.parseLong(mysrc), Short.valueOf(srcPort));
        switchPorts.add(node);
        
             if(Destination.contains("of"))
            {            
         mydst = Destination.substring(3);
        
        if(mydst.equals("000000000000000a"))
        {mydst="10";}
        else if(mydst.equals("000000000000000b"))
        {mydst="11";}
         else if(mydst.equals("000000000000000c"))
        {mydst="12";}
         else if(mydst.equals("000000000000000d"))
        {mydst="13";}
         else if(mydst.equals("000000000000000e"))
        {mydst="14";}

            }
            else
            {
                mydst = Destination;
                
            }
        
        NodePortTuple nodeN = new NodePortTuple(Long.parseLong(mydst), Short.valueOf(dstPort));
        switchPorts.add(nodeN);
        
        
            }//if Source equals Destination
            else
            {
            
       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       
       String userpass = "onos" + ":" + "rocks";
       String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
       con.setRequestProperty ("Authorization", basicAuth);

	//add reuqest header
	con.setRequestMethod("GET");
	con.setRequestProperty("User-Agent", USER_AGENT);
	//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
               //  con.setRequestProperty("Content-Type", "application/json");
           
                      
	//String urlParameters = jsonData;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
                 con.connect();
                 
                 
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//print result
        System.out.println("Receive response from ONOS: ");
 	System.out.println(response.toString());
          
                  
        String result = response.toString();
        JSONObject jsonResponse = new JSONObject(result);
               
        JSONArray paths = jsonResponse.getJSONArray("paths");
   
        cost = paths.getJSONObject(0).getInt("cost");
        
        JSONArray links = paths.getJSONObject(0).getJSONArray("links");
        
        String mysrc="";String mydst="";
   
            if(Source.contains("of"))
            {            
         mysrc = Source.substring(3);
        
        if(mysrc.equals("000000000000000a"))
        {mysrc="10";}
        else if(mysrc.equals("000000000000000b"))
        {mysrc="11";}
        else if(mysrc.equals("000000000000000c"))
        {mysrc="12";}
        else if(mysrc.equals("000000000000000d"))
        {mysrc="13";}
        else if(mysrc.equals("000000000000000e"))
        {mysrc="14";}

            }
            else
            {
                mysrc = Source;
                
            }
        
        
        NodePortTuple node = new NodePortTuple(Long.parseLong(mysrc), Short.valueOf(srcPort));
        switchPorts.add(node);
        
        for(int i=0; i<links.length(); i++)
        {
        
        JSONObject source = links.getJSONObject(i).getJSONObject("src");
        
     //   System.out.println("source");
        String port = source.getString("port");
     //   System.out.println("port: "+port);
        String device = source.getString("device");
    //    System.out.println("device: "+device);
        device = device.substring(3);
        
        if(device.equals("000000000000000a"))
        {device="10";}
        else if(device.equals("000000000000000b"))
        {device="11";}
        else if(device.equals("000000000000000c"))
        {device="12";}
        else if(device.equals("000000000000000d"))
        {device="13";}
        else if(device.equals("000000000000000e"))
        {device="14";}
        
        NodePortTuple node1 = new NodePortTuple(Long.parseLong(device), Short.valueOf(port));
        switchPorts.add(node1);
        
        JSONObject destination = links.getJSONObject(i).getJSONObject("dst");
        
    //    System.out.println("destination");
        String dport = destination.getString("port");
    //  System.out.println("port: "+dport);
        String ddevice = destination.getString("device");
    //    System.out.println("device: "+ddevice);
        ddevice = ddevice.substring(3);
        
        
        if(ddevice.contains("000000000"))
        {
        if(ddevice.equals("000000000000000a"))
        {ddevice="10";}
        else if(ddevice.equals("000000000000000b"))
        {ddevice="11";}
        else if(ddevice.equals("000000000000000c"))
        {ddevice="12";}
        else if(ddevice.equals("000000000000000d"))
        {ddevice="13";}
        else if(ddevice.equals("000000000000000e"))
        {ddevice="14";}
        
        
        
        NodePortTuple node2 = new NodePortTuple(Long.parseLong(ddevice), Short.valueOf(dport));
        switchPorts.add(node2);
        
        }
        
        }
        
            if(Destination.contains("of"))
            {            
         mydst = Destination.substring(3);
        
        if(mydst.equals("000000000000000a"))
        {mydst="10";}
        else if(mydst.equals("000000000000000b"))
        {mydst="11";}
       else if(mydst.equals("000000000000000c"))
        {mydst="12";}
        else if(mydst.equals("000000000000000d"))
        {mydst="13";}
        else if(mydst.equals("000000000000000e"))
        {mydst="14";}
            }
            else
            {
                mydst = Destination;
                
            }
        
        NodePortTuple nodeN = new NodePortTuple(Long.parseLong(mydst), Short.valueOf(dstPort));
        switchPorts.add(nodeN);
        
        
        }//else Source different from Destination
        
     //   System.out.println("cost: "+cost);
        if(Source.contains("of"))
        {
        Source=Source.substring(3);
        Destination=Destination.substring(3);
        }
        
   //     System.out.println("Source: "+Source);
    //   System.out.println("Destination: "+Destination);
        
         if(Source.equals("000000000000000a"))
        {Source="10";}
        else if(Source.equals("000000000000000b"))
        {Source="11";}
          else if(Source.equals("000000000000000c"))
        {Source="12";}
          else if(Source.equals("000000000000000d"))
        {Source="13";}
          else if(Source.equals("000000000000000e"))
        {Source="14";}
         
          if(Destination.equals("000000000000000a"))
        {Destination="10";}
        else if(Destination.equals("000000000000000b"))
        {Destination="11";}
        else if(Destination.equals("000000000000000c"))
        {Destination="12";}
        else if(Destination.equals("000000000000000d"))
        {Destination="13";}
        else if(Destination.equals("000000000000000e"))
        {Destination="14";}
                        
        RouteId myrouteid = new RouteId(Long.parseLong(Source), Long.parseLong(Destination), 0);
        //myrouteid.setSrc(Long.parseLong(Source.substring(3)));
       // myrouteid.setDst(Long.parseLong(Destination.substring(3)));
       // myrouteid.setCookie(0);
        
        returned_route = new Route(myrouteid, switchPorts);
        
       // returned_route.setId(myrouteid);
      
        int routeCount = Integer.valueOf(cost);
        
       // returned_route.setPath(switchPorts);
        returned_route.setRouteCount(routeCount);
        
                         } catch (Exception e) {
            e.printStackTrace();
           // System.exit(1);
        }
          
        List<NodePortTuple> switchPortList = returned_route.getPath();

        System.out.println("Route found: " + switchPortList.toString());
        
        Routes.add(returned_route);
        
        return Routes;
        
       // return returned_route;
        
         }    

    /************************************************************************************************/
            /************************************************************************************************/
        
     public ArrayList<Route> getDisjointRoutes(String Source, String srcPort, String Destination, String dstPort) throws MalformedURLException, ProtocolException, IOException, JSONException {
            
            
    //    System.out.println("getRoute: source: "+Source+" - srcPort: "+srcPort+" - destination: "+Destination+" - dstPort: "+dstPort);
            
            ArrayList<Route> disjointRoutes = new ArrayList();
         
         DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
         writeFile();

        Route returned_route = null;
    //    System.out.println("G Source: "+Source);
     //   System.out.println("G Destination: "+Destination);
        String srcUrl = "";
        String dstUrl = "";
        
        if((Source.contains("of")) && (Destination.contains("of")))
            {  
       //    System.out.println("we are in if");
        srcUrl = Source.replaceAll(":", "%3A");
        dstUrl = Destination.replaceAll(":", "%3A");
            }
        else
           {
             //  System.out.println("we are in else");
           switch (Integer.parseInt(Destination))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    dstUrl = "of%3A000000000000000".concat(Destination);
                    break;
                case 10:
                    dstUrl = "of%3A000000000000000a";
                    break;
                case 11:
                    dstUrl = "of%3A000000000000000b";
                    break;
                case 12:
                    dstUrl = "of%3A000000000000000c";
                    break;
                case 13:
                    dstUrl = "of%3A000000000000000d";
                    break;
                case 14:
                    dstUrl = "of%3A000000000000000e";
                    break;
            }
            String oldsrc="";String newsrcString="";
            switch (Integer.parseInt(Source))
            {
                case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                    srcUrl = "of%3A000000000000000".concat(Source);
                    break;
                case 10:
                    srcUrl = "of%3A000000000000000a";
                    break;
                case 11:
                    srcUrl = "of%3A000000000000000b";
                    break;
                case 12:
                    srcUrl = "of%3A000000000000000c";
                    break;
                case 13:
                    srcUrl = "of%3A000000000000000d";
                    break;
                case 14:
                    srcUrl = "of%3A000000000000000e";
                    break;
            }    
       
           }
        String simpleUrl = "http://localhost:8181/onos/v1/paths/";
        String url=simpleUrl.concat(srcUrl).concat("/").concat(dstUrl).concat("/disjoint");
        String username="onos";
        String password="rocks";
        
         System.out.println("url: "+url);
        //System.out.println("Destination: "+Destination);
        
         JSONParser parser = new JSONParser();
 
        try {
 
       String modif = parser.parse(new FileReader("getPaths.txt")).toString();
            
        
        
      //  int cost = 0;
            
            if(Source.equals(Destination))
            {
         List<NodePortTuple> switchPorts = new ArrayList<NodePortTuple>();
         
            // List<NodePortTuple> switchPorts = new ArrayList<NodePortTuple>() ;
                  
        String mysrc="";String mydst="";
   
            if(Source.contains("of"))
            {            
         mysrc = Source.substring(3);
        
        if(mysrc.equals("000000000000000a"))
        {mysrc="10";}
        else if(mysrc.equals("000000000000000b"))
        {mysrc="11";}
        else if(mysrc.equals("000000000000000c"))
        {mysrc="12";}
        else if(mysrc.equals("000000000000000d"))
        {mysrc="13";}
        else if(mysrc.equals("000000000000000e"))
        {mysrc="14";}

            }
            else
            {
                mysrc = Source;
                
            }
        
        NodePortTuple node = new NodePortTuple(Long.parseLong(mysrc), Short.valueOf(srcPort));
        switchPorts.add(node);
        
             if(Destination.contains("of"))
            {            
         mydst = Destination.substring(3);
        
        if(mydst.equals("000000000000000a"))
        {mydst="10";}
        else if(mydst.equals("000000000000000b"))
        {mydst="11";}
         else if(mydst.equals("000000000000000c"))
        {mydst="12";}
         else if(mydst.equals("000000000000000d"))
        {mydst="13";}
         else if(mydst.equals("000000000000000e"))
        {mydst="14";}

            }
            else
            {
                mydst = Destination;
                
            }
        
        NodePortTuple nodeN = new NodePortTuple(Long.parseLong(mydst), Short.valueOf(dstPort));
        switchPorts.add(nodeN);
        
        
            }//if Source equals Destination
            else
            {
                
       List<NodePortTuple> switchPorts = new ArrayList<NodePortTuple>();
            
       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       
       String userpass = "onos" + ":" + "rocks";
       String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
       con.setRequestProperty ("Authorization", basicAuth);

	//add reuqest header
	con.setRequestMethod("GET");
	con.setRequestProperty("User-Agent", USER_AGENT);
	//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
               //  con.setRequestProperty("Content-Type", "application/json");
           
                      
	//String urlParameters = jsonData;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
                 con.connect();
                 
                 
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
        
        String mysrc="";String mydst="";
   
            if(Source.contains("of"))
            {            
         mysrc = Source.substring(3);
        
        if(mysrc.equals("000000000000000a"))
        {mysrc="10";}
        else if(mysrc.equals("000000000000000b"))
        {mysrc="11";}
        else if(mysrc.equals("000000000000000c"))
        {mysrc="12";}
        else if(mysrc.equals("000000000000000d"))
        {mysrc="13";}
        else if(mysrc.equals("000000000000000e"))
        {mysrc="14";}

            }
            else
            {
                mysrc = Source;
                
            }
            
         if(Destination.contains("of"))
            {            
         mydst = Destination.substring(3);
        
        if(mydst.equals("000000000000000a"))
        {mydst="10";}
        else if(mydst.equals("000000000000000b"))
        {mydst="11";}
       else if(mydst.equals("000000000000000c"))
        {mydst="12";}
        else if(mydst.equals("000000000000000d"))
        {mydst="13";}
        else if(mydst.equals("000000000000000e"))
        {mydst="14";}
            }
            else
            {
                mydst = Destination;
                
            }
         
         if(Source.contains("of"))
        {
        Source=Source.substring(3);
        Destination=Destination.substring(3);
        }
        
         if(Source.equals("000000000000000a"))
        {Source="10";}
        else if(Source.equals("000000000000000b"))
        {Source="11";}
          else if(Source.equals("000000000000000c"))
        {Source="12";}
          else if(Source.equals("000000000000000d"))
        {Source="13";}
          else if(Source.equals("000000000000000e"))
        {Source="14";}
         
          if(Destination.equals("000000000000000a"))
        {Destination="10";}
        else if(Destination.equals("000000000000000b"))
        {Destination="11";}
        else if(Destination.equals("000000000000000c"))
        {Destination="12";}
        else if(Destination.equals("000000000000000d"))
        {Destination="13";}
        else if(Destination.equals("000000000000000e"))
        {Destination="14";}

            
    /*********************************************PARSING JSON RESULT***********************/      
	//print result
        System.out.println("Receive response from ONOS: ");
 	System.out.println(response.toString());
          
                  
        String result = response.toString();
        JSONObject jsonResponse = new JSONObject(result);
               
        JSONArray paths = jsonResponse.getJSONArray("paths");
   
        System.out.println("paths.length(): "+paths.length());

       for(int ind=0; ind<paths.length(); ind++)
        {
       // System.out.println("ind "+ind);

        List<NodePortTuple> switchprimPorts = new ArrayList<NodePortTuple>();
       
        JSONObject primary = paths.getJSONObject(ind).getJSONObject("primary");//get couple primary/backup paths 
        
        //JSONObject backup = paths.getJSONObject(ind).getJSONObject("backup");//get couple primary/backup paths 
        
       // System.out.println("primary: " + primary.toString());
       // System.out.println("backup: " + backup.toString());
        
        /******primary*-***/    
        //System.out.println("primary");
        
        NodePortTuple node = new NodePortTuple(Long.parseLong(mysrc), Short.valueOf(srcPort));
        switchprimPorts.add(node);
       // System.out.println("node found: " + switchPorts.toString());
        
        JSONArray links = primary.getJSONArray("links");
        
        for(int k=0; k<links.length(); k++)
        {
        
        JSONObject source = links.getJSONObject(k).getJSONObject("src");
        
     //   System.out.println("source");
        String port = source.getString("port");
     //   System.out.println("port: "+port);
        String device = source.getString("device");
    //    System.out.println("device: "+device);
        device = device.substring(3);
        
        if(device.equals("000000000000000a"))
        {device="10";}
        else if(device.equals("000000000000000b"))
        {device="11";}
        else if(device.equals("000000000000000c"))
        {device="12";}
        else if(device.equals("000000000000000d"))
        {device="13";}
        else if(device.equals("000000000000000e"))
        {device="14";}
        
        NodePortTuple node1 = new NodePortTuple(Long.parseLong(device), Short.valueOf(port));
        switchprimPorts.add(node1);
      //  System.out.println("node found: " + switchPorts.toString());
        
        JSONObject destination = links.getJSONObject(k).getJSONObject("dst");
        
    //    System.out.println("destination");
        String dport = destination.getString("port");
    //  System.out.println("port: "+dport);
        String ddevice = destination.getString("device");
    //    System.out.println("device: "+ddevice);
        ddevice = ddevice.substring(3);
        
        
        if(ddevice.contains("000000000"))
        {
        if(ddevice.equals("000000000000000a"))
        {ddevice="10";}
        else if(ddevice.equals("000000000000000b"))
        {ddevice="11";}
        else if(ddevice.equals("000000000000000c"))
        {ddevice="12";}
        else if(ddevice.equals("000000000000000d"))
        {ddevice="13";}
        else if(ddevice.equals("000000000000000e"))
        {ddevice="14";}
        
        
        
        NodePortTuple node2 = new NodePortTuple(Long.parseLong(ddevice), Short.valueOf(dport));
        switchprimPorts.add(node2);
       // System.out.println("node found: " + switchPorts.toString());
        
        }

        }//for all segments in the primary path
        
        NodePortTuple nodeN = new NodePortTuple(Long.parseLong(mydst), Short.valueOf(dstPort));
        switchprimPorts.add(nodeN);
       // System.out.println("node found: " + switchPorts.toString());
        
        
        RouteId myrouteid = new RouteId(Long.parseLong(Source), Long.parseLong(Destination), 0);

        returned_route = new Route(myrouteid, switchprimPorts);
        
        disjointRoutes.add(returned_route);
 
        }//for all links in the primary path
       
       for(int ind=0; ind<paths.length(); ind++)
        {
       // System.out.println("ind "+ind);

        List<NodePortTuple> switchbackPorts = new ArrayList<NodePortTuple>();
       
        JSONObject backup = paths.getJSONObject(ind).getJSONObject("backup");//get couple primary/backup paths 
        
        //JSONObject backup = paths.getJSONObject(ind).getJSONObject("backup");//get couple primary/backup paths 
        
       // System.out.println("primary: " + primary.toString());
       // System.out.println("backup: " + backup.toString());
        
        /******primary*-***/    
        //System.out.println("primary");
        
        NodePortTuple node = new NodePortTuple(Long.parseLong(mysrc), Short.valueOf(srcPort));
        switchbackPorts.add(node);
       // System.out.println("node found: " + switchPorts.toString());
        
        JSONArray blinks = backup.getJSONArray("links");
        
        for(int k=0; k<blinks.length(); k++)
        {
        
        JSONObject source = blinks.getJSONObject(k).getJSONObject("src");
        
     //   System.out.println("source");
        String port = source.getString("port");
     //   System.out.println("port: "+port);
        String device = source.getString("device");
    //    System.out.println("device: "+device);
        device = device.substring(3);
        
        if(device.equals("000000000000000a"))
        {device="10";}
        else if(device.equals("000000000000000b"))
        {device="11";}
        else if(device.equals("000000000000000c"))
        {device="12";}
        else if(device.equals("000000000000000d"))
        {device="13";}
        else if(device.equals("000000000000000e"))
        {device="14";}
        
        NodePortTuple node1 = new NodePortTuple(Long.parseLong(device), Short.valueOf(port));
        switchbackPorts.add(node1);
      //  System.out.println("node found: " + switchPorts.toString());
        
        JSONObject destination = blinks.getJSONObject(k).getJSONObject("dst");
        
    //    System.out.println("destination");
        String dport = destination.getString("port");
    //  System.out.println("port: "+dport);
        String ddevice = destination.getString("device");
    //    System.out.println("device: "+ddevice);
        ddevice = ddevice.substring(3);
        
        
        if(ddevice.contains("000000000"))
        {
        if(ddevice.equals("000000000000000a"))
        {ddevice="10";}
        else if(ddevice.equals("000000000000000b"))
        {ddevice="11";}
        else if(ddevice.equals("000000000000000c"))
        {ddevice="12";}
        else if(ddevice.equals("000000000000000d"))
        {ddevice="13";}
        else if(ddevice.equals("000000000000000e"))
        {ddevice="14";}
        
        
        
        NodePortTuple node2 = new NodePortTuple(Long.parseLong(ddevice), Short.valueOf(dport));
        switchbackPorts.add(node2);
       // System.out.println("node found: " + switchPorts.toString());
        
        }

        }//for all segments in the primary path
        
        NodePortTuple nodeN = new NodePortTuple(Long.parseLong(mydst), Short.valueOf(dstPort));
        switchbackPorts.add(nodeN);
       // System.out.println("node found: " + switchPorts.toString());
        
        
        RouteId myrouteid = new RouteId(Long.parseLong(Source), Long.parseLong(Destination), 0);

        returned_route = new Route(myrouteid, switchbackPorts);
        
        disjointRoutes.add(returned_route);
 
        }//for all links found in the backup path

                         }//else Source different from Destination
          
        //List<NodePortTuple> switchPortList = returned_route.getPath();

       // System.out.println("Route found: " + switchPortList.toString());
        
        }//try
        
        catch (Exception e) {
            e.printStackTrace();
           // System.exit(1);
        }
        
        System.out.println("disjointRoutes.size(): "+disjointRoutes.size());
        for(int i=0; i<disjointRoutes.size(); i++)
        {
       Route tmp = disjointRoutes.get(i);
       
       //List<NodePortTuple> switchPortList = tmp.getPath();
       //System.out.println("Route found: " + switchPortList.toString());
        }
        return disjointRoutes;
        
         }    
    /************************************************************************************************/
        
        	
	public void sendAddHostIntent() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();

                String url = "http://127.0.0.1:8181/onos/v1/intents";
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
               // con.setRequestProperty("Accept-Charset", "UTF-8");
                
	//add reuqest header
	con.setRequestMethod("POST");
	//con.setRequestProperty("User-Agent", USER_AGENT);
	//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/json");
               //  con.setRequestProperty("Accept", "application/json;charset=utf-8");

                
              String urlParameters = "{\n" +
                        "  \"type\": \"HostToHostIntent\",\n" +
                        "  \"appId\": \"myApp\",\n" +
                        "   \"one\": \"00:00:00:00:00:03/None\",\n" +
                        "   \"two\": \"00:00:00:00:00:06/None\",\n" +
                        "  \"priority\": 55\n" +
                        "}";
                    
                URLEncoder.encode(urlParameters,"UTF-8");
                
                int responseCode = con.getResponseCode();
	//System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
	//System.out.println("Response Code : " + responseCode);
	
	// Send post request
	/*con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters.toString());
	wr.flush();
	wr.close();*/

	

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
	
	//print result
	//System.out.println(response.toString());
        
	}
        
        
     /************************************************************************************************/
    /************************************************************************************************/    
        
	private void sendGet() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();

                String url = "http://127.0.0.1:8181/onos/v1/topology";
	
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                

                String userpass = "onos" + ":" + "rocks";
                String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
                con.setRequestProperty ("Authorization", basicAuth);
                con.setRequestProperty("Accept-Charset", "UTF-8");
                
              
	con.setRequestMethod("GET");
               
                con.connect();
                
               
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();

	//print result
	//System.out.println(response.toString());
           
                
	}
	
        /************************************************************************************************/
       /************************************************************************************************/
        
	public void sendPost() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();


                String url = "http://193.205.83.126:8008/";
          
                
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                 con.setRequestProperty("Content-Type", "application/xml");
                

                 File xmlFile = new File("sg1-2sap-web.nffg");
                 
                    Reader fileReader = new FileReader(xmlFile);
                    BufferedReader bufReader = new BufferedReader(fileReader);
        
                    StringBuilder sb = new StringBuilder();
                    String line = bufReader.readLine();
                        while( line != null){
                            sb.append(line).append("\n");
                            line = bufReader.readLine();
                            }
                    String xml2String = sb.toString();
                        
	String urlParameters = xml2String;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
	// Send post request
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	int responseCode = con.getResponseCode();//
	//System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
	//System.out.println("Response Code : " + responseCode);

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
	
	//print result
	//System.out.println(response.toString());

	}
        
    /************************************************************************************************/
    /************************************************************************************************/
      
	public void sendServiceRequest() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
        //    System.out.println("\nTesting 3 - Send Http POST request");
            
       

	//String url = "http://127.0.0.1:8888/subpath/get-config";
               // String url = "http://10.30.2.141:8008/escape/sg/";
                String url = "http://192.168.56.101:8008/escape/sg/";
                           
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setDoOutput(true);

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/json");
                

                // The edit-config file in a virtualizer format
                  //  File xmlFile = new File("unify-edit-config.xml");
                //    File xmlFile = new File("sg1_connectivity_sap12.nffg");
                   File xmlFile = new File("sg1_connectivity_sap12_e2e_req.nffg");
                    Reader fileReader = new FileReader(xmlFile);
                    BufferedReader bufReader = new BufferedReader(fileReader);
        
                    StringBuilder sb = new StringBuilder();
                    String line = bufReader.readLine();
                        while( line != null){
                           // System.out.println(line);
                            sb.append(line).append("\n");
                            line = bufReader.readLine();
                            }
                    String xml2String = sb.toString();
        
	String urlParameters = xml2String;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
           
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	//System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
                
                int responseCode = con.getResponseCode();
	System.out.println("Response Code : " + responseCode);

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
               	
	//print result
	//System.out.println(response.toString());
            
	}

    /************************************************************************************************/
        /************************************************************************************************/
      
	public void sendEditConfig() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
        //    System.out.println("\nTesting 3 - Send Http POST request");
            
       

	String url = "http://127.0.0.1:8899/subpath/edit-config";
               // String url = "http://10.30.2.141:8008/escape/sg/";
             //   String url = "http://192.168.56.101:8008/escape/sg/";
                           
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setDoOutput(true);

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/json");
                

                // The edit-config file in a virtualizer format
                    File xmlFile = new File("vnf-config.xml");
                //    File xmlFile = new File("sg1_connectivity_sap12.nffg");
                  // File xmlFile = new File("sg1_connectivity_sap12_e2e_req.nffg");
                    Reader fileReader = new FileReader(xmlFile);
                    BufferedReader bufReader = new BufferedReader(fileReader);
        
                    StringBuilder sb = new StringBuilder();
                    String line = bufReader.readLine();
                        while( line != null){
                           // System.out.println(line);
                            sb.append(line).append("\n");
                            line = bufReader.readLine();
                            }
                    String xml2String = sb.toString();
        
	String urlParameters = xml2String;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
           
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	//System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
                
                int responseCode = con.getResponseCode();
	System.out.println("Response Code : " + responseCode);

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
               	
	//print result
	//System.out.println(response.toString());
            
	}

    /************************************************************************************************/
        
        
        public void sendCancelConfig() throws Exception {
            
            DSE.DSE.count_REST_Messages = DSE.DSE.count_REST_Messages + 1;
            writeFile();
            
        //    System.out.println("\nTesting 3 - Send Http POST request");
            
       

	String url = "http://127.0.0.1:8899/subpath/cancel-config";
               // String url = "http://10.30.2.141:8008/escape/sg/";
             //   String url = "http://192.168.56.101:8008/escape/sg/";
                           
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setDoOutput(true);

	//add reuqest header
	con.setRequestMethod("POST");
	con.setRequestProperty("User-Agent", USER_AGENT);
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/json");
                

                // The edit-config file in a virtualizer format
                    File xmlFile = new File("vnf-cancel.xml");
                //    File xmlFile = new File("sg1_connectivity_sap12.nffg");
                  // File xmlFile = new File("sg1_connectivity_sap12_e2e_req.nffg");
                    Reader fileReader = new FileReader(xmlFile);
                    BufferedReader bufReader = new BufferedReader(fileReader);
        
                    StringBuilder sb = new StringBuilder();
                    String line = bufReader.readLine();
                        while( line != null){
                           // System.out.println(line);
                            sb.append(line).append("\n");
                            line = bufReader.readLine();
                            }
                    String xml2String = sb.toString();
        
	String urlParameters = xml2String;//"sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
           
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();

	//System.out.println("\nSending 'POST' request to URL : " + url);
	//System.out.println("Post parameters : " + urlParameters);
                
                int responseCode = con.getResponseCode();
	System.out.println("Response Code : " + responseCode);

	BufferedReader in = new BufferedReader(
	        new InputStreamReader(con.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
	response.append(inputLine);
	}
	in.close();
               	
	//print result
	//System.out.println(response.toString());
            
	}

    /************************************************************************************************/
        
    /************************************************************************************************/
        
        public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
	byte[] buffer = new byte[1024] ;
	int bytesRead;
	
	while ((bytesRead = fin.read(buffer)) != -1 ) {
	out.write(buffer, 0, bytesRead);
	    }
	    fin.close();
	}

    /************************************************************************************************/
    /************************************************************************************************/
        
       public void writeFile() throws IOException  {
           
        FileWriter fwMsg = new FileWriter("Statistics/restMsg.txt", true);
        BufferedWriter restMsg = new BufferedWriter(fwMsg);
        restMsg.append(Integer.toString(DSE.DSE.count_REST_Messages));
        restMsg.newLine();
        restMsg.flush();
	
	}

    /************************************************************************************************/
    /************************************************************************************************/
}

