import java.awt.event.KeyEvent;
import javax.swing.Timer;

/**
 * Game.java
 * ==========
 * Ana oyun kontrolcüsü. Tüm oyun nesnelerini tutar ve oyun döngüsünü yönetir.
 *
 * EKRANLAR:
 *   SCREEN_MAZE  (Key 1) → Maze ekranı
 *   SCREEN_TREE  (Key 2) → Ağaç ekranı
 *   SCREEN_TABLE (Key 3) → Tablo ekranı
 *
 * OYUN DÖNGÜSÜ (javax.swing.Timer, 100ms aralıkla):
 *   Her tıkta:
 *     1. Klavye girdisini işle
 *     2. Ateş toplarını hareket ettir
 *     3. Ateş tobu - robot çarpışmalarını kontrol et
 *     4. Her 4 tıkta bir robotları hareket ettir
 *     5. Her 20 tıkta bir kuyruktan maze'e yeni eleman ekle
 *     6. Komşu kare hasarı hesapla
 *     7. Ekranı çiz
 *
 * ZAMANLAMA (1 tık = 100ms):
 *   Oyuncu hareketi    : her tık (yön tuşu basılıysa)
 *   Robot hareketi     : her 4 tık (400ms)
 *   Ateş tobu hareketi : her tık (100ms)
 *   Yeni kuyruk elemanı: her 20 tık (2 saniye)
 *   Ekran zamanı       : tık / 10 (saniye)
 */
public class Game {

    // ─── EKRAN SABİTLERİ ─────────────────────────────────────────────────────────

    public static final int SCREEN_MAZE  = 1;
    public static final int SCREEN_TREE  = 2;
    public static final int SCREEN_TABLE = 3;

    // ─── OYUN NESNELERI ──────────────────────────────────────────────────────────

    /** Oyun alanı ızgarası */
    private Maze maze;

    /** İnsan oyuncu */
    private Player player;

    /** Düşman robotlar (dizi; aktif/pasif bayrağıyla yönetilir) */
    private Robot[] robots;

    /** Aktif robot sayısı (dizi indeksini takip eder) */
    private int robotCount;

    /** Aktif ateş topları */
    private Fireball[] fireballs;

    /** Aktif ateş tobu sayısı */
    private int fireballCount;

    /** Giriş kuyruğu */
    private InputQueue inputQueue;

    /** İfade ağacı */
    private ExpressionTree tree;

    /** Doğruluk tablosu (ağaç tamamlandıktan sonra doldurulur) */
    private TruthTable table;

    /** Yüksek skor tablosu */
    private HighScoreTable highScore;

    /** Ekran çizimi ve klavye girişi */
    private Display display;

    // ─── OYUN DURUMU ─────────────────────────────────────────────────────────────

    /** Aktif ekran: SCREEN_MAZE / SCREEN_TREE / SCREEN_TABLE */
    private int currentScreen;

    /** Oyun hala devam ediyor mu? */
    private boolean running;

    /** Geçen tık sayısı */
    private int tick;

    /** Swing Timer: her 100ms bir tık üretir */
    private Timer gameTimer;

    // Maksimum dizi boyutları
    private static final int MAX_ROBOTS    = 20;
    private static final int MAX_FIREBALLS = 10;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    public Game() {
        maze          = new Maze();
        tree          = new ExpressionTree();
        inputQueue    = new InputQueue();
        highScore     = new HighScoreTable();
        display       = new Display();

        robots        = new Robot[MAX_ROBOTS];
        fireballs     = new Fireball[MAX_FIREBALLS];
        robotCount    = 0;
        fireballCount = 0;

        currentScreen = SCREEN_MAZE;
        running       = false;
        tick          = 0;
    }

    // ─── BAŞLATMA ────────────────────────────────────────────────────────────────

