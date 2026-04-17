/**
 * TruthTable.java
 * ================
 * İfade ağacından doğruluk tablosu hesaplar ve Karnaugh haritasını oluşturur.
 *
 * HESAPLAMA:
 *   4 değişken (A, B, C, D) → 2^4 = 16 satır
 *   Her satır için değişken değerlerine göre ağaç özyinelemeli değerlendirilir.
 *
 * KARNAUGH HARİTASI:
 *   4 değişken için 4×4 tablo (AB satır ekseni, CD sütun ekseni).
 *   Gray code sıralaması kullanılır: 00, 01, 11, 10
 *
 * BASİTLEŞTİRME (PLACEHOLDER):
 *   simplify() metodu boş bırakılmıştır.
 *   Takım kendi algoritmasını buraya yazacak
 *   (Quine-McCluskey, Petrick's method veya benzeri bir yöntem).
 *
 * SORU SLOTLARI:
 *   Her sütun için rastgele bir satır "soru" olarak işaretlenir (sarı gösterilir).
 *   Oyuncu bu boşlukları doldurursa +3 puan kazanır, yanlışsa -2 ceza.
 */
public class TruthTable {

    // ─── SABITLER ────────────────────────────────────────────────────────────────

    /** Satır sayısı (2^4 = 16) */
    public static final int NUM_ROWS = 16;

    /** Değişken sayısı */
    public static final int NUM_VARS = 4;

    /**
     * Karnaugh haritası için Gray code sıralaması.
     * satır/sütun indeksine göre AB veya CD değerlerini verir.
     * index 0→00, 1→01, 2→11, 3→10
     */
    private static final int[] GRAY = {0, 1, 3, 2};

    // ─── ALANLAR ─────────────────────────────────────────────────────────────────

    /** Hesaplama yapılacak ifade ağacı */
    private ExpressionTree tree;

    /**
     * Doğruluk tablosu sonuçları.
     * results[minterm][subExprIndex]
     * minterm: 0-15 (0000'dan 1111'e kadar)
     * subExprIndex: 0 → son ifade sonucu
     *               1..n → ara ifadeler (alt ağaçlar)
     */
    private int[][] results;

    /**
     * Alt ifade isimleri (sütun başlıkları).
     * Örnek: "avB", "~(avB)", "A+C", "(~(avB))>(A+C)"
     * Bu alan Display.java tarafından başlık olarak kullanılır.
     */
    private String[] subExprNames;

    /** Kaç alt ifade hesaplandı */
    private int subExprCount;

    /**
     * Her sütun için soru slotu satır indeksi.
     * questionSlot[i] = i. sütunda hangi satır sarıyla gösterilecek.
     * -1 → soru yok
     */
    private int[] questionSlot;

    /** Hesaplama tamamlandı mı? */
    private boolean computed;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Yeni bir doğruluk tablosu oluşturur.
     *
     * @param tree Hesaplanacak ifade ağacı
     */
    public TruthTable(ExpressionTree tree) {
        this.tree     = tree;
        this.computed = false;

        // Maksimum 10 sütun (değişkenler + ara ifadeler + sonuç)
        this.results       = new int[NUM_ROWS][10];
        this.subExprNames  = new String[10];
        this.questionSlot  = new int[10];
        this.subExprCount  = 0;

        // Soru slotlarını başlangıçta "yok" olarak ayarla
        for (int i = 0; i < 10; i++) {
            questionSlot[i] = -1;
        }
    }

    // ─── HESAPLAMA ────────────────────────────────────────────────────────────────

