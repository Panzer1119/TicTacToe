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
public class FieldCoordinates implements Serializable {
    
    private final int row;
    private final int col;
    private final Action action;
    
    public static enum Action {
        ENTERED,
        EXITED
    }
    
    public FieldCoordinates(Field field, Action action) {
        this(field.getRow(), field.getCol(), action);
    }
    
    public FieldCoordinates(int row, int col, Action action) {
        this.row = row;
        this.col = col;
        this.action = action;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
    
    public Action getAction() {
        return action;
    }
    
}
