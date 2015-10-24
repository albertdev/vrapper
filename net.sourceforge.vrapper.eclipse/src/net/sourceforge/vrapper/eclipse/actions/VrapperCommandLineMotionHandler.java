package net.sourceforge.vrapper.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;

/**
 * Handles Home / End / Page up / Page down motions.
 */
public class VrapperCommandLineMotionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String commandId = event.getCommand().getId();
        // Guaranteed to be present through core expression in plugin.xml
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        try {
            // [FIXME] Look up BufferManager instance through event context instead of singleton use.
            InputInterceptor interceptor = InputInterceptorManager.INSTANCE.findActiveInterceptor(activeEditor);
            if (interceptor == null) {
                VrapperLog.error("Could not find interceptor for part " + activeEditor);
                return null;
            }
            if ( ! (interceptor.getEditorAdaptor().getCurrentMode() instanceof AbstractCommandLineMode)) {
                VrapperLog.error("Command line is not shown!");
                return null;
            }
            CommandLineUI commandLine = interceptor.getEditorAdaptor().getCommandLine();
            if (commandId.endsWith(".lineStart")) {
                commandLine.setPosition(0);
            } else if (commandId.endsWith(".lineEnd")) {
                int lastPos = commandLine.getEndPosition();
                commandLine.setPosition(lastPos);
            } else if (commandId.endsWith(".wordNext")) {
                
            }
        } catch (VrapperPlatformException e) {
            VrapperLog.error("Failed to find editor for part " + activeEditor, e);
        } catch (UnknownEditorException e) {
            VrapperLog.error("Could not find interceptor for part " + activeEditor, e);
        }
        return null;
    }
}
