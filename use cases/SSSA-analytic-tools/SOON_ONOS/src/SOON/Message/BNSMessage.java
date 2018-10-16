package Message;

import org.w3c.dom.*;

public class BNSMessage extends Message {
	
	private Document doc;
	protected String name;
	
	public BNSMessage()
	{
		type = Message.BNS_MSG;
	}	
	
	public BNSMessage(String h, int p, String n, Document d, String s)
	{  
           // System.out.println("we are in bns message");
            
		type = Message.BNS_MSG;
		name = n;
		doc = d;
		dstHost = h;
		dstPort = p;
		sessionID = s;
	}
        
        public BNSMessage(Document d, String s)
	{  
           // System.out.println("we are in bns message");
            
		//type = Message.BNS_MSG;
		//name = n;
		doc = d;
		//dstHost = h;
		//dstPort = p;
		sessionID = s;
	}
	
	public Document getValue()
	{
		return doc;	
	}
	
	public void setValue(Document d)
	{
		doc = d;	
	}
	
	public String getBnsName()
	{
		return name;	
	}
	
	public void setBnsName(String h)
	{
		name = h;	
	}	
	
	public String getCommand()
	{
		return doc.getDocumentElement().getNodeName();
	}			
}