package DSE;

import Controller.Ethernet;
import Controller.IPv4;
import Controller.MACAddress;
import Manager.*;
import Message.*;
import DSE.BNServer.TupleStatistics;
import StateListener.StateListenerNew;
import Utility.*;
import ae.ApplicationEntity;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import rest.HttpExample;


public class DSE {

    public volatile Document docSource = null;  
    public volatile int execute = 0;
    
    protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    public volatile Document tab[] = new Document[100];

    public int value = 0;

    public static long startTime = 0;
    public static long endTime = 0;
    boolean flag = false;
    long latency = 0;
    BufferedWriter w;

    private UDPManager dseManager;
    public UDPManager bnsManager;
    private TCPManager aeManager;

    private Hashtable fsmList;
    private PolicyDB polDB;
    
    private static MiddleboxDB mbDB;

    private String hostName;
    File routeInfoFile, checkFile, checkPath;
    boolean isCheckPathModified, isCheckPathModified2 = false;
    File pathFile2;
    private InetAddress host;
    private TrafficGenerator tg;

    File pathFile;
    static FileWriter pathFW;
    static BufferedWriter pathBW;
    static FileReader pathFR;
    static BufferedReader pathBR;

    public static int NUMBER_OF_SWITCHES_WITH_MIDDLEBOX = 5;
    public static ArrayList<Long> switchesWithMiddlebox = new ArrayList<>();//2, 4, 6, 8
    
    public static ArrayList<Long> switchesWithMiddleboxPerFlow = new ArrayList<>();//2, 4, 6, 8
    public static ConcurrentHashMap<Integer, Document> received_requests_doc = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Long> received_requests_time = new ConcurrentHashMap<>();
   // public static int id_received_request = 1;
    
   // public static ConcurrentHashMap<String, Double> current_bytes_flow = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Double> current_bytes_flow_forward = new ConcurrentHashMap<>();
  //  public static ConcurrentHashMap<String, Double> current_bytes_flow_backward = new ConcurrentHashMap<>();
    
    public static boolean doBlock = false;
    ArrayList<ArrayList<Element>> allNewPaths;
    CopyOnWriteArrayList<Message> msgList2;

    private final boolean doRedirection = true;
    private final boolean doRedirectionFlow = true;

    public static ConcurrentHashMap<Long, TupleStatistics<ArrayList<String>, Long, Long>> statsModel;
    public static Set<Long> overloadedSwitches = new HashSet<>();
    
    public static Set<String> involvedSwitches = new HashSet<>();

    public static int count_accepted_requests = 0;
    public static ConcurrentHashMap<String, String> accepted_requests, accepted_requests_tmp;
    public static CopyOnWriteArrayList<String> active_requests, active_requests_tmp;
    public static ArrayList<String> generated_requests   = new ArrayList<>();
    public static int count_req = 0;
    public static int count_rejected_req = 0;
    boolean isLoadEvaluated, isRatioEvaluated = false;
    List<HashMap<String, ArrayList<String>>> deletedEntries;
    ArrayList<Element> newPaths;
    HashMap<String, HashMap<String, String>> matchEntries;
    boolean flag_remove = false;

    FileWriter fwNumFlowEntriesSw4;
    BufferedWriter bwNumFlowEntriesSw4;
    ArrayList<BufferedWriter> bwNumBytes;
    
    ArrayList<String> requestsToResend = new ArrayList<String>();
     
    // static boolean received = false;

    static double totalRedirTime = 0d;
    static int countRedirection = 0;
    
    final static long threshold = 500;//1Gb
    final static int requestedSLA = 10000;//100Mb   => Guarantee at least this value
    
    static double totalFLOWRedirTime = 0d;
    
    int count = 0;
    
    public static File restFile;
    public static int count_REST_Messages = 0;

    public static Element srcNode_rInfo;
    public static Element dstNode_rInfo;

    private static volatile DSE dseClass;
    private static final Object dseLock = new Object();

    private ApplicationEntity aeContainer;
    
    public static ConcurrentHashMap<String, String> currentIntents = new ConcurrentHashMap<>();
    
    public static ConcurrentHashMap<String, Integer> requestsSLA = new ConcurrentHashMap<>();
    
    public static ConcurrentHashMap<String, Double> throughput_per_switch = new ConcurrentHashMap<>();
    
    public static ConcurrentHashMap<String, Tuple> macTable = new ConcurrentHashMap<>();
    
    public static A a = new A();
    public static A a2 = new A();
    // public static ConcurrentHashMap<String, Tuple> macTable = new ConcurrentHashMap<>();
    
    public static ConcurrentHashMap<String, String> values_for_class_A = new ConcurrentHashMap<>();//classes A,B,C introduced to add more depth
    public static ConcurrentHashMap<String, String> values_for_class_B = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> values_for_class_C = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> values_for_class_D = new ConcurrentHashMap<>();
    
    public StateListenerNew sl;

