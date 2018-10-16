/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package StateListener;

import DSE.DSE;
import DSE.DSE.Tuple;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.Map.Entry;
//import jyang.parser.YANG_Body;
//import jyang.parser.YANG_Config;
//import jyang.parser.YANG_Specification;
//import jyang.parser.YangTreeNode;
//import jyang.parser.yang;
//import jyang.tools.Yang2Yin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.slf4j.LoggerFactory;


/**
 *
 * @author lara
 */
public class StateListenerNew extends Thread{
    private static final String YINFILE = "configuration/yinFile.json";
    private static final String YANGFILE = "configuration/yangFile.yang";
    private static final String MAPPINGFILE = "configuration/mappingFile.txt";
    private String AppId;
    //protected List<String> state;
    protected HashMap<String, Object> state;
    protected HashMap<String, Object> stateThreshold;
    //protected HashMap<String, ListValues> stateList;
    protected HashMap<String, String> lists;
    private Object root;
    private boolean stopCondition = false;
    private List<String> toListenPush;
    private HashMap<String, Threshold> toListenThreshold;
    //  private List<PeriodicVariableTask> toListenTimer;
    //private List<String> nullValuesToListen;
    private HashMap<String, String> YangToJava;
    private HashMap<String, String> YangType;
    private HashMap<String, Boolean> YangMandatory;
    //private List<NotifyMsg> whatHappened;
    //private ReadLock readLock;
    //private WriteLock writeLock;
    private ConnectionModuleClient cM;
    private final ObjectNode rootJson;
    private final ObjectMapper mapper;
    private HashMap<String, Object> stateNew;
    private HashMap<String, Boolean> config;
    private Timer timer;
    protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    protected Gson myGson = new Gson();

    /*****PERSONALIZABLE FUNCTIONS*******/

    private Object personalizedDeserialization(Class<?> type, String json){
        try{
            if(type==Tuple.class){
                ObjectNode node= (ObjectNode)mapper.readTree(json);
                //Long mac = (node.has("mac"))?node.get("mac").asLong():0;
                String mac = String.valueOf((node.has("mac"))?node.get("mac").asLong():0);
                //Integer ip = (node.has("ip"))?node.get("ip").asInt():0;
                String ip = String.valueOf((node.has("ip"))?node.get("ip").asInt():0);
                //Tuple<Long, Integer, Long, Short> ipMacEntry;
                Tuple<String, String, Long, Short> ipMacEntry;
                ipMacEntry = ((DSE)root).new Tuple<>(mac, ip, (long)0, (short)0);
                return ipMacEntry;
            }
            /*if(type == Ip4Address.class){
                Ip4Address value = Ip4Address.valueOf(json);
                return value;
            }
            if(type == PortNumber.class){
                log.info("E' un port number, the value passed is "+json+" and the type is "+type);
                PortNumber value = PortNumber.portNumber(json);
                return value;
            }
            if(type == DeviceId.class){
                DeviceId value = DeviceId.deviceId(json);
                return value;
            }*/
        }catch(Exception e){
            //log.info("Can't convert the json correctly");
            log.error(e.getMessage());
            return null;
        }
        return null;
    }

    private Object personalizedNewInstance(Class<?> valueType){
        if(valueType == Tuple.class){
            return ((DSE)root).new Tuple<Long, Integer, Long, Short>();
        }
        return null;
    }

