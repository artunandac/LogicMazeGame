/**
 * Player.java
 * ============
 * İnsan oyuncuyu (P) temsil eden sınıf.
 *
 * Özellikler:
 *   - 100 can puanıyla başlar
 *   - 8 eşya kapasiteli sırt çantası (dizi)
 *   - İki depolama modu: TREE veya BACKPACK (M tuşuyla değiştirilir)
 *   - Ateş tobu tutabilir (@ toplayarak)
 *   - Cursor tuşlarıyla 4 yönde hareket
 *   - SPACE ile ateş tobu ateşler
 */
public class Player {

    // ─── DEPOLAMA MODU SABİTLERİ ─────────────────────────────────────────────────

    /** Semboller doğrudan ağaca yerleştirilir */
    public static final int STORAGE_TREE    = 0;

    /** Semboller sırt çantasına yerleştirilir */
    public static final int STORAGE_BACKPACK = 1;

    // ─── SIRT ÇANTASI SABİTİ ─────────────────────────────────────────────────────

    /** Sırt çantasının maksimum kapasitesi */
    public static final int BACKPACK_CAPACITY = 8;

    // ─── ALANLAR ─────────────────────────────────────────────────────────────────

    /** Oyuncunun satır konumu */
    private int row;

    /** Oyuncunun sütun konumu */
    private int col;

    /** Can puanı (0 olunca oyun biter) */
    private int life;

    /** Skor puanı */
    private int score;

    /** Elde tuttuğu ateş tobu sayısı */
    private int fireballCount;

    /**
     * Son hareket yönü (ateş tobu bu yöne atılır).
     * Başlangıçta sağ yönü varsayılır.
     */
    private int direction;

    /**
     * Aktif depolama modu: STORAGE_TREE veya STORAGE_BACKPACK
     * Başlangıçta TREE modunda başlar.
     */
    private int storageMode;

    /**
     * Sırt çantası (dizi tabanlı).
     * Boş slotlar Symbol.EMPTY (' ') ile temsil edilir.
     */
    private char[] backpack;

    /** Sırt çantasındaki mevcut eşya sayısı */
    private int backpackSize;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Yeni bir oyuncu oluşturur.
     *
     * @param row Başlangıç satırı
     * @param col Başlangıç sütunu
     */
    public Player(int row, int col) {
        this.row           = row;
        this.col           = col;
        this.life          = 100;
        this.score         = 0;
        this.fireballCount = 0;
        this.direction     = Symbol.RIGHT; // Varsayılan yön
        this.storageMode   = STORAGE_TREE;

        // Sırt çantası başlangıçta boş
        this.backpack     = new char[BACKPACK_CAPACITY];
        this.backpackSize = 0;
        for (int i = 0; i < BACKPACK_CAPACITY; i++) {
            backpack[i] = Symbol.EMPTY;
        }
    }

    // ─── HAREKET ─────────────────────────────────────────────────────────────────

    /**
     * Oyuncuyu bir yönde hareket ettirmeyi dener.
     * Hareket başarılıysa maze güncellenir ve yön güncellenir.
     *
     * Hareket kuralları:
     *   - Duvar → hareket yok
     *   - Başka eleman (robot, ateş tobu) → duvar gibi davranır
     *   - Mantık sembolü → toplanır, hücre boşalır
     *   - Paketlenmiş ateş tobu → toplanır (fireballCount artar)
     *
     * @param dir  Yön (Symbol.UP / DOWN / LEFT / RIGHT)
     * @param maze Oyun alanı
     * @param tree Ağaç (sembol toplandığında TREE modunda kullanılır)
     * @return true → hareket başarılı
     */
    public boolean move(int dir, Maze maze, ExpressionTree tree) {
        direction = dir; // Yönü güncelle (ateş tobu bu yöne gidecek)

        int newRow = row;
        int newCol = col;

        switch (dir) {
            case Symbol.UP:    newRow--; break;
            case Symbol.DOWN:  newRow++; break;
            case Symbol.LEFT:  newCol--; break;
            case Symbol.RIGHT: newCol++; break;
        }

        // Sınır dışı kontrol
        if (!maze.isInBounds(newRow, newCol)) return false;

        char target = maze.getCell(newRow, newCol);

        // Duvar veya geçilemez nesne → hareket yok
        if (!canMoveTo(target)) return false;

        // Eski konumu temizle
        maze.setCell(row, col, Symbol.EMPTY);

        // Hedefe göre işlem yap
        if (Symbol.isLogicSymbol(target)) {
            collectLogicSymbol(target, tree); // Sembolü topla
        } else if (target == Symbol.FIREBALL_PACKED) {
            fireballCount++; // Ateş tobu topla
        }

        // Yeni konuma taşın
        row = newRow;
        col = newCol;
        maze.setCell(row, col, Symbol.PLAYER);

        return true;
    }

    /**
     * Hedef hücreye gidilebilir mi?
     * Boş, mantık sembolü veya paketlenmiş ateş tobu → evet
     * Duvar, robot, aktif ateş tobu → hayır
     */
    private boolean canMoveTo(char target) {
        return target == Symbol.EMPTY
            || Symbol.isLogicSymbol(target)
            || target == Symbol.FIREBALL_PACKED;
    }

    // ─── SEMBOL TOPLAMA ───────────────────────────────────────────────────────────

