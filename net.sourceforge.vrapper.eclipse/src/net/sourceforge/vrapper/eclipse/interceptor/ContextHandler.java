package net.sourceforge.vrapper.eclipse.interceptor;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;

public class ContextHandler extends VrapperEventAdapter {

    private IContextActivation vrapperEnabledContext;
    private IContextActivation vrapperModeContext;
    private IContextService contextService;

    public void hookContextService(EditorInfo currentEditor) {
        IEditorSite editorSite = currentEditor.getTopLevelEditor().getEditorSite();
        contextService = (IContextService) editorSite.getService(IContextService.class);
        if (contextService == null) {
            VrapperLog.error("Null contextservice!");
            throw new VrapperPlatformException("Could not retrieve IContextService from editor site"
                    + " " + editorSite);
        } else {
            // Only activate contexts when enabling Vrapper
//            if (vrapperEnabledContext == null) {
//                vrapperEnabledContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active");
//            }
//            vrapperModeContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active.normal");
            vrapperToggled(true);
        }
    }

    public void unhookContextService(EditorInfo currentEditor) {
        if (contextService == null) {
            VrapperLog.error("Null contextservice in unhook!");
            return;
        }
        if (vrapperEnabledContext != null) {
            contextService.deactivateContext(vrapperEnabledContext);
        }
        if (vrapperModeContext != null) {
            contextService.deactivateContext(vrapperModeContext);
        }
        vrapperEnabledContext = null;
        vrapperModeContext = null;
    }

    @Override
    public void modeAboutToSwitch(EditorMode currentMode, EditorMode newMode) {
        // TODO Auto-generated method stub
        super.modeAboutToSwitch(currentMode, newMode);
    }

    @Override
    public void modeSwitched(EditorMode oldMode, EditorMode currentMode) {
        if (vrapperModeContext != null) {
            contextService.deactivateContext(vrapperModeContext);
            VrapperLog.debug("Disabled vrapper mode context");
            vrapperModeContext = null;
        }
        if (currentMode instanceof NormalMode) {
            vrapperModeContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active.normal");
            VrapperLog.debug("Enabled vrapper normal mode context");
        }
        if (currentMode instanceof AbstractCommandLineMode) {
            vrapperModeContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active.command");
            VrapperLog.debug("Enabled vrapper commandline mode context");
        }
        if (currentMode instanceof InsertMode) {
            vrapperModeContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active.insert");
            VrapperLog.debug("Enabled vrapper insert mode context");
        }
        if (currentMode instanceof AbstractVisualMode) {
            vrapperModeContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active.visual");
            VrapperLog.debug("Enabled vrapper visual mode context");
        }
    }

    @Override
    public void vrapperToggled(boolean enabled) {
        if (enabled) {
            if (vrapperEnabledContext == null) {
                vrapperEnabledContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active");
                VrapperLog.debug("Enabled vrapper active context");
            }
            if (vrapperModeContext == null) {
                vrapperModeContext = contextService.activateContext("net.sourceforge.vrapper.eclipse.active.normal");
                VrapperLog.debug("Enabled vrapper normal mode context");
            }
        } else {
            if (vrapperEnabledContext != null) {
                contextService.deactivateContext(vrapperEnabledContext);
                VrapperLog.debug("Disabled vrapper active context");
            }
            if (vrapperModeContext != null) {
                contextService.deactivateContext(vrapperModeContext);
                VrapperLog.debug("Disabled vrapper normal mode context");
            }
            VrapperLog.debug("Vrapper disabled, deactivated contexts");
            vrapperEnabledContext = null;
            vrapperModeContext = null;
        }
    }
}
