package net.sourceforge.vrapper.utils;

/**
 * Holds information about a Position and its associated label.
 */
public interface LabeledPosition {

    public String getLabel();

    public Position getPosition();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object obj);
}
