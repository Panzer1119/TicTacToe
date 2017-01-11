/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.panzercraft;

import de.panzercraft.objects.Field;
import jaddon.controller.JFrameManager;
import jaddon.controller.StaticStandard;
import jaddon.utils.JUtils;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * This TicTacToe game was programmed in only 1 hour
 * @author Paul Hagedorn
 * @version 11.01.2017
 */
public class TicTacToe implements ActionListener, WindowListener {
    
    public static final String VERSION = "11.01.2017";
    public static final String PROGRAMNAME = "TicTacToe";
    
    private final JFrameManager frame = new JFrameManager(PROGRAMNAME, VERSION);
    private final JMenuBar MB1 = new JMenuBar();
    private final JMenu M1 = new JMenu("File");
    private final JMenu M2 = new JMenu("Game");
    private final JMenuItem M1I1 = new JMenuItem("Exit");
    private final JMenuItem M1I2 = new JMenuItem("Restart");
    private final JMenuItem M2I1 = new JMenuItem("Reset");
    
    private final Field[][] fields = new Field[3][3];
    private int id_turn = -1;
    private boolean game_finished = false;
    private boolean xturn = true;
    
    public TicTacToe() {
        frame.setDefaultCloseOperation(JFrameManager.DO_NOTHING_ON_CLOSE);
        frame.setSize(new Dimension(600, 600));
        frame.setLayout(new GridLayout(3, 3));
        frame.addWindowListener(this);
        init();
        M1I1.addActionListener(this);
        M1I2.addActionListener(this);
        M2I1.addActionListener(this);
        M2I1.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        M1.add(M1I2);
        M1.add(M1I1);
        M2.add(M2I1);
        MB1.add(M1);
        MB1.add(M2);
        frame.setJMenuBar(MB1);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public void reset() {
        for(Field[] fields_ : fields) {
            for(Field field : fields_) {
                field.reset();
            }
        }
        frame.revalidate();
        frame.repaint();
        game_finished = false;
        xturn = false;
        checkFinish(0, 0);
        xturn = true;
    }
    
    public void init() {
        for(int i = 0; i < fields.length; i++) {
            for(int z = 0; z < fields[i].length; z++) {
                Field field = new Field();
                fields[i][z] = field;
                field.addActionListener(this);
                frame.add(field);
            }
        }
        reset();
        frame.setIconImage("/de/panzercraft/assets/images/Field_" + ((Math.random() >= 0.5) ? "O" : "X") + ".png");
    }
    
    public void checkFinish(int row, int col) {
        int count = 0;
        int count_fields = fields.length * fields[0].length;
        for(Field[] fields_ : fields) {
            for(Field field : fields_) {
                if(field.getState() != Field.CLEAR) {
                    count++;
                }
            }
        }
        int state = (xturn ? Field.X : Field.O);
        int state_opposite = (!xturn ? Field.X : Field.O);
        boolean draw = true;
        if(fields[row][0].getState() == state && fields[row][1].getState() == state && fields[row][2].getState() == state) {
            game_finished = true;
            draw = false;
        } else if(fields[0][col].getState() == state && fields[1][col].getState() == state && fields[2][col].getState() == state) {
            game_finished = true;
            draw = false;
        } else if(fields[0][0].getState() == state && fields[1][1].getState() == state && fields[2][2].getState() == state) {
            game_finished = true;
            draw = false;
        } else if(fields[0][2].getState() == state && fields[1][1].getState() == state && fields[2][0].getState() == state) {
            game_finished = true;
            draw = false;
        } else {
            if(count == count_fields) {
                if(!game_finished) {
                    draw = true;
                }
                game_finished = true;
            }
        }
        if(game_finished) {
            String extra = "";
            if(draw) {
                extra = " Its a draw!";
            } else {
                extra = " Player " + Field.getPlayer(state) + " has won!";
            }
            String complete = "Finished!" + extra;
            frame.delWork(id_turn);
            id_turn = frame.addWork(complete, false);
            JOptionPane.showMessageDialog(frame, complete);
        } else {
            frame.delWork(id_turn);
            id_turn = frame.addWork(Field.getPlayer(state_opposite) + "'s turn", false);
        }
    }
    
    public void exit() {
        StaticStandard.exit();
    }
    
    public static void main(String[] args) {
        TicTacToe x = new TicTacToe();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof Field) {
            if(game_finished) {
                return;
            }
            for(int i = 0; i < fields.length; i++) {
                for(int z = 0; z < fields[i].length; z++) {
                    Field field = fields[i][z];
                    if(e.getSource() == field) {
                        if(!game_finished) {
                            if(field.getState() == Field.CLEAR) {
                                field.setState((xturn) ? Field.X : Field.O);
                            }
                            checkFinish(i, z);
                            xturn = !xturn;
                        }
                        return;
                    }
                }
            }
        } else {
            if(e.getSource() == M1I1) {
                exit();
            } else if(e.getSource() == M1I2) {
                JUtils.restart();
            } else if(e.getSource() == M2I1) {
                reset();
            }
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exit();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
    
}
