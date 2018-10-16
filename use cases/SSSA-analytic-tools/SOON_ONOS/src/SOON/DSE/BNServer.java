package DSE;

import static DSE.DSE.overloadedSwitches;
import Message.*;
import Utility.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import Controller.Ethernet;
import Controller.IPv4;
import Controller.Route;
import Controller.NodePortTuple;
import Controller.MACAddress;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.json.JSONException;
import org.slf4j.LoggerFactory;
//import org.kohsuke.args4j.CmdLineException;
//import org.kohsuke.args4j.CmdLineParser;

import org.w3c.dom.*;
import rest.HttpExample;

public class BNServer extends Thread {
    
    protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

 //   private static IFloodlightProviderService dev;
    //   protected static IDeviceService device;
 //   protected static ITopologyService topology;
    //   protected static IRoutingService routingEngine;
    Timer timer;

    private String deviceAddress;
    private int devicePort;
    private int port;
    public static String bnsName;
    public String dseHostName;
    public static int dsePort;
    private DatagramSocket dataSocket;
    private Socket deviceSocket;
    private boolean isLoop;
    private int timerLogin;

    public static Socket dseSocket = null;
    public static boolean isDSEConnected = false;

    long startTime, endTime = 0;
    long setupTime = 0;

    long startTimePath, endTimePath = 0;
    long setupTimePath = 0;

    int count = 1;
    static public int counter = 1;

    static File routeInfoFile, checkFile, checkPath, msgCountFile;
    FileWriter fwMsg;
    static BufferedWriter bwMsg;
    public static final int FORWARDING_APP_ID = 2; // TODO: This must be managed
    // by a global APP_ID class

    static int hops = 0;

    private String myMessage = "one";
    private static BNServer INSTANCE = null; //Singleton

    static BufferedWriter writer;
    static File flowstats;

    static FileWriter pathFW;
    static BufferedWriter pathBW;

    long origSrcSw, origDstSw = 0;
    String origSrcIp, origDstIp;

    public static ArrayList<HashMap<Integer, ArrayList<ArrayList<Segment>>>> all_paths;//two paths
    public static int path_id, prev_path_id = 1;
    public static HashMap<Integer, ArrayList<ArrayList<Segment>>> bi_path;
    public static ArrayList<Segment> segments;
    public static ArrayList<ArrayList<Segment>> bi_segments;
    public static Document docPath;

    public static ConcurrentHashMap<String, ArrayList<Object>> requestCharacteristics;

    public static ConcurrentHashMap<String, String> intent_corresponding_flow = new ConcurrentHashMap<String, String>();
   
    public static ConcurrentHashMap<String, String> intent_gives_flow = new ConcurrentHashMap<String, String>();
   
            ConcurrentHashMap<Long, ArrayList<Object>> statsCurr = new ConcurrentHashMap<Long, ArrayList<Object>>();
        ConcurrentHashMap<Long, ArrayList<Object>> statsPrev = new ConcurrentHashMap<Long, ArrayList<Object>>();

        public ConcurrentHashMap<Long, Long> mapCurrentBytes = new ConcurrentHashMap<>();
        public ConcurrentHashMap<Long, Long> mapPeriodBytes = new ConcurrentHashMap<>();
        

    public static String mess = "ddd";
    //ConcurrentHashMap<Long, ArrayList<Object>> statsPrev, statsCurr;
    public static ConcurrentHashMap<Long, Long> mapBytes, mapTotalBytes;


    int stats_counter = 1;
    int stats_flow_counter = 1;

    public static int stats_duration_sec = 30;//stats for overloaded switches => taken every 50 seconds

    public static int stats_flow_duration_sec = 90;//stats for flow SLA => taken every 5 seconds
   
    public static int stats_Switch_duration_sec = 100;

    static int count_msgs_to_controller = 0;
 
    int MBOX_PORT = 3;

    BNSMessage msg2 = null;
    BNSMessage msg;
    CopyOnWriteArrayList<BNSMessage> msgList;

    public static ObjectOutputStream oos;

    private BNServer() throws IOException {

       // log.info("we are in the BNSer method");
        msgList = new CopyOnWriteArrayList<>();

        mapBytes = new ConcurrentHashMap<>();
        mapTotalBytes = new ConcurrentHashMap<>();

        requestCharacteristics = new ConcurrentHashMap<>();
       

    }

   public class Tuple<MAC, IP, SWDPID, PORT> {

        public final MAC mac;
        public final IP ip;
        public final SWDPID swdpid;
        public final PORT port;

        public Tuple(MAC mac, IP ip, SWDPID swdpid, PORT port) {
            this.mac = mac;
            this.ip = ip;
            this.swdpid = swdpid;
            this.port = port;
        }
    }

    Tuple<Long, Integer, Long, Short> ipMacEntry;
    public static ConcurrentHashMap<Integer, Tuple> macTable = new ConcurrentHashMap<>();

