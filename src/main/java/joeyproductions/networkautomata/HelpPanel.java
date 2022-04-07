/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * A little tutorial to fill out the empty space of the puzzle.
 * @author Joseph Cramsey
 */
public class HelpPanel extends JTextPane {
    
    public static final int MIN_WIDTH = 400;
    
    private HelpPanel() {
        super();
    }
    
    public static JComponent createHelpPanel() {
        HelpPanel product = new HelpPanel();
        product.setEditable(false);
        product.setContentType("text/html");
        product.setOpaque(true);
        //int padding = 16;
        //product.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        
        StyleSheet styleSheet = new StyleSheet();
        styleSheet.addRule("body { background-color: #202020; padding: 32px; }");
        styleSheet.addRule("p, li { color: #CCCCCC; font-size: 16pt; font-family: sans-serif; margin: 4px 0px 16px 16px; }");
        styleSheet.addRule("h1, h2 { color: #FFFFFF; font-family: monospace; }");
        styleSheet.addRule("h1 { font-size: 32pt; }");
        styleSheet.addRule("h2 { font-size: 24pt; margin: 16px 0px 0px 24px; }");
        
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        htmlEditorKit.setStyleSheet(styleSheet);
        HTMLDocument htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        product.setEditorKit(htmlEditorKit);
        product.setDocument(htmlDocument);
        
        StringBuilder sb = new StringBuilder();
        String newParagraph = "<p>&nbsp;&nbsp;&nbsp;";
        sb.append("<html><body>");
        sb.append("<h1>Network Automata</h1>");
        sb.append("<h2>A puzzle game by Joseph Cramsey</h2>");
        sb.append(newParagraph);
        sb.append("In this game, you are presented with a procedurally-generated "
                + "puzzle, constructed of inputs at the top, outputs on the bottom, "
                + "and intermediary nodes in between.</p>");
        sb.append(newParagraph);
        sb.append("Each node can be <b>ON</b> or <b>OFF</b>, and you may freely alter the nodes "
                + "along the top directly by <b>left-clicking</b> on them. From there, the "
                + "states of the nodes below are a result of a calcation, based on the "
                + "inputs at the top.</p>");
        sb.append(newParagraph);
        sb.append("To solve the puzzle, you must find the correct input states "
                + "which will cause the right-most output (labeled \"<b>X</b>\") to be <b>ON</b>. "
                + "Some puzzles have multiple solutions.</p>");
        sb.append(newParagraph);
        sb.append("<b>Note:</b> The other 4 output node states are not counted as part of the solution. "
                + "No matter what their states are, all that matters is the state of \"<b>X</b>\".</p>");
        sb.append("<h2>Node Rules</h2>");
        sb.append(newParagraph);
        sb.append("Each node has a number of connections to others above it. "
                + "The following rules only apply to connections coming in from \"source nodes\" above, "
                + "not to connections coming out of the bottom.</p>");
        sb.append("<ul>");
        sb.append("<li>Nodes with a single possible connection will simply copy the state "
                + "of the connected source node.</li>");
        sb.append("<li>Nodes with multiple sources will be <b>ON</b> if <b>2-3</b> of the "
                + "incoming connections carry an <b>ON</b> signal.</li>");
        sb.append("<li>Nodes with multiple sources will be <b>OFF</b> if <b>0, 1, 4, or more</b> of the "
                + "incoming connections carry an <b>ON</b> signal.</li>");
        sb.append("<li>A node with with a bar passing through the middle is <b>inverted</b>. "
                + "This means that its state (according to the above rules) will "
                + "actually output to its <i>opposite</i>. An inverted node, which would normally be "
                + "<b>ON</b>, will actually be <b>OFF</b>, and vice-versa.</li>");
        sb.append("</ul>");
        sb.append("<h2>Controls</h2>");
        sb.append(newParagraph);
        sb.append("<b>Left Mouse Click:</b> Switch an input node, or <i>(for any "
                + "other kind of node)</i> visually-isolate the connections that have "
                + "anything to do with the selected node.</p>");
        sb.append(newParagraph);
        sb.append("<i>You can achieve this "
                + "visual isolation by hovering your mouse pointer over a node, but "
                + "left-clicking on it will lock the isolation, until you click on "
                + "the node again.</i></p>");
        sb.append(newParagraph);
        sb.append("<b>Right Mouse Click:</b> Mark node. This helps a player to "
                + "keep track of nodes while pondering a solution.</p>");
        sb.append(newParagraph);
        sb.append("<i>Marking a node "
                + "does not change how it functions in any way, and is simply "
                + "a visual reminder for the player to use.</i></p>");
        sb.append("</body></html>");
        
        product.setText(sb.toString());
        
        JScrollPane scroll = new JScrollPane(product) {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(MIN_WIDTH, MainWindow.MIN_HEIGHT);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600, MainWindow.MIN_HEIGHT);
            }
        };
        
        SwingUtilities.invokeLater(() -> {
            scroll.getVerticalScrollBar().setValue(0);
        });
        
        return scroll;
    }
}
