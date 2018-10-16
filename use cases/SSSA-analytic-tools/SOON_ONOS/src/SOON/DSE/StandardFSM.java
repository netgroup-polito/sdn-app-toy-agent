package DSE;

import java.util.*;
import org.w3c.dom.*;
import Message.*;
import Utility.*;

abstract public class StandardFSM extends FSM {

    final protected int UNDEF = 0;
    final protected int START = 1;
    final protected int END = 2;
    final protected int WAIT_FOR_RSP = 3;
    final protected int RSP_TIMEOUT = 4;
    final protected int SEND_TO_BNS = 5;
    final protected int SEND_TO_DSE = 6;

    protected String type;
    protected int instanceID;
    protected Vector receivedDseMsg; 
    protected int dseMsgSent; 
    protected Message message;
    protected String aeRequestName;
    protected String dseRequestName;
    protected boolean isMaster;
    protected String currentStateName;

    public StandardFSM(DSE dse, String ses, String aeReq, String dseReq) {
        super(dse, ses);
        currentStateName = "UNDEF";
        aeRequestName = aeReq;
        dseRequestName = dseReq;
        dseMsgSent = 0;
        receivedDseMsg = new Vector();
        state = START;
        isMaster = false;
    }


    public int execute(Message msg) {
        switch (state) {
            case START: {
                currentStateName = "START";
                stateSTART(msg);
            }
            break;

            case WAIT_FOR_RSP: {
                currentStateName = "WAIT_FOR_RSP";
                stateWAIT_FOR_RSP(msg);
            }
            break;

            case SEND_TO_BNS: {
                currentStateName = "SEND_TO_BNS";
                stateSEND_TO_BNS(msg);
            }
            break;

            case SEND_TO_DSE: {
                currentStateName = "SEND_TO_DSE";
                stateSEND_TO_DSE(msg);
            }
            break;

            case RSP_TIMEOUT: {
                currentStateName = "RSP_TIMEOUT";
                stateRSP_TIMEOUT();
            }
            break;

            case END: {
                currentStateName = "END";
                stateEND();
            }
            break;
        }

        return state;
    }

    protected void stateSTART(Message msg) {
        System.out.println("State START");
        message = msg;
        if (message.getCommand().equals(aeRequestName)) {
            state = SEND_TO_DSE;
            isMaster = true;
        } else if (message.getCommand().equals(dseRequestName)) {
            state = SEND_TO_BNS;
            isMaster = false;
        }
    }

    protected void stateRSP_TIMEOUT() {
        System.out.println("State RSP_TIMEOUT");
        sendResponseToAe("Time escaped");
        state = END;
        execute(null);
    }

    protected void stateWAIT_FOR_RSP(Message msg) {
        System.out.println("State WAIT_FOR_RSP");
        if (msg.getType() == Message.DSE_MSG || msg.getType() == Message.BNS_MSG) //ricevuta risposta da DSE
        {
            receivedDseMsg.add(msg);
            if (receivedDseMsg.size() >= dseMsgSent) {
                if (isMaster) {
                    sendResponseToAe("OK");
                } else {
                    sendResponseToDse("OK");
                }
                stateEND();
            }
        }
    }

    //sends the configuration
    protected void stateSEND_TO_DSE(Message msg) {
        System.out.println("State SEND_TO_DSE");
        AEMessage aeMsg = (AEMessage) msg;
        state = WAIT_FOR_RSP;
    }

    protected void stateSEND_TO_BNS(Message msg) {
        
        try {
            DSEMessage dseMsg;
            dseMsg = (DSEMessage)(msg);
            state = WAIT_FOR_RSP;

            sendRequestToBNS(dseMsg);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void timeout() {
        if (state != END) {
            state = RSP_TIMEOUT;
            execute(null);
        }
    }


    protected void sendResponseToAe(String value) {
        Document doc = createAEResponse(value);
        dseContainer.sendMessage(new AEMessage(doc, message.getSessionID()));
    }

    private void sendRequestToBNS(DSEMessage dseMsg) {
        Document doc = createBNSRequest(dseMsg.getValue());
        //	XMLUtility.getInstance().saveXML(doc, "BNSrequest.xml");
        BNSMessage mess = new BNSMessage(Config.getInstance().getString("BNSname"), Config.getInstance().getInt("BNServerPort"), "", doc, dseMsg.getSessionID());

        dseContainer.sendMessage(mess);
    }

    protected void sendResponseToDse(String value) {
        Document doc = createDSEResponse(value);
        DSEMessage am = new DSEMessage(message.getSrcHost(), message.getSrcPort(), doc, message.getSessionID());
        dseContainer.sendMessage(am);
    }

    protected Message getMessage() {
        return message;
    }

    abstract public Document createAEResponse(String value);

    abstract public Document createDSEResponse(String value);

    abstract public Document createBNSRequest(Document msgDoc);

    abstract public Document createDSERequest(Document msgDoc);

}

