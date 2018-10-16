/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.frogsssa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.mycompany.frogsssa.Resources;
import com.mycompany.frogsssa.service.CommandMsg.command;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import com.mycompany.frogsssa.testDD;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;


/**
 *
 * @author lara
 */
@Stateless
@Path("ConnectionModule")
public class ConnectionModule{

    @PersistenceContext(unitName = "com.mycompany_frogsssa_war_1.0-SNAPSHOTPU")
    private EntityManager em;
    private static HashMap<String, String> resDM = new HashMap<String, String>();
    private static HashMap<String, EventOutput> SSEClients = new HashMap<>();
    private static HashMap<String, testDD> DDClients = new HashMap<>();
    private static testDD dd = new testDD("tcp://127.0.0.1:5555", ConnectionModule.class.getClassLoader().getResource("files/keys.json").getPath(), "ConnectionModule"+(new Long((new Random()).nextLong())).toString(), "connMod");
    private static sseResource conn = new sseResource();
    private static HashMap<Long, JsonNode> resToServiceLayers = new HashMap();
    private static HashMap<Long, Double> resTime = new HashMap();

    public enum action{ADDED, UPDATED, DELETED, NOCHANGES, PERIODIC};
    public class NotifyMsg{
        public action act;
        public Object obj;
        public String var;
        public Date timestamp;
    }
    
    public ConnectionModule() {
//        final ResourceConfig config = new ResourceConfig();
//        config.register(SseFeature.class);
    }
    
    public static boolean addEndpoint(EventOutput se, String id){
        if(!SSEClients.containsKey(id)){
            SSEClients.put(id, se);
	}else{
	    SSEClients.replace(id, se);
	}
	if(SSEClients.get(id) == se)
	    return true;
	return false;
    }
    
    public static void deleteEndpoint(String id){
        if(SSEClients.containsKey(id))
            SSEClients.remove(id);
    }
    
    public static EventOutput getEndpoint(String id){
        return SSEClients.get(id);
    }

//    public static void messageReceived(Long id, String m){
//        System.out.println("Message received from " + id + " : " + m);
//    }
//    
//    private Resources findR(String id){
//        if(resDM.containsKey(id))
//            return resDM.get(id);
//        return null;
//    }
    