    private void makeHostsDetectable() {

        Document MACTable = XMLUtility.getInstance().loadXML("MACTable.xml");
        NodeList macEntries = MACTable.getElementsByTagName("MacEntries");
        Element macEntries2 = (Element) macEntries.item(0);
        NodeList entries = macEntries2.getElementsByTagName("MacEntry");

        //push ARP entries
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            int ip = IPv4.toIPv4Address(entry.getAttribute("ip"));
            Long mac = Ethernet.toLong(MACAddress.valueOf(entry.getAttribute("mac")).toBytes());
            Long swdpid = Long.parseLong(entry.getAttribute("switch"));
            Short port = Short.parseShort(entry.getAttribute("port"));
            MACAddress macAdd = MACAddress.valueOf(mac);
            //macBytes = macAdd.toBytes();

            ipMacEntry = new Tuple<>(mac, ip, swdpid, port);

            if (macTable.putIfAbsent(ip, ipMacEntry) == null) {//return null if key didnt exist before
                //  log.info("Host detected ip = " + ip);
            } else {
                log.info("Host already detected");//key already exists
            }
        }

    }

    public class AppendingObjectOutputStream extends ObjectOutputStream {

        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }

    }

    public static void main(String[] args) throws InterruptedException, IOException {

        System.out.println("************************************");
        System.out.println("              BNServer              ");
        System.out.println("************************************");

        BNServer bns = new BNServer();

        bns.init(args);

        routeInfoFile = new File("files/RouteInfoReply.txt");
        checkFile = new File("files/checkFile.txt");

        msgCountFile = new File("results/msgCountFile.txt");

        FileWriter fwMsg = new FileWriter(msgCountFile, true);
        bwMsg = new BufferedWriter(fwMsg);

        flowstats = new File("flowstats");
        writer = new BufferedWriter(new FileWriter(flowstats));

    }

    public String getMyMessage() {
        return myMessage;
    }

    public void setMyMessage(String mess) {
        myMessage = mess;
    }

    Runnable OnosStarter = new Runnable() {

        @Override
        public void run() {

            //controller.run();
            //  System.out.println("Running the controller");
            //Floodlight fl = new Floodlight(args);
        }
    };

    public void init(String[] args) {

       // System.out.println("we are in the init class");
        Config conf = Config.getInstance();
        dseHostName = conf.getString("dseHostName");
        dsePort = conf.getInt("dsePort");
        // deviceAddress = conf.getString("deviceAddress");
        // devicePort = conf.getInt("devicePort");
        bnsName = conf.getString("BNSname");
        port = conf.getInt("BNServerPort");
        timerLogin = conf.getInt("TimerLogin");

        if (conf.getString("loop").equals("yes")) {
            isLoop = true;
        } else {
            isLoop = false;
        }

        /*   System.setProperty("org.restlet.engine.loggerFacadeClass",
         "org.restlet.ext.slf4j.Slf4jLoggerFacade");
         CmdLineSettings settings = new CmdLineSettings();
         CmdLineParser parser = new CmdLineParser(settings);
         try {
         parser.parseArgument(args);
         } catch (CmdLineException e) {
         parser.printUsage(System.out);
         System.exit(1);
         }*/
        //statsModel = new ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>>();
       // statsCurr = new ConcurrentHashMap<Long, ArrayList<Object>>();
       // statsPrev = new ConcurrentHashMap<Long, ArrayList<Object>>();

        Thread onStarter = new Thread(OnosStarter);

        onStarter.start();
        start();

        makeHostsDetectable();

    }

    //private boolean deletePath(long swdpid, short port, short port2, String srcIP, String dstIP, short vlid_match, String identifier) throws IOException, Exception {
    private boolean deletePath(long swdpid, short port, short port2, String srcIP, String dstIP, String priority, String identifier) throws IOException, Exception {

       log.info("delete Path");
     //  System.out.println("cancel flow from sw " + swdpid);
        HttpExample http = new HttpExample();
        String key = identifier.concat("-").concat(String.valueOf(swdpid)).concat("-").concat(String.valueOf(port)).concat("-").concat(String.valueOf(port2)).concat("-").concat(srcIP).concat("-").concat(dstIP).concat("-").concat(priority);
     //    System.out.println("sending path delete with this key : "+key);
        http.deleteIntent(key);

     //   System.out.println("Flow deleted sucessfully!");
        return true;

    }

    private boolean checkDuplicateSegment(Segment segment, Segment segment_tmp, short vlid_match, short vlid_action) {
        short tempVlan = vlid_action;
        short temVlanMatch = vlid_match;
        boolean seg_exists = false;
        for (int indx = segment.intermNodePortTuple.size() - 1; indx > 0; indx -= 2) {
            if (indx > 2) {
                /* vlid_action might have been set by ingress node and we need to use it
                 in the match of the subsequent flows of that route(except the ingress node itself)
                 */
                if (vlid_action != 0) {
                    vlid_match = vlid_action;
                }
                vlid_action = 0; //avoids unnecessary modification of vlan id

            } else { //Modify vlan id ONLY once(at the begining) for a given route
                vlid_action = tempVlan;
                vlid_match = temVlanMatch;
            }
            short outPort = segment.intermNodePortTuple.get(indx).getPortId();
            short inPort = segment.intermNodePortTuple.get(indx - 1).getPortId();
            if (segment_tmp.srcIP.equals(segment.srcIP)
                    && segment_tmp.dstIP.equals(segment.dstIP)
                    //&& segment_tmp.swdpid == segment.swdpid
                    //&& segment_tmp.swdpid2 == segment.swdpid2
                    && segment_tmp.port == inPort //segment.port
                    //&& segment_tmp.port2 == outPort //segment.port2
                    && segment_tmp.vlid_match == vlid_match /*segment.vlid_match*/) {//ignore duplicate segment
                seg_exists = true;
                //  System.out.println("segment already exist");
                break;
            }
        }
        if (seg_exists) {
            return true;//ignore
        }
        return false;
    }


     public static class TupleStatistics<FLOWS, TOTAL_BYTES, BYTES_IN_A_PERIOD> implements java.io.Serializable { //Tuple<srcMac, srcIP, swId, Port>

        public final FLOWS flows;
        public final TOTAL_BYTES total_bytes;
        public final BYTES_IN_A_PERIOD bytes_in_a_period;

        public TupleStatistics(FLOWS flows, TOTAL_BYTES total_bytes, BYTES_IN_A_PERIOD bytes_in_a_period) {
            this.flows = flows;
            this.total_bytes = total_bytes;
            this.bytes_in_a_period = bytes_in_a_period;
        }
    }

    private final class StatisticsTask implements Callable<ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>>> {

        ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>> stats = new ConcurrentHashMap<>();
        //final Set<Long> switchDpids = controller.getAllSwitchDpids();
        Long sId;

        // @Override
        public ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>> call() throws Exception {

            try {

                ArrayList<Object> flowsCurr;
                //total_bytes = collectStatistics(sId);
                ArrayList<Object> sCurr;

             //    System.out.println(" collectStatistics of sId: "+sId);
                sCurr = collectStatistics(sId);

                log.info(" we are in the call method: "+stats_counter);
                long totalBytes = mapTotalBytes.get(sId);

                flowsCurr = (ArrayList<Object>) sCurr.get(0);
                long bytesInPeriod = (long) sCurr.get(1);

                statsCurr.put(sId, sCurr);

                if (stats_counter >= 1) {

                    TupleStatistics<ArrayList<Object>, Long, Long> tupleStatistics;
                    //log.info("in if");
                    tupleStatistics = new TupleStatistics<>(flowsCurr, totalBytes, bytesInPeriod);

                    stats.put(sId, tupleStatistics);

                }
                // System.out.println("statsModel size = "+stats.size());
            } catch (InterruptedException | TimeoutException | ExecutionException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException | IOException ex) {
               // System.out.println("Something went wrong while collecting statistics");
                Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            return stats;
        }

        private StatisticsTask(Long sId) {
            this.sId = sId;
        }

    }

    class Statistics implements Runnable {

        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();

        @Override
        public void run() {

            log.info("Collecting Statistics");

            HttpExample http = new HttpExample();

            try {

                final Set<Long> switchDpids = http.getSwitchDpids();

                statsPrev.clear();

                // log.info("statsPrev size after removing all elements:" + statsPrev.size());
                statsPrev.putAll(statsCurr);

                //  log.info("statsPrev size after replaced by  statsCurr :" + statsPrev.size());
                statsCurr.clear();

               // log.info("statsCurr size after removing all elements:" + statsCurr.size());
                ExecutorService threadPool = Executors.newFixedThreadPool(14);
               // Thread.activeCount();

                CompletionService<ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>>> pool = new ExecutorCompletionService<>(threadPool);

                for (final Long sId : switchDpids) {
                   
               //  if (DSE.switchesWithMiddlebox.contains(sId))   
                      //   {
                  log.info("submit task for switch: "+sId);
                    //  log.info(pool.toString());
                    pool.submit(new StatisticsTask(sId));
                       //  }

                }

                ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>> allStats = new ConcurrentHashMap<>();

                int N = switchDpids.size(); 
                        
                for (int i = 0; i < N; i++) {

                    //    log.info("we are before the for: "+i); 
                    try {
                        ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>> resultStats = pool.take().get();
                   // ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>> resultStats = pool.poll().get();

                           log.info("resultStats: "+resultStats.size()); 
                        for (long k : resultStats.keySet()) {
                            allStats.put(k, resultStats.get(k));
                        }
                        //Compute the result
                    } catch (InterruptedException | ExecutionException ex) {
                       // log.info(ex.toString());
                        Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                log.info("size of allStats after thread=" + allStats.size());

                //  if (stats_counter > 1 && !allStats.isEmpty()) {
                Document docRes = XMLUtility.getInstance().createDocument();
                Element root = docRes.createElement("Statistics");
                docRes.appendChild(root);

                msg2 = new BNSMessage("127.0.0.1", dsePort, bnsName, docRes, msg.getSessionID());
                //sendMsgToDSE(msg2);
               // log.info("Sending statistics data for " + allStats.size() + " switches");

                sendStatisticsToDSE(allStats, msg2);

               // }
                for (long k : allStats.keySet()) {
                    allStats.remove(k);
                }

                allStats.clear();

                allStats.putAll(
                        new ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>>());

                stats_counter++;

            } catch (Exception ex) {
               // log.info(ex.toString());
                Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * **********************************************************************************************************************
     */
    /**
     * **********************************************************************************************************************
     */
    class StatisticsFlow implements Runnable {

        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();

        @Override
        public void run() {

           // log.info("Collecting PER FLOW Statistics");
            ConcurrentHashMap<String, Integer> last_switch_flow_associated_bytes = new ConcurrentHashMap<>();

            HttpExample http = new HttpExample();

            try {

                Document docRes = XMLUtility.getInstance().createDocument();
                Element root = docRes.createElement("FlowStatistics");
                docRes.appendChild(root);

                int nbElements = requestCharacteristics.size();
               
                log.info("requestCharacteristics.size()> "+requestCharacteristics.size());
               
                int j = 0;

                Iterator iter = requestCharacteristics.keySet().iterator();

                //while ((j<(nbElements-1)) && iter.hasNext())
                while (iter.hasNext()) {

                    String request = iter.next().toString();
                  //  log.info("request: "+request);

                    ArrayList<Object> tmpChar = (ArrayList<Object>) requestCharacteristics.get(request);

                    if (tmpChar != null && !tmpChar.isEmpty()) {

                        //  log.info("tmpChar.size(): "+tmpChar.size());
                        List<NodePortTuple> switchPortList = (List<NodePortTuple>) tmpChar.get(0);

                       log.info("switchPortList: "+switchPortList.size());
                        long switchID = switchPortList.get(switchPortList.size() - 1).getNodeId();
                        int portID = switchPortList.get(switchPortList.size() - 1).getPortId();

                        ArrayList<String> flowsIentifiersLast = http.getFlowStatsPerPort(switchID, portID);

                        log.info("Last switchID: "+switchID);
                       log.info("Last portID: "+portID);
                      
                        String fl_id = request.concat("-").concat(String.valueOf(switchID)).concat("-").concat(String.valueOf(portID));
                        log.info("flow_id: "+fl_id);
                       
                        String corrisponding_flow_id = intent_corresponding_flow.get(fl_id);
                        log.info("corrisponding_flow_id: "+corrisponding_flow_id);

                        int numBytes = 0;

                        // log.info("flowsIentifiers at the LAST switch: ");
                        for (int indx = 0; indx < flowsIentifiersLast.size(); indx++) {

                       log.info(flowsIentifiersLast.get(indx));
                               log.info("bytes: "+http.getFlowBytes(flowsIentifiersLast.get(indx)));
                            if (corrisponding_flow_id.equals(flowsIentifiersLast.get(indx))) {
                                numBytes = http.getFlowBytes(flowsIentifiersLast.get(indx));
                                last_switch_flow_associated_bytes.put(flowsIentifiersLast.get(indx), numBytes);
                                log.info("numBytes of the flow of LAST switch: "+numBytes);
                            }
                        }

             //   for(int i=0; i<switchPortList.size(); i++)
                    //      {
                        //   long swID = switchPortList.get(i).getNodeId();    
                        //   int ptID = switchPortList.get(i).getPortId();
             
                       
              
               // }
                        ArrayList<String> installedIntents = ((ArrayList<String>) tmpChar.get(1));

                   // for (int indx = 0; indx < installedIntents.size(); indx++) {
                       //log.info("installedIntents(indx): "+installedIntents.get(indx));
                        // }
                        for (int indx = 0; indx < installedIntents.size(); indx++) {

                            Element intent = docRes.createElement("Intent");
                            root.appendChild(intent);
                           
                            String FlowIDtest = intent_gives_flow.get(installedIntents.get(indx));
                            int flowBytesTest = http.getFlowBytes(FlowIDtest);
                           
                            if(flowBytesTest > 0)
                            {
                               
                             Element flow = docRes.createElement("Flow");
                        root.appendChild(flow);
                        flow.setAttribute("requestID", request);
                       // flow.setAttribute("flowID", corrisponding_flow_id);
                        //flow.setAttribute("numBytes", String.valueOf(numBytes));
                        flow.setAttribute("flowID", intent_gives_flow.get(installedIntents.get(indx)));
                        flow.setAttribute("numBytes", String.valueOf(http.getFlowBytes(FlowIDtest)));
                        flow.setAttribute("sw", String.valueOf(switchID));
                        flow.setAttribute("port", String.valueOf(portID));
                       
                       
                       
                            intent.setAttribute("intentID", installedIntents.get(indx));
                            intent.setAttribute("requestID", request);
                            intent.setAttribute("FlowID", intent_gives_flow.get(installedIntents.get(indx)));
                                                       
                            log.info("FlowIDtest: "+FlowIDtest);
                           
                            log.info("Flowbytes: "+String.valueOf(http.getFlowBytes(FlowIDtest)));
                           
                            intent.setAttribute("Flowbytes", String.valueOf(http.getFlowBytes(FlowIDtest)));
                           
                            }
                           
                        }

                    }
                    j++;

                }//for all the current requests

               
                if (stats_flow_counter > 1) {// && !allPerFlowStats.isEmpty()) {

                  //  XMLUtility.getInstance().printXML(docRes);

                    msg2 = new BNSMessage("127.0.0.1", dsePort, bnsName, docRes, msg.getSessionID());
                //sendMsgToDSE(msg2);
                    log.info("Sending per flow statistics data!!");

                    sendPerFlowStatisticsToDSE(msg2);

                }

                stats_flow_counter++;

            } catch (Exception ex) {
                Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }//run
    }

    /**
     * *********************************************************************************************************************
     */
   
    class StatisticsSwitch implements Runnable {

    
        @Override
        public void run() {

            log.info("Collect Stats for the flows");

            HttpExample http = new HttpExample();

            try {
                http.getAllFlows();
            } catch (Exception ex) {
                Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }//run
    }

    /**
     * *********************************************************************************************************************
     */
    @Override
    public synchronized void run() {

        try {

            dataSocket = new DatagramSocket(port);

            if (!isLoop) {
            }

            while (true) {

                BNSMessage msg_tmp = receiveMsgFromDSE();

                msgList.add(msg_tmp);
                for (Iterator<BNSMessage> msgIter = msgList.iterator(); msgIter.hasNext();) {
                    msg = msgIter.next();
                    if (msg == null) {

                        break;
                    }
                    msgList.remove(msg);

                    Document doc = msg.getValue();
                    String messageType = doc.getDocumentElement().getNodeName();

                    if (messageType.equals("DeleteFlowRequest")) {

                        XMLUtility.getInstance().printXML(doc);
                        String srcIP = ((Element) msg.getValue().getElementsByTagName("srcNode")
                                .item(0)).getAttribute("ip");
                        String dstIP = ((Element) msg.getValue().getElementsByTagName("dstNode")
                                .item(0)).getAttribute("ip");

                       // flowSetUp2(srcIP, dstIP, 1);
                        Document docRes = XMLUtility.getInstance().createDocument();
                        Element root = docRes.createElement("DeleteFlowReply");
                        docRes.appendChild(root);
                        Element resp = docRes.createElement("Response");
                        root.appendChild(resp);
                        resp.appendChild(docRes.createTextNode("Flow Deleted!"));

                        msg2 = new BNSMessage("127.0.0.1" /*dseHostName*/, dsePort, bnsName, docRes, msg.getSessionID());

                        sendMsgToDSE(msg2);
                        //log.info("Message sent to DSE " + dseHostName + " port " + dsePort + " sessionID " + msg.getSessionID());
                         log.info("the message sent to dse is");
                        XMLUtility.getInstance().printXML(msg2.getValue());
                    }
                   
                    else if (messageType.equals("ServiceDeliveryRequest")
                            || messageType.equals("FlowSetupRequest")) {

                        startTimePath = System.currentTimeMillis();
                       
                        log.info("doc giving segment: ");
                        XMLUtility.getInstance().printXML(msg.getValue());

                        ++count_msgs_to_controller;

                        bwMsg.append(Integer.toString(count_msgs_to_controller));
                        bwMsg.newLine();
                        bwMsg.flush();
                        //log.info("count_msgs_to_controller=" + count_msgs_to_controller);

                        long swdpid1 = 0;
                        short port1 = 0;
                        long swdpid2 = 0;
                        short port2 = 0;

                        String pathType = msg.getValue().getDocumentElement().getAttribute("pathType");
                        String srcIP = ((Element) msg.getValue().getElementsByTagName("intermSrcNode")
                                .item(0)).getAttribute("ip");

                        boolean isFirstNode = Boolean.parseBoolean((msg.getValue().getDocumentElement().getAttribute("isFirstNode")));
                        if (isFirstNode == true) {
                            segments = new ArrayList<Segment>();
                            origSrcIp = ((Element) msg.getValue().getElementsByTagName("srcNode")
                                    .item(0)).getAttribute("ip");
                            Tuple srcInfo2 = handleIPResolution(origSrcIp);
                            origSrcSw = (long) srcInfo2.swdpid;

                            origDstIp = ((Element) msg.getValue().getElementsByTagName("dstNode")
                                    .item(0)).getAttribute("ip");
                            Tuple dstInfo2 = handleIPResolution(origDstIp);
                            origDstSw = (long) dstInfo2.swdpid;

                            Tuple srcInfo = handleIPResolution(srcIP);

                            if (srcInfo == null) {//IP doesnt exist
                                //log.info("Can't detect source host with IP address " + srcIP);
                                //log.info("System is exiting");
                                System.exit(0);
                            }
                            swdpid1 = (long) srcInfo.swdpid;
                            port1 = (short) srcInfo.port;

                            //needed for adaptive case
                            ((Element) msg.getValue().getElementsByTagName("intermSrcNode")
                                    .item(0)).setAttribute("switch", srcInfo2.swdpid.toString());
                            ((Element) msg.getValue().getElementsByTagName("intermSrcNode")
                                    .item(0)).setAttribute("port", srcInfo2.port.toString());

                        } else {
                            swdpid1 = Long.parseLong(((Element) msg.getValue().getElementsByTagName("intermSrcNode")
                                    .item(0)).getAttribute("switch"));
                            port1 = Short.parseShort(((Element) msg.getValue().getElementsByTagName("intermSrcNode")
                                    .item(0)).getAttribute("port"));
                        }

                        String dstIP = ((Element) msg.getValue().getElementsByTagName("intermDstNode")
                                .item(0)).getAttribute("ip");

                        boolean isLastNode = Boolean.parseBoolean((msg.getValue().getDocumentElement().getAttribute("isLastNode")));
                        if (isLastNode == true) {
                            //log.info("resolving dstIP");
                            Tuple dstInfo = handleIPResolution(dstIP);
                            if (dstInfo == null) {//IP doesnt exist
                                // log.info("Can't detect destination host with IP address " + dstIP);
                                //log.info("System is exiting");
                                System.exit(0);
                            }
                            swdpid2 = (long) dstInfo.swdpid;
                            port2 = (short) dstInfo.port;

                            //needed for adaptive case
                            ((Element) msg.getValue().getElementsByTagName("intermDstNode")
                                    .item(0)).setAttribute("switch", dstInfo.swdpid.toString());
                            ((Element) msg.getValue().getElementsByTagName("intermDstNode")
                                    .item(0)).setAttribute("port", dstInfo.port.toString());
                        } else {
                            swdpid2 = Long.parseLong(((Element) msg.getValue().getElementsByTagName("intermDstNode")
                                    .item(0)).getAttribute("switch"));
                            port2 = Short.parseShort(((Element) msg.getValue().getElementsByTagName("intermDstNode").item(0)).getAttribute("port"));
                        }
                        String direction = msg.getValue().getDocumentElement().getAttribute("direction");
                        String subnetMask = msg.getValue().getDocumentElement().getAttribute("subnet");

                        short vlid_action = Short.parseShort(msg.getValue().getDocumentElement().getAttribute("vlid_action"));
                        short vlid_match = Short.parseShort(msg.getValue().getDocumentElement().getAttribute("vlid_match"));
                        boolean complete = Boolean.parseBoolean(msg.getValue().getDocumentElement().getAttribute("complete"));

                        String requestID = ((Element) msg.getValue().getElementsByTagName("requestID")
                                .item(0)).getTextContent();
                        String serviceTime = ((Element) msg.getValue().getElementsByTagName("serviceTime")
                                .item(0)).getTextContent();
                        String iteration = ((Element) msg.getValue().getElementsByTagName("iteration")
                                .item(0)).getTextContent();

                        //    log.info("serviceTime for requestID " + requestID + " in BNS = " + serviceTime);
                        String responseMessageType = null;

                        if (messageType.equals("ServiceDeliveryRequest")) {
                            responseMessageType = "ServiceDeliveryReply";
                        } else if (messageType.equals("FlowSetupRequest")) {
                            responseMessageType = "FlowSetupReply";
                        }
                        startTime = System.currentTimeMillis();
                        if (count == 1) {
                            bi_path = new HashMap<Integer, ArrayList<ArrayList<Segment>>>();
                            bi_segments = new ArrayList<ArrayList<Segment>>();
                        }
                        Segment segment = new Segment(pathType, direction, origSrcSw, origDstSw,
                                swdpid1, port1, srcIP, dstIP, subnetMask, swdpid2, port2,
                                (short) vlid_action, (short) vlid_match, isLastNode, 0, Double.parseDouble(serviceTime));

                        List<NodePortTuple> intermNodePortTuple;

                        if (overloadedSwitches.contains(swdpid1) || overloadedSwitches.contains(swdpid2)) {
                            //   log.info("Either switch " + swdpid1 + " or " + swdpid2 + " is overloaded");
                            //System.exit(0);
                        } else {

                           // log.info("START Setting up one direction");
                            String identifier = requestID.concat("-").concat(direction);
                          //  log.info("identifier: "+identifier);

                         //   log.info("requestCharacteristics.size(): "+requestCharacteristics.size());
                            if (!(requestCharacteristics.containsKey(identifier))) {
                                ArrayList<Object> characteristics = new ArrayList<>();
                                //  log.info("newarrayList");
                                requestCharacteristics.put(identifier, characteristics);
                            }

                            intermNodePortTuple = setup(segment, identifier);//ONE setup forward and ONE setup backward

                            log.info("END Setting up one direction");
                            if (intermNodePortTuple != null) {
                                segment.intermNodePortTuple = intermNodePortTuple;
                                boolean s_exists = false;
                                for (ArrayList<Segment> segments_tmp : bi_segments) {
                                    for (Segment segment_tmp : segments_tmp) {
                                        s_exists = checkDuplicateSegment(segment, segment_tmp, vlid_match, vlid_action);
                                        if (s_exists) {
                                            break;
                                        }
                                    }
                                    if (s_exists) {
                                        break;
                                    }
                                }
                                if (!s_exists) {
                                    for (Segment s : segments) {
                                        s_exists = checkDuplicateSegment(segment, s, vlid_match, vlid_action);

                                        if (s_exists) {
                                            //log.info("segment already exist, second block");
                                            break;
                                        }
                                    }
                                }
                                if (!s_exists) {
                                    //log.info("add new segment");
                                    segments.add(segment);
                                }
                                //log.info("Subpath setup successful! " + "sw1 " + swdpid1 + " port1 " + port1 + " swdpid2 " + swdpid2 + " port2 " + port2);
                                endTime = System.currentTimeMillis();
                                setupTime += (endTime - startTime);

                                /*
                                 w_segment.append(Long.toString(endTime - startTime));
                                 w_segment.newLine();
                                 w_segment.close();*/
                                long duration = endTime - startTime;

                                count++;
                                if (isLastNode == true) { //either forward or backward direction is done
                                    bi_segments.add(segments);
                                   // log.info("Last segment src ip=" + segment.srcIP + " dst ip=" + segment.dstIP);
                                }
                                if (complete == true) {

                               //     log.info("Complete == true");
                                    //   log.info("requestID " + requestID + " iteration " + iteration + " endTime " + endTime + " startTime " + startTime + " setupTime " + setupTime + " duration " + duration + " count " + count);
                                    bi_path.put(path_id, bi_segments);
                                    docPath = XMLUtility.getInstance().createDocument();
                                    Element root = docPath.createElement(responseMessageType);
                                    docPath.appendChild(root);
                                    Element resp = docPath.createElement("Response");
                                    root.appendChild(resp);
                                    resp.setAttribute("requestID", requestID);
                                    resp.setAttribute("serviceTime", serviceTime);
                                    resp.setAttribute("startTime", "0");
                                    resp.setAttribute("iteration", iteration);
                                    resp.setAttribute("src", origSrcIp);
                                    resp.setAttribute("dst", origDstIp);

                                    log.info("request completed");

                                    endTimePath = System.currentTimeMillis();

                                    setupTimePath = endTimePath - startTimePath;
                                    //  log.info("setupTimePath: "+setupTimePath);
                                    File stp = new File("setupTimePath.txt");
                                    BufferedWriter bwStp = new BufferedWriter(new FileWriter(stp, true));
                                    //bwStp.append(identifier).append(" ");
                                    bwStp.append(Long.toString(setupTimePath));
                                    bwStp.newLine();
                                    bwStp.flush();
                                    bwStp.close();

                                    setupTimePath = 0;

                                    Set<Integer> keyset = bi_path.keySet();
                                    for (int k : keyset) {
                                        ArrayList<ArrayList<Segment>> bp = bi_path.get(k);
                                        if (!bp.isEmpty()) {
                                            org.w3c.dom.Element req = docPath.createElement("Request");
                                            resp.appendChild(req);
                                            //log.info("bp size = " + bp.size());
                                            for (int i = 0; i < bp.size(); i++) {
                                                ArrayList<Segment> uni_path = bp.get(i);
                                                if (!uni_path.isEmpty()) {
                                                    Element path = docPath.createElement("Path");
                                                    req.appendChild(path);
                                                    org.w3c.dom.Element src = docPath.createElement("srcIP");
                                                    src.setTextContent(uni_path.get(0).srcIP);
                                                    path.appendChild(src);
                                                    org.w3c.dom.Element dst = docPath.createElement("dstIP");
                                                    dst.setTextContent(uni_path.get(0).dstIP);
                                                    path.appendChild(dst);
                                                    org.w3c.dom.Element dir = docPath.createElement("direction");
                                                    dir.setTextContent(uni_path.get(0).direction);
                                                    path.appendChild(dir);

                                                    for (Segment segm : uni_path) {
                                                        Element seg = docPath.createElement("Segment");
                                                        path.appendChild(seg);
                                                        short vlid_action2 = segm.vlid_action;
                                                        short vlid_match2 = segm.vlid_match;
                                                        short tempVlan = vlid_action2;
                                                        short temVlanMatch = vlid_match2;
                                                        for (int indx = segm.intermNodePortTuple.size() - 1; indx > 0; indx -= 2) {
                                                            if (indx > 2) {
                                                                // vlid_action might have been set by ingress node and we need to use it
                                                                //in the match of the subsequent flows of that route(except the ingress node itself)
                                                                if (vlid_action2 != 0) {
                                                                    vlid_match2 = vlid_action2;
                                                                }
                                                                vlid_action2 = 0; //avoids unnecessary modification of vlan id

                                                            } else { //Modify vlan id ONLY once(at the begining) for a given route
                                                                vlid_action2 = tempVlan;
                                                                vlid_match2 = temVlanMatch;
                                                            }

                                                            long switchDPID = segm.intermNodePortTuple.get(indx).getNodeId();
                                                            short outPort = segm.intermNodePortTuple.get(indx).getPortId();
                                                            short inPort = segm.intermNodePortTuple.get(indx - 1).getPortId();

                                                            Element node = docPath.createElement("Node");
                                                            seg.appendChild(node);

                                                            org.w3c.dom.Element prt1 = docPath.createElement("inPort");
                                                            prt1.setTextContent(String.valueOf(inPort));
                                                            node.appendChild(prt1);

                                                            org.w3c.dom.Element prt2 = docPath.createElement("outPort");
                                                            prt2.setTextContent(String.valueOf(outPort));
                                                            node.appendChild(prt2);

                                                            org.w3c.dom.Element vlan_m = docPath.createElement("vlan_match");
                                                            vlan_m.setTextContent(String.valueOf(vlid_match2));
                                                            node.appendChild(vlan_m);

                                                            org.w3c.dom.Element sw = docPath.createElement("switch");
                                                            sw.setTextContent(String.valueOf(switchDPID));
                                                            node.appendChild(sw);
                                                        }
                                                    }
                                                } else {
                                                    log.info("uni_path is empty!");

                                                }
                                            }
                                        } else {
                                            log.info("bp is empty");
                                        }
                                    }
                                   
                                   /*  log.info("Path setup complete! "
                                     + " path_id = " + path_id
                                     + " total number of paths=" + bi_path.size() + " elements");*/

                                   

                                    path_id++; //increment path_id after finishing backward setup

                                    long setupTime_av = setupTime / 2 /*(count - 1)*/; // devide by 2 for unidirectional
                                    setupTime = 0;

                                    count = 1;
                                    /*
                                     w.append(Long.toString(setupTime_av));
                                     w.newLine();
                                     w.flush();
                                     */

                                    if (counter == 1) {
                                        counter++;
                                       
                                         log.info("starting timer");
                                        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                                       // StatisticsCollector Collect = new StatisticsCollector();
                                        exec.scheduleAtFixedRate(new Statistics(), 0, stats_duration_sec, TimeUnit.SECONDS);
                                       // log.info("starting timer");
                                    //    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                                    //    StatisticsCollector Collect = new StatisticsCollector();
                                   //     exec.scheduleAtFixedRate(Collect.new Statistics(), 0, stats_duration_sec, TimeUnit.SECONDS);

                                    //  log.info("starting timer> statistics per flow");
                                     //    ScheduledExecutorService execFlow = Executors.newSingleThreadScheduledExecutor();
                                    // execFlow.scheduleAtFixedRate(new StatisticsFlow(),0, stats_flow_duration_sec, TimeUnit.SECONDS);
                                       
                                   //     ScheduledExecutorService execSwitch = Executors.newSingleThreadScheduledExecutor();
                                     //   execSwitch.scheduleAtFixedRate(new StatisticsSwitch(),0, stats_Switch_duration_sec, TimeUnit.SECONDS);
                                    }

                                    resp.appendChild(docPath.createTextNode("Service delivery sucessful!"));

                                    msg2 = new BNSMessage("127.0.0.1" /*dseHostName*/, dsePort, bnsName, docPath, msg.getSessionID());

                                     log.info(("Service delivery sucessful!"));
                                    
                                             
                                    sendMsgToDSE(msg2);
                                    // log.info("Message sent to DSE " + dseHostName + " port " + dsePort + " sessionID " + msg.getSessionID());
                                 //   log.info("Service delivery sucessful!");
                                   XMLUtility.getInstance().printXML(msg2.getValue());

                                }
            /***********Populate Database****************                   
                Document docIntents = XMLUtility.getInstance().createDocument();
                Element iroot = docIntents.createElement("Intents");
                docIntents.appendChild(iroot);
                org.w3c.dom.Element inten;
                ArrayList<String> tmpIntents = (ArrayList<String>)Constants.active_intents_per_request.get(identifier);
               for (int ind=0;ind<tmpIntents.size();ind++) {                  
                   String intent = tmpIntents.get(ind);
                   inten = docIntents.createElement("Intent");          
                   inten.setAttribute("id", String.valueOf(ind+1));
                   inten.setAttribute("key", intent);
                   iroot.appendChild(inten);
                                    }
                XMLUtility.getInstance().printXML(docIntents);
                XMLUtility.getInstance().saveXML(docIntents, "Database/".concat(identifier.concat("_intents.xml")));
               
               
                Document docFlows = XMLUtility.getInstance().createDocument();
                Element froot = docFlows.createElement("Flows");
                docFlows.appendChild(froot);
                org.w3c.dom.Element flow;
                ArrayList<String> tmpFlows = (ArrayList<String>)Constants.active_flows_per_request.get(identifier);
               for (int jnd=0;jnd<tmpFlows.size();jnd++) {                  
                   String flo = tmpFlows.get(jnd);
                   flow = docFlows.createElement("Flow");          
                   flow.setAttribute("id", String.valueOf(jnd+1));
                   flow.setAttribute("key", flo);
                   froot.appendChild(flow);
                                    }
                XMLUtility.getInstance().printXML(docFlows);
                XMLUtility.getInstance().saveXML(docFlows, "Database/".concat(identifier.concat("_flows.xml")));
               
               
                Document docSwitches = XMLUtility.getInstance().createDocument();
                Element sroot = docSwitches.createElement("Switches");
                docSwitches.appendChild(sroot);
                org.w3c.dom.Element swit;
                ArrayList<String> tmpSwitches = (ArrayList<String>)Constants.involved_switches_per_request.get(identifier);
               for (int knd=0;knd<tmpSwitches.size();knd++) {                  
                   String switches = tmpSwitches.get(knd);
                   swit = docSwitches.createElement("Switch");          
                   swit.setAttribute("id", String.valueOf(knd+1));
                   swit.setAttribute("key", switches);
                   sroot.appendChild(swit);
                                    }
                XMLUtility.getInstance().printXML(docSwitches);
                XMLUtility.getInstance().saveXML(docSwitches, "Database/".concat(identifier.concat("_switches.xml")));
               
            /*****************************************************/

                            } else {
                              //  System.err.println("Error while setting up subpath");
                                Document docError = XMLUtility.getInstance().createDocument();
                                Element root = docError.createElement(responseMessageType);
                                docError.appendChild(root);
                                Element resp = docError.createElement("Error");
                                root.appendChild(resp);
                                resp.appendChild(docError.createTextNode("ERROR! Cannot complete path setup."));
                                Element id = docError.createElement("ReqID");
                                root.appendChild(id);
                                String[] parts = identifier.split("-");
                                String reqID = parts[0];
                                id.appendChild(docError.createTextNode(reqID));
                                msg2 = new BNSMessage("127.0.0.1" /*dseHostName*/, dsePort, bnsName, docError, msg.getSessionID());

                                sendMsgToDSE(msg2);
                                // log.info("Message sent to DSE " + dseHostName + " port " + dsePort + " sessionID " + msg.getSessionID());
                                log.info("the message sent to dse is");
                                XMLUtility.getInstance().printXML(msg2.getValue());

                            }
                            //SwitchPort srcSwPort = new SwitchPort(123455, 13);
                            //SwitchPort dstSwPort = new SwitchPort(123455, 14);
                        }

                    } else if (messageType.equals("ServiceCancellationRequest")) {

                        ++count_msgs_to_controller;

                        bwMsg.append(Integer.toString(count_msgs_to_controller));
                        bwMsg.newLine();
                        bwMsg.flush();
                        //log.info("count_msgs_to_controller=" + count_msgs_to_controller);

                          log.info("ServiceCancellationRequest");
                          XMLUtility.getInstance().printXML(msg.getValue());

                        String requestID = ((Element) msg.getValue().getElementsByTagName("requestID")
                                .item(0)).getTextContent();

                        String srcIP = ((Element) msg.getValue().getElementsByTagName("srcNode")
                                .item(0)).getAttribute("ip");
                        String dstIP = ((Element) msg.getValue().getElementsByTagName("dstNode")
                                .item(0)).getAttribute("ip");

                        long swId = Long.parseLong(msg.getValue().getDocumentElement().getAttribute("switch"));
                        short inPort = Short.parseShort(msg.getValue().getDocumentElement().getAttribute("inPort"));
                        short outPort = Short.parseShort(msg.getValue().getDocumentElement().getAttribute("outPort"));
                        short vlid_match = Short.parseShort(msg.getValue().getDocumentElement().getAttribute("vlid_match"));
                        String direction = msg.getValue().getDocumentElement().getAttribute("direction");
                        String priority = msg.getValue().getDocumentElement().getAttribute("priority");

                     //   log.info("Path to delete:" + swId + " / " + srcIP + ", " + dstIP /*+ ") with vlid_match " + vlid_match*/);

                        String identifier = requestID.concat("-").concat(direction);

                        //if (deletePath(swId, inPort, outPort, srcIP, dstIP, vlid_match, identifier)) {
                        if (deletePath(swId, inPort, outPort, srcIP, dstIP, priority, identifier)) {

                            requestCharacteristics.remove(identifier);

                            log.info("requestCharacteristics.size() AFTER CANCEL: "+requestCharacteristics.size());
                            log.info("Path deleted successfully");
                            Document docRes = XMLUtility.getInstance().createDocument();
                            Element root = docRes.createElement("ServiceCancellationReply");
                            docRes.appendChild(root);
                            Element resp = docRes.createElement("Response");

                            root.appendChild(resp);
                            resp.appendChild(docRes.createTextNode("Service cancellation (path deletion) sucessful!"));

                            msg2 = new BNSMessage("127.0.0.1" /*dseHostName*/, dsePort, bnsName, docRes, msg.getSessionID());
                        } else {
                            log.info("FAILED service cancellation!");
                        }
                    } else if (messageType.equals("RouteInfoRequest")) {
                        ++count_msgs_to_controller;
                          log.info("RouteInfoRequest received");
                        bwMsg.append(Integer.toString(count_msgs_to_controller));
                        bwMsg.newLine();
                        bwMsg.flush();
                        //log.info("count_msgs_to_controller=" + count_msgs_to_controller);

                      //  XMLUtility.getInstance().printXML(doc);
                        Element srcNode = (Element) doc.getElementsByTagName("srcNode").item(0);

                        String srcIP = srcNode.getAttribute("ip");
                        boolean isFirstNode = Boolean.parseBoolean((doc.getDocumentElement().getAttribute("isFirstNode")));
                        String srcSW = null;
                        short srcPort = 0;
                        if (isFirstNode == true) {
                            //log.info("resolving srcIP:" + srcIP);
                            Tuple srcInfo = handleIPResolution(srcIP);
                            srcSW = ((Long) srcInfo.swdpid).toString();
                            srcPort = (short) srcInfo.port;
                        } else {
                            srcSW = srcNode.getAttribute("switch");
                            srcPort = Short.parseShort(srcNode.getAttribute("outPort"));
                        }

                        long destSW = 0;
                        short destPort = 0;

                        Element destNode = (Element) doc.getElementsByTagName("destNode").item(0);
                        NodeList candidates = destNode.getElementsByTagName("candidate");
                        Element selectedNode = null;
                        int selectedPathLength = Integer.MAX_VALUE;

                     //   log.info("no of candidates=" + candidates.getLength());
                        for (int i = 0; i < candidates.getLength(); i++) {
                            Element cand = (Element) candidates.item(i);
                            destSW = Long.parseLong(cand.getAttribute("switch"));
                            destPort = Short.parseShort(cand.getAttribute("port"));

                            HttpExample restApi = new HttpExample();

                            String OFsrcSW = "";
                            String OFdestSW = "";
                            Document mappingOF = XMLUtility.getInstance().loadXML("OFSwitches.xml");
                            NodeList switches = mappingOF.getElementsByTagName("Switch");
                            for (int ind = 0; ind < switches.getLength(); ind++) {
                                Node node = switches.item(ind);
                                Element value = (Element) node;
                                String id = value.getAttribute("id");
                                String of = value.getAttribute("of");

                                if (srcSW.equals(id)) {
                                    OFsrcSW = of;
                                } else if (String.valueOf(destSW).equals(id)) {
                                    OFdestSW = of;
                                }
                            }

                          //   log.info("Source: "+OFsrcSW);
                            //   log.info("Destination: "+OFdestSW);
                           
                            ArrayList<Route> routes = restApi.getRoute(srcSW, String.valueOf(srcPort), String.valueOf(destSW), String.valueOf(destPort));
                            Route route = routes.get(0);
                           
                            //Route route = restApi.getRoute(srcSW, String.valueOf(srcPort), String.valueOf(destSW), String.valueOf(destPort));

                            //  Route route = routingEngine.getRoute(Long.parseLong(srcSW), srcPort, destSW, destPort, 0); //cookie = 0, i.e., default route
                            if (route != null) {
                              //  log.info("route of length " + route.getPath().size() + " found between " + srcSW + "," + srcPort
                                //    + " " + destSW + "," + destPort);
                                if (route.getPath().size() < selectedPathLength) { //should be true for the first time sinc selectedPathLength intialized to MAX
                                    selectedNode = cand;
                                    selectedPathLength = route.getPath().size();
                                }
                            } else {
                                log.info("Cannot find candidate route to switch " + destSW + " port " + destPort);
                            }
                        }

                        String ipAddress = null, portNum = null, outPortNum = null, switchID = null, vlid = null;
                        if (selectedNode != null) {
                            ipAddress = selectedNode.getAttribute("ip");
                            portNum = selectedNode.getAttribute("port");
                            outPortNum = selectedNode.getAttribute("outPort");
                            switchID = selectedNode.getAttribute("switch");
                            vlid = selectedNode.getAttribute("vlid");

                        //    log.info("Selected switch =" + switchID + " port/outPort=" + portNum + "/" + outPortNum
                            //    + " vlid=" + vlid);
                            FileWriter fw, fw2;
                            BufferedWriter bw, bw2;
                            FileReader fr;
                            BufferedReader br;

                            checkFile = new File("files/checkFile.txt");
                            fr = new FileReader(checkFile);
                            br = new BufferedReader(fr);
                            String str = br.readLine();
                            while (str == null || str.equals("0") == false) {
                                //    log.info("waiting for checkFile...");
                                Thread.sleep(20);
                                fr = new FileReader(checkFile);
                                br = new BufferedReader(fr);
                                str = br.readLine();
                                //    log.info("checkFile contains " + str);
                                br.close();
                                fr.close();
                            }

                            fw2 = new FileWriter(routeInfoFile, false);
                            bw2 = new BufferedWriter(fw2);

                            bw2.write(ipAddress);
                            bw2.newLine();
                            bw2.write(switchID);
                            bw2.newLine();
                            bw2.write(portNum);
                            bw2.newLine();
                            bw2.write(outPortNum);
                            bw2.newLine();
                            bw2.write(vlid);
                            //bw2.newLine();
                            //source info (found by the IPResolution)
                            bw2.newLine();
                            bw2.write(srcSW);
                            bw2.newLine();
                            bw2.write(srcPort);
                            bw2.newLine();
                            bw2.write(srcIP);
                            bw2.newLine();

                            bw2.close();
                            fw2.close();

                            fw = new FileWriter(checkFile, false);
                            bw = new BufferedWriter(fw);
                            bw.write("1");
                            bw.close();
                            fw.close();
                          //  log.info("checkFile is updated to 1");

                            //log.info("Route Info saved to RouteInfoReply.txt ");
                        }
                    } else {
                        log.info("Unknown message Type: " + messageType);
                    }

                    //w.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public DatagramSocket getDatagramSocket() {
        return dataSocket;
    }

    public BNSMessage receiveMsgFromDSE() {
       
       
        //   log.info("Waiting for messages ");
        BNSMessage msg = null;
        try {
            byte[] recvBuf = new byte[500000];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            dataSocket.receive(packet);
            packet.setLength(recvBuf.length);//bbbb // commented just now
               log.info("Message Received");
            int byteCount = packet.getLength();
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData()) //recvBuf
                    ) {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
                Object o = is.readObject();
                is.close();
                msg = (BNSMessage) o;
                if (msg == null) {
                    log.info("The object is not a message");
                } else {
                    //   log.info("The message is not null ");
                  //  log.info("message object hashcode = " + o.hashCode());

                    //XMLUtility.getInstance().printXML(msg.getValue());
                }
                byteStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            //System.err.println("Exception case in receiveMsgFromDSE, Exception:  " + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public void sendMsgToDSE(BNSMessage msg) {
        try {
            msg.setSrcHost(bnsName);
            msg.setSrcPort(port);
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(500000)) {
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                //os.flush();
                os.writeObject(msg);
                os.flush();
                byte[] sendBuf = byteStream.toByteArray();
                DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(msg.getDstHost()), msg.getDstPort());
                int byteCount = packet.getLength();
                //  log.info("packet length (sendMsgToDSE) = " + byteCount);
                dataSocket.send(packet);
                byteStream.close();
                os.close();
            }
          //  log.info("Message Sent to DSE " + dseHostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Exception:  " + e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendStatisticsToDSE(ConcurrentHashMap<Long, TupleStatistics<ArrayList<Object>, Long, Long>> statsModel, BNSMessage msg) { //modification of sendMsgToDSE

        try {
            if (!isDSEConnected) {
                dseSocket = new Socket((String) (/*dseipAddress.getText()*/"127.0.0.1"), Integer.parseInt((String) (/*dseport.getValue()*/"4048")));
                dseSocket.setKeepAlive(true);
                isDSEConnected = true;
                oos = new ObjectOutputStream(dseSocket.getOutputStream());//a modifier
                log.info("TCP socket established between BNServer and DSE ");

            }

            oos.flush();
            oos.writeObject(statsModel);
            oos.flush();
            oos = new AppendingObjectOutputStream(dseSocket.getOutputStream());

        } catch (IOException | NumberFormatException ex) {
           // log.info("exception case (TCP)");
            ex.printStackTrace();
            isDSEConnected = false;
        }

    }

    public void sendPerFlowStatisticsToDSE(BNSMessage msg) {

        try {

          //  log.info("isDSEConnected: "+isDSEConnected);
            if (!isDSEConnected) {
                dseSocket = new Socket((String) (/*dseipAddress.getText()*/"127.0.0.1"), Integer.parseInt((String) (/*dseport.getValue()*/"4048")));
                dseSocket.setKeepAlive(true);
                isDSEConnected = true;
                oos = new ObjectOutputStream(dseSocket.getOutputStream());//a modifier
                //    log.info("TCP socket established between BNServer and DSE ");

            }

            oos.flush();
            //oos.writeObject(statsModel);
            oos.writeObject(msg.getValue());
            oos.flush();
            oos = new AppendingObjectOutputStream(dseSocket.getOutputStream());

        } catch (IOException | NumberFormatException ex) {
          //  log.info("exception case (TCP)");
            ex.printStackTrace();
            isDSEConnected = false;
        }

    }

    protected void setText(Element el, String value) {
        el.appendChild(el.getOwnerDocument().createTextNode(value));
    }

    protected String getText(Element el) {
        return el.getFirstChild().getNodeValue();
    }

    protected Element getElement(Document doc, String name) {
        return (Element) (doc.getElementsByTagName(name).item(0));
    }

    /* private IDevice getHost(String hostAdd) {

     Collection<? extends IDevice> devices = device.getAllDevices();
     if (devices != null && devices.size() > 0) {
     for (IDevice d : devices) {
     //SwitchPort[] srcDevAps = d.getAttachmentPoints();
     List<Integer> vals = Arrays.asList(d.getIPv4Addresses());
     log.info("devices is null " + d.getIPv4Addresses().length);

     //String IPs = IPv4.fromIPv4AddressCollection(vals);
     String ip = IPv4.fromIPv4Address(vals.get(0));

     if (hostAdd.equals(ip)) {
     log.info("\tIP Address:" + ip);
     log.info("\tMAC Address:" + d.getMACAddressString());
     log.info("\tLast seen:" + d.getLastSeen().toString());
     return d;
     }
     }
     }

     return null;
     }*/
    /* private boolean flowSetUp2(String src, String dst, int actionType) throws IOException {

     log.info("Hi hi");
     //Thread.sleep(1000);
     log.info("no. of devices found:" + device.getAllDevices().size());

     IDevice srcDevice = getHost("192.168.200.1");//srcip
     IDevice dstDevice = getHost("192.168.200.2");//dstip

     if (srcDevice == null) {
     log.info("Source device not found");
     return false;
     }
     if (dstDevice == null) {
     log.info("Destination device  not found");
     return false;
     }
       
     SwitchPort[] srcDevAps = srcDevice.getAttachmentPoints();
     String ip1 = IPv4.fromIPv4Address(Arrays.asList(srcDevice.getIPv4Addresses()).get(0));
     String mac1 = srcDevice.getMACAddressString(); //mac1=a0:36:9f:0a:af:7f

     SwitchPort[] dstDevAps = dstDevice.getAttachmentPoints();
     String ip2 = IPv4.fromIPv4Address(Arrays.asList(dstDevice.getIPv4Addresses()).get(0));
     //ip2 = "192.168.200.100";
     String mac2 = dstDevice.getMACAddressString(); //mac2=68:05:ca:14:27:2a
     mac2 = "a0:36:9f:0a:af:78";
     int iSrcDaps = 0, iDstDaps = 0;

     while ((iSrcDaps < srcDevAps.length) && (iDstDaps < dstDevAps.length)) {
     SwitchPort srcDap = srcDevAps[iSrcDaps];
     SwitchPort dstDap = dstDevAps[iDstDaps];

     // srcCluster and dstCluster here cannot be null as
     // every switch will be at least in its own L2 domain.
     Long srcCluster
     = topology.getL2DomainId(srcDap.getSwitchDPID());
     Long dstCluster
     = topology.getL2DomainId(dstDap.getSwitchDPID());

     int srcVsDest = srcCluster.compareTo(dstCluster);
     if (srcVsDest == 0) {
     if (!srcDap.equals(dstDap)) {
   

     Route route
     = routingEngine.getRoute(srcDap.getSwitchDPID(),
     (short) srcDap.getPort(),
     dstDap.getSwitchDPID(),
     (short) dstDap.getPort(), 0); //cookie = 0, i.e., default route
     if (route != null) {
     //log.info("Route found!");
     List<NodePortTuple> switchPortList = route.getPath();
     for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {
     // indx and indx-1 will always have the same switch DPID.
     long switchDPID = switchPortList.get(indx).getNodeId();
                       
     short outPort = switchPortList.get(indx).getPortId();
     short inPort = switchPortList.get(indx - 1).getPortId();
     log.info("sw=" + switchDPID);
     log.info("port=" + inPort);
     //src node
     if (actionType == 0) {
     //addFlow2(sw, (short) inPort, (short) outPort, ip1, ip2, mac1, mac2);
     //addFlow2(sw, (short) outPort, (short) inPort, ip2, ip1, mac2, mac1);
     } else if (actionType == 1) {
     //deletFlow(sw, (short) inPort, (short) outPort);
     //deletFlow(sw, (short) outPort, (short) inPort);
     }

     }
     }
     }
     iSrcDaps++;
     iDstDaps++;
     } else if (srcVsDest < 0) {
     iSrcDaps++;
     } else {
     iDstDaps++;
     }
     }

     return true;
     }*/
    public List<NodePortTuple> setup(Segment segment, String identifier) throws IOException, InterruptedException, Exception {

        log.info("Switch and port OK: sw1=" + segment.swdpid + " sw2=" + segment.swdpid2 + " port1=" + segment.port + " port2=" + segment.port2);

        if ((segment.swdpid != segment.swdpid2) || (segment.swdpid == segment.swdpid2 && segment.port != segment.port2)) {

            return setFlow(identifier, segment.pathType, segment.direction, segment.origSrcSw, segment.origDstSw, segment.swdpid, segment.swdpid2, segment.port, segment.port2, segment.srcIP, segment.dstIP, segment.subnetMask, segment.actionType, segment.vlid_match, segment.vlid_action, segment.isLastNode);

        }

        return null;
    }

    public List<NodePortTuple> setFlow(String identifier, String pathType, String direction, long origSrcSw, long origDstSw, long swdpid, long swdpid2, short port, short port2, String srcIP, String dstIP, String subnetMask, int actionType, short vlid_match, short vlid_action, boolean isLastNode) throws IOException, InterruptedException, Exception {
       
       log.info("set Flow: getRoute!!");
        boolean first = true;
       
        HttpExample restApi = new HttpExample();

        List<NodePortTuple> NewswitchPortList = new ArrayList<>();
        ArrayList<String> NewIntents = new ArrayList<String>();
       
        ArrayList<String> idIntents = new ArrayList<String>();
        ArrayList<String> idFlows = new ArrayList<String>();
        ArrayList<String> idSwitches = new ArrayList<String>();

      //   log.info("F Source: "+String.valueOf(swdpid));
     //    log.info("F Destination: "+String.valueOf(swdpid2));
       
       // Route route = restApi.getRoute(String.valueOf(swdpid), String.valueOf(port), String.valueOf(swdpid2), String.valueOf(port2));
       
        ArrayList<Route> disjointRoutes = new ArrayList<>();
       
       /* if(swdpid == swdpid2)
        {
        disjointRoutes = restApi.getRoute(String.valueOf(swdpid), String.valueOf(port), String.valueOf(swdpid2), String.valueOf(port2));
        }
        else
        {
        disjointRoutes = restApi.getDisjointRoutes(String.valueOf(swdpid), String.valueOf(port), String.valueOf(swdpid2), String.valueOf(port2));
        }*/
        
        disjointRoutes = restApi.getRoute(String.valueOf(swdpid), String.valueOf(port), String.valueOf(swdpid2), String.valueOf(port2));
       
        if (disjointRoutes.isEmpty())
        {
            //DSE.count_REJECT_REDIRECTION = DSE.count_REJECT_REDIRECTION + 1;
            //writeFile();
            disjointRoutes = restApi.getRoute(String.valueOf(swdpid), String.valueOf(port), String.valueOf(swdpid2), String.valueOf(port2));
        }

        if (!disjointRoutes.isEmpty())
        {
            int index = 0;
            boolean routeFound = false;
           
            while((index < disjointRoutes.size()) && (!routeFound))
            {

              Route route = disjointRoutes.get(index);
             
         //   log.info("Route found!");
            List<NodePortTuple> switchPortList = route.getPath();

         //   log.info("flow is being setup for route with swport size " + switchPortList.size());
            //log.info("Route Length = "+route.getRouteCount());
            log.info("Route found: " + switchPortList.toString());
            log.info("DSE.overloadedSwitches.size() " + DSE.overloadedSwitches.size());
           
            boolean overloaded = false;
           
            for(int k=0; k<switchPortList.size(); k++)
            {
            long switchDPID = switchPortList.get(k).getNodeId();
            log.info("switchDPID: " + switchDPID);
            if(DSE.overloadedSwitches.contains(switchDPID))
            {
            overloaded=true;
            }
            }
           
            log.info("overloaded: " + overloaded);
            if(overloaded==true)//if route contains one of the overloaded switches
            {
                log.info("Route found passes across an overloaded switch!!");
            }
            else
            {
               
            routeFound = true;
            log.info("routeFound = true");
           
            short tempVlan = vlid_action;
            short temVlanMatch = vlid_match;

       //     log.info("switchPortList.size(): "+switchPortList.size());
            ArrayList<Object> tempCharac = (ArrayList<Object>) requestCharacteristics.get(identifier);

            if (tempCharac.isEmpty()) {
                //  log.info("first time to insert in tempCharac");
                NewswitchPortList = switchPortList;
            } else //if(!(tempCharac.get(0) == null))
            {
                first = false;
                List<NodePortTuple> OldswitchPortList = (List<NodePortTuple>) tempCharac.get(0);
                NewswitchPortList.addAll(OldswitchPortList);
                NewswitchPortList.addAll(switchPortList);
          //  log.info("NewswitchPortList.size(): "+NewswitchPortList.size());

            }

            ArrayList<String> installedIntents = new ArrayList<>();

            for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {

                log.info("switchPortList(indx): "+switchPortList.get(indx));
                boolean doStrip = false;

                if (indx > 2) {
                    /* vlid_action might have been set by ingress node and we need to use it
                     in the match of the subsequent flows of that route(except the ingress node itself)
                     */
                    if (vlid_action != 0) {
                        vlid_match = vlid_action;
                    }
                    vlid_action = 0; //avoids unnecessary modification of vlan id

                } else { //Modify vlan id ONLY once(at the begining) for a given route
                    vlid_action = tempVlan;
                    vlid_match = temVlanMatch;
                }

                if (pathType.equals("composite") && (indx == switchPortList.size() - 1) && (swdpid2 == origSrcSw || swdpid2 == origDstSw)) {
                    //set doStrip
                    doStrip = true;
                }

                // indx and indx-1 will always have the same switch DPID.
                long switchDPID = switchPortList.get(indx).getNodeId();

              //  IOFSwitch sw = controller.getSwitch(switchDPID);
             //   log.info("sw=" + switchDPID);
                short outPort = switchPortList.get(indx).getPortId();
                short inPort = switchPortList.get(indx - 1).getPortId();
           //     log.info("inPort=" + inPort);
            //    log.info("outPort=" + outPort);

                String Source = String.valueOf(srcIP);
                String Destination = String.valueOf(dstIP);

           //     log.info("addFlow in the flow table of the current switch!!!");
                String Switch = String.valueOf(switchPortList.get(indx - 1).getNodeId());
                String dstSW = String.valueOf(switchPortList.get(indx).getNodeId());

                log.info("Current Switch: " + Switch);
                log.info("Destination Switch: " + dstSW);

         //   log.info("inPort: "+String.valueOf(inPort));
                //    log.info("outPort: "+String.valueOf(outPort));
                String SourceMAC = "";
                String DestinationMAC = "";

                MACAddress ipMacSrc = getMACAddress(Source);

                MACAddress ipMacDest = getMACAddress(Destination);

                SourceMAC = ipMacSrc.toString();
                DestinationMAC = ipMacDest.toString();

        //    log.info("Source MAC: "+SourceMAC);
                //    log.info("Destination MAC: "+DestinationMAC);
                HttpExample http = new HttpExample();

                String currentIntent = "";

             //   log.info("send pointtopoint intent: " + Switch + " - " + dstSW);

                try {
                    //http.addFlow(Switch, String.valueOf(inPort), String.valueOf(outPort), Source, Destination);

     // currentIntent = http.sendPointToPointIntent(identifier, Switch, Switch, Source, Destination, SourceMAC, DestinationMAC, String.valueOf(inPort), String.valueOf(outPort));
   //  currentIntent = http.sendPointToPointIntent(identifier, Switch, dstSW, Source, Destination, SourceMAC, DestinationMAC, String.valueOf(inPort), String.valueOf(outPort));
                    String VLAN_ID = "";
                    String[] parts = identifier.split("-");
                    String reqID = parts[0];

                    VLAN_ID = reqID;

                    currentIntent = http.PointToPointIntent_VLAN(identifier, Switch, dstSW, Source, Destination, SourceMAC, DestinationMAC, VLAN_ID, String.valueOf(inPort), String.valueOf(outPort));

                    log.info("current Intent: "+currentIntent);
                   
                    installedIntents.add(currentIntent);
                   
                    String currentFlowId = "";
                           
                    if(!(currentIntent.equals("")))
                    {
//                    currentFlowId = http.getFlowIdFromIntent(currentIntent);
                        currentFlowId = " ";
                    }
                   
                   if(Constants.active_intents_per_request.containsKey(identifier))
                    {
                   
                    ArrayList<String> tmpIntents = (ArrayList<String>)Constants.active_intents_per_request.get(identifier);
                    tmpIntents.add(currentIntent);
                    Constants.active_intents_per_request.put(identifier, tmpIntents);
                   
                   
                    ArrayList<String> tmpFlows = (ArrayList<String>)Constants.active_flows_per_request.get(identifier);
                    tmpFlows.add(currentFlowId);
                    Constants.active_flows_per_request.put(identifier, tmpFlows);
                   
                    ArrayList<String> tmpSwitches = (ArrayList<String>)Constants.involved_switches_per_request.get(identifier);
                    tmpSwitches.add(Switch);
                    Constants.involved_switches_per_request.put(identifier, tmpSwitches);
                   
                    log.info("if Intent");
                    }
                    else
                    {
                    idIntents.add(currentIntent);
                    Constants.active_intents_per_request.put(identifier, idIntents);
                   
                    idFlows.add(currentFlowId);
                    Constants.active_flows_per_request.put(identifier, idFlows);
   
                    idSwitches.add(Switch);
                    Constants.involved_switches_per_request.put(identifier, idSwitches);
                   
                    log.info("else Intent");
                    }

                } catch (ProtocolException ex) {
                    Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(BNServer.class.getName()).log(Level.SEVERE, null, ex);
                }

               
               
            }

            //   log.info("installedIntents.size(): "+installedIntents.size());
            if (first) {
                //    log.info("first time to insert Intents in tempCharac");
                NewIntents = installedIntents;
            } else //if(!(tempCharac.get(1) == null))
            {

                ArrayList<String> OldIntents = (ArrayList<String>) tempCharac.get(1);

                NewIntents.addAll(OldIntents);
                NewIntents.addAll(installedIntents);
         //   log.info("NewIntents.size(): "+NewIntents.size());

            }

            tempCharac.clear();
            tempCharac.add(0, NewswitchPortList);
            tempCharac.add(1, NewIntents);
            requestCharacteristics.put(identifier, tempCharac);

            return switchPortList;
           
        }//else: route does not contains any overloaded switch
           
            index++;
           
            }//while on all the available disjoint routes
           
        }//if disjointRoutes is not empty
        else
        {log.info("Cant find path between " + swdpid + " and " + swdpid2);}
        return null;
    }

    /* private Map<String, Long> resolveIP(String givenIp) {
     Map<String, Object> row;
     Map<String, Long> swPort = null;
     if (Forwarding.storageSourceService != null) {

     String[] colNames = {"ip", "swPort"};

     IResultSet resultSet = storageSourceService.executeQuery(TABLE_NAME, colNames, null, null);
     for (Iterator<IResultSet> it = resultSet.iterator(); it.hasNext();) {
     row = it.next().getRow();
     String ip = (String) row.get("ip");
     log.info("given ip=" + givenIp + " ip=" + ip);
     if (givenIp.equals(ip)) {
     swPort = (Map) row.get("swPort");
     log.info("check ServiceDelivery ip=" + ip + " sw=" + swPort.get("sw"));
     break;
     }

     }

     } else {
     log.info("storage null");
     }
     return swPort;
     }*/
    
    public ArrayList<Object> collectStatistics(Long swId) throws InterruptedException, TimeoutException, ExecutionException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, Exception {

        Map<Long, Map<Short, Map<String, Long>>> swObj = new HashMap<>();
        //Set<Long> switchDpids = controller.getAllSwitchDpids();
        Map<Long, Float> swThr = new HashMap<>();
        float avg_thoughput = 0;

        ArrayList<Object> stats = new ArrayList<>();

        Object portStatsReply, flowStatsReply;
        Map<Short, Map<String, Long>> prtSt = new HashMap<Short, Map<String, Long>>();
        Map<String, Long> txrxBytes = new HashMap<>();
        long currFlowBytes = 0;
        long currPortBytes = 0;
        try {

            log.info("get FLOW stats!!");
            HttpExample http = new HttpExample();

            ArrayList<Object> flowEntries = new ArrayList<>();
            flowEntries = http.getFlowStats(swId);

            for (int ind = 0; ind < flowEntries.size(); ind++) {

                String flEnt = flowEntries.get(ind).toString();
                 log.info("current Flow Entry: "+flEnt);  
                Pattern pattern = Pattern.compile("\"bytes\":(.*?),");
                Matcher matcher = pattern.matcher(flEnt);
                if (matcher.find()) {
                    // log.info("matcher: "+matcher.group(1));
                    currFlowBytes += Long.parseLong(matcher.group(1));
                } else {
                    currFlowBytes += 0;
                }
               log.info("currFlowBytes: "+currFlowBytes);            

            }

            stats.add(flowEntries);

            long total_bytes = 0;
            long bytes_prev = 0;
            long bytes_in_period = 0;

            if (mapBytes.containsKey(swId)) {
                Set<Long> sKeys = mapBytes.keySet();
                for (long sKey : sKeys) {
                    if (sKey == swId) {
                        bytes_prev = mapBytes.get(swId);
                        mapBytes.replace(swId, currFlowBytes);

                        total_bytes = bytes_prev + currFlowBytes;

                        mapTotalBytes.replace(swId, total_bytes);
                        break;
                    }
                }
            } else {
                total_bytes = currFlowBytes;
                mapBytes.put(swId, currFlowBytes);
                mapTotalBytes.put(swId, total_bytes);
            }
            bytes_in_period = currFlowBytes - bytes_prev;
            total_bytes = currFlowBytes;
            //bytes_in_period = currFlowBytes - bytes_prev;

            stats.add(currFlowBytes);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            //log.info("Something went wrong in collectStatistics");
        }

        return stats;
    }
    
    private Tuple handleIPResolution(String givenIP) throws InterruptedException {
        Tuple tpl = null;
        //synchronized (MACTracker.macTable) { //to avoid java.util.ConcurrentModificationException
        Set<Integer> keys = macTable.keySet();
        for (int key : keys) {
            Tuple entry = macTable.get(key);
            String ip = IPv4.fromIPv4Address((int) entry.ip);
            MACAddress macAdd = MACAddress.valueOf((long) entry.mac);
            long swdpid = (long) entry.swdpid;
            short prt = (short) entry.port;
          //  log.info("MAC=" + macAdd + " IP=" + ip /*IPv4.fromIPv4Address(ip)*/
                 //   + " sw=" + swdpid + " port=" + prt);

            if (ip.equals(givenIP)) {
                tpl = entry;
                break;
            }
            //}
        }
        if (tpl == null) {//retry
            //  log.info("Retry resolving IP address:" + givenIP);
            //Thread.sleep(10);
            return handleIPResolution(givenIP);
        }
        return tpl;
    }

    private MACAddress getMACAddress(String givenIP) throws InterruptedException {
        Tuple tpl = null;
        MACAddress macAdd = null;
        //synchronized (MACTracker.macTable) { //to avoid java.util.ConcurrentModificationException
        Set<Integer> keys = macTable.keySet();
        for (int key : keys) {
            Tuple entry = macTable.get(key);
            String ip = IPv4.fromIPv4Address((int) entry.ip);
            macAdd = MACAddress.valueOf((long) entry.mac);
            long swdpid = (long) entry.swdpid;
            short prt = (short) entry.port;
          //  log.info("MAC=" + macAdd + " IP=" + ip /*IPv4.fromIPv4Address(ip)*/
            //  + " sw=" + swdpid + " port=" + prt);

            if (ip.equals(givenIP)) {
                tpl = entry;
                break;
            }
            //}
        }
        return macAdd;
    }
   
   
        /************************************************************************************************/
    /**
     * @throws java.io.IOException**********************************************************************************************
       
       public void writeFile() throws IOException  {
          
        FileWriter fwMsg = new FileWriter("Statistics/rejectRedirection.txt", true);
        BufferedWriter reject = new BufferedWriter(fwMsg);
        reject.append(Integer.toString(DSE.count_REJECT_REDIRECTION));
        reject.newLine();
        reject.flush();
   
    }

    /************************************************************************************************/

}

