
package ae;


import Message.AEMessage;
import Scheduler.Admin;
import Utility.XMLUtility;
import java.net.InetAddress;
import java.util.ArrayList;
import org.w3c.dom.*;

public class Request implements Scheduler.Event{

    private Document my_document = null;
    private String session = "";
    static int MIN = 1;
    static int MAX = 11;
    
    private AEMessage mymsg = null;
   
    public Request()
	{
               }
    
     public Request(AEMessage msg)
	{
        mymsg = msg;
        }
     
       public Request(Document doc)
	{
        my_document = doc;
        }



    public Request(Document my_doc, String my_string)
	{
         my_document = my_doc;
         session = my_string;
        }
    




    @Override
    public void entering(Scheduler.SimEnt locale) {
        
        System.out.println("we are sending a request");
        
        ApplicationEntity ae = new ApplicationEntity();
        XMLUtility.getInstance().printXML(my_document);
        ae.sendRequest(mymsg);
    
    }//entering
    

/***************************************************************************************************/    
}
