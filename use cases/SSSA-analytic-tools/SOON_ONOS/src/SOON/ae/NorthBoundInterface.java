
package ae;

import Utility.XMLUtility;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Molka
 * 
 * This class contains all the elements necessary for the integration with Unify
 */

public class NorthBoundInterface {
    
      private String srcIp = " ";
      private String dstIp = "";
      private ApplicationEntity aeContainer;
    
    public NorthBoundInterface()
    {  }
        
    
    public ArrayList<Document> mappingParameters(org.w3c.dom.Document myDoc)
    {
    
         System.out.println("we are in NorthBoundInterface");
         
         ArrayList<org.w3c.dom.Document> tab_documents = new ArrayList<>();
          
       //  Document editConfig = XMLUtility.getInstance().loadXML("unify-edit-config.xml");
         //XMLUtility.getInstance().printXML(editConfig);
         NodeList flowentry = myDoc.getElementsByTagName("flowentry");
        // String request =" ";
         
         for (int i = 0; i < flowentry.getLength(); i++) {
             
         Node node = flowentry.item(i);
         
         Element value = (Element) node;    
         
         int length = 0;
         length = value.getElementsByTagName("action").getLength();

       //  if(length > 0)
       //  {
      //  String id = value.getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue();

      //  String name = value.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();

     //   Integer priority = Integer.parseInt(value.getElementsByTagName("priority").item(0).getChildNodes().item(0).getNodeValue());
        
        String port = value.getElementsByTagName("port").item(0).getChildNodes().item(0).getNodeValue();

     //   String action = value.getElementsByTagName("action").item(0).getChildNodes().item(0).getNodeValue();
         
        String out = value.getElementsByTagName("out").item(0).getChildNodes().item(0).getNodeValue(); 
        
        //String match = value.getElementsByTagName("match").item(0).getChildNodes().item(0).getNodeValue();
        
        System.out.println("port: "+port);
        System.out.println("out: "+out);
        
        String sourceID = " ";
        Pattern pattern = Pattern.compile("port\\[id=port-(.*?)\\]");
        Matcher matcher = pattern.matcher(port);
         if (matcher.find())
         {
          System.out.println(matcher.group(1));
          sourceID = matcher.group(1);
         }
         
        String destID = " ";
        Pattern pattern2 = Pattern.compile("port\\[id=port-(.*?)\\]");
        Matcher matcher2 = pattern2.matcher(out);
         if (matcher2.find())
         {
          System.out.println(matcher2.group(1));
          destID = matcher2.group(1);
         }
         
         
       /*  Document mappingSAPip = XMLUtility.getInstance().loadXML("unify-mapping-SAP-IP.xml");
         //XMLUtility.getInstance().printXML(editConfig);
         //System.out.println("Root element :"+mappingSAPip.getDocumentElement().getNodeName());
         NodeList nList = mappingSAPip.getElementsByTagName("SAP");
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
               String SAP_id = eElement.getAttribute("id"); 
               String ip = eElement.getAttribute("srcIp"); 
               if(SAP_id.equals(sourceID))
                    {
                      srcIp = ip;
                    }
               else
               if(SAP_id.equals(destID))
                    {
                      dstIp = ip;
                    }
            }
         }*/
         srcIp = sourceID;
         dstIp = destID;
         
                  
         System.out.println("srcIp: "+srcIp);
         System.out.println("dstIp: "+dstIp);  
         
         //org.w3c.dom.Document doc = createXMLRequest("FlowSetupRequest", i+1, 0, false);//Simple Path Setup
         org.w3c.dom.Document doc = createXMLRequest("FlowSetupRequest", HTTPPOSTServer.num_requests, 500, false);//Simple Path Setup
         HTTPPOSTServer.num_requests = HTTPPOSTServer.num_requests + 1;
         
         tab_documents.add(doc);
                     
       //  }//if action tag exists         
         }//for
         

      return tab_documents;
    
    }
    
  private org.w3c.dom.Document createXMLRequest(String requestType, int requestID, double serviceTime, boolean testFlag) {
      
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();
        
        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement(requestType);
      
            root.setAttribute("isFirstNode", "false");
            root.setAttribute("complete", "false");
            root.setAttribute("isLastNode", "false");
            root.setAttribute("vlid_action", "0");//0=No vlan ID
            root.setAttribute("vlid_match", "0");//0=No vlan ID
            root.setAttribute("direction", "forward");
            //cls = (String) comboClass.getSelectedItem();
            //root.setAttribute("class", cls);//0=No vlan ID
            //root.setAttribute("subnet", "32");
            root.setAttribute("pathType", "simple");
            
            root.setAttribute("testFlag", Boolean.toString(testFlag));

            doc.appendChild(root);

            org.w3c.dom.Element ae = doc.createElement("ApplicationEntity");
            ae.setAttribute("name", aeName);
            ae.setAttribute("ipaddress", aeAddress);
            root.appendChild(ae);

            org.w3c.dom.Element req = doc.createElement("requestID");
            req.setTextContent(String.valueOf(requestID));
            root.appendChild(req);

            org.w3c.dom.Element servTime = doc.createElement("serviceTime");
            servTime.setTextContent(String.valueOf(serviceTime));
            root.appendChild(servTime);

            org.w3c.dom.Element iter = doc.createElement("iteration");
            iter.setTextContent("0");//initially 0 for every request
            root.appendChild(iter);

            org.w3c.dom.Element srcNode = doc.createElement("srcNode");
            //srcNode.setAttribute("switch", srcSw.getText());
            //srcNode.setAttribute("port", "1");
            srcNode.setAttribute("ip", srcIp);// "192.168.200.1");
            root.appendChild(srcNode);

            org.w3c.dom.Element dstNode = doc.createElement("dstNode");
            //dstNode.setAttribute("switch", dstSw.getText());
            //dstNode.setAttribute("port", "1");
            dstNode.setAttribute("ip", dstIp);// "192.168.200.2");
            root.appendChild(dstNode);

            org.w3c.dom.Element intermSrcNode = doc.createElement("intermSrcNode");
            //intermSrcNode.setAttribute("switch", srcSw.getText());
            intermSrcNode.setAttribute("port", "1");
            intermSrcNode.setAttribute("ip", srcIp);// "192.168.200.1");
            root.appendChild(intermSrcNode);

            org.w3c.dom.Element intermDstNode = doc.createElement("intermDstNode");
            //intermDstNode.setAttribute("switch", "5");
            //intermDstNode.setAttribute("switch", dstSw.getText());
            intermDstNode.setAttribute("port", "1");
            intermDstNode.setAttribute("ip", dstIp);// "192.168.200.2");
            root.appendChild(intermDstNode);

           
        } catch (Exception e) {
            System.out.println(e);
        }
        return doc;
    }   
    
 /*****************************************************************************************************/   
    
    
    
    
}//class
