/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * A node on the network.
 * @author Joseph Cramsey
 */
public class NetworkNode {
    
    public static final int MIN_SOURCE_COUNT = 1;
    public static final int MAX_SOURCE_COUNT = 4;
    public static final int MIN_SOURCE_BIRTH_COUNT = 2;
    public static final int MAX_SOURCE_BIRTH_COUNT = 3;
    
    public NetworkNode[] sources;
    public final ArrayList<NetworkNode> patronList;
    public NodePainter painter;
    
    public boolean state = false;
    public boolean isInverted = false;
    public boolean isEndNode = false;
    public boolean clickChangesState = false;
    public boolean isFlagged = false;
    public int bitIndex = 0;
    
    public NetworkNode(boolean isEndNode) {
        this.isEndNode = isEndNode;
        patronList = new ArrayList<>();
    }
    
    public boolean isSource() {
        return !patronList.isEmpty();
    }
    
    public void evaluate() {
        if (sources.length == 1) { // Nodes with one source are repeaters
            state = sources[0].state;
        }
        else {
            int count = 0;
            for (NetworkNode source : sources) {
                if (source.state) count++;
            }

            state = count >= MIN_SOURCE_BIRTH_COUNT && count <= MAX_SOURCE_BIRTH_COUNT;
        }
        if (isInverted) state = !state;
    }
    
    public void collect(ArrayList<NetworkNode> collection) {
        collection.add(this);
        collectUp(collection);
        collectDown(collection);
    }
    
    private void collectUp(ArrayList<NetworkNode> collection) {
        if (sources == null) return;
        if (sources.length == 0) return;
        for (NetworkNode source : sources) {
            collection.add(source);
            source.collectUp(collection);
        }
    }
    
    private void collectDown(ArrayList<NetworkNode> collection) {
        if (patronList == null) return;
        if (patronList.isEmpty()) return;
        ListIterator<NetworkNode> iter = patronList.listIterator();
        while (iter.hasNext()) {
            NetworkNode patron = iter.next();
            collection.add(patron);
            patron.collectDown(collection);
        }
    }
}
