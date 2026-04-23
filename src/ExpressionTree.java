public class ExpressionTree {

    // 1-indexed tam binary tree: parent(i)=i/2, left(i)=i*2, right(i)=i*2+1
    private char[] nodes       = new char[32];
    private int    cursorIndex = 1;

    public void placeSymbol(char c) {
        // TODO: cursor konumuna yaz, sonraki boş slota geç
    }

    public void moveCursor(char dir) {
        // TODO: W=parent(i/2), A=sol(i*2), D=sağ(i*2+1)
        // Her harekette -1 penalty
    }

    public char removeAtCursor() {
        // TODO: cursor konumundaki sembolü al, slotu temizle (-2 penalty)
        return 0;
    }

    public int getCursorIndex() { return cursorIndex; }

    public boolean isValid() {
        // TODO: min 3 değişken + min depth 3
        return false;
    }

    public String toInfix() {
        // TODO: recursive inorder
        return "";
    }

    public String toPostfix() {
        // TODO: recursive postorder
        return "";
    }

    public boolean evaluate(boolean a, boolean b, boolean c, boolean d) {
        // TODO: TruthTable hesabı için
        return false;
    }

    public int getNodeCount() {
        // TODO: 10 * nodeCount skoru için
        return 0;
    }
}