    private DSE() throws IOException {

        restFile = new File("results/restMsg.txt");
 
        fsmList = new Hashtable();
        Config conf = Config.getInstance();
        hostName = conf.getString("hostname");

        polDB = new PolicyDB();
        mbDB = new MiddleboxDB();
      
        dseManager = new UDPManager(this, hostName, conf.getInt("DSEPort"));
        dseManager.start();
        
        bnsManager = new UDPManager(this, hostName, conf.getInt("BNSPort"));        
        bnsManager.start();

        aeManager = new TCPManager(this, hostName, conf.getInt("AEPort"));
        aeManager.start();

        //host = InetAddress.getByName("192.168.56.102");//192.168.56.1
        host = InetAddress.getByName("127.0.0.1");
        // host = InetAddress.getByName("192.168.50.101");//per silvia
      
        // tg = new TrafficGenerator(host, 50000);
        

        msgList2 = new CopyOnWriteArrayList<Message>();

        //timeout();
        value = 1;
        checkFile = new File("files/checkFile.txt");
        routeInfoFile = new File("files/RouteInfoReply.txt");

        fwNumFlowEntriesSw4 = new FileWriter("results/numFlowEntriesSw4", true);
        bwNumFlowEntriesSw4 = new BufferedWriter(fwNumFlowEntriesSw4);

        bwNumBytes = new ArrayList<>();
        for (int swNo = 1; swNo <= 14; swNo++) {
            FileWriter fw = new FileWriter("results/numBytes" + swNo + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bwNumBytes.add(bw);
        }

        
        switchesWithMiddlebox.add((long) 2);
        switchesWithMiddlebox.add((long) 4);
        switchesWithMiddlebox.add((long) 6);
        switchesWithMiddlebox.add((long) 8);
        switchesWithMiddlebox.add((long) 10);
                
       /* switchesWithMiddleboxPerFlow.add((long) 3);
        switchesWithMiddleboxPerFlow.add((long) 7);
        switchesWithMiddleboxPerFlow.add((long) 11);
      /*  switchesWithMiddleboxPerFlow.add((long) 8);
       switchesWithMiddleboxPerFlow.add((long) 9);
       switchesWithMiddleboxPerFlow.add((long) 10);*/

        accepted_requests = new ConcurrentHashMap<>();
        active_requests = new CopyOnWriteArrayList<>();

        deletedEntries = new ArrayList<HashMap<String, ArrayList<String>>>();
        
        /******STATE LISTENER********/
        sl = new StateListenerNew(this);

    }
    
    public A initialize() {
            
        log.info("we are in initialize "+System.nanoTime());

        //A a = new A();
        
        a.b = new B();
        a.c = new C();
        
        //foglie di A
        a.fol1 = 1;
        a.fol2 = "Str3";
        
        //A contiene B + foglie di B
        a.b.fol1 = 10;
        a.b.fol2 = "Application";
        //a.b.listaA = new ArrayList<A>();
        
        A lis1 = new A();A lis2 = new A();A lis3 = new A();A lis4 = new A();
        //errorlis4 = null;
        //errorlis3 = null;
        //error
        lis3.b = new B();//null;
        lis3.c = new C();//null;
        lis3.fol1 = 10;
        lis3.fol2 = "test1";
        lis4.b = new B();//null;
        lis4.c = new C();//null;
        lis4.fol1 = 10;
        lis4.fol2 = "test2";
        
        
        lis2.b = new B();//null;
        lis2.c = new C();//null;
        lis2.fol1 = 10;
        lis2.fol2 = "test3";
        lis1.b = new B();//null;
        lis1.c = new C();//null;
        lis1.fol1 = 40;
        lis1.fol2 = "tes";
        a.b.listaA.add(lis1);
        a.b.listaA.add(lis2);
        a.b.listaA.add(lis3);
        a.b.listaA.add(lis4); 
        
        //A contiene C + foglie di C
        a.c.fol1 = 20;
        a.c.fol2 = "test4";
        a.c.fol3 = "orchestrator";
        
        a.c.a = new A();
        
        //C contiene A
        a.c.a.fol1 = 25;
        a.c.a.fol2 = "Applic1"; 
        
        a.c.a.c = new C();
        
        a.c.a.c.fol1 = 30;
        a.c.a.c.fol2 = "req";
        a.c.a.c.fol3 = "usr";
        
        a.c.a.c.a = new A();
        
        a.c.a.c.a.fol1 = 44;
        a.c.a.c.a.fol2 = "foll1";
        a.c.a.c.a.b = new B();//null;
        a.c.a.c.a.c = new C();//null;
        
        a.c.a.b = new B();
        
        a.c.a.b.fol1 = 50;
        a.c.a.b.fol2 = "foll2";
        
        /*************************/
        
        a.c.a.b.c = new C();
        
        a.c.a.b.c.a = new A();//null;
        a.c.a.b.c.fol1 = 66 ;
        a.c.a.b.c.fol2 = "json";
        a.c.a.b.c.fol3 = "js";
                
      //  a.c.a.b.listaA = new ArrayList<A>();
        A liss1 = new A(); A liss2 = new A(); A liss3 = a;
        
        liss2.fol1 = 11;
        liss2.fol2 = "liss";
    //    liss2.b = null;
     //   liss2.c = null;
        
        liss1.fol1 = 77;
        liss1.fol2 = "list1";
        
        liss1.b = new B();
        liss1.c = new C();
        
     //   liss1.b.listaA = new ArrayList<A>();
        A lista1 = new A();A lista2 = new A();A lista3 = new A();
        

        lista3.fol1 = 45;
        lista3.fol2 = "lista1";
        lista3.b = new B();//null;
        lista2.fol1 = 45;
        lista2.fol2 = "lista11";
        lista2.b = new B();//null;
        
        
        lista1.fol1 = 45;
        lista1.fol2 = "lista111";
        lista1.b = new B();//null;
        
        lista1.c = new C(); 
        lista1.c.fol1 = 96;
        lista1.c.fol2 = "96";
        lista1.c.fol3 = "lista3";
        lista1.c.a = new A();//null;
        
        liss3.b = new B();
        liss3.b.fol1 = 50;
        
        liss1.b.fol1 = 54;
        liss1.b.fol2 = "final";
                
        liss1.b.listaA.add(lista1);
        liss1.b.listaA.add(lista2);
        liss1.b.listaA.add(lista3);
        
        
        
        liss1.c.fol1 = 58;
        
        liss1.c.fol2 = "foglia";
        liss1.c.fol3 = "last";
        
        liss1.c.a = new A();
        liss1.c.a.fol1 = 67;
        liss1.c.a.fol2 = "tor";
        liss1.c.a.b = new B();//null;
        liss1.c.a.c = new C();//null;        
                
        a.c.a.b.listaA.add(liss1);
        a.c.a.b.listaA.add(liss2);
        a.c.a.b.listaA.add(liss3);
        
         
        return a;

    }

    public void init(String[] args){


         //initialize();
        initializeFull();
         makeHostsDetectable();


    }

    public void initializeFull() {

        String jsonString = "{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":null,\"c\":null,\"fol1\":67," +
                "\"fol2\":\"tor\"},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[],\"c\":null},\"c\":{\"a\":null," +
                "\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":77,\"fol2\":\"list1\"}," +
                "{\"c\":{\"a\":null,\"c\":null,\"fol1\":1},\"fol1\":11,\"fol2\":\"liss\"},{\"c\":{\"a\":null," +
                "\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]}," +
                "\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]},\"c\":{\"a\":null," +
                "\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"},\"fol1\":30," +
                "\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":null,\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"}," +
                "\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]},\"c\":{\"a\":{\"b\":null,\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]}," +
                "\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"c\":{\"a\":{\"b\":null,\"c\":null,\"fol1\":44,\"fol2\":\"foll1\"},\"c\":{\"a\":null,\"fol1\":20," +
                "\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"}," +
                "\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"}," +
                "\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":50,\"fol2\":\"test\"," +
                "\"listaA\":[{\"b\":{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":{\"fol1\":54," +
                "\"fol2\":\"final\",\"listaA\":[],\"c\":null},\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"," +
                "\"a\":null}},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":45,\"fol2\":\"lista111\"," +
                "\"b\":null,\"c\":null},{\"fol1\":46,\"fol2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"fol1\":96," +
                "\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}},\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\"," +
                "\"b\":null},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":77,\"fol2\":\"list1\"}," +
                "{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null}},\"fol1\":11,\"fol2\":\"liss\"}," +
                "{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"fol1\":44," +
                "\"fol2\":\"foll1\",\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":25," +
                "\"fol2\":\"Applic1\"}],\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}}," +
                "\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"," +
                "\"b\":null}}},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}," +
                "{\"b\":{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":{\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[],\"c\":null},\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}}," +
                "\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":45,\"fol2\":\"lista111\"," +
                "\"b\":null,\"c\":null},{\"fol1\":46,\"fol2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"fol1\":96," +
                "\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}},\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\"," +
                "\"b\":null},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":77,\"fol2\":\"list1\"}," +
                "{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null}},\"fol1\":11,\"fol2\":\"liss\"}," +
                "{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"fol1\":44," +
                "\"fol2\":\"foll1\",\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":25," +
                "\"fol2\":\"Applic1\"}],\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}}," +
                "\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"," +
                "\"b\":null}}},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]}," +
                "\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":67,\"fol2\":\"tor\"}," +
                "\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null},\"fol1\":58,\"fol2\":\"foglia\"," +
                "\"fol3\":\"last\"},\"fol1\":1,\"fol2\":\"list1\",\"b\":{\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[{\"fol1\":45,\"fol2\":\"lista111\",\"b\":null,\"c\":null},{\"fol1\":46," +
                "\"fol2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"," +
                "\"a\":null}}},{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null}},\"fol1\":11," +
                "\"fol2\":\"liss\"},{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":3,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"a\":null," +
                "\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[{\"fol1\":25,\"fol2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":null,\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":null,\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":54," +
                "\"fol2\":\"final\",\"listaA\":[{\"fol1\":25,\"fol2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":{\"c\":null," +
                "\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]},\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":96," +
                "\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":54," +
                "\"fol2\":\"final\",\"listaA\":[{\"fol1\":25,\"fol2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":null," +
                "\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]}," +
                "\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"c\":null,\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":30,\"fol2\":\"req\"," +
                "\"fol3\":\"usr\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}," +
                "\"a2\":{\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[]},\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":67," +
                "\"fol2\":\"tor\"},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"fol1\":77,\"fol2\":\"list1\",\"b\":{\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[{\"fol1\":45,\"fol2\":\"lista111\",\"b\":null,\"c\":null},{\"fol1\":46," +
                "\"fol2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"," +
                "\"a\":null}},\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null},\"fol1\":58,\"fol2\":\"foglia\"," +
                "\"fol3\":\"last\"}},{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null}},\"fol1\":11," +
                "\"fol2\":\"liss\"},{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"a\":null," +
                "\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[{\"fol1\":25,\"fol2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":null,\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":null,\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":54," +
                "\"fol2\":\"final\",\"listaA\":[{\"fol1\":25,\"fol2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":{\"c\":null," +
                "\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]},\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":96," +
                "\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":54," +
                "\"fol2\":\"final\",\"listaA\":[{\"fol1\":25,\"fol2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":null," +
                "\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]}," +
                "\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"c\":null,\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":30,\"fol2\":\"req\"," +
                "\"fol3\":\"usr\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"b\":{\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":null,\"c\":null," +
                "\"fol1\":67,\"fol2\":\"tor\"},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50," +
                "\"fol2\":\"foll2\",\"listaA\":[{\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[],\"c\":null}," +
                "\"c\":{\"a\":null,\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":77," +
                "\"fol2\":\"list1\"},{\"c\":{\"a\":null,\"c\":null,\"fol1\":1},\"fol1\":11,\"fol2\":\"liss\"}," +
                "{\"c\":{\"a\":null,\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":1," +
                "\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]}," +
                "\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":null,\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"}," +
                "\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]},\"c\":{\"a\":{\"b\":null,\"fol1\":25," +
                "\"fol2\":\"Applic1\"},\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":null,\"fol1\":54,\"fol2\":\"final\",\"listaA\":[]}," +
                "\"c\":{\"a\":null,\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"},\"fol1\":44,\"fol2\":\"foll1\"}," +
                "\"c\":{\"a\":{\"b\":null,\"c\":null,\"fol1\":44,\"fol2\":\"foll1\"},\"c\":{\"a\":null,\"fol1\":20," +
                "\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"}," +
                "\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"}," +
                "\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":50,\"fol2\":\"test\"," +
                "\"listaA\":[{\"b\":{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":{\"fol1\":54," +
                "\"fol2\":\"final\",\"listaA\":[],\"c\":null},\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\"," +
                "\"a\":null}},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":45,\"fol2\":\"lista111\"," +
                "\"b\":null,\"c\":null},{\"fol1\":46,\"fol2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"fol1\":96," +
                "\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}},\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\"," +
                "\"b\":null},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":77,\"fol2\":\"list1\"}," +
                "{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null}},\"fol1\":11,\"fol2\":\"liss\"}," +
                "{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"fol1\":44," +
                "\"fol2\":\"foll1\",\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":25," +
                "\"fol2\":\"Applic1\"}],\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}}," +
                "\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"," +
                "\"b\":null}}},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}," +
                "{\"b\":{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":{\"fol1\":54,\"fol2\":\"final\"," +
                "\"listaA\":[],\"c\":null},\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}}," +
                "\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":45,\"fol2\":\"lista111\"," +
                "\"b\":null,\"c\":null},{\"fol1\":46,\"fol2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"fol1\":96," +
                "\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}},\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\"," +
                "\"b\":null},\"fol1\":58,\"fol2\":\"foglia\",\"fol3\":\"last\"},\"fol1\":77,\"fol2\":\"list1\"}," +
                "{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\",\"b\":null}},\"fol1\":11,\"fol2\":\"liss\"}," +
                "{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"fol1\":44," +
                "\"fol2\":\"foll1\",\"b\":{\"fol1\":54,\"fol2\":\"final\",\"listaA\":[{\"fol1\":25," +
                "\"fol2\":\"Applic1\"}],\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":null}}," +
                "\"c\":{\"fol1\":96,\"fol2\":\"96\",\"fol3\":\"lista3\",\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"," +
                "\"b\":null}}},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}]}," +
                "\"fol1\":1,\"fol2\":\"Str3\"},\"throughput_per_switch\":[{\"id\":\"1\",\"tp\":823},{\"id\":\"2\"," +
                "\"tp\":655},{\"id\":\"3\",\"tp\":830},{\"id\":\"4\",\"tp\":634},{\"id\":\"5\",\"tp\":533}," +
                "{\"id\":\"6\",\"tp\":596},{\"id\":\"7\",\"tp\":802}],\"macTable\":[{\"id\":\"1\"," +
                "\"ip\":\"10.0.0.1\",\"mac\":\"00:00:00:00:00:11\"},{\"id\":\"2\",\"ip\":\"10.0.0.2\"," +
                "\"mac\":\"00:00:00:00:00:22\"},{\"id\":\"3\",\"ip\":\"10.0.0.3\",\"mac\":\"00:00:00:00:00:33\"}," +
                "{\"id\":\"4\",\"ip\":\"10.0.0.4\",\"mac\":\"00:00:00:00:00:44\"},{\"id\":\"5\",\"ip\":\"10.0.0.5\"," +
                "\"mac\":\"00:00:00:00:00:55\"},{\"id\":\"6\",\"ip\":\"10.0.0.6\",\"mac\":\"00:00:00:00:00:66\"}," +
                "{\"id\":\"7\",\"ip\":\"10.0.0.7\",\"mac\":\"00:00:00:00:00:77\"}]}";

        String jsonStringSmall = "{\"a\":{\"b\":{\"c\":{},\"fol1\":50,\"fol2\":\"test\",\"listaA\":[]}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"fol1\":66,\"fol2\":\"json\",\"fol3\":\"js\"},\"fol1\":50,\"fol2\":\"foll2\"," +
                "\"listaA\":[{\"c\":{\"a\":{\"fol1\":67,\"fol2\":\"tor\"},\"fol1\":58,\"fol2\":\"foglia\"," +
                "\"fol3\":\"last\"},\"fol1\":77,\"fol2\":\"list1\"},{\"c\":{\"a\":{}},\"fol1\":11,\"fol2\":\"liss\"}," +
                "{\"c\":{\"a\":{\"fol1\":25,\"fol2\":\"Applic1\"},\"fol1\":20,\"fol2\":\"test4\"," +
                "\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}]},\"c\":{\"a\":{\"fol1\":44," +
                "\"fol2\":\"foll1\"},\"fol1\":30,\"fol2\":\"req\",\"fol3\":\"usr\"},\"fol1\":25,\"fol2\":\"Applic1\"}," +
                "\"fol1\":20,\"fol2\":\"test4\",\"fol3\":\"orchestrator\"},\"fol1\":1,\"fol2\":\"Str3\"}}";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonStringSmall);
            this.a = mapper.convertValue(jsonNode.get("a"), A.class);
            /*
            this.a2 = mapper.convertValue(jsonNode.get("a2"), A.class);
            for (JsonNode n : jsonNode.get("macTable")) {
                Tuple s = new Tuple<>(n.get("mac").asText(), n.get("ip").asText(), n.get("id").asInt(), null);
                this.macTable.put(n.get("id").asText(), s);
            }
            for (JsonNode n : jsonNode.get("throughput_per_switch")) {
                this.throughput_per_switch.put(String.valueOf(n.get("id").asInt()), n.get("tp").asDouble());
            }
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    //singleton class
    public static DSE getInstance() throws IOException {
        synchronized (dseLock) {
            if (dseClass == null) {
               // System.out.println("instaniating dseClass  ");
                dseClass = new DSE();
            }
        }
        return dseClass;
    }
    
        public class Tuple<MAC, IP, SWDPID, PORT> {
        // public class Tuple<String, String, SWDPID, PORT> {
        public MAC mac;
        //public String mac;
        public IP ip;
        //public String ip;
        public SWDPID swdpid;
        public PORT port;

       public Tuple(MAC mac, IP ip, SWDPID swdpid, PORT port) {
        //public Tuple(String mac, String ip, SWDPID swdpid, PORT port) {
            this.mac = mac;
            this.ip = ip;
            this.swdpid = swdpid;
            this.port = port;
        }
        
        public Tuple(){}
    }

          Tuple<Long, Integer, Long, Short> ipMacEntry;
         //Tuple<String, String, Long, Short> ipMacEntry;
          
    
        private void makeHostsDetectable() {
     
        Document MACTable = XMLUtility.getInstance().loadXML("MACTable.xml");
        //XMLUtility.getInstance().printXML(MACTable);
        NodeList macEntries = MACTable.getElementsByTagName("MacEntries");
        Element macEntries2 = (Element) macEntries.item(0);
        NodeList entries = macEntries2.getElementsByTagName("MacEntry");

        //push ARP entries
        for (int i = 0; i < 7; i++) {
            Element entry = (Element) entries.item(i);
            int ip = IPv4.toIPv4Address(entry.getAttribute("ip"));
            //String ip = entry.getAttribute("ip");
            Long mac = Ethernet.toLong(MACAddress.valueOf(entry.getAttribute("mac")).toBytes());
            //String mac = entry.getAttribute("mac");
            Long swdpid = Long.parseLong(entry.getAttribute("switch"));
            Short port = Short.parseShort(entry.getAttribute("port"));
            MACAddress macAdd = MACAddress.valueOf(mac);
            //macBytes = macAdd.toBytes();
          
            ipMacEntry = new Tuple<>(mac, ip, swdpid, port);
            
            System.out.println("LARA > ipMacEntry "+ipMacEntry.ip + " " +ipMacEntry.mac);

            //if (macTable.putIfAbsent(ip, ipMacEntry) == null) {//return null if key didnt exist before
            if (macTable.putIfAbsent(String.valueOf(i+1), ipMacEntry) == null) {//return null if key didnt exist before
                  System.out.println("Host detected ip = " + ip);
            } else {
                System.out.println("Host already detected");//key already exists
            }
        }

    }

    public String getHostName() {
        return hostName;
    }

    public PolicyDB getPolicyDB() {
        return polDB;
    }

    public static MiddleboxDB getMiddleboxDB() {
        return mbDB;
    }

    public File getRouteInfoFile() {
        return routeInfoFile;
    }

    public File getCheckFile() {
        return checkFile;
    }

    public File getPathFile() {
        return pathFile2;
    }

    public void placeMiddleboxes() {
        
        HashMap<String, HashMap<String, Object>> swServiceMap = new HashMap<String, HashMap<String, Object>>(); // stores a key constructed from swid and servicetype
        String swID = null;
        int nSwitches = switchesWithMiddlebox.size();
         
        log.info(nSwitches + " switches available for middlebox placement");
        
        if (nSwitches > 0) {
            ArrayList<Long> switchesToBeSelected = new ArrayList<>();
            Random r = new Random();
            for (int s = 0; s < nSwitches; s++) {
                long randomSWID;
                int randomIndex = r.nextInt(nSwitches);//intially 5

                randomSWID = switchesWithMiddlebox.get(randomIndex);
                
                while (switchesToBeSelected.contains(randomSWID)) {
                    randomIndex = r.nextInt(switchesWithMiddlebox.size()); //initially 5
                    randomSWID = switchesWithMiddlebox.get(randomIndex);
                }
                switchesToBeSelected.add(randomSWID);
                //log.info("randomSWID : "+randomSWID);
            }
            
            //log.info("switchesToBeSelected.size(): "+switchesToBeSelected.size());
            Document doc = XMLUtility.getInstance().createDocument();
            Element root = doc.createElement("SwitchMBoxes");
            doc.appendChild(root);

            Document mBox = XMLUtility.getInstance().loadXML("Services.xml");
            NodeList nodelist = mBox.getElementsByTagName("Services");

          //  System.out.println("nodelist.getLength(): "+nodelist.getLength());
            boolean found;
            for (int i = 0; i < nodelist.getLength(); i++) {
                Element e = (Element) nodelist.item(i);
                NodeList ch = e.getElementsByTagName("Service");
                for (int j = 0; j < ch.getLength(); j++) {
                    Element m = (Element) ch.item(j);
                    String mbType = m.getAttribute("type");
                    String instance = m.getAttribute("vlid");

                    int randInd = r.nextInt(nSwitches);
                    swID = String.valueOf(switchesToBeSelected.get(randInd));

                    if (overloadedSwitches.contains(switchesToBeSelected.get(randInd))) {
                        log.info("skipping overloaded Switch " + swID);
                        continue;
                    }
                    if (swServiceMap.containsKey(mbType)
                            && swServiceMap.get(mbType).values().size() < nSwitches
                            && swServiceMap.get(mbType).containsKey(swID)) {
                        found = false;
                        while (swServiceMap.get(mbType).values().size() < nSwitches
                                && swServiceMap.get(mbType).containsKey(swID)) {
                            randInd = r.nextInt(nSwitches);
                            swID = String.valueOf(switchesToBeSelected.get(randInd));
                        }
                        found = true;
                    } else {
                        found = true;
                    }
                    if (found) {
                        if (swServiceMap.containsKey(mbType)) {
                            swServiceMap.get(mbType).put(swID, null);
                        } else {
                            HashMap<String, Object> mp = new HashMap<>();
                            mp.put(swID, null);
                            swServiceMap.put(mbType, mp);
                        }

                        Element swMbox = doc.createElement("SwitchMBox");
                        swMbox.setAttribute("switch", swID);
                        
                     /*   String porta1 = "";String porta2 = "";
                         switch (swID)
                            {
                            case "2":
                                 porta1 = "1";porta2 = "3";
                            break;
                            case "4":
                                porta1 = "3";porta2 = "2";
                            break;
                            case "6":
                                porta1 = "2";porta2 = "1";
                            break;
                            case "8":
                                porta1 = "2";porta2 = "4";
                            break;
                            default:
                                porta1 = "5";porta2 = "5";
                                    }*/
                        
                        
                        
                        swMbox.setAttribute("vlid", instance);
                        swMbox.setAttribute("port", "3");//porta1 "3"
                        swMbox.setAttribute("outPort", "4");//porta2 "4"
                        root.appendChild(swMbox);
                    }
                }
            }
            XMLUtility.getInstance().saveXML(doc, "SwitchMBoxes.xml");
        }

    }
    
    
        private ArrayList<String> returnFlowsToRemoveWithoutRemoving(ArrayList<String> flowStats, long currSwitch) throws FileNotFoundException, IOException, InterruptedException {
       
        log.info(" identifying flows from switch " + currSwitch + " and other switches in the same path ");

        /* 
         - get flow statistics from the given switch //done in BNServer
         - get source and destination Ip address from the active flows ???
         - if the source and destination IPs found in all_paths
         - if (currentTime-startTime) < serviceTime //service is in progress and needs to be continued
         - remove flow (delete already established flows)
         - redirect the flow to other switches
         - else
         - do nothing
         - else //unlikely to have this case
         - do nothing
         */
        // get source and destination Ip address from the active flows ???
        ArrayList<String> flowEntries = flowStats;
        ArrayList<String> idRequestsToRemove = new ArrayList<String>();
        
        newPaths = new ArrayList<>();

        matchEntries = new HashMap<>();

         for (String flowEntry : flowEntries) {
             
          log.info("flowEntry: "+flowEntry);
          
          String srcIP = ""; String dstIP = ""; String requestID = "";
          
           Pattern pattern1 = Pattern.compile("\"subtype\":\"ETH_SRC\",\"type\":\"L2MODIFICATION\",\"mac\":\"(.*?)\"");
           Matcher matcher1 = pattern1.matcher(flowEntry);
                if (matcher1.find())
                   {
                  //  System.out.println("srcMAC: "+matcher1.group(1));
                    srcIP = getIPaddress(matcher1.group(1));
                    
                    }
                
           Pattern pattern2 = Pattern.compile("\"type\":\"ETH_DST\",\"mac\":\"(.*?)\"");
           Matcher matcher2 = pattern2.matcher(flowEntry);
                if (matcher2.find())
                   {
                //    System.out.println("dstMAC: "+matcher2.group(1));
                    dstIP = getIPaddress(matcher2.group(1));
                    
                    }
                
         //  Pattern pattern3 = Pattern.compile("\"priority\":(.\\d?)");
        Pattern pattern3 = Pattern.compile("\"priority\":(.*?)\"");
           Matcher matcher3 = pattern3.matcher(flowEntry);
                if (matcher3.find())
                   {
                     String[] tempIt = new String[2];
                        tempIt = matcher3.group(1).split(",");
                       requestID = tempIt[0];
                      
                    }
             
          
            log.info("srcIP: "+srcIP);
            log.info("dstIP: "+dstIP);

            
            
            if((!(idRequestsToRemove.contains(requestID)))&& (!(requestsToResend.contains(requestID))))
            {
            idRequestsToRemove.add(requestID);
            requestsToResend.add(requestID);
            
            log.info("requestID: "+requestID);
            }
           // System.out.println("identify flows - srcIP: "+srcIP+" - dstIP: "+dstIP);
          //  identifyFlows(srcIP, dstIP, requestID);
           //  removePath2(srcIP, dstIP);

        }

        //return newPaths;
         return idRequestsToRemove;
    }
        
        
    public void resendRequestAfterIdentification(ArrayList<String> idRequestsToRemove) throws InterruptedException {

        double currTime;
        
        log.info("we are in resendRequestAfterIdentification "+requestsToResend.size());
        
      //  System.out.println("srcIP - dstIP "+srcIP+" - "+dstIP);

        for (int ind=0; ind<requestsToResend.size(); ind++) {
            
            String req = requestsToResend.get(ind);
            
           
         //   System.out.println("file to open: "+" path" + req + "_" + last_iteration + ".xml");
            String last_iteration = accepted_requests.get(req.concat("-"));
            
            log.info("req: "+req+" iteration: "+last_iteration);//we consider always the iteration equal to 1
            
             if (generated_requests.contains(req))
                 {
         //   Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "-_" + "1" + "_" + ".xml");
               Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "-_" + last_iteration + ".xml");
            
            Thread.sleep(50);
            
            String srcIP2="";
                String dstIP2="";
                short inPort2;
                short outPort2;
                String direction;
                double serviceTime=0;
                long swId;
                double start_Time;
                StringBuilder sbSrcIP2;
                StringBuilder sbDstIP2;
                String reqID="";
                double remTimeSeconds=0;
               String sla = "0";
                String iteration="";

            if (d != null) {
                
                
                XMLUtility.getInstance().printXML(d);
                
                NodeList responses = d.getElementsByTagName("Response");
                if (responses != null) {
                    if (responses.getLength() > 0) {
                        Element rs = (Element) responses.item(0);
                       reqID = rs.getAttribute("requestID");
                        serviceTime = Double.parseDouble(rs.getAttribute("serviceTime"));
                        start_Time = Double.parseDouble(rs.getAttribute("startTime"));
                        String[] tempIt = new String[2];
                        tempIt = rs.getAttribute("iteration").split("_");
                        iteration= tempIt[0];

                        currTime = System.currentTimeMillis();
                        double servedTime = (double) (currTime - start_Time);
                        int retval = Double.compare(serviceTime, (double) (servedTime / (double) 1000));
                        remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));

                        
                      Element ps = (Element) rs.getElementsByTagName("Request").item(0);
                        
                   //   System.out.println("rs.getElementsByTagName(\"Request\").getLength()> "+rs.getElementsByTagName("Request").getLength());
                        
                      
                      int elements = rs.getElementsByTagName("Request").getLength();
                        
                         if(elements > 0)
                           {
                               
                     //   System.out.println("we are in if path");
                        
                        NodeList pths = ps.getElementsByTagName("Path");
                        Element pth;
                        HashMap<String, ArrayList<String>> entries = new HashMap<>();
                        
                      //  System.out.println("pths.getLength(): "+pths.getLength());

                    //    for (int j = 0; j < pths.getLength(); j++) {
                            pth = (Element) pths.item(0);
                            srcIP2 = pth.getElementsByTagName("srcIP").item(0).getTextContent();
                            dstIP2 = pth.getElementsByTagName("dstIP").item(0).getTextContent();
                            direction = pth.getElementsByTagName("direction").item(0).getTextContent();
                            
                        //    System.out.println("3direction: "+direction);

                          //  String tempSrcIP2 = srcIP2, tempDstIP2 = dstIP2;
                            
                          //  System.out.println("srcIP2: "+srcIP2);
                        //    System.out.println("DstIP2: "+dstIP2);
                            
                         //   System.out.println("tempSrcIP2: "+tempSrcIP2);
                        //    System.out.println("tempDstIP2: "+tempDstIP2);

                           
               //   System.out.println("tempSrcIP2 / "+tempSrcIP2+" srcIP: "+srcIP);
               //   System.out.println("tempDstIP2 / "+tempDstIP2+" dstIP: "+dstIP);

                  /*         if (tempSrcIP2.equals(srcIP) && tempDstIP2.equals(dstIP)) {
                                
                             //  System.out.println("ips 2 match");
                                
                                NodeList segs = pth.getElementsByTagName("Segment");
                                for (int jj = 0; jj < segs.getLength(); jj++) {
                                    Element sgmnt = (Element) segs.item(jj);
                                    NodeList nodes = sgmnt.getElementsByTagName("Node");
                                    for (int jjj = 0; jjj < nodes.getLength(); jjj++) {
                                        Element node = (Element) nodes.item(jjj);
                                        inPort2 = Short.parseShort(node.getElementsByTagName("inPort").item(0).getTextContent());
                                        outPort2 = Short.parseShort(node.getElementsByTagName("outPort").item(0).getTextContent());
                                        short vlan_match2 = Short.parseShort(node.getElementsByTagName("vlan_match").item(0).getTextContent());//new
                                        swId = Long.parseLong(node.getElementsByTagName("switch").item(0).getTextContent());

                                     //   System.out.println("Deleting flow entries from switch " + swId);

                                        boolean flg = false, flg2 = false;
                                        for (ArrayList<Element> newPaths2 : allNewPaths) {
                                            for (Element newPath2 : newPaths2) {
                                                String reqID2 = newPath2.getAttribute("requestID");
                                                if (reqID2.equals(reqID)) {
                                                    flg2 = true;
                                                    break;
                                                }
                                            }
                                            if (flg2) {
                                                break;
                                            }
                                        }
                                        for (Element newPath : newPaths) {
                                            String reqID3 = newPath.getAttribute("requestID");
                                            if (reqID3.equals(reqID)) {
                                                flg = true;
                                                break;
                                            }
                                        }

                                  
                                        if (!flag) {//re-evalute remaining time (done only at first iteration)
                                            currTime = System.currentTimeMillis();
                                            servedTime = (double) (currTime - start_Time);
                                            retval = Double.compare(serviceTime, (double) (servedTime / 1000));
                                            remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));
                                            flag = true;
                                            //System.out.println("new stopTime is set for request " + reqID);

                                        }

                                   //     BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", doc, requestMessage2.getSessionID());
                                        

                                      //    bnsManager.sendMessage(mess);

                                    //    System.out.println("sendRequestToBNS");
                                    //    System.out.println("Flow canceled! switch = " + swId + " inPort2 = " + inPort2 + " srcIP2 = " + srcIP2 + " dstIP2 = " + dstIP2 + " direction = " + direction);

                                        // if (remTimeSeconds > 3 || matchEntries.containsKey(reqID)) {
                                        if (retval > 0 || matchEntries.containsKey(reqID)) {//retval > 0 remainting time is >0
                                            ArrayList entryFields = new ArrayList();

                                            entryFields.add(srcIP2);
                                            entryFields.add(dstIP2);
                                            entryFields.add(Short.toString(vlan_match2));
                                            entryFields.add(Short.toString(inPort2));
                                            entries.put(Long.toString(swId), entryFields);

                                            if (!flg && !flg2) {
                                                rs.setAttribute("stopTime", Double.toString(currTime));
                                                newPaths.add(rs);
                                                //System.out.println("new Path is ready to be established for request " + reqID);
                                                countRedirection++;
                                                //System.out.println("countRedirection = " + countRedirection);
                                            }
                                        }
                                    }
                                }

                                HashMap<String, String> mEntries = new HashMap<>();
                                mEntries.put(srcIP, dstIP);

                                if (!matchEntries.containsKey(reqID)) {
                                    matchEntries.put(reqID, mEntries);
                                } else // request exists
                                {
                                    if (!matchEntries.get(reqID).containsKey(srcIP)
                                            || !matchEntries.get(reqID).get(srcIP).equals(dstIP)) {

                                        matchEntries.get(reqID).put(srcIP, dstIP);
                                    }
                                }

                                if (matchEntries.get(reqID).size() < 2) { 
                                    System.out.println("remove reverse path");
                                    System.out.println("dstIP - srcIP "+dstIP+" - "+srcIP);
                                    identifyFlows(dstIP, srcIP, reqID);
                                }
                            }
                           
                           */
                          
                       // }
                   
                    }

                }
            }

                
            } else {
              //  System.out.println("Cannot find XML document for the specified request");
                System.exit(1);
            }

            if(remTimeSeconds > 0)
               {  
            resendRequest(Integer.parseInt(reqID), remTimeSeconds, iteration, sla, srcIP2, dstIP2);
            }
            
        }
                    }


    }   

    
    public void removeRequestAfterIdentification(ArrayList<String> idRequestsToRemove, ArrayList<String> flowStats, long currSwitch) throws InterruptedException {

        double currTime;
        
        log.info("we are in removeRequest "+requestsToResend.size());
        
      //  System.out.println("srcIP - dstIP "+srcIP+" - "+dstIP);

        for (int ind=0; ind<requestsToResend.size(); ind++) {
            
            String req = requestsToResend.get(ind);
            
            
            String last_iteration = accepted_requests.get(req.concat("-"));
            
            log.info("req: "+req+" iteration: "+last_iteration);
            
            if (generated_requests.contains(req))
            {
         //   System.out.println("file to open: "+" path" + req + "_" + last_iteration + ".xml");

           // Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "-_" + "1" + "_" + ".xml");
             Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "-_" + last_iteration + ".xml");
            
            
             if (d != null) {
                String srcIP2;
                String dstIP2;
                short inPort2;
                short outPort2;
                String direction;
                String iteration;
                double serviceTime;
                long swId;
                double start_Time;
                StringBuilder sbSrcIP2;
                StringBuilder sbDstIP2;
                
             //   XMLUtility.getInstance().printXML(d);
                
                NodeList responses = d.getElementsByTagName("Response");
                if (responses != null) {
                    if (responses.getLength() > 0) {
                        Element rs = (Element) responses.item(0);
                        String reqID = rs.getAttribute("requestID");
                        serviceTime = Double.parseDouble(rs.getAttribute("serviceTime"));
                        start_Time = Double.parseDouble(rs.getAttribute("startTime"));
                                                
                        String[] tempIt = new String[2];
                        tempIt = rs.getAttribute("iteration").split("_");
                        iteration= tempIt[0];

                        currTime = System.currentTimeMillis();
                        double servedTime = (double) (currTime - start_Time);
                        int retval = Double.compare(serviceTime, (double) (servedTime / (double) 1000));
                        double remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));

                        
                      Element ps = (Element) rs.getElementsByTagName("Request").item(0);
                        
                   //   System.out.println("rs.getElementsByTagName(\"Request\").getLength()> "+rs.getElementsByTagName("Request").getLength());
                        
                      
                      int elements = rs.getElementsByTagName("Request").getLength();
                        
                         if(elements > 0)
                           {
                               
                     //   System.out.println("we are in if path");
                        
                        NodeList pths = ps.getElementsByTagName("Path");
                        Element pth;
                        HashMap<String, ArrayList<String>> entries = new HashMap<>();
                        
                      //  System.out.println("pths.getLength(): "+pths.getLength());

                        for (int j = 0; j < pths.getLength(); j++) {
                            pth = (Element) pths.item(j);
                            srcIP2 = pth.getElementsByTagName("srcIP").item(0).getTextContent();
                            dstIP2 = pth.getElementsByTagName("dstIP").item(0).getTextContent();
                            direction = pth.getElementsByTagName("direction").item(0).getTextContent();
                            
                           // System.out.println("3direction: "+direction);

                            String tempSrcIP2 = srcIP2, tempDstIP2 = dstIP2;
   

                         //  if (tempSrcIP2.equals(srcIP) && tempDstIP2.equals(dstIP)) {
                                
                           //    System.out.println("ips 2 match");
                                
                                NodeList segs = pth.getElementsByTagName("Segment");
                                for (int jj = 0; jj < segs.getLength(); jj++) {
                                    Element sgmnt = (Element) segs.item(jj);
                                    NodeList nodes = sgmnt.getElementsByTagName("Node");
                                    for (int jjj = 0; jjj < nodes.getLength(); jjj++) {
                                        Element node = (Element) nodes.item(jjj);
                                        inPort2 = Short.parseShort(node.getElementsByTagName("inPort").item(0).getTextContent());
                                        outPort2 = Short.parseShort(node.getElementsByTagName("outPort").item(0).getTextContent());
                                        short vlan_match2 = Short.parseShort(node.getElementsByTagName("vlan_match").item(0).getTextContent());//new
                                        swId = Long.parseLong(node.getElementsByTagName("switch").item(0).getTextContent());

                                     //   System.out.println("Deleting flow entries from switch " + swId);

                                        boolean flg = false, flg2 = false;
                                        for (ArrayList<Element> newPaths2 : allNewPaths) {
                                            for (Element newPath2 : newPaths2) {
                                                String reqID2 = newPath2.getAttribute("requestID");
                                                if (reqID2.equals(reqID)) {
                                                    flg2 = true;
                                                    break;
                                                }
                                            }
                                            if (flg2) {
                                                break;
                                            }
                                        }
                                        for (Element newPath : newPaths) {
                                            String reqID3 = newPath.getAttribute("requestID");
                                            if (reqID3.equals(reqID)) {
                                                flg = true;
                                                break;
                                            }
                                        }

                                     //   AEMessage requestMessage2;
                                        BNSMessage requestMessage2;
                                        org.w3c.dom.Document doc
                                                = createXMLRequest("ServiceCancellationRequest", swId, inPort2, outPort2, srcIP2, dstIP2, /*vlan_match_flow,*/ vlan_match2, direction, reqID); //new added vlan_match_flow

                                       // requestMessage2 = new AEMessage(doc, "");
                                          requestMessage2 = new BNSMessage(doc, "");
                                        
                                      //  System.out.println("Cancellation request");
                                     //   XMLUtility.getInstance().printXML(doc);
                                        
                                    //    String command = requestMessage2.getCommand();
                                   
                                        if (!flag) {//re-evalute remaining time (done only at first iteration)
                                            currTime = System.currentTimeMillis();
                                            servedTime = (double) (currTime - start_Time);
                                            retval = Double.compare(serviceTime, (double) (servedTime / 1000));
                                            remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));
                                            flag = true;
                                            //System.out.println("new stopTime is set for request " + reqID);

                                        }

                                    //    BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", requestMessage2.getValue(), requestMessage2.getSessionID());
                                          BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", doc, requestMessage2.getSessionID());
                                        
                                        
                                       // sendMessage(mess);//message sent to BNS directly
                                          bnsManager.sendMessage(mess);

                                    //    System.out.println("sendRequestToBNS");
                                    //    System.out.println("Flow canceled! switch = " + swId + " inPort2 = " + inPort2 + " srcIP2 = " + srcIP2 + " dstIP2 = " + dstIP2 + " direction = " + direction);

                                        // if (remTimeSeconds > 3 || matchEntries.containsKey(reqID)) {
                                        if (retval > 0 || matchEntries.containsKey(reqID)) {//retval > 0 remainting time is >0
                                            ArrayList entryFields = new ArrayList();

                                            entryFields.add(srcIP2);
                                            entryFields.add(dstIP2);
                                            entryFields.add(Short.toString(vlan_match2));
                                            entryFields.add(Short.toString(inPort2));
                                            entries.put(Long.toString(swId), entryFields);

                                            if (!flg && !flg2) {
                                                rs.setAttribute("stopTime", Double.toString(currTime));
                                                newPaths.add(rs);
                                                //System.out.println("new Path is ready to be established for request " + reqID);
                                                countRedirection++;
                                                //System.out.println("countRedirection = " + countRedirection);
                                            }
                                        }
                                    }
                                }

                              // }
                        }
                   
                    }

                }
            }

                
            } else {
              //  System.out.println("Cannot find XML document for the specified request");
                System.exit(1);
            }
            
            requestsToResend.remove(ind);
            
        }
        }

    }  
    
    
    private ArrayList<Element> removeFlows(ArrayList<String> flowStats, long currSwitch) throws FileNotFoundException, IOException, InterruptedException {
       
        log.info(" Removing flows from switch " + currSwitch + " and other switches in the same path ");

        /* 
         - get flow statistics from the given switch //done in BNServer
         - get source and destination Ip address from the active flows ???
         - if the source and destination IPs found in all_paths
         - if (currentTime-startTime) < serviceTime //service is in progress and needs to be continued
         - remove flow (delete already established flows)
         - redirect the flow to other switches
         - else
         - do nothing
         - else //unlikely to have this case
         - do nothing
         */
        // get source and destination Ip address from the active flows ???
        ArrayList<String> flowEntries = flowStats;
        newPaths = new ArrayList<>();

        matchEntries = new HashMap<>();

         for (String flowEntry : flowEntries) {
             
          log.info("flowEntry: "+flowEntry);
          
          String srcIP = ""; String dstIP = "";
          
           Pattern pattern1 = Pattern.compile("\"subtype\":\"ETH_SRC\",\"type\":\"L2MODIFICATION\",\"mac\":\"(.*?)\"");
           Matcher matcher1 = pattern1.matcher(flowEntry);
                if (matcher1.find())
                   {
                  //  System.out.println("srcMAC: "+matcher1.group(1));
                    srcIP = getIPaddress(matcher1.group(1));
                    
                    }
                
           Pattern pattern2 = Pattern.compile("\"type\":\"ETH_DST\",\"mac\":\"(.*?)\"");
           Matcher matcher2 = pattern2.matcher(flowEntry);
                if (matcher2.find())
                   {
                //    System.out.println("dstMAC: "+matcher2.group(1));
                    dstIP = getIPaddress(matcher2.group(1));
                    
                    }
             
          
            log.info("srcIP: "+srcIP);
            log.info("dstIP: "+dstIP);
            log.info("remove flows - srcIP: "+srcIP+" - dstIP: "+dstIP);
            removeFlows(srcIP, dstIP);
           //  removePath2(srcIP, dstIP);

        }

        return newPaths;
    }
    
    
   /* public void deleteRequest(String srcIP, String dstIP, String IDToRemove) {

        double currTime;
        
        System.out.println("ID Request ToRemove: "+IDToRemove);

        System.out.println("number of active requests: "+active_requests.size());
        
        if(active_requests.contains(IDToRemove))
        {
        
        System.out.println("we are in delete request of srcIP: "+srcIP+" - dstIP: "+dstIP);
        
        HttpExample http = new HttpExample();

                    String reqID = IDToRemove;//Requests_to_Resend.get(identifier);
                    
                    Document forward_switches = XMLUtility.getInstance().loadXML("Database/".concat(reqID.concat("-forward_switches.xml")));
                    //XMLUtility.getInstance().printXML(forward_switches);
                    
                    Document forward_flows = XMLUtility.getInstance().loadXML("Database/".concat(reqID.concat("-forward_flows.xml")));
                    //XMLUtility.getInstance().printXML(forward_flows);
                    
                    Document forward_intents = XMLUtility.getInstance().loadXML("Database/".concat(reqID.concat("-forward_intents.xml")));
                    //XMLUtility.getInstance().printXML(forward_intents);
                    
                    Document backward_switches = XMLUtility.getInstance().loadXML("Database/".concat(reqID.concat("-backward_switches.xml")));
                    //XMLUtility.getInstance().printXML(backward_switches);
                    
                    Document backward_flows = XMLUtility.getInstance().loadXML("Database/".concat(reqID.concat("-backward_flows.xml")));
                    //XMLUtility.getInstance().printXML(backward_flows);
                    
                    Document backward_intents = XMLUtility.getInstance().loadXML("Database/".concat(reqID.concat("-backward_intents.xml")));
                    //XMLUtility.getInstance().printXML(backward_intents);

                    NodeList involved_switches = forward_switches.getElementsByTagName("Switch");
                    NodeList back_switches = backward_switches.getElementsByTagName("Switch");
 
                    
                    NodeList for_intents = forward_intents.getElementsByTagName("Intent");
                    for (int i = 0; i < for_intents.getLength(); i++) 
                    {
                    Element value = (Element) for_intents.item(i);    
                    String key = value.getAttribute("key");
                    
                    try {
                            http.deleteIntentbyID(key);
                        } catch (Exception ex) {
                            Logger.getLogger(Adaptation.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    NodeList back_intents = backward_intents.getElementsByTagName("Intent");
                    for (int i = 0; i < back_intents.getLength(); i++) 
                    {
                    Element value = (Element) back_intents.item(i);    
                    String key = value.getAttribute("key");
                    
                    try {
                            http.deleteIntentbyID(key);
                        } catch (Exception ex) {
                            Logger.getLogger(Adaptation.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

        
        }//active_requests.contains(IDToRemove)
        
        active_requests.remove(IDToRemove);
        System.out.println("active_requests.size(): "+active_requests.size());

    }*/


    private void redirectFlows(ArrayList<ArrayList<Element>> allNewPaths) {
        
       log.info("we are REDIRECTING the deleted flows");
        
      log.info("allNewPaths.size(): "+allNewPaths.size());
        
        for (ArrayList<Element> newPaths2 : allNewPaths) {
            
            for (Element newPath : newPaths2) {
                
                int reqID = Integer.parseInt(newPath.getAttribute("requestID"));
                double stopTime = Double.parseDouble(newPath.getAttribute("stopTime"));
                Element ps = (Element) newPath.getElementsByTagName("Request").item(0);
                NodeList pths = ps.getElementsByTagName("Path");
                Element p = (Element) pths.item(0);//either forward or backward is used (needs swaping in the later case)

                String src = p.getElementsByTagName("srcIP").item(0).getTextContent();
                String dst = p.getElementsByTagName("dstIP").item(0).getTextContent();
                String dir = p.getElementsByTagName("direction").item(0).getTextContent();
                String sla = "0";//p.getElementsByTagName("SLA").item(0).getTextContent();
                double servTimeSeconds = Double.parseDouble(((Element) p.getParentNode().getParentNode()).getAttribute("serviceTime"));
                double stTime = Double.parseDouble(((Element) p.getParentNode().getParentNode()).getAttribute("startTime"));
                String iteration = ((Element) p.getParentNode().getParentNode()).getAttribute("iteration");
                double servedTime = (double) (stopTime - stTime);
                
                log.info("reqID: "+reqID);
                log.info("srcIP: "+src);
                log.info("dstIP: "+dst);
                log.info("direction: "+dir);
                log.info("SLA: "+sla);

                double remTimeSeconds = (double) (servTimeSeconds - (double) (servedTime / 1000));
                log.info("remTimeSeconds=" + remTimeSeconds);

              //  if (Double.compare(remTimeSeconds, 3.0D) >= 0) {
                //    System.out.println("we are in if");
                    if (dir.equals("backward")) {
                        //swap ips
                        String temp = src;
                        src = dst;
                        dst = temp;
                    }
                    
                    if(remTimeSeconds > 0)
                    {
                    log.info("Setting new path (iteration=" + iteration + ") for request " + reqID + " from " + src + " to " + dst + " excluding the overloaded switch(es)");
                   // resendRequest(reqID, remTimeSeconds, iteration, sla, src, dst);//fwd
                     resendRequest(2, remTimeSeconds, iteration, sla, src, dst);//fwd
                    }
                    else
                    {
                    log.info("Cancel request!!!");
                    }
                            
            }

        }

    }

