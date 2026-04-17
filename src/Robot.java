import java.util.Random;

/**
 * Robot.java
 * ===========
 * Bilgisayar tarafından kontrol edilen düşman robot (X).
 *
 * İki tipi vardır (başlangıçta %50-%50 olasılıkla belirlenir):
 *   RANDOM   (Yeşil) → 4 yönden rastgele geçilebilir birine gider
 *   TARGETED (Kırmızı) → Maze'deki en yakın mantık sembolüne doğru hareket eder
 *
 * Hız: Oyuncunun 4 katı yavaş (her 4 tıkta 1 kare)
 * Davranış: Engellere çarparsa (duvar, başka nesne) o yönde hareket etmez
 * Ateş tobu çarparsa: yok olur, oyuncu +50 skor kazanır
 * Komşu karede oyuncu varsa: oyuncu her tıkta -5 can kaybeder
 */
public class Robot {

    // ─── TİP SABİTLERİ ───────────────────────────────────────────────────────────

    /** Rastgele hareket eden robot (yeşil) */
    public static final int RANDOM   = 0;

    /** Hedefe yönelen robot (kırmızı) */
    public static final int TARGETED = 1;

    // ─── ALANLAR ─────────────────────────────────────────────────────────────────

    /** Robotun satır konumu */
    private int row;

    /** Robotun sütun konumu */
    private int col;

    /** Robot tipi: RANDOM veya TARGETED */
    private int type;

    /**
     * Robot hala aktif mi (hayatta mı)?
     * false → ateş tobu çarptı, yok edildi
     */
    private boolean active;

    /** Hareket sayacı: 4'e ulaşınca robot 1 kare hareket eder */
    private int moveCounter;

    /** Rastgele hareket için */
    private Random random;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Yeni bir robot oluşturur. Tipi %50-%50 olasılıkla belirlenir.
     *
     * @param row Başlangıç satırı
     * @param col Başlangıç sütunu
     */
    public Robot(int row, int col) {
        this.row  = row;
        this.col  = col;
        this.active      = true;
        this.moveCounter = 0;
        this.random      = new Random();

        // %50 RANDOM, %50 TARGETED
        this.type = (random.nextInt(2) == 0) ? RANDOM : TARGETED;
    }

    // ─── GÜNCELLEME ──────────────────────────────────────────────────────────────

    /**
     * Her oyun tıkında çağrılır. Robot 4 tıkta bir hareket eder.
     *
     * @param maze Oyun alanı (duvar/engel kontrolü için)
     */
    public void update(Maze maze) {
        if (!active) return;

        moveCounter++;
        if (moveCounter < 4) {
            return; // Henüz hareket zamanı değil
        }
        moveCounter = 0; // Sayacı sıfırla

        // Tipe göre hareket yöntemi seç
        if (type == RANDOM) {
            moveRandom(maze);
        } else {
            moveTargeted(maze);
        }
    }

    // ─── HAREKETİ: RASTGELE ──────────────────────────────────────────────────────

    /**
     * 4 yönden geçilebilir olanlar arasından rastgele birini seçerek hareket eder.
     * Geçilebilir yön yoksa hareket etmez.
     *
     * @param maze Oyun alanı
     */
    private void moveRandom(Maze maze) {
        // 4 yönü karıştır ve geçilebilir ilk yöne git
        int[] dirs = {Symbol.UP, Symbol.DOWN, Symbol.LEFT, Symbol.RIGHT};

        // Fisher-Yates shuffle (dizi karıştırma)
        for (int i = 3; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = temp;
        }

        // Karıştırılmış yönlerde ilk geçilebiliri dene
        for (int dir : dirs) {
            if (tryMove(maze, dir)) {
                return;
            }
        }
        // Tüm yönler engelliyse hareketsiz kal
    }

    // ─── HAREKETİ: HEDEFLİ ───────────────────────────────────────────────────────

