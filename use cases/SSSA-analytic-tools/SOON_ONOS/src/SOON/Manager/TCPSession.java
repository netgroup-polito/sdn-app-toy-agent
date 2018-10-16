package Manager;

import DSE.*;
import Message.*;
import DSE.BNServer;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

public class TCPSession extends Thread {

    private Socket socket;
    private AEMessage msg;
    private BNSMessage bnsmsg;
    private TCPManager manager;
    private String sessionID;
    private DSE dseContainer;
    ObjectInputStream ois;

    public TCPSession(DSE con, TCPManager m, Socket s, String ses) throws IOException {
        socket = s;
        manager = m;
        sessionID = ses;
        dseContainer = con;
        ois = new ObjectInputStream(socket.getInputStream());
       
    }

    public void run() {

        System.out.println("TCPSession");
        try { 
            while (true) {
                
                synchronized (this) {
                
                Object o = ois.readObject();
                
                System.out.println("while true: tcp session");
                
               
                if (o instanceof ConcurrentHashMap) {
                    
                    
                    ConcurrentHashMap<Long, BNServer.TupleStatistics<ArrayList<String>, Long, Long>> statsModel
                            = (ConcurrentHashMap<Long, BNServer.TupleStatistics<ArrayList<String>, Long, Long>>) o;
                   
                    manager.receiveStatisticsFromBNS(statsModel);
                } 

                else if (o instanceof AEMessage) {
                    msg = (AEMessage) o;
                    sessionID = msg.getSessionID();
                    manager.receiveMsg(msg);
                }
                
                else if (o instanceof Document) {
                   
                    manager.receiveStatisticsPerFlow((Document)o);
                }
                
                else {
                   System.out.println("unexpected or NULL message received (TCPSession)");
                }
                
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException ee) {
            System.out.println("exception case (TCPSession)");
            ee.printStackTrace();
            try {
                this.sleep(10000000);
            } catch (Exception t) {
            }

        } catch (Exception ex) {
            Logger.getLogger(TCPSession.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public synchronized void sendMessage(Message msg) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(msg);
            oos.flush();

            } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSessionID() {
        return sessionID;
    }
}
