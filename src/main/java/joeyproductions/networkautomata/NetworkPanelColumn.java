/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A layout column
 * @author Joseph Cramsey
 */
public class NetworkPanelColumn extends JPanel {
    
    public static final int COL_PADDING = 4;
    
    private NetworkPanelColumn() {
        //
    }
    
    public static NetworkPanelColumn createColumn(ArrayList<NetworkNode> col, NetworkPanel parentPanel) {
        NetworkPanelColumn product = new NetworkPanelColumn();
        product.setLayout(new BoxLayout(product, BoxLayout.X_AXIS));
        product.setOpaque(false);
        
        product.add(Box.createHorizontalGlue());

        for (int j = 0; j < col.size(); j++) {
            NetworkNode node = col.get(j);
            NodePainter painter = NodePainter.createPainter(node, j, product, parentPanel);
            parentPanel.addPainter(painter);
            product.add(painter);
            if (j < col.size() - 1) {
                product.add(Box.createHorizontalStrut(COL_PADDING));
            }
        }

        product.add(Box.createHorizontalGlue());
        
        return product;
    }
}