    /**
     * Oyunu başlatır:
     *   1. Ekranı oluştur (Enigma penceresi)
     *   2. Maze'i dosyadan yükle
     *   3. Yüksek skor tablosunu yükle
     *   4. Oyuncuyu rastgele konuma yerleştir
     *   5. Kuyruğu doldur ve ilk 10 elemanı maze'e yerleştir
     *   6. Oyun döngüsünü başlat
     */
    public void start() {
        // Enigma penceresini oluştur ve klavye dinleyicisini ekle
        display.init();

        // Maze'i dosyadan yükle
        maze.loadFromFile("maze.txt");

        // Yüksek skor tablosunu yükle
        highScore.loadFromFile("highscore.txt");

        // Kuyruğu rastgele elemanlarla doldur
        inputQueue.fill();

        // Oyuncuyu rastgele boş bir konuma yerleştir
        int[] startPos = maze.findRandomEmptySpot();
        if (startPos != null) {
            player = new Player(startPos[0], startPos[1]);
            maze.setCell(startPos[0], startPos[1], Symbol.PLAYER);
        } else {
            player = new Player(1, 1); // Yedek konum
        }

        // İlk 10 kuyruk elemanını maze'e yerleştir (oyun başlangıcı için)
        for (int i = 0; i < 10; i++) {
            spawnFromQueue();
        }

        running = true;

        // Swing Timer: her 100ms'de tick() metodunu çağır
        // Swing EDT (Event Dispatch Thread) üzerinde çalışır →
        // klavye eventi ve oyun güncellemesi aynı thread'de, thread-safe
        gameTimer = new Timer(100, e -> tick());
        gameTimer.start();
    }

    // ─── OYUN DÖNGÜSÜ (HER 100MS) ────────────────────────────────────────────────

    /**
     * Her 100ms'de bir çağrılan ana güncelleme metodu.
     * Sırasıyla giriş, güncelleme ve çizim işlemlerini yapar.
     */
    private void tick() {
        if (!running) {
            gameTimer.stop();
            gameOver();
            return;
        }

        // ── 1. Klavye girdisini işle ─────────────────────────────────────────────
        handleInput();

        // ── 2. Maze ekranındaki oyun mekaniğini güncelle ─────────────────────────
        if (currentScreen == SCREEN_MAZE) {
            updateFireballs();                          // Her tık: ateş topları
            checkFireballRobotCollisions();             // Ateş tobu-robot çarpışması
            if (tick % 4 == 0) updateRobots();         // Her 4 tık: robotlar
            if (tick % 20 == 0 && tick > 0) spawnFromQueue(); // Her 20 tık: yeni eleman
            checkNeighborHarm();                        // Komşu robot hasarı
        }

        // ── 3. Ekranı çiz ────────────────────────────────────────────────────────
        render();

        tick++;

        // Oyun bitti mi kontrol et
        if (player.isDead()) {
            running = false;
        }
    }

    // ─── GİRDİ İŞLEME ────────────────────────────────────────────────────────────

    /**
     * Klavye girdisini ekrana göre işler.
     * Yön tuşları: Display.isDirHeld() ile kontrol edilir (basılı tutulabilir)
     * Aksiyon tuşları: Display.consumeActionKey() ile tek seferlik alınır
     */
    private void handleInput() {
        // ── Aksiyon tuşları (tek seferlik) ───────────────────────────────────────
        int key = display.consumeActionKey();

        if (key == KeyEvent.VK_1) {
            currentScreen = SCREEN_MAZE;
            return;
        }
        if (key == KeyEvent.VK_2) {
            currentScreen = SCREEN_TREE;
            return;
        }
        if (key == KeyEvent.VK_3) {
            currentScreen = SCREEN_TABLE;
            return;
        }

        // ── Maze ekranı tuşları ───────────────────────────────────────────────────
        if (currentScreen == SCREEN_MAZE) {
            // M: depolama modunu değiştir
            if (key == KeyEvent.VK_M) {
                player.toggleStorageMode();
            }
            // SPACE: ateş tobu ateşle
            if (key == KeyEvent.VK_SPACE) {
                Fireball fb = player.fire();
                if (fb != null) {
                    addFireball(fb);
                    // Ateş topunun başlangıç konumunu maze'e yaz
                    if (maze.isInBounds(fb.getRow(), fb.getCol())
                     && maze.isEmpty(fb.getRow(), fb.getCol())) {
                        maze.setCell(fb.getRow(), fb.getCol(), Symbol.FIREBALL_ACTIVE);
                    }
                }
            }

            // Yön tuşları: basılı tutuluyorsa hareket et
            if      (display.isDirHeld(Symbol.UP))    player.move(Symbol.UP,    maze, tree);
            else if (display.isDirHeld(Symbol.DOWN))  player.move(Symbol.DOWN,  maze, tree);
            else if (display.isDirHeld(Symbol.LEFT))  player.move(Symbol.LEFT,  maze, tree);
            else if (display.isDirHeld(Symbol.RIGHT)) player.move(Symbol.RIGHT, maze, tree);
        }

        // ── Ağaç ekranı tuşları ───────────────────────────────────────────────────
        else if (currentScreen == SCREEN_TREE) {
            if (key == KeyEvent.VK_W) {
                // W: ebeveyne git (-1 ceza)
                if (tree.moveCursorToParent()) player.addScore(-1);
            }
            if (key == KeyEvent.VK_A) {
                // A: sol çocuğa git (-1 ceza)
                if (tree.moveCursorToLeft()) player.addScore(-1);
            }
            if (key == KeyEvent.VK_D) {
                // D: sağ çocuğa git (-1 ceza)
                if (tree.moveCursorToRight()) player.addScore(-1);
            }
            if (key == KeyEvent.VK_T) {
                // T: backpack'ten ağaca yerleştir
                player.placeFromBackpackToTree(tree);
            }
            if (key == KeyEvent.VK_R) {
                // R: ağaçtan backpack'e al (-2 ceza)
                char sym = tree.takeFromCursor();
                if (sym != Symbol.EMPTY) {
                    if (!player.addToBackpack(sym)) {
                        // Çanta doluysa geri koy
                        tree.placeAtCursor(sym);
                    } else {
                        player.addScore(-2);
                    }
                }
            }
            if (key == KeyEvent.VK_F) {
                // F: ağacı bitir / doğrula
                finalizeTree();
            }
        }
    }

