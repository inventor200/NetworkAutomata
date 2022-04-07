/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joeyproductions.networkautomata;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

/**
 * The abstract network map object.
 * @author Joseph Cramsey
 */
public class NetworkMap {
    
    public static final int END_COL_COUNT = 5;
    public static final int START_COL_COUNT = 8;
    public static final int MAX_INPUT = Math.round((float)Math.pow(2, START_COL_COUNT)) - 1;
    public static final int MIN_ROW_COUNT = 3;
    public static final int MAX_ROW_COUNT = 8;
    public static final int MIN_COL_COUNT = 2;
    public static final int MAX_COL_COUNT = START_COL_COUNT * 2;
    
    public ArrayList<NetworkNode>[] network;
    
    public NetworkMap(int seed) {
        Random rand = new Random(seed);
        
        int rowCount = rand.nextInt(MAX_ROW_COUNT - MIN_ROW_COUNT) + MIN_ROW_COUNT;
        network = new ArrayList[rowCount];
        
        network[0] = new ArrayList<>();
        for (int j = 0; j < START_COL_COUNT; j++) {
            NetworkNode node = new NetworkNode(true);
            node.clickChangesState = true;
            network[0].add(node);
        }
        
        for (int i = 1; i < rowCount; i++) {
            network[i] = new ArrayList<>();
            int prevCount = network[i - 1].size();
            int colCount = rand.nextInt(MAX_COL_COUNT - MIN_COL_COUNT) + MIN_COL_COUNT;
            if (i == rowCount - 1) {
                colCount = END_COL_COUNT;
            }
            for (int j = 0; j < colCount; j++) {
                NetworkNode node = new NetworkNode(i == rowCount - 1);
                if (!node.isEndNode) {
                    node.isInverted = rand.nextInt(16) > 12;
                }
                network[i].add(node);
                int sourceCount = rand.nextInt(prevCount - NetworkNode.MIN_SOURCE_COUNT) + NetworkNode.MIN_SOURCE_COUNT;
                if (sourceCount > NetworkNode.MAX_SOURCE_COUNT) {
                    sourceCount = NetworkNode.MAX_SOURCE_COUNT;
                }
                node.sources = new NetworkNode[sourceCount];
                
                int[] takenSources = new int[prevCount];
                for (int k = 0; k < prevCount; k++) {
                    takenSources[k] = k;
                }
                
                for (int k = 0; k < sourceCount; k++) {
                    // We leave a buffer at the end for taken indices
                    int takenIndex = rand.nextInt(prevCount - k);
                    int sourceIndex = takenSources[takenIndex];
                    // Remove the taken index, and slide the rest over
                    for (int m = takenIndex; m < prevCount - k - 1; m++) {
                        takenSources[m] = takenSources[m + 1];
                    }
                    NetworkNode source = network[i - 1].get(sourceIndex);
                    node.sources[k] = source;
                    source.patronList.add(node);
                }
            }
        }
        
        boolean removalMade;
        do {
            removalMade = false;
            for (int i = 1; i < rowCount - 1; i++) {
                ListIterator<NetworkNode> iter = network[i].listIterator();
                while (iter.hasNext()) {
                    NetworkNode node = iter.next();
                    if (!node.isSource()) {
                        removalMade = true;
                        iter.remove();
                        for (NetworkNode source : node.sources) {
                            // This node is no longer participating, so
                            // do not count it as a patron.
                            source.patronList.remove(node);
                        }
                    }
                }
            }
        } while (removalMade);
        
        for (int j = 0; j < START_COL_COUNT; j++) {
            network[0].get(j).bitIndex = START_COL_COUNT - j;
        }
        
        for (int j = 0; j < END_COL_COUNT; j++) {
            network[rowCount - 1].get(j).bitIndex = END_COL_COUNT - (j + 1);
        }
    }
    
    // Checks to see if brute-forcing works
    // The idea being that some players might be trying to use an semi-optimized
    // algorithm to check for easy solutions, such as turning on each input
    // bit one at a time, or turning all of them on, and selected one to be off
    // one at a time. If we deny these solutions, then players must actually work
    // toward a solution. For 8 bits of input, this will remove 28 solutions
    public boolean isTooEasy() {
        // All off
        if (!isEvaluationSafe(evaluate(0))) return true;
        // All on
        if (!isEvaluationSafe(evaluate(MAX_INPUT))) return true;
        
        // Each bit on, alone
        for (int i = 0; i < START_COL_COUNT; i++) {
            if (!isEvaluationSafe(evaluate(1 << i))) return true;
            // or off, alone
            if (!isEvaluationSafe(evaluate(MAX_INPUT - (1 << i)))) return true;
        }
        
        // Progressively turning on one by one, left to right
        int total = 0;
        for (int i = 0; i < START_COL_COUNT; i++) {
            total += 1 << i;
            if (!isEvaluationSafe(evaluate(total))) return true;
            // off one by one (which also serves as on, right to left)
            if (!isEvaluationSafe(evaluate(MAX_INPUT - total))) return true;
        }
        
        return false;
    }
    
    public boolean hasUnusedInputs() {
        for (int j = 0; j < START_COL_COUNT; j++) {
            if (!network[0].get(j).isSource()) return true;
        }
        
        return false;
    }
    
    public boolean isLegal() {
        for (int i = 1; i < network.length - 1; i++) {
            if (network[i].size() < MIN_COL_COUNT) return false;
        }
        
        return true;
    }
    
    public boolean isSafe() {
        return network[network.length - 1].get(END_COL_COUNT - 1).sources.length == 0;
    }
    
    public int getVulnerabilityCount() {
        int count = 0;
        
        for (int i = 0; i < MAX_INPUT + 1; i++) {
            if (!isEvaluationSafe(evaluate(i))) count++;
        }
        
        return count;
    }
    
    public int getOutputCount() {
        int count = 0;
        
        for (int i = 0; i < MAX_INPUT + 1; i++) {
            int eval = evaluate(i);
            if (isEvaluationSafe(eval) && eval > 0) count++;
        }
        
        return count;
    }
    
    public boolean hasGoodOutputs() {
        int outputCount = getOutputCount();
        
        return hasGoodOutputs(outputCount);
    }
    
    public boolean hasGoodOutputs(int outputCount) {
        return outputCount > 0 && outputCount < MAX_INPUT + 1;
    }
    
    public int addInputs() {
        int total = 0;
        int base = 1;
        for (int j = START_COL_COUNT - 1; j >= 0; j--) {
            if (network[0].get(j).state) {
                total += base;
            }
            base *= 2;
        }
        
        return total;
    }
    
    public int evaluate(int input) {
        if (input < 0) input *= -1;
        input %= (MAX_INPUT + 1);
        int base = (MAX_INPUT + 1) / 2;
        
        for (int j = 0; j < START_COL_COUNT; j++) {
            boolean isOn = input >= base;
            network[0].get(j).state = isOn;
            if (isOn) input -= base;
            base /= 2;
        }
        
        for (int i = 1; i < network.length; i++) {
            for (int j = 0; j < network[i].size(); j++) {
                network[i].get(j).evaluate();
            }
        }
        
        int total = 0;
        base = 1;
        for (int j = END_COL_COUNT - 1; j >= 0; j--) {
            if (network[network.length - 1].get(j).state) {
                total += base;
            }
            base *= 2;
        }
        
        return total;
    }
    
    public static boolean isEvaluationSafe(int evaluation) {
        return evaluation % 2 == 0;
    }
}
