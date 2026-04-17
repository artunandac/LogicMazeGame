import enigma.console.TextAttributes;
import enigma.console.TextWindow;
import enigma.core.Enigma;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Display.java
 * =============
 * Enigma kütüphanesi üzerinden tüm ekran çizim işlemlerini yönetir.
 *
 * Enigma API özeti:
 *   TextWindow.output(col, row, char, TextAttributes) → belirli koordinata karakter çizer
 *   TextAttributes(Color fg)         → sadece ön plan rengi
 *   TextAttributes(Color fg, Color bg) → ön plan + arka plan rengi
 *
 * PENCERE DÜZENİ (Maze ekranı):
 *   Sütun  0-44 : Maze alanı (21 satır × 45 sütun)
 *   Sütun    46 : Ayraç
 *   Sütun 47-69 : Bilgi paneli (Input queue, istatistikler, çanta)
 *
 * KLAVYE:
 *   Yön tuşları  → oyuncu hareketi
 *   SPACE        → ateş tobu
 *   1 / 2 / 3    → ekran geçişi
 *   M            → depolama modu değiştir
 *   W / A / D    → ağaç ekranında imleç hareketi
 *   T            → çantadan ağaca yerleştir
 *   R            → ağaçtan çantaya al (-2 ceza)
 *   F            → ağacı bitir / doğrula
 */
public class Display {

    // ─── PENCERE BOYUTLARI ───────────────────────────────────────────────────────

    /** Pencere satır sayısı */
    public static final int WIN_ROWS = 26;

    /** Pencere sütun sayısı */
    public static final int WIN_COLS = 72;

    /** Bilgi panelinin başladığı sütun */
    public static final int INFO_COL = 47;

    // ─── RENK SABİTLERİ (TextAttributes) ────────────────────────────────────────

    /** Normal beyaz metin */
    private static final TextAttributes ATTR_WHITE =
        new TextAttributes(Color.WHITE, Color.BLACK);

    /** Oyuncu rengi (sarı) */
    private static final TextAttributes ATTR_PLAYER =
        new TextAttributes(Color.YELLOW, Color.BLACK);

    /** Robot: rastgele (yeşil) */
    private static final TextAttributes ATTR_ROBOT_RANDOM =
        new TextAttributes(Color.GREEN, Color.BLACK);

    /** Robot: hedefli (kırmızı) */
    private static final TextAttributes ATTR_ROBOT_TARGETED =
        new TextAttributes(Color.RED, Color.BLACK);

    /** Duvar rengi (gri) */
    private static final TextAttributes ATTR_WALL =
        new TextAttributes(Color.LIGHT_GRAY, Color.DARK_GRAY);

    /** Ateş tobu rengi (turuncu) */
    private static final TextAttributes ATTR_FIREBALL =
        new TextAttributes(Color.ORANGE, Color.BLACK);

    /** Mantık sembolü rengi (camgöbeği) */
    private static final TextAttributes ATTR_SYMBOL =
        new TextAttributes(Color.CYAN, Color.BLACK);

    /** Ağaç imleci (yeşil arka plan) */
    private static final TextAttributes ATTR_CURSOR =
        new TextAttributes(Color.BLACK, Color.GREEN);

    /** Soru slotu (sarı arka plan, tablo ekranı) */
    private static final TextAttributes ATTR_QUESTION =
        new TextAttributes(Color.BLACK, Color.YELLOW);

    /** Başlık / bilgi metni (açık sarı) */
    private static final TextAttributes ATTR_INFO =
        new TextAttributes(Color.YELLOW, Color.BLACK);

    // ─── ALANLAR ─────────────────────────────────────────────────────────────────

    /** Enigma text penceresi */
    private TextWindow textWindow;

    /** Yön tuşları için basılı tutulan durum (true = basılı) */
    private boolean[] dirHeld;

    /**
     * Son basılan aksiyon tuşu (SPACE, 1, 2, 3, M, W, A, D, T, R, F).
     * Game.java her tıkta okur ve sıfırlar.
     */
    private int lastActionKey;

