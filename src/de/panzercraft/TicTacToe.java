/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.panzercraft;

import de.panzercraft.objects.Field;
import de.panzercraft.objects.Message;
import de.panzercraft.objects.Move;
import jaddon.controller.JFrameManager;
import jaddon.controller.StaticStandard;
import jaddon.dialog.JWaitingDialog;
import jaddon.net.Client;
import jaddon.net.InputProcessor;
import jaddon.net.Server;
import jaddon.utils.JUtils;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * This TicTacToe game was programmed in only 1 hour
 * @author Paul Hagedorn
 * @version 12.01.2017
 */
public class TicTacToe implements ActionListener, WindowListener {
    
    public static final String VERSION = "12.01.2017";
    public static final String PROGRAMNAME = "TicTacToe";
    
    public static final int STANDARDPORT = 4533;
    
    public static final int NONE = -1;
    public static final int HOST = 0;
    public static final int SLAVE = 1;
    
    private final TicTacToe tictactoe = this;
    
    private final JFrameManager frame = new JFrameManager(PROGRAMNAME, VERSION);
    private final JMenuBar MB1 = new JMenuBar();
    private final JMenu M1 = new JMenu("File");
    private final JMenu M2 = new JMenu("Game");
    private final JMenuItem M1I1 = new JMenuItem("Exit");
    private final JMenuItem M1I2 = new JMenuItem("Restart");
    private final JMenuItem M2I1 = new JMenuItem("Reset");
    private final JMenuItem M2I2 = new JMenuItem("Undo");
    private final JMenuItem M2I3 = new JMenuItem("Redo");
    private final JMenuItem M2I4 = new JMenuItem("Host");
    private final JMenuItem M2I5 = new JMenuItem("Join");
    private final JMenuItem M2I6 = new JMenuItem("Rejoin");
    
    private final Field[][] fields = new Field[3][3];
    private int id_turn = -1;
    private boolean game_finished = false;
    private boolean xturn = true;
    private int move_number = 0;
    private final ArrayList<Move> moves = new ArrayList<>();
    private int cpu = Field.CLEAR;
    private int multiplayer_state = NONE;
    private final Timer timer = new Timer(100, this);
    private String last_host = null;
    
    private Server server = new Server(STANDARDPORT);
    private Client client_logged_in = null;
    private Client client = null;
    
    public TicTacToe() {
        frame.setDefaultCloseOperation(JFrameManager.DO_NOTHING_ON_CLOSE);
        frame.setSize(new Dimension(600, 600));
        frame.setLayout(new GridLayout(3, 3));
        frame.addWindowListener(this);
        init();
        M1I1.addActionListener(this);
        M1I2.addActionListener(this);
        M2I1.addActionListener(this);
        M2I2.addActionListener(this);
        M2I3.addActionListener(this);
        M2I4.addActionListener(this);
        M2I5.addActionListener(this);
        M2I6.addActionListener(this);
        M2I1.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        M2I2.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        M2I3.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
        M1.add(M1I2);
        M1.add(M1I1);
        M2.add(M2I2);
        M2.add(M2I3);
        M2.add(M2I1);
        M2.add(new JSeparator());
        M2.add(M2I4);
        M2.add(M2I5);
        M2.add(M2I6);
        MB1.add(M1);
        MB1.add(M2);
        frame.setJMenuBar(MB1);
        frame.pack();
        frame.setLocationRelativeTo(null);
        timer.start();
        frame.setVisible(true);
    }
    
    private void startServer() {
        try {
            server.startWThread().join();
        } catch (Exception ex) {
        }
    }
    
    private void stopServer() {
        try {
            server.stop();
            cpu = Field.CLEAR;
            client_logged_in = null;
            if(multiplayer_state == HOST) {
                multiplayer_state = NONE;
            }
        } catch (Exception ex) {
        }
    }
    
    private void startClient(String host) {
        try {
            client = new Client(InetAddress.getByName(host), STANDARDPORT);
            client.setInputProcessor(INPUTPROCESSORCLIENT);
            client.setReconnectAfterConnectionLoss(true);
            client.startWThread().join();
        } catch (Exception ex) {
        }
    }
    