    /**
     * En yakın mantık sembolüne doğru hareket eder.
     * Roblar engellere çarparsa (duvar, nesne) o yönde devam edemez.
     * NOT: Tam yol bulma algoritması (pathfinding) YOKTUR.
     *      Sadece Manhattan mesafesini azaltacak yöne gidilir.
     *
     * @param maze Oyun alanı
     */
    private void moveTargeted(Maze maze) {
        // Maze'deki en yakın mantık sembolünü bul
        int[] target = findNearestSymbol(maze);

        if (target == null) {
            // Maze'de mantık sembolü yoksa rastgele hareket et
            moveRandom(maze);
            return;
        }

        int targetRow = target[0];
        int targetCol = target[1];
        int dr = targetRow - row; // Dikey fark
        int dc = targetCol - col; // Yatay fark

        // Önce büyük farka göre birincil yönü belirle,
        // engellenirse ikincil yönü dene
        int primaryDir, secondaryDir;

        if (Math.abs(dr) >= Math.abs(dc)) {
            // Dikey mesafe daha büyük
            primaryDir   = (dr > 0) ? Symbol.DOWN  : Symbol.UP;
            secondaryDir = (dc > 0) ? Symbol.RIGHT : Symbol.LEFT;
        } else {
            // Yatay mesafe daha büyük
            primaryDir   = (dc > 0) ? Symbol.RIGHT : Symbol.LEFT;
            secondaryDir = (dr > 0) ? Symbol.DOWN  : Symbol.UP;
        }

        // Önce birincil yönü dene
        if (tryMove(maze, primaryDir)) return;
        // Sonra ikincil yönü dene
        if (tryMove(maze, secondaryDir)) return;
        // Her ikisi de engelliyse hareketsiz kal
    }

    // ─── YARDIMCI: HAREKET DENEME ────────────────────────────────────────────────

    /**
     * Belirtilen yönde hareket etmeyi dener.
     * Hedef hücre geçilebilirse (boş veya mantık sembolü ise) hareketi gerçekleştirir.
     * Robot mantık sembolü toplayan bir hücreye giderse sembolü siler.
     *
     * @param maze Oyun alanı
     * @param dir  Yön (Symbol.UP/DOWN/LEFT/RIGHT)
     * @return true → hareket başarılı, false → engellenildi
     */
    private boolean tryMove(Maze maze, int dir) {
        int newRow = row;
        int newCol = col;

        switch (dir) {
            case Symbol.UP:    newRow--; break;
            case Symbol.DOWN:  newRow++; break;
            case Symbol.LEFT:  newCol--; break;
            case Symbol.RIGHT: newCol++; break;
        }

        // Sınır kontrolü
        if (!maze.isInBounds(newRow, newCol)) return false;

        char target = maze.getCell(newRow, newCol);

        // Yalnızca boş veya mantık sembolü olan karelere gidebilir
        // Duvar, oyuncu, başka robot, ateş tobu → geçemez
        if (target != Symbol.EMPTY && !Symbol.isLogicSymbol(target)) {
            return false;
        }

        // Eski konumu temizle
        maze.setCell(row, col, Symbol.EMPTY);

        // Yeni konuma taşın (mantık sembolü varsa toplanır/silinir)
        row = newRow;
        col = newCol;
        maze.setCell(row, col, Symbol.ROBOT);

        return true;
    }

    // ─── YARDIMCI: EN YAKIN SEMBOL ───────────────────────────────────────────────

    /**
     * Maze'deki tüm mantık sembollerini tarar ve Manhattan mesafesiyle
     * en yakın olanın konumunu döndürür.
     *
     * @param maze Oyun alanı
     * @return [row, col] dizisi veya null (maze'de sembol yoksa)
     */
    private int[] findNearestSymbol(Maze maze) {
        int minDist  = Integer.MAX_VALUE;
        int[] result = null;

        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                if (Symbol.isLogicSymbol(maze.getCell(r, c))) {
                    // Manhattan mesafesi = |satır farkı| + |sütun farkı|
                    int dist = Math.abs(r - row) + Math.abs(c - col);
                    if (dist < minDist) {
                        minDist  = dist;
                        result   = new int[]{r, c};
                    }
                }
            }
        }
        return result;
    }

    // ─── GETTER / SETTER ─────────────────────────────────────────────────────────

    public int  getRow()       { return row; }
    public int  getCol()       { return col; }
    public int  getType()      { return type; }
    public boolean isActive()  { return active; }

    /** Robot ateş topuyla vurulduğunda Game.java bu metodu çağırır */
    public void destroy(Maze maze) {
        maze.setCell(row, col, Symbol.EMPTY);
        active = false;
    }
}
