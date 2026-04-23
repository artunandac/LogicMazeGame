public class LogicSymbol {

    public int  x, y;
    public char symbol;

    // Geçerli semboller: A B C D  a b c d  ~ ^ v + > =
    public LogicSymbol(int x, int y, char symbol) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
    }
}
