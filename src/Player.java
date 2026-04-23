public class Player {

    public int  x, y;
    public int  hp           = 100;
    public int  score        = 0;
    public int  fireballCount = 0;
    public boolean storageTree = true;   // true=TREE, false=BACKPACK

    public char[] backpack     = new char[8];
    public int    backpackSize = 0;

    // Son hareket yönü (fireball için)
    public int lastDx = 1, lastDy = 0;

    // Duvara ve robota çarpmadan hareket; hareket edildiyse lastDx/lastDy güncellenir
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

    // Backpack'e ekle; dolu ise false döner
    public boolean addToBackpack(char symbol) {
        if (backpackSize >= backpack.length) return false;
        backpack[backpackSize++] = symbol;
        return true;
    }

    // Backpack'ten idx konumundaki elemanı al, diziyi kaydır
    public char removeFromBackpack(int idx) {
        if (idx < 0 || idx >= backpackSize) return 0;
        char sym = backpack[idx];
        for (int i = idx; i < backpackSize - 1; i++)
            backpack[i] = backpack[i + 1];
        backpackSize--;
        return sym;
    }

    public void toggleStorageMode() { storageTree = !storageTree; }

    public boolean isAlive() { return hp > 0; }
}
