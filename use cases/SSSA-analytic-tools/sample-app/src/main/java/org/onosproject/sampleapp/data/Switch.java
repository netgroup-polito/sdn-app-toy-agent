package org.onosproject.sampleapp.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Switch {

    private long id;
    private String ip;
    private String mac;

    public Switch(JsonNode jsonLoad) {

        this.id = jsonLoad.path("id").asLong(0);
        this.ip = jsonLoad.path("ip").asText("0.0.0.0");
        this.mac = jsonLoad.path("mac").asText("00:00:00:00:00:00");
    }

    public Switch(long id, String ip, String mac) {
        this.id = id;
        this.ip = ip;
        this.mac = mac;
    }

    public Switch() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public JsonNode toJson() {

        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);

        json.put("id", this.id);
        json.put("ip", this.ip);
        json.put("mac", this.mac);

        return json;
    }
}
