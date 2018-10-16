package org.onosproject.sampleapp.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class A {


    private B b = null;

    private C c = null;

    private int fol1 = 0;

    private String fol2 = "Applyyyyy";

    public A(JsonNode jsonA) {

        this.b = !jsonA.path("b").isMissingNode() && !jsonA.path("b").isNull() ? new B(jsonA.path("b")) : null;
        this.c = !jsonA.path("c").isMissingNode() && !jsonA.path("c").isNull() ? new C(jsonA.path("c")) : null;
        this.fol1 = jsonA.path("l1").asInt();
        this.fol2 = jsonA.path("l2").textValue();
    }

    public A(String jsonStringA) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonA = mapper.readTree(jsonStringA);
            this.b = !jsonA.path("b").isMissingNode() && !jsonA.path("b").isNull() ? new B(mapper
                    .writeValueAsString(jsonA.path("b"))) : null;
            this.c = !jsonA.path("c").isMissingNode() && !jsonA.path("c").isNull() ? new C(mapper
                    .writeValueAsString(jsonA.path("c"))) : null;
            this.fol1 = jsonA.path("l1").asInt();
            this.fol2 = jsonA.path("l2").textValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public A() {}

    public JsonNode toJson() {

        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);

        json.put("l1", this.getL1());
        json.put("l2", this.getL2());
        json.set("b", b != null ? this.getB().toJson() : null);
        json.set("c", c != null ? this.getC().toJson() : null);

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    public B getB() {
        System.out.println(this.b);
        return b;
    }

    public C getC() {
        System.out.println(this.c);
        return c;
    }

    public int getL1() {
        System.out.println(this.fol1);
        return fol1;
    }

    public String getL2() {
        System.out.println(this.fol2);
        return fol2;
    }

    public void setB(B b) {
        this.b = b;
        System.out.println(this.b != null ? this.b.toJson() : null);
    }

    public void setC(C c) {
        this.c = c;
        System.out.println(this.c != null ? this.c.toJson() : null);
    }

    public void setL1(int fol1) {
        this.fol1 = fol1;
        System.out.println(this.fol1);
    }

    public void setL2(String fol2) {
        this.fol2 = fol2;
        System.out.println(this.fol2);
    }
}