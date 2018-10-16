package ae;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;
import Message.*;
import Scheduler.Scheduler;
import Utility.*;
import java.util.ArrayList;

import org.w3c.dom.Document;

public class ApplicationEntity extends JFrame {

    JTextField ipAddress;
    JTextField bandwidth;
    JTextField dseipAddress;
    JFormattedTextField dseport;
    JButton dseConnection;
    static Socket dseSocket = null;
    boolean dseConnected = false;
    org.w3c.dom.Document aeRequestXML = null;
    org.w3c.dom.Document responseRequestXML = null;
    Socket aeSocket;
    ServerSocket ss;
    public volatile Document requestDB;
   
    static ObjectOutputStream oos;
    
      
    static ArrayList<org.w3c.dom.Document> Requests = new ArrayList<>();
    static ArrayList<org.w3c.dom.Document> Releases = new ArrayList<>();
    
    
    public ApplicationEntity() {

        super("Application Entity (AE)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(630, 550);
        setResizable(false);
        JPanel pnl = new JPanel();
        pnl.setLayout(null);
        pnl.setBackground(Color.WHITE);
        
        JTabbedPane tabbedPanel = new JTabbedPane();
        tabbedPanel.addTab("OF Service Delivery Path Setup", new OpenflowPanel(this));
       
        tabbedPanel.setBounds(10, 100, 600, 400);
        JPanel dsePnl = getDSEpanel();
        dsePnl.setBounds(174, 40, 530, 50);
        pnl.add(dsePnl);
        pnl.add(tabbedPanel);
        setContentPane(pnl);
        show();

        Config conf = Config.getInstance();
        String tcpHost = conf.getString("AEAddress");
        System.out.println("the address is" + tcpHost);
        int tcpPort = conf.getInt("AEXmlPort");
        System.out.println("the port is" + tcpPort);
        String client = "";
   
        
               
            /*********************************************************************/
            /*********************Unify Integration*******************************/
            //connectDse();
            
            /******************open a REST API*******************************************/
            
            
            
            /***************open socket with external addresses**************************
            
               try {
            ss = new ServerSocket(tcpPort);
            //Socket aeSocket;
            System.out.println("listening on the port" + tcpPort);
            //org.w3c.dom.Document xmlRequest;
            
            int cont = 1;

            aeSocket = new Socket();

            while (true) {

                aeSocket = ss.accept();

                long start = System.currentTimeMillis();

                System.out.println("time socket accepted & thread created" + start + " with ID " + cont);

                System.out.println("socket accepted");

                System.out.println("the socket ID is " + cont);

                MyServerThread mythread = new MyServerThread(aeSocket, cont);//, dseSocket, cont);

                mythread.start();

                cont++;

                long variab = mythread.getId();

                System.out.println("ID by getId() " + variab);

            }//while true

        } catch (Exception e) {
            e.printStackTrace();

        }
            
            /***************************************************************************
           
            NorthBoundInterface nbI = new NorthBoundInterface();
            
            ArrayList<org.w3c.dom.Document> documents = nbI.mappingParameters();
                   
            for(int i=0; i<documents.size(); i++)
            {

            XMLUtility.getInstance().printXML(documents.get(i));
            
            AEMessage msg = new AEMessage(documents.get(i), "");
            //msg.setValue(doc);
        
            msg = sendRequest(msg);
            
            }
            
                 
           /*********************************************************************/


    }

    public class AppendingObjectOutputStream extends ObjectOutputStream {

        public AppendingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            // do not write a header, but reset:
            // this line added after another question
            // showed a problem with the original
            reset();
        }

    }

