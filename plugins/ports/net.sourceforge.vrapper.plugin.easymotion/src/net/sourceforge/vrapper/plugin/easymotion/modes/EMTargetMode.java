package net.sourceforge.vrapper.plugin.easymotion.modes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.plugin.easymotion.commands.motions.EMJumpToTargetMotion;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.DirectionHint;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.MotionPairsHint;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.PreviousModeHint;
import net.sourceforge.vrapper.plugin.easymotion.utils.EMTargetTrieNode;
import net.sourceforge.vrapper.plugin.easymotion.utils.EditorAdaptorStateManager;
import net.sourceforge.vrapper.plugin.easymotion.utils.StateManager;
import net.sourceforge.vrapper.utils.LabeledPosition;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * This mode will request input to identify a target.
 */
public class EMTargetMode extends AbstractMode {

    public static final String NAME = EMTargetMode.class.getName();
    private static final String DISPLAY_NAME = "EM TARGET";
    private static final int MAX_TARGETS = 100;

    public static final StateManager STATEMANAGER = new EditorAdaptorStateManager();

    protected static final KeyStroke KEY_RETURN = ConstructorWrappers.key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE = ConstructorWrappers.key(SpecialKey.ESC);
    protected static final KeyStroke KEY_BACKSP = ConstructorWrappers.key(SpecialKey.BACKSPACE);

    /** Holds the most recent input from the input mode. */
    public static String LAST_INPUT;

    protected DirectionHint directionHint;
    protected boolean fromVisual;
    protected String lastModeName;
    protected String targetKeys;
    private MotionPairsHint motions;
    private List<LabeledPosition> labels;
    private EMTargetTrieNode targetsTree;

