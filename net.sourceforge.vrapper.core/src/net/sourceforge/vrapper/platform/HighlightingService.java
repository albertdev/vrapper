package net.sourceforge.vrapper.platform;

import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.utils.LabeledPosition;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;

/**
 * Text highlighting service using plugin-provided (plugin.xml) Eclipse annotations.
 */
public interface HighlightingService {

    /**
     * Highlights given region using Eclipse annotation type.
     * @param type Eclipse annotation type.
     * @param name highlighting name.
     * @param region range of text to highlight.
     * @return annotation handle or @a null if there was an error.
     */
    Object highlightRegion(final String type, final String name, final TextRange region);

    /**
     * Highlights a region of @a length model characters starting from @a offset
     * using Eclipse annotation type.
     * @param type Eclipse annotation type.
     * @param name highlighting name.
     * @param offset model offset of the first character to highlight.
     * @param length region length
     * @return annotation handle or @a null if there was an error.
     */
    Object highlightRegion(final String type, final String name,
            final int offset, final int length);

    /**
     * Returns text region previously highlighted with @ref highlightRegion.
     * @param annotationHandle handle returned by @ref highlightRegion
     * @return text region or null in case of an error.
     */
    TextRange getHighlightedRegion(final Object annotationHandle);

    /**
     * Removes highlighting identified by the annotation handle.
     * @param annotationHandle handle returned by @ref highlightRegion.
     */
    void removeHighlighting(final Object annotationHandle);

    /**
     * Highlights given regions using Eclipse annotation type. This is faster than repeatedly
     * calling {@link #highlightRegion(String, String, TextRange)}.
     * The implementation should do its best to make the order of the return value match the order
     * of the input values.
     * @param type Eclipse annotation type.
     * @param name highlighting name.
     * @param region range of text to highlight.
     * @return annotation handle or @a null if there was an error.
     */
    List<Object> highlightRegions(String type, String name, List<TextRange> regions);

    /**
     * Removes highlights identified by the annotation handles. This is faster than repeatedly
     * calling {@link #removeHighlighting(Object)}.
     * @param annotationHandle handle returned by @ref highlightRegion.
     */
    void removeHighlights(List<Object> annotationHandles);

    /**
     * Paints labels on top of a given {@link Position}. Positions which are inside a view are
     * ignored.
     *
     * <p>Note that these labels should be short-lived: when edits happen, a piece of text may move
     * to a slightly different offset. The label painting code will however keep painting it in the
     * original spot.
     * @param positions {@link Map} containing unique (in terms of model offset) positions and their
     * associated label.
     * @return a {@link List} of {@link LabeledPosition} instances which serve as a handle to remove
     * the labels later on.
     */
    List<LabeledPosition> labelPositions(Map<Position, String> positionLabels);

    /**
     * Removes labeled positions from view.
     * @param positionLabels one or more of the handle objects returned by {@link #labelPositions(Map)}
     */
    void removeLabels(List<LabeledPosition> positionLabels);
}
