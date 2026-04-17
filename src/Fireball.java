/**
 * Fireball.java
 * ==============
 * Oyuncunun ateşlediği aktif ateş tobu (o).
 *
 * Davranış:
 *   - Oyuncu SPACE'e basınca oluşturulur
 *   - Oyuncunun baktığı yöne doğru her tıkta 1 kare ilerler
 *   - Duvara veya engele çarparsa durur (devre dışı olur)
 *   - Robota çarparsa robotu yok eder (+50 skor) ve ilerlemeye devam eder
 *   - active=false olduğunda Game tarafından listeden çıkarılır
 */
public class Fireball {

    /** Ateş topunun satır konumu (0-20) */
    private int row;

    /** Ateş topunun sütun konumu (0-44) */
    private int col;

    /** Hareket yönü: Symbol.UP / DOWN / LEFT / RIGHT */
    private int direction;

    /**
     * Ateş tobu aktif mi?
     * false olduğunda nesne silinebilir/tekrar kullanılabilir.
     */
    private boolean active;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Yeni bir ateş tobu oluşturur.
     *
     * @param row       Başlangıç satırı (oyuncunun bir kare ilerisi)
     * @param col       Başlangıç sütunu
     * @param direction Hareket yönü (Symbol.UP/DOWN/LEFT/RIGHT)
     */
    public Fireball(int row, int col, int direction) {
        this.row       = row;
        this.col       = col;
        this.direction = direction;
        this.active    = true;
    }

    // ─── HAREKET ─────────────────────────────────────────────────────────────────

    /**
     * Ateş topunu bir kare ilerletir.
     * Önce yeni konumu hesaplar:
     *   - Yeni konum duvara veya geçilmez bir nesneye çarpıyorsa → devre dışı bırak
     *   - Geçerliyse → eski konumu temizle, yeni konuma yaz
     *
     * @param maze Oyun alanı (duvar ve engel kontrolü için)
     * @return true → ilerleyebildi, false → engele çarptı, devre dışı oldu
     */
    public boolean move(Maze maze) {
        if (!active) return false;

        // Yeni konumu hesapla
        int newRow = row;
        int newCol = col;

        switch (direction) {
            case Symbol.UP:    newRow--; break;
            case Symbol.DOWN:  newRow++; break;
            case Symbol.LEFT:  newCol--; break;
            case Symbol.RIGHT: newCol++; break;
        }

        // Sınır dışı veya duvara çarptıysa dur
        if (!maze.isInBounds(newRow, newCol) || maze.isWall(newRow, newCol)) {
            // Eski konumu temizle
            maze.setCell(row, col, Symbol.EMPTY);
            active = false;
            return false;
        }

        char target = maze.getCell(newRow, newCol);

        // Robota çarparsa: çarpma mantığı Game.java'da işlenir
        // (checkFireballRobotCollision metodunda)
        // Burada sadece mantık sembolü, ateş tobu veya oyuncu gibi engellere bak

        // Mantık sembolü, paketlenmiş ateş tobu → ateş tobu geçemez, durur
        if (Symbol.isLogicSymbol(target) || target == Symbol.FIREBALL_PACKED) {
            maze.setCell(row, col, Symbol.EMPTY);
            active = false;
            return false;
        }

        // Robotsa: Game.java çarpma olayını yakalar, ateş tobu ilerlemeye devam eder
        // (Robot için özel durum yok burada)

        // Oyuncuya çarparsa: ateş tobu durur (kendi oyuncusuna zarar vermez)
        if (target == Symbol.PLAYER) {
            maze.setCell(row, col, Symbol.EMPTY);
            active = false;
            return false;
        }

        // Normal hareket: eski konumu temizle, yeni konuma geç
        maze.setCell(row, col, Symbol.EMPTY);
        row = newRow;
        col = newCol;

        // Robot değilse hücreye kendi karakterini yaz
        // (Robot varsa Game.java çarpma işlemi yapar, biz yazmayız)
        if (target != Symbol.ROBOT) {
            maze.setCell(row, col, Symbol.FIREBALL_ACTIVE);
        }

        return true;
    }

    // ─── GETTER / SETTER ─────────────────────────────────────────────────────────

    public int getRow()       { return row; }
    public int getCol()       { return col; }
    public int getDirection() { return direction; }
    public boolean isActive() { return active; }

    /** Ateş topunu devre dışı bırakır (robota çarptığında Game.java çağırır) */
    public void deactivate()  { active = false; }
}
