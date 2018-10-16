package DSE;

import Message.Message;
import Utility.Config;
import Message.*;
import org.w3c.dom.*;
import Utility.*;


public class TrcFeFSM extends FSM {

    final protected int UNDEF = 0;
    final protected int START = 1;
    final protected int END = 2;
    final protected int WAIT_FOR_PDFE_REQUEST = 3;
    final protected int WAIT_FOR_BNS = 4;

    protected int state;
    private long bandwidth;
    private DSEMessage pdfeMes;

    public TrcFeFSM(DSE dse, String ses) {
        super(dse, ses);

        state = START;
    }//

    public int execute(Message msg) {
        
        switch (state) {
            case START: {
                stateSTART(msg);

            }//start
            break;

            case WAIT_FOR_PDFE_REQUEST: {
               
                Document dseReq = null;
                System.out.println("WAIT_FOR_PDFE_REQUEST");
                if (msg.getType() == Message.DSE_MSG) {

                    DSEMessage dseMsg = (DSEMessage) msg;

                    if (msg.getCommand().equals("StatisticsRequestFromDSE")) {
                        
                        System.out.println("received Statistics req");
                        System.out.println("the message to send to the bns is");
                        XMLUtility.getInstance().printXML(dseMsg.getValue());
                        sendRequestToBNS(dseMsg);
                        System.out.println("message sent to bns");

                        //sendRequestToBNS(dseMsg);   
                        state = WAIT_FOR_BNS;

                    }
                    if (msg.getCommand().equals("FlowSetupRequestFromDSE")) {
                        
                        System.out.println("received Flow Setup Request");
                        System.out.println("the message to send to the bns is");
                        XMLUtility.getInstance().printXML(dseMsg.getValue());
                        sendRequestToBNS(dseMsg);
                        System.out.println("message sent to bns");

                        //sendRequestToBNS(dseMsg);   
                        state = WAIT_FOR_BNS;

                    } else if (msg.getCommand().equals("DeleteFlowFromDSE")) {
                        
                        System.out.println("received Flow delete Request");
                        System.out.println("the message to send to the bns is");
                        XMLUtility.getInstance().printXML(dseMsg.getValue());
                        sendRequestToBNS(dseMsg);
                        System.out.println("message sent to bns");

                        //sendRequestToBNS(dseMsg);   
                        state = WAIT_FOR_BNS;

                    }  
                }

            }//WAIT_FOR_CMD
            break;

            case WAIT_FOR_BNS: {
                Document bnsReply = null; //the xml received from the bns
                System.out.println("state wait for bns");
                
                if (msg.getType() == Message.BNS_MSG) {
                    String command = new String("");//the case where the command received from the bns  is response we consider its first child
                    System.out.println("the command received from the bns is " + msg.getCommand());
                    BNSMessage bnsMsg = (BNSMessage) msg;
                    bnsReply = bnsMsg.getValue(); //the xml received from BNS
                    if ((bnsReply.getElementsByTagName("errore").item(0)) != null) {
                        command = new String("errore");
                        System.out.println("the significative command received from the BNS is " + command);
                    }
                    Document pdfeResponse;
                    System.out.println("the xml received from the bns is ");
                    XMLUtility.getInstance().printXML(bnsMsg.getValue());
             
                    if (msg.getCommand().equals("StatisticsReply")) {
                        System.out.println("statistics reply from controller");
                        state = END;
                        stateEND();

                    } else if (msg.getCommand().equals("FlowSetupReply")) {
                        System.out.println("FlowSetup reply controller");
                        state = END; 
                        stateEND();

                    } 
                    else if (msg.getCommand().equals("xnm:error") || command.equals("errore")) {////////ajouter the command

                        System.out.println("error");

                        state = END;
                        stateEND();
                    }//else
                } //if   

            }
            break;

            case END: {

                System.out.println("end trc fe");
                stateEND();
            }//end
            break;
        }//switch
        return state;
    }//end execute(Message msg)

    public void timeout() {
        if (state != END) {
            state = END;
            execute(null);
        }
    }

    private void sendRequestToBNS(DSEMessage dseMsg) {
        
        BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", dseMsg.getValue(), dseMsg.getSessionID());
        dseContainer.sendMessage(mess);
    }

    protected void sendResponseToPdFe(Document xmlResponse) {

        DSEMessage am = new DSEMessage(pdfeMes.getSrcHost(), pdfeMes.getSrcPort(), xmlResponse, pdfeMes.getSessionID());
        dseContainer.sendMessage(am);
    }


    protected void stateSTART(Message msg) {
        pdfeMes = (DSEMessage) msg;
  
        if (pdfeMes.getCommand().equals("StatisticsRequestFromDSE")) {
            state = WAIT_FOR_PDFE_REQUEST;

        } else if (pdfeMes.getCommand().equals("FlowSetupRequestFromDSE")) {
            state = WAIT_FOR_PDFE_REQUEST;

        } else if (pdfeMes.getCommand().equals("DeleteFlowFromDSE")) {
            state = WAIT_FOR_PDFE_REQUEST;

        }

    }

}//class