    /* public boolean writeToSocket(BufferedWriter socket, String request) {

     try {
     System.out.println("writing to socket");
     socket.write(request);

     //socket.flush();
     //socket.close();
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



     /* public org.w3c.dom.Document readFromSocket() {
     Config conf = Config.getInstance();
     String tcpHost = conf.getString("AEAddress");
     System.out.println("the address is" + tcpHost);
     int tcpPort = conf.getInt("AEXmlPort");
     System.out.println("the port is" + tcpPort);
     String client = "";

     try {
     ss = new ServerSocket(tcpPort);
     System.out.println("listening on the port" + tcpPort);
     org.w3c.dom.Document xmlRequest;
     while (true) {
     aeSocket = ss.accept();


     System.out.println("aperto un socket sulla porta locale " + aeSocket.getLocalPort() + " alla porta remota " + aeSocket.getPort() + " dell'Host " + (aeSocket.getInetAddress()).getHostName());
     // informazioni sul Client che ha effettuato la chiamata
     InetAddress address = aeSocket.getInetAddress();
     client = address.getHostName();
     int porta = aeSocket.getPort();
     System.out.println("In chiamata Client: " + client + "porta:" + porta);
     //dseContainer.isConnected(client, true);
     String hostAddress = InetAddress.getLocalHost().getHostAddress();
     StringTokenizer str = new StringTokenizer(hostAddress, ".");
     hostAddress = str.nextToken() + str.nextToken() + str.nextToken() + str.nextToken();
     xmlRequest = readFromSocketToXmlDoc();
     //aeSocket.close();
     System.out.println("the received doc is");
     XMLUtility.getInstance().printXML(xmlRequest);
     return xmlRequest;
     }

     } catch (Exception e) {
     e.printStackTrace();
     return null;
     }
     }

     */
    public boolean isConnected() {
        return dseConnected;
    }

    private JPanel getDSEpanel() {
        JPanel pnl = new JPanel();
        pnl.setBackground(Color.white);
        MaskFormatter portMask = null;
        try {
            portMask = new MaskFormatter("####");
            portMask.setPlaceholderCharacter('0');
        } catch (Exception e) {
            e.printStackTrace();
        }
        //dseipAddress = new JTextField(10);
        dseipAddress = new JTextField("localhost");
        pnl.add(new JLabel("SE Address"));
        // pnl.add(dseipAddress);
        pnl.add(dseipAddress);
        //dseport = new JFormattedTextField(portMask);
        dseport = new JFormattedTextField("4048");
        dseport.setColumns(4);
        pnl.add(new JLabel("Port"));
        pnl.add(dseport);
        dseConnection = new JButton("Connect");
        dseConnection.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (dseConnected) {
                    disconnectDse();
                } else {
                    connectDse();
                }
            }
        });

        pnl.add(dseConnection);
        return pnl;
    }

    private void connectDse() {
        try {
            dseSocket = new Socket((String) (dseipAddress.getText()), Integer.parseInt((String) (dseport.getValue())));
            dseSocket.setKeepAlive(true);
            dseConnected = true;
            dseConnection.setText("Connected");
            System.out.println("connected to DSE");
            oos = new ObjectOutputStream(dseSocket.getOutputStream());//a modifier
        } catch (Exception e) {
            System.out.println("Error! Connection to DSE not available");
            e.printStackTrace();
            dseConnected = false;
            dseConnection.setText("Notconnected");
        }
    }

    private void disconnectDse() {
        try {
            dseSocket.close();
        } catch (Exception e) {
            System.out.println("Impossible to close the DSE connection");
        }
        dseConnected = true;
        dseConnection.setText("Problems occurred");
    }


    /* private org.w3c.dom.Document createAEResponse(String value) {
     org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();
     org.w3c.dom.Element root = doc.createElement("ServiceRequestResponse");
     doc.appendChild(root);
     org.w3c.dom.Element resp = doc.createElement("Response");
     resp.appendChild(doc.createTextNode(value));
     root.appendChild(resp);
     return doc;
     }
     */
    public AEMessage sendRequest(AEMessage msg) {
        
        try {
            
            System.out.println("we are in send request method");
            
            msg.setSrcHost(InetAddress.getLocalHost().getHostName());

            //ObjectOutputStream oos = new ObjectOutputStream(dseSocket.getOutputStream());//a modifier
            oos.writeObject(msg);//a modifier
            //oos.reset();
            oos.flush();
            oos = new AppendingObjectOutputStream(dseSocket.getOutputStream());//a modifier

            System.out.println("Here: " + InetAddress.getLocalHost().getHostName());

            /*
             ObjectInputStream ois = new ObjectInputStream(dseSocket.getInputStream());//a modifier
             msg = (AEMessage) ois.readObject();//a modifier
             System.out.println("Message Received from DSE");
             */
            // ois.close();
            //System.out.println("Ahmedooo "+XMLUtility.getInstance().toString(msg.getValue()));
            //System.out.println(msg.getValue());
        } catch (IOException ex) {
            System.out.println("Exception case (AE)");
            ex.printStackTrace();
        }
        return msg;
    }

        
        
        
    public static void main(String arg[]) {

        new ApplicationEntity();
        
    
        
    }

}
