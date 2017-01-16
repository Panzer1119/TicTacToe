/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.panzercraft.objects;

import java.io.Serializable;

/**
 *
 * @author Paul
 */
public class Message implements Serializable {
    
    public int host = -1;
    public int slave = -1;
    
    public Message() {
        
    }
    
}
