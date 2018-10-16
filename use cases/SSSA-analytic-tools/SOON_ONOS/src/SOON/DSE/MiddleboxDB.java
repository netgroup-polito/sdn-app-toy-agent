package DSE;

import static DSE.DSE.doBlock;
import static DSE.DSE.overloadedSwitches;
import Utility.*;
import java.util.*;
import org.w3c.dom.*;

public class MiddleboxDB {

    private Document serviceDB;
    private Document SwitchMboxDB;
    private Document middleboxDB;
    private Document ServiceInstanceDB;

    public MiddleboxDB() {
        
        serviceDB = XMLUtility.getInstance().createDocument();
        SwitchMboxDB = XMLUtility.getInstance().createDocument();
        middleboxDB = XMLUtility.getInstance().createDocument();
        ServiceInstanceDB = XMLUtility.getInstance().createDocument();
    }

    public void setDB(Document servivceDB, Document swDB/*, Document instanceDB*/) {
        this.serviceDB = servivceDB;
        this.SwitchMboxDB = swDB;
        // middleboxDB = mboxDB;]
        //ServiceInstanceDB = instanceDB;
    }

    public void setMiddleboxDB(Document mboxDB) {
        middleboxDB = mboxDB;
    }

    public Document getMiddleboxDB() {
        return middleboxDB;
    }

    public ArrayList<Element> getServiceMiddleboxes(String serviceID) { //returns one of the VLAN IDs belonging to serviceID 
        if (serviceDB.getDocumentElement() == null) {
            System.out.println("Policy empty!");
            return null;
        }
        HashMap<Long, String> switchType = new HashMap<>();
        ArrayList<Element> swmboxes = new ArrayList<>();

        //XMLUtility.getInstance().printXML(serviceDB);
        NodeList nodelist = serviceDB.getElementsByTagName("Services");
        Element mb;
        if (overloadedSwitches.size() > 0) {
            System.out.println("Overloaded switches ");
            for (long s : overloadedSwitches) {
                System.out.println(s);
            }
        }
        //select all middleboxes with the same vlan id
        for (int j = 0; j < nodelist.getLength(); j++) {
            Element policy = (Element) nodelist.item(j);
            NodeList ch = policy.getElementsByTagName("Service");
            int no_services = ch.getLength();
            Random r = new Random();
            for (int c = 0; c < no_services; c++) {//15 
                mb = (Element) ch.item(c);
                String servType = mb.getAttribute("type");

                if (servType.equals(serviceID)) {
                    String mboxInstance = mb.getAttribute("vlid");
                    //System.out.println("Getting service mboxes for service type:" + serviceID + " instance:" + mboxInstance);
                    Element swMbox = getSwitchMboxAttachment(mboxInstance);
                    if (swMbox != null) {
                        //System.out.println("check middlebox on switch " + swMbox.getAttribute("switch"));
                        long sw = Long.parseLong(swMbox.getAttribute("switch"));
                        if (!overloadedSwitches.contains(sw)
                                || (!doBlock /*&& switchesWithMiddlebox.size() <= 3*/)) {//to be commented for blocking case
                            if (switchType.containsKey(sw) == false) {
                                swmboxes.add(swMbox);
                                switchType.put(sw, servType);
                                //System.out.println("allow middlebox on switch " + sw);
                            }
                        }
                    } else {
                        System.out.println("NULL swMbox");
                    }
                }
            }
        }
        //System.out.println("CANNOT FIND MIDDLEBOX!");
        return swmboxes;
    }

    public Element getSwitchMboxAttachment(String instance) {
        if (SwitchMboxDB.getDocumentElement() == null) {
            System.out.println("SwitchMbox empty!");
            return null;
        }
        NodeList nodelist = SwitchMboxDB.getElementsByTagName("SwitchMBoxes");
        for (int j = 0; j < nodelist.getLength(); j++) {
            Element switchMbox = (Element) nodelist.item(j);
            NodeList sm = switchMbox.getElementsByTagName("SwitchMBox");
            //System.out.println("SwitchMBoxes size " + sm.getLength());
            for (int c = 0; c < sm.getLength(); c++) {
                Element swmb = (Element) sm.item(c);
                String vlan = swmb.getAttribute("vlid");
                if (vlan.equals(instance)) {
                    return swmb;
                }
            }
        }
        return null;
    }
}
//fin nrdb
