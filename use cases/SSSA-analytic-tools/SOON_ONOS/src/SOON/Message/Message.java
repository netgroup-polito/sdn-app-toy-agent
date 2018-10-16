package Message;

import DSE.Path;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class Message implements Serializable {

    protected int type = 0;
    protected int srcPort;
    protected String srcHost;
    protected int dstPort;
    protected String dstHost;
    protected String sessionID;

    final static public int AE_MSG = 1;
    final static public int CSE_MSG = 2;
    final static public int DSE_MSG = 3;
    final static public int BNS_MSG = 4;
    final static public int FL_MSG = 5;

    static public String myMess = "Hi";
   
    static public  List<ArrayList<Path>> all_paths = Collections.synchronizedList(new ArrayList<ArrayList<Path>>());//all bidirection paths for all requests

    public static synchronized String getMyMess() {
        return myMess;
    }

    public List<ArrayList<Path>> getAllPaths() {
        //synchronized (all_paths) {
            System.out.println("returning all_paths with " + all_paths.size() + " elements");
            return all_paths;
       // }

    }

    public void addPath(ArrayList<Path> path) {
        all_paths.add(path);
        System.out.println("Path added. all_paths has " + all_paths.size() + " elements");
    }

    public void setAllPaths(ArrayList<ArrayList<Path>> paths) {
        //synchronized (all_paths) {
            all_paths = paths;
        //}
    }

    public int getType() {
        return type;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int p) {
        srcPort = p;
    }

    public String getSrcHost() {
        return srcHost;
    }

    public void setSrcHost(String h) {
        srcHost = h;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int p) {
        dstPort = p;
    }

    public String getDstHost() {
        return dstHost;
    }

    public void setDstHost(String h) {
        dstHost = h;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String s) {
        sessionID = s;
    }

    abstract public String getCommand();
}