//for delete request
    private Document createXMLRequest(String request, long currSwitch, short inPort, short outPort, String srcIP, String dstIP, short vlan_match, String direction, String reqID) {
        
     //   System.out.println("we are creating a: "+request);
        
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();
        try {
            org.w3c.dom.Element root = doc.createElement(request);
            root.setAttribute("vlid_match", Short.toString(vlan_match));//0=No vlan ID
            root.setAttribute("inPort", Short.toString(inPort));
            root.setAttribute("outPort", Short.toString(outPort));
            root.setAttribute("direction", direction);
            root.setAttribute("switch", String.valueOf(currSwitch));
            root.setAttribute("priority", reqID);
            doc.appendChild(root);
            
            org.w3c.dom.Element req = doc.createElement("requestID");
            req.setTextContent(String.valueOf(reqID));
            root.appendChild(req);

            org.w3c.dom.Element srcNode = doc.createElement("srcNode");
            srcNode.setAttribute("ip", srcIP);
            root.appendChild(srcNode);

            org.w3c.dom.Element dstNode = doc.createElement("dstNode");
            dstNode.setAttribute("ip", dstIP);
            root.appendChild(dstNode);

            org.w3c.dom.Element intermSrcNode = doc.createElement("intermSrcNode");
            intermSrcNode.setAttribute("port", "1");
            intermSrcNode.setAttribute("ip", srcIP);
            root.appendChild(intermSrcNode);

            org.w3c.dom.Element intermDstNode = doc.createElement("intermDstNode");
            intermDstNode.setAttribute("port", "1");
            intermDstNode.setAttribute("ip", dstIP);
            root.appendChild(intermDstNode);

        } catch (Exception e) {
            
        }

        return doc;
    }

    private org.w3c.dom.Document createXMLRequest(String request, int requestID, double serviceTime, String iteration, String srcIp, String dstIp) {
        
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();
        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement(request);
            root.setAttribute("isFirstNode", "false");
            root.setAttribute("complete", "false");
            root.setAttribute("isLastNode", "false");
            root.setAttribute("vlid_action", "0");//0=No vlan ID
            root.setAttribute("vlid_match", "0");//0=No vlan ID
            root.setAttribute("direction", "forward");
            root.setAttribute("pathType", "simple");
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
            iter.setTextContent(iteration);
            root.appendChild(iter);

            org.w3c.dom.Element srcNode = doc.createElement("srcNode");
            //srcNode.setAttribute("switch", srcSw.getText());
            //srcNode.setAttribute("port", "1");
            srcNode.setAttribute("ip", srcIp);
            root.appendChild(srcNode);

            org.w3c.dom.Element dstNode = doc.createElement("dstNode");
            dstNode.setAttribute("ip", dstIp);
            root.appendChild(dstNode);

            org.w3c.dom.Element intermSrcNode = doc.createElement("intermSrcNode");
            intermSrcNode.setAttribute("port", "1");
            intermSrcNode.setAttribute("ip", srcIp);
            root.appendChild(intermSrcNode);

            org.w3c.dom.Element intermDstNode = doc.createElement("intermDstNode");
            intermDstNode.setAttribute("port", "1");
            intermDstNode.setAttribute("ip", dstIp);
            root.appendChild(intermDstNode);
        } catch (Exception e) {
            
        }
        return doc;

    }
    
        private org.w3c.dom.Document createXMLRequest(String request, int requestID, double serviceTime, String iteration, String SLAvalue, String srcIp, String dstIp) {
        
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();
        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement(request);
            root.setAttribute("isFirstNode", "false");
            root.setAttribute("complete", "false");
            root.setAttribute("isLastNode", "false");
            root.setAttribute("vlid_action", "0");//0=No vlan ID
            root.setAttribute("vlid_match", "0");//0=No vlan ID
            root.setAttribute("direction", "forward");
            root.setAttribute("pathType", "simple");
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
            iter.setTextContent(iteration);
            root.appendChild(iter);
            
            org.w3c.dom.Element sla = doc.createElement("SLA");
            sla.setTextContent(SLAvalue);
            root.appendChild(sla);

            org.w3c.dom.Element srcNode = doc.createElement("srcNode");
            //srcNode.setAttribute("switch", srcSw.getText());
            //srcNode.setAttribute("port", "1");
            srcNode.setAttribute("ip", srcIp);
            root.appendChild(srcNode);

            org.w3c.dom.Element dstNode = doc.createElement("dstNode");
            dstNode.setAttribute("ip", dstIp);
            root.appendChild(dstNode);

            org.w3c.dom.Element intermSrcNode = doc.createElement("intermSrcNode");
            intermSrcNode.setAttribute("port", "1");
            intermSrcNode.setAttribute("ip", srcIp);
            root.appendChild(intermSrcNode);

            org.w3c.dom.Element intermDstNode = doc.createElement("intermDstNode");
            intermDstNode.setAttribute("port", "1");
            intermDstNode.setAttribute("ip", dstIp);
            root.appendChild(intermDstNode);
        } catch (Exception e) {
            
        }
        return doc;

    }

   /* private void resendRequest(int reqID, double remTimeSeconds, String iteration, String src, String dst) {
        
        System.out.println("we are RESENDING the request!!");
        
        org.w3c.dom.Document doc = createXMLRequest("ServiceDeliveryRequest", reqID, remTimeSeconds, iteration, src, dst);

        AEMessage msg = new AEMessage(doc, "");
        
        XMLUtility.getInstance().printXML(doc);
        
        String command = msg.getCommand();

        String session = msg.getSessionID();

        FSM f = new ControllerFsm(this, session);
        fsmList.put(session, f);
        f.stateSTART(msg);
        FSM f2 = (FSM) fsmList.get(session);
        if (f2 != null) {
            f2.execute(msg);
        }
    }*/
    
        private void resendRequest(int reqID, double remTimeSeconds, String iteration, String SLA, String src, String dst) {
        
        log.info("we are RESENDING the request!! "+reqID);
        
        int newReqID = reqID + 1000;
        
        log.info("newReqID "+newReqID);
        
        org.w3c.dom.Document doc = createXMLRequest("ServiceDeliveryRequest", newReqID, remTimeSeconds, iteration, SLA, src, dst);
        
        
        AEMessage msg = new AEMessage(doc, "");
        
                
        received_requests_doc.put(newReqID, ((AEMessage)msg).getValue());
        
          current_bytes_flow_forward.put(String.valueOf(newReqID), 0.0);
               // current_bytes_flow_backward.put(reqID, 0.0);
                
                startTime = System.currentTimeMillis();
               
                //received_requests_time.put(id_received_request, startTime);
                received_requests_time.put(newReqID, startTime);
        
       // XMLUtility.getInstance().printXML(doc);
        
        String command = msg.getCommand();

        String session = msg.getSessionID();
                                     
        FSM f = new ControllerFsm(this, session);
        fsmList.put(session, f);
        f.stateSTART(msg);
        
        FSM f2 = (FSM) fsmList.get(session);
        if (f2 != null) {
            f2.execute(msg);
        }
    }

    private void refreshActiveRequests() {
        
        ArrayList<String> requests_to_remove = new ArrayList<>();
        
        for (int index2 = 0; index2 < active_requests.size(); index2++) {
            String strIndex = (Integer.toString(index2));
            String req = active_requests.get(index2);
         //   System.out.println("(active) request number: " + req);
            String last_iteration = accepted_requests.get(req);

            Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "_" + last_iteration + ".xml");
            
          //  XMLUtility.getInstance().printXML(d);

            if (d != null) {
                String srcIP2;
                String dstIP2;
                short inPort2 = 0;
                short outPort2 = 0;
                String direction = "";
                double serviceTime;
                long swId;
                double start_Time;
                NodeList responses = d.getElementsByTagName("Response");
                if (responses != null) {
                    if (responses.getLength() > 0) {
                        Element rs = (Element) responses.item(0);
                        String reqID = rs.getAttribute("requestID");
                        serviceTime = Double.parseDouble(rs.getAttribute("serviceTime"));
                        start_Time = Double.parseDouble(rs.getAttribute("startTime"));

                        long currTime = System.currentTimeMillis();
                        double servedTime = (double) (currTime - start_Time);
                        int retval = Double.compare(serviceTime, (double) (servedTime / (double) 1000));
                        double remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));

                        // if (remTimeSeconds <= 0 && !reqID.equals("0") && !req.equals("101")) {
                        if (retval <= 0 && !reqID.equals("0") && !req.equals("101")) {
                            //System.out.println("Clearing flow entries from  all switch for completed request " + reqID);
                            Element ps = (Element) rs.getElementsByTagName("Request").item(0);
                            NodeList pths = ps.getElementsByTagName("Path");
                            Element pth;
                            HashMap<String, ArrayList<String>> entries = new HashMap<>();
                            for (int j = 0; j < pths.getLength(); j++) {
                                pth = (Element) pths.item(j);
                                srcIP2 = pth.getElementsByTagName("srcIP").item(0).getTextContent();
                                dstIP2 = pth.getElementsByTagName("dstIP").item(0).getTextContent();
                                direction = pth.getElementsByTagName("direction").item(0).getTextContent();
                             //   System.out.println(" 1direction: "+direction);

                                NodeList segs = pth.getElementsByTagName("Segment");
                                for (int jj = 0; jj < segs.getLength(); jj++) {
                                    Element sgmnt = (Element) segs.item(jj);
                                    NodeList nodes = sgmnt.getElementsByTagName("Node");
                                    for (int jjj = 0; jjj < nodes.getLength(); jjj++) {
                                        Element node = (Element) nodes.item(jjj);
                                        inPort2 = Short.parseShort(node.getElementsByTagName("inPort").item(0).getTextContent());
                                        outPort2 = Short.parseShort(node.getElementsByTagName("outPort").item(0).getTextContent());
                                        short vlan_match2 = Short.parseShort(node.getElementsByTagName("vlan_match").item(0).getTextContent());//new
                                        swId = Long.parseLong(node.getElementsByTagName("switch").item(0).getTextContent());

                                        AEMessage requestMessage2;
                                        org.w3c.dom.Document doc
                                                = createXMLRequest("ServiceCancellationRequest", swId, inPort2, outPort2, srcIP2, dstIP2, vlan_match2, direction, reqID); //new added vlan_match_flow
                                        requestMessage2 = new AEMessage(doc, "");

                                      //  XMLUtility.getInstance().printXML(doc);
                                       // String command = requestMessage2.getCommand();
                                 
                                        BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", requestMessage2.getValue(), requestMessage2.getSessionID());
                                        this.sendMessage(mess);//message sent to BNS directly
                                      //  System.out.println("Flow cleared! switch = " + swId + " inPort = " + inPort2 + " srcIP = " + srcIP2 + " dstIP = " + dstIP2 + " direction = " + direction);

                                    }
                                }
                            }
                            if (active_requests.contains(reqID.concat("-").concat(direction))) {
                                requests_to_remove.add(reqID);
                            }
                        }

                    }
                }
            }

        }

        for (String req : requests_to_remove) {
            active_requests.remove(req);
            generated_requests.remove(req);

            //System.out.println("request " + req + " has been removed from active requests list");

        }

    }
    
       public void removePath2(String srcIP, String dstIP) {

        double currTime;

      //  System.out.println("we are in remove path 2 of srcIP: "+srcIP+" - dstIP: "+dstIP);
        
      //  System.out.println("IDToRemove: "+IDToRemove);
          
     //   System.out.println("entering active requests");
        System.out.println("number of active requests: "+active_requests.size());
        
      //  String tempo = IDToRemove.concat("-");
                
        for (String req : active_requests) {
            
          //  System.out.println("req: "+req);
            
       // if (req.equals(tempo))
                //{
            
                                   
            String last_iteration = accepted_requests.get(req);
         //   System.out.println("last iteration: "+last_iteration);

            Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "_" + last_iteration + ".xml");

            if (d != null) {
       
              //  System.out.println("path file found");
                
               // XMLUtility.getInstance().printXML(d);

                String srcIP2;
                String dstIP2;
                short inPort2;
                short outPort2;
                String direction;
                double serviceTime;
                long swId;
                double start_Time;
                StringBuilder sbSrcIP2;
                StringBuilder sbDstIP2;

                NodeList responses = d.getElementsByTagName("Response");
                if (responses != null) {
                    if (responses.getLength() > 0) {
                        Element rs = (Element) responses.item(0);
                        String reqID = rs.getAttribute("requestID");
                        
                        serviceTime = Double.parseDouble(rs.getAttribute("serviceTime"));
                        start_Time = Double.parseDouble(rs.getAttribute("startTime"));

                        currTime = System.currentTimeMillis();
                        double servedTime = (double) (currTime - start_Time);
                        double remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));

                        //if (remTimeSeconds > 3 || matchEntries.containsKey(reqID)) {
                        Element ps = (Element) rs.getElementsByTagName("Request").item(0);
                        NodeList pths = ps.getElementsByTagName("Path");
                        Element pth;
                        HashMap<String, ArrayList<String>> entries = new HashMap<>();

                        for (int j = 0; j < pths.getLength(); j++) {
                            
                            pth = (Element) pths.item(j);
                            srcIP2 = pth.getElementsByTagName("srcIP").item(0).getTextContent();
                            dstIP2 = pth.getElementsByTagName("dstIP").item(0).getTextContent();
                            direction = pth.getElementsByTagName("direction").item(0).getTextContent();
                            
                         //   System.out.println("2direction: "+direction);

                            String tempSrcIP2 = srcIP2, tempDstIP2 = dstIP2;

                            switch (direction) {
                                case "forward":
                                    String[] srcIP2Digits = tempSrcIP2.split("\\.");
                                    sbSrcIP2 = new StringBuilder();
                                    sbSrcIP2.append(srcIP2Digits[0]).append(".").append(srcIP2Digits[1]).append(".")
                                            .append(srcIP2Digits[2]).append(".1");
                                    tempSrcIP2 = sbSrcIP2.toString();

                                    break;
                              /*  case "backward":
                                    //backward
                                    String[] dstIP2Digits = tempDstIP2.split("\\.");
                                    sbDstIP2 = new StringBuilder();
                                    sbDstIP2.append(dstIP2Digits[0]).append(".").append(dstIP2Digits[1]).append(".")
                                            .append(dstIP2Digits[2]).append(".1");
                                    tempDstIP2 = sbDstIP2.toString();

                                    break;*/
                                default:
                                    break;
                            }
                         //   System.out.println("matching given IPs");
                        //    System.out.println("tempSrcIP2 - srcIP: "+tempSrcIP2 + "-" +srcIP);
                        //    System.out.println("tempDstIP2 - dstIP: "+tempDstIP2 + "-" +dstIP);
                            
                            String temp = reqID.concat("-");
                            
                            if(temp.equals(req))
                            {
                             //   System.out.println("temp: "+temp);
                             //   System.out.println("req: "+req);
                                
                            if ((tempSrcIP2.equals(srcIP) && tempDstIP2.equals(dstIP))  || (tempSrcIP2.equals(dstIP) && tempDstIP2.equals(srcIP))) {
                                log.info("IPs match!");
                                NodeList segs = pth.getElementsByTagName("Segment");
                                for (int jj = 0; jj < segs.getLength(); jj++) {
                                    Element sgmnt = (Element) segs.item(jj);
                                    NodeList nodes = sgmnt.getElementsByTagName("Node");
                                    for (int jjj = 0; jjj < nodes.getLength(); jjj++) {
                                        Element node = (Element) nodes.item(jjj);
                                        inPort2 = Short.parseShort(node.getElementsByTagName("inPort").item(0).getTextContent());
                                        outPort2 = Short.parseShort(node.getElementsByTagName("outPort").item(0).getTextContent());
                                        short vlan_match2 = Short.parseShort(node.getElementsByTagName("vlan_match").item(0).getTextContent());//new
                                        swId = Long.parseLong(node.getElementsByTagName("switch").item(0).getTextContent());

                                     //   System.out.println("Deleting flow entries from switch " + swId);

                                        //create new requestMessage and set attributs
                                        AEMessage requestMessage2;
                                        org.w3c.dom.Document doc
                                                = createXMLRequest("ServiceCancellationRequest", swId, inPort2, outPort2, srcIP2, dstIP2, /*vlan_match_flow,*/ vlan_match2, direction, reqID); //new added vlan_match_flow

                                        requestMessage2 = new AEMessage(doc, "");

                                      //  XMLUtility.getInstance().printXML(doc);
                                      //  String command = requestMessage2.getCommand();
                                       
                                        BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", requestMessage2.getValue(), requestMessage2.getSessionID());

                                        this.sendMessage(mess);//message sent to BNS directly
                                        
                                        log.info("Flow cleared! switch = " + swId + " inPort2 = " + inPort2 + " srcIP2 = " + srcIP2 + " dstIP2 = " + dstIP2 + " direction = " + direction);
               
                                    }
                                }

                                HashMap<String, String> mEntries = new HashMap<>();
                                mEntries.put(srcIP, dstIP);

                               /* if (!flag_remove) {
                                    flag_remove = true;
                                    removePath(dstIP, srcIP);
                                }*/
                            }//if check match of IPs
                            
                        }
                            else
                            {
                            log.info("request ID not correct");
                            }
                        }
                    }
                }
                
                active_requests.remove(req);
                log.info("number of active requests: "+active_requests.size());
                               
            } else {
               // System.out.println("Cannot find XML document for the specified request");
                System.exit(1);
            }
            
      //  }//if tempo
        }

    }

    public void removePath(String srcIP, String dstIP, String IDToRemove) {

        double currTime;

        log.info("we are in remove path of srcIP: "+srcIP+" - dstIP: "+dstIP);
        
        log.info("IDToRemove: "+IDToRemove);
          
        log.info("entering active requests");
        log.info("number of active requests: "+active_requests.size());
        
        String tempo = IDToRemove.concat("-");
                
        for (String req : active_requests) {
            
            log.info("req: "+req);
            
        if (req.equals(tempo))
                {
            
                                   
            String last_iteration = accepted_requests.get(req);
            log.info("last iteration: "+last_iteration);

            Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "_" + last_iteration + ".xml");

            if (d != null) {
       
                log.info("path file found");
                
               // XMLUtility.getInstance().printXML(d);

                String srcIP2;
                String dstIP2;
                short inPort2;
                short outPort2;
                String direction;
                double serviceTime;
                long swId;
                double start_Time;
                StringBuilder sbSrcIP2;
                StringBuilder sbDstIP2;

                NodeList responses = d.getElementsByTagName("Response");
                if (responses != null) {
                    if (responses.getLength() > 0) {
                        Element rs = (Element) responses.item(0);
                        String reqID = rs.getAttribute("requestID");
                        
                        serviceTime = Double.parseDouble(rs.getAttribute("serviceTime"));
                        start_Time = Double.parseDouble(rs.getAttribute("startTime"));

                        currTime = System.currentTimeMillis();
                        double servedTime = (double) (currTime - start_Time);
                        double remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));

                        //if (remTimeSeconds > 3 || matchEntries.containsKey(reqID)) {
                        Element ps = (Element) rs.getElementsByTagName("Request").item(0);
                        NodeList pths = ps.getElementsByTagName("Path");
                        Element pth;
                        HashMap<String, ArrayList<String>> entries = new HashMap<>();

                        for (int j = 0; j < pths.getLength(); j++) {
                            
                            pth = (Element) pths.item(j);
                            srcIP2 = pth.getElementsByTagName("srcIP").item(0).getTextContent();
                            dstIP2 = pth.getElementsByTagName("dstIP").item(0).getTextContent();
                            direction = pth.getElementsByTagName("direction").item(0).getTextContent();
                            
                            log.info("2direction: "+direction);

                            String tempSrcIP2 = srcIP2, tempDstIP2 = dstIP2;

                            switch (direction) {
                                case "forward":
                                    String[] srcIP2Digits = tempSrcIP2.split("\\.");
                                    sbSrcIP2 = new StringBuilder();
                                    sbSrcIP2.append(srcIP2Digits[0]).append(".").append(srcIP2Digits[1]).append(".")
                                            .append(srcIP2Digits[2]).append(".1");
                                    tempSrcIP2 = sbSrcIP2.toString();

                                    break;
                                case "backward"://backward
                                    String[] dstIP2Digits = tempDstIP2.split("\\.");
                                    sbDstIP2 = new StringBuilder();
                                    sbDstIP2.append(dstIP2Digits[0]).append(".").append(dstIP2Digits[1]).append(".")
                                            .append(dstIP2Digits[2]).append(".1");
                                    tempDstIP2 = sbDstIP2.toString();

                                    break;
                                default:
                                    break;
                            }
                            log.info("matching given IPs");
                            log.info("tempSrcIP2 - srcIP: "+tempSrcIP2 + "-" +srcIP);
                            log.info("tempDstIP2 - dstIP: "+tempDstIP2 + "-" +dstIP);
                            
                            String temp = reqID.concat("-");
                            
                            if(temp.equals(req))
                            {
                                log.info("temp: "+temp);
                                log.info("req: "+req);
                                
                         //   if ((tempSrcIP2.equals(srcIP) && tempDstIP2.equals(dstIP))  || (tempSrcIP2.equals(dstIP) && tempDstIP2.equals(srcIP))) {
                                log.info("Ips match!");
                                NodeList segs = pth.getElementsByTagName("Segment");
                                for (int jj = 0; jj < segs.getLength(); jj++) {
                                    Element sgmnt = (Element) segs.item(jj);
                                    NodeList nodes = sgmnt.getElementsByTagName("Node");
                                    for (int jjj = 0; jjj < nodes.getLength(); jjj++) {
                                        Element node = (Element) nodes.item(jjj);
                                        inPort2 = Short.parseShort(node.getElementsByTagName("inPort").item(0).getTextContent());
                                        outPort2 = Short.parseShort(node.getElementsByTagName("outPort").item(0).getTextContent());
                                        short vlan_match2 = Short.parseShort(node.getElementsByTagName("vlan_match").item(0).getTextContent());//new
                                        swId = Long.parseLong(node.getElementsByTagName("switch").item(0).getTextContent());

                                     //   log.info("Deleting flow entries from switch " + swId);

                                        //create new requestMessage and set attributs
                                        AEMessage requestMessage2;
                                        org.w3c.dom.Document doc
                                                = createXMLRequest("ServiceCancellationRequest", swId, inPort2, outPort2, srcIP2, dstIP2, /*vlan_match_flow,*/ vlan_match2, direction, reqID); //new added vlan_match_flow

                                        requestMessage2 = new AEMessage(doc, "");

                                       // XMLUtility.getInstance().printXML(doc);
                                      //  String command = requestMessage2.getCommand();
                                       
                                        BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", requestMessage2.getValue(), requestMessage2.getSessionID());

                                       /// XMLUtility.getInstance().printXML(requestMessage2.getValue());
                                        
                                       // sendMessage(mess);//message sent to BNS directly
                                        bnsManager.sendMessage(mess);
                                        
                                        log.info("Flow deleted! switch = " + swId + " inPort2 = " + inPort2 + " srcIP2 = " + srcIP2 + " dstIP2 = " + dstIP2 + " direction = " + direction);
               
                                    }
                                }

                                HashMap<String, String> mEntries = new HashMap<>();
                                mEntries.put(srcIP, dstIP);

                                if (!flag_remove) {
                                    flag_remove = true;
                                    removePath(dstIP, srcIP, IDToRemove);
                                }
                           // }//if check match of IPs
                            
                        }
                            else
                            {
                            log.info("request ID not correct");
                            }
                        }
                    }
                }
                
                active_requests.remove(req);
                               
            } else {
               // log.info("Cannot find XML document for the specified request");
                System.exit(1);
            }
            
        }
        }

    }

    public void removeFlows(String srcIP, String dstIP) throws InterruptedException {

        double currTime;
        
        log.info("we are in remove flows with IP addresses-- "+active_requests.size());
        
        log.info("srcIP - dstIP "+srcIP+" - "+dstIP);

        for (String req : active_requests) {
            
            String last_iteration = accepted_requests.get(req);
            
            log.info("req: "+req+" iteration: "+last_iteration);
            
            log.info("file to open: "+" path" + req + "_" + last_iteration + ".xml");

            Document d = XMLUtility.getInstance().loadXML("paths/path" + req + "_" + last_iteration + ".xml");
            
            Thread.sleep(50);

            if (d != null) {
                String srcIP2;
                String dstIP2;
                short inPort2;
                short outPort2;
                String direction;
                double serviceTime;
                long swId;
                double start_Time;
                StringBuilder sbSrcIP2;
                StringBuilder sbDstIP2;
                
             //   XMLUtility.getInstance().printXML(d);
                
                NodeList responses = d.getElementsByTagName("Response");
                if (responses != null) {
                    if (responses.getLength() > 0) {
                        Element rs = (Element) responses.item(0);
                        String reqID = rs.getAttribute("requestID");
                        serviceTime = Double.parseDouble(rs.getAttribute("serviceTime"));
                        start_Time = Double.parseDouble(rs.getAttribute("startTime"));

                        currTime = System.currentTimeMillis();
                        double servedTime = (double) (currTime - start_Time);
                        int retval = Double.compare(serviceTime, (double) (servedTime / (double) 1000));
                        double remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));

                        
                      Element ps = (Element) rs.getElementsByTagName("Request").item(0);
                        
                   //   System.out.println("rs.getElementsByTagName(\"Request\").getLength()> "+rs.getElementsByTagName("Request").getLength());
                        
                      
                      int elements = rs.getElementsByTagName("Request").getLength();
                        
                         if(elements > 0)
                           {
                               
                     //   System.out.println("we are in if path");
                        
                        NodeList pths = ps.getElementsByTagName("Path");
                        Element pth;
                        HashMap<String, ArrayList<String>> entries = new HashMap<>();
                        
                      //  System.out.println("pths.getLength(): "+pths.getLength());

                        for (int j = 0; j < pths.getLength(); j++) {
                            pth = (Element) pths.item(j);
                            srcIP2 = pth.getElementsByTagName("srcIP").item(0).getTextContent();
                            dstIP2 = pth.getElementsByTagName("dstIP").item(0).getTextContent();
                            direction = pth.getElementsByTagName("direction").item(0).getTextContent();
                            
                            log.info("3direction: "+direction);

                            String tempSrcIP2 = srcIP2, tempDstIP2 = dstIP2;
                            
                            log.info("srcIP2: "+srcIP2);
                            log.info("DstIP2: "+dstIP2);
                            
                            log.info("tempSrcIP2: "+tempSrcIP2);
                            log.info("tempDstIP2: "+tempDstIP2);

                        /*  switch (direction) {
                                case "forward":
                                    String[] srcIP2Digits = tempSrcIP2.split("\\.");
                                    sbSrcIP2 = new StringBuilder();
                                    sbSrcIP2.append(srcIP2Digits[0]).append(".").append(srcIP2Digits[1]).append(".")
                                            .append(srcIP2Digits[2]).append(".1");
                                    tempSrcIP2 = sbSrcIP2.toString();

                                    break;
                                case "backward":
                                    String[] dstIP2Digits = tempDstIP2.split("\\.");
                                    sbDstIP2 = new StringBuilder();
                                    sbDstIP2.append(dstIP2Digits[0]).append(".").append(dstIP2Digits[1]).append(".")
                                            .append(dstIP2Digits[2]).append(".1");
                                    tempDstIP2 = sbDstIP2.toString();

                                    break;
                                default:
                                    break;
                            }*/
                            
                  log.info("tempSrcIP2 / "+tempSrcIP2+" srcIP: "+srcIP);
                  log.info("tempDstIP2 / "+tempDstIP2+" dstIP: "+dstIP);

                           if (tempSrcIP2.equals(srcIP) && tempDstIP2.equals(dstIP)) {
                                
                               log.info("ips 2 match");
                                
                                NodeList segs = pth.getElementsByTagName("Segment");
                                for (int jj = 0; jj < segs.getLength(); jj++) {
                                    Element sgmnt = (Element) segs.item(jj);
                                    NodeList nodes = sgmnt.getElementsByTagName("Node");
                                    for (int jjj = 0; jjj < nodes.getLength(); jjj++) {
                                        Element node = (Element) nodes.item(jjj);
                                        inPort2 = Short.parseShort(node.getElementsByTagName("inPort").item(0).getTextContent());
                                        outPort2 = Short.parseShort(node.getElementsByTagName("outPort").item(0).getTextContent());
                                        short vlan_match2 = Short.parseShort(node.getElementsByTagName("vlan_match").item(0).getTextContent());//new
                                        swId = Long.parseLong(node.getElementsByTagName("switch").item(0).getTextContent());

                                     //   System.out.println("Deleting flow entries from switch " + swId);

                                        boolean flg = false, flg2 = false;
                                        for (ArrayList<Element> newPaths2 : allNewPaths) {
                                            for (Element newPath2 : newPaths2) {
                                                String reqID2 = newPath2.getAttribute("requestID");
                                                if (reqID2.equals(reqID)) {
                                                    flg2 = true;
                                                    break;
                                                }
                                            }
                                            if (flg2) {
                                                break;
                                            }
                                        }
                                        for (Element newPath : newPaths) {
                                            String reqID3 = newPath.getAttribute("requestID");
                                            if (reqID3.equals(reqID)) {
                                                flg = true;
                                                break;
                                            }
                                        }

                                     //   AEMessage requestMessage2;
                                        BNSMessage requestMessage2;
                                        org.w3c.dom.Document doc
                                                = createXMLRequest("ServiceCancellationRequest", swId, inPort2, outPort2, srcIP2, dstIP2, /*vlan_match_flow,*/ vlan_match2, direction, reqID); //new added vlan_match_flow

                                       // requestMessage2 = new AEMessage(doc, "");
                                          requestMessage2 = new BNSMessage(doc, "");
                                        
                                      //  System.out.println("Cancellation request");
                                     //   XMLUtility.getInstance().printXML(doc);
                                        
                                    //    String command = requestMessage2.getCommand();
                                   
                                        if (!flag) {//re-evalute remaining time (done only at first iteration)
                                            currTime = System.currentTimeMillis();
                                            servedTime = (double) (currTime - start_Time);
                                            retval = Double.compare(serviceTime, (double) (servedTime / 1000));
                                            remTimeSeconds = (double) (serviceTime - (double) (servedTime / 1000));
                                            flag = true;
                                            //System.out.println("new stopTime is set for request " + reqID);

                                        }

                                    //    BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", requestMessage2.getValue(), requestMessage2.getSessionID());
                                          BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", doc, requestMessage2.getSessionID());
                                        
                                        
                                       // sendMessage(mess);//message sent to BNS directly
                                          bnsManager.sendMessage(mess);

                                    //    System.out.println("sendRequestToBNS");
                                    //    System.out.println("Flow canceled! switch = " + swId + " inPort2 = " + inPort2 + " srcIP2 = " + srcIP2 + " dstIP2 = " + dstIP2 + " direction = " + direction);

                                        // if (remTimeSeconds > 3 || matchEntries.containsKey(reqID)) {
                                        if (retval > 0 || matchEntries.containsKey(reqID)) {//retval > 0 remainting time is >0
                                            ArrayList entryFields = new ArrayList();

                                            entryFields.add(srcIP2);
                                            entryFields.add(dstIP2);
                                            entryFields.add(Short.toString(vlan_match2));
                                            entryFields.add(Short.toString(inPort2));
                                            entries.put(Long.toString(swId), entryFields);

                                            if (!flg && !flg2) {
                                                rs.setAttribute("stopTime", Double.toString(currTime));
                                                newPaths.add(rs);
                                                //System.out.println("new Path is ready to be established for request " + reqID);
                                                countRedirection++;
                                                //System.out.println("countRedirection = " + countRedirection);
                                            }
                                        }
                                    }
                                }

                                HashMap<String, String> mEntries = new HashMap<>();
                                mEntries.put(srcIP, dstIP);

                                if (!matchEntries.containsKey(reqID)) {
                                    matchEntries.put(reqID, mEntries);
                                } else // request exists
                                {
                                    if (!matchEntries.get(reqID).containsKey(srcIP)
                                            || !matchEntries.get(reqID).get(srcIP).equals(dstIP)) {

                                        matchEntries.get(reqID).put(srcIP, dstIP);
                                    }
                                }

                                if (matchEntries.get(reqID).size() < 2) { 
                                    log.info("remove reverse path");
                                    log.info("dstIP - srcIP "+dstIP+" - "+srcIP);
                                    removeFlows(dstIP, srcIP);
                                }
                            }
                        }
                   
                    }

                }
            }

                
            } else {
              //  System.out.println("Cannot find XML document for the specified request");
                System.exit(1);
            }
            
            // active_requests.remove(req);//by molka
          //   System.out.println("number of active requests: "+active_requests.size());
        }

    }

    public static class TupleServedRequest<SRC_IP, DST_IP, SERVICE_TIME, START_TIME> implements java.io.Serializable { //Tuple<srcMac, srcIP, swId, Port>

        public final SRC_IP src_ip;
        public final DST_IP dst_ip;
        public final SERVICE_TIME service_time;
        public final START_TIME start_time;

        public TupleServedRequest(SRC_IP src_ip, DST_IP dst_ip, SERVICE_TIME service_time, START_TIME start_time) {
            this.src_ip = src_ip;
            this.dst_ip = dst_ip;
            this.service_time = service_time;
            this.start_time = start_time;
        }
    }

    public void messageReceived(Message msg2) throws IOException, InterruptedException {

        msgList2.add(msg2);
        Message msg;
        for (Iterator<Message> msgIter = msgList2.iterator(); msgIter.hasNext();) {
            msg = msgIter.next();
            msgList2.remove(msg);

            String command = msg.getCommand();
            log.info("Received message -> " + command);

            String session = msg.getSessionID();
                    
                 if (command.equals("StatisticsRequest")) {

                FSM f = new ControllerFsm(this, session);

                fsmList.put(session, f);
                f.stateSTART(msg);

            } else if (command.equals("StatisticsRequestFromDSE")) {

                FSM f = new TrcFeFSM(this, session);
                fsmList.put(session, f);
                f.stateSTART(msg);

            } else if (command.equals("StatisticsReply")) {
               // System.out.println("Statistics reply received");

                //XMLUtility.getInstance().printXML(((BNSMessage) msg).getValue());
                String value1;
                Document d = ((BNSMessage) msg).getValue();
                Element rootElement = d.getDocumentElement();
                value1 = d.getTextContent();
                this.sendMessage(new AEMessage(d, msg.getSessionID()));
              //  System.out.println("statistics reply sent to AE");

            } 

            else if (command.equals("FlowSetupRequest")) {
                
               String reqID = ((Element) ((AEMessage)msg).getValue().getElementsByTagName("requestID").item(0)).getTextContent();
               
               // received_requests_doc.put(id_received_request, ((AEMessage)msg).getValue());
                received_requests_doc.put(Integer.parseInt(reqID), ((AEMessage)msg).getValue());
                
                current_bytes_flow_forward.put(reqID, 0.0);
               // current_bytes_flow_backward.put(reqID, 0.0);
                
                startTime = System.currentTimeMillis();
               
                //received_requests_time.put(id_received_request, startTime);
                received_requests_time.put(Integer.parseInt(reqID), startTime);
                
                //id_received_request = id_received_request + 1;
                
                log.info("Setup Simple path request received from AE");
             
                //startTime = System.currentTimeMillis();

                FSM f = new ControllerFsm(this, session);

                fsmList.put(session, f);
                f.stateSTART(msg);

            }
            
            
            else if (command.equals("ServiceCancellationRequest")) {

                FSM f = new ControllerFsm(this, session);
              
                fsmList.put(session, f);
                f.stateSTART(msg);

            } 
            
      /********************************************************************************************************************************/      
            
            else if (command.equals("ServiceDeliveryRequest")) {
                
                //received = true;
                
                String reqID = ((Element) ((AEMessage)msg).getValue().getElementsByTagName("requestID").item(0)).getTextContent();
               
               // received_requests_doc.put(id_received_request, ((AEMessage)msg).getValue());
                 received_requests_doc.put(Integer.parseInt(reqID), ((AEMessage)msg).getValue());
                
                current_bytes_flow_forward.put(reqID, 0.0);
              //  current_bytes_flow_backward.put(reqID, 0.0);
                
                received_requests_doc.put(Integer.parseInt(reqID), ((AEMessage)msg).getValue());
                
                startTime = System.currentTimeMillis();
                //received_requests_time.put(id_received_request, startTime);
                received_requests_time.put(Integer.parseInt(reqID), startTime);
                
                //id_received_request = id_received_request + 1;
                
                log.info("ServiceDeliveryRequest <-> Composite request received from AE");
                
                Document doc = ((AEMessage)msg).getValue();

                XMLUtility.getInstance().printXML(doc);
                String requestID =  doc.getElementsByTagName("requestID").item(0).getTextContent();
                
                log.info("requestID "+requestID);

                FSM f = new ControllerFsm(this, session);

                fsmList.put(session, f);
                f.stateSTART(msg);

            } else if (command.equals("RouteInfoReply")) {
                
              //  System.out.println("RouteInfoReply received from BNS");

                //System.out.println("ControllerFsm");
                FSM f = new ControllerFsm(this, session);

                fsmList.put(session, f);
                f.stateSTART(msg);

            } 
            else if (command.equals("DeleteFlowFromDSE")) {

                FSM f = new TrcFeFSM(this, session);
                fsmList.put(session, f);
                f.stateSTART(msg);

            } 
            
            else if (command.equals("ServiceDeliveryReply") || command.equals("FlowSetupReply")) {
                
                                               
                log.info("we are in reply!!!");
                
               
                
                
              //  XMLUtility.getInstance().printXML(((BNSMessage) msg).getValue());

                Document d = ((BNSMessage) msg).getValue();
                if (d.getDocumentElement().getElementsByTagName("Response").getLength() > 0) {
                    String requestID = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("requestID");
                    String serviceTime = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("serviceTime");
                    String iteration = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("iteration");
                    String srcIp = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("src");
                    String dstIp = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("dst");
          
                    
                    String direction = "";
                    direction = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("direction");;
                    //direction = ((Element) d.getDocumentElement().getElementsByTagName("direction").item(0)).getNodeName();
                   
                 //   System.out.println("path direction: "+direction);
                    
                    Thread.sleep(100);
                    
               //     System.out.println("srcIp: "+srcIp);
              //      System.out.println("dstIp: "+dstIp);
                    
                   // if (command.equals("FlowSetupReply"))
                  //  {
                    tg.send(requestID + "///" + srcIp + "///" + dstIp + "///" + "1///15///" + serviceTime + "***");
                  //  }
                     
                    double start_time = System.currentTimeMillis();
                   // System.out.println("requestID=" + requestID + " iteration=" + iteration + " serviceTime=" + serviceTime + " start_time=" + start_time);
                    String reqID = ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).getAttribute("requestID");
                    //System.out.println("reqID = " + reqID + " requestID=" + requestID);
                    
                    String newIteration = "";
                    
                   
                    
                    if(iteration.contains("_"))
                    {
                    String[] tempIt = new String[2];
                    tempIt = iteration.split("_");
                    newIteration = String.valueOf(Integer.parseInt(tempIt[0]) + 1).concat("_").concat(direction);
                    }
                    else
                    {
                    newIteration = String.valueOf(Integer.parseInt(iteration) + 1).concat("_").concat(direction);
                    }    

                  //  System.out.println("iteration: "+iteration);
                //   System.out.println("5direction: "+direction);
                    
                    
                    if (reqID.equals(requestID)) {
                        ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).setAttribute("startTime", Double.toString(start_time));
                        ((Element) d.getDocumentElement().getElementsByTagName("Response").item(0)).setAttribute("iteration", newIteration);
                    }
                    
                     log.info("save XML: "+requestID.concat("-").concat(direction) + "_" +iteration +".xml");
                  //  XMLUtility.getInstance().saveXML(d, "paths/path" + requestID.concat("-").concat(direction) + "_" + newIteration +".xml");
                       XMLUtility.getInstance().saveXML(d, "paths/path" + requestID.concat("-").concat(direction) + "_" + iteration +".xml");
                    
                   // XMLUtility.getInstance().printXML(d);

                  //  if (!requestID.equals("0")) {
                        if (!accepted_requests.containsKey(requestID.concat("-").concat(direction))) {
                            accepted_requests.put(requestID.concat("-").concat(direction), iteration);
                           // accepted_requests.put(requestID.concat("-").concat(direction), newIteration);
                            count_accepted_requests++;
                            
                            
                            log.info("reqID concat direction " + requestID.concat("-").concat(direction));
                            log.info("iteration " + iteration);
                            
                        } else {
                            accepted_requests.remove(requestID.concat("-").concat(direction));
                            accepted_requests.put(requestID.concat("-").concat(direction), iteration);
                           // accepted_requests.put(requestID.concat("-").concat(direction), newIteration);
                        }
                  //  }

                    active_requests.add(requestID.concat("-").concat(direction));    
                  //  log.info("active_requests.size(): "+active_requests.size());
                    log.info("Traffic generation request has been sent for request no. " + requestID);
                    
                    generated_requests.add(requestID);
                } else {
                    //System.err.println("Invalid message received from BNServer");
                       System.err.println("Rejected request: no path found");
                }
                

        
            } else if (command.equals("ServiceCancellationReply")) {
                //System.out.println("Flow delete reply received");

                //XMLUtility.getInstance().printXML(((BNSMessage) msg).getValue());
                Document d = ((BNSMessage) msg).getValue();
                this.sendMessage(new AEMessage(d, msg.getSessionID()));
              //  System.out.println("ServiceCancellationReply reply sent to AE");

            } 


            FSM f = (FSM) fsmList.get(session);
            if (f != null) {
                f.execute(msg);
            }

        }
    }

    public synchronized void sendMessage(Message msg) {
        
       // System.out.println("we are in send  MESSAGE: "+msg.getType());
        
        switch (msg.getType()) {
            case Message.AE_MSG:
                aeManager.sendMessage(msg);
              //  System.out.println("send  MESSAGE AE: ");
                break;

            case Message.DSE_MSG:
                dseManager.sendMessage(msg);
                break;

            case Message.BNS_MSG:
                bnsManager.sendMessage(msg);
              //  System.out.println("send  MESSAGE BNS: ");
                break;
        }
    }
    


    public void deleteFsm(String ses) {

        fsmList.remove(ses);
    }

    protected void sendResponseToAe(Message msg, String value1, String value2) {
      //  System.out.println(value1);
        Document doc = createAEResponse(value1, value2);
       // System.out.println("Doc to send to AE ");
        //XMLUtility.getInstance().printXML(doc);
        this.sendMessage(new AEMessage(doc, msg.getSessionID()));
    }

    public Document createAEResponse(String value1, String value2) {
        Document doc = XMLUtility.getInstance().createDocument();
        Element root = doc.createElement("ServiceRequestResponse");
        doc.appendChild(root);
        Element resp = doc.createElement("Response");
        resp.appendChild(doc.createTextNode(value1));
        root.appendChild(resp);
        Element server = doc.createElement("serverIP");
        server.appendChild(doc.createTextNode(value2));
        root.appendChild(server);
        return doc;
    }

    public static void main(String[] args) throws IOException {

        System.out.println("********** NEW DSE ************");

        DSE dse = new DSE();
        dse.init(args);
        
        //Document database = XMLUtility.getInstance().loadXML("LSP_DB.xml");
        Document policy = XMLUtility.getInstance().loadXML("Policy.xml");
        //XMLUtility.getInstance().printXML(policy);

        Document clsDB = XMLUtility.getInstance().loadXML("Class.xml");
        //XMLUtility.getInstance().printXML(clsDB);

        dse.getPolicyDB().setDB(policy, clsDB);

        dse.placeMiddleboxes(); //initial placement of middleboxes
        dse.placeMiddleboxesFlow(); //initial placement of middleboxes
        //System.out.println("Done intial middlebox placement!");

        Document Services = XMLUtility.getInstance().loadXML("Services.xml");
        //XMLUtility.getInstance().printXML(Services);
        Document switchMboxes = XMLUtility.getInstance().loadXML("SwitchMBoxes.xml");
        getMiddleboxDB().setDB(Services, switchMboxes/*, ServiceInstances*/); //should be called after 'placeMiddleboxes'       
        //XMLUtility.getInstance().printXML(switchMboxes);
        
    
    }

    public synchronized void statisticsReceived(ConcurrentHashMap<Long, TupleStatistics<ArrayList<String>, Long, Long>> stats) throws IOException, FileNotFoundException, InterruptedException {
        
        log.info("STATISTICS RECEIVED FROM CONTROLLER");
        statsModel = stats;
        Set<Long> ks = stats.keySet();
        
        double startRedirection = 0.0;
       
        TupleStatistics<ArrayList<String>, Long, Long> tupleStatistics;
      
         HashMap<Long, TupleStatistics<ArrayList<String>, Long, Long>> tmpStatistics = new HashMap<>();
        long bytes_in_a_period, total_bytes;
        log.info("************************************************");
        
        log.info("ks "+ks);
        log.info("doRedirection  "+doRedirection);
      //  System.out.println("received  "+received);

        if (doRedirection) {
            
            allNewPaths = new ArrayList<>();
            deletedEntries = new ArrayList<>();
            
            boolean overlo = false;
            
            startRedirection = System.currentTimeMillis();
            
            for (long key : ks) {
                tupleStatistics = stats.get(key);
                bytes_in_a_period = tupleStatistics.bytes_in_a_period;
                //total_bytes = tupleStatistics.total_bytes;
         
                bwNumBytes.get((int) (key - 1)).append(Long.toString(bytes_in_a_period));
                bwNumBytes.get((int) (key - 1)).append("*");
                      
               // System.out.println("sw=" + key + " bytes_in_a_period = " + bytes_in_a_period);
                
                double throughput_in_a_period = (bytes_in_a_period / BNServer.stats_duration_sec);
                log.info("sw=" + key + " throughput_in_a_period = " + throughput_in_a_period);
                
                throughput_per_switch.put(String.valueOf(key), throughput_in_a_period);
                
                log.info("Added stat in throughtput per switch "+throughput_per_switch);
                
                log.info("we are in statistics Received "+System.nanoTime());
                
               // if (switchesWithMiddlebox.contains(key) && bytes_in_a_period >= threshold) {
               if (switchesWithMiddlebox.contains(key) && throughput_in_a_period >= threshold) { 
                    
                    log.info("ALARM RAISED: OVERLOADED SWITCHES "+System.nanoTime());
                    
                    overlo = true;
                    
                    
                    if (switchesWithMiddlebox.size() > 1) {
                    

                        double sTimeRemove = System.currentTimeMillis();

                        log.info("switch " + key + " is overloaded, not available at the moment");
                        int overLCount=0;
                        overLCount++;
                      /*  File overL = new File ("switchOverloaded.txt");
                        try (BufferedWriter bwOverL = new BufferedWriter (new FileWriter (overL,true))) {
                            bwOverL.append(Long.toString(key)).append(" ");
                            bwOverL.append(Integer.toString(overLCount));
                            bwOverL.newLine();
                            bwOverL.flush();
                        }*/
                        
                      
                        tmpStatistics.put(key, tupleStatistics);

                       overloadedSwitches.add(key);
                  
                       switchesWithMiddlebox.remove(key);
                        
                       
                     /*   double fTimeRemove = System.currentTimeMillis();
                        totalRedirTime += (double) ((fTimeRemove - sTimeRemove) / 1000);
                        File redTime = new File ("redirectionTime.txt");
                        try (BufferedWriter bwRed = new BufferedWriter (new FileWriter (redTime,true))) {
                            bwRed.append(Double.toString(totalRedirTime)).append(" ");
                            bwRed.append(Integer.toString(count_rejected_req));
                            bwRed.newLine();
                     bwRed.flush();
                        }*/
                    }

                    

                } else if (overloadedSwitches.contains(key) && throughput_in_a_period < threshold) { //switch released/available again

                    double sTimeRelease = System.currentTimeMillis();
                   

                    overloadedSwitches.remove(key);
                    
                    switchesWithMiddlebox.add(key);
                    log.info("switch " + key + " released/available again");

                    double fTimeRelease = System.currentTimeMillis();

                    totalRedirTime += (double) ((fTimeRelease - sTimeRelease) / 1000);
                    
                       
                }

            }

           log.info("Number of overloadedSwitches = " + overloadedSwitches.size());
            
            log.info("tmpStatistics.size() = " + tmpStatistics.size());
          
            active_requests_tmp = new CopyOnWriteArrayList<>(active_requests);
            accepted_requests_tmp = new ConcurrentHashMap<>(accepted_requests);

            
    /***************************************IDENTIFY ACTIVE FLOWS to rEMOVE********************************************** 
            //active flows 
            for (long sId : tmpStatistics.keySet()) {

               // TupleStatistics<ArrayList<ArrayList<Object>>, Long, Long> tmpSt = tmpStatistics.get(sId);
                TupleStatistics<ArrayList<String>, Long, Long> tmpSt = tmpStatistics.get(sId);
                
             System.out.println("IDENTIFY flows from switch: "+sId);   
             
             ArrayList<String> requestsIdentified = returnFlowsToRemoveWithoutRemoving(tmpSt.flows, sId);

              System.out.println("requestsIdentified.size() "+requestsIdentified.size());

            }
            
     /***************************************************************************************************

            placeMiddleboxes();
            
         //molka  Thread.sleep(10);//try

          //  System.out.println("Services.xml file");
            
            Document Services = XMLUtility.getInstance().loadXML("Services.xml");
           // XMLUtility.getInstance().printXML(Services);
            
            Document switchMboxes = XMLUtility.getInstance().loadXML("SwitchMBoxes.xml");
            getMiddleboxDB().setDB(Services, switchMboxes); //should be called after 'placeMiddleboxes'
        
            System.out.println("New Placement");
            
        //    System.out.println("switchMboxes.xml file");
         //   XMLUtility.getInstance().printXML(switchMboxes);
            
             if (allNewPaths.isEmpty())
             {
             System.out.println("allNewPaths is empty!!");
             }

           // if (!allNewPaths.isEmpty()) {
                double sTimeRedirect = System.currentTimeMillis();
                // redirectFlows(allNewPaths);
         
                resendRequestAfterIdentification(requestsToResend);
                double fTimeRedirect = System.currentTimeMillis();
                totalRedirTime += (double) ((fTimeRedirect - sTimeRedirect) / 1000);
          //  }
            
             double finishRedirection = System.currentTimeMillis();
             
             double redirTime = ((finishRedirection - startRedirection) / 1000);
             
             System.out.println("startRedirection "+startRedirection);
             System.out.println("finishRedirection "+finishRedirection);
             System.out.println("redirTime "+redirTime);
              if(overlo)
             {
                 writeinFile("results/redirectionTime.txt", redirTime);
             }
             
             
     /**************************************REMOVE ACTIVE FLOWS**********************************************   
             
            System.out.println("START REMOVING FLOWS"); 
             
            //REMOVE active flows 
            for (long sId : tmpStatistics.keySet()) {//for every switch involved in the path


               TupleStatistics<ArrayList<String>, Long, Long> tmpSt = tmpStatistics.get(sId);
                
             System.out.println("remove flows from switch: "+sId);  
             
           removeRequestAfterIdentification(requestsToResend, tmpSt.flows, sId);
            
            // ArrayList<Element> requestsRemoved = removeFlows(tmpSt.flows, sId);
             //   System.out.println("requestsRemoved.size() "+requestsRemoved.size());
                

                
            }
            
            System.out.println("requestsToResend.size() "+requestsToResend.size());
 
         /************************************************************************************/   
            
        } else {
            for (long key : ks) { // for no-redirection only
                tupleStatistics = stats.get(key);
                bytes_in_a_period = tupleStatistics.bytes_in_a_period;
                total_bytes = tupleStatistics.total_bytes;
                bwNumBytes.get((int) key - 1).append(Long.toString(bytes_in_a_period));
                bwNumBytes.get((int) key - 1).append("*");
                //System.out.println("sw=" + key + " total bytes(load) = " + total_bytes + " bytes_in_a_period = " + bytes_in_a_period);  
            }
        }

        refreshActiveRequests();

      /*  for (int s = 1; s <= 11; s++) {
            bwNumBytes.get(s - 1).newLine();
            bwNumBytes.get(s - 1).flush();
        }*/

    /*   System.out.println("************************************************");
l
        System.out.println("numRequests=" + OpenflowPanel.numRequests + " count_req=" + count_req + " count_accepted_requests=" + count_accepted_requests + " count_rejected_req=" + count_rejected_req);
        int[] numFlowEntries = new int[stats.size()];
        TupleStatistics<ArrayList<String>, Long, Long> tupleStatistics2;
        TupleStatistics<ArrayList<String>, Long, Long> tupleStatisticsSw4;

        if (!isLoadEvaluated && (count_accepted_requests + count_rejected_req >= (OpenflowPanel.numRequests / 2))) {
            FileWriter fwSwitchLoad = new FileWriter("results/switchLoad", true);
            try (BufferedWriter bwSwitchLoad = new BufferedWriter(fwSwitchLoad)) {
                long[] allBytes = new long[stats.size()];
                int i;
                for (long k : ks) {
                    tupleStatistics2 = stats.get(k);
                    bytes_in_a_period = tupleStatistics2.bytes_in_a_period;
                    total_bytes = tupleStatistics2.total_bytes;
                    if (k < Integer.MIN_VALUE || k > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException(k + " cannot be cast to int without changing its value.");
                    }
                    i = (int) k;

                    allBytes[i - 1] = total_bytes;
                    //}
                }

                for (int j = 0; j < allBytes.length; j++) {
                    bwSwitchLoad.append("sw=" + (j + 1) + "\tload(bytes)=" + allBytes[j]);
                    bwSwitchLoad.newLine();
                }
                bwSwitchLoad.close();
            }

            isLoadEvaluated = true;

            System.out.println("switch load collected");

        }
        if (!isRatioEvaluated && active_requests.isEmpty() && (count_accepted_requests + count_rejected_req == OpenflowPanel.numRequests)) {
            FileWriter fwSwitchLoad = new FileWriter("results/switchLoad2", true);
            FileWriter fwNumFlowEntries = new FileWriter("results/numFlowEntries", true);
            BufferedWriter bwNumFlowEntries = new BufferedWriter(fwNumFlowEntries);
            try (BufferedWriter bwSwitchLoad2 = new BufferedWriter(fwSwitchLoad)) {
                long[] allBytes = new long[stats.size()];
                int i;
                for (long k : ks) {
                    tupleStatistics2 = stats.get(k);
                    total_bytes = tupleStatistics2.total_bytes;
                    if (k < Integer.MIN_VALUE || k > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException(k + " cannot be cast to int without changing its value.");
                    }
                    i = (int) k;
                    allBytes[i - 1] = total_bytes;

                    numFlowEntries[i - 1] = tupleStatistics2.flows.size();
                }

                for (int j = 0; j < allBytes.length; j++) {
                    bwSwitchLoad2.append("sw=" + (j + 1) + "\tload(bytes)=" + allBytes[j]);
                    bwSwitchLoad2.newLine();
                    bwNumFlowEntries.append("sw=" + (j + 1) + "\numFlows=" + numFlowEntries[j]);
                }
                bwSwitchLoad2.close();
                bwNumFlowEntries.close();
            }
            isLoadEvaluated = true;
            System.out.println("switch load collected");

            FileWriter fwRatio = new FileWriter("results/rejectionAcceptanceRatio", true);
            try (BufferedWriter bwRatio = new BufferedWriter(fwRatio)) {
                float ratio = (float) ((count_rejected_req * 100) / OpenflowPanel.numRequests);
                bwRatio.append(String.valueOf(ratio));
                bwRatio.newLine();
                bwRatio.close();

                System.out.println("Rejection acceptance ratio evaluated");
                isRatioEvaluated = true;
            }

            double avgRedirTime = (double) (totalRedirTime / countRedirection);
            FileWriter fwAvgRedTime = new FileWriter("results/avgRedTime", true);
            try (BufferedWriter bwAvgRedTimeAndOverhead = new BufferedWriter(fwAvgRedTime)) {
                bwAvgRedTimeAndOverhead.append(String.valueOf("avgRedirectionTime=" + avgRedirTime));
                bwAvgRedTimeAndOverhead.append(String.valueOf("noRedirections=" + countRedirection));
            }
        }*/
    }
    
    
    
