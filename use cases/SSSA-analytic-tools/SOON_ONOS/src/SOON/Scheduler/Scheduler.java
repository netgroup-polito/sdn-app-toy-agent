
package Scheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public final class Scheduler implements Runnable {
    
    private static Scheduler instance;

    public static Scheduler instance() {
        if (instance == null) {
            instance = new Scheduler();
        }
        return instance;
    }

    private Scheduler() {
    }

    private void killall() {
        while (from2set.keySet().size() > 0) {
            SimEnt se = null;
            Iterator it = from2set.keySet().iterator();
            se = (SimEnt)it.next();
            se.suicide();

        }

    }


    public void stop() {
        done = true;
    }

    void deathSimEnt(SimEnt ent) {
        Set form;
        form = new HashSet(getEventsFrom(ent));

        for (Iterator it = form.iterator(); it.hasNext(); ) {
            EventHandle h = (EventHandle)it.next();
            deregister(h);
        }



        from2set.remove(ent);
        Set to = new HashSet(getEventsTo(ent));
        for (Iterator it = to.iterator(); it.hasNext(); ) {
            EventHandle h = (EventHandle)it.next();
            deregister(h);
        }
        to2set.remove(ent);
    }

    void birthSimEnt(SimEnt ent) {
        Set from = instance().getEventsFrom(ent);
        Set to = instance().getEventsTo(ent);
    }

    public void reset() {
        this.from2set.clear();
        this.to2set.clear();
        this.ud2ehandle.clear();
        this.timeNow = 0;
        this.uid = 0;
        this.done = false;

    }

    private final HashMap from2set = new HashMap();

    private Set getEventsFrom(SimEnt e) {
        HashSet set = (HashSet)from2set.get(e);
        if (set == null) {
            set = new HashSet();
            from2set.put(e, set);
        }
        return set;
    }
    private HashMap to2set = new HashMap();

    private Set getEventsTo(SimEnt e) {
        HashSet set = (HashSet)to2set.get(e);
        if (set == null) {
            set = new HashSet();
            to2set.put(e, set);
        }
        return set;
    }
    private final TreeMap ud2ehandle = new TreeMap();

    /**
     * @param registrar
     * @param target
     * @param ev
     * @param t
     * @return
     */
    public static Scheduler.EventHandle register(SimEnt registrar,
                                                 SimEnt target, Event ev,
                                                 double t) {
        double deliveryTime = Scheduler.getTime() + t;
        EventHandle handle =
            new EventHandle(registrar, target, ev, new UniqueDouble(deliveryTime));
        instance().getEventsFrom(handle.registrar).add(handle);
        instance().getEventsTo(handle.target).add(handle);
        instance().ud2ehandle.put(handle.udt, handle);
        return handle;
    }

    public static void deregister(EventHandle handle) {
        instance().getEventsFrom(handle.registrar).remove(handle);
        instance().getEventsTo(handle.target).remove(handle);
        UniqueDouble testUdt = handle.udt;
        instance().ud2ehandle.remove(handle.udt);
    }
    
    private boolean done = false;
    private static double timeNow = 0;

    public void run() {
        do {
            if (ud2ehandle.size() == 0) {
                done = true;
            } else {
                UniqueDouble udt =
                    (Scheduler.UniqueDouble)ud2ehandle.firstKey();
                EventHandle h;
                h = (Scheduler.EventHandle)ud2ehandle.get(udt);
                timeNow = udt.value.doubleValue();
                h.ev.entering(h.target);
                h.target.recv(h.registrar, h.ev);
                h.registrar.deliveryAck(h);
                deregister(h);

            }

        } while (!done);
        killall();
        reset();
    }

    public static double getTime() {
        return timeNow;
    }

    public static class EventHandle {
        private final SimEnt registrar, target;
        private final Event ev;
        private final UniqueDouble udt;

        private EventHandle(SimEnt registrar, SimEnt target, Event ev,
                            UniqueDouble udt) {
            this.registrar = registrar;
            this.target = target;
            this.ev = ev;
            this.udt = udt;
        }
    }

    private int uid = 0;

    private static class UniqueDouble implements Comparable {
        Double value;
        int discriminator;

        UniqueDouble(double value) {
            this.value = new Double(value);
            discriminator = (Scheduler.instance().uid);
            (Scheduler.instance().uid)++;
        }

        public int compareTo(Object o) {
            UniqueDouble other = (UniqueDouble)o;

            if (this.value.doubleValue() < other.value.doubleValue()) {
                return -1;
            } else if (this.value.doubleValue() > other.value.doubleValue()) {
                return +1;
            } else {
                if (this.discriminator < other.discriminator) {
                    return -1;
                } else if (this.discriminator > other.discriminator) {
                    return +1;
                } else
                    return 0;
            }
        }

        public String toString() {
            return value.toString() + "(" + discriminator + ")";
        }
    }


}

