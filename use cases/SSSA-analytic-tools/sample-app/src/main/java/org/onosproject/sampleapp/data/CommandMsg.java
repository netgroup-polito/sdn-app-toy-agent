package org.onosproject.sampleapp.data;

class CommandMsg {
    Long id;
    String var;
    String objret;
    Object obj;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getObjret() {
        return objret;
    }

    public void setObjret(String objret) {
        this.objret = objret;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
