package DSE;


import org.w3c.dom.*;
import Utility.*;

public class ControllerFsm extends PdFeControllerFsm

{
  

 
public ControllerFsm(DSE dse, String ses)
	{
		 super(dse, ses); 
	}

 public Document createAEResponse(String value)
	{
    Document doc = XMLUtility.getInstance().createDocument();  
    Element root = doc.createElement("StatisticsRequestResponse");
    doc.appendChild(root);      
    Element resp = doc.createElement("Response");  
		resp.appendChild(doc.createTextNode(value));     
		root.appendChild(resp);   
		return doc;
	}


}