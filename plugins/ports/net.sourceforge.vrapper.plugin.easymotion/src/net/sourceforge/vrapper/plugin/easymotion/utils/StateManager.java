package net.sourceforge.vrapper.plugin.easymotion.utils;

import java.util.List;

import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Interface to abstract away the retrieval of Sneak's current state. This should help during
 * testing as the sneak mode might otherwise not be regisetered in the {@link EditorAdaptor}
 * instance.
 */
public interface StateManager {
}