    private Object personalizedSerialization(String field, Object value){
        if(value==null){
            return null;
        }
        try {
            String type = YangType.get(field);
            if (type == null) return null;
            if (type.equals("boolean")) return Boolean.parseBoolean(value.toString());
            if (type.equals("uint16")) return Integer.parseInt(value.toString());
            if (type.equals("int32")) return Integer.parseInt(value.toString());
            if (type.equals("decimal64")) return Double.parseDouble(value.toString());
            if (type.equals("inet:port-number")) return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return value.toString();
    }

    /***********END OF PERSONALIZED PART
     ******************************/

    public StateListenerNew(Object root){
//        log.info("In constructor");
        this.root = root;
        state = new HashMap<>();
        stateThreshold = new HashMap<>();
        //stateList = new HashMap<>();
        toListenPush = new ArrayList<>();
        toListenThreshold = new HashMap<>();
        // toListenTimer = new ArrayList<>();
        //nullValuesToListen = new ArrayList<>();
        YangToJava = new HashMap<>();
        YangType = new HashMap<>();
        YangMandatory = new HashMap<>();
        //whatHappened = new ArrayList<>();
        //ReentrantReadWriteLock wHLock = new ReentrantReadWriteLock();
        //readLock = wHLock.readLock();
        //writeLock = wHLock.writeLock();
        lists = new HashMap<>();
        config = new HashMap<>();
        mapper = new ObjectMapper();
        timer = new Timer();
        //PARSE YANG FILE
        ClassLoader loader = StateListenerNew.class.getClassLoader();

        //double time11 = System.nanoTime();//.currentTimeMillis();

        try{
            Properties prop = new Properties();
            InputStream propFile = loader.getResourceAsStream("configuration/appProperties.properties");
            if (propFile!=null)prop.load(propFile);
            AppId = prop.getProperty("appId", "StateListener");
            // String baseUri = prop.getProperty("baseUri", "http://130.192.225.154:8080/frogsssa-1.0-SNAPSHOT/webresources/ConnectionModule");
            //String eventsUri = prop.getProperty("eventsUri", "http://130.192.225.154:8080/frogsssa-1.0-SNAPSHOT/webresources/events");
            String baseUri = prop.getProperty("baseUri", "http://127.0.0.1:9090/frogsssa-1.0-SNAPSHOT/webresources/ConnectionModule");
            String eventsUri = prop.getProperty("eventsUri", "http://127.0.0.1:9090/frogsssa-1.0-SNAPSHOT/webresources/events");
            log.info("appId "+AppId);
            log.info("baseUri "+baseUri);
            log.info("eventsUri "+eventsUri);

            cM = new ConnectionModuleClient(this, AppId, baseUri, eventsUri);

            log.info("cm client created!!");



            InputStream yangFile = loader.getResourceAsStream(YANGFILE);
            String yangString = new String();
            try(Scanner s = new Scanner(yangFile)){
                while(s.hasNextLine())
                    yangString+=s.nextLine();
            }
            cM.SetDataModel(yangString);
            log.info("set DM: " + yangString);


            /*new yang(new FileInputStream(yangFile));

            YANG_Specification spec = yang.Start();
            //System.out.println(spec);
            spec.check();

            File yin = new File("src/main/resources/files/yinFile.txt");
            if(!yin.exists())
                yin.createNewFile();
            new Yang2Yin(spec, new String[0], new PrintStream(yin));
            Vector<YANG_Body> bodies= spec.getBodies();
            YangTreeNode yangTree = spec.getSchemaTree();
            //findYangLeafs(yangTree);
            for(int i=0; i< bodies.size();i++){
                //System.out.println("body "+i);
                //System.out.println(bodies.get(i));
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(yin);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            //System.out.println("Root yin "+doc.getDocumentElement().getNodeName());

            NodeList nodes = doc.getElementsByTagName("leaf");
            for(int i=0; i<nodes.getLength();i++){
                Node n = nodes.item(i);
                //System.out.println("node "+i+" "+n);
                    for(int j=0;j<n.getAttributes().getLength();j++)
                        //System.out.println("--Attribute "+n.getAttributes().item(j).getNodeName()+" "+n.getAttributes().item(j).getNodeValue());
                if(n.getNodeType()==Node.ELEMENT_NODE){
                    Element e=(Element)n;
                    NodeList childs = e.getChildNodes();
                    for(int k=0;k<childs.getLength();k++){
                        if(childs.item(k).getNodeType()==Node.ELEMENT_NODE)
                            //System.out.println("++figlio : "+childs.item(k).getNodeName()+" "+childs.item(k).getAttributes().item(0).getNodeValue());
                    }
            }

            }

            findYinLeafs(doc.getDocumentElement(), "");*/

            InputStream yinFile = loader.getResourceAsStream(YINFILE);
            JsonNode rootYin = mapper.readTree(yinFile);


            // log.info("read yinFile " +rootYin);
            // System.out.println(rootYin);

            findYinLeafs(rootYin, rootYin.get("@name").textValue());

        } catch (Exception ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }

        //log.info("++CONFIG "+config);

//        log.info("--toListenPush "+toListenPush);

//        log.info("--toListenThreshold "+toListenThreshold);

//        log.info("--toListenTimer "+toListenTimer);

        //PARSE MAPPING FILE
        InputStream mapFile = loader.getResourceAsStream(MAPPINGFILE);
        try(Scanner s = new Scanner(mapFile)){
            while(s.hasNextLine()){
                String line = s.nextLine();
                String[] couples = line.split(Pattern.quote(";"));
                for(int i=0; i<couples.length;i++){
                    String[] yj = couples[i].split(Pattern.quote(":"));
                    if(yj.length==2)
                        YangToJava.put(yj[1].trim(), yj[0].trim());
                }
            }
        }
        //ADD VARIABLES TO LISTEN
        Collection<String> all = YangToJava.keySet();
        List<String> sorted = new ArrayList<String>(all);
        Collections.sort(sorted);
        List<String> leafs = new ArrayList<>();
        for(int i=0; i<sorted.size()-1; i++){
            String id0 = sorted.get(i);
            String id1 = sorted.get(i+1);;
            if(!id1.contains(id0))
                leafs.add(id0);
        }
        leafs.add(sorted.get(sorted.size()-1));
        for(String l:YangToJava.keySet()){
            if(l.endsWith("]")){
                String index = l.substring(l.lastIndexOf("[")+1, l.lastIndexOf("]"));
                String idList = l.substring(0, l.length()-index.length()-2);
                lists.put(idList.substring(5)+"[]", index);
            }
        }
        rootJson = mapper.createObjectNode();

        for(String l:leafs)
            createTree(rootJson, YangToJava.get(l));

        //check push-never-threshold-periodic
//            for(String s:leafs){
//                String s1 = s.substring(5);
//                toListenPush.add(s1);
//                //this.addNewListener(s1);
//            }
        //this.setName("statelistener");

        //double time12 = System.nanoTime();//.currentTimeMillis();System.currentTimeMillis();
        //log.info("++timeMapping "+(time12-time11));

        this.start();

    }


    public void run(){

        /*while(!stopCondition){
            try {
                if(root==null)
                    stopSL();
                else{
                   // log.info("Parte il ciclo");
                    long id = this.getId();
                    //log.info("idProcess "+id);

                    String threadName = ManagementFactory.getRuntimeMXBean().getName();
                    String name = this.getName();
                   // log.info("ProcessName "+threadName);

                    ThreadMXBean txb = ManagementFactory.getThreadMXBean();
                    long cpu = txb.getCurrentThreadCpuTime();
                   // log.info("cpu "+cpu);

                    long pid = Long.valueOf(threadName.split("@")[0]);
                   // log.info("pid "+pid);
                    //checkValue();
                    saveNewValues();
                    sleep(500000);
                }
            } catch (InterruptedException ex) {
                stopSL();
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JsonProcessingException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       // log.info("The program has been stopped");
        cM.deleteResources();
        */

    }

    /*private void stopTimerTasks(){
     //  log.info("Stopping periodicTasks....");
        for(PeriodicVariableTask t:toListenTimer)
            t.cancel();
       // log.info("...Stopped periodic tasks");
    }*/

    public void saveNewValues() throws JsonProcessingException{

        stateNew = new HashMap<>();
        for(String s:toListenPush){
            try {
                String sj = fromYangToJava(s);
                saveValues(root, sj.substring(5), sj.substring(5), stateNew);
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //  checkChangesSaved();
        Map<String, Object> thr = new HashMap<>();
        for(String s:toListenThreshold.keySet()){
            try {
                if(YangToJava.containsValue(s)){
                    String sj = null;
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(s)){
                            sj = k;
                            break;
                        }
                    saveValues(root, sj.substring(5), sj.substring(5), thr);
                }
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // checkThreshold(thr);
    }

    public void saveValues(Object actual, String subToListen, String complete, Map<String, Object> toSave) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{

        if(subToListen.contains("/")){
            String inter = subToListen.substring(0, subToListen.indexOf("/"));
            if(inter.contains("[")){
                String lName = inter.substring(0, inter.indexOf("["));
                String index = inter.substring(inter.indexOf("[")+1, inter.length()-1);
                actual = actual.getClass().getField(lName).get(actual);
                if(actual!=null){
                    if(List.class.isAssignableFrom(actual.getClass())){
                        for(Object item:(List)actual){
                            String indexValue = searchLeafInList(item, index);
                            String complToPass = complete.substring(0, complete.length()-subToListen.length())+lName+"["+indexValue+"]"+subToListen.substring(inter.length());
                            saveValues(item, subToListen.substring(inter.length()+1), complToPass, toSave);
                        }
                    }else if(Map.class.isAssignableFrom(actual.getClass())){
                        for(Object key:((Map)actual).keySet()){
                            String indexValue = key.toString();
                            String complToPass = complete.substring(0, complete.length()-subToListen.length())+lName+"["+indexValue+"]"+subToListen.substring(inter.length());
                            if(subToListen.substring(inter.length()+1).equals("{key}")){
                                //save the key
                                toSave.put(complToPass, key);
                            }
                            else
                                saveValues(((Map)actual).get(key), subToListen.substring(inter.length()+1), complToPass, toSave);
                        }
                    }else
                        return;
                }
            }else{
                if(!inter.equals("{value}"))
                    actual = actual.getClass().getField(inter).get(actual);
                if(actual!=null)
                    saveValues(actual, subToListen.substring(inter.length()+1), complete, toSave);
            }
        }else{
            //leaf
            if(subToListen.contains("[")){
                //è una mappa
                String mapName = subToListen.substring(0, subToListen.indexOf("["));
//                String ind = subToListen.substring(subToListen.indexOf("[")+1, subToListen.indexOf("]"));
                Map mappa = (Map) actual.getClass().getField(mapName).get(actual);
                if(mappa!=null){
                    for(Object k:mappa.keySet()){
                        String complToPass = complete.substring(0, complete.lastIndexOf("[")+1)+k.toString()+"]";
                        toSave.put(complToPass, mappa.get(k));
                    }
                }
            }else{
                if(!subToListen.equals("{value}"))
                    actual = actual.getClass().getField(subToListen).get(actual);
                toSave.put(complete, actual);
            }
        }
    }

   /* private void checkChangesSaved() throws JsonProcessingException{
        List<NotifyMsg> happenings = new ArrayList<>();
        HashMap<String, Object> copyState = new HashMap<>();
        HashMap<String, Object> copyNewState = new HashMap<>();
        List<String> ancoraPresenti = new ArrayList<>();
        if(state!=null && stateNew!=null){
            copyState.putAll(state);
            copyNewState.putAll(stateNew);
            for(String k:state.keySet()){
                if(stateNew.containsKey(k)){
                    if(state.get(k)==null){
                        if(stateNew.get(k)!=null){
                            //ADDED
                           NotifyMsg e = new NotifyMsg();
                           e.act=action.ADDED;
                           e.var=trasformInPrint(k);
                           e.obj=stateNew.get(k).toString();
                           e.currentTime = System.nanoTime();
                           happenings.add(e);
                           //log.info((new Gson()).toJson(e));
                        }else{
                            stateNew.remove(k);
                            copyNewState.remove(k);
                            continue;
                        }
                    }
                    if(stateNew.get(k)==null){
                        stateNew.remove(k);
                        copyNewState.remove(k);
                        continue;
                    }
                    //non sono stati eliminati
                    if(!state.get(k).equals(stateNew.get(k))){
                       //CHANGED VALUE
                       NotifyMsg e = new NotifyMsg();
                       e.act=action.UPDATED;
                       e.var=trasformInPrint(k);
                       e.obj=stateNew.get(k).toString();
                       e.currentTime = System.nanoTime();
                       happenings.add(e);
                       //log.info((new Gson()).toJson(e));
                    }
                    copyState.remove(k);
                    copyNewState.remove(k);
                    ancoraPresenti.add(k);
                }
            }
            //update the actual state
            state = stateNew;
            //copyState contains the eliminated
            ObjectNode rootJ = mapper.createObjectNode();
            for(String k:copyState.keySet()){
                NotifyMsg e = new NotifyMsg();
               // if(copyNewState.get(k) != null){
                e.act=action.DELETED;
                e.obj=copyState.get(k).toString();
                e.var=trasformInPrint(k);
                e.currentTime = System.nanoTime();
                happenings.add(e);
                insertInNode(rootJ, k, generalIndexes(k), e.obj);
                //log.info((new Gson()).toJson(e));
               // }
            }
            //copyNewState contains the added
            rootJ = mapper.createObjectNode();
            for(String k:copyNewState.keySet()){
                NotifyMsg e = new NotifyMsg();
                e.act=action.ADDED;
                e.obj=copyNewState.get(k).toString();
                e.var=trasformInPrint(k);
                e.currentTime = System.nanoTime();
                happenings.add(e);
                insertInNode(rootJ, k, generalIndexes(k), e.obj);
                //log.info((new Gson()).toJson(e));
            }

            rootJ = mapper.createObjectNode();
            for(String s:ancoraPresenti)
                insertInNode(rootJ, s, generalIndexes(s), "presente");
        }

        for(NotifyMsg e:happenings){
//            log.info(e.act+" "+e.var+" "+e.obj);
           // cM.somethingChanged((new Gson()).toJson(e));
               cM.somethingChanged(mapper.writeValueAsString(e));
        }

    }*/

    private void insertInNode(ObjectNode node, String s, String complete, Object v){
        if(s.contains("/")){
            String f = s.substring(0, s.indexOf("/"));
            String field = (f.contains("["))?f.substring(0, f.indexOf("[")):f;
            String index = (f.contains("["))?f.substring(f.indexOf("[")+1, f.indexOf("]")):null;
            if(node.findValue(field)!=null){
                //JsonNode next = node.get(field);
                JsonNode next = node.findValue(field);
                if(next.isArray()){
                    Iterator<JsonNode> nodes = ((ArrayNode)next).elements();
                    String list = getListName(complete, s);
                    if(lists.containsKey(list)){
                        String ind = lists.get(list);
                        boolean found = false;
                        while(nodes.hasNext()){
                            ObjectNode objN = (ObjectNode)nodes.next();
                            if(objN.findValue(ind)!=null && objN.get(ind).asText().equals(index)){
                                insertInNode(objN, s.substring(s.indexOf("/")+1), complete, v);
                                found = true;
                                break;
                            }
                        }
                        if(found==false){
                            ObjectNode obj = mapper.createObjectNode();
                            obj.put(ind, index);
                            insertInNode(obj, s.substring(s.indexOf("/")+1), complete, v);
                            ((ArrayNode)next).add(obj);
                        }
                    }
                }else{
                    insertInNode((ObjectNode)next, s.substring(s.indexOf("/")+1), complete, v);
                }
            }else{
                if(index==null){
                    ObjectNode next = mapper.createObjectNode();
                    insertInNode(next, s.substring(s.indexOf("/")+1), complete, v);
                    node.put(field, next);
                }else{
                    ArrayNode array = mapper.createArrayNode();
                    String list = getListName(complete, s);
                    if(lists.containsKey(list)){
                        String ind = lists.get(list);
                        ObjectNode next = mapper.createObjectNode();
                        next.put(ind, index);
                        insertInNode(next, s.substring(s.indexOf("/")+1), complete, v);
                        array.add(next);
                    }
                    node.put(field, array);
                }
            }
        }else{
            if((node.findValue(s))==null && v!=null)
                node.put(s, v.toString());
        }
    }

    private String getListName(String complete, String last){
        String[] c = complete.split(Pattern.quote("/"));
        String[] l = last.split(Pattern.quote("/"));
        String res =new String();
        for(int i=0;i<c.length-l.length+1;i++)
            res+=c[i]+"/";
        res = res.substring(0,res.lastIndexOf("[")+1)+"]";
        return res;
    }

    private String trasformInPrint(String var) {
        String[] partsWithoutIndex = var.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String j=partsWithoutIndex[0];
        String onlyLastOne = partsWithoutIndex[0];
        String y=null;
        if(partsWithoutIndex.length>1)
            for(int i=1;i<partsWithoutIndex.length;i++){
                if(i%2==0){
                    //nome lista
                    j+=partsWithoutIndex[i];
                    onlyLastOne+=partsWithoutIndex[i];
                }else{
                    if(lists.containsKey(j+"[]"))
                        j+="["+lists.get(j+"[]")+"]";
                    if(i==partsWithoutIndex.length-1){
                        if(lists.containsKey(j+"[]"))
                            onlyLastOne+=("["+lists.get(j+"[]")+"]");
                    }
                    else
                        onlyLastOne+=("["+partsWithoutIndex[i]+"]");

                }
            }
        String toVerify = "root/"+j;
        for(String s:YangToJava.keySet())
            if(s.equals(toVerify))
                y=YangToJava.get("root/"+j);
        if(y!=null){
            String[] yparse = y.split(Pattern.quote("[]"));
            String toPub=new String();
            for(int i=0; i<partsWithoutIndex.length;i++){
                if(i%2==0)
                    toPub+= yparse[i/2];
                else
                    toPub+="["+partsWithoutIndex[i]+"]";
            }
            return toPub;
        }
        return y;
    }

    //private JsonNode getCorrectItem(String newVal, String complete){
    private JsonNode getCorrectItem(JsonNode node, String complete){

        //double timestamp1 = System.nanoTime();

        //complete in Yang
        //newVal in Yang
        // try{
        //JsonNode node = mapper.readTree(newVal);
        JsonNode newNode;
        if(node.isObject()){
            newNode = mapper.createObjectNode();
            Iterator<String> fields = node.fieldNames();
            while(fields.hasNext()){
                String fieldJava = null;
                String fieldName = (String)fields.next();
                if(YangToJava.containsValue(complete+"/"+fieldName)) {
                    double timestamp3 = System.nanoTime();
                    for (String k : YangToJava.keySet())
                        if (YangToJava.get(k).equals(complete + "/" + fieldName)) fieldJava = k;
                    double timestamp2 = System.nanoTime();
                    log.info("map '"+complete + "/" + fieldName+"': "+((timestamp2-timestamp3)/1000000));
                }
                if(fieldJava!=null){
                    fieldJava=fieldJava.substring(fieldJava.lastIndexOf("/")+1);
                    if(node.get(fieldName).isValueNode())
                        ((ObjectNode)newNode).put(fieldJava, node.get(fieldName));
                    else{
                        String newCampo = (node.get(fieldName).isObject())?complete+"/"+fieldName:complete+"/"+fieldName+"[]";
                        // JsonNode subItem = getCorrectItem(mapper.writeValueAsString(node.get(fieldName)),complete+"/"+fieldName);
                        JsonNode subItem = getCorrectItem(node.get(fieldName),complete+"/"+fieldName);
                        ((ObjectNode)newNode).put(fieldJava, subItem);
                    }
                }
            }
        }else{
            newNode = mapper.createArrayNode();
            Iterator<JsonNode> iter = ((ArrayNode)node).elements();
            while(iter.hasNext()){
                JsonNode item = iter.next();
                //JsonNode subItem = getCorrectItem(mapper.writeValueAsString(item),complete+"[]");
                JsonNode subItem = getCorrectItem(item,complete+"[]");
                ((ArrayNode)newNode).add(subItem);
            }
        }

        //double timestamp2 = System.nanoTime();
        //log.info("timestamp getCorrectItem: "+(timestamp2-timestamp1));
        return newNode;

        // }catch(IOException ex){
        //    Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        //}
        // return null;

    }

    private void createTree(JsonNode node, String l) {

        //double timestamp1 = System.nanoTime();

        if(l==null || l.equals(""))
            return;
        String[] splitted = l.split(Pattern.quote("/"));
        if(node.isObject()){
            JsonNode next;
            if(splitted[0].contains("[")){
                String inter = splitted[0].substring(0, splitted[0].indexOf("["));
                next = ((ObjectNode)node).get(inter);
                if(next==null)
                    ((ObjectNode)node).put(inter, mapper.createArrayNode());
                next = ((ObjectNode)node).get(inter);
            }else{
                next = ((ObjectNode)node).get(splitted[0]);
                if(next==null){
                    if(splitted.length>1 || (splitted.length>1&&splitted[1].contains("[")))
                        ((ObjectNode)node).put(splitted[0], mapper.createObjectNode());
                    else if (splitted.length==1 && splitted[0].contains("[")){
                        ArrayNode mappa = mapper.createArrayNode();
                        ObjectNode elemMappa = mapper.createObjectNode();
                        elemMappa.put("key", "");
                        elemMappa.put("value", "");
                        ((ObjectNode)node).put(splitted[0], mappa);
                    }else
                        ((ObjectNode)node).put(splitted[0], new String());
                }
                next = ((ObjectNode)node).get(splitted[0]);
            }
            if(splitted.length>1)
                createTree(next, l.substring(splitted[0].length()+1));
            if(splitted.length==1&&next.isArray()){
//                ObjectNode elemMappa = mapper.createObjectNode();
//                elemMappa.put("key", "");
//                elemMappa.put("value", "");
//                ((ArrayNode)next).add(elemMappa);
            }
        }else{
            JsonNode next;
            if(splitted[0].contains("[")){
                String inter = splitted[0].substring(0, splitted[0].indexOf("["));
                if(node.isArray()){
                    //è una lista
                    if(((ArrayNode)node).elements().hasNext()==false)
                        ((ArrayNode)node).addObject();
                    next = ((ArrayNode)node).get(0);
                    if(((ObjectNode)next).get(inter)==null)
                        ((ObjectNode)next).put(inter, mapper.createArrayNode());
                    next = ((ObjectNode)next).get(inter);
                }else{
                    //è l'elemento di una mappa
                    ArrayNode newNode = mapper.createArrayNode();
                    ObjectNode nn = mapper.createObjectNode();
                    nn.put("id", "");
                    nn.put("value", nn);
                    newNode.add(nn);
                    return;
                }
            }else{
                //if(((ArrayNode)node).elements().hasNext()==false)
                if(node.isArray()){
                    if(((ArrayNode)node).elements().hasNext()==false)
                        ((ArrayNode)node).addObject();
                    next = ((ArrayNode)node).get(0);
                    if(((ObjectNode)next).get(splitted[0])==null){
                        if(splitted.length>2)
                            ((ObjectNode)next).put(splitted[0], mapper.createObjectNode());
                        else
                            ((ObjectNode)next).put(splitted[0], new String());
                    }
                    next = ((ObjectNode)next).get(splitted[0]);
                }
                else{
                    ArrayNode newNode = mapper.createArrayNode();
                    ObjectNode nn = mapper.createObjectNode();
                    nn.put("id", "");
                    nn.put("value", nn);
                    newNode.add(nn);
                    return;
                }

            }
            if(splitted.length>1)
                createTree(next, l.substring(splitted[0].length()+1));
        }

        //double timestamp2 = System.nanoTime();
        //log.info("timestamp createTree: "+(timestamp2-timestamp1));
    }

    public Object getComplexObj(String var) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

        //double timestamp1 = System.nanoTime();

        String[] spl = var.split(Pattern.quote("/"));
        JsonNode ref = rootJson;

        //double timestamp3 = System.nanoTime();
        //log.("timeSplit "+((timestamp3-timestamp1)/1000000));

        //double timestamp4 = System.nanoTime();

        for(int i=0;i<spl.length;i++){
            String field =(spl[i].contains("["))?spl[i].substring(0, spl[i].indexOf("[")):spl[i];
            String index =(spl[i].contains("["))?spl[i].substring(spl[i].indexOf("[")+1, spl[i].indexOf("]")):null;

            if(ref.isObject()){
                if(((ObjectNode)ref).get(field)!=null){
                    ref = ((ObjectNode)ref).get(field);
                    if(index!=null && !index.equals("")){
                        ref=((ArrayNode)ref).get(0);
                    }if(index!=null && index.equals("")&& i!=spl.length-1)
                        return null;
                    continue;
                }else{
                    //log.info("var not found "+field);
                    return null;
                }
            }else{
                if(((ArrayNode)ref).elements().hasNext()==false){
                    return null;
                }
                ref = ((ArrayNode)ref).get(0);
                if(((ObjectNode)ref).get(field)==null){
                    return null;
                }
                continue;
            }
        }

        //double timestamp5 = System.nanoTime();
        //log.("timeFor "+((timestamp5-timestamp4)/1000000));

        //double timestamp6 = System.nanoTime();

        if(ref.isValueNode()){
            //is a leaf, but it is not present in state
            String varJava = fromYangToJava(var);
            //  log.info("Var java "+varJava);
            Object value = getLeafValue(varJava.substring(5));
            String varYangNoIndexes = noIndexes(var);
            Object serialized = personalizedSerialization(varYangNoIndexes, value);
//            ObjectNode result = mapper.createObjectNode();
//            result.put(var.substring(var.lastIndexOf("/")+1), value.toString());
            return serialized;
        }
        JsonNode res;// = (ref.isObject())?mapper.createObjectNode():mapper.createArrayNode();
        var=(ref.isArray()&&var.endsWith("[]"))?var.substring(0, var.length()-2):var;

        //double timestamp7 = System.nanoTime();
        //log.("timeIf "+((timestamp7-timestamp6)/1000000));

        //double timestamp8 = System.nanoTime();

        res = fillResult(ref, var);

        //double timestamp9 = System.nanoTime();
        //log.("timeFillResult "+((timestamp9-timestamp8)/1000000));


        //double timestamp10 = System.nanoTime();
        if(var.endsWith("]") && res.size()==0)
            res = null;
//        log.info("The result is "+res);
        JsonNode r = mapper.createObjectNode();
        ((ObjectNode)r).put(var.substring(var.lastIndexOf("/")+1), res);
//        log.info("The result is ready");
        //double timestamp11 = System.nanoTime();
        //log.("timePutRes "+((timestamp11-timestamp10)/1000000));

        //log.info("timestamp getComplexObj: "+(timestamp2-timestamp1));
        return res;
    }

    private JsonNode fillResult(JsonNode ref, String var) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {

        JsonNode toRet;

        //log.("fillResult - json: "+ref.toString());
        //log.("fillResult - var: "+var);

        if(ref.isObject()){

            //fill fields
            toRet = mapper.createObjectNode();
            Iterator<String> field = ((ObjectNode)ref).fieldNames();


            if(!field.hasNext()){

                //log.("if field has NO next: ");

                //searchCorrispondentField
                String varWithoutIndexes = new String();
                String[] varSp = var.split("["+Pattern.quote("[]")+"]");
                for(int i=0; i<varSp.length;i++)
                    if(i%2==0)
                        varWithoutIndexes+=varSp[i]+"[]";
                varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);

                //double timestamp1 = System.nanoTime();

                if(YangToJava.containsValue(varWithoutIndexes)){
                    String key = null;

                    double timestamp3 = System.nanoTime();

                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(varWithoutIndexes))
                            key = k;

                    double timestamp2 = System.nanoTime();
                    log.info("map '"+varWithoutIndexes+"': "+((timestamp2-timestamp3)/1000000));


                    String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String jWithIndex = new String();
                    for(int i=0;i<yspez.length;i++){
                        if(i%2==0)
                            jWithIndex+=jspez[i];
                        else
                            jWithIndex+="["+yspez[i]+"]";
                    }
                    ((ObjectNode)toRet).put(var, getLeafValue(jWithIndex.substring(5)).toString());
                    field.next();

                }
                return toRet;
            }

            //double timestamp8 = System.nanoTime();

            while(field.hasNext()){
                //double timestampIter1 = System.nanoTime();
                String fieldName = field.next();
                if(ref.get(fieldName).isValueNode()){

                    ////double timestamp6 = System.nanoTime();

                    String varWithoutIndexes = new String();
                    String[] varSp = (var+"/"+fieldName).split("["+Pattern.quote("[]")+"]");
                    for(int i=0; i<varSp.length;i++)
                        if(i%2==0)
                            varWithoutIndexes+=varSp[i]+"[]";
                    varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);

                    ////double timestamp7 = System.nanoTime();
                    ////log.("time nextIf "+((timestamp7-timestamp6)/1000000));

                    if(YangToJava.containsValue(varWithoutIndexes)){
                        String key = null;

                        double timestamp1 = System.nanoTime();

                        for(String k:YangToJava.keySet())
                            if(YangToJava.get(k).equals(varWithoutIndexes))
                                key = k;

                        double timestamp2 = System.nanoTime();
                        log.info("map '"+varWithoutIndexes+"': "+((timestamp2-timestamp1)/1000000));



                        //double timestamp10 = System.nanoTime();
                        String[] yspez = (var+"/"+fieldName).split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                        String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                        //double timestampyjpez = System.nanoTime();
                        //log.info("time yjpezz "+((timestampyjpez-timestamp10)/1000000));

                        //double timestampforspez1 = System.nanoTime();
                        String jWithIndex = new String();
                        for(int i=0;i<yspez.length;i++){
                            if(i%2==0)
                                jWithIndex+=jspez[i];
                            else
                                jWithIndex+="["+yspez[i]+"]";
                        }
                        //double timestampforspez2 = System.nanoTime();
                        //log.info("time forspez2 "+((timestampforspez2-timestampforspez1)/1000000));

                        //double timegetLeafValue1 = System.nanoTime();
                        Object value = getLeafValue(jWithIndex.substring(5));
                        //double timestamp12 = System.nanoTime();
                        //log.info("time getLeafValue1 "+((timestamp12-timegetLeafValue1)/1000000));


                        if(value!=null){

                            //double timestamp11 = System.nanoTime();

                            //log.info("Devo parsare l'oggetto "+value);
                            Object parsed = personalizedSerialization(varWithoutIndexes, value);
                            if(parsed != null){
                                //log.info("ora parsed è di class "+parsed.getClass());
                                if(Boolean.class.isAssignableFrom(parsed.getClass())){
                                    //log.info("Trattato come boolean");
                                    ((ObjectNode)toRet).put(fieldName, (Boolean)parsed);}
                                else if(parsed.getClass() == Long.class){
                                    //log.info("Trattato come long");
                                    ((ObjectNode)toRet).put(fieldName, (Long)parsed);
                                }
                                else if(Integer.class.isAssignableFrom(parsed.getClass())){
                                    //log.info("Trattato come int");
                                    ((ObjectNode)toRet).put(fieldName, (Integer)parsed);}
                                else if(Double.class.isAssignableFrom(parsed.getClass())){
                                    //log.info("trattato come double");
                                    ((ObjectNode)toRet).put(fieldName, (Double)parsed);}
                                else {//log.info("Trattato come string");
                                    ((ObjectNode)toRet).put(fieldName, parsed.toString());
                                }
                            }

                            //double timestamp14 = System.nanoTime();
                            //log.info("time deser "+((timestamp14-timestamp11)/1000000));
                        }
                    }
                }else{
                    JsonNode f = fillResult(((ObjectNode)ref).get(fieldName), var+"/"+fieldName);
//                    if(f.size()!=0)
                    ((ObjectNode)toRet).put(fieldName, f);
                }
                //double timestampiter2 = System.nanoTime();
                //log.("time iterazione "+((timestampiter2-timestampIter1)/1000000));
            }

            //double timestamp9 = System.nanoTime();
            //log.("time next "+((timestamp9-timestamp8)/1000000));

            return toRet;
        }else{

            //add elements
            String listWithoutIndex = noIndexes(var);
//                    new String();
//                String[] varSp = var.split("["+Pattern.quote("[]")+"]");
//                for(int i=0; i<varSp.length;i++)
//                    if(i%2==0)
//                        listWithoutIndex+=varSp[i]+"[]";
//                listWithoutIndex = listWithoutIndex.substring(0, listWithoutIndex.length()-2);
            toRet = mapper.createArrayNode();
            String listInJava = null;
            for(String l:YangToJava.keySet()){
                if(YangToJava.get(l).contains(listWithoutIndex+"[") && YangToJava.get(l).substring(0, listWithoutIndex.length()+1).equals(listWithoutIndex+"[")){
                    String rem = YangToJava.get(l).substring(listWithoutIndex.length());
                    if(!rem.contains("/"))
                        listInJava = l;
                }
            }
            String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
            String[] jspez = listInJava.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
            String jWithIndex = new String();
            for(int i=0;i<yspez.length;i++){
                if(i%2==0)
                    jWithIndex+=jspez[i];
                else
                    jWithIndex+="["+yspez[i]+"]";
            }
            //ListValues e = stateList.get(jWithIndex.substring(5)+"[]");
            String lN = generalIndexes(jWithIndex.substring(5))+"[]";
            String e = (lists.containsKey(lN))?lists.get(lN):null;
            if(e!=null){
                String indice=e;
                Object list = getLists(root, jWithIndex.substring(5)+"[]", jWithIndex.substring(5)+"[]");
                if(list!=null && List.class.isAssignableFrom(list.getClass())){
                    List<Object> elems = new ArrayList<>();
                    elems.addAll((List)list);
                    for(Object obj:elems){
                        String idItem = searchLeafInList(obj, indice);
                        JsonNode child = fillResult(((ArrayNode)ref).get(0), var+"["+idItem+"]");
                        if(child.size()!=0)
                            ((ArrayNode)toRet).add(child);
                    }
                }else if(list!=null && Map.class.isAssignableFrom(list.getClass())){
                    Map<Object, Object> elems = new HashMap<>();
                    elems.putAll((Map)list);
                    for(Object k:elems.keySet()){
                        JsonNode child = fillResult(((ArrayNode)ref).get(0), var+"["+k+"]");
                        if(child.size()!=0)
                            ((ArrayNode)toRet).add(child);
                    }
                }
//                if(e.List!=null)elems.addAll(e.List);
//                for(Object obj:elems){
//                    String idItem = searchLeafInList(obj, indice);
//                    ((ArrayNode)toRet).add(fillResult(((ArrayNode)ref).get(0), var+"["+idItem+"]"));
//                }
//
            }

            return toRet;
        }
    }

