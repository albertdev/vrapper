package net.sourceforge.vrapper.plugin.easymotion.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.NavigatingMotion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * This motion can be triggered by the ';' and ',' keys to repeat jumping forward or backward.
 */
public class EMPreviousOrNextMotion extends CountAwareMotion implements NavigatingMotion {

    private Motion forwardMotion;
    private Motion backwardMotion;
    private boolean isBackwards;

    public EMPreviousOrNextMotion(Motion forwardMotion, Motion backwardMotion) {
        this.forwardMotion = forwardMotion;
        this.backwardMotion = backwardMotion;
    }

    public EMPreviousOrNextMotion(Motion forwardMotion, Motion backwardMotion, boolean isBackwards) {
        this.forwardMotion = forwardMotion;
        this.backwardMotion = backwardMotion;
        this.isBackwards = isBackwards;
    }

    @Override
    public BorderPolicy borderPolicy() {
        if (isBackwards) {
            return backwardMotion.borderPolicy();
        }
        return forwardMotion.borderPolicy();
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        if (isBackwards) {
            return backwardMotion.stickyColumnPolicy();
        }
        return forwardMotion.stickyColumnPolicy();
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition) throws CommandExecutionException {
        if (isBackwards) {
            return backwardMotion.withCount(count).destination(editorAdaptor, fromPosition);
        }
        return forwardMotion.withCount(count).destination(editorAdaptor, fromPosition);
    }

    @Override
    public NavigatingMotion reverse() {
        return new EMPreviousOrNextMotion(forwardMotion, backwardMotion, ! isBackwards);
    }

    @Override
    public boolean isBackward() {
        return isBackwards;
    }

    @Override
    public NavigatingMotion repetition() {
        return this;
    }
}
