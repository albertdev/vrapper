package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import net.sourceforge.vrapper.eclipse.utils.EclipseLabeledPosition;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.LabeledPosition;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;

public class EclipseHighlightingService implements HighlightingService {

    private final AbstractTextEditor editor;
    private final CursorService cursorService;
    private StyledText editorTextView;
    private LabelPainter labelPainter;

    EclipseHighlightingService(AbstractTextEditor editor, ISourceViewer sourceViewer, CursorService cursorService) {
        this.editor = editor;
        this.cursorService = cursorService;
        editorTextView = sourceViewer.getTextWidget();
        labelPainter = new LabelPainter();
        editorTextView.addPaintListener(labelPainter);
    }

    public void uninstallHooks() {
        if (editorTextView != null) {
            editorTextView.removePaintListener(labelPainter);
            labelPainter = null;
            editorTextView = null;
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", // IAnnotationModelExtension uses raw Map
        "unchecked"}) // Converting to raw map or putting is considered unsafe
    public List<Object> highlightRegions(final String type, final String name, final List<TextRange> regions) {
        List<Object> annotations = new ArrayList<Object>();
        final IAnnotationModel am = getAnnotationModel();
        if (am instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension ame = (IAnnotationModelExtension) am;
            Map temp = new LinkedHashMap(regions.size());
            for (TextRange region : regions) {
                Annotation annotation = new Annotation(type, false, name);
                int offset = region.getLeftBound().getModelOffset();
                int length = region.getModelLength();
                temp.put(annotation, new org.eclipse.jface.text.Position(offset, length));
            }
            ame.replaceAnnotations(null, temp);
            annotations.addAll(temp.keySet());
        } else if (am != null) {
            // Slower method
            for (TextRange region : regions) {
                annotations.add(highlightRegion(type, name, region));
            }
        }
        return annotations;
    }

    @Override
    public Object highlightRegion(final String type, final String name, final TextRange region) {
        return this.highlightRegion(type, name, region.getLeftBound().getModelOffset(), region.getModelLength());
    }

    @Override
    public Object highlightRegion(String type, String name, int offset, int length) {
        final IAnnotationModel am = getAnnotationModel();
        if (am != null) {
            final Annotation annotation = new Annotation(type, false, name);
            am.addAnnotation(annotation, new org.eclipse.jface.text.Position(offset, length));
            return annotation;
        }
        return null;
    }

    @Override
    public TextRange getHighlightedRegion(Object annotationHandle) {
        final Annotation annotation = (Annotation) annotationHandle;
        final IAnnotationModel am = getAnnotationModel();
        if (am != null && annotation != null) {
            final org.eclipse.jface.text.Position position = am.getPosition(annotation);
            if (position != null) {
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(position.getOffset()),
                        cursorService.newPositionForModelOffset(position.getOffset() + position.getLength()));
            }
        }
        return null;
    }

    @Override
    public void removeHighlights(List<Object> annotationHandles) {
        final IAnnotationModel am = getAnnotationModel();
        if (am instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension ame = (IAnnotationModelExtension) am;
            Annotation[] temp = annotationHandles.toArray(new Annotation[annotationHandles.size()]);
            ame.replaceAnnotations(temp, null);
        } else if (am != null) {
            //Slower method
            for (Object annotationHandle : annotationHandles) {
                final Annotation annotation = (Annotation) annotationHandle;
                if (annotation != null) {
                    am.removeAnnotation(annotation);
                }
            }
        }
    }

    @Override
    public void removeHighlighting(Object annotationHandle) {
        final Annotation annotation = (Annotation) annotationHandle;
        final IAnnotationModel am = getAnnotationModel();
        if (am != null && annotation != null) {
            am.removeAnnotation(annotation);
        }
    }

    private IAnnotationModel getAnnotationModel() {
        IDocumentProvider doc = editor.getDocumentProvider();
        return doc != null ? doc.getAnnotationModel(editor.getEditorInput()) : null;
    }

    @Override
    public List<LabeledPosition> labelPositions(Map<Position, String> positionLabels) {
        if (positionLabels == null) {
            throw new VrapperPlatformException("Positionlabels parameter was null");
        }
        ArrayList<LabeledPosition> result = new ArrayList<LabeledPosition>();
        for (Map.Entry<Position, String> entry : positionLabels.entrySet()) {
            LabeledPosition label = new EclipseLabeledPosition(entry.getValue(), entry.getKey());
            labelPainter.labels.put(label.getPosition(), label);
            result.add(label);
        }
        editorTextView.redraw();
        return result;
    }

    @Override
    public void removeLabels(List<LabeledPosition> positionLabels) {
        TreeMap<Position, LabeledPosition> allLabels = labelPainter.labels;
        // Check whether all labels need to be cleared. Clearing everything at once is O(1),
        // removing it one at a time means the underlying tree needs to be repaired every time.
        // Checking should be n * O(log n) but with little overhead
        if (allLabels.size() == positionLabels.size()) {
            int i = 0;
            while (i < positionLabels.size()
                    && allLabels.containsKey(positionLabels.get(i).getPosition())) {
                i++;
            }
            if (i == positionLabels.size()) {
                allLabels.clear();
                editorTextView.redraw();
                return;
            }
        }

        for (LabeledPosition label : positionLabels) {
            allLabels.remove(label.getPosition());
        }
        editorTextView.redraw();
    }

    private static class LabelPainter implements PaintListener {
        private TreeMap<Position, LabeledPosition> labels;

        protected LabelPainter() {
            labels = new TreeMap<Position, LabeledPosition>();
        }

        @Override
        public void paintControl(PaintEvent e) {
            if (labels.isEmpty()) {
                return;
            }
            StyledText st = (StyledText) e.widget;
            GC gc = e.gc;
            Display dp = e.display;

            gc.setFont(JFaceResources.getDialogFont());
            Color borderColor = dp.getSystemColor(SWT.COLOR_WIDGET_BORDER);
            // Use editor's colors as those properly follow dark theming
            Color labelBackground = st.getBackground();
            Color labelForeground = st.getForeground();

            for (LabeledPosition lp : labels.values()) {
                int viewOffset = lp.getPosition().getViewOffset();
                Point lpPixelOffsets;
                try {
                    lpPixelOffsets = st.getLocationAtOffset(viewOffset);
                } catch (Exception ex) {
                    VrapperLog.error("Failed to find pixel coordinates for offset V " + viewOffset
                            + ". Error: " + ex);
                    continue;
                }
                Point labelExtent = gc.textExtent(lp.getLabel());
                int padding = 2;
                // Total increase of width due to padding
                int totalPadding = padding * 2;

                gc.setBackground(labelBackground);
                gc.setForeground(borderColor);

                gc.fillRectangle(lpPixelOffsets.x - padding, lpPixelOffsets.y - padding,
                        labelExtent.x + totalPadding, labelExtent.y + totalPadding);
                gc.drawRectangle(lpPixelOffsets.x - padding, lpPixelOffsets.y - padding,
                        labelExtent.x + totalPadding, labelExtent.y + totalPadding);

                gc.setForeground(labelForeground);
                gc.drawString(lp.getLabel(), lpPixelOffsets.x, lpPixelOffsets.y, true);
            }
        }
    }
}
