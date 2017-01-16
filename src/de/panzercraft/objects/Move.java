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
public class Move implements Serializable {
    
    public final int row;
    public final int col;
    public final int player;
    public final int number;
    
    public Move(int row, int col, int player, int number) {
        this.row = row;
        this.col = col;
        this.player = player;
        this.number = number;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getPlayer() {
        return player;
    }
    
    public int getNumber() {
        return number;
    }
    
    @Override
    public String toString() {
        return String.format("Player %s took the field %d:%d at the %d move", Field.getPlayer(player), row, col, number);
    }
    
}
