package org.onosproject.sampleapp.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {

    private int count = 0;
    private String subject = "";
    private String body = "";

    public Message(int count, String subject, String body) {
        this.count = count;
        this.subject = subject;
        this.body = body;
    }

    public Message() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
