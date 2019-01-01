package net.sourceforge.vrapper.plugin.easymotion.modes;

import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.DirectionHint;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.MotionPairsHint;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.PreviousModeHint;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.SuspendVisualModeCommand;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public class ChangeToEMModeCommand extends CountIgnoringNonRepeatableCommand {
    private final ModeSwitchHint[] args;

    public static ChangeToEMModeCommand forPair(MotionPairsHint pair, DirectionHint direction) {
        return new ChangeToEMModeCommand(pair, direction);
    }

    protected ChangeToEMModeCommand(ModeSwitchHint... args) {
        this.args = args;
    }

    /**
     * Returns a new command which properly switches back to one of the visual modes when target
     * picking is done.
     */
    public ChangeToEMModeCommand inVisualMode() {
        ModeSwitchHint[] extendedArgs = new ModeSwitchHint[args.length + 1];
        System.arraycopy(args, 0, extendedArgs, 0, args.length);
        extendedArgs[args.length] = EMModeHints.FROM_VISUAL;

        return new ChangeToEMModeCommand(extendedArgs);
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        if (VimUtils.findModeHint(EMModeHints.FROM_VISUAL.getClass(), args) != null) {
            SuspendVisualModeCommand.INSTANCE.execute(editorAdaptor);
        }

        String previousMode = editorAdaptor.getCurrentModeName();
        ModeSwitchHint[] extendedHints = new ModeSwitchHint[args.length + 1];
        System.arraycopy(args, 0, extendedHints, 0, args.length);
        int indexOfLastHint = args.length /* -1 + 1 */;

        extendedHints[indexOfLastHint] = new PreviousModeHint(previousMode);

        // [TODO] Implement motions which ask for input
//        editorAdaptor.changeMode(EMInputMode.NAME, args);
        editorAdaptor.changeMode(EMTargetMode.NAME, extendedHints);
    }
}
