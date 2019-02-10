package net.sourceforge.vrapper.plugin.easymotion.utils;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Defines an additional char offset to simulate t/T in vim-sneak.
 */
public class EMCharOffset {
    public static final EMCharOffset NONE = new EMCharOffset(0);
    private final int offset;

    public EMCharOffset(int offset) {
        this.offset = offset;
    }

    public Position apply(EditorAdaptor editorAdaptor, TextRange sneakMatch) {
        if (offset == 0) {
            return sneakMatch.getLeftBound();
        }
        Position start = sneakMatch.getLeftBound();
        return editorAdaptor.getCursorService().shiftPositionForModelOffset(start.getModelOffset(),
                offset, true);
    }

    public EMCharOffset reverse() {
        if (offset == 0) {
            return NONE;
        } else {
            return new EMCharOffset(-offset);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + offset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EMCharOffset other = (EMCharOffset) obj;
        if (offset != other.offset)
            return false;
        return true;
    }

    public boolean equalsAmountOfChars(EMCharOffset searchOffset) {
        return Math.abs(offset) == Math.abs(searchOffset.offset);
    }

    public int getOffset() {
        return offset;
    }
}