    private int setComplexObject(String var, String newVal) {

        int returnVal = 0;
        //log.("DEBUG: setComplexObj");

        try {

            double set1 = System.nanoTime();//.currentTimeMillis();

            JsonNode toSet = mapper.readTree(newVal);

            double set2 = System.nanoTime();//.currentTimeMillis();
            // log.info("++readTree "+((set2-set1)/1000000));
//            log.info("toSet is "+toSet);
            //check if all the values are configurable
            if(!configVariables(noIndexes(var), toSet)){
                // log.info("not to config..");
                return 1;
            }
//            if(!configVariables(var))
//                return;

            double set3 = System.nanoTime();//.currentTimeMillis();

            returnVal = fillVariables(toSet, var);

            double set4 = System.nanoTime();//.currentTimeMillis();
            // log.info("++fillVariables "+((set4-set3)/1000000));
            // log.info("timestamp setComplexObj: "+(timestamp2-timestamp1));
            return returnVal;

        } catch (IOException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }catch(NoSuchFieldException ex){
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }catch(IllegalAccessException ex){
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
    }

    private boolean configVariables(String var, JsonNode toSet){
        if(toSet.isValueNode()){
//            log.info("Is a value Node: "+var);
            return config.get(var);
        }
        if(toSet.isObject()){
            Iterator<Entry<String, JsonNode>> iter = ((ObjectNode)toSet).fields();
            boolean ok = true;
            while(iter.hasNext()){
                Entry<String, JsonNode> field = iter.next();
                if(field.getValue().isValueNode()){
                    //leaf - check config
                    if(config.containsKey(var+"/"+field.getKey()))
                        ok = ok && config.get(var+"/"+field.getKey());
                    else{
//                        log.info("Config non contiene "+var+"/"+field.getKey());
                        ok = true;
                    }
                }else
                    ok = ok && configVariables(var+"/"+field.getKey(), field.getValue());
            }
            return ok;

        }else{
            Iterator<JsonNode> children = ((ArrayNode)toSet).elements();
            boolean ok = true;
            while(children.hasNext()){
                var = (var.endsWith("]"))?var : var+"[]";
                ok = ok && configVariables(var, children.next());
            }
            return ok;
        }
    }

    private boolean configVariables(String var){
        var = deleteIndexes(var);
        String[] fields = var.split(Pattern.quote("/"));
        JsonNode n = rootJson;
        for(int i=0;i<fields.length;i++){
            if(fields[i].contains("[]"))
                fields[i] = fields[i].substring(0, fields[i].length()-2);
            if(n.isArray()){
                n = n.get(0);
                n = ((ObjectNode)n).get(fields[i]);
            }else{
                n = ((ObjectNode)n).get(fields[i]);
            }
        }
//        var = var.replace("/", "/");
        boolean c = checkConfig(n, var);
        return c;
    }

    private boolean checkConfig(JsonNode n, String v){
        if(n.isValueNode()){
            if(config.containsKey(v))
                return config.get(v);
            return false;
        }
        if(n.isArray()){
            n = n.get(0);
            v = (v.endsWith("]"))?v:v+"[]";
            return checkConfig(n, v);
        }else{
            Iterator<String> it = ((ObjectNode)n).fieldNames();
            boolean cc = true;
            while(it.hasNext()){
                String fName = (String)it.next();
                cc = cc && checkConfig(n.get(fName), v+"/"+fName);
            }
            return cc;
        }
    }

    private int fillVariables(JsonNode toSet, String var) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {

        //log.debug("DEBUG: fillVariables, var: " + var);
//        log.info("In fillVariables");
        // log.info("var "+var+" to Set "+toSet);
        if(toSet.isValueNode()){
            //   log.info("In fillVariables - reached leaf");
            //set the corrispondent leaf

            double set5 = System.nanoTime();//.currentTimeMillis();

            String j = fromYangToJava(var);

            double set6 = System.nanoTime();//.currentTimeMillis();
            //   log.info("++readFromYang "+(set6-set5));   //equal to 0


            //if(state.containsKey(j.substring(5))){
//            log.info("And variable is "+j);
            if(j!=null){
                if(setVariable(j.substring(5), j.substring(5), toSet.asText(), root))
                {//log.info("++err5 ");
                    return 0;}
                else
                {  // log.info("++err1 ");
                    return 1;}
            }else
                return 2;
            //}
        }else{
            if(toSet.isObject()){
                //    log.info("Sono nel isObject");
                if(var.endsWith("]")){
                    String varWithoutIndexes = new String();
                    String[] varSp = var.split("["+Pattern.quote("[]")+"]");
                    for(int i=0; i<varSp.length;i++)
                        if(i%2==0)
                            varWithoutIndexes+=varSp[i]+"[]";
                    varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);
                    if(YangToJava.containsValue(varWithoutIndexes)){
                        String key = null;
                        double timestamp1 = System.nanoTime();
                        for(String k:YangToJava.keySet())
                            if(YangToJava.get(k).equals(varWithoutIndexes))
                                key = k;
                        double timestamp2 = System.nanoTime();
                        log.info("map '"+varWithoutIndexes+"': "+((timestamp2-timestamp1)/1000000));
                        String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                        String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                        String jWithIndex = new String();
                        /*
                        for(int i=0;i<yspez.length;i++){
                            if(i%2==0)
                                jWithIndex+=jspez[i];
                            else
                                jWithIndex+="["+yspez[i]+"]";
                        }*/
                        for(int i=0;i<yspez.length;i++){
                            try {
                                Integer.parseInt(yspez[i]);
                                jWithIndex+="["+yspez[i]+"]";
                            } catch (NumberFormatException ex) {
                                jWithIndex+=jspez[i];
                                if (i == yspez.length - 1)
                                    jWithIndex+="[]";
                            }
                        }
                        jWithIndex = jWithIndex.substring(5);

                        //double set7 = System.nanoTime();//.currentTimeMillis();

                        //JsonNode newValJava = getCorrectItem(mapper.writeValueAsString(toSet), varWithoutIndexes+"[]");
                        JsonNode newValJava = getCorrectItem(toSet, varWithoutIndexes+"[]");
                        if(newValJava!=null){
                            if(setVariable(jWithIndex, jWithIndex,mapper.writeValueAsString(newValJava), root))
                                return 0;
                            else
                            {//log.info("++err2 ");
                                return 1;}
                        }
                        //double set8 = System.nanoTime();//.currentTimeMillis();
                        // log.info("++writeValue1 "+((set8-set7)/1000000));
                        //   log.info("++err3 ");
                        return 1;
                    }else
                        return 2;
                }else{
                    Iterator<String> fields = toSet.fieldNames();
                    int res = 0;
                    while(fields.hasNext()){
                        String fieldName = (String)fields.next();
                        //  log.info("Setting "+fieldName);
                        int resc = fillVariables(toSet.get(fieldName), var+"/"+fieldName);
                        //   log.info("resc "+resc);
                        res = (resc==0)?res:resc;
                    }
                    //log.info("++err4 ");
                    return res;
                }
            }else{
                //log.info("Sono nell'else - no object");
                //capire qual è la lista corrispondente
                //without indexes
                String varWithoutIndexes = new String();
                String[] varSp = var.split("["+Pattern.quote("[]")+"]");
                for(int i=0; i<varSp.length;i++)
                    if(i%2==0)
                        varWithoutIndexes+=varSp[i]+"[]";
                varWithoutIndexes = varWithoutIndexes.substring(0, varWithoutIndexes.length()-2);
                //  log.info("Var without indexes "+varWithoutIndexes);
                if(YangToJava.containsValue(varWithoutIndexes)){
                    //    log.info("Yang to Java contains the value");
                    String key = null;
                    double timestamp3 = System.nanoTime();
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(varWithoutIndexes))
                            key = k;
                    double timestamp2 = System.nanoTime();
                    log.info("map '"+varWithoutIndexes+"': "+((timestamp2-timestamp3)/1000000));
                    //   log.info("And the key is "+key);
                    String[] yspez = var.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String[] jspez = key.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
                    String jWithIndex = new String();

                    for(int i=0;i<yspez.length;i++){
                        if(i%2==0)
                            jWithIndex+=jspez[i];
                        else
                            jWithIndex+="["+yspez[i]+"]";
                    }


                    /*
                    for(int i=0;i<yspez.length;i++){
                        try {
                            Integer.parseInt(yspez[i]);
                            jWithIndex+="["+yspez[i]+"]";
                        } catch (NumberFormatException ex) {
                            jWithIndex+=jspez[i];
                            if (i == yspez.length - 1)
                                jWithIndex+="[]";
                        }
                    }*/
                    if(jWithIndex.length()<=5){
                        //   log.info("Is root.!! Can't be a list");
                        return 2;
                    }
                    jWithIndex = jWithIndex.substring(5);
                    //crearne una nuova
                    Class<?> type=null;
                    String indice = null;
                    String jgen = generalIndexes(jWithIndex);
                    //  log.info("With generl indexes is "+jgen);
                    if(lists.containsKey(jgen+"[]")){
                        //    log.info("The list exists");
                        indice = lists.get(jgen+"[]");
                        Object actual = root;
                        String[] fields = jWithIndex.split(Pattern.quote("/"));
                        Field f = actual.getClass().getDeclaredField(fields[0]);
                        for(int i=1;i<fields.length;i++){
                            if(fields[i].contains("[")){
                                if(java.util.List.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i].substring(0, fields[i].indexOf("[")));
                                }else if(Map.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i].substring(0, fields[i].indexOf("[")));
                                }else
                                    f = f.getType().getDeclaredField(fields[i].substring(0, fields[i].indexOf("[")));
                            }else{
                                if(java.util.List.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i]);
                                }else if(Map.class.isAssignableFrom(f.getType())){
                                    ParameterizedType pt = (ParameterizedType)f.getGenericType();
                                    Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                                    f = itemType.getField(fields[i].substring(0, fields[i].indexOf("[")));
                                }else
                                    f = f.getType().getDeclaredField(fields[i]);
                            }
                        }
                        ParameterizedType pt = (ParameterizedType)f.getGenericType();
                        type = (Class<?>)pt.getActualTypeArguments()[0];

