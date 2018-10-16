package Message;

import org.w3c.dom.*;

public class AEMessage extends Message {
	
	private Document doc;
	protected String name;
	
	public AEMessage()
	{
		type = Message.AE_MSG;
	}	
	
	public AEMessage(Document d, String s)
	{
		type = Message.AE_MSG;
//		name = n;
		doc = d;
//		host = h;
//		port = p;
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