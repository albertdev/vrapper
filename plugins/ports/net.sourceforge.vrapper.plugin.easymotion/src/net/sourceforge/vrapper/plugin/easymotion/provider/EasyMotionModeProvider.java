package net.sourceforge.vrapper.plugin.easymotion.provider;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.platform.AbstractPlatformSpecificModeProvider;
import net.sourceforge.vrapper.plugin.easymotion.modes.EMTargetMode;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class EasyMotionModeProvider extends AbstractPlatformSpecificModeProvider {

    public EasyMotionModeProvider() {
        super(EasyMotionModeProvider.class.getName());
    }

    @Override
    public List<EditorMode> getModes(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        List<EditorMode> modes = new ArrayList<EditorMode>(2);
//        modes.add(new EMInputMode(editorAdaptor));
        modes.add(new EMTargetMode(editorAdaptor));
        return modes;
    }
}
