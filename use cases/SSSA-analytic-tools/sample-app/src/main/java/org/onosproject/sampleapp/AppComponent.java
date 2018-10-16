/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.sampleapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.Tools;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.sampleapp.config.AConfiguration;
import org.onosproject.sampleapp.data.A;
import org.onosproject.sampleapp.data.Load;
import org.onosproject.sampleapp.data.Message;
import org.onosproject.sampleapp.data.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Skeletal ONOS application component.
 */
@Service
@Component(immediate = true)
public class AppComponent implements AppComponentService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry configRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService configService;


    private ApplicationId appId;
    private A a;
    private A a2;
    private List<Switch> switchList = new ArrayList<>();
    private List<Load> loadList = new ArrayList<>();
    private int testRepetition = 1000;
    private Message message;

    private final ExecutorService eventExecutor =
            Executors.newSingleThreadExecutor(Tools.groupedThreads("onos/configtest-ctl", "event-handler", log));

    private final ConfigFactory configFactory =
            new ConfigFactory(SubjectFactories.APP_SUBJECT_FACTORY, AConfiguration.class, "configtest") {
                @Override
                public AConfiguration createConfig() {
                    return new AConfiguration();
                }
            };

    private final NetworkConfigListener configListener = new InternalConfigListener();

    @Activate
    protected void activate() {

        appId = coreService.registerApplication("it.polito.configtest");
        configService.addListener(configListener);
        configRegistry.registerConfigFactory(configFactory);
        message = new Message(1, "test", "my test");
        this.initializeFull();
        String s = a.toJson().toString();
        log.info("Started");
        log.info("json: " + s);
    }

    @Deactivate
    protected void deactivate() {

        configService.removeListener(configListener);
        configRegistry.unregisterConfigFactory(configFactory);
        eventExecutor.shutdown();
        log.info("Stopped");
    }

    @Override
    public int getTestRepetition() {
        return this.testRepetition;
    }

    @Override
    public void setTestRepetition(int testRepetition) {
        this.testRepetition = testRepetition;
    }

    @Override
    public A getA2() {
        return this.a2;
    }

    @Override
    public void setA2(A a2) {
        this.a2 = a2;
    }

    @Override
    public A getA() {
        return this.a;
    }

    @Override
    public void setA(A a) {
        this.a = a;
    }

    @Override
    public List<Switch> getSwitches() {
        return this.switchList;
    }

    @Override
    public void setSwitchList(List<Switch> switchList) {
        this.switchList = switchList;
    }

    @Override
    public List<Load> getLoadList() {
        return loadList;
    }

    @Override
    public void setLoadList(List<Load> loadList) {
        this.loadList = loadList;
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public void initializeFull() {

        String jsonString = "{\"a\":{\"b\":{\"c\":{\"c\":{\"a\":{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{}}}}," +
                "\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":50,\"l2\":\"test\"," +
                "\"l\":[{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}," +
                "\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]}," +
                "\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11," +
                "\"l2\":\"liss\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20," +
                "\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}," +
                "{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50," +
                "\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
                "\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
                "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
                "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
                "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"},{\"l1\":46," +
                "\"l2\":\"lista1\"}]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":1,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
                "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
                "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
                "\"l3\":\"orchestrator\"},\"l1\":3,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
                "\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
                "\"l3\":\"96\",\"l2\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
                "\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"l\":[]},\"c\":{}}},\"l1\":30,\"l2\":\"req\"," +
                "\"l3\":\"usr\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":20,\"l2\":\"test4\"," +
                "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}," +
                "\"a2\":{\"b\":{\"c\":{\"c\":{\"a\":{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{}}}},\"l1\":20," +
                "\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":50,\"l2\":\"test\"," +
                "\"l\":[{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}," +
                "\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]}," +
                "\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11," +
                "\"l2\":\"liss\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20," +
                "\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}," +
                "{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50," +
                "\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
                "\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
                "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
                "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
                "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
                "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}," +
                "\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{\"l1\":67," +
                "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
                "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
                "\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}," +
                "\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"l1\":25," +
                "\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"}," +
                "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
                "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\"," +
                "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
                "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
                "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
                "\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"l\":[]},\"c\":{}}},\"l1\":30,\"l2\":\"req\"," +
                "\"l3\":\"usr\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":20,\"l2\":\"test4\"," +
                "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"},\"switches\":[{\"id\":1,\"ip\":\"10.0.0.1\"," +
                "\"mac\":\"00:00:00:00:00:11\"},{\"id\":2,\"ip\":\"10.0.0.2\",\"mac\":\"00:00:00:00:00:22\"}," +
                "{\"id\":3,\"ip\":\"10.0.0.3\",\"mac\":\"00:00:00:00:00:33\"},{\"id\":4,\"ip\":\"10.0.0.4\"," +
                "\"mac\":\"00:00:00:00:00:44\"},{\"id\":5,\"ip\":\"10.0.0.5\",\"mac\":\"00:00:00:00:00:55\"}," +
                "{\"id\":6,\"ip\":\"10.0.0.6\",\"mac\":\"00:00:00:00:00:66\"},{\"id\":7,\"ip\":\"10.0.0.7\"," +
                "\"mac\":\"00:00:00:00:00:77\"}],\"load\":[{\"id\":\"1\",\"tp\":823.0},{\"id\":\"2\",\"tp\":655.0}," +
                "{\"id\":\"3\",\"tp\":830.0},{\"id\":\"4\",\"tp\":634.0},{\"id\":\"5\",\"tp\":533.0},{\"id\":\"6\"," +
                "\"tp\":666.0},{\"id\":\"7\",\"tp\":802.0}]}";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);
            this.a = mapper.convertValue(jsonNode.get("a"), A.class);
            this.a2 = mapper.convertValue(jsonNode.get("a2"), A.class);
            this.switchList = mapper.convertValue(jsonNode.get("switches"), new TypeReference<List<Switch>>() { });
            this.loadList = mapper.convertValue(jsonNode.get("load"), new TypeReference<List<Load>>() { });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        //this.a = initA();
        //this.a2 = initA();
        this.switchList = sampleSwitches();
        this.loadList = sampleLoads(this.switchList);
    }

    /*
    private A initA() {

        // log.info("we are in initA " + System.nanoTime());

        A a = new A();

        a.b = new B();
        a.c = new C();

        // foglie di A
        a.fol1 = 1;
        a.fol2 = "Str3";

        // A contiene B + foglie di B
        a.b.fol1 = 10;
        a.b.fol2 = "Application";
        // a.getB.listaA = new ArrayList<A>();

        A lis1 = new A();
        A lis2 = new A();
        A lis3 = new A();
        A lis4 = new A();
        // errorlis4 = null;
        // errorlis3 = null;
        // error
        lis3.b = new B();  // null;
        lis3.c = new C();  // null;
        lis3.fol1 = 10;
        lis3.fol2 = "test1";
        lis4.b = new B();  // null;
        lis4.c = new C();  // null;
        lis4.fol1 = 10;
        lis4.fol2 = "test2";


        lis2.b = new B();  // null;
        lis2.c = new C();  // null;
        lis2.fol1 = 10;
        lis2.fol2 = "test3";
        lis1.b = new B();  // null;
        lis1.c = new C();  // null;
        lis1.fol1 = 40;
        lis1.fol2 = "tes";
        a.b.listaA.add(lis1);
        a.b.listaA.add(lis2);
        a.b.listaA.add(lis3);
        a.b.listaA.add(lis4);

        // A contiene C + foglie di C
        a.c.fol1 = 20;
        a.c.fol2 = "test4";
        a.c.fol3 = "orchestrator";

        a.c.a = new A();

        // C contiene A
        a.c.a.fol1 = 25;
        a.c.a.fol2 = "Applic1";

        a.c.a.c = new C();

        a.c.a.c.fol1 = 30;
        a.c.a.c.fol2 = "req";
        a.c.a.c.fol3 = "usr";

        a.c.a.c.a = new A();

        a.c.a.c.a.fol1 = 44;
        a.c.a.c.a.fol2 = "foll1";
        a.c.a.c.a.b = new B();  // null;
        a.c.a.c.a.c = new C();  // null;

        a.c.a.b = new B();

        a.c.a.b.fol1 = 50;
        a.c.a.b.fol2 = "foll2";*/

        /*
        a.c.a.b.c = new C();

        a.c.a.b.c.a = new A();  // null;
        a.c.a.b.c.fol1 = 66;
        a.c.a.b.c.fol2 = "json";
        a.c.a.b.c.fol3 = "js";

        //  a.getC.a.getB.listaA = new ArrayList<A>();
        A liss1 = new A(); A liss2 = new A(); A liss3 = new A();

        liss2.fol1 = 11;
        liss2.fol2 = "liss";
        // liss2.getB = null;
        // liss2.getC = null;

        liss1.fol1 = 77;
        liss1.fol2 = "list1";

        liss1.b = new B();
        liss1.c = new C();

        // liss1.getB.listaA = new ArrayList<A>();
        A lista1 = new A();
        A lista2 = new A();
        A lista3 = new A();


        lista3.fol1 = 45;
        lista3.fol2 = "lista1";
        lista3.b = new B();  // null;
        lista2.fol1 = 45;
        lista2.fol2 = "lista11";
        lista2.b = new B();  // null;


        lista1.fol1 = 45;
        lista1.fol2 = "lista111";
        lista1.b = new B();  // null;

        lista1.c = new C();
        lista1.c.fol1 = 96;
        lista1.c.fol2 = "96";
        lista1.c.fol3 = "lista3";
        lista1.c.a = new A();  // null;

        liss3.b = new B();
        liss3.b.fol1 = 50;

        liss1.b.fol1 = 54;
        liss1.b.fol2 = "final";

        liss1.b.c = new C();
        liss1.b.c.fol1 = 15;
        liss1.b.c.fol2 = "end";
        liss1.b.c.fol3 = "set";

        liss1.b.listaA.add(lista1);
        liss1.b.listaA.add(lista2);
        liss1.b.listaA.add(lista3);



        liss1.c.fol1 = 58;
        liss1.c.fol2 = "foglia";
        liss1.c.fol3 = "last";

        liss1.c.a = new A();
        liss1.c.a.fol1 = 67;
        liss1.c.a.fol2 = "tor";
        liss1.c.a.b = new B();  // null;
        liss1.c.a.c = new C();  // null;

        a.c.a.b.listaA.add(liss1);
        a.c.a.b.listaA.add(liss2);
        a.c.a.b.listaA.add(liss3);

        return a;

    }*/

    private List<Switch> sampleSwitches() {

        List<Switch> switches = new ArrayList<>();

        switches.add(new Switch(1, "10.0.0.1", "00:00:00:00:00:11"));
        switches.add(new Switch(2, "10.0.0.2", "00:00:00:00:00:22"));
        switches.add(new Switch(3, "10.0.0.3", "00:00:00:00:00:33"));
        switches.add(new Switch(4, "10.0.0.4", "00:00:00:00:00:44"));
        switches.add(new Switch(5, "10.0.0.5", "00:00:00:00:00:55"));
        switches.add(new Switch(6, "10.0.0.6", "00:00:00:00:00:66"));
        switches.add(new Switch(7, "10.0.0.7", "00:00:00:00:00:77"));

        return switches;
    }

    private List<Load> sampleLoads(List<Switch> switches) {

        List<Load> loads = new ArrayList<>();

        for (Switch s : switches) {
            loads.add(new Load(s.getId(), new Random().nextInt(500) + 500));
        }

        return loads;
    }

    private class InternalConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            if (!event.configClass().equals(AConfiguration.class)) {
                return;
            }
            switch (event.type()) {
                case CONFIG_ADDED:
                    log.warn("ADDED");
                case CONFIG_UPDATED:
                    eventExecutor.execute(AppComponent.this::readConfiguration);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Load configuration by the Network Config API.
     */
    private void readConfiguration() {

        AConfiguration config = configRegistry.getConfig(appId, AConfiguration.class);
        if (config == null) {
            log.debug("No configuration found");
            return;
        }

        log.info("Updating configuration ...");

        this.a = config.getA();

        log.info("Updated configuration");
    }
}