    public JsonNode getValue(String AppId, String var){
        if(!SSEClients.containsKey(AppId))
            return null;
        CommandMsg msg = new CommandMsg();
        msg.act = command.GET;
        msg.var = var;
        Long v = (new Random()).nextLong();
        msg.id = v;
        SendData(AppId, (new Gson()).toJson(msg));
        synchronized(resToServiceLayers){
        while(!resToServiceLayers.containsKey(v))
            try{resToServiceLayers.wait();} catch (InterruptedException ex) {
                Logger.getLogger(ConnectionModule.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        JsonNode ret = new ObjectNode(JsonNodeFactory.instance);
        ((ObjectNode) ret).set("res", resToServiceLayers.get(v));
        Double time = resTime.get(v);
        resToServiceLayers.remove(v);
        resTime.remove(v);
        ((ObjectNode) ret).put("time", time);
        return ret;
    }
    
    public static JsonNode configVar(String id, String var, String json){
        JsonNode ret = new ObjectNode(JsonNodeFactory.instance);
        if(SSEClients.containsKey(id)){
            CommandMsg msg = new CommandMsg();
            msg.act=command.CONFIG;
            msg.var=var;
            msg.obj=json;
            Long v = (new Random()).nextLong();
            msg.id = v;
            SendData(id, (new Gson()).toJson(msg));
            synchronized(resToServiceLayers){
            while(!resToServiceLayers.containsKey(v))
                try{resToServiceLayers.wait();} catch (InterruptedException ex) {
                    Logger.getLogger(ConnectionModule.class.getName()).log(Level.SEVERE, null, ex);
                    ((ObjectNode) ret).put("res", 3);
                    return ret;
                }
            }

            ((ObjectNode) ret).put("res", resToServiceLayers.get(v).asInt());
            ((ObjectNode) ret).put("time", resTime.get(v));
            resToServiceLayers.remove(v);
            resTime.remove(v);
            return ret;
            }
        ((ObjectNode) ret).put("res", 4);
        return ret;
    }
    
    public static int deleteVar(String id, String var){
        if(SSEClients.containsKey(id)){
            CommandMsg msg = new CommandMsg();
            Long v = (new Random()).nextLong();
            msg.id = v;
            msg.act = command.DELETE;
            msg.var = var;
            SendData(id, (new Gson()).toJson(msg));
            synchronized(resToServiceLayers){
            while(!resToServiceLayers.containsKey(v))
                try{resToServiceLayers.wait();} catch (InterruptedException ex) {
                    Logger.getLogger(ConnectionModule.class.getName()).log(Level.SEVERE, null, ex);
                    return 3;
                }
            }
            Integer ret = resToServiceLayers.get(v).asInt();
            resToServiceLayers.remove(v);
            return ret;
        }
        return 4;
    }
    
    @Path("events/{id}")
    public static class sseResource{
        final EventOutput evOut = new EventOutput();
        
//        private static Resources findR(String id){
//        for(int i = 0; i < res.size(); i++)
//            if(id==res.get(i).getId())
//                return res.get(i);
//        return null;
//    }
        
        @GET
        @Produces(SseFeature.SERVER_SENT_EVENTS)
        public EventOutput getServerSentEvents(@PathParam("id") final String id){
            new Thread(new Runnable() {
                public void run() {
                    final OutboundEvent.Builder builder = new OutboundEvent.Builder();
                }
            }).start();
            if(ConnectionModule.addEndpoint(evOut, id))
            //writeEvents(id);
	            sendMessage(id, "messaggio iniziale");
            return evOut;
        }
        
        
        public static void writeEvents(final String id){
            new Thread(new Runnable() {
                public void run() {
                    try{
                        EventOutput e = ConnectionModule.getEndpoint(id);
                        for(int i=0;i<10;i++){
                            final OutboundEvent.Builder builder = new OutboundEvent.Builder();
                            builder.name("message-to-client");
                            builder.data(String.class, "Events " + i);
                            final OutboundEvent event = builder.build();
                            e.write(event);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ConnectionModule.class.getName()).log(Level.SEVERE, null, ex);
                        throw new RuntimeException("Exception in write " + ex);
                    }
                }
            }).start();
        }
        
        public static void sendMessage(final String id, final String message){
                final EventOutput e = ConnectionModule.getEndpoint(id);
                if(e!=null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final OutboundEvent.Builder builder = new OutboundEvent.Builder();
                                builder.name("message-to-client");
                                builder.data(String.class, message);
                                final OutboundEvent event = builder.build();
                                e.write(event);
                            } catch (IOException ex) {
                                Logger.getLogger(ConnectionModule.class.getName()).log(Level.SEVERE, null, ex);
                                System.err.println("Error while sending the message " + message +  "to the client " + id);
                                ConnectionModule.deleteEndpoint(id);
                                new RuntimeException("Error while sending the message " + message +  "to the client " + id);
                            }
                        }
                    }).start();
                }
            }
        
    }
    
    @Path("{id}/response")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void getResponse(@PathParam("id") String id, String msgJson) throws IOException{
        CommandMsg msg = (new Gson()).fromJson(msgJson, CommandMsg.class);
        synchronized(resToServiceLayers){
        resToServiceLayers.put(msg.id, (new ObjectMapper()).readTree(msg.objret));
        resTime.put(msg.id, msg.time);
        resToServiceLayers.notifyAll();}
    }
    
    private static boolean SendData(String id, String message){
        final String ident = id;
        final String m = message;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                final OutboundEvent.Builder builder = new OutboundEvent.Builder();
                builder.name("sse sever - client");
                builder.data(String.class, m);
                final OutboundEvent event = builder.build();
                if(SSEClients.containsKey(ident))
                {
                    EventOutput e = SSEClients.get(ident);
                    if(e!=null)
                        e.write(event);
                }
            } catch (IOException ex) {
                Logger.getLogger(ConnectionModule.class.getName()).log(Level.SEVERE, null, ex);
            }            
            }
        });
        t.start();
