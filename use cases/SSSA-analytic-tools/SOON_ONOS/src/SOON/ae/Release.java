
package ae;

//import Message.ServiceMessage;
import Message.AEMessage;
import Utility.XMLUtility;


public class Release implements Scheduler.Event{
    
    private int idy = 0;
    private AEMessage mymsg = null;

    public Release()
	{}
    
    public Release(AEMessage msg)
	{mymsg = msg;}
    

    @Override
    public void entering(Scheduler.SimEnt locale) {

    System.out.println("Event Release");

    XMLUtility.getInstance().printXML(mymsg.getValue());
    
    }

}
    
    

