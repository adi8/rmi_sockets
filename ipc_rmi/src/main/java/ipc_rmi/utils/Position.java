package ipc_rmi.utils;

public class Position {

    /**
     * X coordinate value.
     */
    private int x;

    /**
     * Y coordinate value.
     */
    private int y;

    /**
     * Constructor.
     *
     * @param x - x coordinate value
     * @param y - y coordinate value
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns x coordinate value.
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     * Returns y coordinate value.
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     * Increments x coordinate value by x.
     *
     * @param x - Number of places to move
     */
    public void incrementX(int x) {
        this.x += x;
    }

    /**
     * Increments y coordinate value by y.
     *
     * @param y - Number of places to move
     */
    public void incrementY(int y) {
        this.y += y;
    }

    /**
     * Decrements x coordinate value by x.
     *
     * @param x - Number of places to move
     */
    public void decrementX(int x) {
        this.x -= x;
    }

    /**
     * Decrements y coordinate value by y.
     *
     * @param y - Number of places to move
     */
    public void decrementY(int y) {
        this.y -= y;
    }

    /**
     * Checks if given position is equal to this position.
     *
     * @param pos
     * @return boolean
     */
    @Override
    public boolean equals(Object pos) {
        boolean flag = false;
        Position p = (Position) pos;

        if (this.x == p.getX() && this.y == p.getY())
            flag = true;

        return flag;
    }

    /**
     * Returns a unique integer hash for an object.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        return x + y;
    }

    /**
     * A string representation of Position.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

}
