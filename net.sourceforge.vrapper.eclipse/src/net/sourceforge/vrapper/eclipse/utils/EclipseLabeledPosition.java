package net.sourceforge.vrapper.eclipse.utils;

import net.sourceforge.vrapper.utils.LabeledPosition;
import net.sourceforge.vrapper.utils.Position;

/**
 * Holds information about a Position and its associated label.
 */
public class EclipseLabeledPosition implements LabeledPosition {

    private String label;
    private Position position;
    private int positionModelOffset;

    public EclipseLabeledPosition(String label, Position position) {
        this.label = label;
        this.position = position;
        this.positionModelOffset = position.getModelOffset();
    }

    public String getLabel() {
        return label;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + positionModelOffset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EclipseLabeledPosition other = (EclipseLabeledPosition) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (positionModelOffset != other.positionModelOffset)
            return false;
        return true;
    }
}