    public EMTargetMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        if (stroke.equals(KEY_ESCAPE)) {
            editorAdaptor.changeModeSafely(lastModeName, AbstractVisualMode.RECALL_SELECTION_HINT);

        } else if (stroke.getCharacter() == KeyStroke.SPECIAL_KEY) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("EasyMotion: Invalid target");
            editorAdaptor.changeModeSafely(lastModeName, AbstractVisualMode.RECALL_SELECTION_HINT);

        } else {
            EMTargetTrieNode nextChild = targetsTree.pickNextChild(stroke.getCharacter());
            
            if (nextChild == null) {
                editorAdaptor.getUserInterfaceService().setErrorMessage("EasyMotion: Invalid target");
                editorAdaptor.changeModeSafely(lastModeName, AbstractVisualMode.RECALL_SELECTION_HINT);
            } else {
                Position target = nextChild.getTargetInfo();

                // No definitive choice made yet. Recreate labels and move in tree.
                if (target == null) {
                    HighlightingService highlightingService = editorAdaptor.getHighlightingService();

                    highlightingService.removeLabels(labels);
                    Map<Position, String> newPositionsAndLabels = new HashMap<Position, String>();
                    nextChild.getPositionsAndLabels("", newPositionsAndLabels);

                    labels = highlightingService.labelPositions(newPositionsAndLabels);

                    targetsTree = nextChild;
                } else {
                    jumpToPositionInLastMode(target);
                }
            }
        }
        return true;
    }

    public static Search constructSearchKeyword(EditorAdaptor editorAdaptor, String searchString,
            boolean sneakBackwards) {
        // reuse 
        boolean caseSensitive = ! editorAdaptor.getConfiguration().get(Options.IGNORE_CASE)
            || (editorAdaptor.getConfiguration().get(Options.SMART_CASE)
                && StringUtils.containsUppercase(searchString));
        Search searchKeyword = new Search(searchString, sneakBackwards, caseSensitive);
        return searchKeyword;
    }

    protected void jumpToPositionInLastMode(Position target) {
        try {
            Motion targetMotion = new EMJumpToTargetMotion(target, motions.getForwardMotion(),
                    motions.getBackwardMotion());
            MotionCommand motionCommand;
            if (fromVisual) {
                motionCommand = new VisualMotionCommand(targetMotion);
            } else {
                motionCommand = new MotionCommand(targetMotion);
            }

            editorAdaptor.changeMode(lastModeName, AbstractVisualMode.RECALL_SELECTION_HINT,
                            new ExecuteCommandHint.OnEnter(motionCommand));
        } catch (CommandExecutionException e) {
            VrapperLog.error("Last mode raised an error when trying to have an easymotion jump", e);
            editorAdaptor.changeModeSafely(NormalMode.NAME);

        } catch (RuntimeException e) {
            VrapperLog.error("Last mode raised an error when trying to have an easymotion jump", e);
            editorAdaptor.changeModeSafely(NormalMode.NAME);
        }
    }

    @Override
    public void enterMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.enterMode(hints);

        // reset defaults
        fromVisual = false;
        lastModeName = null;
        directionHint = null;
        labels = Collections.emptyList();

        for (ModeSwitchHint hint : hints) {
            if (hint == EMModeHints.FROM_VISUAL) {
                fromVisual = true;
            } else if (hint instanceof DirectionHint) {
                directionHint = (DirectionHint) hint;
            } else if (hint instanceof MotionPairsHint) {
                motions = (MotionPairsHint) hint;
            } else if (hint instanceof PreviousModeHint) {
                lastModeName = ((PreviousModeHint)hint).getPreviousMode();
            }
        }
        if (lastModeName == null) {
            VrapperLog.error("No previous mode hint passed to EasyMotion target mode");
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            return;
        }
        if (directionHint == null) {
            VrapperLog.error("No direction hint passed to EasyMotion target mode");
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            return;
        }

        Position[] targets = calculateTargets();

        if (targets.length == 0) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("EasyMotion: No matches");
            editorAdaptor.changeMode(lastModeName, AbstractVisualMode.RECALL_SELECTION_HINT);
        } else if (targets.length == 1) {
            jumpToPositionInLastMode(targets[0]);
        } else {

            String targetKeys = "abc"; //"asdghklqwertyuiopzxcvbnmfj;";

            targetsTree = buildTargetsTree(targetKeys, targets, "");

            Map<Position, String> targetsAndLabels = new HashMap<Position, String>();

            targetsTree.getPositionsAndLabels("", targetsAndLabels);
            labels = editorAdaptor.getHighlightingService().labelPositions(targetsAndLabels);

            editorAdaptor.getUserInterfaceService().setInfoMessage("Target key: ");
            editorAdaptor.getUserInterfaceService().setLastCommandResultValue("Target key: ");
            editorAdaptor.getUserInterfaceService().setInfoSet(true);
        }
    }

    private Position[] calculateTargets() throws CommandExecutionException {
        Position currentPosition = editorAdaptor.getPosition();
        TextRange forwardsRange = directionHint.getForwardsRange(editorAdaptor, currentPosition);
        TextRange backwardsRange = directionHint.getBackwardsRange(editorAdaptor, currentPosition);

        ArrayList<Position> forwardTargets = new ArrayList<Position>();
        ArrayList<Position> backwardTargets = new ArrayList<Position>();

        if (forwardsRange != null) {
            Motion forwardMotion = motions.getForwardMotion();
            calculateForwardsTargets(currentPosition, forwardsRange, forwardTargets, forwardMotion);
        }
        if (backwardsRange != null) {
            Motion backwardMotion = motions.getBackwardMotion();
            calculateBackwardsTargets(currentPosition, backwardsRange, backwardTargets, backwardMotion);
        }
        return mergeBiDirectionalTargets(forwardTargets, backwardTargets);
    }

    private void calculateForwardsTargets(Position currentPosition, TextRange forwardsRange,
            ArrayList<Position> forwardTargets, Motion forwardMotion)
            throws CommandExecutionException {
        Position searchPos = forwardMotion.destination(editorAdaptor);

        Position previousPosition = currentPosition;
        int countResults = 0;
        while (forwardsRange.getEnd().compareTo(searchPos) >= 0
                && countResults < MAX_TARGETS
                && ! previousPosition.equals(searchPos)) {
            previousPosition = searchPos;
            editorAdaptor.setPosition(searchPos, StickyColumnPolicy.NEVER);
            // Matches inside a folded element return view offset -1, ignore those
            if (searchPos.getViewOffset() >= 0) {
                forwardTargets.add(searchPos);
            }
            searchPos = forwardMotion.destination(editorAdaptor);
        }
        if (countResults > MAX_TARGETS) {
            VrapperLog.error("Maximum forwards targets reached");
        }
        editorAdaptor.setPosition(currentPosition, StickyColumnPolicy.NEVER);
    }

    private void calculateBackwardsTargets(Position currentPosition, TextRange backwardsRange,
            ArrayList<Position> backwardTargets, Motion backwardMotion)
            throws CommandExecutionException {
        Position searchPos = backwardMotion.destination(editorAdaptor);

        Position previousPosition = currentPosition;
        int countResults = 0;
        while (backwardsRange.getStart().compareTo(searchPos) <= 0
                && countResults < MAX_TARGETS
                && ! previousPosition.equals(searchPos)) {
            previousPosition = searchPos;
            editorAdaptor.setPosition(searchPos, StickyColumnPolicy.NEVER);
            // Matches inside a folded element return view offset -1, ignore those
            if (searchPos.getViewOffset() >= 0) {
                backwardTargets.add(searchPos);
            }
            searchPos = backwardMotion.destination(editorAdaptor);
        }
        if (countResults > MAX_TARGETS) {
            VrapperLog.error("Maximum forwards targets reached");
        }
        editorAdaptor.setPosition(currentPosition, StickyColumnPolicy.NEVER);
    }

    private Position[] mergeBiDirectionalTargets(List<Position> forwardTargetList, List<Position> backwardTargetList) {
        // Zip two lists together: first one from forward, then one from backward, etc
        int forwardTargetCount = forwardTargetList.size();
        int backwardTargetCount = backwardTargetList.size();
        Position[] forwardTargets = forwardTargetList.toArray(new Position[forwardTargetCount]);
        Position[] backwardTargets = backwardTargetList.toArray(new Position[backwardTargetCount]);
        Position[] targets = new Position[forwardTargetCount + backwardTargetCount];
        int maxTargetsToMerge = Math.min(forwardTargetCount, backwardTargetCount);
        int mergedIndex = 0;
        for (int i = 0; i < maxTargetsToMerge; i++, mergedIndex += 2) {
            targets[mergedIndex] = forwardTargets[i];
            targets[mergedIndex + 1] = backwardTargets[i];
        }
        if (forwardTargetCount > maxTargetsToMerge) {
            System.arraycopy(forwardTargets, maxTargetsToMerge, targets, maxTargetsToMerge * 2, forwardTargetCount - maxTargetsToMerge);
        } else if (backwardTargetCount > maxTargetsToMerge) {
            System.arraycopy(backwardTargets, maxTargetsToMerge, targets, maxTargetsToMerge * 2, backwardTargetCount - maxTargetsToMerge);
        }
        return targets;
    }

    /**
     * Builds a tree out of the targets so that each target is adressable with a series of keys.
     * @param targetKeys the list of keys to choose from.
     * @param targets The flat list of targets.
     * @param prefix 
     * @param targetsAndLabels Map in which the targets with their associated series of keys will be stored.
     * @return {@link EMTargetTrieNode} root of the grouping tree.
     */
    private EMTargetTrieNode buildTargetsTree(String targetKeys, Position[] targets, String prefix) {
        // Spread available targets out over targetKeys. If there are more targets than keys we use
        // the given key as prefix and start spreading it out even more. The last keys should be
        // preferred for grouping while the first few keys are reserved for direct targets.

        int keysNeeded = Math.min(targetKeys.length(), targets.length);

        // This array counts how much targets each key has assigned, whether direct or with nesting.
        int[] counts = new int[keysNeeded];
        Arrays.fill(counts, 1);
        if (keysNeeded < targets.length) {
            // Count how much we need in each group
            int targetsLeft = targets.length - keysNeeded;
            int addTargetsPerRound = (targetKeys.length() - 1); // Taken from EasyMotion code
            while (targetsLeft > 0) {
                int i = counts.length - 1;
                while (i >= 0 && targetsLeft > 0) {
                    if (targetsLeft > addTargetsPerRound) {
                        counts[i] += addTargetsPerRound;
                        targetsLeft -= addTargetsPerRound;
                    } else {
                        counts[i] += targetsLeft;
                        targetsLeft = 0;
                    }
                    i--;
                }
            }
        }

        int i = 0;
        int targetIndex = 0;
        EMTargetTrieNode currentNode = new EMTargetTrieNode();
        while (i < counts.length) {
            char currentKey = targetKeys.charAt(i);
            String keySequence = prefix + currentKey;
            if (counts[i] == 1) {
                // Direct target
                currentNode.addLeaf(currentKey, targets[targetIndex]);
            } else {
                // Create nested group
                int nNestedTargets = counts[i];
                Position[] nestedTargets = new Position[nNestedTargets];
                System.arraycopy(targets, targetIndex, nestedTargets, 0, nNestedTargets);
                EMTargetTrieNode node;
                node = buildTargetsTree(targetKeys, nestedTargets, keySequence);
                currentNode.addNestedNode(currentKey, node);
            }
            targetIndex += counts[i];
            i++;
        }
        return currentNode;
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.leaveMode(hints);
        editorAdaptor.getUserInterfaceService().setInfoMessage(null);

        editorAdaptor.getHighlightingService().removeLabels(labels);
    }

    /**
     * EasyMotion mode doesn't accept keymaps.
     */
    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return null;
    }
}
