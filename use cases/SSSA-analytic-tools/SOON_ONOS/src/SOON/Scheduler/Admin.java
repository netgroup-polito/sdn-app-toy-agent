
package Scheduler;


public class Admin extends SimEnt {

    public int id;
    private SimEnt sim;
    private int sentmsg = 0;
    private int recmsg = 0;
    private double time = 0;

    public Admin(int id) {
        super();
        this.id = id;
    }


       public Admin(int id, Event ev, double t) {
        super();
        this.id = id;
        time = t;
        
        send(this, ev, time);
    }

    public void recv(SimEnt src, Event ev) {


        if (ev instanceof ae.Release) {
              sentmsg++;
                    }
        
        if (ev instanceof ae.Request) {
            recmsg++;
        }
    }

    public void deliveryAck(Scheduler.EventHandle h) {
    }

    public void setAdmin(SimEnt sim) {
        this.sim = sim;
    }
    public String getName(){
        return "Admin Instance";
    }

    
}
