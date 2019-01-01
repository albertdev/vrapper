package net.sourceforge.vrapper.plugin.easymotion.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.AbstractModelSideMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.NavigatingMotion;

public class EMJumpToTargetMotion extends AbstractModelSideMotion /*implements NavigatingMotion*/ {


    private Position target;
    private Motion backwardMotion;
    private Motion forwardMotion;

    public EMJumpToTargetMotion(Position target, Motion forwardMotion, Motion backwardMotion) {
        this.target = target;
        this.forwardMotion = forwardMotion;
        this.backwardMotion = backwardMotion;
    }

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
        return target.getModelOffset();
    }

    @Override
    public boolean isJump() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> type) {
        if (NavigatingMotion.class.equals(type)) {
            return (T) new EMPreviousOrNextMotion(forwardMotion, backwardMotion);
        }
        return super.getAdapter(type);
    }
}
