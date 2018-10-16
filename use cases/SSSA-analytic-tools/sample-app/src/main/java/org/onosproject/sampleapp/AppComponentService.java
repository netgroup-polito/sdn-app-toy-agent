package org.onosproject.sampleapp;


import org.onosproject.sampleapp.data.A;
import org.onosproject.sampleapp.data.Load;
import org.onosproject.sampleapp.data.Message;
import org.onosproject.sampleapp.data.Switch;

import java.util.List;

public interface AppComponentService {

    void initializeFull();

    void init();

    int getTestRepetition();

    void setTestRepetition(int testRepetition);

    A getA();

    void setA(A a);

    A getA2();

    void setA2(A a2);

    List<Switch> getSwitches();

    void setSwitchList(List<Switch> switchList);

    List<Load> getLoadList();

    void setLoadList(List<Load> loadList);

    Message getMessage();

    void setMessage(Message message);
}
