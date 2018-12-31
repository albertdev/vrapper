package net.sourceforge.vrapper.utils;

public abstract class AbstractPosition implements Position {

    public int compareTo(Position o) {
        int diff = getModelOffset()-o.getModelOffset();
        return (int) Math.signum(diff);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getModelOffset();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            return compareTo((Position)obj) == 0;
        }
        return false;
    }

}