//        if(SEClients.containsKey(id))
//            SEClients.get(id).sendMessage(message);
        if (t.isInterrupted())
            return false;
        return true;
    }
    
    @POST
    @Path("{id}/dataModel")
    @Consumes(MediaType.TEXT_PLAIN)
    //@Produces(MediaType.TEXT_PLAIN)
    public void DataModel(@PathParam("id") String id, String DM){
        //from xml to yang?
        if(!resDM.containsKey(id))
            resDM.put(id, DM);
        else
            resDM.replace(id, DM);
        System.out.println("Setted DM for "+id+" "+DM);
        dd.publish(id+"/yang", DM);
        //return result;
    }
    
    @POST
    @Path("create")
    @Consumes(MediaType.TEXT_PLAIN)
    public void create(@Context UriInfo uriInfo, String id) {
//        Resources entity = new Resources();
//        //Long id = (new Random()).nextLong();
//        entity.setId(id);
//        //create resources entrance
        resDM.put(id, null);
        URI ad = uriInfo.getBaseUri();
        dd.publish(id+"/restServer", ad.toString());     
        //SSE      
        return;
    }
    
    @GET
    @Path("address")
    @Produces(MediaType.TEXT_PLAIN)
    public String address(@Context UriInfo uriInfo){
        return uriInfo.getBaseUri().toString();
    }
    
    public static void someConfiguration(String id, String msg){
        if(SSEClients.containsKey(id)){
            SendData(id, msg);
        }
    }
    
    
    @POST
    @Path("{id}/change")
    @Consumes(MediaType.APPLICATION_JSON)
    public void somethingChanged(@PathParam("id") String id, String m){
        NotifyMsg msg = ((new Gson()).fromJson(m, NotifyMsg.class));
        String topic = id+"/"+msg.var.replaceAll(Pattern.quote("."), "/");
        if(msg.obj instanceof Number){
            msg.obj = ((Number)msg.obj).toString();
        }
        m = (msg.act==action.DELETED)?("{\"event\":"+msg.act+",\n\"timestamp\":"+msg.timestamp+"}"):("{\"event\":"+msg.act+",\n\"timestamp\":"+msg.timestamp+",\n\"data\":"+msg.obj+"}");
//        testDD d1 = new testDD("tcp://127.0.0.1:5555", "/home/lara/GIT/DoubleDecker/keys/a-keys.json", (new Long((new Random()).nextLong())).toString(), "connMod");
        System.out.println("dd "+dd.status());
        dd.publish(topic, m);
        //d1.quit();
    }
    

    /*@PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Long id, Resources entity) {
        super.edit(entity);
    }*/

//// don't use    
//    @POST
//    @Path("{id}/{x}/DM")
//    @Consumes(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.TEXT_PLAIN)
//    public String Correspondence(@PathParam("id") final String id, @PathParam("x") final String x, String c){
//       Resources r = findR(id);
//       if(r==null)
//           Boolean.toString(false);
//       boolean result = r.setCorrespondence(x,c);
//       //sseResource.sendMessage(id, "Modified DM of " + id);
//       new Thread(new Runnable() {
//           @Override
//           public void run() {
//               SendData(id, "corrispondenza per " + x + " settata");
//           }
//       }).start();
//       return Boolean.toString(result);
//    }
 
////don't use
//    @POST
//    @Path("{id}/{x}/Value")
//    @Consumes(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.TEXT_PLAIN)
//    public String Value(@PathParam("id") String id, @PathParam("x") String x, String o){
//       Resources r = findR(id);
//       if(r==null)
//           return Boolean.toString(false);
//       //from string to object
//       return Boolean.toString(r.setValue(x,o));
//    }
    
    
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        if(resDM.containsKey(id))
            resDM.remove(id);
        SSEClients.remove(id);
        //DDClients.remove(id);
    }

//    @GET
//    @Path("{id}")
//    @Produces(MediaType.APPLICATION_XML)
//    public Resources find(@PathParam("id") String id) {
//        sseResource.sendMessage(id, "ricevo id");
//        for(int i=0;i<res.size();i++){
//            Long idConf = res.get(i).getId();
//            if(idConf.longValue()==id.longValue())
//                return res.get(i);
//        }
//        return null;
//    }

    
    public static String getYang(String id){
        if(resDM.containsKey(id))
            return resDM.get(id);
        return null;
    }

//    //methods to be accessed by serviceLayerService
//    public Resources getRes(Long id){
//        if(res.containsKey(id))
//            return res.get(id);
//        else return null;
//    }
//    
//    public static Object getResVariable(Long id, String var){
//        if(res.containsKey(id)){
//            Resources r = res.get(id);
//            return r.getValue(var);
//        }
//        return null;
//    }
}