public synchronized void statisticsPerFlowReceived(Document doc) throws IOException, FileNotFoundException, InterruptedException, Exception {
        
        log.info("STATISTICS RECEIVED PER FLOW");
        
        log.info("************************************************");
        
       // int requestedSLA = 500000;//1Mb   => Guarantee at least this value
        
       // XMLUtility.getInstance().printXML(doc);
        
        if ((doRedirectionFlow) && (doc != null)) {
            
            
        //for each request> create a doc file that contains its characteristics
            
        ArrayList<Document> xmlDocs = parseXMLFlowStatistics(doc);
         
         int numDocuments = xmlDocs.size();
         
         log.info("numDocuments: "+numDocuments);
            
         for(int j=0; j<numDocuments; j++)   
         {
        /********************************************************/  
             
             Document tmpDoc = xmlDocs.get(j);
            
      //  System.out.println("Redirect Flow if overloaded");
        
        XMLUtility.getInstance().printXML(tmpDoc);
        
        ArrayList<String> listFlowsperRequest = new ArrayList<>();
        
        ArrayList<String> listSwitchesperRequest = new ArrayList<>();
        
        String reqID = "";String numBytes = "0";
        
        NodeList flows = tmpDoc.getElementsByTagName("Flow");
        
        boolean raised_alarm = false;
        
        log.info("check forward direction");

        String temp_requestID = "";

            for(int indx=0; indx < flows.getLength(); indx++)
            {
                        Element fl = (Element) flows.item(indx);
                        reqID = fl.getAttribute("requestID");
                       // System.out.println("requestID: "+reqID);
                        String flID = fl.getAttribute("flowID");
                       // System.out.println("flowID: "+flID);
                        String tmpnumBytes = fl.getAttribute("numBytes");
                        
                        String switchID = fl.getAttribute("sw");
                        
                        if((Integer.valueOf(tmpnumBytes) >=0) && (reqID.contains("forward")))
                        {numBytes = tmpnumBytes;
                        listSwitchesperRequest.add(switchID);
                        }
                       
                        
                listFlowsperRequest.add(flID);
                
            }    
                        
          String[] result = reqID.split("-", 2);
          String request = result[0];
          
          temp_requestID = request;
         
      /*   if(requestsSLA.containsKey(request))
         {
         //requestedSLA = requestsSLA.get(request);
         System.out.println("requested SLA: "+requestedSLA);             
         }*/
         
        double currFlowBytes = Double.parseDouble(numBytes);
        double bytes_prev = current_bytes_flow_forward.get(request);
         
        double bytes_in_period = currFlowBytes - bytes_prev;
        
        log.info("currFlowBytes: "+currFlowBytes); log.info("bytes_prev: "+bytes_prev); log.info("bytes_in_period: "+bytes_in_period); 
         
        double throughput = (bytes_in_period / BNServer.stats_flow_duration_sec);
        
        if(throughput > 0) //we should guarantee a minimum SLA
                {
        writeinFile("results/throughput.txt", throughput);
                }
        
        writeinFile("results/numberBytes.txt", currFlowBytes);
        
        current_bytes_flow_forward.put(request, currFlowBytes);
         
        //  log.info("numBytes: "+doub_numBytes); 
          
          log.info("Throughput forward: "+throughput); 
                        
                if((currFlowBytes > requestedSLA) && (currFlowBytes > 0)) //we should guarantee a minimum SLA
                {
                log.info("ALARM RAISED - FORWARD DIRECTION: SLA NOT RESPECTED");
                raised_alarm = true;
                }
                
          
        log.info("check backward direction");

        numBytes = "0";
        temp_requestID = "";

            for(int indx=0; indx < flows.getLength(); indx++)
            {
                        Element fl = (Element) flows.item(indx);
                        reqID = fl.getAttribute("requestID");
                       // System.out.println("requestID: "+reqID);
                        String flID = fl.getAttribute("flowID");
                       // System.out.println("flowID: "+flID);
                        String tmpnumBytes = fl.getAttribute("numBytes");
                        
                        if((Integer.valueOf(tmpnumBytes) >=0) && (reqID.contains("backward")))
                        {numBytes = tmpnumBytes;}
                       
                        
                listFlowsperRequest.add(flID);
                
            }    
                        
          result = reqID.split("-", 2);
          request = result[0];
          
          temp_requestID = request;
         
       /*  if(requestsSLA.containsKey(request))
         {
         //requestedSLA = requestsSLA.get(request);
         System.out.println("requested SLA: "+requestedSLA);             
         }*/
         
        currFlowBytes = Double.parseDouble(numBytes);
       // bytes_prev = current_bytes_flow_backward.get(request);
         
        bytes_in_period = currFlowBytes - bytes_prev;
        
    //    System.out.println("currFlowBytes: "+currFlowBytes); System.out.println("bytes_prev: "+bytes_prev); System.out.println("bytes_in_period: "+bytes_in_period); 
         
        throughput = (bytes_in_period / BNServer.stats_flow_duration_sec);
        
       // current_bytes_flow_backward.put(request, currFlowBytes);
         
          log.info("numBytes: "+currFlowBytes); 
          
         // System.out.println("Throughput backward: "+throughput); 
                        
               if((currFlowBytes < requestedSLA) && (currFlowBytes > 0)) //we should guarantee a minimum SLA
                {
                log.info("ALARM RAISED - BACKWARD DIRECTION: SLA NOT RESPECTED");
                raised_alarm = true;
                }
                
           // }//for: check number of bytes in both directions: forward and backward
                
    /*****************************************************************************************************************/
    /****************************************************************************************************************    
        ArrayList<String> listSwitches = new ArrayList<>();
        ArrayList<String> listSwitches_forward = new ArrayList<>();
        ArrayList<String> listPorts = new ArrayList<>();
        ArrayList<String> listIntents = new ArrayList<>();
        
        NodeList listFlows = tmpDoc.getElementsByTagName("Flow");
        
        System.out.println("listFlows.getLength(): "+listFlows.getLength());
        
         for (int i = 0; i < listFlows.getLength(); i++) {
            
              //  System.out.println("i: "+i);
                Element cand = (Element) listFlows.item(i);
                
                String swID = "";String reqId = "";
                
                NamedNodeMap tmpAtt = cand.getAttributes();
                
              //  System.out.println("tmpAtt.getLength(): "+tmpAtt.getLength());
                
                for (int jdx=0; jdx < tmpAtt.getLength(); jdx++)
                {
                Node tmpNode = tmpAtt.item(jdx);
                
            //    System.out.println("tmpNode.getNodeName(): "+tmpNode.getNodeName());  
                
                if (tmpNode.getNodeName().contains("sw"))
                {
                //tmpswID = tmpNode.getNodeName();
                swID = tmpNode.getNodeValue();
                }
                
                
                 if (tmpNode.getNodeName().contains("request"))
                {
                reqId = tmpNode.getNodeValue();
                }
                }
                
                if(reqId.contains("forward"))
                {
                  listSwitches_forward.add(swID);
                }
                //String swID = cand.getAttribute("sw".concat(String.valueOf(i)));
               // System.out.println("swID: "+swID);
                listSwitches.add(swID);
                String portID = cand.getAttribute("port".concat(String.valueOf(i)));
                listPorts.add(portID);
                
                
            }
        
        NodeList listaIntents = tmpDoc.getElementsByTagName("Intent");
        
        for (int i = 0; i < listaIntents.getLength(); i++) {
            
                Element cand = (Element) listaIntents.item(i);
                String intentID = cand.getAttribute("intentID");
                listIntents.add(intentID);
            }
     
            HttpExample http = new HttpExample();

            double sTimeRemove = System.currentTimeMillis();
            
        System.out.println("listSwitches.size(): "+listSwitches.size());
        System.out.println("listFlowsperRequest.size(): "+listFlowsperRequest.size());
        
            //remove ALL the flows of the current request 
            
                for(int index=0; index < listFlowsperRequest.size(); index++)                
                {
                   String flowID = listFlowsperRequest.get(index);
                   
                   String switchID = listSwitches.get(index);
                   
                   if(index < listSwitches_forward.size())
                   {
                    String swIDForward = listSwitches_forward.get(index);
                    involvedSwitches.add(swIDForward);
                   }
                 //  String portID = listPorts.get(index);
                   
              
                   System.out.println("remove the flow with this ID: "+flowID); 
                   System.out.println("involved switch: "+switchID); 
                   
                   if(switchesWithMiddleboxPerFlow.contains(Long.valueOf(switchID)))
                   {switchesWithMiddleboxPerFlow.remove(Long.valueOf(switchID));}
                   
                  // ArrayList<String> flowsIentifiers = http.getFlowStatsPerPort(Long.valueOf(switchID), Integer.valueOf(portID));
                 //  System.out.println("flowsIentifiers.size(): "+flowsIentifiers.size());
                   
                 //   String flowID = flowsIentifiers.get(0);
                
                   removeCurrentFlow(flowID, switchID);
                               
                }
                
                listFlowsperRequest.clear();
                listSwitches.clear();
                listSwitches_forward.clear();
                listPorts.clear();
                
            //remove intents
                                  
                    for (int in1 = 0; in1 < listIntents.size(); in1++) {
                        
                       System.out.println("installedIntents(in1): "+listIntents.get(in1));
                       
                       http.deleteIntentbyID(listIntents.get(in1));
                    }
                    
            //redirect flow <=> resend the same request avoiding the INVOLVED switches

                //placeMiddleboxesFlow();//avoid overloaded switches

          //  Thread.sleep(10);//try
           // System.out.println("Services.xml file");        
            Document Services = XMLUtility.getInstance().loadXML("Services.xml");
         //   XMLUtility.getInstance().printXML(Services);
            
          //  Document switchMboxes = XMLUtility.getInstance().loadXML("SwitchMBoxes.xml");
         //   getMiddleboxDB().setDB(Services, switchMboxes); //should be called after 'placeMiddleboxesFlow'
          //  System.out.println("New Placement");
            
         //   System.out.println("switchMboxes.xml file");
         //   XMLUtility.getInstance().printXML(switchMboxes);
            
        //  refreshActiveRequests();//to be verified
            /*************************************reproduce only this part*******************************/
     
            
            if(raised_alarm)
           {
               
           log.info("we are in alarm raised"); 
           double startRedirection = System.currentTimeMillis();
            
            Document document = received_requests_doc.get(Integer.valueOf(temp_requestID));
           
           // XMLUtility.getInstance().printXML(document);

            String delreqID = ((Element)(document.getElementsByTagName("requestID").item(0))).getTextContent();
        
            if (delreqID.contains("30"))
            {
            String src = ((Element) document.getElementsByTagName("srcNode").item(0)).getAttribute("ip");
            String dst = ((Element) document.getElementsByTagName("dstNode").item(0)).getAttribute("ip");
            
            log.info("listSwitchesperRequest.size(): "+listSwitchesperRequest.size());
            
            for(int ind=0; ind<listSwitchesperRequest.size(); ind++)
            {   
            long key = Long.parseLong(listSwitchesperRequest.get(ind));
            log.info("key: "+key);
            if(switchesWithMiddlebox.contains(key))
            {
                 overloadedSwitches.add(key);
                 switchesWithMiddlebox.remove(key);
            }
            }
            
            log.info("switchesWithMiddlebox.size(): "+switchesWithMiddlebox.size());
            
            placeMiddleboxesFlow();
            placeMiddleboxes();
            Document Services = XMLUtility.getInstance().loadXML("Services.xml");          
            Document switchMboxes = XMLUtility.getInstance().loadXML("SwitchMBoxes.xml");
            getMiddleboxDB().setDB(Services, switchMboxes); //should be called after 'placeMiddlebo
            log.info("New Placement");
                                   
            boolean done = redirectOverloadedFlows(document);
            
            removePath(src, dst, delreqID);
            
            for(int ind=0; ind<listSwitchesperRequest.size(); ind++)
            {   
            long key = Long.parseLong(listSwitchesperRequest.get(ind));
            log.info("after remove> key: "+key);
            if(overloadedSwitches.contains(key))
            {
                 overloadedSwitches.remove(key);
                 switchesWithMiddlebox.add(key);
            }
            }
            
            placeMiddleboxesFlow();
            placeMiddleboxes();
            Services = XMLUtility.getInstance().loadXML("Services.xml");          
            switchMboxes = XMLUtility.getInstance().loadXML("SwitchMBoxes.xml");
            getMiddleboxDB().setDB(Services, switchMboxes); //should be called after 'placeMiddlebo
            log.info("New Placement");
                        
            double finishRedirection = System.currentTimeMillis();
            
            double redirectionTime = ((finishRedirection - startRedirection) / 1000);
            
            log.info("redirectionTime: "+redirectionTime);
            
            writeinFile("results/redirectionTime.txt", redirectionTime);

                }
            
         }
            
                
         //    }//redirect single flow if it does not respect the SLA
    /*****************************************************************************************************************/
    /***************************************************************************************************************** 
        
        double fTimeRemove = System.currentTimeMillis();
        totalFLOWRedirTime = (double) ((fTimeRemove - sTimeRemove) / 1000);
        System.out.println("totalFLOWRedirTime: "+totalFLOWRedirTime);
        File flowRedTime = new File ("FlowRedirectionTime.txt");
                        try (BufferedWriter bwRed = new BufferedWriter (new FileWriter (flowRedTime,true))) {
                            bwRed.append(Double.toString(totalFLOWRedirTime)).append(" ");
                            bwRed.newLine();
                            bwRed.flush();
                        }
        totalFLOWRedirTime = 0d;
        
    /*****************************************************************************************************************/       
  
                else
                {
        switchesWithMiddleboxPerFlow.clear();
        switchesWithMiddleboxPerFlow.add((long) 3);
        switchesWithMiddleboxPerFlow.add((long) 7);
        switchesWithMiddleboxPerFlow.add((long) 11);
       /* switchesWithMiddleboxPerFlow.add((long) 8);
        switchesWithMiddleboxPerFlow.add((long) 9);
        switchesWithMiddleboxPerFlow.add((long) 10);*/
                }
           
        
        switchesWithMiddleboxPerFlow.clear();
        switchesWithMiddleboxPerFlow.add((long) 3);
        switchesWithMiddleboxPerFlow.add((long) 7);
        switchesWithMiddleboxPerFlow.add((long) 11);
       /* switchesWithMiddleboxPerFlow.add((long) 8);
        switchesWithMiddleboxPerFlow.add((long) 9);
        switchesWithMiddleboxPerFlow.add((long) 10);*/
                
        }//for all the requests in the statistics file    
          
        } else {
            
            log.info("No Flow Redirection");
        
        }

       
        log.info("************************************************");

    }