    /**
     * Doğruluk tablosunu hesaplar.
     * 16 satır için A, B, C, D değerlerini 0000'dan 1111'e kadar dener
     * ve her satır için ağacı değerlendirir.
     *
     * Ayrıca her sütun için rastgele bir soru slotu belirler.
     */
    public void compute() {
        // Alt ifade isimlerini topla (ağaçtaki önemli alt ağaçların isimleri)
        subExprCount = 0;
        collectSubExpressions(1);

        // Her minterm (0-15) için değerleri hesapla
        for (int minterm = 0; minterm < NUM_ROWS; minterm++) {
            // Minterm'den A, B, C, D değerlerini çıkar
            int a = (minterm >> 3) & 1; // Bit 3
            int b = (minterm >> 2) & 1; // Bit 2
            int c = (minterm >> 1) & 1; // Bit 1
            int d = (minterm)      & 1; // Bit 0

            int[] varVals = {a, b, c, d};

            // Her alt ifadeyi değerlendir
            // TODO: Takım buradaki alt ifade değerlendirmesini tamamlayacak
            // Şimdilik sadece kök düğümü (tüm ifadeyi) değerlendiriyoruz
            results[minterm][0] = evaluate(1, varVals);
        }

        // Rastgele soru slotu belirle (her sütun için)
        java.util.Random rng = new java.util.Random();
        for (int col = 0; col < subExprCount; col++) {
            questionSlot[col] = rng.nextInt(NUM_ROWS);
        }

        computed = true;
    }

    // ─── DEĞERLENDİRME (ÖZYİNELEMELİ) ──────────────────────────────────────────

    /**
     * İfade ağacını verilen değişken değerleriyle özyinelemeli olarak değerlendirir.
     *
     * Çalışma mantığı:
     *   - Düğüm değişkense → varVals dizisinden değerini al
     *   - NOT (tekli) → sol çocuğun tersini al (1-sol)
     *   - AND → sol & sağ
     *   - OR  → sol | sağ
     *   - XOR → sol ^ sağ (Java'nın ^ operatörü XOR yapar)
     *   - IMPLIES → (sol=1 ve sağ=0) ise 0, diğer durumlarda 1
     *   - IFF → sol == sağ ise 1, değilse 0
     *
     * @param nodeIndex Ağaçtaki düğüm indeksi (1'den başlar)
     * @param varVals   [A, B, C, D] değerleri (0 veya 1)
     * @return Düğümün değeri (0 veya 1)
     */
    public int evaluate(int nodeIndex, int[] varVals) {
        // Geçersiz veya boş düğüm
        if (nodeIndex > tree.getMaxNodes() || tree.getNode(nodeIndex) == Symbol.EMPTY) {
            return 0;
        }

        char sym = tree.getNode(nodeIndex);

        // ── Değişken düğümler ──
        if (Symbol.isVariable(sym)) {
            int idx = Symbol.getVariableIndex(sym);
            if (idx < 0 || idx >= varVals.length) return 0;

            int val = varVals[idx];
            // Küçük harf (a, b, c, d) = NOT(değişken)
            if (sym == Symbol.NOT_A || sym == Symbol.NOT_B
             || sym == Symbol.NOT_C || sym == Symbol.NOT_D) {
                return 1 - val; // Terse çevir
            }
            return val;
        }

        // ── Operatör düğümler ──

        // NOT: Tekli operatör, sadece sol çocuk
        if (sym == Symbol.NOT) {
            int left = evaluate(nodeIndex * 2, varVals);
            return 1 - left;
        }

        // İkili operatörler: sol ve sağ çocuk
        int left  = evaluate(nodeIndex * 2,     varVals);
        int right = evaluate(nodeIndex * 2 + 1, varVals);

        switch (sym) {
            case Symbol.AND:
                return left & right;       // 1 yalnızca ikisi de 1 ise

            case Symbol.OR:
                return left | right;       // 1 en az biri 1 ise

            case Symbol.XOR:
                return left ^ right;       // 1 yalnızca biri 1 ise (farklıysa)

            case Symbol.IMPLIES:
                // P → Q ≡ ¬P ∨ Q  (sol=1 ve sağ=0 ise 0, diğer durumlarda 1)
                return (left == 1 && right == 0) ? 0 : 1;

            case Symbol.IFF:
                // P ↔ Q: ikisi eşitse 1
                return (left == right) ? 1 : 0;

            default:
                return 0;
        }
    }

