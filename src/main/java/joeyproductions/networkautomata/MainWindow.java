/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * The main viewing window for the simulation.
 * @author Joseph Cramsey
 */
public class MainWindow {
    
    public static MainWindow SINGLETON;
    
    private JFrame frame;
    private JPanel layoutPanel;
    private NetworkPanel networkPanel;
    private NetworkMap map;
    private boolean readyForEval = false;
    
    public static void main(String[] args) {
        SINGLETON = new MainWindow();
    }
    
    public MainWindow() {
        boolean hasUnusedInputs = false;
        boolean isIllegal = false;
        boolean isSafe;
        int outputCount = 0;
        boolean hasGoodOutputs;
        int vulnCount = 0;
        boolean isTooEasy = false;
        int seed = 0;
        int batchSize = 65536;
        int maxSeed = batchSize*1;
        boolean found = false;
        for (int i = 0; i <= maxSeed; i++) {
            seed = i;
            map = new NetworkMap(seed);
            hasUnusedInputs = map.hasUnusedInputs();
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
                found = true;
                break;
            }
        }
        if (found) System.out.println("FOUND!!");
        System.out.println("Seed: " + seed + " / " + maxSeed);
        if (hasUnusedInputs) System.out.println("Map has unused inputs!");
        if (isIllegal) System.out.println("Map is illegal!");
        if (isTooEasy) System.out.println("Map is too easy!");
        System.out.println("Output count: " + outputCount + " / " + (NetworkMap.MAX_INPUT + 1));
        System.out.println("Vulnerability count: " + vulnCount);
        
        frame = new JFrame("Network Automata");
        SwingUtilities.invokeLater(() -> {
            networkPanel = NetworkPanel.createNetworkPanel(map);
            Dimension minSize = new Dimension(
                    networkPanel.getPreferredSize().width + 64, 600
            );
            JScrollPane scroll = new JScrollPane(networkPanel) {
                @Override
                public Dimension getMinimumSize() {
                    return minSize;
                }
                
                @Override
                public Dimension getPreferredSize() {
                    return minSize;
                }
                
                @Override
                public Dimension getMaximumSize() {
                    return minSize;
                }
            };
            scroll.getVerticalScrollBar().setUnitIncrement(64);
            layoutPanel = new JPanel(new BorderLayout());
            layoutPanel.add(scroll, BorderLayout.CENTER);
            frame.setContentPane(layoutPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.pack();
            frame.setResizable(true);
            frame.setVisible(true);
            readyForEval = true;
        });
        
        try {
            while (!readyForEval) Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        map.evaluate(147);
        SwingUtilities.invokeLater(() -> {
            networkPanel.revalidate();
            networkPanel.repaint();
        });
    }
}
