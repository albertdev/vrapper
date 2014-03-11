package net.sourceforge.vrapper.plugin.methodtextobj.tests;

import static org.mockito.Mockito.*;

import org.junit.Test;

import net.sourceforge.vrapper.core.tests.cases.VisualTestCase;
import net.sourceforge.vrapper.plugin.methodtextobj.provider.MethodTextObjectProvider;

public class MethodTextObjTests extends VisualTestCase {

    protected static final String METHOD_BEFORE = "    public void setUp() {\n        super.setUp()"
            + "\n    }\n\n";
    
    @Override
    public void setUp() {
        super.setUp();
        when(platform.getPlatformSpecificTextObjectProvider()).thenReturn(new MethodTextObjectProvider());
        reloadEditorAdaptor();
    }
    
    @Test public void testAllMethod() {
		checkCommand(forKeySeq("af"),
				false,  METHOD_BEFORE + "    public void testAll","M",
				"ethod() {\n        checkCommand()\n    }\n\n",
				false,  METHOD_BEFORE + "    ","public void testAllMethod() {\n        checkCommand()\n    }","\n\n");
    }
}
