package net.sourceforge.vrapper.plugin.easymotion.provider;


import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.plugin.easymotion.modes.ChangeToEMModeCommand;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.Directions;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMModeHints.MotionPairsHint;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;

public class EMStateProvider extends AbstractEclipseSpecificStateProvider {

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {

        // [FIXME] Move motion plug commands to separate function so visual mode can properly reuse it

        Command em_j = ChangeToEMModeCommand.forPair(MotionPairsHint.LINE_UP_DOWN, Directions.FORWARDS);
        Command em_k = ChangeToEMModeCommand.forPair(MotionPairsHint.LINE_UP_DOWN, Directions.BACKWARDS);
        Command em_bd_jk = ChangeToEMModeCommand.forPair(MotionPairsHint.LINE_UP_DOWN, Directions.BIDIRECTION);

        Command em_w = ChangeToEMModeCommand.forPair(MotionPairsHint.WORD_MOVE, Directions.FORWARDS);
        Command em_b = ChangeToEMModeCommand.forPair(MotionPairsHint.WORD_MOVE, Directions.BACKWARDS);
        Command em_bd_w = ChangeToEMModeCommand.forPair(MotionPairsHint.WORD_MOVE, Directions.BIDIRECTION);

        Command em_W = ChangeToEMModeCommand.forPair(MotionPairsHint.BIG_WORD_MOVE, Directions.FORWARDS);
        Command em_B = ChangeToEMModeCommand.forPair(MotionPairsHint.BIG_WORD_MOVE, Directions.BACKWARDS);
        Command em_bd_W = ChangeToEMModeCommand.forPair(MotionPairsHint.BIG_WORD_MOVE, Directions.BIDIRECTION);

        Command em_e = ChangeToEMModeCommand.forPair(MotionPairsHint.WORDEND_MOVE, Directions.FORWARDS);
        Command em_ge = ChangeToEMModeCommand.forPair(MotionPairsHint.WORDEND_MOVE, Directions.BACKWARDS);
        Command em_bd_e = ChangeToEMModeCommand.forPair(MotionPairsHint.WORDEND_MOVE, Directions.BIDIRECTION);

        Command em_E = ChangeToEMModeCommand.forPair(MotionPairsHint.BIG_WORDEND_MOVE, Directions.FORWARDS);
        Command em_gE = ChangeToEMModeCommand.forPair(MotionPairsHint.BIG_WORDEND_MOVE, Directions.BACKWARDS);
        Command em_bd_E = ChangeToEMModeCommand.forPair(MotionPairsHint.BIG_WORDEND_MOVE, Directions.BIDIRECTION);

        return state(
            leafBind(new PlugKeyStroke("(easymotion-j)"), em_j),
            leafBind(new PlugKeyStroke("(easymotion-k)"), em_k),
            leafBind(new PlugKeyStroke("(easymotion-bd-jk)"), em_bd_jk),
            leafBind(new PlugKeyStroke("(easymotion-w)"), em_w),
            leafBind(new PlugKeyStroke("(easymotion-b)"), em_b),
            leafBind(new PlugKeyStroke("(easymotion-bd-w)"), em_bd_w),
            leafBind(new PlugKeyStroke("(easymotion-W)"), em_W),
            leafBind(new PlugKeyStroke("(easymotion-B)"), em_B),
            leafBind(new PlugKeyStroke("(easymotion-bd-W)"), em_bd_W),
            leafBind(new PlugKeyStroke("(easymotion-e)"), em_e),
            leafBind(new PlugKeyStroke("(easymotion-ge)"), em_ge),
            leafBind(new PlugKeyStroke("(easymotion-bd-e)"), em_bd_e),
            leafBind(new PlugKeyStroke("(easymotion-E)"), em_E),
            leafBind(new PlugKeyStroke("(easymotion-gE)"), em_gE),
            leafBind(new PlugKeyStroke("(easymotion-bd-E)"), em_bd_E)
        );
    }

    @Override
    protected State<Command> visualModeBindings() {
        return new ConvertingState<Command, Command>(VisualModeCommandConversionFunction.INSTANCE, normalModeBindings());
    }

    /**
     * Allows us to wrap the motion command binding state for normal mode and translate it for
     * visual mode.
     */
    private static class VisualModeCommandConversionFunction implements Function<Command, Command> {
        private static VisualModeCommandConversionFunction INSTANCE = new VisualModeCommandConversionFunction();

        @Override
        public Command call(Command arg) {
            if (arg instanceof ChangeToEMModeCommand) {
                return ((ChangeToEMModeCommand)arg).inVisualMode();
            }
            return arg;
        }
    }
}
