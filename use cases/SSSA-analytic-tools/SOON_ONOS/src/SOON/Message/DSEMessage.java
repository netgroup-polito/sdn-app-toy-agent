package Message;

import org.w3c.dom.*;


public class DSEMessage extends Message {
	
	private Document doc;
	
	
	public DSEMessage(String h, int p, Document d, String s)
	{
		type = Message.DSE_MSG;
		doc = d;
		dstHost = h;
		dstPort = p;
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
	
	public String getCommand()
	{
		return doc.getDocumentElement().getNodeName();
	}		
}