/***********************************************************************************************************************/
/***********************************************************************************************************************/

   private void removeCurrentFlow(String flowID, String switchID) throws FileNotFoundException, IOException, InterruptedException, Exception {
       
      //  log.info(" Removing flow from switch " + switchID);
        
        HttpExample http = new HttpExample();
        
        http.deleteFlow(flowID, switchID);

    }
   
/**************************************************************************************************************************/
   
   /**************************************************************************************************************************/
   
       private double passedTime(int reqID) {
        
      //  System.out.println("we are REDIRECTING the overloaded flow");
        
        boolean done = false;
        
      //  XMLUtility.getInstance().printXML(doc);

              //  int reqID = Integer.valueOf(((Element)(doc.getElementsByTagName("requestID").item(0))).getTextContent());
        
            //    String src = ((Element) doc.getElementsByTagName("srcNode").item(0)).getAttribute("ip");
              //  String dst = ((Element) doc.getElementsByTagName("dstNode").item(0)).getAttribute("ip");
                
         //   double servTimeSeconds = Double.parseDouble(((Element)(doc.getElementsByTagName("serviceTime").item(0))).getTextContent());
                
                long startTime = received_requests_time.get(reqID);
                
               //  System.out.println("stTime: "+stTime);
                
            //    String iteration = ((Element)(doc.getElementsByTagName("iteration").item(0))).getTextContent();
            //    String sla = ((Element)(doc.getElementsByTagName("SLA").item(0))).getTextContent();
                
                long currTime = System.currentTimeMillis();
                double servedTime = (double) (currTime - startTime);
                
             //   System.out.println("servedTime: "+(servedTime / 1000));
                
            //    System.out.println("reqID: "+reqID);
          //      System.out.println("srcIP: "+src);
           //     System.out.println("dstIP: "+dst);

            //    double remainedTimeSeconds = (double) (servTimeSeconds - (double) (servedTime / 1000));
           //     System.out.println("remTimeSeconds=" + remainedTimeSeconds);
                
          //      if(remainedTimeSeconds > 0)
         //       {
                //    System.out.println("Setting new path (iteration=" + iteration + ") for request " + reqID + " from " + src + " to " + dst + " excluding the overloaded switch(es)");
         //           resendRequest(reqID, remainedTimeSeconds, iteration, sla, src, dst);//fwd
         //           done = true;
         //       }

                return servedTime;
    }
   
   
   
