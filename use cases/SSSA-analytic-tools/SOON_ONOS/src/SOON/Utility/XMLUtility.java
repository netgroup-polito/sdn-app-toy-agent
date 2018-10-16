package Utility;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class XMLUtility
{
	DocumentBuilder db;

	private static XMLUtility instance = null;

	static public XMLUtility getInstance ()
	{
		if(instance == null)
			instance = new XMLUtility();
		return instance;
	}

	protected XMLUtility()
	{
		try
		{
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
	  	db = dbf.newDocumentBuilder();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
  }

  public synchronized Document loadXML(String xmlFile)
  {
  	Document doc = db.newDocument();
		try
		{
	    doc = db.parse(xmlFile);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return doc;
  }

  public synchronized Document loadXMLfromStream(InputStream xmlFile)
  {
  	Document doc = db.newDocument();
		try
		{
	    doc = db.parse(xmlFile);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return doc;
  }

  public Document transformXML(Document docXml, Document docXslt)
  {
  	Document res = db.newDocument();
		try
		{
  		DOMSource sourceXml = new DOMSource(docXml);
  		DOMSource sourceXslt = new DOMSource(docXslt);
  		DOMResult	result = new DOMResult();
      TransformerFactory transFact = TransformerFactory.newInstance();
    	Transformer trans = transFact.newTransformer(sourceXslt);
    	trans.transform(sourceXml, result);
    	res = (Document)result.getNode();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return res;
  }

  public void printXML(Document doc)
  {
  	try
  	{
	  	TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			transformer.transform(new DOMSource(doc), new StreamResult(System.out));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
  }

  public void saveXMLtoStream(Document doc, OutputStream outputStream)
  {
  	try
  	{
	  	TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
  }

  public void saveXML(Document doc, String filename)
  {
  	try
  	{
  		TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			transformer.transform(new DOMSource(doc), new StreamResult(new File(filename)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
  }
  
  
  public String toString(Document doc)
  {
  	String str = "";
  	try
  	{ 
  		StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");		
			transformer.transform(source, result);
			str = result.getWriter().toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return str;
  }
  
  
  public Document createDocument()  
  {
  	try
		{
  		return db.newDocument();		
  	}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;		  	
  }
}