    // ─── ALT İFADE TOPLAMA ───────────────────────────────────────────────────────

    /**
     * Ağacı gezerek önemli alt ifadeleri toplar.
     * Bunlar doğruluk tablosunun sütun başlıkları olur.
     *
     * TODO: Bu metod takım tarafından geliştirilebilir.
     * Şimdilik sadece kök (tüm ifade) için bir sütun oluşturuluyor.
     *
     * @param nodeIndex Başlangıç düğümü
     */
    private void collectSubExpressions(int nodeIndex) {
        // Basit başlangıç: sadece son ifadeyi (kök) bir sütun olarak ekle
        if (nodeIndex != 1) return; // Şimdilik sadece kök

        if (tree.getNode(1) != Symbol.EMPTY) {
            subExprNames[0] = tree.toInfix();
            subExprCount    = 1;
        }
    }

    // ─── KARNAUGH HARİTASI ────────────────────────────────────────────────────────

    /**
     * Karnaugh haritasının değerini (0 veya 1) döndürür.
     *
     * @param abRow AB satır indeksi (0-3, Gray code: 00,01,11,10)
     * @param cdCol CD sütun indeksi (0-3, Gray code: 00,01,11,10)
     * @return 0 veya 1
     */
    public int getKarnaughValue(int abRow, int cdCol) {
        if (!computed) return 0;

        // Gray code'u binary'e çevir
        int ab = GRAY[abRow]; // AB değerleri (0-3 = 00,01,10,11)
        int cd = GRAY[cdCol]; // CD değerleri

        int a = (ab >> 1) & 1;
        int b = (ab)      & 1;
        int c = (cd >> 1) & 1;
        int d = (cd)      & 1;

        // Minterm indeksini hesapla
        int minterm = (a << 3) | (b << 2) | (c << 1) | d;

        return results[minterm][0]; // Kök ifadenin sonucu
    }

    // ─── BASİTLEŞTİRME (PLACEHOLDER) ─────────────────────────────────────────────

    /**
     * Karnaugh haritasından basitleştirilmiş ifadeyi döndürür.
     *
     * ============================================================
     * TODO: Bu metod TAKİM tarafından doldurulacak!
     * ============================================================
     *
     * Öneri:
     *   1. getKarnaughValue() ile 4×4 tabloyu al
     *   2. 1 olan hücreleri grupla (2'nin kuvveti büyüklüğünde dikdörtgenler)
     *   3. Her grup için minimal ifadeyi belirle
     *   4. Grupları OR ile birleştir
     *
     * @return Basitleştirilmiş ifade string'i (örnek: "A' + B + C'")
     */
    public String simplify() {
        // TODO: Karnaugh haritası basitleştirme algoritması buraya yazılacak
        return "TODO: simplify() metodu implement edilmedi";
    }

    // ─── GETTER METODLARI ────────────────────────────────────────────────────────

    /**
     * Belirli bir minterm ve sütun için hesaplanan değeri döndürür.
     *
     * @param minterm    Satır indeksi (0-15)
     * @param exprIndex  Sütun indeksi (0=kök ifade)
     * @return 0 veya 1
     */
    public int getResult(int minterm, int exprIndex) {
        if (minterm < 0 || minterm >= NUM_ROWS) return 0;
        if (exprIndex < 0 || exprIndex >= subExprCount) return 0;
        return results[minterm][exprIndex];
    }

    /** Alt ifade sütun sayısını döndürür */
    public int getSubExprCount()          { return subExprCount; }

    /** i. sütunun başlık adını döndürür */
    public String getSubExprName(int i)   {
        if (i < 0 || i >= subExprCount) return "";
        return subExprNames[i];
    }

    /** i. sütunun soru slotu satırını döndürür (-1 = soru yok) */
    public int getQuestionSlot(int i) {
        if (i < 0 || i >= subExprCount) return -1;
        return questionSlot[i];
    }

    /** Hesaplama tamamlandı mı? */
    public boolean isComputed()           { return computed; }
}