    // ─── YAPICI METOD & BAŞLATMA ─────────────────────────────────────────────────

    public Display() {
        dirHeld       = new boolean[4]; // UP=0, DOWN=1, LEFT=2, RIGHT=3
        lastActionKey = 0;
    }

    /**
     * Enigma penceresini oluşturur ve klavye dinleyicisini ekler.
     * Game.start() tarafından çağrılır.
     */
    public void init() {
        // Enigma TextWindow oluştur: başlık, satır sayısı, sütun sayısı
        textWindow = Enigma.createTextWindow("Logic Maze Game", WIN_ROWS, WIN_COLS);

        // Klavye dinleyicisi ekle
        textWindow.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // Yön tuşlarını "basılı tutuldu" olarak işaretle
                switch (key) {
                    case KeyEvent.VK_UP:    dirHeld[Symbol.UP]    = true; break;
                    case KeyEvent.VK_DOWN:  dirHeld[Symbol.DOWN]  = true; break;
                    case KeyEvent.VK_LEFT:  dirHeld[Symbol.LEFT]  = true; break;
                    case KeyEvent.VK_RIGHT: dirHeld[Symbol.RIGHT] = true; break;

                    // Aksiyon tuşları: Game.java tarafından işlenecek
                    default:
                        lastActionKey = key;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                // Yön tuşu bırakıldığında işareti kaldır
                switch (key) {
                    case KeyEvent.VK_UP:    dirHeld[Symbol.UP]    = false; break;
                    case KeyEvent.VK_DOWN:  dirHeld[Symbol.DOWN]  = false; break;
                    case KeyEvent.VK_LEFT:  dirHeld[Symbol.LEFT]  = false; break;
                    case KeyEvent.VK_RIGHT: dirHeld[Symbol.RIGHT] = false; break;
                }
            }
        });
    }

    // ─── TUŞ DURUMU OKUMA ────────────────────────────────────────────────────────

    /**
     * Belirtilen yön tuşu şu an basılı mı?
     * Game.java her tıkta bu metodu kontrol eder.
     *
     * @param dir Symbol.UP / DOWN / LEFT / RIGHT
     */
    public boolean isDirHeld(int dir) {
        return dirHeld[dir];
    }

    /**
     * Son basılan aksiyon tuşunu döndürür ve sıfırlar.
     * (SPACE, 1, 2, 3, M, W, A, D, T, R, F)
     *
     * @return KeyEvent.VK_* sabiti, basılmamışsa 0
     */
    public int consumeActionKey() {
        int key  = lastActionKey;
        lastActionKey = 0;
        return key;
    }

    // ─── EKRANI TEMİZLE ──────────────────────────────────────────────────────────

    /**
     * Tüm pencereyi boşlukla doldurarak temizler.
     */
    public void clear() {
        for (int r = 0; r < WIN_ROWS; r++) {
            for (int c = 0; c < WIN_COLS; c++) {
                textWindow.output(c, r, ' ', ATTR_WHITE);
            }
        }
    }

    // ─── MAZE EKRANI ─────────────────────────────────────────────────────────────

    /**
     * Maze ekranını (Ekran 1) tamamen çizer.
     * Maze ızgarası + bilgi paneli + sırt çantası
     *
     * @param maze   Oyun alanı
     * @param player Oyuncu bilgileri
     * @param robots Robot dizisi
     * @param robotCount Aktif robot sayısı
     * @param queue  Giriş kuyruğu
     * @param timeSec Geçen süre (saniye)
     */
    public void drawMazeScreen(Maze maze, Player player,
                               Robot[] robots, int robotCount,
                               InputQueue queue, int timeSec) {
        // ── 1. Maze ızgarası ────────────────────────────────────────────────────
        for (int r = 0; r < Maze.ROWS; r++) {
            for (int c = 0; c < Maze.COLS; c++) {
                char ch    = maze.getCell(r, c);
                TextAttributes attr = getAttributeForChar(ch, robots, robotCount);
                textWindow.output(c, r, ch, attr);
            }
        }

        // ── 2. Bilgi paneli (sağ taraf) ─────────────────────────────────────────
        drawInfoPanel(player, queue, timeSec);

        // ── 3. Sırt çantası ─────────────────────────────────────────────────────
        drawBackpack(player, 11);
    }

    /**
     * Bir karakterin rengini belirler.
     * Robot için: RANDOM → yeşil, TARGETED → kırmızı
     */
    private TextAttributes getAttributeForChar(char ch,
                                                Robot[] robots, int robotCount) {
        switch (ch) {
            case Symbol.WALL:            return ATTR_WALL;
            case Symbol.PLAYER:          return ATTR_PLAYER;
            case Symbol.FIREBALL_ACTIVE:
            case Symbol.FIREBALL_PACKED: return ATTR_FIREBALL;
            case Symbol.ROBOT:           return ATTR_ROBOT_TARGETED; // Varsayılan
            case Symbol.EMPTY:           return ATTR_WHITE;
            default:
                // Mantık sembolü
                if (Symbol.isLogicSymbol(ch)) return ATTR_SYMBOL;
                return ATTR_WHITE;
        }
    }

    /**
     * Sağ taraftaki bilgi panelini çizer.
     * Input queue, Time, Score, Fireball, Life, Storage
     */
    private void drawInfoPanel(Player player, InputQueue queue, int timeSec) {
        int c = INFO_COL;

        // Input Queue başlığı
        put(c, 0, "Input",           ATTR_INFO);
        put(c, 1, "<<<<<<<<<< ",     ATTR_WHITE);

        // Kuyruk içeriği (10 eleman, tek satırda)
        StringBuilder queueStr = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            queueStr.append(queue.get(i));
        }
        put(c, 2, queueStr.toString(), ATTR_SYMBOL);
        put(c, 3, "<<<<<<<<<< ",     ATTR_WHITE);

        // İstatistikler
        put(c, 5,  "Time     : " + timeSec,              ATTR_WHITE);
        put(c, 6,  "Score    : " + player.getScore(),     ATTR_WHITE);
        put(c, 7,  "Fireball : " + player.getFireballCount(), ATTR_WHITE);
        put(c, 8,  "Life     : " + player.getLife(),      ATTR_WHITE);
        put(c, 9,  "Storage  : " + player.getStorageModeString(), ATTR_INFO);
    }

    // ─── AĞAÇ EKRANI ─────────────────────────────────────────────────────────────

    /**
     * Ağaç ekranını (Ekran 2) çizer.
     * İfade ağacının ASCII görselini + bilgi panelini gösterir.
     *
     * @param tree   İfade ağacı
     * @param player Oyuncu bilgileri
     * @param queue  Giriş kuyruğu
     * @param timeSec Geçen süre
     */
    public void drawTreeScreen(ExpressionTree tree, Player player,
                               InputQueue queue, int timeSec) {
        // Ağaç görselini çiz (sol taraf)
        drawTreeAscii(tree);

        // İfadeyi göster (eğer finalize edildiyse)
        // Bu bilgi Game.java'dan gelir; şimdilik boş bırakılmış

        // Sağ taraf: bilgi paneli + çanta
        drawInfoPanel(player, queue, timeSec);
        drawBackpack(player, 11);
    }

    /**
     * İfade ağacını ASCII art olarak çizer.
     * Derinlik 1-4 arası 4 seviyeyi gösterir.
     *
     * Ağaç düzeni (PDF'deki gibi):
     *   Kök satır 2, derinlik 2 → satır 5, derinlik 3 → satır 9, derinlik 4 → satır 13
     *
     * TODO: Bu metod görsel iyileştirme için geliştirilebilir.
     *       Şimdilik basit bir yerleşim kullanılıyor.
     */
    private void drawTreeAscii(ExpressionTree tree) {
        // Düğüm pozisyonları: [col] her derinlik seviyesi için
        // Derinlik 1 (kök): sütun 22
        // Derinlik 2: sütunlar 11, 33
        // Derinlik 3: sütunlar 5, 17, 28, 39
        // Derinlik 4: sütunlar 2, 8, 14, 20, 25, 31, 37, 43

        int[] rowForDepth = {0, 1, 5, 9, 13}; // Derinlik 1-4 için satır
        int[][] colForNode = {
            {},                              // Kullanılmıyor (0. indeks)
            {22},                            // Derinlik 1: kök (indeks 1)
            {11, 33},                        // Derinlik 2: indeks 2, 3
            {5, 17, 28, 39},                 // Derinlik 3: indeks 4-7
            {2, 8, 14, 20, 25, 31, 37, 43}  // Derinlik 4: indeks 8-15
        };

        for (int depth = 1; depth <= 4; depth++) {
            int startIndex = (int) Math.pow(2, depth - 1); // İlk düğüm indeksi bu derinlikte
            int endIndex   = startIndex * 2 - 1;            // Son düğüm indeksi

            for (int nodeIdx = startIndex; nodeIdx <= endIndex && nodeIdx <= tree.getMaxNodes(); nodeIdx++) {
                char sym = tree.getNode(nodeIdx);
                int posInRow = nodeIdx - startIndex; // Satırdaki konum (0'dan başlar)

                if (posInRow >= colForNode[depth].length) break;
                int col = colForNode[depth][posInRow];
                int row = rowForDepth[depth];

                // İmleçse yeşil arka plan
                TextAttributes attr = (nodeIdx == tree.getCursorIndex())
                    ? ATTR_CURSOR
                    : (sym == Symbol.EMPTY ? ATTR_WHITE : ATTR_SYMBOL);

                char displayChar = (sym == Symbol.EMPTY) ? '.' : sym;
                textWindow.output(col, row, displayChar, attr);

                // Bağlantı çizgileri (basit '/' ve '\')
                if (depth < 4 && sym != Symbol.EMPTY) {
                    int childRow = rowForDepth[depth + 1];
                    // TODO: Bağlantı çizgilerini daha detaylı çizmek için geliştirilebilir
                }
            }
        }
    }

    // ─── TABLO EKRANI ─────────────────────────────────────────────────────────────

    /**
     * Tablo ekranını (Ekran 3) çizer.
     * Doğruluk tablosu + Karnaugh haritası
     *
     * @param table Hesaplanmış doğruluk tablosu
     */
    public void drawTableScreen(TruthTable table) {
        if (!table.isComputed()) {
            put(2, 5, "Tablo henüz hesaplanmadı.", ATTR_INFO);
            return;
        }

        // ── Tablo başlığı ────────────────────────────────────────────────────────
        put(0, 0, "ABCD | " + table.getSubExprName(0), ATTR_INFO);
        put(0, 1, "-----+", ATTR_WHITE);

        // ── Tablo satırları ──────────────────────────────────────────────────────
        for (int minterm = 0; minterm < TruthTable.NUM_ROWS; minterm++) {
            // ABCD değerlerini hesapla
            int a = (minterm >> 3) & 1;
            int b = (minterm >> 2) & 1;
            int c = (minterm >> 1) & 1;
            int d = (minterm)      & 1;

            String abcdStr = "" + a + b + c + d;
            int result     = table.getResult(minterm, 0);

            // Soru slotu mu?
            boolean isQuestion = (table.getQuestionSlot(0) == minterm);
            TextAttributes valAttr = isQuestion ? ATTR_QUESTION : ATTR_WHITE;

            String rowStr = abcdStr + " |  ";
            put(0, minterm + 2, rowStr, ATTR_WHITE);

            // Sonuç değeri (soru slotuysa sarı)
            textWindow.output(9, minterm + 2, (char)('0' + result), valAttr);
        }

        // ── Karnaugh haritası (sağ taraf, sütun 35'ten başlar) ──────────────────
        drawKarnaughMap(table, 35, 0);
    }

    /**
     * Karnaugh haritasını çizer.
     *
     * @param table  Doğruluk tablosu
     * @param startCol  Haritanın başladığı sütun
     * @param startRow  Haritanın başladığı satır
     */
    private void drawKarnaughMap(TruthTable table, int startCol, int startRow) {
        // Başlıklar
        put(startCol + 3, startRow,     "CD",      ATTR_INFO);
        put(startCol,     startRow + 1, "   00 01 11 10", ATTR_INFO);
        put(startCol,     startRow + 2, "AB",       ATTR_INFO);

        String[] rowLabels = {"00", "01", "11", "10"};

        for (int abRow = 0; abRow < 4; abRow++) {
            // Satır etiketi
            put(startCol, startRow + 3 + abRow, rowLabels[abRow], ATTR_INFO);

            for (int cdCol = 0; cdCol < 4; cdCol++) {
                int val = table.getKarnaughValue(abRow, cdCol);
                int col = startCol + 3 + cdCol * 3;
                int row = startRow + 3 + abRow;
                textWindow.output(col, row, (char)('0' + val), ATTR_WHITE);
            }
        }
    }

    // ─── YÜKSEK SKOR TABLOSU ─────────────────────────────────────────────────────

    /**
     * Yüksek skor tablosunu ekrana çizer.
     *
     * @param highScore Skor tablosu
     */
    public void drawHighScoreScreen(HighScoreTable highScore) {
        clear();
        put(25, 3, "=== YÜKSEK SKOR TABLOSU ===", ATTR_INFO);

        for (int i = 1; i <= highScore.getSize(); i++) {
            String name  = highScore.getNameAt(i);
            int    score = highScore.getScoreAt(i);
            String line  = i + ". " + padRight(name, 20) + " " + score;
            put(20, 3 + i + 1, line, ATTR_WHITE);
        }
    }

    // ─── SIRT ÇANTASI ────────────────────────────────────────────────────────────

    /**
     * Sırt çantasını bilgi panelinin alt kısmına çizer.
     *
     * @param player   Oyuncu
     * @param startRow Başlangıç satırı
     */
    private void drawBackpack(Player player, int startRow) {
        int c = INFO_COL;

        // Çanta çerçevesi
        put(c, startRow,     "+---+",     ATTR_WHITE);
        put(c, startRow + 9, "+---+",     ATTR_WHITE);
        put(c, startRow + 10,"Backpack",  ATTR_INFO);

        // Çanta içeriği (8 slot, ikişer ikişer yan yana)
        for (int i = 0; i < Player.BACKPACK_CAPACITY; i++) {
            char item = player.getBackpackItem(i);
            int  row  = startRow + 1 + i;
            put(c,     row, "| ", ATTR_WHITE);
            textWindow.output(c + 2, row,
                (item == Symbol.EMPTY) ? ' ' : item,
                Symbol.isLogicSymbol(item) ? ATTR_SYMBOL : ATTR_WHITE);
            put(c + 3, row, " |", ATTR_WHITE);
        }
    }

    // ─── MESAJ GÖSTER ────────────────────────────────────────────────────────────

    /**
     * Ekranın alt kısmında bir mesaj gösterir.
     * Hata mesajları, bildirimler için kullanılır.
     *
     * @param message Gösterilecek mesaj
     */
    public void showMessage(String message) {
        int row = WIN_ROWS - 2;
        // Önce satırı temizle
        for (int c = 0; c < WIN_COLS; c++) {
            textWindow.output(c, row, ' ', ATTR_WHITE);
        }
        put(2, row, message, ATTR_INFO);
    }

    // ─── YARDIMCI METODLAR ───────────────────────────────────────────────────────

    /**
     * Belirtilen konuma bir string yazar.
     *
     * @param col   Başlangıç sütunu
     * @param row   Satır
     * @param text  Yazılacak metin
     * @param attr  Renk nitelikleri
     */
    private void put(int col, int row, String text, TextAttributes attr) {
        for (int i = 0; i < text.length(); i++) {
            if (col + i < WIN_COLS && row < WIN_ROWS) {
                textWindow.output(col + i, row, text.charAt(i), attr);
            }
        }
    }

    /**
     * Bir string'i belirtilen uzunluğa sağdan boşlukla doldurur.
     * Yüksek skor tablosu hizalaması için kullanılır.
     */
    private String padRight(String s, int length) {
        if (s.length() >= length) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) sb.append(' ');
        return sb.toString();
    }
}
