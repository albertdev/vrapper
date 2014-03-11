package net.sourceforge.vrapper.core.tests.cases;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Test;

// FIXME: needs testing with different values of 'selection' variable
// (it affects most of the tests)

public class VisualModeTests extends VisualTestCase {

	@Test public void testMotionsInVisualMode() {
		checkCommand(forKeySeq("w"),
				false, "","Al","a ma kota",
				false, "","Ala ","ma kota");
		checkCommand(forKeySeq("w"),
				true,  "","Ala ma k","ota",
				true, "Ala ","ma k","ota");
		checkCommand(forKeySeq("w"),
				true,  "A","lamak","ota i psa",
				false, "Alama","kota ","i psa");
		checkCommand(forKeySeq("e"),
				true,  "A","lamak","ota i psa",
				false, "Alama","kota"," i psa");
		checkCommand(forKeySeq("b"),
				false, "Alama","kota ","i psa",
				true,  "","Alamak","ota i psa");
		checkCommand(forKeySeq("h"),
				false, " ktoto","t","aki ",
				true,  " ktot","ot","aki ");
		checkCommand(forKeySeq("h"),
				true,  " ktoto","t","aki ",
				true,  " ktot","ot","aki ");
		checkCommand(forKeySeq("l"),
				true,  " ktot","ot","aki ",
				false,  " ktoto","t","aki ");
		// undefined behavior, inverse selection over 1 character should not
		// happen anymore
//		checkCommand(forKeySeq("l"),
//				true,  " ktoto","t","aki ",
//				false, " ktotot","","aki ");
		checkCommand(forKeySeq("l"),
				false, " ktoto","t","aki ",
				false, " ktoto","ta","ki ");
	}

	@Test public void testCommandsInVisualMode() throws Exception {
		checkCommand(forKeySeq("o"),
				false, "A","la"," ma kota",
				true,  "A","la"," ma kota");
		checkCommand(forKeySeq("o"),
				true,  "A","la"," ma kota",
				false, "A","la"," ma kota");

		// FIXME:
		// it's broken in test case, works quite well
		// in real eclipse
		checkCommand(forKeySeq("x"),
				false, "A","la"," ma kota",
				false, "A",""," ma kota");
		verify(adaptor).changeMode(NormalMode.NAME);

		checkCommand(forKeySeq("d"),
				true,  "A","LA"," MA kota",
				true,  "A",""," MA kota");
		verify(adaptor, times(2)).changeMode(NormalMode.NAME);

		checkLeavingCommand(forKeySeq("y"), true,
				"A", "LA", " MA kota",
				"A", 'L', "A MA kota");
		verify(adaptor, times(3)).changeMode(NormalMode.NAME);

		checkCommand(forKeySeq("s"),
				true,  "A","LA"," MA kota",
				true,  "A",""," MA kota");
		// TODO: obtain correct arguments used by by ChangeOperation when changing mode
//		verify(adaptor).changeMode(InsertMode.NAME);
	}

    @Test
    public void testPastingInVisualMode() throws Exception {
        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("p"),
                false, "The internet is ","awesome","!",
                false, "The internet is a series of tube","","s!");
        verify(adaptor).changeMode(NormalMode.NAME);
        assertYanked(ContentType.TEXT, "awesome");

        defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "\t\ta series of tubes\n"));
        checkCommand(forKeySeq("p"),
                true, "The internet is ","awesome","!",
                true, "The internet is \n\t\t","","a series of tubes\n!");
        verify(adaptor, times(2)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.TEXT, "awesome");

        defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "a series of tubes\n"));
        checkCommand(forKeySeq("p"),
                false, "The internet is \n","awesome\n","!",
                false, "The internet is \n","","a series of tubes\n!");
        verify(adaptor, times(3)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.LINES, "awesome\n");

        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("p"),
                false, "The internet is \n","awesome\n","!",
                false, "The internet is \n","","a series of tubes\n!");
        verify(adaptor, times(4)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.LINES, "awesome\n");

        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("2p"),
                false, "The internet is ","awesome","!",
                false, "The internet is a series of tubesa series of tube","","s!");
        verify(adaptor, times(5)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.TEXT, "awesome");
    }

    @Test public void visualModeShouldHaveAName() {
		adaptor.changeModeSafely(VisualMode.NAME);
		assertEquals("visual mode", adaptor.getCurrentModeName());
	}

	@Test public void visualModeShouldEnterPainlesslyAndDeselectOnLeave() throws Exception {
	    CursorService cursorService = platform.getCursorService();
	    Position position = cursorService.newPositionForModelOffset(42);
	    cursorService.setPosition(position, StickyColumnPolicy.ON_CHANGE);
		adaptor.changeMode(NormalMode.NAME);
		adaptor.changeMode(VisualMode.NAME);
		// verify that selection has been cleared. getSelection will return non-null!
		verify(adaptor).setSelection(null);
	}

	@Test public void testTextObjects() {
		checkCommand(forKeySeq("iw"),
				false,  "It's Some","th","ing interesting.",
				false,  "It's ","Something"," interesting.");
    }

	@Test
	public void test_fMotions() {
		checkCommand(forKeySeq("fs"),
				false,  "Ther","e"," was a bug about it",
				false,  "Ther","e was"," a bug about it");
	}

	@Test
	public void test_tMotions() {
		// Check repeated use of t motion in visual mode.
		// Oddly, this appears to fail in Vim 7.4
	
		checkCommand(forKeySeq("t)"),
				false,  "getText(","line.getEndOffset","() - line.getBeginOffset()));",
				false,  "getText(","line.getEndOffset(",") - line.getBeginOffset()));");
		checkCommand(forKeySeq("t)"),
				false,  "getText(","line.getEndOffset(",") - line.getBeginOffset()));",
				false,  "getText(","line.getEndOffset() - line.getBeginOffset(",")));");
	}

	@Test
    public void test_J() {
		checkLeavingCommand(forKeySeq("J"),
				false,  "Hell","o,\nW","orld!\n;-)",
				"Hello,",' ',"World!\n;-)");
		checkLeavingCommand(forKeySeq("gJ"),
				false,  "new Hell","o\nW","orld();\n//;-)",
				"new Hello",'W',"orld();\n//;-)");
		checkLeavingCommand(forKeySeq("J"),
				false,  "","\n\nh","ello",
				"",'h',"ello");
    }
	
	@Test
	public void test_tilde() {
		checkLeavingCommand(forKeySeq("~"),
				false,  "with ","some CAPITAL"," letters",
				"with ",'S',"OME capital letters");
		
		checkLeavingCommand(forKeySeq("~"),
				false,  "with ","some\nCAPITAL"," letters",
				"with ",'S',"OME\ncapital letters");
		
		checkLeavingCommand(forKeySeq("~"),
				true,  "with ","some CAPITAL"," letters",
				"with ",'S',"OME capital letters");
		
		checkLeavingCommand(forKeySeq("~"),
				true,  "with ","some\nCAPITAL"," letters",
				"with ",'S',"OME\ncapital letters");
	}
	
	@Test
	public void test_CtrlC_exits() {
		checkLeavingCommand(forKeySeq("<C-c>"), true,
				"test", "123", "test",
				"test", '1', "23test");
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}

	@Test
	public void test_ShiftWidth() {
		when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
		when(configuration.get(Options.TAB_STOP)).thenReturn(4);
		when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(4);

		checkLeavingCommand(forKeySeq(">"),
				false, "","    Hello,\n    W","orld!\n;-)",
				"        ",'H',"ello,\n        World!\n;-)");
		checkLeavingCommand(forKeySeq(">"),
				false, "    ","Hello,\n    W","orld!\n;-)",
				"        ",'H',"ello,\n        World!\n;-)");
		checkLeavingCommand(forKeySeq(">"),
				false, "   "," Hello,\n   "," World!\n;-)",
				"        ",'H',"ello,\n        World!\n;-)");
		checkLeavingCommand(forKeySeq(">"),
				false, "   "," Hello,\n","    World!\n;-)",
				"        ",'H',"ello,\n    World!\n;-)");
		checkLeavingCommand(forKeySeq(">"),
				false, "   "," Hello,\n    World!\n","\n;-)",
				"        ",'H',"ello,\n        World!\n\n;-)");
		checkLeavingCommand(forKeySeq(">"),
				false, "   "," Hello,\n    World!\n  ","\n;-)",
				"        ",'H',"ello,\n        World!\n  \n;-)");
	}
}