    // ─── AĞAÇ DOĞRULAMA ──────────────────────────────────────────────────────────

    /**
     * F tuşuna basıldığında ağacı doğrular ve sonucu gösterir.
     * Geçerliyse tablo ekranına geçer.
     * Geçersizse -10 ceza verir ve mesaj gösterir.
     */
    private void finalizeTree() {
        if (!tree.isValid()) {
            // Geçersiz ağaç: -10 ceza
            player.addScore(-10);
            display.showMessage("Hata: En az 3 değişken ve min. 3 derinlik gerekli! (-10)");
            return;
        }

        // Geçerli ağaç: skor hesapla ve tablo ekranına geç
        int treeScore = 10 * tree.countNodes();
        player.addScore(treeScore);
        display.showMessage("İfade: Infix=" + tree.toInfix()
            + "  Postfix=" + tree.toPostfix()
            + "  +" + treeScore + " puan!");

        // Doğruluk tablosunu hesapla
        table = new TruthTable(tree);
        table.compute();

        currentScreen = SCREEN_TABLE;
    }

    // ─── ATEŞ TOBU GÜNCELLEMESİ ──────────────────────────────────────────────────

    /**
     * Tüm aktif ateş toplarını bir adım ilerletir.
     * Aktif olmayan (duvara çarpmış) ateş topları temizlenir.
     */
    private void updateFireballs() {
        for (int i = 0; i < fireballCount; i++) {
            if (fireballs[i] != null && fireballs[i].isActive()) {
                fireballs[i].move(maze);
            }
        }
        compactFireballs(); // Pasif olanları diziden çıkar
    }

    /**
     * Ateş tobu - robot çarpışmalarını kontrol eder.
     * Bir ateş tobu robotla aynı karedeyse:
     *   - Robot yok edilir
     *   - Oyuncu +50 puan kazanır
     *   - Ateş tobu ilerlemeye devam eder (deactivate edilmez)
     */
    private void checkFireballRobotCollisions() {
        for (int fi = 0; fi < fireballCount; fi++) {
            if (fireballs[fi] == null || !fireballs[fi].isActive()) continue;

            int fbRow = fireballs[fi].getRow();
            int fbCol = fireballs[fi].getCol();

            // Bu konumda robot var mı?
            for (int ri = 0; ri < robotCount; ri++) {
                if (robots[ri] == null || !robots[ri].isActive()) continue;

                if (robots[ri].getRow() == fbRow && robots[ri].getCol() == fbCol) {
                    // Robot yok et, puan kazan
                    robots[ri].destroy(maze);
                    player.addScore(50);
                    // Ateş tobu devam eder (deactivate edilmez)
                }
            }
        }
    }