/**************************************************************************************************************************/
   
       private boolean redirectOverloadedFlows(Document doc) {
        
        log.info("we are REDIRECTING the overloaded flow");
        
        boolean done = false;
        
       // XMLUtility.getInstance().printXML(doc);

                int reqID = Integer.valueOf(((Element)(doc.getElementsByTagName("requestID").item(0))).getTextContent());
        
                String src = ((Element) doc.getElementsByTagName("srcNode").item(0)).getAttribute("ip");
                String dst = ((Element) doc.getElementsByTagName("dstNode").item(0)).getAttribute("ip");
                
                double servTimeSeconds = Double.parseDouble(((Element)(doc.getElementsByTagName("serviceTime").item(0))).getTextContent());
                
                long stTime = received_requests_time.get(reqID);
                
             //    log.info("stTime: "+stTime);
                
                String iteration = ((Element)(doc.getElementsByTagName("iteration").item(0))).getTextContent();
                String sla = ((Element)(doc.getElementsByTagName("SLA").item(0))).getTextContent();
                
                long currTime = System.currentTimeMillis();
                double servedTime = (double) (currTime - stTime);
                
            //    log.info("servedTime: "+(servedTime / 1000));
                
            //    log.info("reqID: "+reqID);
            //    log.info("srcIP: "+src);
           //     log.info("dstIP: "+dst);

                double remainedTimeSeconds = (double) (servTimeSeconds - (double) (servedTime / 1000));
          //      log.info("remTimeSeconds=" + remainedTimeSeconds);
                
                if(remainedTimeSeconds > 0)
                {
                    log.info("Setting new path (iteration=" + iteration + ") for request " + reqID + " from " + src + " to " + dst + " excluding the overloaded switch(es)");
                    resendRequest(reqID, remainedTimeSeconds, iteration, sla, src, dst);//fwd
                    done = true;
                }

                return done;
    }

