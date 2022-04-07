/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * The main viewing window for the simulation.
 * @author Joseph Cramsey
 */
public class MainWindow implements ActionListener, ItemListener {
    
    public static MainWindow SINGLETON;
    public static final int MIN_WIDTH = 640;
    public static final int MIN_HEIGHT = 400;
    
    private JFrame frame;
    private JPanel layoutPanel;
    private NetworkPanel networkPanel = null;
    private JScrollPane networkScroll;
    private NetworkMap map;
    private boolean readyForEval = false;
    private long lastSeed = 0;
    private boolean firstPack = false;
    
    private JMenuItem newGameButton;
    private JMenuItem exitButton;
    
    public static void main(String[] args) {
        SINGLETON = new MainWindow();
    }
    
    public MainWindow() {
        frame = new JFrame("Network Automata");
        MainWindow _this = this;
        SwingUtilities.invokeLater(() -> {
            networkScroll = new JScrollPane() {
                @Override
                public Dimension getPreferredSize() {
                    Component view = networkScroll.getViewport().getView();
                    if (view == null) {
                        return new Dimension(
                                MIN_WIDTH - HelpPanel.MIN_WIDTH, MIN_HEIGHT
                        );
                    }
                    return new Dimension(
                            view.getPreferredSize().width + 64, MIN_HEIGHT
                    );
                }
                
                @Override
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
            networkScroll.getVerticalScrollBar().setUnitIncrement(64);
            layoutPanel = new JPanel();
            layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.X_AXIS));
            layoutPanel.setBackground(Color.DARK_GRAY);
            layoutPanel.add(Box.createHorizontalGlue());
            layoutPanel.add(networkScroll);
            layoutPanel.add(HelpPanel.createHelpPanel());
            frame.setContentPane(layoutPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(MIN_WIDTH, MIN_HEIGHT);
            
            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("Game");
            menu.setMnemonic(KeyEvent.VK_G);
            menu.getAccessibleContext().setAccessibleDescription(
                    "Access game commands"
            );
            menuBar.add(menu);

            newGameButton = new JMenuItem("New Game", KeyEvent.VK_N);
            newGameButton.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_N, ActionEvent.CTRL_MASK));
            newGameButton.getAccessibleContext().setAccessibleDescription(
                    "Generate a new game"
            );
            newGameButton.addActionListener(_this);
            menu.add(newGameButton);
            
            exitButton = new JMenuItem("Exit", KeyEvent.VK_X);
            exitButton.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_F4, ActionEvent.ALT_MASK));
            exitButton.getAccessibleContext().setAccessibleDescription(
                    "Exit the game"
            );
            exitButton.addActionListener(_this);
            menu.add(exitButton);
            
            menuBar.add(menu);
            frame.setJMenuBar(menuBar);
            
            frame.setResizable(true);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            readyForEval = true;
        });
        
        try {
            while (!readyForEval) Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Random firstRand = new Random();
        lastSeed = firstRand.nextInt(100000);
        generateNewPuzzle();
    }
    
    private void generateNewPuzzle() {
        boolean hasUnusedInputs = false;
        boolean isIllegal = false;
        boolean isSafe;
        int outputCount = 0;
        boolean hasGoodOutputs;
        int vulnCount = 0;
        boolean isTooEasy = false;
        long seed = 0;
        long maxSeed = lastSeed + (long)65536;
        //boolean found = false;
        for (long i = lastSeed; i <= maxSeed; i++) {
            seed = i;
            map = new NetworkMap(seed);
            hasUnusedInputs = map.hasUnusedInputs();
            if (hasUnusedInputs) continue;
            isIllegal = !map.isLegal();
            if (isIllegal) continue;
            outputCount = map.getOutputCount();
            hasGoodOutputs = map.hasGoodOutputs(outputCount);
            if (!hasGoodOutputs) continue;
            isTooEasy = map.isTooEasy();
            if (isTooEasy) continue;
            isSafe = map.isSafe();
            if (isSafe) {
                vulnCount = 0;
            }
            else {
                vulnCount = map.getVulnerabilityCount();
            }
            if (vulnCount <= 8 && vulnCount > 4) {
                //found = true;
                break;
            }
        }
        /*if (found) System.out.println("FOUND!!");
        System.out.println("Seed: " + seed + " / " + maxSeed);
        if (hasUnusedInputs) System.out.println("Map has unused inputs!");
        if (isIllegal) System.out.println("Map is illegal!");
        if (isTooEasy) System.out.println("Map is too easy!");
        System.out.println("Output count: " + outputCount + " / " + (NetworkMap.MAX_INPUT + 1));
        System.out.println("Vulnerability count: " + vulnCount);*/
        
        lastSeed = seed + (long)1;
        
        //System.out.println(map.solutionToString());
        
        map.evaluate(0);
        SwingUtilities.invokeLater(() -> {
            networkPanel = NetworkPanel.createNetworkPanel(map);
            networkScroll.getViewport().setView(networkPanel);
            networkScroll.getVerticalScrollBar().setValue(0);
            
            layoutPanel.revalidate();
            if (!firstPack) {
                frame.pack();
                frame.repaint();
                firstPack = true;
            }
            else {
                layoutPanel.repaint();
            }
            
            JOptionPane.showMessageDialog(frame,
                    "Number of solutions: " + map.getCachedVulnerabilityCount());
        });
    }
    
    public void showCongrats() {
        String[] options = {
            "Let me review",
            "New game"
        };
        int choice = JOptionPane.showOptionDialog(frame,
                "Congratulations! You solved this puzzle! ",
                "Congratulations!",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == 1) {
            generateNewPuzzle();
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == newGameButton) {
            generateNewPuzzle();
        }
        else if (ae.getSource() == exitButton) {
            System.exit(0);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        //
    }
}