    /**
     * Toplanan mantık sembolünü moda göre ağaca veya sırt çantasına yerleştirir.
     *
     * Kural:
     *   - TREE modu → ağaca yerleştir
     *   - BACKPACK modu ve çanta dolmamışsa → sırt çantasına ekle
     *   - BACKPACK modu ama çanta doluysa → ağaca yerleştir
     *
     * @param symbol Toplanan sembol
     * @param tree   İfade ağacı
     */
    public void collectLogicSymbol(char symbol, ExpressionTree tree) {
        if (storageMode == STORAGE_TREE) {
            // TREE modu: doğrudan ağaca
            tree.placeAtCursor(symbol);
        } else {
            // BACKPACK modu
            if (!isBackpackFull()) {
                addToBackpack(symbol); // Sırt çantasına ekle
            } else {
                tree.placeAtCursor(symbol); // Çanta doluysa ağaca
            }
        }
    }

    // ─── SIRT ÇANTASI ────────────────────────────────────────────────────────────

    /**
     * Sırt çantasına bir eşya ekler.
     * Çanta doluysa ekleme yapılmaz.
     *
     * @param item Eklenecek eşya karakteri
     * @return true → ekleme başarılı
     */
    public boolean addToBackpack(char item) {
        if (isBackpackFull()) return false;

        // İlk boş slotu bul ve ekle
        for (int i = 0; i < BACKPACK_CAPACITY; i++) {
            if (backpack[i] == Symbol.EMPTY) {
                backpack[i] = item;
                backpackSize++;
                return true;
            }
        }
        return false;
    }

    /**
     * Sırt çantasından belirli indeksteki eşyayı çıkarır.
     *
     * @param index Çantadaki slot indeksi (0-7)
     * @return Çıkarılan eşya, yoksa Symbol.EMPTY
     */
    public char removeFromBackpack(int index) {
        if (index < 0 || index >= BACKPACK_CAPACITY) return Symbol.EMPTY;
        if (backpack[index] == Symbol.EMPTY) return Symbol.EMPTY;

        char item = backpack[index];
        backpack[index] = Symbol.EMPTY;
        backpackSize--;
        return item;
    }

    /**
     * Sırt çantasındaki ilk dolu slottaki eşyayı ağaca yerleştirir (T tuşu).
     * Yerleştirme başarılıysa çantadan çıkarılır.
     *
     * @param tree İfade ağacı
     * @return true → başarılı
     */
    public boolean placeFromBackpackToTree(ExpressionTree tree) {
        // Çantada eşya var mı?
        for (int i = 0; i < BACKPACK_CAPACITY; i++) {
            if (backpack[i] != Symbol.EMPTY) {
                char item = removeFromBackpack(i);
                tree.placeAtCursor(item);
                return true;
            }
        }
        return false; // Çanta boş
    }

    /** Sırt çantası dolu mu? */
    public boolean isBackpackFull() {
        return backpackSize >= BACKPACK_CAPACITY;
    }

    /** Sırt çantası boş mu? */
    public boolean isBackpackEmpty() {
        return backpackSize == 0;
    }

    // ─── ATEŞ TOBU ───────────────────────────────────────────────────────────────

    /**
     * Ateş tobu ateşler (SPACE tuşu).
     * Ateş tobu yoksa ateşleme yapılamaz.
     *
     * @return Yeni oluşturulan Fireball nesnesi veya null (ateş tobu yoksa)
     */
    public Fireball fire() {
        if (fireballCount <= 0) return null;

        fireballCount--;

        // Oyuncunun baktığı yönde bir kare ileriden başlat
        int fbRow = row;
        int fbCol = col;
        switch (direction) {
            case Symbol.UP:    fbRow--; break;
            case Symbol.DOWN:  fbRow++; break;
            case Symbol.LEFT:  fbCol--; break;
            case Symbol.RIGHT: fbCol++; break;
        }

        return new Fireball(fbRow, fbCol, direction);
    }

    // ─── DEPOLAMA MODU ───────────────────────────────────────────────────────────

    /**
     * Depolama modunu değiştirir (M tuşu).
     * TREE ↔ BACKPACK
     */
    public void toggleStorageMode() {
        storageMode = (storageMode == STORAGE_TREE) ? STORAGE_BACKPACK : STORAGE_TREE;
    }

    // ─── CAN VE SKOR ─────────────────────────────────────────────────────────────

    /**
     * Can puanını düşürür (komşu robot, yanlış tablo cevabı vb.)
     *
     * @param amount Düşürülecek miktar (pozitif sayı)
     */
    public void loseLife(int amount) {
        life -= amount;
        if (life < 0) life = 0;
    }

    /**
     * Skora puan ekler.
     *
     * @param amount Eklenecek puan (pozitif veya negatif/ceza)
     */
    public void addScore(int amount) {
        score += amount;
    }

    /** Oyuncu öldü mü? (can = 0) */
    public boolean isDead() {
        return life <= 0;
    }

    // ─── GETTER / SETTER ─────────────────────────────────────────────────────────

    public int  getRow()          { return row; }
    public int  getCol()          { return col; }
    public int  getLife()         { return life; }
    public int  getScore()        { return score; }
    public int  getFireballCount(){ return fireballCount; }
    public int  getDirection()    { return direction; }
    public int  getStorageMode()  { return storageMode; }
    public int  getBackpackSize() { return backpackSize; }

    /** Sırt çantasındaki i. slotun içeriğini döndürür */
    public char getBackpackItem(int i) {
        if (i < 0 || i >= BACKPACK_CAPACITY) return Symbol.EMPTY;
        return backpack[i];
    }

    /** Oyuncu konumunu günceller (başlangıç yerleştirmesi için) */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /** Depolama modunu string olarak döndürür (ekranda göstermek için) */
    public String getStorageModeString() {
        return (storageMode == STORAGE_TREE) ? "Tree" : "Backpack";
    }
}
