
package DSE;


import Message.*;
import Utility.*;
import static DSE.DSE.*;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import DSE.BNServer.TupleStatistics;
import org.w3c.dom.*;

abstract public class PdFeControllerFsm extends FSM {

    final protected int UNDEF = 0;
    final protected int START = 1;
    final protected int END = 2;
    final protected int WAIT_FOR_SCF_REQUEST = 3;
    final protected int WAIT_FOR_TrcFE_CHECK = 4;
    final protected int WAIT_FOR_TrcFE_CONF = 5;
    final protected int WAIT_FOR_BNS = 6;
    //final protected int RSP_TIMEOUT = 7;
    protected String type;
    protected int instanceID;
    protected Vector receivedDseMsg; //messaggi ricevuti dai DSE

    protected int dseMsgSent; //messaggi inviati ai DSE
    protected Message message;
    protected AEMessage requestMessage;
    protected String aeRequestName;
    protected String dseRequestName;
    protected boolean isMaster;
    protected String currentStateName;

    long startingress = 0;

    long startegress = 0;

    long finingress = 0;

    long finegress = 0;

    long latency0 = 0;
    long startTime2 = 0;
    long latency2 = 0;

    BufferedWriter w_seg, w_alg, w_ps;

    public static String myMessage = "default message";
    public HashMap<String, ArrayList<String>> switchesAssignedForRequest;
    public int nAvailable = NUMBER_OF_SWITCHES_WITH_MIDDLEBOX;

    int routeInfo_seq_num_sent = 1;
    int routeInfo_seq_num_recv = 0;

    Timer timer;
    int counter = 0;

    public PdFeControllerFsm(DSE dse, String ses) {
        super(dse, ses);
        currentStateName = "UNDEF";
        dseMsgSent = 0;
        receivedDseMsg = new Vector();
        switchesAssignedForRequest = new HashMap<String, ArrayList<String>>();
        state = START;
        isMaster = false;

    }


