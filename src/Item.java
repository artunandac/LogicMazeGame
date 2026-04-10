

public class Item {
    private char symbol='@';
    private int x, y;
    private int type;
    private int pointValue;
    private boolean isCollected;

    public Item(int startX, int startY, int itemType) {
        this.x = startX;
        this.y = startY;
        this.type = itemType;
        this.isCollected = false;

        if (this.type == 1) this.pointValue = 3;
        else if (this.type == 2) this.pointValue = 10;
        else if (this.type == 3) this.pointValue = 30;
        else if (this.type == 4) this.pointValue = 0;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPointValue() {
        return pointValue;
    }

    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean status) {
        this.isCollected = status;
    }
}