/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package StateListener;

/**
 *
 * @author lara
 */
class CommandMsg {
    Long id;
    command act;
    String var;
    String objret;
    Object obj;
    Double time;
    public enum command{GET, CONFIG, DELETE};
    
    public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public command getAct() {
    return act;
}

public void setAct(command act) {
    this.act = act;
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

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }
}
