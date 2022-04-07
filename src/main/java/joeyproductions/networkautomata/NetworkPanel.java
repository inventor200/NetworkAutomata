/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * The panel responsible for drawing the network nodes.
 * @author Joseph Cramsey
 */
public class NetworkPanel extends JPanel {
    
    public static final int TOP_BOTTOM_PADDING = 32;
    public static final int ROW_PADDING = 128;
    
    private final NetworkMap map;
    private final ArrayList<NodePainter> painters;
    private final ArrayList<NetworkNode> soloList;
    private NetworkNode lockNode = null;
    
    private NetworkPanel(NetworkMap map) {
        this.map = map;
        painters = new ArrayList<>();
        soloList = new ArrayList<>();
    }
    
    public static NetworkPanel createNetworkPanel(NetworkMap map) {
        NetworkPanel product = new NetworkPanel(map);
        BoxLayout layout = new BoxLayout(product, BoxLayout.Y_AXIS);
        product.setLayout(layout);
        product.setOpaque(true);
        product.setBackground(NodePainter.INVERSION_COLOR);
        
        product.add(Box.createVerticalStrut(TOP_BOTTOM_PADDING));
        for (int i = 0; i < map.network.length; i++) {
            NetworkPanelColumn column = NetworkPanelColumn.createColumn(map.network[i], product);
            
            product.add(column);
            if (i < map.network.length - 1) {
                product.add(Box.createVerticalStrut(ROW_PADDING));
            }
        }
        product.add(Box.createVerticalStrut(TOP_BOTTOM_PADDING));
        product.add(Box.createVerticalGlue());
        
        return product;
    }
    
    public void addPainter(NodePainter painter) {
        painters.add(painter);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        Graphics2D g2 = (Graphics2D)g;
        
        paintNodes(g2, false);
        paintNodes(g2, true);
    }
    
    private boolean hasSoloNode(NetworkNode node) {
        if (soloList.isEmpty()) return true;
        return soloList.contains(node);
    }
    
    private void paintNodes(Graphics2D g2, boolean matchState) {
        // Connections
        for (int i = 1; i < map.network.length; i++) {
            int cols = map.network[i].size();
            
            for (int j = 0; j < cols; j++) {
                NetworkNode node = map.network[i].get(j);
                boolean isSolo = hasSoloNode(node);
                Point nodePos = node.painter.getCentroid();
                int posX = nodePos.x;
                int posY = nodePos.y - (NodePainter.NODE_RADIUS / 2);
                
                for (NetworkNode source : node.sources) {
                    if (source.state != matchState) continue;
                    if (!(hasSoloNode(source) && isSolo)) continue;
                    nodePos = source.painter.getCentroid();
                    int lastX = nodePos.x;
                    int lastY = nodePos.y + (NodePainter.NODE_RADIUS / 2);
                    
                    Color lineColor = source.painter.getColor();
                    if (!source.state) {
                        lineColor = lineColor.darker().darker();
                    }
                    g2.setColor(lineColor);
                    
                    int reach = (posY - lastY) / 2;
                    int bendSpan = Math.min(reach, Math.abs(posX - lastX)) / 2;
                    int s1 = lastY + reach + (posX / 32);
                    int s0 = s1 - bendSpan;
                    int s2 = s1 + bendSpan;
                    
                    int xDir = bendSpan;
                    if (lastX > posX) xDir = -bendSpan;
                    
                    g2.drawLine(lastX, lastY, lastX, s0);
                    g2.drawLine(lastX, s0, lastX + xDir, s1);
                    g2.drawLine(lastX + xDir, s1, posX - xDir, s1);
                    g2.drawLine(posX - xDir, s1, posX, s2);
                    g2.drawLine(posX, s2, posX, posY);
                }
            }
        }
    }
    
    public int getNetworkLength() {
        return map.network.length;
    }
    
    public void clearSoloList() {
        soloList.clear();
    }
    
    public void traceNode(NetworkNode node) {
        clearSoloList();
        node.collect(soloList);
    }
    
    public void lockOnNode(NetworkNode node) {
        lockNode = node;
        SwingUtilities.invokeLater(() -> {
            repaint();
        });
    }
    
    public NetworkNode getLockNode() {
        return lockNode;
    }
    
    public void evaluate() {
        int eval = map.evaluate(map.addInputs());
        boolean isSolved = !NetworkMap.isEvaluationSafe(eval);
        if (isSolved) {
            MainWindow.SINGLETON.showCongrats();
        }
    }
}
