package DSE;

import Message.Message;
import Utility.Config;
import Message.*;
import org.w3c.dom.*;
import Utility.*;


public class TrcControllerFsm extends FSM{
    
        final protected int UNDEF = 0;
	final protected int START = 1; 	
	final protected int END = 2;
        final protected int WAIT_FOR_PDFE_REQUEST = 3;
	final protected int WAIT_FOR_BNS = 4;
	
	protected int state;
        private long bandwidth;

	private DSEMessage pdfeMes;
	
	
	public TrcControllerFsm(DSE dse, String ses){
	super(dse, ses);	
	
        state = START;
	}//
	

        @Override
	public int execute(Message msg)

	{
              System.out.println("execute!!!!");
		switch(state)
		{
		case START:
		{
                  stateSTART(msg);   
                    
                }//start
		break;

		case WAIT_FOR_PDFE_REQUEST:
		{
		
                    Document dseReq=null; 
                        if(msg.getType() == Message.DSE_MSG){
                        	
                         DSEMessage dseMsg = (DSEMessage) msg;
                         
                         if(msg.getCommand().equals("StatisticsRequest")){
                             
                         System.out.println("received statistics req");
                             
                         dseReq = dseMsg.getValue();
                         Element band = (Element)((dseReq.getElementsByTagName("band")).item(0));
                         String b =  band.getTextContent();
                         bandwidth = Long.parseLong(b);
                         
                        
                         XMLUtility.getInstance().printXML(dseMsg.getValue());
                         sendRequestToBNS(dseMsg);
  
                         }
   
                        }
			
		}//WAIT_FOR_CMD
		break;

		case WAIT_FOR_BNS :
		{
                    Document bnsReply = null; //the xml received from the bns
                    System.out.println("state wait for bns");
                                        
                    if(msg.getType() == Message.BNS_MSG){   
                        String command = new String("");//the case where the command received from the bns  is response we consider its first child
                        System.out.println("the command received from the bns is " + msg.getCommand());
                        BNSMessage bnsMsg = (BNSMessage)msg;
                        bnsReply = bnsMsg.getValue(); //the xml received from BNS
                        if((bnsReply.getElementsByTagName("errore").item(0))!= null){
                         command = new String("errore");
                        System.out.println("the significative command received from the BNS is "+ command);
                        }
                        Document pdfeResponse;
                         System.out.println("the xml received from the bns is ");
                         XMLUtility.getInstance().printXML(bnsMsg.getValue());

                    
                         if(msg.getCommand().equals("xnm:error") ||  command.equals("errore")){////////ajouter the command
                           
                            //ajouter le cas ou la reponse est negative
                            pdfeResponse = createPdFeResponse("nack","configurationResponse","0"); 
                           sendResponseToPdFe(pdfeResponse); 
                           System.out.println("config non ok, nack sent to pdfe");
                           XMLUtility.getInstance().printXML(pdfeResponse);
                           
                        state = END;
                        stateEND();
                        }//else
                    } //if   
                    
                    
			
                }//WAIT_FOR_ROUTER
		break;

		case END :
		{

			System.out.println("end trc fe");
                        stateEND();
		}//end
		break;
		}//switch
return state;
	}//end execute(Message msg)

public void timeout()
	{
		if(state != END)
		{
			state = END;
			execute(null);								
		}
	}

private void sendRequestToBNS(DSEMessage dseMsg)
	{
		//Document doc = createBNSRequest(dseMsg.getValue());		
	//	XMLUtility.getInstance().saveXML(doc, "BNSrequest.xml");
		BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"),"", dseMsg.getValue(), dseMsg.getSessionID());
		
                dseContainer.sendMessage(mess); 
	}

private void sendRequestToFL(DSEMessage dseMsg)
{

		FLMessage mess = new FLMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"),"", dseMsg.getValue(), dseMsg.getSessionID());
		dseContainer.sendMessage(mess); 
}
protected void sendResponseToPdFe(Document xmlResponse)
	{
						
                DSEMessage am = new DSEMessage(pdfeMes.getSrcHost(), pdfeMes.getSrcPort(), xmlResponse, pdfeMes.getSessionID());
		dseContainer.sendMessage(am);
	}	

private Document createPdFeResponse(String value, String type/*bw response or configuration response*/, String usedBand){
    Document pdfeResponse = XMLUtility.getInstance().createDocument(); 
    if(type.equals("bandwidthResponse")){
    Element root = pdfeResponse.createElement("BWAvailabilityReply");
    Element isBandWidthOk = pdfeResponse.createElement("band");
    Element usedBandWidth = pdfeResponse.createElement("usedband");
    pdfeResponse.appendChild(root);
    
    root.appendChild(isBandWidthOk);
    root.appendChild(usedBandWidth);
    isBandWidthOk.setTextContent(value);
    usedBandWidth.setTextContent(usedBand);
    }
    else if(type.equals("configurationResponse")){
    Element root = pdfeResponse.createElement("SetCoSConfigurationResponse");
    
    pdfeResponse.appendChild(root);
    root.setTextContent(value);
    }
    
return pdfeResponse;
}

        @Override
        protected void stateSTART(Message msg){
            
            pdfeMes = (DSEMessage)msg;
                  
                    if(pdfeMes.getCommand().equals("StatisticsRequest")){
                        
                    state = WAIT_FOR_PDFE_REQUEST;
                    //System.out.println("the next state is"+ state);
                    }
                

}                          
                    

}//class
