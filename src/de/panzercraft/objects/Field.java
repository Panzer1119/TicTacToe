/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.panzercraft.objects;

import jaddon.icons.IconPlus;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JButton;

/**
 *
 * @author Paul
 */
public class Field extends JButton {
    
    private static final Image IMAGECLEAR = IconPlus.getImage("/de/panzercraft/assets/images/Field_Clear.png");
    private static final Image IMAGEO = IconPlus.getImage("/de/panzercraft/assets/images/Field_O.png");
    private static final Image IMAGEX = IconPlus.getImage("/de/panzercraft/assets/images/Field_X.png");
    
    public static final int CLEAR = 0;
    public static final int O = 1;
    public static final int X = 2;
    
    private Image image = null;
    private int state = -1;
    
    public Field() {
        super();
        setState(CLEAR);
    }
    
    public void reset() {
        setState(CLEAR);
    }
    
    public Field setState(int state) {
        switch(state) {
            case CLEAR:
                image = IMAGECLEAR;
                break;
            case O:
                image = IMAGEO;
                break;
            case X:
                image = IMAGEX;
                break;
            default:
                image = IMAGECLEAR;
                break;
        }
        this.state = state;
        return this;
    }
    
    public int getState() {
        return state;
    }
    
    public static String getPlayer(int state) {
        switch(state) {
            case CLEAR:
                return "CLEAR";
            case O:
                return "O";
            case X:
                return "X";
            default:
                return "DRAW";
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null, null);
    }
    
}
