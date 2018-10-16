package org.onosproject.sampleapp.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class B {

    private List<A> listaA = new ArrayList<>();

    private C c = null;

    private int fol1 = 0;

    private String fol2 = "test";

    public B(JsonNode jsonB) {

        JsonNode jsonList = jsonB.path("l");
        if (jsonList.isArray()) {
            for (JsonNode e : jsonList) {
                listaA.add(new A(e));
            }
        }
        this.c = !jsonB.path("c").isMissingNode() && !jsonB.path("c").isNull() ? new C(jsonB.path("c")) : null;
        this.fol1 = jsonB.path("l1").asInt(0);
        this.fol2 = jsonB.path("l2").textValue();
    }

    public B(String jsonStringB) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonB = mapper.readTree(jsonStringB);
            JsonNode jsonList = jsonB.path("l");
            if (jsonList.isArray()) {
                for (JsonNode e : jsonList) {
                    listaA.add(new A(mapper.writeValueAsString(e)));
                }
            }

            this.c = !jsonB.path("c").isMissingNode() && !jsonB.path("c").isNull() ? new C(mapper
                    .writeValueAsString(jsonB.path("c"))) : null;
            this.fol1 = jsonB.path("l1").asInt(0);
            this.fol2 = jsonB.path("l2").textValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public B() {}

    public JsonNode toJson() {

        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);

        json.put("l1", this.fol1);
        json.put("l2", this.fol2);

        json.putArray("l");
        for (A a : this.listaA) {
            ((ArrayNode) json.get("l")).add(a != null ? a.toJson() : null);
        }
        json.set("c", c != null ? this.c.toJson() : null);

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    public List<A> getL() {
        System.out.println(this.listaA);
        return listaA;
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

    public void setL(List<A> listaA) {
        this.listaA = listaA;
        System.out.println(this.listaA);
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