package org.onosproject.sampleapp.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Load {

    private long id;
    private int tp;

    public Load(JsonNode jsonLoad) {

        this.id = jsonLoad.path("id").asLong(0);
        this.tp = jsonLoad.path("l2").asInt(0);
    }

    public Load(long id, int tp) {
        this.id = id;
        this.tp = tp;
    }

    public Load() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    public JsonNode toJson() {

        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);

        json.put("id", this.id);
        json.put("tp", this.tp);

        return json;
    }
}
