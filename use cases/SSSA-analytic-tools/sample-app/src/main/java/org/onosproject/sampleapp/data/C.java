package org.onosproject.sampleapp.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class C {

    private A a = null;

    private C c = null;

    private int fol1 = 0;

    private String fol2 = "lara";

    private String fol3 = "orchesrator";

    public C(JsonNode jsonC) {

        this.a = !jsonC.path("a").isMissingNode() && !jsonC.path("a").isNull() ? new A(jsonC.path("a")) : null;
        this.c = !jsonC.path("c").isMissingNode() && !jsonC.path("c").isNull() ? new C(jsonC.path("c")) : null;
        this.fol1 = jsonC.path("l1").asInt(0);
        this.fol2 = jsonC.path("l2").asText("lara");
        this.fol3 = jsonC.path("l3").asText("orchesrator");
    }

    public C(String jsonStringC) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonC = mapper.readTree(jsonStringC);
            this.setA(!jsonC.path("a").isMissingNode() && !jsonC.path("a").isNull() ? new A(mapper
                    .writeValueAsString(jsonC.path("a"))) : null);
            this.setC(!jsonC.path("c").isMissingNode() && !jsonC.path("c").isNull() ? new C(mapper
                    .writeValueAsString(jsonC.path("c"))) : null);
            this.setL1(jsonC.path("l1").asInt(0));
            this.setL2(jsonC.path("l2").asText("lara"));
            this.setL3(jsonC.path("l3").asText("orchesrator"));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public C() {}

    public JsonNode toJson() {

        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);

        json.put("l1", this.getL1());
        json.put("l2", this.getL2());
        json.put("l3", this.getL3());
        json.set("a", this.a != null ? this.getA().toJson() : null);
        json.set("c", this.c != null ? this.getC().toJson() : null);

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    public A getA() {
        System.out.println(this.a);
        return a;
    }

    public int getL1() {
        System.out.println(this.fol1);
        return fol1;
    }

    public String getL2() {
        System.out.println(this.fol2);
        return fol2;
    }

    public String getL3() {
        System.out.println(this.fol3);
        return fol3;
    }

    public void setA(A a) {
        this.a = a;
        System.out.println(this.a != null ? this.a.toJson() : null);
    }

    public void setL1(int fol1) {
        this.fol1 = fol1;
        System.out.println(this.fol1);
    }

    public void setL2(String fol2) {
        this.fol2 = fol2;
        System.out.println(this.fol2);
    }

    public void setL3(String fol3) {
        this.fol3 = fol3;
        System.out.println(this.fol3);
    }

    public C getC() {
        System.out.println(this.c);
        return c;
    }

    public void setC(C c) {
        this.c = c;
        System.out.println(this.c != null ? this.c.toJson() : null);
    }
}
