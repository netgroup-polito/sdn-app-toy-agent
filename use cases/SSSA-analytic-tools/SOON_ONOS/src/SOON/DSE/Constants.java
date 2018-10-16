/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DSE;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ubuntu
 */
public class Constants {
    
    
    public static ConcurrentHashMap<String, ArrayList<String>> active_intents_per_request = new ConcurrentHashMap<>();//two different directions
    
    public static ConcurrentHashMap<String, ArrayList<String>> active_flows_per_request = new ConcurrentHashMap<>(); //two different directions
    
    public static ConcurrentHashMap<String, ArrayList<String>> involved_switches_per_request = new ConcurrentHashMap<>();
    
    public static volatile boolean test;
    
}
