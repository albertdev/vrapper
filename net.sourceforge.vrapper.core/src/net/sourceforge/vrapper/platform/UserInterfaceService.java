package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Provides access to vim-like mechanisms for showing information about the
 * editor state: command line, active mode, info and error message.<br>
 * Whether and how this information is displayed is entirely up to the
 * implementation.
 *
 * @author Matthias Radig
 */
public interface UserInterfaceService {
	
    static final String VRAPPER_DISABLED = "vrapper disabled";

    /**
     * Indicates the current mode of the editor.
     */
    void setEditorMode(String modeName);
    
    /**
     * For the :ascii command - prints ASCII values of the char under the cursor
     */
    void setAsciiValues(String asciiValue, int decValue, String hexValue, String octalValue);
    
    /**
     * Informative output set by a recently executed command. Only used when {@link #setInfoSet(boolean)}
     * got called with parameter <code>true</code>.
     */
    String getLastCommandResultValue();
    
    /**
     * Set the info returned by a command. Only used when {@link #setInfoSet(boolean)} got called
     * with parameter <code>true</code>.
     */
    void setLastCommandResultValue(String lastCommandResultValue);
    
    /**
     * Message of any kind.
     *
     * <p>Note that the Normal mode resets this info message all the time. Commands should use
     * {@link #setLastCommandResultValue(String)} and {@link #setInfoSet(boolean)}.
     */
    void setInfoMessage(String content);
    
    /**
     * Get last Info status message
     */
    String getLastInfoValue();

    /**
     * Error message, e.g. no search results found.
     */
    void setErrorMessage(String content);
    
    /**
     * Get last Error status message
     */
    String getLastErrorValue();

    /**
     * Whether a macro is currently being recorded.
     */
    void setRecording(boolean recording, String macroName);
   
    /**
     * Set to true when running an :ascii/ga command. 
     * Set to false when completing command. Keeps Info line from getting wiped out.
     * @param asciiSet
     */
    void setInfoSet(boolean infoSet);
   
    /**
     * Called in CommandBasedMode to determine whether or not to wipe out the Info line text.
     */
    boolean isInfoSet();

    /**
     * Create a new command line ui.
     * @param editorAdaptor
     */
    CommandLineUI getCommandLineUI(EditorAdaptor editorAdaptor);
}
