
package Scheduler;


public abstract class SimEnt {

    public SimEnt() {
        
        Scheduler.instance().birthSimEnt(this);
    }

    public final void suicide() {
        this.destructor();
        Scheduler.instance().deathSimEnt(this);
    }

    public void destructor() {
    }

    public final Scheduler.EventHandle send(SimEnt dst, Event ev, double t) {
        
        return Scheduler.instance().register(this, dst, ev, t);
    }

    public void revokeSend(Scheduler.EventHandle h) {
        Scheduler.instance().deregister(h);
    }

    public abstract void recv(SimEnt src, Event ev);

    public abstract void deliveryAck(Scheduler.EventHandle h);

}