    public int execute(Message msg) {

        switch (state) {
            case START: {
                stateSTART(msg);
            }
            break;

            case WAIT_FOR_SCF_REQUEST: {

                state = WAIT_FOR_BNS;

                if (msg.getType() == Message.AE_MSG && (msg.getCommand().equals("ServiceDeliveryRequest")
                        || msg.getCommand().equals("FlowSetupRequest"))) {
                    
                    System.out.println("pdfe: service delivery request");
                   
                   
                    AEMessage tempRequestMessage = null;
                    if (msg.getType() == Message.AE_MSG) {
                        requestMessage = (AEMessage) msg;
                        tempRequestMessage = requestMessage;
                    }

                     XMLUtility.getInstance().printXML(requestMessage.getValue());
                    Element srcNode;
                    Element dstNode;
                    srcNode = (Element) requestMessage.getValue().getElementsByTagName("srcNode").item(0);
                    dstNode = (Element) requestMessage.getValue().getElementsByTagName("dstNode").item(0);

                    String srcIp = srcNode.getAttribute("ip");
                    String customerServiceClass = dseContainer.getPolicyDB().getClass(srcIp);
                    
                    System.out.println("source IP: "+srcNode.getAttribute("ip"));
                    System.out.println("destination IP: "+dstNode.getAttribute("ip"));
                    
                    Element reqID = (Element) requestMessage.getValue().getElementsByTagName("requestID").item(0);
                    System.out.println("request ID: "+reqID.getTextContent());
                    
                 //   Element sla = (Element) requestMessage.getValue().getElementsByTagName("SLA").item(0);
                 //   int slaVal = Integer.valueOf(sla.getTextContent());
                 //   System.out.println("requested SLA: "+slaVal);
                    
                 //   DSE.requestsSLA.put(reqID.getTextContent(), slaVal);
                    
                 //   Element VirtFunctions = (Element) requestMessage.getValue().getElementsByTagName("VFs").item(0);
                      String VFs = "1";//VirtFunctions.getTextContent();
                 //   System.out.println("Number of Virtual Functions: "+VFs);
                    
                    String[] services=null;
                    if (msg.getCommand().equals("FlowSetupRequest")) {
                        System.out.println("NO virtual functions considered");
                        services = null;
                    } else {
                        System.out.println("virtual functions ARE considered");
//                        services = dseContainer.getPolicyDB().getServicePolicy(customerServiceClass, VFs);
//                        System.out.println("services.length: "+services.length);
                    }

                    String mask = dseContainer.getPolicyDB().getSubnetMask(customerServiceClass);

                    long endtime0 = System.currentTimeMillis();
                    latency0 = endtime0 - DSE.startTime;
                    
                    System.out.println("latency0: "+latency0);

                    count_req++;
                    
                    try {
                        boolean ok;
                        switchesAssignedForRequest = new HashMap<String, ArrayList<String>>();
                        ok = provideService(srcNode, dstNode, services, mask, false); //forward
                        //***Do not perform backward
                        if (ok) {//continue backward setup only if the service was not rejected on forward direction
                            requestMessage = tempRequestMessage;
                            switchesAssignedForRequest = new HashMap<String, ArrayList<String>>();
                            provideService(dstNode, srcNode, services, mask, true); //backward
                            }
                        //***/
                    } catch (IOException ex) {
                        Logger.getLogger(PdFeControllerFsm.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PdFeControllerFsm.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if (msg.getType() == Message.BNS_MSG && msg.getCommand().equals("RouteInfoReply")) {
                    BNSMessage requestMessage_rInfo = (BNSMessage) msg;

                    routeInfo_seq_num_recv = Integer.parseInt(requestMessage_rInfo.getValue().getDocumentElement().getAttribute("seqNum"));
                    srcNode_rInfo = (Element) requestMessage_rInfo.getValue().getElementsByTagName("srcNode").item(0);
                    dstNode_rInfo = (Element) requestMessage_rInfo.getValue().getElementsByTagName("dstNode").item(0);
                    //System.out.println("RouteInfoReply received seqNum=" + routeInfo_seq_num_recv);

                } else if (msg.getType() == Message.AE_MSG && msg.getCommand().equals("ServiceCancellationRequest")) {
                    //BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", ((AEMessage) msg).getValue(), ((AEMessage) msg).getSessionID());
                    requestMessage = (AEMessage) msg;

                    Element srcNode = (Element) requestMessage.getValue().getElementsByTagName("srcNode").item(0);
                    Element dstNode = (Element) requestMessage.getValue().getElementsByTagName("dstNode").item(0);

                    String srcIP = srcNode.getAttribute("ip");
                    String dstIP = dstNode.getAttribute("ip");
                    System.out.println("Preparing to send service cancellation request to BNServer");
                    
                    Element reqID = (Element) requestMessage.getValue().getElementsByTagName("requestID").item(0);
                    System.out.println("request ID: "+reqID.getTextContent());

                    dseContainer.removePath(srcIP, dstIP, reqID.getTextContent());
                   //   dseContainer.deleteRequest(srcIP, dstIP, reqID.getTextContent());

                    //sendRequestToBNS((AEMessage) msg);
                } else {//ServiceCancellationRequest
                    System.out.println("still waiting for scf message");
                }
            }
            break;
            case WAIT_FOR_BNS: {

                if (msg.getType() == Message.BNS_MSG) {
                    state = END;
                    stateEND();
                }
            }
            break;

            case END: {
                currentStateName = "END";
                //System.out.println("state end");
                stateEND();
            }

            break;
        }

        return state;

    }

    private void sendRequestToBNS(AEMessage aeMsg) {
        
        System.out.println("sendRequestToBNS");
        BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", aeMsg.getValue(), aeMsg.getSessionID());
        XMLUtility.getInstance().printXML(mess.getValue());
        dseContainer.bnsManager.sendMessage(mess);
       // dseContainer.sendMessage(mess);
        

    }

   
    protected void sendResponseToAe(String value) {
        Document doc = createAEResponse(value);
        dseContainer.sendMessage(new AEMessage(doc, requestMessage.getSessionID()));
    }

      protected void sendResponseToAe(Document doc) {
      //  Document doc = createAEResponse(value);
        dseContainer.sendMessage(new AEMessage(doc, requestMessage.getSessionID()));
    }
      
    public void timeout() {
        if (state != END) {
            state = END;

            execute(null);
        }
    }

    protected void stateSTART(Message msg) {
        message = msg;
        if (message.getCommand().equals("StatisticsRequest")
                || message.getCommand().equals("FlowSetupRequest")
                || message.getCommand().equals("DeleteFlowRequest")
                || message.getCommand().equals("ServiceDeliveryRequest")
                || message.getCommand().equals("ServiceCancellationRequest")
                //|| message.getCommand().equals("RouteInfoRequest")
                || message.getCommand().equals("RouteInfoReply")) { //??

            state = WAIT_FOR_SCF_REQUEST;

            //System.out.println("the next state is " + state);
        } else {
            System.out.println("wrong command");
        }

    }

    public void timeoutConfig(int time) {
        while (true) {
            try {
                //sleep(50);			
            } catch (Exception e) {
            }
            time = time - 500;

            if (time <= 0) {
                //obj.timeout();	
                //System.out.println("Scaduto il timeout!");
                state = END;
                stateEND();
                break;
            }
        }
    }

    abstract public Document createAEResponse(String value);/**/


    private boolean provideService(Element srcNode, Element dstNode, String[] services, String mask, boolean flag) throws IOException, InterruptedException {
        
        System.out.println("*********** Service delivery **************");
        
        String direction="";

        String reqID = requestMessage.getValue().getElementsByTagName("requestID").item(0).getTextContent();

        ArrayList<ArrayList<Object>> servicesReached = new ArrayList<ArrayList<Object>>();

        startTime2 = System.currentTimeMillis(); //take new starTime for each additional iteration
        requestMessage.getValue().getDocumentElement().setAttribute("isLastNode", "false");

        if (flag == false) {
            direction = "forward";
        } 
         else {
            direction = "backward";
              }

        requestMessage.getValue().getDocumentElement().setAttribute("direction", direction);
        ArrayList switchesAssigned = new ArrayList();

        //String pathType = requestMessage.getValue().getDocumentElement().getAttribute("pathType");
        //servNodes = null;
        if (services == null || services.length == 0) { //Either no policy specified or Simple path has been requested
            
            System.out.println("provide service for a simple request");
            
            ((Element) requestMessage.getValue().getDocumentElement()).setAttribute("pathType", "simple");
            //String firstTerminal = servNodes[0];//the first middle box in the list becomes a dst to the main source
            ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("switch", srcNode.getAttribute("switch"));
            ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("port", srcNode.getAttribute("port"));
            ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("switch", dstNode.getAttribute("switch"));
            ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("port", dstNode.getAttribute("port"));

            ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("ip", dstNode.getAttribute("ip"));
            ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("ip", srcNode.getAttribute("ip"));

            if (flag == true) {//backward direction finished
                requestMessage.getValue().getDocumentElement().setAttribute("complete", "true");
                flag = false;
            }
           // requestMessage.getValue().getDocumentElement().setAttribute("complete", "true");//only forward direction considered
            requestMessage.getValue().getDocumentElement().setAttribute("subnet", mask);
            requestMessage.getValue().getDocumentElement().setAttribute("isFirstNode", "true");
            requestMessage.getValue().getDocumentElement().setAttribute("isLastNode", "true");

            System.out.println("the message to send to the bns is");
            XMLUtility.getInstance().printXML(requestMessage.getValue());
            
            sendRequestToBNS(requestMessage);
            //latency2 = endTime2 - startTime2;
            //System.out.println("message sent to bns");
        } else {
            
            System.out.println("provide service for a composite request");
            
            System.out.println("Service nodes found=" + services.length);

            ((Element) requestMessage.getValue().getDocumentElement()).setAttribute("pathType", "composite");

            ArrayList<Element> swMboxes;
            //create an element from another element

            Document d = XMLUtility.getInstance().createDocument();

            //Element swMb = d.createElement("swmb");//(Element) requestMessage.getValue().getDocumentElement().getOwnerDocument().createElement("swmb");
            Element Curr = d.createElement("Curr");
            Element Next = d.createElement("Next");
            Element mbLastTerminal = d.createElement("mbLastTerminal");

            String vlid_prev;
            String vlid;
            for (int ind = 0; ind < services.length; ind++) {
                String serv = services[ind];
                System.out.println("Service Found " + serv);
                swMboxes = DSE.getMiddleboxDB().getServiceMiddleboxes(serv);
                
                System.out.println("swMboxes.size(): "+swMboxes.size());
                ArrayList<Object> routeInfo;
                
               /*   if (swMboxes.size() < 3) { 
                      
                      System.out.println("REJECT REQUEST " + reqID + ", no sufficient available switches ");
                
                 
                      count_rejected_req++;
                        File rej = new File ("reqRejected.txt");
                        BufferedWriter bwRej = new BufferedWriter (new FileWriter (rej,true));
                        bwRej.append(reqID).append(" ");
                        bwRej.append(Integer.toString(count_rejected_req));
                        bwRej.newLine();
                        bwRej.flush();
                        bwRej.close();

                        //send Response To AE;
                        Document dRej = XMLUtility.getInstance().createDocument();
                        Element root = dRej.createElement("ServiceRequestResponse");
                        dRej.appendChild(root);
                        Element resp = dRej.createElement("Response");
                        resp.appendChild(dRej.createTextNode("Request rejected"));
                        root.appendChild(resp);

                      //   sendResponseToAe(dRej);
                        return false;
                  }*/
                
                
                if (ind == 0) {
                    routeInfo = getClosestServiceInstance(reqID, srcNode, swMboxes, true, false);//identify destination
                    if (routeInfo == null || routeInfo.isEmpty()) {
                        //reject request
                        count_rejected_req++;
                        File rej = new File ("reqRejected.txt");
                        BufferedWriter bwRej = new BufferedWriter (new FileWriter (rej,true));
                        bwRej.append(reqID).append(" ");
                        bwRej.append(Integer.toString(count_rejected_req));
                        bwRej.newLine();
                        bwRej.flush();
                        bwRej.close();

                        //send Response To AE;
                        Document dRej = XMLUtility.getInstance().createDocument();
                        Element root = dRej.createElement("ServiceRequestResponse");
                        dRej.appendChild(root);
                        Element resp = dRej.createElement("Response");
                        resp.appendChild(dRej.createTextNode("Request rejected"));
                        root.appendChild(resp);


                        return false;
                    } else {
            
                        Next.setAttribute("switch", routeInfo.get(0).toString());
                        Next.setAttribute("port", routeInfo.get(1).toString());
                        Next.setAttribute("ip", routeInfo.get(2).toString());

                        Next.setAttribute("outPort", routeInfo.get(3).toString());
                        Next.setAttribute("vlid", routeInfo.get(4).toString());

                        Curr.setAttribute("switch", routeInfo.get(5).toString());
                        Curr.setAttribute("port", routeInfo.get(6).toString());
                        Curr.setAttribute("ip", routeInfo.get(7).toString());
                        Curr.setAttribute("outPort", routeInfo.get(8).toString());
                        Curr.setAttribute("vlid", routeInfo.get(9).toString());

                        switchesAssigned.add(routeInfo.get(0).toString());
                        System.out.println("chosen switch1: "+routeInfo.get(0).toString());
                        servicesReached.add(routeInfo);
                        /*for(int i=0; i<swMboxes.size(); i++)
                        {
                        String swID = swMboxes.get(i).getAttribute("switch");
                        if(swID.equals(routeInfo.get(0).toString()))
                        swMboxes.remove(swMboxes.get(i));
                        }*/
                    }
                } 
                
                else if (ind <= (services.length - 1)) {

                    routeInfo = getClosestServiceInstance(reqID, Next, swMboxes, false, false);
                    if (routeInfo == null || routeInfo.isEmpty()) {
                        //reject request
                        count_rejected_req++;

                        //send Response To AE;
                        Document dRej = XMLUtility.getInstance().createDocument();
                        Element root = dRej.createElement("ServiceRequestResponse");
                        dRej.appendChild(root);
                        Element resp = dRej.createElement("Response");
                        resp.appendChild(dRej.createTextNode("Request rejected"));
                        root.appendChild(resp);

                        return false;
                    } else {

                        Next.setAttribute("switch", routeInfo.get(0).toString());
                        Next.setAttribute("port", routeInfo.get(1).toString());
                        Next.setAttribute("ip", routeInfo.get(2).toString());

                        Next.setAttribute("outPort", routeInfo.get(3).toString());
                        Next.setAttribute("vlid", routeInfo.get(4).toString());

                        //vlid = Next.getAttribute("vlid");
                        //System.out.println("Route Info inside provideService: switch=" + Next.getAttribute("switch") + " vlid=" + vlid);
                        Curr.setAttribute("switch", routeInfo.get(5).toString());
                        Curr.setAttribute("port", routeInfo.get(6).toString());
                        Curr.setAttribute("ip", routeInfo.get(7).toString());
                        Curr.setAttribute("outPort", routeInfo.get(8).toString());
                        Curr.setAttribute("vlid", routeInfo.get(9).toString());

                        switchesAssigned.add(routeInfo.get(0).toString());
                        System.out.println("chosen switch2: "+routeInfo.get(0).toString());
                        servicesReached.add(routeInfo);
                       /*  for(int i=0; i<swMboxes.size(); i++)
                        {
                        String swID = swMboxes.get(i).getAttribute("switch");
                        if(swID.equals(routeInfo.get(0).toString()))
                        swMboxes.remove(swMboxes.get(i));
                        }*/
                    }
                }
                
                // if (!switchesAssigned.isEmpty()) {
                System.out.println("Assigned switches is put in the arraylist");
                switchesAssignedForRequest.put(reqID, switchesAssigned);
           // }
            }


            vlid = "0";
            for (int ind2 = 0; ind2 < servicesReached.size(); ind2++) {
                ArrayList<Object> rInfo = servicesReached.get(ind2);
                d = XMLUtility.getInstance().createDocument();
                Curr = d.createElement("Curr");
                Next = d.createElement("Next");
                vlid_prev = vlid;
                if (ind2 == 0) {
                    Next.setAttribute("switch", rInfo.get(0).toString());
                    Next.setAttribute("port", rInfo.get(1).toString());
                    Next.setAttribute("ip", rInfo.get(2).toString());

                    Next.setAttribute("outPort", rInfo.get(3).toString());
                    Next.setAttribute("vlid", rInfo.get(4).toString());

                    vlid = Next.getAttribute("vlid");

                    Curr.setAttribute("switch", rInfo.get(5).toString());
                    Curr.setAttribute("port", rInfo.get(6).toString());
                    Curr.setAttribute("ip", rInfo.get(7).toString());
                    Curr.setAttribute("outPort", rInfo.get(8).toString());
                    Curr.setAttribute("vlid", rInfo.get(9).toString());

                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("switch", Curr.getAttribute("switch"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("port", Curr.getAttribute("port"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("switch", Next.getAttribute("switch"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("port", Next.getAttribute("port"));

                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("ip", dstNode.getAttribute("ip"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("ip", srcNode.getAttribute("ip"));

                    requestMessage.getValue().getDocumentElement().setAttribute("vlid_match", "0");
                    requestMessage.getValue().getDocumentElement().setAttribute("vlid_action", vlid);
                    requestMessage.getValue().getDocumentElement().setAttribute("subnet", mask);

                    //System.out.println("check vlanMach" + vlid_prev + " vlid_action=" + vlid);
                    //System.out.println("CHECK curr: " + Curr.getAttribute("vlid") + " next: " + Next.getAttribute("vlid"));
                    requestMessage.getValue().getDocumentElement().setAttribute("isFirstNode", "true");

                    if (ind2 == servicesReached.size() - 1) {
                        mbLastTerminal = Next;//swMb
                    }

                    System.out.println("the message to send to the bns is");
                    XMLUtility.getInstance().printXML(requestMessage.getValue());
                    sendRequestToBNS(requestMessage);
                    //System.out.println("message sent to bns");

                } else if (ind2 <= (servicesReached.size() - 1)) {
                    Next.setAttribute("switch", rInfo.get(0).toString());
                    Next.setAttribute("port", rInfo.get(1).toString());
                    Next.setAttribute("ip", rInfo.get(2).toString());
                    Next.setAttribute("outPort", rInfo.get(3).toString());
                    Next.setAttribute("vlid", rInfo.get(4).toString());

                    vlid = Next.getAttribute("vlid");
                    //System.out.println("Route Info inside provideService: switch=" + Next.getAttribute("switch") + " vlid=" + vlid);

                    Curr.setAttribute("switch", rInfo.get(5).toString());
                    Curr.setAttribute("port", rInfo.get(6).toString());
                    Curr.setAttribute("ip", rInfo.get(7).toString());
                    Curr.setAttribute("outPort", rInfo.get(8).toString());
                    Curr.setAttribute("vlid", rInfo.get(9).toString());

                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("switch", Curr.getAttribute("switch"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("port", Curr.getAttribute("outPort"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("switch", Next.getAttribute("switch"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("port", Next.getAttribute("port"));

                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("ip", dstNode.getAttribute("ip"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("ip", srcNode.getAttribute("ip"));

                    requestMessage.getValue().getDocumentElement().setAttribute("vlid_match", vlid_prev);
                    requestMessage.getValue().getDocumentElement().setAttribute("vlid_action", vlid);
                    requestMessage.getValue().getDocumentElement().setAttribute("subnet", mask);

                    //System.out.println("check vlanMach" + vlid_prev + " vlid_action=" + vlid);
                    requestMessage.getValue().getDocumentElement().setAttribute("isFirstNode", "false");
                    //System.out.println("CHECK curr: " + Curr.getAttribute("vlid") + " next: " + Next.getAttribute("vlid"));

                    if (ind2 == servicesReached.size() - 1) {
                        mbLastTerminal = Next;//swMb
                    }

                    sendRequestToBNS(requestMessage);
                    //System.out.println("message sent to bns");

                }
                if (ind2 == (servicesReached.size() - 1)) {

                    Curr = mbLastTerminal;
                    Next = dstNode;

                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("switch", Curr.getAttribute("switch"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("port", Curr.getAttribute("outPort"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("switch", Next.getAttribute("switch"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("port", Next.getAttribute("port"));

                    ((Element) requestMessage.getValue().getElementsByTagName("intermDstNode").item(0)).setAttribute("ip", dstNode.getAttribute("ip"));
                    ((Element) requestMessage.getValue().getElementsByTagName("intermSrcNode").item(0)).setAttribute("ip", srcNode.getAttribute("ip"));

                    requestMessage.getValue().getDocumentElement().setAttribute("vlid_match", vlid /*vlid_prev*/);
                    requestMessage.getValue().getDocumentElement().setAttribute("vlid_action", "0");
                    requestMessage.getValue().getDocumentElement().setAttribute("subnet", mask);
                    //System.out.println("check vlanMach=" + Curr.getAttribute("vlid") + " vlid_action=0");

                    requestMessage.getValue().getDocumentElement().setAttribute("isFirstNode", "false");
                    requestMessage.getValue().getDocumentElement().setAttribute("isLastNode", "true");

                    System.out.println("CHECK THE LAST MIDDLEBOX");
                    XMLUtility.getInstance().printXML(requestMessage.getValue());
                    
                    if (flag == true) {//consider backward direction finished
                        System.out.println("REVERSE setup completed");
                        requestMessage.getValue().getDocumentElement().setAttribute("complete", "true");
                        flag = false;
                    }
                  // requestMessage.getValue().getDocumentElement().setAttribute("complete", "true");//consider only forward direction
                    System.out.println("the message to send to the bns is");
                  //  XMLUtility.getInstance().printXML(requestMessage.getValue());
                    sendRequestToBNS(requestMessage);
                    //System.out.println("message sent to bns");
                }
            }
        }
        return true;
    }

    private synchronized ArrayList<Object> getClosestServiceInstance(String requestID, Element Curr, ArrayList<Element> swMboxes,
            boolean isFirstNode, boolean isLastNode) throws InterruptedException, FileNotFoundException, IOException {
        
        System.out.println("get closest Service Instance");
        
        ArrayList<Object> routeInfo = new ArrayList<Object>();
        long s = System.currentTimeMillis();

        //System.out.println("getting the closest instance for request " + requestID + " switch " + Curr.getAttribute("switch")
        //        + " port " + Curr.getAttribute("port") + " ip " + Curr.getAttribute("ip"));
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();
        org.w3c.dom.Element root = doc.createElement("RouteInfoRequest");

        root.setAttribute("seqNum", Integer.toString(routeInfo_seq_num_sent));
        root.setAttribute("isFirstNode", String.valueOf(isFirstNode));
        root.setAttribute("isLastNode", String.valueOf(isLastNode));
        org.w3c.dom.Element srcNode = doc.createElement("srcNode");
        srcNode.setAttribute("switch", Curr.getAttribute("switch"));
        srcNode.setAttribute("port", Curr.getAttribute("port"));
        srcNode.setAttribute("outPort", Curr.getAttribute("outPort"));
        srcNode.setAttribute("ip", Curr.getAttribute("ip"));

        String outPortSrc = Curr.getAttribute("outPort");
        String vlidSrc = Curr.getAttribute("vlid");

        root.appendChild(srcNode);
        org.w3c.dom.Element destNode = doc.createElement("destNode");
        int count_cand = 0;

             int switches = switchesWithMiddlebox.size();
        ArrayList<String> assignedSwitches = switchesAssignedForRequest.get(requestID);
        for (Element swMbox : swMboxes) {
            org.w3c.dom.Element candidate = doc.createElement("candidate");
            String swID = swMbox.getAttribute("switch");
            //System.out.println(" swMboxes size = " + swMboxes.size());
            //System.out.println(" switch to be candidate = " + swID);

            if ((!doBlock && switches == 1)
                    || (switches < 2 && !Curr.getAttribute("switch").equals(swID))
                    || ((switches >= 2 && !Curr.getAttribute("switch").equals(swID)
                    && (assignedSwitches == null || !assignedSwitches.contains(swID))))) {
                candidate.setAttribute("switch", swID);
                candidate.setAttribute("port", swMbox.getAttribute("port"));
                candidate.setAttribute("outPort", swMbox.getAttribute("outPort"));
                candidate.setAttribute("vlid", swMbox.getAttribute("vlid"));
                //System.out.println("check outPort=" + swMbox.getAttribute("outPort") + " ip=" + swMbox.getAttribute("ip"));
                candidate.setAttribute("ip", swMbox.getAttribute("ip"));
                destNode.appendChild(candidate);
                count_cand++;
                System.out.println("added new candidate switch:" + swID);
                
            } else if (accepted_requests.containsKey(requestID)) {
                candidate.setAttribute("switch", swID);
                candidate.setAttribute("port", swMbox.getAttribute("port"));
                candidate.setAttribute("outPort", swMbox.getAttribute("outPort"));
                candidate.setAttribute("vlid", swMbox.getAttribute("vlid"));
                //System.out.println("check outPort=" + swMbox.getAttribute("outPort") + " ip=" + swMbox.getAttribute("ip"));
                candidate.setAttribute("ip", swMbox.getAttribute("ip"));
                destNode.appendChild(candidate);
                count_cand++;
            }

        }


        System.out.println("number of candidate switches to connect from sw " + Curr.getAttribute("switch") + " = " + count_cand);
        if (count_cand >= 1) {//at least one possible destination (candidate) found
            root.appendChild(destNode);
            doc.appendChild(root);
            AEMessage mess = new AEMessage(doc, "");

            sendRequestToBNS(mess); //upon receiving this message, BNServer will update the routeInfo database

            System.out.println("routeInfoRequest sent");
            File routeInfoFile, checkFile;
            FileReader fr, fr2;
            BufferedReader br, br2;
            FileWriter fw;
            BufferedWriter bw;

            String str;

            checkFile = dseContainer.getCheckFile();
            routeInfoFile = dseContainer.getRouteInfoFile();

            fr = new FileReader(checkFile);
            br = new BufferedReader(fr);
            str = br.readLine();
            while (str == null || str.equals("1") == false /*|| !routeInfoFile.exists()*/) {
               // System.out.println("waiting for checkFile.....");
                Thread.sleep(20);
                fr = new FileReader(checkFile);
                br = new BufferedReader(fr);
                str = br.readLine();
               // System.out.println("checkFile contains " + str);
                br.close();
                fr.close();
            }

            fr2 = new FileReader(routeInfoFile);
            br2 = new BufferedReader(fr2);

            System.out.println("Route info received from BNS: from switch " + Curr.getAttribute("switch"));
            String ip = br2.readLine();
            String sw = br2.readLine();
            String port = br2.readLine();
            String outPort = br2.readLine();
            String vlid = br2.readLine();

            String swSrc = br2.readLine();
            String portSrc = br2.readLine();
            String ipSrc = br2.readLine();

            System.out.println("switch=" + sw);
            System.out.println("port=" + port);
            System.out.println("outPort=" + outPort);

            br2.close();
            fr2.close();

            if (ip.equals("null") || sw.equals("null") || port.equals("null") || outPort.equals("null") || vlid.equals("null")) {
                requestMessage = null;
                System.out.println("REJECT REQUEST " + requestID + ", can't find route " /*+ Curr.getAttribute("switch") + "/" + Curr.getAttribute("ip")*/);
                return null;
            }

            boolean isAvail;

            if (!doBlock) {
                isAvail = true;//for non-blocking case
            } else //for blocking case
             if (accepted_requests.containsKey(requestID)) {
                    isAvail = true;
                } else {
                    isAvail = checkLoad(Long.parseLong(sw));
                }

            if (!isAvail) {
                System.out.println("Switch " + sw + " has reached its limit");
                for (int i = 0; i < swMboxes.size(); i++) {
                    Element sm = swMboxes.get(i);
                    String sId = sm.getAttribute("switch");
                    if (sId.equals(sw)) {
                        swMboxes.remove(i);  //remove the switch from the candidates list
                        System.out.println("Switch " + sw + " has been removed from the candidates list");
                        break;
                    }
                }

                checkFile = new File("files/checkFile.txt");
                fw = new FileWriter(checkFile, false);
                bw = new BufferedWriter(fw);
                bw.write("0");
                bw.close();
                fw.close();

                //System.out.println("checkFile is updated to 0");
                if (swMboxes.isEmpty()) {//no more swMbox to be used
                    //reject request
                    System.out.println("REJECT REQUEST " + requestID + ", no more swMbox available");
                    requestMessage = null;
                    return null;
                } else {
                    System.out.println("retry getting closest instance");
                    return getClosestServiceInstance(requestID, srcNode, swMboxes, isFirstNode, isLastNode);//recursive call
                }
            }

            routeInfo.add(sw);
            routeInfo.add(port);
            routeInfo.add(ip);
            routeInfo.add(outPort);
            routeInfo.add(vlid);

            routeInfo.add(swSrc);
            routeInfo.add(portSrc);
            routeInfo.add(ipSrc);

            routeInfo.add(outPortSrc);
            routeInfo.add(vlidSrc);

            checkFile = new File("files/checkFile.txt");
            fw = new FileWriter(checkFile, false);
            bw = new BufferedWriter(fw);
            bw.write("0");
            bw.close();
            fw.close();

            return routeInfo;
        } else {
            System.out.println("REJECT REQUEST " + requestID + ", No candidate found. switchesWithMiddlebox size=" + switches);
            requestMessage = null;
            return null; //No candidate found
        }
    }

    private boolean checkLoad(Long swId) {
        if (DSE.statsModel != null) {
            Set<Long> ks = DSE.statsModel.keySet();
            long bytes_in_a_period;
            TupleStatistics<ArrayList<String>, Long, Long> tupleStatistics;
            //System.out.println("************************************************");
            for (Long key : ks) {
                if (key.equals(swId)) {
                    tupleStatistics = DSE.statsModel.get(key);
                    bytes_in_a_period = tupleStatistics.bytes_in_a_period;
                    //System.out.println("current dest sw = " + key + " bytes_in_a_period = " + bytes_in_a_period);
                    if (bytes_in_a_period >= threshold) { //6MB, 6000000 Bytes
                        //System.out.println(swId + " is overloaded");
                        return false;
                    }
                }
            }
        }
        //System.out.println("ACCEPT REQUEST");
        return true;

    }

    
}//class

