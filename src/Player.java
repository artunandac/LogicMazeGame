public class Player {

    public int  x, y;
    public int  hp           = 100;
    public int  score        = 0;
    public int  fireballCount = 0;
    public boolean storageTree = false ;   // true=TREE, false=BACKPACK

    private Queue backpackQueue = new Queue(8);

    // Son hareket yönü (fireball için)
    public int lastDx = 1, lastDy = 0;

    public boolean move(int dx, int dy, Board board, Robot[] robots, int robotCount) {
        if (dx == 0 && dy == 0) return false;
        int nx = x + dx, ny = y + dy;
        if (board.isWall(nx, ny)) return false;
        for (int i = 0; i < robotCount; i++)
            if (robots[i].x == nx && robots[i].y == ny) return false;
        x = nx; y = ny;
        lastDx = dx; lastDy = dy;
        return true;
    }

    public boolean addToBackpack(char symbol) {
        if (backpackQueue.isFull()) return false;
        backpackQueue.enqueue(symbol);
        return true;
    }

    public char removeFromBackpack(int idx) {
        if (backpackQueue.isEmpty()) return 0;
        return (char) backpackQueue.dequeue();
    }

    public int getBackpackSize() {
        return backpackQueue.size();
    }

    // Backpack'i formatlanmış string dizisi olarak döndürür (HUD için)
    public String[] printBackpack() {
        int sz = backpackQueue.size();
        // Queue'daki elemanları oku (dequeue + enqueue döngüsü)
        char[] temp = new char[sz];
        for (int i = 0; i < sz; i++) {
            temp[i] = (char) backpackQueue.dequeue();
            backpackQueue.enqueue(temp[i]);
        }
        // 8 satırlık formatlanmış çıktı oluştur
        String[] lines = new String[8];
        for (int i = 0; i < 8; i++) {
            char ch = (i < sz) ? temp[i] : ' ';
            lines[i] = "| " + ch + "    |";
        }
        return lines;
    }

    public void toggleStorageMode() { storageTree = !storageTree; }

    public boolean isAlive() { return hp > 0; }
}
