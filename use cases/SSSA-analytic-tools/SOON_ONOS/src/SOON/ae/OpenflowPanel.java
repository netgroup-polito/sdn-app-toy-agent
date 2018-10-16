package ae;


import Message.*;
import Utility.*;
import static ae.RequestGenerator.serviceTime;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.*;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.SEVERE;

public class OpenflowPanel extends JPanel {

    private JCheckBox bidirectional;
    private ApplicationEntity aeContainer;
    // private JFormattedTextField srcIp;
    private JTextField srcSw;
    private JTextField dstSw;
    private JTextField srcIp;
    private JTextField dstIp;
    private JTextField txtTest;
    private JTextField txtSLA;
    
        
    private int reqID = 1;

    // private JFormattedTextField dstIp;
    private JFormattedTextField bandwidth;
    private JTextField lspName;
    private JTextArea createRequestXML;
    private JTextArea createResponseXML;

    private JComboBox<String> comboClass;
    String cls;
    private JTextField msgtoFloodlight;
    private org.w3c.dom.Document requestSpec;

    public static int numRequests = 10;
    int MIN = 1;
    int MAX = 11;
  
    

    OpenflowPanel(ApplicationEntity ae) {
        aeContainer = ae;
        //requestSpec = XMLUtility.getInstance().loadXML("serviceRequest.xml");

        setLayout(null);

        MaskFormatter ipMask = null;
        MaskFormatter bandwidthMask = null;

        try {
            ipMask = new MaskFormatter("###.###.###.###");
            ipMask.setPlaceholderCharacter('0');
            bandwidthMask = new MaskFormatter("####");
            bandwidthMask.setPlaceholderCharacter('0');
        } catch (Exception e) {
            e.printStackTrace();
        }
      
        JLabel srcIpText = new JLabel("IP Source");

        srcIpText.setBounds(30, 20, 100, 15);

        add(srcIpText);

        srcIp = new JTextField();
        srcIp.setText("10.0.1.1");
  
        srcIp.setBounds(100, 20, 120, 20);

        add(srcIp);

        JLabel dstIpText = new JLabel("IP dest");

        dstIpText.setBounds(30, 50, 100, 15);

        add(dstIpText);

        dstIp = new JTextField();
        dstIp.setBounds(100, 50, 120, 20);
        dstIp.setText("10.0.11.1");

        add(dstIp);

        JLabel classText = new JLabel("Class");
        classText.setBounds(230, 20, 60, 15);
        add(classText);

        comboClass = new JComboBox<String>();
        comboClass.addItem("a");
        comboClass.addItem("b");
        comboClass.addItem("c");
        comboClass.setBounds(280, 20, 80, 20);
        comboClass.setEditable(true);

        add(comboClass);

        JLabel lblTest = new JLabel("Tests");
        lblTest.setBounds(230, 60, 60, 15);
        add(lblTest);
        txtTest = new JTextField();
        txtTest.setBounds(280, 60, 60, 20);
        txtTest.setText("1");
        add(txtTest);
        
        JLabel lblSLA = new JLabel("SLA");
        lblSLA.setBounds(230, 80, 60, 15);
        add(lblSLA);
        txtSLA = new JTextField();
        txtSLA.setBounds(280, 80, 60, 20);
        txtSLA.setText("0");
        add(txtSLA);

        JButton clear = new JButton("Clear");
        clear.setBounds(50, 80, 100, 20);
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                createRequestXML.setText("");
                createResponseXML.setText("");
            }
        });
        add(clear);

        JButton setupFlow = new JButton("Setup Simple Path");

        setupFlow.setBounds(395, 20, 195, 20);

        setupFlow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (aeContainer.isConnected()) {
                    cls = (String) comboClass.getSelectedItem();
                    org.w3c.dom.Document doc = createXMLRequest("FlowSetupRequest", reqID, 10000, false);
                    reqID = reqID + 1;
                    AEMessage msg = new AEMessage(doc, "");
                    createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                    //msg.setValue(doc);
                    XMLUtility.getInstance().printXML(doc);
                    msg = aeContainer.sendRequest(msg);

                    createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));
                }
            }
        });
        add(setupFlow);

        JButton service = new JButton("Setup Composite Path");
        service.setBounds(395, 60, 195, 20); //setBounds(400, 100, 130, 20)
        service.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (aeContainer.isConnected()) {
                    cls = (String) comboClass.getSelectedItem();
                    org.w3c.dom.Document doc = createXMLRequest("ServiceDeliveryRequest", reqID, 10000000, false);
                    reqID = reqID + 1;
                    AEMessage msg = new AEMessage(doc, "");
                    createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                    XMLUtility.getInstance().printXML(doc);
                    msg = aeContainer.sendRequest(msg);

                    createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));
                }
            }
        });
        add(service);

        JButton delete = new JButton("Delete Path");
        delete.setBounds(395, 100, 195, 20);
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (aeContainer.isConnected()) {
                    cls = (String) comboClass.getSelectedItem();
                    org.w3c.dom.Document doc = createXMLRequest("ServiceCancellationRequest", 1, 0, false);
                    AEMessage msg = new AEMessage(doc, "");
                    createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                    XMLUtility.getInstance().printXML(doc);
                    msg = aeContainer.sendRequest(msg);

                    createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));
                }
            }
        });
        add(delete);

        JButton multiplePath = new JButton("Setup Multiple Path");
        multiplePath.setBounds(190, 110, 195, 20); 
        multiplePath.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (aeContainer.isConnected()) {
  
                   
                    int no_of_tests = Integer.parseInt(txtTest.getText());
                    //NodeList nodelist = requestSpec.getElementsByTagName("paths");
                    try {
                        boolean flag = false;
                        for (int t = 0; t < no_of_tests; t++) {
                            FileWriter writer1;
                            FileWriter writer2;
                            writer1 = new FileWriter("results/latency", true);
                            writer2 = new FileWriter("results/flowSetupTime", true);
                            BufferedWriter w = new BufferedWriter(writer1);
                            BufferedWriter w2 = new BufferedWriter(writer2);

                            w.append("Test " + String.valueOf(t + 1));
                            w.newLine();
                            w2.append("Test " + String.valueOf(t + 1));
                            w2.newLine();
                            w.flush();
                            w2.flush();
                            w.close();
                            w2.close();

                            flag = true;
                            int src_ip_decimal, dst_ip_decimal;
                            String src_ip = "", dst_ip = "";
                            //HashMap<Integer, Integer> ip_pair = new HashMap<Integer, Integer>();
                            ArrayList<String> srcIPs = new ArrayList<String>();
                            ArrayList<String> dstIPs = new ArrayList<String>();
                            RequestGenerator.GenerateRequests(numRequests);
                            
                            boolean request_exists;
                            
                            double intArrivTime, serviceTime;
                            double[] arrivalTimes = RequestGenerator.getArrivalTime();
                            double[] serviceTimes = RequestGenerator.getServiceTime();
                            for (int i = 0; i < numRequests; i++) {
                                
                                    do {
                                    request_exists = false;
                                    do {
                                        src_ip_decimal = MIN + (int) (Math.random() * ((MAX - MIN) + 1));
                                        dst_ip_decimal = MIN + (int) (Math.random() * ((MAX - MIN) + 1));
                                    } while (src_ip_decimal == dst_ip_decimal);
                                    src_ip = "10.0." + src_ip_decimal + ".1";
                                    dst_ip = "10.0." + dst_ip_decimal + ".1";
                                   // System.out.println("preparing to send request from "+ src_ip + " to " + dst_ip);

                                    for (int ind = 0; ind < srcIPs.size(); ind++) {
                                        if (srcIPs.get(ind).equals(src_ip)
                                                && dstIPs.get(ind).equals(dst_ip)) {
                                            request_exists = true;
                                            break;
                                        }
                                    }
                                } while (request_exists);

                                srcIPs.add(src_ip);
                                dstIPs.add(dst_ip);
                                
                              //  System.out.println("Sending request number " + (i + 1));
                                
                                serviceTime = serviceTimes[i + 1];
                                //org.w3c.dom.Element servReq = (org.w3c.dom.Element) r.item(i);
                                //srcIp.setText(servReq.getAttribute("srcIp"));
                                //dstIp.setText(servReq.getAttribute("dstIp"));
                                srcIp.setText(src_ip);
                                dstIp.setText(dst_ip);
                                org.w3c.dom.Document doc = createXMLRequest("ServiceDeliveryRequest", (i + 1), serviceTime, flag);
                                AEMessage msg = new AEMessage(doc, "");
                                createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                                
                                ApplicationEntity.Requests.add(doc);
                                
                               // XMLUtility.getInstance().printXML(doc);
                                //msg = aeContainer.sendRequest(msg);
                      
                                
                                //System.out.println("New request sent: intArrivTime=" + intArrivTime + " serviceTime=" + serviceTime);
                                createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));

                                if (flag == true) {
                                    flag = false;
                                }
                             
                                                            
                               org.w3c.dom.Document docCancel = createXMLRequest("ServiceCancellationRequest", (i + 1), serviceTime, flag);
                               AEMessage msgCancel = new AEMessage(docCancel, "");
                               createRequestXML.setText(XMLUtility.getInstance().toString(docCancel));
                              
                               ApplicationEntity.Releases.add(docCancel);
                                
                               
                               /*
                                
                                boolean request_exists;
                                
                                if (i == 0) {//setup path for testing purpose
                                    src_ip = "10.0.14.1";
                                    dst_ip = "10.0.15.1";

                                    srcIp.setText(src_ip);
                                    dstIp.setText(dst_ip);
                                    serviceTime = 50;
                                    org.w3c.dom.Document doc = createXMLRequest("ServiceDeliveryRequest", 0, serviceTime, flag);
                                    AEMessage msg = new AEMessage(doc, "");
                                    createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                                    XMLUtility.getInstance().printXML(doc);
                                    msg = aeContainer.sendRequest(msg);
                                    System.out.println("Request is sent to setup test path from " + src_ip + " to " + dst_ip);
                                    createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));
                                    
                                    org.w3c.dom.Document docCancel = createXMLRequest("ServiceCancellationRequest", 0, serviceTime, flag);
                                    AEMessage msgCancel = new AEMessage(docCancel, "");
                                    createRequestXML.setText(XMLUtility.getInstance().toString(docCancel));
                                    XMLUtility.getInstance().printXML(docCancel);
                                    //msgCancel = aeContainer.sendRequestTime(msgCancel, serviceTime);

                     
                                }
                                do {
                                    request_exists = false;
                                    do {
                                        src_ip_decimal = MIN + (int) (Math.random() * ((MAX - MIN) + 1));
                                        dst_ip_decimal = MIN + (int) (Math.random() * ((MAX - MIN) + 1));
                                    } while (src_ip_decimal == dst_ip_decimal);
                                    src_ip = "10.0." + src_ip_decimal + ".1";
                                    dst_ip = "10.0." + dst_ip_decimal + ".1";
                                    System.out.println("preparing to send request from "
                                            + src_ip + " to " + dst_ip);

                                    for (int ind = 0; ind < srcIPs.size(); ind++) {
                                        if (srcIPs.get(ind).equals(src_ip)
                                                && dstIPs.get(ind).equals(dst_ip)) {
                                            request_exists = true;
                                            break;
                                        }
                                    }
                                } while (request_exists);

                                srcIPs.add(src_ip);
                                dstIPs.add(dst_ip);

                                //ip_pair.put(src_ip_decimal, dst_ip_decimal);
                                //org.w3c.dom.Element req = (org.w3c.dom.Element) nodelist.item(j);
                                //NodeList r = req.getElementsByTagName("path");
                                //numRequests = r.getLength();
                                //for (int i = 0; i < numRequests; i++) {
                                if (i == 0) {
                                    intArrivTime = 10;
                                } else {
                                    //intArrivTime = RequestGenerator.getArrivalTime()[i + 1] - RequestGenerator.getArrivalTime()[i];
                                    intArrivTime = arrivalTimes[i + 1] - arrivalTimes[i];
                                }
                                //serviceTime = RequestGenerator.getServiceTime()[i + 1];
                                serviceTime = serviceTimes[i + 1];
                                
                                
                                org.w3c.dom.Document docCancel = createXMLRequest("ServiceCancellationRequest", (i + 1), serviceTime, flag);
                                AEMessage msgCancel = new AEMessage(docCancel, "");
                                createRequestXML.setText(XMLUtility.getInstance().toString(docCancel));
                                //XMLUtility.getInstance().printXML(docCancel);
                               
                                msgCancel = aeContainer.sendRequest(msgCancel);
                            
                                
                              //  Thread.sleep((long) intArrivTime * 1000);
         
                                System.out.println("Sending request number " + (i + 1));
                                

                                //org.w3c.dom.Element servReq = (org.w3c.dom.Element) r.item(i);
                                //srcIp.setText(servReq.getAttribute("srcIp"));
                                //dstIp.setText(servReq.getAttribute("dstIp"));
                                srcIp.setText(src_ip);
                                dstIp.setText(dst_ip);
                                org.w3c.dom.Document doc = createXMLRequest("ServiceDeliveryRequest", (i + 1), serviceTime, flag);
                                AEMessage msg = new AEMessage(doc, "");
                                createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                                XMLUtility.getInstance().printXML(doc);
                                msg = aeContainer.sendRequest(msg);
                      
                                
                                //System.out.println("New request sent: intArrivTime=" + intArrivTime + " serviceTime=" + serviceTime);
                                createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));

                                if (flag == true) {
                                    flag = false;
                                }
                                //}
                                
                                
                                */
                            }
                            
                            int k = 0;
                            
                            ScheduledExecutorService execReq = Executors.newSingleThreadScheduledExecutor();
                            execReq.scheduleAtFixedRate(new SendRequests(),0, 50, TimeUnit.SECONDS);

                              
                            ScheduledExecutorService execDel = Executors.newSingleThreadScheduledExecutor();
                            execDel.scheduleWithFixedDelay(new SendDeleteRequests(), 260, 50, TimeUnit.SECONDS);//50 = IAT value //30 = service time value
                            
                            k++;
                        }

                            
                            
                    } catch (IOException ex) {
                       // Logger.getLogger(OpenflowPanel.class.getName()).log(Level.w SEVERE, null, ex);
                    }
                   
                       
                }
            }
        });
        add(multiplePath);

        JButton stats = new JButton("Statistics");
        stats.setEnabled(false);
        stats.setBounds(395, 130, 195, 20); //395, 100, 195, 20
        stats.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (aeContainer.isConnected()) {
                    org.w3c.dom.Document doc = createXMLRequest();
                    AEMessage msg = new AEMessage(doc, "");
                    createRequestXML.setText(XMLUtility.getInstance().toString(doc));
                    //msg.setValue(doc);
                    XMLUtility.getInstance().printXML(doc);
                    msg = aeContainer.sendRequest(msg);
                    System.out.println("Statistics Request sent");

                    createResponseXML.setText(XMLUtility.getInstance().toString(msg.getValue()));
                }
            }
        });
        add(stats);

        JLabel lspRq = new JLabel("XML Delivery Path Setup request");
        lspRq.setBounds(30, 150, 250, 15);
        JLabel lspRs = new JLabel("XML Delivery Path Setup response");
        lspRs.setBounds(330, 150, 250, 15);
        add(lspRq);
        add(lspRs);

        createRequestXML = new JTextArea("");
        createRequestXML.setEditable(false);
        JScrollPane scrollRq = new JScrollPane(createRequestXML);
        scrollRq.setBounds(10, 170, 280, 230);
        createResponseXML = new JTextArea("");
        createResponseXML.setEditable(false);
        JScrollPane scrollRs = new JScrollPane(createResponseXML);
        scrollRs.setBounds(300, 170, 280, 230);

        add(scrollRq);
        add(scrollRs);

    }

    /**
     *
     */
    private org.w3c.dom.Document createXMLRequest2() {
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();

        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement("FlowSetupRequest");
            doc.appendChild(root);

            org.w3c.dom.Element ae = doc.createElement("ApplicationEntity");
            ae.setAttribute("name", aeName);
            ae.setAttribute("ipaddress", aeAddress);
            root.appendChild(ae);

            org.w3c.dom.Element srcIP = doc.createElement("srcSw");
            srcIP.appendChild(doc.createTextNode(srcSw.getText()));
            org.w3c.dom.Element dstIP = doc.createElement("dstSw");
            dstIP.appendChild(doc.createTextNode(dstSw.getText()));

            root.appendChild(srcIP);
            root.appendChild(dstIP);

        } catch (Exception e) {
            System.out.println(e);
        }
        return doc;
    }

    private org.w3c.dom.Document createXMLRequest3() {
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();

        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement("DeleteFlowRequest");
            doc.appendChild(root);

            org.w3c.dom.Element ae = doc.createElement("ApplicationEntity");
            ae.setAttribute("name", aeName);
            ae.setAttribute("ipaddress", aeAddress);
            root.appendChild(ae);

            org.w3c.dom.Element srcIP = doc.createElement("srcSw");
            srcIP.appendChild(doc.createTextNode(srcSw.getText()));
            org.w3c.dom.Element dstIP = doc.createElement("dstSw");
            dstIP.appendChild(doc.createTextNode(dstSw.getText()));

            root.appendChild(srcIP);
            root.appendChild(dstIP);

        } catch (Exception e) {
            System.out.println(e);
        }
        return doc;
    }

    private org.w3c.dom.Document createXMLRequest(String request, int requestID, double serviceTime, boolean testFlag) {
        
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();

        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement(request);
            /*
             if (request.equals("ServiceDeliveryRequest")) {
             root.setAttribute("pathType", "composite");
             } else {
             root.setAttribute("pathType", "simple");
             }*/
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
            
            org.w3c.dom.Element SLAvalue = doc.createElement("SLA");
            SLAvalue.setTextContent(txtSLA.getText());
            root.appendChild(SLAvalue);

            org.w3c.dom.Element servTime = doc.createElement("serviceTime");
            servTime.setTextContent(String.valueOf(serviceTime));
            root.appendChild(servTime);

            org.w3c.dom.Element iter = doc.createElement("iteration");
            iter.setTextContent("0");//initially 0 for every request
            root.appendChild(iter);

            org.w3c.dom.Element srcNode = doc.createElement("srcNode");
            //srcNode.setAttribute("switch", srcSw.getText());
            //srcNode.setAttribute("port", "1");
            srcNode.setAttribute("ip", srcIp.getText());// "192.168.200.1");
            root.appendChild(srcNode);

            org.w3c.dom.Element dstNode = doc.createElement("dstNode");
            //dstNode.setAttribute("switch", dstSw.getText());
            //dstNode.setAttribute("port", "1");
            dstNode.setAttribute("ip", dstIp.getText());// "192.168.200.2");
            root.appendChild(dstNode);

            org.w3c.dom.Element intermSrcNode = doc.createElement("intermSrcNode");
            //intermSrcNode.setAttribute("switch", srcSw.getText());
            intermSrcNode.setAttribute("port", "1");
            intermSrcNode.setAttribute("ip", srcIp.getText());// "192.168.200.1");
            root.appendChild(intermSrcNode);

            org.w3c.dom.Element intermDstNode = doc.createElement("intermDstNode");
            //intermDstNode.setAttribute("switch", "5");
            //intermDstNode.setAttribute("switch", dstSw.getText());
            intermDstNode.setAttribute("port", "1");
            intermDstNode.setAttribute("ip", dstIp.getText());// "192.168.200.2");
            root.appendChild(intermDstNode);

        } catch (Exception e) {
            System.out.println(e);
        }
        return doc;
    }

    /**
     *
     */
    private org.w3c.dom.Document createXMLRequest() {
        org.w3c.dom.Document doc = XMLUtility.getInstance().createDocument();

        System.out.println("inside createXMLRequest");
        try {
            String aeName = InetAddress.getLocalHost().getHostName();
            String aeAddress = InetAddress.getLocalHost().getHostAddress();

            org.w3c.dom.Element root = doc.createElement("StatisticsRequest");
            doc.appendChild(root);

            org.w3c.dom.Element ae = doc.createElement("ApplicationEntity");
            ae.setAttribute("name", aeName);
            ae.setAttribute("ipaddress", aeAddress);
            root.appendChild(ae);

        } catch (Exception e) {
            System.out.println(e);
        }
        return doc;
    }

}

   /*************************************************************************************************************************/
   /*************************************************************************************************************************/

