package Manager;

import java.net.*;
import java.io.*;
import java.util.*;
import DSE.*;
import Message.*;
import Utility.*;
import DSE.BNServer;
import java.util.concurrent.ConcurrentHashMap;
import org.w3c.dom.Document;

public class TCPManager extends Thread implements Manager //****
{

    private DSE dseContainer;
    private String tcpHost;
    private int tcpPort;
    private Vector tcpSessions = new Vector();
    private int sessionID;
    private String client = "";

    private String session = "test";

    public TCPManager(DSE dse, String host, int port) {
        dseContainer = dse;
        tcpHost = host;
        tcpPort = port;
        sessionID = 1;
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(tcpPort);

            while (true) {

                Socket s = ss.accept();
                System.out.println("aperto un TCP socket sulla porta locale " + s.getLocalPort() + " alla porta remota " + s.getPort() + " dell'Host " + (s.getInetAddress()).getHostName());
             
                InetAddress address = s.getInetAddress();
                client = address.getHostName();
                              
                String hostAddress = InetAddress.getLocalHost().getHostAddress();

                StringTokenizer str = new StringTokenizer(hostAddress, ".");
                hostAddress = str.nextToken() + str.nextToken() + str.nextToken() + str.nextToken();
	
                TCPSession ses = new TCPSession(dseContainer, this, s, session.concat(String.valueOf(sessionID)));
                ses.start();

                tcpSessions.add(ses);

                sessionID++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message msg) {
        
        System.out.println("we are in send  MESSAGE - TCP manager: ");

        if (msg.getType() == 1) {
            System.out.println("Message sent dans TCPSession ");
            XMLUtility.getInstance().printXML(((AEMessage) msg).getValue());
        }

        String session = msg.getSessionID();
        System.out.println("TCPSession ID " + session + " Numero sessioni in memoria " + tcpSessions.size());

        TCPSession ses = null;
        for (int i = 0; i < tcpSessions.size(); i++) {
            ses = (TCPSession) (tcpSessions.elementAt(i));
            System.out.println("ses.getSessionID(): "+ses.getSessionID());
            System.out.println("session: "+session);
            
            if (ses.getSessionID().equals(session)) {
                ses.sendMessage(msg);
                break;
            } else {
                System.out.println("TCPSession non trovato!");
            }
        }

    }

    public void receiveMsg(AEMessage msg) throws IOException, InterruptedException {
        dseContainer.messageReceived(msg);
    }
    
    public void receiveMsg(BNSMessage msg) throws IOException, InterruptedException {
        dseContainer.messageReceived(msg);
    }

    public void receiveStatisticsFromBNS(ConcurrentHashMap<Long, BNServer.TupleStatistics<ArrayList<String>, Long, Long>> statsModel) throws IOException, InterruptedException {
        dseContainer.statisticsReceived(statsModel);
    }
    
    
    public void receiveStatisticsPerFlow(Document doc) throws IOException, InterruptedException, Exception {
        dseContainer.statisticsPerFlowReceived(doc);
    }
}
