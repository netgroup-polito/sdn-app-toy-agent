package Manager;

import DSE.*;
import Message.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import org.slf4j.LoggerFactory;

public class UDPManager extends Thread implements Manager {

    private DSE dseContainer;
    private DatagramSocket socket = null;
    private int srcPort;
    private String srcHost;

    public static int count = 0;
    
    protected final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    public UDPManager(DSE dse, String host, int port) {
        try {
            srcHost = host;
            srcPort = port;
            dseContainer = dse;
            socket = new DatagramSocket(srcPort);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                byte[] recvBuf = new byte[500000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                
                System.out.println("we are in UDP manager");

                Object o;
                try (ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData()); ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream))) {
                    o = is.readObject();
                  //  is.close(); //??
                    byteStream.close();
                }
                Message msg;

                if (o instanceof Message) {
                    msg = (Message) o;
                    dseContainer.messageReceived(msg);
                } else {
                    System.out.println("unexpected or NULL message received (UDPManager)");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("exception case");
            }
        }
    }

    public void sendMessage(Message msg) {
        try {

            msg.setSrcHost(dseContainer.getHostName());
            msg.setSrcPort(srcPort);
            count++;
            InetAddress dstHost = InetAddress.getByName(msg.getDstHost());
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(500000);//5000000

            try (ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream))) {
                os.flush();
                os.writeObject(msg);
                os.flush();
                os.close();

                //retrieves byte array
                byte[] sendBuf = byteStream.toByteArray();

                DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, dstHost, msg.getDstPort());
                int byteCount = packet.getLength();
                socket.send(packet);
                byteStream.reset();
                byteStream.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("exception case in sendMessage (UDPManager)");
        }
    }
}
