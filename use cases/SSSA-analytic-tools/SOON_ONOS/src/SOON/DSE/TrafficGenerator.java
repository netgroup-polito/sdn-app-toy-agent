
package DSE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;


public class TrafficGenerator {

    private static Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    public TrafficGenerator(InetAddress address, int port) throws IOException {
        socket = new Socket(address, port);
        //System.out.println("hello");
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void send(String msg) throws IOException {
        
        if(!(socket.isClosed()))
        {
        System.out.println("traffic generator sends traffic");
        writer.write(msg, 0, msg.length());
        writer.flush();}
        else
        {
        socket = new Socket("127.0.0.1", 50000);
        System.out.println("RECONNECT then send traffic");
        writer.write(msg, 0, msg.length());
        writer.flush();
        
        }
        
    }

    public String recv() throws IOException {
        return reader.readLine();
    }
    
    public void closeSocket() throws IOException{
        socket.close();
    }
    
    public boolean isAlive() throws IOException{
        
        System.out.println("check if socket is closed!!");
        boolean alive = true;
        
        if(socket.isClosed())
        {alive = false;}
        
        return alive;
    }
}
