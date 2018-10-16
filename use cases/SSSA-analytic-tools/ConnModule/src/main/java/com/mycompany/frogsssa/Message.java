/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.frogsssa;

import java.util.Random;

/**
 *
 * @author lara
 */
public class Message {
    private Long id;
    private String message;
    private Long idClient;

    public Message(String message, Long idClient) {
        id = (new Random()).nextLong();
        this.message = message;
        this.idClient = idClient;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Long getIdClient() {
        return idClient;
    }
    
    
}