    /** Pasif ateş toplarını dizinin başına sıkıştırır */
    private void compactFireballs() {
        int newCount = 0;
        for (int i = 0; i < fireballCount; i++) {
            if (fireballs[i] != null && fireballs[i].isActive()) {
                fireballs[newCount++] = fireballs[i];
            }
        }
        // Kalan slotları temizle
        for (int i = newCount; i < fireballCount; i++) fireballs[i] = null;
        fireballCount = newCount;
    }

    /** Yeni bir ateş tobu ekler (MAX_FIREBALLS sınırına kadar) */
    private void addFireball(Fireball fb) {
        if (fireballCount < MAX_FIREBALLS) {
            fireballs[fireballCount++] = fb;
        }
    }

    // ─── ROBOT GÜNCELLEMESİ ──────────────────────────────────────────────────────

    /**
     * Tüm aktif robotları bir adım günceller.
     * Her robot kendi iç sayacıyla 4 tıkta bir hareket eder.
     */
    private void updateRobots() {
        for (int i = 0; i < robotCount; i++) {
            if (robots[i] != null && robots[i].isActive()) {
                robots[i].update(maze);
            }
        }
    }

    // ─── KOMŞU KARE HASARI ────────────────────────────────────────────────────────

    /**
     * Oyuncunun 4 komşu karesinde robot varsa can düşürür.
     * Her tıkta, her komşu robot için -5 can.
     */
    private void checkNeighborHarm() {
        int neighborRobots = maze.countNeighbors(player.getRow(), player.getCol(), Symbol.ROBOT);
        if (neighborRobots > 0) {
            player.loseLife(5 * neighborRobots);
        }
    }

    // ─── KUYRUKTAN YENİ ELEMAN ────────────────────────────────────────────────────

    /**
     * Giriş kuyruğundan bir elemanı alır ve maze'e rastgele boş bir konuma yerleştirir.
     * Eleman Robot ise yeni bir Robot nesnesi oluşturulur.
     */
    private void spawnFromQueue() {
        char element = inputQueue.dequeueAndRefill();

        int[] pos = maze.findRandomEmptySpot();
        if (pos == null) return; // Boş yer yok

        int r = pos[0];
        int c = pos[1];

        if (element == Symbol.ROBOT) {
            // Robot: nesne oluştur ve diziye ekle
            if (robotCount < MAX_ROBOTS) {
                robots[robotCount] = new Robot(r, c);
                robotCount++;
                maze.setCell(r, c, Symbol.ROBOT);
            }
        } else {
            // Mantık sembolü veya ateş tobu: doğrudan maze'e yaz
            maze.setCell(r, c, element);
        }
    }

    // ─── ÇIZIM ───────────────────────────────────────────────────────────────────

    /**
     * Aktif ekranı çizer.
     */
    private void render() {
        int timeSec = tick / 10; // Her 10 tık = 1 saniye

        switch (currentScreen) {
            case SCREEN_MAZE:
                display.drawMazeScreen(maze, player, robots, robotCount, inputQueue, timeSec);
                break;

            case SCREEN_TREE:
                display.drawTreeScreen(tree, player, inputQueue, timeSec);
                break;

            case SCREEN_TABLE:
                if (table != null) {
                    display.drawTableScreen(table);
                }
                break;
        }
    }

    // ─── OYUN SONU ────────────────────────────────────────────────────────────────

    /**
     * Oyun bitince çağrılır.
     * Oyuncunun adını alır, skoru kaydeder, yüksek skor tablosunu gösterir.
     *
     * TODO: Oyuncu adı girişi için basit bir dialog eklenebilir.
     *       Şimdilik sabit "Oyuncu" ismi kullanılıyor.
     */
    private void gameOver() {
        // Oyuncu adını al (basit, genişletilebilir)
        String playerName = "Oyuncu"; // TODO: Enigma console ile ad okuma eklenebilir

        // Skoru tabloya ekle ve kaydet
        highScore.insert(playerName, player.getScore());
        highScore.saveToFile("highscore.txt");

        // Yüksek skor tablosunu göster
        display.clear();
        display.drawHighScoreScreen(highScore);
    }
}
