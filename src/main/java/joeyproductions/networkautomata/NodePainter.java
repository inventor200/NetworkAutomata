/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Handler for painting nodes.
 * @author Joseph Cramsey
 */
public class NodePainter extends JComponent implements MouseListener {
    
    public static final int NODE_RADIUS = 24;
    public static final int NODE_DIAMETER = NODE_RADIUS * 2;
    public static final int BIT_FONT_SIZE = NODE_RADIUS / 2;
    public static final Font BIT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, BIT_FONT_SIZE);
    public static final Color[] PALETTE = {
        Color.RED, Color.YELLOW, Color.CYAN, Color.WHITE
    };
    public static final int STANDARD_PALETTE_LENGTH = PALETTE.length - 1;
    public static final Color INVERSION_COLOR = Color.BLACK;
    
    public NetworkNode data;
    private final NetworkPanel parentPanel;
    private final NetworkPanelColumn column;
    private boolean hasHover = false;
    private Color color;
    
    private NodePainter(NetworkNode data, NetworkPanelColumn column, NetworkPanel parentPanel) {
        this.data = data;
        this.column = column;
        this.parentPanel = parentPanel;
    }
    
    @Override
    public Dimension getSize(Dimension rv) {
        if (rv == null) rv = new Dimension();
        rv.width = NODE_DIAMETER;
        rv.height = NODE_DIAMETER;
        return rv;
    }
    
    @Override
    public Dimension getSize() {
        return getSize(null);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return getSize();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return getSize();
    }
    
    @Override
    public Dimension getMaximumSize() {
        return getSize();
    }
    
    public Color getColor() {
        return color;
    }
    
    public Point getCentroid() {
        Point localPos = getLocation();
        Point colPos = column.getLocation();
        Dimension size = getSize();
        return new Point(
                localPos.x + colPos.x + (size.width / 2),
                localPos.y + colPos.y + (size.height / 2)
        );
    }
    
    public static NodePainter createPainter(NetworkNode data, int colorIndex, NetworkPanelColumn column, NetworkPanel parentPanel) {
        NodePainter product = new NodePainter(data, column, parentPanel);
        data.painter = product;
        
        product.addMouseListener(product);
        
        product.color = getNodeColor(colorIndex, data.isEndNode);
        if (data.isEndNode && data.bitIndex == 0) {
            product.color = Color.RED;
        }
        
        return product;
    }
    
    private static Color mixColors(Color a, Color b) {
        return new Color(
                (a.getRed() + b.getRed()) / 2,
                (a.getGreen() + b.getGreen()) / 2,
                (a.getBlue() + b.getBlue()) / 2,
                Math.max(a.getAlpha(), b.getAlpha())
        );
    }
    
    private Color getTintOrShade(int tintOrShade) {
        if (tintOrShade > 0) {
            return mixColors(color, Color.WHITE);
        }
        else if (tintOrShade < 0) {
            return mixColors(color, Color.BLACK);
        }
        return color;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int tintOrShade = hasHover ? 1 : (data.state ? 0 : -1);
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(getTintOrShade(tintOrShade));
        
        int halfRad = NODE_RADIUS / 2;
        g2.drawOval(halfRad, halfRad, NODE_RADIUS, NODE_RADIUS);
        
        if (data.state) {
            g2.drawOval(halfRad + 2, halfRad + 2, NODE_RADIUS - 4, NODE_RADIUS - 4);
        }
        
        if (data.isInverted) {
            g2.drawRect(halfRad - 4, NODE_RADIUS - 2, NODE_RADIUS + 8, 4);
        }
        
        if (data.isEndNode) {
            g2.setFont(BIT_FONT);
            String indexStr = "" + data.bitIndex;
            if (data.bitIndex == 0) {
                indexStr = "X";
                g2.setColor(tintOrShade > 0 ? Color.ORANGE : Color.RED);
            }
            else {
                g2.setColor(tintOrShade > 0 ? Color.WHITE : Color.LIGHT_GRAY);
            }
            g2.drawString(indexStr,
                    NODE_RADIUS - (BIT_FONT_SIZE / 4),
                    NODE_RADIUS + (BIT_FONT_SIZE / 2));
        }
        
        if (parentPanel.getLockNode() == data) {
            g2.setColor(Color.WHITE);
            g2.drawRect(1, 1, NODE_DIAMETER - 2, NODE_DIAMETER - 2);
        }
        if (data.isFlagged) {
            g2.setColor(Color.WHITE);
            g2.drawLine(1, 1, NODE_DIAMETER - 2, NODE_DIAMETER - 2);
            g2.drawLine(1, NODE_DIAMETER - 2, NODE_DIAMETER - 2, 1);
        }
    }
    
    public boolean hasHover() {
        return hasHover;
    }
    
    private static Color getNodeColor(int colorIndex, boolean isEndNode) {
        if (isEndNode) {
            return PALETTE[STANDARD_PALETTE_LENGTH];
        }
        return PALETTE[colorIndex % STANDARD_PALETTE_LENGTH];
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        //
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == 1) {
            if (data.clickChangesState) {
                data.state = !data.state;
                parentPanel.evaluate();
            }
            else {
                NetworkNode lockNode = parentPanel.getLockNode();
                if (lockNode == data) {
                    parentPanel.lockOnNode(null);
                }
                else {
                    parentPanel.lockOnNode(data);
                    parentPanel.traceNode(data);
                }
            }
            SwingUtilities.invokeLater(() -> {
                parentPanel.repaint();
            });
        }
        else if (e.getButton() == 3) {
            if (!data.isEndNode) {
                data.isFlagged = !data.isFlagged;
                SwingUtilities.invokeLater(() -> {
                    parentPanel.repaint();
                });
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (parentPanel.getLockNode() == null) {
            parentPanel.traceNode(data);
        }
        hasHover = true;
        SwingUtilities.invokeLater(() -> {
            parentPanel.repaint();
        });
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (parentPanel.getLockNode() == null) {
            parentPanel.clearSoloList();
        }
        hasHover = false;
        SwingUtilities.invokeLater(() -> {
            parentPanel.repaint();
        });
    }
}
