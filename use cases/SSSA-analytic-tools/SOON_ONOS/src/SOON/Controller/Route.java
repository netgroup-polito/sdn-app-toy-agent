

package Controller;

import java.util.ArrayList;
import java.util.List;



/**
 * Represents a route between two switches
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class Route implements Comparable<Route> {
    protected RouteId id;
    protected List<NodePortTuple> switchPorts;
    protected int routeCount;

    public Route(RouteId id, List<NodePortTuple> switchPorts) {
        super();
        this.id = id;
        this.switchPorts = switchPorts;
        this.routeCount = 0; // useful if multipath routing available
    }

    public Route(Long src, Long dst) {
        super();
        this.id = new RouteId(src, dst);
        this.switchPorts = new ArrayList<NodePortTuple>();
        this.routeCount = 0;
    }

    /**
     * @return the id
     */
    public RouteId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(RouteId id) {
        this.id = id;
    }

    /**
     * @return the path
     */
    public List<NodePortTuple> getPath() {
        return switchPorts;
    }

    /**
     * @param path the path to set
     */
    public void setPath(List<NodePortTuple> switchPorts) {
        this.switchPorts = switchPorts;
    }

    /**
     * @param routeCount routeCount set by (ECMP) buildRoute method 
     */
    public void setRouteCount(int routeCount) {
        this.routeCount = routeCount;
    }
    
    /**
     * @return routeCount return routeCount set by (ECMP) buildRoute method 
     */
    public int getRouteCount() {
        return routeCount;
    }
    
    @Override
    public int hashCode() {
        final int prime = 5791;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((switchPorts == null) ? 0 : switchPorts.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Route other = (Route) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (switchPorts == null) {
            if (other.switchPorts != null)
                return false;
        } else if (!switchPorts.equals(other.switchPorts))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Route [id=" + id + ", switchPorts=" + switchPorts + "]";
    }

    /**
     * Compares the path lengths between Routes.
     */
    @Override
    public int compareTo(Route o) {
        return ((Integer)switchPorts.size()).compareTo(o.switchPorts.size());
    }
}
