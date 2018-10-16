package ae;

import javax.swing.*;
import Message.*;
import Utility.*;
import java.io.*;
import java.net.*;

class MyDseThread {


    JTextField ipAddress;
    JTextField bandwidth;
    JTextField dseipAddress;
    JFormattedTextField dseport;
    JButton dseConnection;

    boolean dseConnected = false;

    ObjectOutputStream oos;

     Socket dseSocket = null;


    public MyDseThread() {
    }

    public boolean isConnected() {
        return dseConnected;
    }


    synchronized public void connectDse() {
        try {

            dseSocket = new Socket("127.0.0.1", 4048);

            dseSocket.setKeepAlive(true);

            dseConnected = true;

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
        dseConnected = false;
        dseConnection.setText("Problems occurred");


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



  synchronized public AEMessage sendRequest(AEMessage msg) {
      
      
      System.out.println("we are in the send request");
      
      XMLUtility.getInstance().printXML(msg.getValue());
      
      try {
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
      
      
      /*
        try {


            msg.setSrcHost(InetAddress.getLocalHost().getHostName());

            ObjectOutputStream oos = new ObjectOutputStream(dseSocket.getOutputStream());//a modifier
            oos.flush();
            oos.writeObject(msg);//a modifier

           oos.flush();



            ObjectInputStream ois = new ObjectInputStream(dseSocket.getInputStream());//a modifier
            msg = (AEMessage) ois.readObject();//a modifier
            System.out.println("Message Received from DSE");


           // ois.close();

            System.out.println(msg.getValue());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;

*/
    }







}