//                        for(String s:YangToJava.keySet()){
//                            if(s.contains(key) && s.length()>key.length() && s.substring(0, key.length()+1).equals(key+"[")){
//                                indice = s.substring(key.length()+1);
//                                indice = indice.substring(0, indice.indexOf("]"));
//                                break;
//                            }
//                        }
                    }else{
                        //   log.info("The list doesn0t exist");
                        return 2;
                    }
                    //setVariable(jWithIndex, jWithIndex, null, root);
                    Iterator<JsonNode> iter = ((ArrayNode)toSet).elements();
                    int res = 0;

                    //double set9 = System.nanoTime();//.currentTimeMillis();

                    while(iter.hasNext()){
                        //insert the list element

                        // JsonNode newValJava = getCorrectItem(mapper.writeValueAsString(iter.next()), varWithoutIndexes+"[]");
                        JsonNode newValJava = getCorrectItem(iter.next(), varWithoutIndexes+"[]");
                        //double set11 = System.nanoTime();//.currentTimeMillis();
                        // log.info("++getCorrectItem "+((set11-set9)/1000000));

                        if(newValJava!=null){
                            //    log.info("++jWithIndex "+jWithIndex);
                            //   log.info("++jmapper.writeValueAsString(newValJava) "+mapper.writeValueAsString(newValJava));
                            //    log.info("++root "+root);
                            if(!setVariable(jWithIndex+"[]", jWithIndex+"[]",mapper.writeValueAsString(newValJava), root))
                            {res = 1;
                                //   log.info("++err8 ");
                            }
                        }
                    }
                    //double set10 = System.nanoTime();//.currentTimeMillis();
                    //log.info("++writeValue2 "+((set10-set9)/1000000));
                    //log.info("++err9 ");
                    return res;
                }
                return 2;
            }
        }
    }

    private String noIndexes(String s){
        String[] split = s.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String ret = new String();
        for(int i=0;i<split.length;i++){
            if(i%2==0)
                ret+=split[i]+"[]";
        }
        if(!s.endsWith("]"))
            ret = ret.substring(0, ret.length()-2);
        return ret;
    }

    private String generalIndexes(String s){
        String[] split = s.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String l = new String();
        for(int i=0;i<split.length;i++){
            if(i%2==0){
                l+=split[i];
            }else{
                if(lists.containsKey(l+"[]")){
                    String ind = lists.get(l+"[]");
                    l+="["+ind+"]";
                }
            }
        }
        return l;
    }


    private String deleteIndexes(String var){
        String[] parts = var.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String res = new String();
        for(int i=0;i<parts.length;i++)
            if(i%2==0)
                res+=parts[i]+"[]";
        if(!var.endsWith("]"))
            res=res.substring(0, res.length()-2);
        return res;
    }

    public void parseCommand(String msgJson) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException{

        //CommandMsg msg = ((new Gson()).fromJson(msgJson, CommandMsg.class));
        try{

            //  log.info("msg json "+msgJson);
            CommandMsg msg = (mapper.readValue(msgJson, CommandMsg.class));
            String var = fromYangToJava(msg.var);
            log.info("Command "+msg.act);
            log.info("Variable "+msg.var);
            log.info("Variable in the code "+var);


            double timeStart = System.nanoTime();//.currentTimeMillis();
            //   log.info("++time parsing "+timeStart);

            switch(msg.act){
                case GET:
//                log.info("Arrived command GET of "+var);
//                log.info("Translated in "+var);
//                if(var==null)
//                    msg.obj=null;
//                if(var!=null && !var.equals("root") && state.containsKey(var.substring(5))){
////                    log.info("Is a Leaf");
//                    ObjectNode on= mapper.createObjectNode();
//                    String field = (msg.var.contains("/"))?msg.var.substring(msg.var.lastIndexOf("/")+1):msg.var;
//                    on.put(field, (new Gson()).toJson(getLeafValue(var.substring(5))));
//                   msg.objret = mapper.writeValueAsString(on);
////                   log.info("Leaf value "+msg.objret);
//                }
//                else{

                    //creare oggetto da passare!

                    Object result;
//                log.info("IT's not a leaf");
                    //String field = (msg.var.contains("/"))?msg.var.substring(msg.var.lastIndexOf("/")+1):msg.var;
                    result = getComplexObj(msg.var);

                    //double timegetComplex = System.nanoTime();//.currentTimeMillis();
                    //log.info("++timeComplex "+((timegetComplex-timeStart)/1000000));

                    msg.objret = mapper.writeValueAsString(result);

                    //double timeWriteValue = System.nanoTime();//.currentTimeMillis();
                    //log.info("++timewriteValue "+((timeWriteValue-timegetComplex)/1000000));


                    double timeFinal = System.nanoTime();//.currentTimeMillis();
                    msg.time = (timeFinal-timeStart)/1000000;
                    log.info("++timeFinal "+msg.time);

                    //cM.setResourceValue((new Gson().toJson(msg)));
                    cM.setResourceValue(mapper.writeValueAsString(msg));



                    // log.info("timestamp Get: "+(timestamp4-timestamp3));
                    break;
                case CONFIG:

                    //double timeStar = System.nanoTime();//.currentTimeMillis();
                    //     log.info("++timeStart "+timeStar);

                    String noInd = deleteIndexes(msg.var);
                    if(config.containsKey(noInd) && !config.get(noInd)){
                        //no configurable
                        //  log.info("Not configurable");
                        msg.objret = "2";
                        //cM.setResourceValue((new Gson()).toJson(msg));
                        cM.setResourceValue(mapper.writeValueAsString(msg));
                        return;
                    }

                    try {

                        Integer ret;
//                    if(var!=null){
//                        ((AppComponent)root).withdrawIntercepts();
                        //case 1: is a leaf - it is configurable (no configurable leafs are handled in the previous if)
                        if(var!=null && !var.equals("root")&&state.containsKey(var.substring(5))){
                            //   log.info("Config a leaf "+var);
                            boolean setted = setVariable(var.substring(5), var.substring(5), (String)msg.obj, root);
                            ret = (setted)?0:1;
                            //    log.info("Leaf should be configured");
                        }else{
                            //  log.info("Config a complex object");
                            ret = setComplexObject(msg.var, (String)msg.obj);
                            //  log.info("complex object should be configured");
                        }

                        //double timeSetVariable = System.nanoTime();//.currentTimeMillis();
                        //   log.info("++timeSetting "+((timeSetVariable-timeStar)/1000000));
//                    }else{
//                        log.info("Variable not found");
//                        ret = 2;
//                    }
                        msg.objret = ret.toString();//if equal to 0 REST generates 400 bad request
                        //    log.info("Result: "+ret);

                        double timeFin = System.nanoTime();//.currentTimeMillis();
                        msg.time = (timeFin-timeStart)/1000000;
                        log.info("++timeFin "+msg.time);

                        //  cM.setResourceValue((new Gson()).toJson(msg));
                        //    log.info("++setResourceValue "+mapper.writeValueAsString(msg));
                        cM.setResourceValue(mapper.writeValueAsString(msg));



                        //log.info("timestamp Config: "+(timestamp2-timestamp1));
//                    ((AppComponent)root).flowRuleService.removeFlowRulesById(((AppComponent)root).appId);
//                    ((AppComponent)root).requestIntercepts();
                        return;
                    } catch (NoSuchFieldException ex) {
                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    msg.objret = (new Integer(2)).toString();
                    //  log.info("Result: "+msg.objret);
                    // cM.setResourceValue((new Gson()).toJson(msg));
                    cM.setResourceValue(mapper.writeValueAsString(msg));


//                ((AppComponent)root).flowRuleService.removeFlowRulesById(((AppComponent)root).appId);
//                ((AppComponent)root).requestIntercepts();
                    break;
                case DELETE:
                    //delete
                    //   log.info("Arrived from ConnectionModule the command DELETE for "+msg.var);
                    Integer ret;
                    try{
                        if(var==null || var.equals("root")){
                            //     log.info("Can't delete the variable");
                            ret = 1;
                        }else{
                            String YangGeneralVar = noIndexes(msg.var);
                            if(YangMandatory.containsKey(YangGeneralVar) && YangMandatory.get(YangGeneralVar)){
                                //   log.info("The variable is mandatory");
                                ret = 1;
                            }else
                                ret = deleteVariable(root, var.substring(5), var.substring(5));
                        }
                    } catch (NoSuchFieldException ex) {
                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        ret = 1;
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        ret = 1;
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        ret = 1;
                    }
                    msg.objret = ret.toString();
                    //    log.info("From delete returning "+ret);
                    // cM.setResourceValue((new Gson()).toJson(msg));
                    cM.setResourceValue(mapper.writeValueAsString(msg));
                    break;
            }
        }catch (com.fasterxml.jackson.core.JsonParseException ex) {
            // Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int deleteVariable(Object actual, String var, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String[] fs = var.split(Pattern.quote("/"));
        if(fs.length==1){
            //delete
            if(var.contains("[")){
                //delete an element of the list
                String index = var.substring(var.lastIndexOf("[")+1, var.lastIndexOf("]"));
                if(index!=null && index.matches("")){
                    //shouldn't do this!
                    Field f = actual.getClass().getField(var.substring(0,var.length()-2));
                    f.set(actual, null);
                    return 0;
                }
                String listName = complete.substring(0, complete.length()-index.length()-1);
                listName+="]";
                listName = generalIndexes(listName)+"[]";
                String indice = null;
                //  log.info("Deleting from "+listName);
                if(lists.containsKey(listName))
                    indice = lists.get(listName);
                if(indice!=null){
                    //    log.info("Index!=null");
                    actual = actual.getClass().getField(var.substring(0, var.lastIndexOf("["))).get(actual);
                    Object delete = null;
                    if(List.class.isAssignableFrom(actual.getClass())){
                        for(Object item:(List)actual){
                            if(item.getClass().getField(indice).get(item).toString().equals(index)){
                                delete = item;
                                break;
                            }
                        }
                        if(delete!=null){
                            ((List)actual).remove(delete);
                            return 0;
                        }
                    }else if(Map.class.isAssignableFrom(actual.getClass())){
                        //     log.info("Is a Map!");
                        if(((Map)actual).containsKey(index)){
                            ((Map)actual).remove(index);
                            return 0;
                        }
                        else{
                            //   log.info("Faccio il ciclo");
                            for(Object k:((Map)actual).keySet())
                                if(k.toString().equals(index)){
                                    delete = k; break;}
                            if(delete!=null){
                                ((Map)actual).remove(delete);
                                return 0;
                            }
                            return 2;
                        }
                    }
                }
                return 2;
            }else{
                Field f = actual.getClass().getField(var);
                Class<?> type = f.getType();
                if(Boolean.class.isAssignableFrom(type))
                    f.set(actual, false);
                else if(f.get(actual) instanceof Number)
                    f.set(actual, 0);
                else
                    f.set(actual, null);
                return 0;
            }
        }else{
            //enter
            if(fs[0].contains("[")){
                String fName = fs[0].substring(0, fs[0].indexOf("["));
                String index = fs[0].substring(fs[0].indexOf("[")+1, fs[0].length()-1);
                actual = actual.getClass().getField(fName).get(actual);
                String listName = complete.substring(0, complete.length()-var.length()+fName.length());
                String indice = null;
                listName = generalIndexes(listName);
                if(lists.containsKey(listName+"[]"))
                    indice = lists.get(listName+"[]");
                if(actual!=null){
                    if(List.class.isAssignableFrom(actual.getClass())){
                        for(Object item:(List)actual)
                            if(item.getClass().getField(indice).get(item).toString().equals(index))
                                return deleteVariable(item, var.substring(fs[0].length()+1), complete);
                    }else if(Map.class.isAssignableFrom(actual.getClass())){
                        for(Object k:((Map)actual).keySet())
                            if(k.toString().equals(index))
                                return deleteVariable(((Map)actual).get(k), var.substring(fs[0].length()+1), complete);

                    }
                }
                return 2;
            }else{
                actual = actual.getClass().getField(fs[0]).get(actual);
                if(actual!=null)
                    return deleteVariable(actual, var.substring(fs[0].length()+1), complete);
                return 2;
            }
        }
    }

    public boolean setVariable(String var, String complete, String newVal, Object actual) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException{
        String[] fs = var.split(Pattern.quote("/"));
        if(fs.length==1){
            //to set
            if(var.contains("[")){
                String index = var.substring(var.indexOf("[")+1, var.indexOf("]"));
                Field f = actual.getClass().getField(var.substring(0, var.lastIndexOf("[")));
                ParameterizedType pt = (ParameterizedType)f.getGenericType();
                Class<?> itemType = (Class<?>)pt.getActualTypeArguments()[0];
                if(List.class.isAssignableFrom(f.getType())){
                    if(f.get(actual)==null){ // -- Gabriele: non mi è chiaro in quale caso entra qui, possibili errori
                        int i = Integer.parseInt(index);
                        try {
                            //  log.info("Setting the values of a list");
                            List<Object> l = (f.getType().isInterface())?new ArrayList<>():(List)f.getType().newInstance();
                            Object toInsert;
                            try {
                                toInsert = mapper.readValue(newVal, itemType);
                            } catch(Exception ex){
                                toInsert = personalizedDeserialization(itemType, newVal);
                            }
                            try{
                                //l.add((new Gson()).fromJson(newVal, itemType));
                                if(toInsert!=null)
                                    l.set(i, toInsert);
                            } catch (IndexOutOfBoundsException ex) {
                                l.add(toInsert);
                            }
                            f.set(actual, l);
                            return true;
                        } catch (InstantiationException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                            //  log.info(ex.getMessage());
                        }
                    }else if(index.matches("")){
                        //   log.info("return else - var "+var);
                        //List<Object> newList = (new Gson()).fromJson(newVal, List.class);
                        //((List)f.get(actual)).add((new Gson()).fromJson(newVal, itemType));
                        String listName = complete.substring(0, complete.length()-index.length()-1);
                        listName = generalIndexes(listName)+"[]";
                        if(lists.containsKey(listName)) {
                            String indice = lists.get(listName);
                            List<Object> l = (List) f.get(actual);
                            Object toChange = null;
                            Object o;
                            try {
                                o = mapper.readValue(newVal, itemType);
                            } catch (Exception e) {
                                o = personalizedDeserialization(itemType, newVal);
                            }
                            for (Object item : l) {
                                if (item.getClass().getField(indice).get(item).toString().equals(o.getClass().getField(indice).get(o).toString())) {
                                    toChange = item;
                                    break;
                                }
                            }
                            l.add(o);
                            if (toChange != null)
                                l.remove(toChange);
                            return true;
                        }
                        return false;
                    }else{
                        String listName = complete.substring(0, complete.length()-index.length()-1);
                        listName = generalIndexes(listName)+"[]";
                        //listName+="]"; -- Gabriele: perchè questa append?
                        if(lists.containsKey(listName)){
                            Object toChange = null;
                            String indice = lists.get(listName);
                            List<Object> l = (List)f.get(actual);
                            for(Object item:l){
                                if(item.getClass().getField(indice).get(item).toString().equals(index)){
                                    toChange = item;
                                    break;
                                }
                            }
                            if(toChange!=null){
                                try{
                                    //l.add((new Gson()).fromJson(newVal, itemType));
                                    l.add(mapper.readValue(newVal, itemType));
                                    l.remove(toChange);
                                    return true;
                                }catch(Exception e){
                                    Object toInsert = personalizedDeserialization(itemType, newVal);
                                    if(toInsert!=null){
                                        l.add(toInsert);
                                        l.remove(toChange);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }else if(Map.class.isAssignableFrom(f.getType())){
                    if(f.get(actual)==null){
                        try {
                            Map<Object, Object> m = (f.getType().isInterface())?new HashMap<>():(Map)f.getType().newInstance();
                            Class<?> valueType = (Class<?>)pt.getActualTypeArguments()[1];
                            ObjectNode node = (ObjectNode)mapper.readTree(newVal);
                            JsonNode kNode = node.get("{key}");
                            node.remove("{key}");
                            Object k = (Number.class.isAssignableFrom(itemType))?kNode.asLong():kNode.asText();
                            Object value = null;
                            try{valueType.newInstance();}
                            catch(Exception valueException){
                                value = personalizedNewInstance(valueType);
                            }
                            if(value!=null){
                                Iterator<String> fields = node.fieldNames();
                                while(fields.hasNext()){
                                    String fieldName = fields.next();
                                    JsonNode v = node.get(fieldName);
                                    Field fV = value.getClass().getField(fieldName);
                                    if(Number.class.isAssignableFrom(fV.getType()))
                                        fV.set(value, v.asDouble());
                                    else
                                        fV.set(value, v.asText());
                                }
                                //value = ((new Gson()).fromJson(mapper.writeValueAsString(node), valueType));
                                m.put(k, value);
                                f.set(actual, m);
                                return true;
                            }
                            else
                            {  // log.info("++fal1 ");
                                return false;}
                            //!!
                            //m.put((new Gson()).fromJson(newVal, Map.Entry<Object,itemType>));
                        } catch (InstantiationException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else if(index.matches("")){
                        try{
                            Class<?> valueType = (Class<?>)pt.getActualTypeArguments()[1];
                            ObjectNode node = (ObjectNode)mapper.readTree(newVal);
                            JsonNode kNode = node.get("{key}");
                            node.remove("{key}");
                            Object k = (Number.class.isAssignableFrom(itemType))?kNode.asLong():kNode.asText();
                            Object value = null;
                            try{
                                if (Number.class.isAssignableFrom(valueType))
                                    value = 0;
                                else
                                    value = valueType.newInstance();
                            }
                            catch(Exception valueException){
                                value = personalizedNewInstance(valueType);
                            }
                            if(value!=null){
                                Iterator<String> fields = node.fieldNames();
                                while(fields.hasNext()){
                                    String fieldName = fields.next();
                                    JsonNode v = node.get(fieldName);
                                    //   log.info("##the field is "+fieldName+" and the value is "+v );
                                    if(fieldName.equals("{value}")){
                                        if(Number.class.isAssignableFrom(valueType))
                                            value = v.asDouble();
                                        else
                                            value = v.asText();
                                    }else{
                                        Field fV = value.getClass().getField(fieldName);
                                        if(Number.class.isAssignableFrom(fV.getType()))
                                            fV.set(value, v.asDouble());
                                        else
                                            fV.set(value, v.asText());
                                    }
                                }
                                //value = ((new Gson()).fromJson(mapper.writeValueAsString(node), valueType));
                                ((Map)f.get(actual)).put(k, value);
                                return true;
                            }
                            else{
                                //  log.info("++fal2 ");
                                return false;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                            // log.info(ex.getMessage());
                        }
                        //((Map)f.get(actual)).put((new Gson()).fromJson(newVal, itemType));
                        //((Map)f.get(actual)).put((new Gson()).fromJson(newVal, itemType));
                    }else{
                        String listName = complete.substring(0, complete.length()-index.length()-1);
                        listName = generalIndexes(listName)+"[]";
                        listName+="]";
                        if(lists.containsKey(listName)){
                            Object toChange = null;
                            String indice = lists.get(listName);
                            if(List.class.isAssignableFrom(f.getType())){
                                List<Object> l = (List)f.get(actual);
                                for(Object item:l){
                                    if(item.getClass().getField(indice).get(item).toString().equals(index)){
                                        toChange = item;
                                        break;
                                    }
                                }
                                if(toChange!=null){
                                    try{
                                        //l.add((new Gson()).fromJson(newVal, itemType));
                                        l.add(mapper.readValue(newVal, itemType));
                                        l.remove(toChange);
                                    }catch(Exception e){
                                        Object toInsert = personalizedDeserialization(itemType, newVal);
                                        if(toInsert!=null){
                                            l.add(toInsert);
                                            l.remove(toChange);
                                        }
                                    }
                                }
                            }else if(Map.class.isAssignableFrom(f.getType())){
                                Map<Object, Object> l = (Map)f.get(actual);
                                for(Object item:l.keySet()){
                                    if(item.toString().equals(index)){
                                        toChange = item;
                                        break;
                                    }
                                }
                                if(toChange!=null){

                                    //l.put((new Gson()).fromJson(newVal, new TypeToken<l>(){}.getType()));
                                    l.remove(toChange);
                                }
                            }
                        }
                    }
                }

            }else{
                Field f = actual.getClass().getField(var);
                //  log.info("--Arrivata al field da configurare "+f.getName()+" "+f.getGenericType());
                //  log.info("Valore: "+newVal);
                Object toInsert;
                try{
                    //toInsert = (new Gson()).fromJson(newVal, f.getGenericType());
                    toInsert = myGson.fromJson(newVal, f.getGenericType());
                    //    log.info("Translated");
                    f.set(actual, toInsert);
                    //   log.info("Settato!");
                    return true;
                }catch(Exception e){
                    //   log.info("Sono nel catch");
                    toInsert = personalizedDeserialization(f.getType(), newVal);
                    //  log.info("ToInsert is "+toInsert);
                    if(toInsert!=null){
                        //     log.info("Non è null");
                        f.set(actual, toInsert);
                        //   log.info("Settato");
                        return true;
                    }
                    //log.("++fal3 ");
                    return false;
                }
//                log.info("okk settato");
            }

        }else{
            if(fs[0].contains("[")){
                //select element in the list
                // log.info("in if ");
                String listName = complete.substring(0, complete.length()-var.length()+fs[0].length());
                String idItem = listName.substring(listName.lastIndexOf("[")+1, listName.lastIndexOf("]"));
                listName = listName.substring(0, listName.length()-idItem.length()-2)+"[]";
                listName = generalIndexes(listName)+"[]";
                String indice = null;
                if(lists.containsKey(listName))
                    indice = lists.get(listName);
                actual = actual.getClass().getField(fs[0].substring(0, fs[0].length()-idItem.length()-2)).get(actual);
                if(List.class.isAssignableFrom(actual.getClass())){
                    for(Object litem:(List)actual){
                        boolean correct = litem.getClass().getField(indice).get(litem).toString().equals(idItem);
                        if(correct)
                            return setVariable(var.substring(fs[0].length()+1), complete, newVal, litem);
                    }
                }else if(Map.class.isAssignableFrom(actual.getClass())){
                    for(Object litem:((Map)actual).keySet()){
                        boolean correct = litem.toString().equals(idItem);
                        if(correct)
                            return setVariable(var.substring(fs[0].length()+1), complete, newVal, ((Map)actual).get(litem));
                    }
                }
            }else{
                Field f = actual.getClass().getField(fs[0]);
                //   log.info("Passing throug "+f.getGenericType());
                actual = f.get(actual);
                // log.info("Var "+var);
                return setVariable(var.substring(fs[0].length()+1), complete, newVal, actual);
            }
        }
        //log.info("return false - var "+var);
        return false;
    }

  /*  public void stopSL(){
        stopTimerTasks();
        stopCondition = true;

    } */


    //returns the id value of the given item of the list
    private String searchLeafInList(Object actual, String idLista) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
        String id = null;
        Field[] fs = actual.getClass().getFields();
        for(int i=0; i<fs.length;i++)
            if(fs[i].getName().equals(idLista)){
                Object f = actual.getClass().getField(idLista).get(actual);
                id = (f==null)?null:f.toString();
                return id;
            }
        return idLista;
    }

    //get the value of a specific leaf
    public Object getLeafValue(String id){

        double timestamp1 = System.nanoTime();

        if(state.containsKey(id))
            return state.get(id);
        try{
            Object actual = root;
            String[] fields = id.split(Pattern.quote("/"));
            String recompose = new String();
            for(int i = 0; i<fields.length; i++){
                //log.info("Actual is "+actual);
                if(actual==null)
                    return null;
                recompose +="/"+fields[i];
                if(fields[i].contains("[")){
                    String field = fields[i].substring(0, fields[i].lastIndexOf("["));
                    String index = fields[i].substring(fields[i].lastIndexOf("[")+1, fields[i].lastIndexOf("]"));
                    actual = actual.getClass().getField(field).get(actual);
                    if(Map.class.isAssignableFrom(actual.getClass())){
                        if(i<fields.length-1 && fields[i+1].equals("{key}")){
                            boolean found = false;
                            for(Object k:((Map)actual).keySet()){
                                if(k.toString().equals(index)){
                                    actual= index;
                                    found = true;
                                    break;
                                }
                            }
                            if(!found)
                                actual = null;
                        }
                        else{
                            boolean found = false;
                            for(Object k:((Map)actual).keySet()){
                                if(k.toString().equals(index)){
                                    actual= ((Map)actual).get(k);
                                    found = true;
                                    break;
                                }
                            }
                            if(!found)
                                actual = null;
                        }
                    }else{
                        String general = generalIndexes(recompose.substring(1));
                        actual = getListItemWithIndex((List)actual,index, general.substring(0, general.lastIndexOf("["))+"[]");
                    }
                }else{
                    if(fields[i].equals("{key}"))
                        continue;
                    if(fields[i].equals("{value}"))
                        continue;
                    actual = actual.getClass().getField(fields[i]).get(actual);
                }
            }
            double timestamp2 = System.nanoTime();
            log.info("fetch '"+id+"': "+(timestamp2-timestamp1)/1000000);
            return actual;
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String fromYangToJava(String y){

        //double timestamp1 = System.nanoTime();

        String[] separated = y.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        String yang = new String();
        for(int i=0; i<separated.length;i++)
            if(i%2==0 && i!=separated.length-1)
                yang+=separated[i]+"[]";
        if(separated.length%2==1)
            yang+=separated[separated.length-1];
        String j =null;
        if(YangToJava.containsValue(yang))
            for(String s:YangToJava.keySet())
                if(YangToJava.get(s).equals(yang))
                    j=s;
        if(j==null)
            return j;
        String[] java = j.split("["+Pattern.quote("[")+"," +Pattern.quote("]")+"]");
        j=new String();
        for(int i=0; i<java.length; i++){
            if(i%2==0)
                j+=java[i];
            else{
                j+="["+separated[i]+"]";
            }
        }
        if(y.endsWith("[]"))
            j+="[]";

        //double timestamp2 = System.nanoTime();
        //log.info("fromYangToJava '"+y+"': "+(timestamp2-timestamp1)/1000000);
        return j;

    }

    public Object getLists(Object actual, String remaining, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        if (actual == null)
            return null;
        String[] fs = remaining.split(Pattern.quote("/"));
        String fint = fs[0];
        if(fint.contains("[]")){
            //Siamo arrivati!
            fint = fint.substring(0, fint.length()-2);
            actual = actual.getClass().getField(fint).get(actual);
            return actual;
        }else{
            if(fint.contains("[")){
                //dobbiamo andare a prendere il valore giusto all'interno della lista
                String indice = fint.substring(fint.indexOf("[")+1, fint.length()-1);
                String listName = complete.substring(0, complete.length()-remaining.substring(fint.length()+1).length() -indice.length()-3) + "[]";
                actual = actual.getClass().getField(fint.substring(0, fint.length()-indice.length()-2)).get(actual);
                Object item = null;
                if(List.class.isAssignableFrom(actual.getClass()))
                    item = getListItemWithIndex((List)actual, indice, listName);
                else if(Map.class.isAssignableFrom(actual.getClass()))
                    item = (((Map)actual).get(indice));
                return getLists(item, remaining.substring(fint.length()+1), complete);
            }else{
                //dobbiamo andare dentro l'oggetto
                actual = actual.getClass().getField(fint).get(actual);
                return getLists(actual, remaining.substring(fint.length()+1), complete);
            }
        }
    }

    //given a list, gets the element with the id specified
    private Object getListItemWithIndex(List list, String indice, String listName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String indexValue=null;
        String general = generalIndexes(listName)+"[]";
        if(lists.containsKey(listName)){
            indexValue = lists.get(listName);
        }
        if(indexValue!=null){
            for(Object obj:list){
                String i = obj.getClass().getField(indexValue).get(obj).toString();
                if(indice.equals(i))
                    return obj;
            }
        }
        return null;
    }

/*    private void findYangLeafs(YangTreeNode tree) {
        YANG_Body node = tree.getNode();
        //System.out.println(node);
        Vector<YangTreeNode> children = tree.getChilds();
        if(children.size()==0){
            //System.out.println("Is a leaf");
            YANG_Config config = node.getConfig();
            //System.out.println(config);
        }
        for(int i=0;i<children.size();i++){
            findYangLeafs(children.get(i));
        }
    }*/


    //Versione YIN (xml) non usata
    private void findYinLeafs(Element e, String prev){
        if(e.getTagName().equals("leaf")){
            //System.out.println(prev+"/"+e.getAttribute("name"));
            NodeList att = e.getChildNodes();
            for(int i=0;i<att.getLength();i++){
                if(att.item(i).getNodeName().equals("config")){
                    //    log.info("Ho trovato il config");
                    boolean c = (att.item(i).getAttributes().item(0).getNodeValue().equals("true"))?true:false;
                    //System.out.println("-+-config "+att.item(i).getAttributes().item(0).getNodeValue());
                    config.put(prev.substring(1)+"/"+e.getAttribute("name"), c);
                }
                if(att.item(i).getNodeName().equals("type")){
                    //  log.info("Ho trovato il type");
                    String t = att.item(i).getAttributes().item(0).getNodeValue();
                    // log.info(prev.substring(1)+"/"+e.getAttribute("name")+" is a "+t);
                    YangType.put(prev.substring(1)+"/"+e.getAttribute("name"), t);
                }
                //default
            }
            if(!config.containsKey(prev+"/"+e.getAttribute("name")))
                config.put(prev.substring(1)+"/"+e.getAttribute("name"), true);
            //System.out.println("Lista config -- "+config);
            return;
        }
        Node n = (Node)e;
        NodeList children = n.getChildNodes();
        boolean list = e.getNodeName().equals("list");
        for(int i=0;i<children.getLength();i++)
            if(children.item(i).getNodeType()==Node.ELEMENT_NODE){
                String pref = prev+"/"+e.getAttribute("name");
                pref=(list)?pref+"[]":pref;
                findYinLeafs((Element)children.item(i), pref);
            }
    }


    //Versione "YIN" Json
    private void findYinLeafs(JsonNode y, String prev) {
        Iterator<Entry<String, JsonNode>> iter = y.fields();
        while(iter.hasNext()){
            Entry<String, JsonNode> value = iter.next();
            String fieldName = value.getKey();
            JsonNode valueNode = value.getValue();
            if(fieldName.equals("leaf")){
                //can be an array
                if(valueNode.isArray()){
                    Iterator<JsonNode> leafs = ((ArrayNode)valueNode).elements();
                    while(leafs.hasNext()){
                        ObjectNode child = (ObjectNode)leafs.next();
                        boolean conf;
                        if(child.get("config")!=null){
                            conf = child.get("config").get("@value").asBoolean();
                        }else{
                            conf = true;
                        }
                        config.put(prev+"/"+child.get("@name").textValue(), conf);

                        if(child.get("type")!=null){
                            String type = child.get("type").get("@name").asText();
                            YangType.put(prev+"/"+child.get("@name").textValue(),type);
                        }
                        if(child.get("mandatory")!=null){
                            Boolean mand = child.get("mandatory").get("@value").asBoolean();
                            YangMandatory.put(prev+"/"+child.get("@name").textValue(), mand);
                        }else
                            YangMandatory.put(prev+"/"+child.get("@name").textValue(), true);
                        //check advertise attribute - prefix:advertise
                        Iterator<String> searchAdv = child.fieldNames();
                        String pref=null;
                        while(searchAdv.hasNext()){
                            String f = searchAdv.next();
                            if(f.endsWith(":advertise")){
                                pref = f.substring(0, f.length()-10);
                                break;
                            }
                        }
                      /*  if(pref!=null){
                            //the advertise field is specified
                            String adv = child.get(pref+":advertise").get("@advertise").asText();
                            if(adv.equals("onchange")){
                                toListenPush.add(prev+"/"+child.get("@name").textValue());
                            }else if(adv.equals("periodic")){
                                if(child.has(pref+":period")){
                                    long p = child.get(pref+":period").get("@period").asLong();
                                    PeriodicVariableTask task = new PeriodicVariableTask(this, prev+"/"+child.get("@name").textValue());
                                    toListenTimer.add(task);
                                    timer.schedule(task, p, p);
                                }
                                //has to have!!
                            }else if(adv.equals("onthreshold")){
                                Object min = null;
                                Object max = null;
                                if(child.has(pref+":minthreshold")){
                                    min = child.get(pref+":minthreshold").get("@minthreshold").asDouble();
                                }
                                if(child.has(pref+":maxthreshold")){
                                    max = child.get(pref+":maxthreshold").get("@maxthreshold").asDouble();
                                }
                                if(min!=null || max!=null)
                                    toListenThreshold.put(prev+"/"+child.get("@name").textValue(), new Threshold(min, max));
                            }
                            //if never - nothing
                        }*/
                        //default:never
                    }
                }else{
                    //one single leaf
                    boolean conf;
                    if(valueNode.get("config")!=null){
                        conf = valueNode.get("config").get("@value").asBoolean();
                    }else{
                        conf = true;
                    }
                    config.put(prev+"/"+valueNode.get("@name").asText(), conf);
                    Iterator<String> searchAdv = valueNode.fieldNames();
                    String pref=null;
                    while(searchAdv.hasNext()){
                        String f = searchAdv.next();
                        if(f.endsWith(":advertise")){
                            pref = f.substring(0, f.length()-10);
                            break;
                        }
                    }
                   /* if(pref!=null){
                        //the advertise field is specified
                        String adv = valueNode.get(pref+":advertise").get("@advertise").asText();
                        if(adv.equals("onchange")){
                            toListenPush.add(prev+"/"+valueNode.get("@name").asText());
                        }else if(adv.equals("periodic")){
                            if(valueNode.has(pref+":period")){
                                long p = valueNode.get(pref+":period").get("@period").asLong();
                                PeriodicVariableTask task = new PeriodicVariableTask(this, prev+"/"+valueNode.get("@name").asText());
                                toListenTimer.add(task);
                                timer.schedule(task, p, p);
                            }
                            //has to have!!
                        }else if(adv.equals("onthreshold")){
                            Object min = null;
                            Object max = null;
                            if(valueNode.has(pref+":minthreshold")){
                                min = valueNode.get(pref+":minthreshold").get("@minthreshold").asDouble();
                            }
                            if(valueNode.has(pref+":maxthreshold")){
                                max = valueNode.get(pref+":maxthreshold").get("@maxthreshold").asDouble();
                            }
                            if(min!=null || max!=null)
                                toListenThreshold.put(prev+"/"+valueNode.get("@name").textValue(), new Threshold(min, max));
                        }
                        //if never - nothing
                    }*/
                    //default:never
                }
            }else{
                //traverse
                if(valueNode.isArray()){
                    Iterator<JsonNode> objs = ((ArrayNode)valueNode).elements();
                    while(objs.hasNext()){
                        JsonNode next = objs.next();
                        if(next.has("@name")&&fieldName.equals("list"))
                            findYinLeafs(next, prev+"/"+next.get("@name").textValue()+"[]");
                        else if(next.has("@name"))
                            findYinLeafs(next, prev+"/"+next.get("@name").textValue());
                    }
                }else{
                    if(valueNode.has("@name")&&fieldName.equals("list"))
                        findYinLeafs(valueNode, prev+"/"+valueNode.get("@name").textValue()+"[]");
                    else if(valueNode.has("@name"))
                        findYinLeafs(valueNode, prev+"/"+valueNode.get("@name").textValue());
                }
            }
        }
    }


  /*  private void checkThreshold(Map<String, Object> thr) throws JsonProcessingException {
        //values in stateNew
        for(String s: thr.keySet()){
            //if threshold -> publish
            boolean pub = false;
            String generalS = generalIndexes(s);
            String y = null;
            if(YangToJava.containsKey("root/"+generalS)){
                y = YangToJava.get("root/"+generalS);
                if(toListenThreshold.containsKey(y)){
                    if(toListenThreshold.get(y).MIN!=null){
                        if(toListenThreshold.get(y).MAX!=null){
                            if(((Number)thr.get(s)).doubleValue() > (Double)toListenThreshold.get(y).MIN && ((Number)thr.get(s)).doubleValue() < (Double)toListenThreshold.get(y).MAX)
                                pub = true;
                        }else if (((Number)thr.get(s)).doubleValue() > (Double)toListenThreshold.get(y).MIN){
                            pub = true;
                        }
                    }else{
                        if(((Number)thr.get(s)).doubleValue() < (Double)toListenThreshold.get(y).MAX)
                            pub = true;
                    }
                }
            }
            if(pub){
                if(!stateThreshold.containsKey(s) || !stateThreshold.get(s).equals(thr.get(s))){
                    NotifyMsg e = new NotifyMsg();
                    e.act = action.UPDATED;
                    e.var = trasformInPrint(s);
                    e.obj = thr.get(s).toString();
                    e.currentTime = System.nanoTime();
                    stateThreshold.put(s, thr.get(s));
                 //   log.info("*OnThreshold* "+(new Gson()).toJson(e));
                    //cM.somethingChanged((new Gson()).toJson(e));
                     cM.somethingChanged(mapper.writeValueAsString(e));
                 //   log.info("Send UPDATE "+System.nanoTime());
                }
            }else{
                if(stateThreshold.containsKey(s))
                    stateThreshold.remove(s);
            }
        }
    }*/

    public enum action{ADDED, UPDATED, DELETED, NOCHANGES, PERIODIC};
    public class NotifyMsg{
        public action act;
        public Object obj;
        public String var;
        public Date timestamp = new Date(System.currentTimeMillis());
        public long currentTime;

        public action getAct() {
            return act;
        }

        public void setAct(action act) {
            this.act = act;
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }

    private class Threshold{
        public Object MAX;
        public Object MIN;

        public Threshold(Object MIN , Object MAX){
            this.MAX = MAX;
            this.MIN = MIN;
        }
    }


    //Task for periodic variables
 /*   private class PeriodicVariableTask extends TimerTask{
        String var;
        StateListenerNew sl;

        public PeriodicVariableTask(StateListenerNew sl, String var){
            this.sl = sl;
            this.var = var;
        }

        public void run(){
            sl.log.info("**Periodic Task of " + var+ " running**");
            Map<String, Object> listToSave = new HashMap<>();
            try{
                if(YangToJava.containsValue(var)){
                    String j = null;
                    for(String k:YangToJava.keySet())
                        if(YangToJava.get(k).equals(var)){
                            j = k;
                            break;
                        }
                    sl.saveValues(sl.root, j.substring(5), j.substring(5), listToSave);
                }
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(String s: listToSave.keySet()){
                NotifyMsg e = new NotifyMsg();
                e.act = action.PERIODIC;
                e.obj = listToSave.get(s).toString();
                e.var = sl.trasformInPrint(s);
                e.currentTime = System.nanoTime();

                try {
                    //sl.cM.somethingChanged((new Gson()).toJson(e));
                    sl.cM.somethingChanged(mapper.writeValueAsString(e));
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }*/

//        private String allGeneralIndexes(String listName) {
//        String[] splitted = listName.split("["+Pattern.quote("[")+Pattern.quote("]")+"]");
//        String j=splitted[0];
//        String onlyLastOne = splitted[0];
//        String y=null;
//        if(splitted.length>1)
//            for(int i=1;i<splitted.length;i++){
//                if(i%2==0){
//                    //nome lista
//                    j+=splitted[i];
//                    onlyLastOne+=splitted[i];
//                }else{
//                    //chiave
//                    if(stateList.containsKey(onlyLastOne+"[]"))
//                        j+="["+stateList.get(onlyLastOne+"[]").idList+"]";
//                    onlyLastOne+=(i==splitted.length-1)?("["+stateList.get(onlyLastOne+"[]").idList+"]"):("["+splitted[i]+"]");
//
//                }
//            }
//        return j;
//    }
//
//    //nullValuesToListen now exist? --> in state, removed form that list
//    //additions or removes in the lists --> added/deleted by state
//    //check if the variables in state have the same value or not
//    public void checkValue(){
//        try{
//            Gson gson = new Gson();
//            List<String> r = new ArrayList<>();
//            List<String> nulls = new ArrayList();
//            nulls.addAll(nullValuesToListen);
//            for(String s:nulls){
//                if(searchLeaf(root, s, s))
//                    r.add(s);
//            }
//            for(String s:r){
//                nullValuesToListen.remove(s);
//            }
//            List<String> copy = new ArrayList<>();
//            copy.addAll(stateList.keySet());
//            for(String lv:copy){
//                List<Object> act = (List)getLists(root, lv, lv);
//                List<NotifyMsg> wH = checkListChanges(lv, stateList.get(lv).List, act);
//
//                if(wH!=null){
//                    writeLock.lock();
//                    whatHappened.addAll(wH);
//                    writeLock.unlock();
//                }
//            }
//            List<String> copyState = new ArrayList();
//            if(state!=null)
//                copyState.addAll(state.keySet());
//            for(String s:copyState){
//                boolean c= getLeafValueChange(root, s, s);
//                if(!c){
//                    NotifyMsg e = new NotifyMsg();
//                    e.act=action.UPDATED;
//                    e.obj=state.get(s);
//                    e.var=s;
//                    writeLock.lock();
//                    whatHappened.add(e);
//                    writeLock.unlock();
//                }
//            }
//            List<NotifyMsg> wH = new ArrayList<>();
//            writeLock.lock();
//            wH.addAll(whatHappened);
//            whatHappened = new ArrayList<>();
//            writeLock.unlock();
//            if(wH!=null){
//                for(NotifyMsg e:wH){
//                    String toPrint = trasformInPrint(e.var);
//                    //System.out.println(e.act + " " + toPrint+" " + gson.toJson(e.obj));
//                }
//            }
//
//            //System.out.println("state aggiornato: " + state);
//            //System.out.println("root "+(new Gson()).toJson(root));
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    //returns true if the actual value and the old value are the same, false othercase
//    //recursive
//    private boolean getLeafValueChange(Object actual, String remaining, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//        String[] fields = remaining.split(Pattern.quote("/"));
//        String finteresting = fields[0];
//        String fremaining = (fields.length>1)?remaining.substring(finteresting.length()+1):null;
//        boolean lista = false;
//        if(finteresting.contains("[")){
//            //devo andare a cercare il giusto oggetto dentro la lista
//            lista = true;
//            String indice = finteresting.substring(finteresting.indexOf("[")+1, finteresting.indexOf("]"));
//            String listName = complete.substring(0, complete.length()-fremaining.length()-indice.length()-3) + "[]";
//            actual = actual.getClass().getField(finteresting.substring(0, finteresting.length()-indice.length()-2)).get(actual);
//            if(actual==null){
//                if(state.get(complete)==null)
//                    return false;
//                else{
//                    //System.out.println("Rimossa lista");
//                    return true;
//                }
//            }
//            Object item = getListItemWithIndex((List)actual, indice, listName);
//            if(item==null){
//                //System.out.println("No items in list");
//                return false;
//            }
//            return getLeafValueChange(item, fremaining, complete);
//        }else{
//            if(fields.length>1){
//                actual = actual.getClass().getField(finteresting).get(actual);
//                if(actual==null){
//                    if(state.get(complete) == null)
//                        return false;
//                    else{
//                        //System.out.println("Removed obj " + finteresting);
//                        return true;
//                    }
//                }
//                return getLeafValueChange(actual, fremaining, complete);
//            }
//            else{
//                actual = actual.getClass().getField(finteresting).get(actual);
//                if(!state.containsKey(complete)){
//                    state.put(complete, actual);
//                    return false;
//                }
//                boolean rValue = state.get(complete).equals(actual);
//                if(!rValue)
//                    state.replace(complete, actual);
//                return rValue;
//            }
//        }
//    }
//
    //callable by the app
//    public void addNewListener(String name){
//        try {
//            searchLeaf(root, name, name);
//            toListen.add(name);
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
////founds additions or removes of items in a list
//    public List<NotifyMsg> checkListChanges(String listName, List oldList, List newList) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//        List<NotifyMsg> res = new ArrayList<>();
//        if(!stateList.containsKey(listName)){
//            //la lista è tutta nuova
//            String id = null; //prendere dal toListen
//            for(Object n:newList){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.ADDED;
//                e.obj = n;
//                e.var = listName;
//                res.add(e);
//                //ADD THE ELEMENT IN STATE
//                String savedInToListen = listName.substring(0, listName.length()-1) + id+"]";
//                List<String> save = new ArrayList<>();
//                for(String t:toListen){
//                    if(t.contains(savedInToListen) && t.substring(0, savedInToListen.length()).equals(savedInToListen))
//                        save.add(t);
//                }
//                for(String t:save){
//                    String fremaining = t.substring(savedInToListen.length());
//                    String idItem = savedInToListen.substring(0, savedInToListen.length()-id.length()-2)+n.getClass().getField(id).get(n).toString()+"]."+fremaining;
//                    searchLeaf(n, fremaining, idItem);
//                }
//            }
//            saveListstate(listName, id, newList);
//            return res;
//        }
//        String id = stateList.get(listName).idList;
//
//        if(oldList==null && newList==null){
//            return null;
//        }
//        if(oldList==null){
//            
//        }
//        if(newList==null){
//            for(Object old:oldList){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.REMOVED;
//                e.obj = old;
//                e.var = listName;
//                res.add(e);
//                //REMOVE THE ELEMENT IN STATE
//                List<String> remove = new ArrayList<>();
//                String identifier = listName.substring(0, listName.length()-1)+old.getClass().getField(id).get(old).toString()+"]";
//                for(String s:state.keySet()){
//                    if(s.contains(identifier) && s.substring(0, identifier.length()).equals(identifier))
//                        remove.add(s);
//                }
//                for(String s:remove)
//                    state.remove(s);
//            }
//            saveListstate(listName, id, newList);
//            return res;
//        }
//        List shadowCopy = new LinkedList();
//        shadowCopy.addAll(newList);
//        for(Object old:oldList){
//            String idValue = old.getClass().getField(id).get(old).toString();
//            boolean found = false;
//            for(Object n:shadowCopy){
//                String idValue2 = n.getClass().getField(id).get(n).toString();
//                if(idValue.equals(idValue2)){
//                    found = true;
//                    break;
//                }
//            }
//            if(found==false){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.REMOVED;
//                e.obj = old;
//                e.var = listName;
//                res.add(e);
//                //REMOVE THE ELEMENT IN STATE
//                List<String> remove = new ArrayList<>();
//                String identifier = listName.substring(0, listName.length()-1)+idValue+"]";
//                for(String s:state.keySet()){
//                    if(s.contains(identifier) && s.substring(0, identifier.length()).equals(identifier))
//                        remove.add(s);
//                }
//                for(String s:remove)
//                    state.remove(s);
//            }
//        }
//        for(Object n:shadowCopy){
//            String idValue = n.getClass().getField(id).get(n).toString();
//            boolean found = false;
//            for(Object old:oldList){
//                String idValue2 = old.getClass().getField(id).get(old).toString();
//                if(idValue.equals(idValue2)){
//                    found = true;
//                    break;
//                }
//            }
//            if(found==false){
//                NotifyMsg e = new NotifyMsg();
//                e.act = action.ADDED;
//                e.obj = n;
//                e.var = listName;
//                res.add(e);
//                //ADD THE ELEMENT IN STATE
//                String savedInToListen = allGeneralIndexes(listName);
//                savedInToListen +="["+id+"]";
//                List<String> save = new ArrayList<>();
//                for(String t:toListen){
//                    if(t.contains(savedInToListen) && t.substring(0, savedInToListen.length()).equals(savedInToListen))
//                        save.add(t);
//                }
//                for(String t:save){
//                    String fremaining = t.substring(savedInToListen.length()+1);
//                    //String idItem = savedInToListen.substring(0, savedInToListen.length()-id.length()-2)+idValue+"]."+fremaining;
//                    String idItem = listName.substring(0, listName.length()-1)+idValue+"]."+fremaining;
//                    searchLeaf(n, fremaining, idItem);
//                }
//            }
//            
//        }
//        if(!res.isEmpty())
//            saveListstate(listName, id, newList);    
//        return (res.isEmpty())?null:res;
//    }
//    //ricorsive method, if the object exists, puts the object to observe in state, and eventuals lists that founds in the stateList
//    //if still the object doesn't exists(or one of the "containers" puts the path to the object in the nullValuesToListen
//    private boolean searchLeaf(Object actual, String fields, String complete) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//        String[] fs = fields.split(Pattern.quote("/"));
//        String finteresting = fs[0];
//        String fremaining = (fs.length>1)?fields.substring(finteresting.length()+1):null;
//        boolean lista = false;
//        String idLista=null;
//        Field f;
//        if(finteresting.contains("[")){
//            lista=true;
//            idLista = finteresting.substring(finteresting.indexOf("[")+1, finteresting.indexOf("]"));
//            finteresting = finteresting.substring(0, finteresting.indexOf("["));
//        }
//        f = actual.getClass().getField(finteresting);
//        actual = f.get(actual);
//        if(actual==null){
//            //salva temporaneamente: l'oggetto di interesse ancora non esiste
//            if(!nullValuesToListen.contains(complete))
//                nullValuesToListen.add(complete);
//            return false;
//        }
//        if(fs.length>1){
//            //calcola nuovo obj e nuovo fields
//            if(lista){
//                //searchLeaf in tutti gli elementi + controllo stato lista              
//                String idL = complete.substring(0,complete.length()-fremaining.length()-idLista.length()-2)+"]";
//                List ll = new ArrayList<>();
//                ll.addAll((List) actual);
//                boolean addedNw = saveListstate(idL, idLista, ll);
//                if(ll.size()!=0){
//                    boolean rValue = false;
//                    for(Object litem:ll){
//                        String idItem = searchLeafInList(litem, idLista);
//                        String idToPass = complete.substring(0,complete.length()-fremaining.length()-idLista.length()-2)+idItem+"]."+fremaining;
//                        boolean fitem = searchLeaf(litem, fremaining, idToPass);
//                        if(addedNw && fitem){
//                            NotifyMsg e = new NotifyMsg();
//                            e.act=action.ADDED;
//                            e.obj=litem;
//                            e.var = idToPass.substring(0, idToPass.length()-fremaining.length()-1);
//                            writeLock.lock();
//                            whatHappened.add(e);
//                            writeLock.unlock();
//                        }
//                        rValue=rValue||fitem;
//                    }
//                    return rValue;
//                }else
//                    return false;
//            }else
//                return searchLeaf(actual, fremaining, complete);
//        }else{
//            if(!state.containsKey(complete)){
//                NotifyMsg e = new NotifyMsg();
//                e.act=action.ADDED;
//                e.var=trasformInPrint(complete);
//                e.obj=actual;
//            }
//            state.put(complete, actual);
//            return true;
//        }
//    }
//    //copies the actual value of a list in the stateList
//    private boolean saveListstate(String key, String idLista, List ll) {
//        ////System.out.println("in saveListstate lists: " + stateList);
//        ListValues toRem = null;
//        if(ll==null){
//            stateList.remove(key);
//            //add in nullValuesToListen
//            String gen = allGeneralIndexes(key);
//            for(String s:toListen){
//                if(s.contains(gen) && s.substring(0, gen.length()).equals(gen)){
//                    try {
//                        boolean present = searchLeaf(root, s, s);
//                        if(!present)
//                            nullValuesToListen.add(s);
//                    } catch (NoSuchFieldException ex) {
//                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IllegalArgumentException ex) {
//                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IllegalAccessException ex) {
//                        Logger.getLogger(StateListenerNew.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                    
//            }
//            ////System.out.println("Dopo lista nulla in saveListstate "+stateList);
//            return true;
//        }
//        List nl = new LinkedList<>();
//        for(int i=0;i<ll.size();i++){
//            nl.add(ll.get(i));
//        }
//        if(stateList.containsKey(key)){
//            stateList.get(key).List = nl;
//            return false;}
//        stateList.put(key, new ListValues(idLista, nl));
//        ////System.out.println("Alla fine ho aggiunto qualcosa "+stateList);
//        return true;
//    }

}
