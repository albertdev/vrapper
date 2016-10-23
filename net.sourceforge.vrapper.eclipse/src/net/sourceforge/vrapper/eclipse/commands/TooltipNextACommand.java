package net.sourceforge.vrapper.eclipse.commands;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.interceptor.EditorInfo;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

public class TooltipNextACommand extends CountIgnoringNonRepeatableCommand {
    protected static ToolTip tooltip;

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        try {
            if (tooltip != null) {
                tooltip.dispose();
            }
            InputInterceptor activeInterceptor = VrapperPlugin.getDefault().findActiveInterceptor();
            EclipsePlatform platform = activeInterceptor.getPlatform();
            AbstractTextEditor underlyingEditor = platform.getUnderlyingEditor();
            ISourceViewer viewer = activeInterceptor.getPlatform().getUnderlyingSourceViewer();
            EditorInfo editorInfo = activeInterceptor.getEditorInfo();
            
//            ToolTipHelper helper = new ToolTipHelper(viewer.getTextWidget());
            Search thingToFind = new Search("a", false, false, SearchOffset.NONE, true);
            SearchResult result = platform.getSearchAndReplaceService().find(thingToFind,
                    editorAdaptor.getPosition());
            if (result.isFound()) {
                int searchStart = result.getLeftBound().getViewOffset();

                StyledText textWidget = viewer.getTextWidget();
                Point textEditorOffset = textWidget.toDisplay(0, 0);

                tooltip = new ToolTip(textWidget.getShell(), SWT.None);
                tooltip.setText("A");
                Point characterOffset = textWidget.getLocationAtOffset(searchStart);
                tooltip.setLocation(textEditorOffset.x + characterOffset.x, textEditorOffset.y + characterOffset.y);
                tooltip.setVisible(true);
                tooltip.setAutoHide(false);
            }
        } catch (VrapperPlatformException e) {

        } catch (UnknownEditorException e) {
        }

    }

    protected static class CompartmentFigure extends Figure {

        public CompartmentFigure() {
            ToolbarLayout layout = new ToolbarLayout();
            layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
            layout.setStretchMinorAxis(false);
            layout.setSpacing(2);
            setLayoutManager(layout);
            setBorder(new CompartmentFigureBorder());
        }

        public class CompartmentFigureBorder extends AbstractBorder {
            public Insets getInsets(IFigure figure) {
                return new Insets(1, 0, 0, 0);
            }

            public void paint(IFigure figure, Graphics graphics, Insets insets) {
//                graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(),
//                        tempRect.getTopRight());
            }
        }
    }
}