    private void stopClient() {
        try {
            client.stop();
            client = null;
            cpu = Field.CLEAR;
            if(multiplayer_state == SLAVE) {
                multiplayer_state = NONE;
            }
        } catch (Exception ex) {
        }
    }
    
    private void stopMultiplayer() {
        stopServer();
        stopClient();
        multiplayer_state = NONE;
    }
    
    private void hostWThread() {
        Runnable run = new Runnable() {
          
            @Override
            public void run() {
                host();
            }
            
        };
        StaticStandard.execute(run);
    }
    
    private void host() {
        reset();
        multiplayer_state = HOST;
        cpu = Field.CLEAR;
        final JWaitingDialog wd = new JWaitingDialog(frame, "Waiting for player", "Host");
        Runnable run = new Runnable() {
          
            @Override
            public void run() {
                boolean wdstarted = false;
                while(cpu == Field.CLEAR && ((wdstarted) ? wd.isRunning() : true)) {
                    if(wd.isRunning()) {
                        wdstarted = true;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception ex) {
                    }
                }
                wd.close();
            }
            
        };
        StaticStandard.execute(run);
        startServer();
        int result = wd.showWaitingDialog();
        if(result != JWaitingDialog.STOPPED_OPTION) {
            multiplayer_state = NONE;
            stopServer();
        } else {
            StaticStandard.log("Found player");
        }
    }
    
    private void joinWThread() {
        Runnable run = new Runnable() {
          
            @Override
            public void run() {
                join();
            }
            
        };
        StaticStandard.execute(run);
    }
    
    private void joinWThread(String host) {
        Runnable run = new Runnable() {
          
            @Override
            public void run() {
                join(host);
            }
            
        };
        StaticStandard.execute(run);
    }
    
    private void join() {
        String host = JOptionPane.showInputDialog(frame, "IP:", "Join", JOptionPane.QUESTION_MESSAGE);
        join(host);
    }
    
