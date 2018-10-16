package DSE;


import org.w3c.dom.*;
import Message.*;

public abstract class FSM 
{	
	final protected int UNDEF = 0;
	final protected int START = 1;
	final protected int END = 2;
		
	protected DSE dseContainer;
	protected int state;
	protected String sessionID;
	
	
        public FSM(){
            
        }
        
        
        
        public FSM(DSE d, String s)
	{
		dseContainer = d;
		sessionID = s;
		state = START;
	}

        public abstract int execute(Message msg);
	
	public abstract void timeout();
			
	protected Element getElement(Document doc, String name)
	{
		return (Element)(doc.getElementsByTagName(name).item(0));
	}
	
	protected void setText(Element el, String value)
	{
		el.appendChild(el.getOwnerDocument().createTextNode(value)); 
	}	
	
	protected String getText(Element el)
	{
		return el.getFirstChild().getNodeValue(); 
	}		
	
	protected void stateSTART(Message msg)
	{
		//System.out.println("State START");		
		state = START;
	}
	
	protected void stateEND()
	{
		state = END;
		dseContainer.deleteFsm(sessionID);
	}
	
	
	protected String getDSEName()
	{
		return dseContainer.getHostName();
	}
}
