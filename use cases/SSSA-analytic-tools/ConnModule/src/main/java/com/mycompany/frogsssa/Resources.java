/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.frogsssa;

import java.io.Serializable;
import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import org.glassfish.jersey.media.sse.EventOutput;

/**
 *
 * @author lara
 */
@Entity
@XmlRootElement
public class Resources implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private HashMap<String, String> correspondence = new HashMap<String, String>();
    private HashMap<String, Object> values = new HashMap<String, Object>();
    private String dataModel;
    
    public HashMap<String, String> getCorrespondence() {
        return correspondence;
    }

    public void setCorrespondence(HashMap<String, String> correspondence) {
        this.correspondence = correspondence;
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }
    

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Resources)) {
            return false;
        }
        Resources other = (Resources) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.frogsssa.Resources[ id=" + id + " ]";
    }
    
    public boolean setCorrespondence(String x, String c){
        if(correspondence.containsKey(x))
            return false;
        correspondence.put(x, c);
        return true;
    }
    
    public boolean setValue(String x, Object o){
        if(!correspondence.containsKey(x))
            return false;
        if(values.containsKey(x))
            values.remove(x);
        values.put(x, o);
        return true;
    }
    
    public String getCorrespondence(String id){
        if(correspondence.containsKey(id))
            return correspondence.get(id);
        return null;
    }
    
    public Object getValue(String id){
        if(values.containsKey(id))
            return values.get(id);
        return null;
    }
}