/***************************************************************************************************************************/
/***************************************************************************************************************************/

   private ArrayList<Document> parseXMLFlowStatistics(Document parseFlow)
        {
            
        log.info("we are parsing the document");
        
       // XMLUtility.getInstance().printXML(parseFlow);
            
        ArrayList<Document> xmlDocs = new ArrayList<>();
        
         NodeList flows = parseFlow.getElementsByTagName("Flow");
         
         ArrayList<String> requestIDs = new ArrayList<>();
         
         int tempID=2;
         ConcurrentHashMap<String, String> sources = new ConcurrentHashMap<String, String>();
         ConcurrentHashMap<String, String> destinations = new ConcurrentHashMap<String, String>();

            for(int indx=0; indx < flows.getLength(); indx++)
            {
                        Element fl = (Element) flows.item(indx);
                        String reqID = fl.getAttribute("requestID");
                        String[] result = reqID.split("-", 2);
                        String request = result[0];
                        log.info("request: "+request);
                        
                        Document tempDoc = (Document)received_requests_doc.get(Integer.valueOf(request));
                       // XMLUtility.getInstance().printXML(tempDoc);
                        
                        String source = ((Element)tempDoc.getElementsByTagName("srcNode").item(0)).getAttribute("ip");
                        String destination = ((Element)tempDoc.getElementsByTagName("dstNode").item(0)).getAttribute("ip");
                        
                        sources.put(request, source);
                        destinations.put(request, destination);
                        
                        requestIDs.add(request);
                        
                       int tempo = Integer.valueOf(request);
                        
                        if(tempID < tempo)
                        {
                        tempID = tempo;
                        }
            } 
            
            log.info("tempID: "+tempID);
            
            int i=1;
            Document docStats = XMLUtility.getInstance().createDocument();
            Element rootStat = docStats.createElement("Statistics");
            docStats.appendChild(rootStat);    
            
            while((i>0) && (i<=tempID))
            {
            if(requestIDs.contains(String.valueOf(i)))
            {
            log.info("i: "+i);
            
            Document docRes = XMLUtility.getInstance().createDocument();
            Element root = docRes.createElement("FlowStatistics");
            docRes.appendChild(root);
            
            Element sliceStat = docStats.createElement("Statistic");   
            rootStat.appendChild(sliceStat);    
            Element sliceID = docStats.createElement("SliceID");   
            sliceStat.appendChild(sliceID);
            Element source = docStats.createElement("sourceIP");   
            sliceStat.appendChild(source);
            Element destination = docStats.createElement("destinationIP");   
            sliceStat.appendChild(destination);
            Element Bytes = docStats.createElement("NumberBytes");
            sliceStat.appendChild(Bytes);   
                  
            
            NodeList tmpflows = parseFlow.getElementsByTagName("Flow");
         
            for(int indx=0; indx < tmpflows.getLength(); indx++)
            {
   
                       // sliceID.setTextContent("0");
                      //  Bytes.setTextContent("0");
                        
                     //   sliceStat.appendChild(sliceID);
                     //   sliceStat.appendChild(Bytes);
                        
                        Element fl = (Element) tmpflows.item(indx);
                        String reqID = fl.getAttribute("requestID");
                        String[] result = reqID.split("-", 2);
                        String request = result[0];
                        
                        String numBytes = fl.getAttribute("numBytes");
                        
                        Node newNode = docRes.importNode(tmpflows.item(indx), true);
                        
                        if(request.equals(String.valueOf(i)))
                        {
                        root.appendChild(newNode);
                        }
                        
                        if((reqID.contains("forward")) && (request.equals(String.valueOf(i))))
                        {
                        sliceID.setTextContent(request);
                        source.setTextContent(sources.get(i));
                        destination.setTextContent(destinations.get(i));
                        Bytes.setTextContent(numBytes);
                        }
                        
           
            
             
            } 
            
            NodeList tmpintents = parseFlow.getElementsByTagName("Intent");
         
            for(int indx=0; indx < tmpintents.getLength(); indx++)
            {
                        Element intent = (Element) tmpintents.item(indx);
                        String reqID = intent.getAttribute("requestID");
                        String[] result = reqID.split("-", 2);
                        String request = result[0];
                        
                        Node newNode = docRes.importNode(tmpintents.item(indx), true);
                        
                        if(request.equals(String.valueOf(i)))
                        {
                        root.appendChild(newNode);
                        }
            }
            
            xmlDocs.add(docRes);
            requestIDs.remove(String.valueOf(i));
            
            XMLUtility.getInstance().saveXML(docStats, "Statistics.xml");
                      
            }
            
            i++;
        }
            
            /*System.out.println("Different XML files");
            for(int j=0; j<xmlDoc.size(); j++)
            {
            Document tmp = (Document)xmlDoc.get(j);
            XMLUtility.getInstance().printXML(tmp);
            
            }*/
            
            return xmlDocs;
            
        }
               
  /***************************************************************************************************************************/
