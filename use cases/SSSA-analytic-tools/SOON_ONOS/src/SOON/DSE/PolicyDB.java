package DSE;

import Utility.*;
import org.w3c.dom.*;

public class PolicyDB {

    private Document db;
    private Document classDb;

    public PolicyDB() {
        
        db = XMLUtility.getInstance().createDocument();
        classDb = XMLUtility.getInstance().createDocument();
    }

    public void setDB(Document d, Document clsdb) {
        db = d;
        classDb = clsdb;
    }

    public String[] getServicePolicy(String servClass) {
        
        if (db.getDocumentElement() == null) {
            System.out.println("Service policy empty!");
            return null;
        }
        XMLUtility.getInstance().printXML(db);
        NodeList nodelist = db.getElementsByTagName("Policy");

        System.out.println("getting policies..." + nodelist.getLength());
        String[] nodes = null;

        boolean found = false;

        for (int j = 0; j < nodelist.getLength(); j++) {
            Element policy = (Element) nodelist.item(j);

            NodeList ch = policy.getElementsByTagName("ServiceChain");
            for (int c = 0; c < ch.getLength(); c++) {
                Element sChain = (Element) ch.item(c);
                String trafficType = sChain.getAttribute("class");
                System.out.println("trafficType: "+trafficType);
                System.out.println("servClass: "+servClass);
                if (trafficType.equals(servClass)) {
                    String chain = sChain.getAttribute("chain");
                    if (chain.length() > 0) {
                        nodes = chain.split("-");
                        System.out.println("No. of Middleboxes in the chain..." + nodes.length);
                        found = true;
                        break;
                    }
                }
            }
            if (found == true) {
                break;
            }
        }
        
        System.out.println("nodes.length: "+nodes.length);
        return nodes;

    }
    
    public String getClass(String ip) {
        if (classDb.getDocumentElement() == null) {
            //System.out.println("Policy empty!");
            return null;
        }

        //XMLUtility.getInstance().printXML(classDb);
        NodeList nodelist = classDb.getElementsByTagName("Classes");

        //System.out.println("getting class info..." + nodelist.getLength());
        for (int j = 0; j < nodelist.getLength(); j++) {
            Element cls = (Element) nodelist.item(j);

            NodeList ch = cls.getElementsByTagName("Class");
            for (int c = 0; c < ch.getLength(); c++) {
                Element cl = (Element) ch.item(c);
                String classType = cl.getAttribute("name");
                String subnetAddress = cl.getAttribute("subnet");
                String[] srcIP_split = subnetAddress.split("/");
                int mask = Integer.parseInt(srcIP_split[1]);
                int shift = mask / 8;
                String address1 = srcIP_split[0]; //Integer.parseInt(srcIP_split[0]);
                String[] baseAddress1 = address1.split("\\.");
                String[] baseAddress2 = ip.split("\\.");

                String temp1 = "", temp2 = "";
                //System.out.println("address1 " + address1 + " ip " + ip + " mask " + mask + " shift " + shift);

                for (int k = 0; k < shift; k++) {
                    //System.out.println("baseAddress1 " + baseAddress1.length);
                    //System.out.println("baseAddress1 " + baseAddress1[k] + " baseAddress2 " + baseAddress2[k]);
                    temp1 = temp1.concat(baseAddress1[k]);
                    temp2 = temp2.concat(baseAddress2[k]);
                    if (k < (shift - 1)) {
                        temp1 = temp1.concat(".");
                        temp2 = temp2.concat(".");
                    }

                }

                if (temp1.equals(temp2)) {
                    //System.out.println("Class found " + classType);
                    return classType;
                }
            }
        }
        return null;
    }
    
    
        public String getSubnetMask(String servClass) {
        if (classDb.getDocumentElement() == null) {
            System.out.println("class DB empty!");
            return null;
        }

        //XMLUtility.getInstance().printXML(classDb);
        NodeList nodelist = classDb.getElementsByTagName("Classes");

        //System.out.println("getting class info..." + nodelist.getLength());

        String subnet; //subnet mask
        for (int j = 0; j < nodelist.getLength(); j++) {
            Element cls = (Element) nodelist.item(j);
            NodeList ch = cls.getElementsByTagName("Class");
            for (int c = 0; c < ch.getLength(); c++) {
                Element cl = (Element) ch.item(c);
                String classType = cl.getAttribute("name");
                if (classType.equals(servClass)) {
                    subnet = cl.getAttribute("subnet");
                    String[] subnet_split = subnet.split("/");
                    String mask = subnet_split[1];
                    //System.out.println("Mask found " + mask);
                    return mask;
                }
            }
        }
        return null;
    }


    public static void main(String arg[]) {

    }

}