/*************************************************************************************************************************/
    
     class SendDeleteRequests implements Runnable {

         int index = 0;
         private ApplicationEntity aeContainer;
         
        @Override
        public void run() {

            System.out.println("Send CANCELLATION Request");
            
            org.w3c.dom.Document doc = ApplicationEntity.Releases.get(index);
            
            XMLUtility.getInstance().printXML(doc);
            
            AEMessage msg = new AEMessage(doc, "");
            
                        
            try {
                msg.setSrcHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException ex) {
                Logger.getLogger(SendRequests.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
               
               // System.out.println("aeContainer.aeSocket.isConnected(): "+aeContainer.aeSocket.isConnected());
           
            ApplicationEntity.oos.writeObject(msg);//a modifier

            ApplicationEntity.oos.flush();
           
                System.out.println("Here del: " + InetAddress.getLocalHost().getHostName());
               
               //  ApplicationEntity.oos.close();

        } catch (IOException ex) {
            System.out.println("Exception case - new thread (AE)");
            ex.printStackTrace();
        }
                        
            index = index + 1;
            
            

                }//run
    }  
  
   /************************************************************************************************************************/

   /*************************************************************************************************************************/
    
     class SendRequests implements Runnable {

        int index = 0;
        private ApplicationEntity aeContainer;
        
        @Override
        public void run() {
            
            System.out.println("Send Request");
            
            
            
            org.w3c.dom.Document doc = ApplicationEntity.Requests.get(index);
            
            XMLUtility.getInstance().printXML(doc);
            
            index = index + 1;
                        
                        
            AEMessage msg = new AEMessage(doc, "");
            
            try {
                msg.setSrcHost(InetAddress.getLocalHost().getHostName());
                 System.out.println("ae set address");
            
               //System.out.println("aeContainer.aeSocket.isConnected(): "+aeContainer.aeSocket.isConnected());
                
                ApplicationEntity.oos.writeObject(msg);//a modifier

            
           
                System.out.println("Here req: " + InetAddress.getLocalHost().getHostName());
                
                ApplicationEntity.oos.flush();
                
                ApplicationEntity.oos.reset();
           

        } catch (IOException ex) {
            System.out.println("Exception case (AE)");
            ex.printStackTrace();
        }

                }//run
    }  
  
   /************************************************************************************************************************/