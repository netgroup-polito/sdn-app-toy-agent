
package DSE;

import java.util.List;
import Controller.NodePortTuple;


public class Segment {

    public String pathType;
    public String direction;
    public long origSrcSw;
    public long origDstSw;
    public long swdpid;
    public short port;
    public String srcIP;
    public String dstIP;
    public String subnetMask;
    public long swdpid2;
    public short port2;
    public short vlid_action;
    public short vlid_match;
    public boolean isLastNode;
    public int actionType;
    public double serviceTime;
    
    public  List<NodePortTuple> intermNodePortTuple;

    public Segment(String pathType, String direction, long origSrcSw, long origDstSw, long swdpid, 
            short port, String srcIP, String dstIP, String subnetMask, long swdpid2, short port2, 
            short vlid_action, short vlid_match, boolean isLastNode, int actionType, double serviceTime) {
        this.pathType = pathType;
        this.direction = direction;
        this.origSrcSw = origSrcSw;
        this.origDstSw = origDstSw;
        this.swdpid = swdpid;
        this.port = port;
        this.srcIP = srcIP;
        this.dstIP = dstIP;
        this.subnetMask = subnetMask;
        this.swdpid2 = swdpid2;
        this.port2 = port2;
        this.vlid_action = vlid_action;
        this.vlid_match = vlid_match;
        this.isLastNode = isLastNode;
        this.actionType = actionType;
        this.serviceTime = serviceTime;
    }
}