    private void join(String host) {
        reset();
        multiplayer_state = SLAVE;
        cpu = Field.CLEAR;
        if(host != null && !host.isEmpty()) {
            final JWaitingDialog wd = new JWaitingDialog(frame, "Waiting for server", "Join");
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    boolean wdstarted = false;
                    while(cpu == Field.CLEAR && ((wdstarted) ? wd.isRunning() : true)) {
                        if(wd.isRunning()) {
                            wdstarted = true;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (Exception ex) {
                        }
                    }
                    wd.close();
                }

            };
            StaticStandard.execute(run);
            startClient(host);
            int result = wd.showWaitingDialog();
            if(result != JWaitingDialog.STOPPED_OPTION) {
                multiplayer_state = NONE;
                stopClient();
            } else {
                last_host = host;
                StaticStandard.log("Found server");
            }
        }
    }
    
    public void reset() {
        stopMultiplayer();
        resetGame();
    }
    
    public void resetGame() {
        for(Field[] fields_ : fields) {
            for(Field field : fields_) {
                field.reset();
            }
        }
        frame.revalidate();
        frame.repaint();
        game_finished = false;
        move_number = 0;
        cpu = Field.CLEAR;
        moves.clear();
        xturn = false;
        checkFinish();
        xturn = true;
    }
    
    public void init() {
        server.setInputProcessor(INPUTPROCESSORSERVER);
        for(int i = 0; i < fields.length; i++) {
            for(int z = 0; z < fields[i].length; z++) {
                Field field = new Field();
                fields[i][z] = field;
                field.addActionListener(this);
                frame.add(field);
            }
        }
        setIconImage(((Math.random() >= 0.5) ? Field.O : Field.X));
        reset();
    }
    
    private void setIconImage(int player) {
        frame.setIconImage("/de/panzercraft/assets/images/Field_" + Field.getPlayer(player) + ".png");
    }
    
    private void switchPlayer() {
        xturn = !xturn;
    }
    
    public void undo() {
        if(!canUndo()) {
            return;
        }
        try {
            move_number--;
            Move move = moves.get(move_number);
            doMove(move, true);
            StaticStandard.log("Undid: \"" + move + "\"");
            checkFinish();
            switchPlayer();
        } catch (Exception ex) {
            StaticStandard.logErr("Error while undoing a move: " + ex, ex);
        }
    }
    
    public void redo() {
        if(!canRedo()) {
            return;
        }
        try {
            Move move = moves.get(move_number);
            move_number++;
            doMove(move, false);
            StaticStandard.log("Redid: \"" + move + "\"");
            checkFinish();
            switchPlayer();
        } catch (Exception ex) {
            StaticStandard.logErr("Error while redoing a move: " + ex, ex);
        }
    }
    
    public boolean canUndo() {
        return !(game_finished || moves.isEmpty() || move_number == 0) && (multiplayer_state == NONE);
    }
    
    public boolean canRedo() {
        return !(game_finished || moves.isEmpty() || move_number >= moves.size()) && (multiplayer_state == NONE);
    }
    
    public void checkFinish() {
        int count = 0;
        int count_fields = fields.length * fields[0].length;
        for(Field[] fields_ : fields) {
            for(Field field : fields_) {
                if(field.getState() != Field.CLEAR) {
                    count++;
                }
            }
        }
        int state = getPlayerActive();
        int state_opposite = getPlayerInactive();
        boolean draw = true;
        if(fields[0][0].getState() == state && fields[1][1].getState() == state && fields[2][2].getState() == state) {
            game_finished = true;
            draw = false;
        } else if(fields[0][2].getState() == state && fields[1][1].getState() == state && fields[2][0].getState() == state) {
            game_finished = true;
            draw = false;
        } else {
            for(int row = 0; row < fields.length; row++) {
                if(fields[row][0].getState() == state && fields[row][1].getState() == state && fields[row][2].getState() == state) {
                    game_finished = true;
                    draw = false;
                }
            }
            for(int col = 0; col < fields[0].length; col++) {
                if(fields[0][col].getState() == state && fields[1][col].getState() == state && fields[2][col].getState() == state) {
                    game_finished = true;
                    draw = false;
                }
            }
        }
        if(count == count_fields) {
            if(!game_finished) {
                draw = true;
            }
            game_finished = true;
        }
        frame.revalidate();
        frame.repaint();
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
            stopMultiplayer();
        } else {
            frame.delWork(id_turn);
            id_turn = frame.addWork(Field.getPlayer(state_opposite) + "'s turn", false);
        }
    }
    
    private boolean doMove(int row, int col, int player) {
        try {
            Field field = fields[row][col];
            if(field.getState() == Field.CLEAR) {
                field.setState(player);
                Move move = new Move(row, col, player, move_number);
                while(move_number < moves.size()) {
                    moves.remove(move_number);
                }
                moves.add(move);
                StaticStandard.log(move);
                move_number++;
                if(multiplayer_state != NONE && cpu != player) {
                    if(multiplayer_state == HOST) {
                        client_logged_in.send(move);
                    } else if(multiplayer_state == SLAVE) {
                        client.send(move);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }
    
    private boolean doMove(Move move, boolean invert) {
        try {
            Field field = fields[move.getRow()][move.getCol()];
            field.setState((invert ? Field.CLEAR : move.getPlayer()));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    private Message rollOut() {
        Message message = new Message();
        boolean isHostX = Math.random() >= 0.5;
        if(isHostX) {
            message.host = Field.X;
            message.slave = Field.O;
        } else {
            message.host = Field.X;
            message.slave = Field.O;
        }
        return message;
    }
    
    private void setDo(boolean show_undo, boolean show_redo) {
        M2I2.setEnabled(show_undo);
        M2I3.setEnabled(show_redo);
    }
    
    public void exit() {
        StaticStandard.exit();
    }
    
    public static void main(String[] args) {
        TicTacToe x = new TicTacToe();
    }
    
    public int getPlayerActive() {
        return (xturn ? Field.X : Field.O);
    }
    
    public int getPlayerInactive() {
        return (!xturn ? Field.X : Field.O);
    }
    
    private void checkMultiplayer() {
        M2I4.setEnabled(multiplayer_state == NONE);
        M2I5.setEnabled(multiplayer_state == NONE);
        M2I6.setEnabled((multiplayer_state == NONE) && (last_host != null && !last_host.isEmpty()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof Field) {
            if(game_finished || (cpu != Field.CLEAR && cpu == getPlayerActive())) {
                return;
            }
            for(int i = 0; i < fields.length; i++) {
                for(int z = 0; z < fields[i].length; z++) {
                    Field field = fields[i][z];
                    if(e.getSource() == field) {
                        if(!game_finished) {
                            boolean valid = doMove(i, z, getPlayerActive());
                            if(valid) {
                                checkFinish();
                                switchPlayer();
                            }
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
            } else if(e.getSource() == M2I2) {
                undo();
            } else if(e.getSource() == M2I3) {
                redo();
            } else if(e.getSource() == M2I4) {
                hostWThread();
            } else if(e.getSource() == M2I5) {
                joinWThread();
            } else if(e.getSource() == timer) {
                checkMultiplayer();
                setDo(canUndo(), canRedo());
            } else if(e.getSource() == M2I6) {
                if(last_host != null && !last_host.isEmpty()) {
                    joinWThread(last_host);
                }
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
    
    public final InputProcessor INPUTPROCESSORSERVER = new InputProcessor() {
        
        @Override
        public void processInput(Object object, Instant timestamp) {
            this.processInput(object, null, timestamp);
        }

        @Override
        public void processInput(Object object, Client client, Instant timestamp) {
            StaticStandard.log(String.format("[SERVER] [%s]: \"%s\"", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), object));
            if(object instanceof Move) {
                Move move = (Move) object;
                if(!game_finished) {
                    boolean valid = tictactoe.doMove(move.row, move.col, move.player);
                    if(valid) {
                        checkFinish();
                        switchPlayer();
                    }
                }
            } else if(object instanceof Message) {
                Message message = (Message) object;
                cpu = message.slave;
                StaticStandard.logErr("Setted CPU: " + cpu);
                setIconImage(message.host);
                xturn = false;
                checkFinish();
                xturn = true;
            }
        }

        @Override
        public void clientLoggedIn(Client client, Instant timestamp) {
            StaticStandard.log(String.format("[SERVER] [%s]: Client \"%s\" logged in", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), client.getInetaddress().getHostAddress()));
            client_logged_in = client;
            client.send(rollOut());
        }

        @Override
        public void clientLoggedOut(Client client, Instant timestamp) {
            StaticStandard.log(String.format("[SERVER] [%s]: Client \"%s\" logged out", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), client.getInetaddress().getHostAddress()));
        }
        
    };
    
    public final InputProcessor INPUTPROCESSORCLIENT = new InputProcessor() {
        
        @Override
        public void processInput(Object object, Instant timestamp) {            
            if(object instanceof Move) {
                Move move = (Move) object;
                if(!game_finished) {
                    boolean valid = tictactoe.doMove(move.row, move.col, move.player);
                    if(valid) {
                        checkFinish();
                        switchPlayer();
                    }
                }
            } else if(object instanceof Message) {
                Message message = (Message) object;
                cpu = message.host;
                StaticStandard.logErr("Setted CPU: " + cpu);
                setIconImage(message.slave);
                xturn = false;
                client.send(message);
                checkFinish();
                xturn = true;
            }
            StaticStandard.log(String.format("[CLIENT] [%s]: \"%s\"", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), object));
        }

        @Override
        public void processInput(Object object, Client client, Instant timestamp) {
            StaticStandard.log(String.format("[CLIENT] [%s] [%s]: \"%s\"", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), ((client != null) ? client.getInetaddress().getHostAddress() : ""), object));
        }

        @Override
        public void clientLoggedIn(Client client, Instant timestamp) {
            StaticStandard.log(String.format("[CLIENT] [%s]: Client \"%s\" logged in", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), client.getInetaddress().getHostAddress()));
        }

        @Override
        public void clientLoggedOut(Client client, Instant timestamp) {
            StaticStandard.log(String.format("[CLIENT] [%s]: Client \"%s\" logged out", LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), client.getInetaddress().getHostAddress()));
        }
        
    };
    
}
