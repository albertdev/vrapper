package net.sourceforge.vrapper.plugin.easymotion.modes;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveUpDownNonWhitespace;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public class EMModeHints {

    public static final ModeSwitchHint FROM_VISUAL = new ModeSwitchHint() {};
    /** Use the same motion as last time, just ask the user again for a new target code. */
    public static final ModeSwitchHint REPEAT_TARGETFIND = new ModeSwitchHint() {};


    public static class InputCharsLimitHint implements ModeSwitchHint {
        public static final InputCharsLimitHint ONE = new InputCharsLimitHint(1);

        private final int count;

        public InputCharsLimitHint(int count) {
            this.count = count;
        }

        public int getInputLimit() {
            return count;
        }
    }

    public static class PreviousModeHint implements ModeSwitchHint {
        private String previousMode;

        public PreviousModeHint(String previousMode) {
            this.previousMode = previousMode;
        }

        public String getPreviousMode() {
            return previousMode;
        }
    }

    // [NOTE] This might be needed when we can't reuse the existing 'f/F/t/T' motions
//    public static class CharOffsetHint implements ModeSwitchHint {
//        public static final CharOffsetHint F_CHARS = new CharOffsetHint(EMCharOffset.NONE);
//        public static final CharOffsetHint T_CHAR_FORWARD =
//                new CharOffsetHint(new EMCharOffset(-1));
//        public static final CharOffsetHint T_CHAR_BACKWARD =
//                new CharOffsetHint(new EMCharOffset(1));
//
//        private final EMCharOffset offset;
//
//        protected CharOffsetHint(EMCharOffset offset) {
//            this.offset = offset;
//        }
//
//        public EMCharOffset getOffset() {
//            return offset;
//        }
//    }

    public static class MotionPairsHint implements ModeSwitchHint {
        public static MotionPairsHint LINE_UP_DOWN = new MotionPairsHint(
                MoveUpDownNonWhitespace.MOVE_DOWN, MoveUpDownNonWhitespace.MOVE_UP);

        private Motion forwardMotion;
        private Motion backwardMotion;

        public MotionPairsHint(Motion forwardMotion, Motion backwardMotion) {
            this.forwardMotion = forwardMotion;
            this.backwardMotion = backwardMotion;
        }

        public Motion getForwardMotion() {
            return forwardMotion;
        }

        public Motion getBackwardMotion() {
            return backwardMotion;
        }
    }

    public interface DirectionHint extends ModeSwitchHint {
        /**
         * Gets the range in which results need to be searched when moving towards end of file.
         * @return a range or <code>null</code> if not necessary for this direction.
         */
        public TextRange getForwardsRange(EditorAdaptor editorAdaptor, Position currentPosition);
        /**
         * Gets the range in which results need to be searched when moving towards beginning of file.
         * @return a range or <code>null</code> if not necessary for this direction.
         */
        public TextRange getBackwardsRange(EditorAdaptor editorAdaptor, Position currentPosition);
    }

    public static enum Directions implements DirectionHint {
        FORWARDS {
            @Override
            public TextRange getForwardsRange(EditorAdaptor editorAdaptor, Position currentPosition) {
                ViewPortInformation information = editorAdaptor.getViewportService().getViewPortInformation();
                TextContent viewContent = editorAdaptor.getViewContent();
                int bottomViewLine = information.getBottomLine();
                LineInformation lineInfo = viewContent.getLineInformation(bottomViewLine);
                int endOffset = lineInfo.getEndOffset();
                Position endPos = editorAdaptor.getCursorService().newPositionForViewOffset(endOffset);
                return new StartEndTextRange(currentPosition, endPos);
            }

            @Override
            public TextRange getBackwardsRange(EditorAdaptor editorAdaptor, Position currentPosition) {
                return null;
            }
        },

        BACKWARDS {
            @Override
            public TextRange getForwardsRange(EditorAdaptor editorAdaptor, Position currentPosition) {
                return null;
            }

            @Override
            public TextRange getBackwardsRange(EditorAdaptor editorAdaptor, Position currentPosition) {
                ViewPortInformation information = editorAdaptor.getViewportService().getViewPortInformation();
                TextContent viewContent = editorAdaptor.getViewContent();
                int topViewLine = information.getTopLine();
                LineInformation lineInfo = viewContent.getLineInformation(topViewLine);
                int beginOffset = lineInfo.getBeginOffset();
                Position startPos = editorAdaptor.getCursorService().newPositionForViewOffset(beginOffset);
                return new StartEndTextRange(startPos, currentPosition);
            }
        },

        BIDIRECTION {
            @Override
            public TextRange getForwardsRange(EditorAdaptor editorAdaptor, Position currentPosition) {
                return FORWARDS.getForwardsRange(editorAdaptor, currentPosition);
            }

            @Override
            public TextRange getBackwardsRange(EditorAdaptor editorAdaptor, Position currentPosition) {
                return BACKWARDS.getBackwardsRange(editorAdaptor, currentPosition);
            }
            
        };
    }
}