/***************************************************************************************************************************/

        
   public void placeMiddleboxesFlow() {
        
      //  System.out.println(" Place middleboxes for FLOW  redirection");
        
        HashMap<String, HashMap<String, Object>> swServiceMap = new HashMap<>(); // stores a key constructed from swid and servicetype
       
        String swID = null;
        int nSwitches = switchesWithMiddleboxPerFlow.size();
        
     //   System.out.println(nSwitches + " switches available for middlebox placement");
        
        if (nSwitches > 0) {
            
            ArrayList<Long> switchesToBeSelected = new ArrayList<>();
            Random r = new Random();
            for (int s = 0; s < nSwitches; s++) {
                long randomSWID;
                int randomIndex = r.nextInt(nSwitches);//intially 5

                randomSWID = switchesWithMiddleboxPerFlow.get(randomIndex);
                
                while (switchesToBeSelected.contains(randomSWID)) {
                    randomIndex = r.nextInt(switchesWithMiddleboxPerFlow.size()); //initially 5
                    randomSWID = switchesWithMiddleboxPerFlow.get(randomIndex);
                }
                switchesToBeSelected.add(randomSWID);
            }
            Document doc = XMLUtility.getInstance().createDocument();
            Element root = doc.createElement("SwitchMBoxes");
            doc.appendChild(root);

            Document mBox = XMLUtility.getInstance().loadXML("Services.xml");
            NodeList nodelist = mBox.getElementsByTagName("Services");

            boolean found;
            for (int i = 0; i < nodelist.getLength(); i++) {
                Element e = (Element) nodelist.item(i);
                NodeList ch = e.getElementsByTagName("Service");
                for (int j = 0; j < ch.getLength(); j++) {
                    Element m = (Element) ch.item(j);
                    String mbType = m.getAttribute("type");
                    String instance = m.getAttribute("vlid");

                    int randInd = r.nextInt(nSwitches);
                    swID = String.valueOf(switchesToBeSelected.get(randInd));

                    if (involvedSwitches.contains(String.valueOf(switchesToBeSelected.get(randInd)))) {
                        log.info("skipping involved Switch " + swID);
                        continue;
                    }
                    if (swServiceMap.containsKey(mbType)
                            && swServiceMap.get(mbType).values().size() < nSwitches
                            && swServiceMap.get(mbType).containsKey(swID)) {
                        found = false;
                        while (swServiceMap.get(mbType).values().size() < nSwitches
                                && swServiceMap.get(mbType).containsKey(swID)) {
                            randInd = r.nextInt(nSwitches);
                            swID = String.valueOf(switchesToBeSelected.get(randInd));
                        }
                        found = true;
                    } else {
                        found = true;
                    }
                    if (found) {
                        if (swServiceMap.containsKey(mbType)) {
                            swServiceMap.get(mbType).put(swID, null);
                        } else {
                            HashMap<String, Object> mp = new HashMap<>();
                            mp.put(swID, null);
                            swServiceMap.put(mbType, mp);
                        }

                        Element swMbox = doc.createElement("SwitchMBox");
                        swMbox.setAttribute("switch", swID);
                        swMbox.setAttribute("vlid", instance);
                        swMbox.setAttribute("port", "3");
                        swMbox.setAttribute("outPort", "4");
                        root.appendChild(swMbox);
                    }
                }
            }
            XMLUtility.getInstance().saveXML(doc, "SwitchMBoxes.xml");
        }

    }
   
/****************************************************************************************************************************/
/*****************************************************************************************************************************/
   
      private String getIPaddress(String MACaddress) {
          
        String ip_address = "";
        
        Document MACTable = XMLUtility.getInstance().loadXML("MACTable.xml");
        NodeList macEntries = MACTable.getElementsByTagName("MacEntries");
        Element macEntries2 = (Element) macEntries.item(0);
        NodeList entries = macEntries2.getElementsByTagName("MacEntry");

        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            
            String ip = entry.getAttribute("ip");
            String mac = entry.getAttribute("mac");
            
            if(mac.equalsIgnoreCase(MACaddress))
            {
            ip_address = ip;
            }
            
       
        }
        
        return ip_address;

    }
      
          /**
     * @param fileName*
     * @param value*
     * @throws java.io.IOException********************************************************************************************/
        
       public void writeinFile(String fileName, double value) throws IOException  {
           
        FileWriter fwMsg = new FileWriter(fileName, true);
        BufferedWriter restMsg = new BufferedWriter(fwMsg);
        restMsg.append(Double.toString(value));
        restMsg.newLine();
        restMsg.flush();
	
	}
    
   
}
