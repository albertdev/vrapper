package net.sourceforge.vrapper.eclipse.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;


public class VrapperShortcutHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if ( ! (event.getTrigger() instanceof Event)) {
            VrapperLog.debug("Shortcut handler received an activation without event trigger?!?");
            return null;
        }
        Event triggerEvent = (Event) event.getTrigger();
        if (triggerEvent.type != SWT.KeyDown) {
            VrapperLog.debug("Shortcut handler received an activation other than key type?!?");
            return null;
        }
        VerifyEvent verifyEvent = new VerifyEvent(triggerEvent);

        // Guaranteed to be present through core expression in plugin.xml
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        try {
            // [FIXME] Look up BufferManager instance through event context instead of singleton use.
            InputInterceptor interceptor = InputInterceptorManager.INSTANCE.findActiveInterceptor(activeEditor);
            if (interceptor == null) {
                VrapperLog.error("Could not find interceptor for part " + activeEditor);
            }
            interceptor.verifyKey(verifyEvent);
        } catch (VrapperPlatformException e) {
            VrapperLog.error("Failed to find editor for part " + activeEditor, e);
        } catch (UnknownEditorException e) {
            VrapperLog.error("Could not find interceptor for part " + activeEditor, e);
        }
//        VrapperLog.debug("Event details: " + event);
//        String key = null;
//        IEditorPart editor = HandlerUtil.getActiveEditor(event);
//        if (event.getTrigger() instanceof Event) {
//            Event triggerEvent = (Event) event.getTrigger();
//            if (triggerEvent.type == SWT.KeyDown) {
//                VerifyEvent verifyEvent = new VerifyEvent(triggerEvent);
//                int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(triggerEvent);
//                KeyStroke keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
//                key = keyStroke.toString();
//            }
//        }
//        VrapperLog.debug("Shortcut handled. Key: " + key);
        return null;
    }

    @Override
    public void setEnabled(Object evaluationContext) {
//        super.setEnabled(evaluationContext);
//        IEvaluationContext context = null;
//        Object defaultVar = null;
//        if (evaluationContext instanceof IEvaluationContext) {
//            context = (IEvaluationContext) evaluationContext;
//            defaultVar = context.getVariable("activeContexts");
//        }
//        List<String> vrapperContexts = new ArrayList<String>();
//        if (defaultVar instanceof Collection) {
//            for (Object o : (Collection)defaultVar){
//                if (o.toString().startsWith("net.sourceforge")) {
//                    vrapperContexts.add(o.toString());
//                }
//            }
//        }
//        VrapperLog.debug("Set enabled called. Result: " + isEnabled() + 
//                ". Vrapper Contexts: " + vrapperContexts + ". All contexts: " + defaultVar);
    }

}
