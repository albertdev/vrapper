package net.sourceforge.vrapper.plugin.easymotion.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.vrapper.utils.Position;

/**
 * This class forms a trie datastructure containing positions to jump to with the paths taken
 * forming the target label. Each new keypress will chop off the current root node, the chosen
 * branch will become the new root until a node is encountered which contains a {@link Position}
 * (a leaf node).
 */
public class EMTargetTrieNode {
    private Map<Character, EMTargetTrieNode> children;
    private Position leafNodeTarget;

    public EMTargetTrieNode() {
        children = new HashMap<Character, EMTargetTrieNode>();
    }

    private EMTargetTrieNode(Position target) {
        leafNodeTarget = target;
    }

    /**
     * Returns a {@link Position} if this is a leaf node. If this node contains children and hence
     * doesn't know the target yet it will return <code>null</code>.
     */
    public Position getTargetInfo() {
        return leafNodeTarget;
    }

    public void addLeaf(char inputKey, Position target) {
        EMTargetTrieNode child = new EMTargetTrieNode(target);
        children.put(inputKey, child);
    }

    public void addNestedNode(char inputKey, EMTargetTrieNode node) {
        children.put(inputKey, node);
    }

    /**
     * Progress through the target tree.
     * @param inputKey key to test for.
     * @return either the next state or <code>null</code> if the key is not recognized.
     * @throws IllegalStateException when this method is called on a leaf node.
     */
    public EMTargetTrieNode pickNextChild(Character inputKey) throws IllegalStateException {
        if (children == null) {
            throw new IllegalStateException("Leaf node does not have children to pick from");
        }
        return children.get(inputKey);
    }

    /**
     * Gets all labels and positions contained in the tree.
     * @param prefix Pass an empty string if not sure.
     * @param positionsAndLabels {@link Map} in which result needs to be stored.
     */
    public void getPositionsAndLabels(String prefix, Map<Position, String> positionsAndLabels) {
        if (children == null) {
            throw new IllegalStateException("Leaf node does not have children to pick from");
        }
        for (Entry<Character, EMTargetTrieNode> child : children.entrySet()) {

            EMTargetTrieNode childNode = child.getValue();
            Position positionInfo = childNode.getTargetInfo();
            String sequence = prefix + child.getKey();

            if (positionInfo == null) {
                // Node contains more nodes. Go recursive
                childNode.getPositionsAndLabels(sequence, positionsAndLabels);
            } else {
                if (sequence.length() > 3) {
                    // Shorten sequence to first 3 characters and add elipsis
                    sequence = new StringBuilder(4).append(sequence, 0, 3).appendCodePoint(8230).toString();
                }
                positionsAndLabels.put(positionInfo, sequence);
            }
        }
    }
}
