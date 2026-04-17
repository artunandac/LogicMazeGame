import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Maze.java
 * ==========
 * 21 satır × 45 sütunluk oyun alanını (ızgarayı) yöneten sınıf.
 *
 * Sadece char[][] ızgara tutar ve üzerindeki işlemleri sağlar.
 * Oyun elemanları (Player, Robot, Fireball) kendi sınıflarında yönetilir
 * ve bu sınıf üzerinden maze'e yansıtılır.
 *
 * Dosya formatı (maze.txt):
 *   - 21 satır, her satır tam 45 karakter
 *   - '#' = duvar, ' ' = boş kare
 *   - Satırlar 45 karakterden kısaysa boşlukla doldurulur
 */
public class Maze {

    // ─── BOYUT SABİTLERİ ─────────────────────────────────────────────────────────

    /** Satır sayısı */
    public static final int ROWS = 21;

    /** Sütun sayısı */
    public static final int COLS = 45;

    // ─── ALANLAR ─────────────────────────────────────────────────────────────────

    /** Oyun alanının ızgarası */
    private char[][] grid;

    /** Rastgele konum bulmak için */
    private Random random;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Tamamen boş bir maze oluşturur.
     * Gerçek maze loadFromFile() ile yüklenir.
     */
    public Maze() {
        grid   = new char[ROWS][COLS];
        random = new Random();

        // Başlangıçta tüm hücreleri boş yap
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = Symbol.EMPTY;
            }
        }
    }

    // ─── DOSYADAN YÜKLEME ────────────────────────────────────────────────────────

    /**
     * Maze dosyasını okuyarak ızgarayı doldurur.
     * Sadece duvar (#) ve boş alan ( ) yüklenir.
     * Eğer dosya okunamazsa, kenarlı basit bir maze oluşturulur.
     *
     * @param filename Maze dosyasının yolu (örnek: "maze.txt")
     */
    public void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for (int r = 0; r < ROWS; r++) {
                String line = reader.readLine();

                if (line == null) {
                    // Dosya beklenenden kısa → kalan satırları boş yap
                    for (int c = 0; c < COLS; c++) grid[r][c] = Symbol.EMPTY;
                    continue;
                }

                for (int c = 0; c < COLS; c++) {
                    if (c < line.length()) {
                        char ch = line.charAt(c);
                        // Sadece duvar karakterini al; diğerleri başlangıçta boştur
                        grid[r][c] = (ch == Symbol.WALL) ? Symbol.WALL : Symbol.EMPTY;
                    } else {
                        grid[r][c] = Symbol.EMPTY; // Kısa satırı doldur
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Uyarı: " + filename + " okunamadı. Varsayılan maze kullanılıyor.");
            buildDefaultMaze(); // Dosya yoksa basit bir maze yap
        }
    }

    /**
     * Dosya yoksa çalışan basit bir yedek maze oluşturur.
     * Sadece kenarlarda duvar olan, içi boş bir kutu.
     */
    private void buildDefaultMaze() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Kenar satır ve sütunlar duvar
                if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                    grid[r][c] = Symbol.WALL;
                } else {
                    grid[r][c] = Symbol.EMPTY;
                }
            }
        }
    }

    // ─── IZGARA ERİŞİMİ ──────────────────────────────────────────────────────────

    /**
     * Belirtilen hücrenin içeriğini döndürür.
     *
     * @param row Satır (0-20)
     * @param col Sütun (0-44)
     * @return Hücre karakteri
     */
    public char getCell(int row, int col) {
        if (!isInBounds(row, col)) return Symbol.WALL; // Sınır dışı = duvar
        return grid[row][col];
    }

    /**
     * Belirtilen hücreye karakter yazar.
     *
     * @param row  Satır (0-20)
     * @param col  Sütun (0-44)
     * @param cell Yazılacak karakter
     */
    public void setCell(int row, int col, char cell) {
        if (isInBounds(row, col)) {
            grid[row][col] = cell;
        }
    }

    // ─── DURUM KONTROL ───────────────────────────────────────────────────────────

    /**
     * Verilen konum ızgara sınırları içinde mi?
     *
     * @param row Satır
     * @param col Sütun
     * @return true → sınırlar içinde
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /**
     * Belirtilen hücre duvar mı?
     */
    public boolean isWall(int row, int col) {
        return getCell(row, col) == Symbol.WALL;
    }

    /**
     * Belirtilen hücre tamamen boş mu?
     */
    public boolean isEmpty(int row, int col) {
        return getCell(row, col) == Symbol.EMPTY;
    }

    // ─── RASTGELE BOŞ KONUM ───────────────────────────────────────────────────────

    /**
     * Maze içinde rastgele bir boş konum bulur.
     * Kullanım: Yeni eleman yerleştirme, oyuncu başlangıcı vb.
     *
     * @return [row, col] dizisi veya null (100 denemede boş bulunamazsa)
     */
    public int[] findRandomEmptySpot() {
        int maxTries = 200;
        for (int i = 0; i < maxTries; i++) {
            // Kenarlardaki duvar sırasını dışla (1'den ROWS-2'ye kadar)
            int r = 1 + random.nextInt(ROWS - 2);
            int c = 1 + random.nextInt(COLS - 2);
            if (isEmpty(r, c)) {
                return new int[]{r, c};
            }
        }
        return null; // Çok nadir: maze çok doluysa
    }

    /**
     * Belirtilen konumun yakınında (komşu 4 karede) belirli bir karakter var mı?
     * Komşu hasar kontrolü için kullanılır.
     *
     * @param row    Merkez satırı
     * @param col    Merkez sütunu
     * @param target Aranacak karakter
     * @return true → komşu karede hedef karakter var
     */
    public boolean hasNeighbor(int row, int col, char target) {
        int[] dr = {-1, 1,  0, 0};
        int[] dc = { 0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nr = row + dr[i];
            int nc = col + dc[i];
            if (isInBounds(nr, nc) && grid[nr][nc] == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * Belirtilen konumun 4 komşusunda kaç tane hedef karakter var?
     * Birden fazla robot komşusuysa birden fazla hasar için kullanılır.
     *
     * @param row    Merkez satırı
     * @param col    Merkez sütunu
     * @param target Sayılacak karakter
     * @return Komşulardaki hedef karakter sayısı (0-4)
     */
    public int countNeighbors(int row, int col, char target) {
        int[] dr = {-1, 1,  0, 0};
        int[] dc = { 0, 0, -1, 1};
        int count = 0;

        for (int i = 0; i < 4; i++) {
            int nr = row + dr[i];
            int nc = col + dc[i];
            if (isInBounds(nr, nc) && grid[nr][nc] == target) {
                count++;
            }
        }
        return count;
    }
}
