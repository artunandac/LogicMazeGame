/**
 * ExpressionTree.java
 * ====================
 * VERİ YAPISI: Dizi Tabanlı İkili Ağaç (Array-Based Binary Tree)
 *
 * Oyuncunun mantık ifadesini oluşturduğu ifade ağacı.
 * Maksimum 5 derinlik, 31 düğüm (node) barındırır.
 *
 * DİZİ TEMSILI:
 *   nodes[1]       = kök (root)
 *   nodes[2*i]     = i'nin sol çocuğu
 *   nodes[2*i + 1] = i'nin sağ çocuğu
 *   nodes[i / 2]   = i'nin ebeveyni
 *
 *   İndeks şeması:
 *           1
 *         /   \
 *        2     3
 *       / \   / \
 *      4   5 6   7
 *     ...         ... (derinlik 5'e kadar devam eder, indeks 31'e kadar)
 *
 * CURSOR (İmleç):
 *   - Yeşil olarak gösterilen aktif düğüm
 *   - W → ebeveyne git  (cursorIndex / 2)
 *   - A → sol çocuğa git (cursorIndex * 2)
 *   - D → sağ çocuğa git (cursorIndex * 2 + 1)
 *   - Her sembol yerleştirildiğinde imleç bir sonraki boş yuvaya gider
 */
public class ExpressionTree {

    /** Maksimum düğüm sayısı (derinlik 5 → 2^5 - 1 = 31 düğüm) */
    private static final int MAX_NODES = 31;

    /**
     * Ağacın düğümleri.
     * nodes[0] kullanılmıyor, indeksler 1'den başlıyor.
     * Boş düğüm = Symbol.EMPTY (' ')
     */
    private char[] nodes;

    /** Şu an seçili (yeşil) olan düğümün indeksi */
    private int cursorIndex;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Boş bir ifade ağacı oluşturur.
     * İmleç kökten (1) başlar.
     */
    public ExpressionTree() {
        nodes = new char[MAX_NODES + 1]; // nodes[0] boş, 1..31 kullanılıyor
        // Tüm düğümleri boş olarak başlat
        for (int i = 0; i <= MAX_NODES; i++) {
            nodes[i] = Symbol.EMPTY;
        }
        cursorIndex = 1; // İmleç kökten başlar
    }

    // ─── SEMBOL YERLEŞTİRME ──────────────────────────────────────────────────────

    /**
     * İmlecin bulunduğu yuvaya bir sembol yerleştirir.
     * Yerleştirdikten sonra imleç BFS sırasındaki bir sonraki boş yuvaya atlar.
     *
     * @param symbol Yerleştirilecek sembol (Symbol sınıfındaki char sabitlerden biri)
     * @return true → yerleştirme başarılı, false → tüm yuvalar dolu
     */
    public boolean placeAtCursor(char symbol) {
        if (cursorIndex < 1 || cursorIndex > MAX_NODES) {
            return false; // Geçersiz imleç konumu
        }
        nodes[cursorIndex] = symbol;

        // İmleçi bir sonraki boş yuvaya taşı
        int nextSlot = findNextEmptySlot();
        if (nextSlot != -1) {
            cursorIndex = nextSlot;
        }
        return true;
    }

    /**
     * Belirli bir indekse direkt sembol yerleştirir (backpack'ten yerleştirme için).
     * İmleci hareket ettirmez.
     *
     * @param index  Hedef düğüm indeksi (1-31)
     * @param symbol Yerleştirilecek sembol
     */
    public void placeAt(int index, char symbol) {
        if (index >= 1 && index <= MAX_NODES) {
            nodes[index] = symbol;
        }
    }

    /**
     * İmlecin bulunduğu düğümdeki sembolü alır ve düğümü boşaltır.
     * (-2 ceza puanı Game.java tarafından uygulanır)
     *
     * @return Alınan sembol, yoksa Symbol.EMPTY
     */
    public char takeFromCursor() {
        if (cursorIndex < 1 || cursorIndex > MAX_NODES) {
            return Symbol.EMPTY;
        }
        char symbol = nodes[cursorIndex];
        nodes[cursorIndex] = Symbol.EMPTY; // Düğümü boşalt
        return symbol;
    }

    // ─── İMLE HAREKETİ ───────────────────────────────────────────────────────────

    /**
     * İmleci ebeveyn düğüme taşır (W tuşu).
     * Zaten kökteyse hareket etmez.
     * (-1 ceza puanı Game.java tarafından uygulanır)
     *
     * @return true → hareket başarılı, false → kökteydik, hareket yok
     */
    public boolean moveCursorToParent() {
        if (cursorIndex <= 1) {
            return false; // Kök düğüm, ebeveyn yok
        }
        cursorIndex = cursorIndex / 2;
        return true;
    }

    /**
     * İmleci sol çocuğa taşır (A tuşu).
     * Derinlik sınırı aşılırsa hareket etmez.
     * (-1 ceza puanı Game.java tarafından uygulanır)
     *
     * @return true → hareket başarılı, false → sınır aşıldı
     */
    public boolean moveCursorToLeft() {
        int left = cursorIndex * 2;
        if (left > MAX_NODES) {
            return false; // Maksimum derinliğe ulaşıldı
        }
        cursorIndex = left;
        return true;
    }

    /**
     * İmleci sağ çocuğa taşır (D tuşu).
     * Derinlik sınırı aşılırsa hareket etmez.
     * (-1 ceza puanı Game.java tarafından uygulanır)
     *
     * @return true → hareket başarılı, false → sınır aşıldı
     */
    public boolean moveCursorToRight() {
        int right = cursorIndex * 2 + 1;
        if (right > MAX_NODES) {
            return false; // Maksimum derinliğe ulaşıldı
        }
        cursorIndex = right;
        return true;
    }

    // ─── İFADE ÜRETİMİ ───────────────────────────────────────────────────────────

    /**
     * Ağacın infix (ara-sıra) gösterimini üretir.
     * Örnek: ((~(a v B)) > (A + C))
     *
     * @return İnfix ifade string'i
     */
    public String toInfix() {
        String result = toInfixHelper(1);
        return result.isEmpty() ? "(boş ağaç)" : result;
    }

    /**
     * Kök indeksinden başlayarak rekürsif infix üretimi.
     * - Değişken → doğrudan sembol adı
     * - NOT (tekli) → ~(sol_çocuk)
     * - Diğer ikili operatörler → (sol_çocuk op sağ_çocuk)
     */
    private String toInfixHelper(int index) {
        // Geçersiz indeks veya boş düğüm
        if (index > MAX_NODES || nodes[index] == Symbol.EMPTY) {
            return "";
        }

        char sym = nodes[index];

        // Değişkense direkt göster
        if (Symbol.isVariable(sym)) {
            return Symbol.toDisplayString(sym);
        }

        // NOT operatörü: tekli, sadece sol çocuğu var
        if (Symbol.isUnary(sym)) {
            String left = toInfixHelper(index * 2);
            return "~(" + left + ")";
        }

        // İkili operatör: (sol op sağ)
        String left  = toInfixHelper(index * 2);
        String right = toInfixHelper(index * 2 + 1);
        String op    = Symbol.toDisplayString(sym);
        return "(" + left + " " + op + " " + right + ")";
    }

    /**
     * Ağacın postfix (son-ek) gösterimini üretir.
     * Örnek: a B v ~ A C + >
     *
     * @return Postfix ifade string'i
     */
    public String toPostfix() {
        String result = toPostfixHelper(1).trim();
        return result.isEmpty() ? "(boş ağaç)" : result;
    }

    /**
     * Kök indeksinden başlayarak rekürsif postfix üretimi.
     * - Değişken → sembol
     * - NOT (tekli) → sol op
     * - İkili → sol sağ op
     */
    private String toPostfixHelper(int index) {
        if (index > MAX_NODES || nodes[index] == Symbol.EMPTY) {
            return "";
        }

        char sym = nodes[index];

        if (Symbol.isVariable(sym)) {
            return Symbol.toDisplayString(sym) + " ";
        }

        if (Symbol.isUnary(sym)) {
            String left = toPostfixHelper(index * 2);
            return left + Symbol.toDisplayString(sym) + " ";
        }

        String left  = toPostfixHelper(index * 2);
        String right = toPostfixHelper(index * 2 + 1);
        return left + right + Symbol.toDisplayString(sym) + " ";
    }

    // ─── DOĞRULAMA ───────────────────────────────────────────────────────────────

    /**
     * Ağacın geçerli bir ifade ağacı olup olmadığını kontrol eder.
     * Koşullar:
     *   1. En az 3 değişken bulunmalı
     *   2. Ağacın derinliği en az 3 olmalı
     *
     * @return true → geçerli, false → geçersiz
     */
    public boolean isValid() {
        return countVariables() >= 3 && getMaxDepth() >= 3;
    }

    /**
     * Ağaçtaki toplam dolu düğüm sayısını döndürür.
     * Skor hesabı: 10 * countNodes()
     */
    public int countNodes() {
        int count = 0;
        for (int i = 1; i <= MAX_NODES; i++) {
            if (nodes[i] != Symbol.EMPTY) count++;
        }
        return count;
    }

    /**
     * Ağaçtaki değişken (yaprak) sayısını döndürür.
     * (A, B, C, D, a=A', b=B', c=C', d=D')
     */
    public int countVariables() {
        int count = 0;
        for (int i = 1; i <= MAX_NODES; i++) {
            if (Symbol.isVariable(nodes[i])) count++;
        }
        return count;
    }

    /**
     * Ağacın maksimum derinliğini döndürür.
     * Kök → derinlik 1
     *
     * Derinlik hesabı: index i için derinlik = i'yi sürekli 2'ye bölünce
     * 1'e ulaşana kadar geçen adım sayısı.
     * (Örnek: index 5 → 5→2→1, derinlik = 3)
     */
    public int getMaxDepth() {
        int maxDepth = 0;
        for (int i = 1; i <= MAX_NODES; i++) {
            if (nodes[i] != Symbol.EMPTY) {
                int depth = getDepthOfIndex(i);
                if (depth > maxDepth) {
                    maxDepth = depth;
                }
            }
        }
        return maxDepth;
    }

    /**
     * Verilen indeksteki düğümün derinliğini döndürür.
     * Kök (indeks 1) → derinlik 1
     */
    private int getDepthOfIndex(int index) {
        int depth = 0;
        int idx = index;
        while (idx >= 1) {
            depth++;
            idx = idx / 2;
        }
        return depth;
    }

    // ─── YARDIMCI METODLAR ───────────────────────────────────────────────────────

    /**
     * BFS sıralamasıyla (1,2,3,4,...) ilk boş yuvayı bulur.
     * İmleç bu yuvaya taşınır.
     *
     * @return Boş yuvanın indeksi, ağaç doluysa -1
     */
    private int findNextEmptySlot() {
        for (int i = 1; i <= MAX_NODES; i++) {
            if (nodes[i] == Symbol.EMPTY) {
                return i;
            }
        }
        return -1; // Ağaç tamamen dolu
    }

    // ─── GETTER METODLARI ────────────────────────────────────────────────────────

    /** İmlecin şu anki indeksini döndürür */
    public int getCursorIndex() {
        return cursorIndex;
    }

    /** İmleci belirli bir indekse taşır (doğrudan erişim için) */
    public void setCursorIndex(int index) {
        if (index >= 1 && index <= MAX_NODES) {
            cursorIndex = index;
        }
    }

    /**
     * Belirli bir indeksteki düğümün sembolünü döndürür.
     *
     * @param index Düğüm indeksi (1-31)
     * @return Sembol karakteri, geçersizse Symbol.EMPTY
     */
    public char getNode(int index) {
        if (index < 1 || index > MAX_NODES) return Symbol.EMPTY;
        return nodes[index];
    }

    /** Maksimum düğüm sayısını döndürür (Display için) */
    public int getMaxNodes() {
        return MAX_NODES;
    }

    /**
     * Ağacın tamamen boş olup olmadığını kontrol eder.
     */
    public boolean isEmpty() {
        for (int i = 1; i <= MAX_NODES; i++) {
            if (nodes[i] != Symbol.EMPTY) return false;
        }
        return true;
    }

    /**
     * Ağacı sıfırlar; tüm düğümler boşaltılır, imleç köke döner.
     */
    public void clear() {
        for (int i = 0; i <= MAX_NODES; i++) {
            nodes[i] = Symbol.EMPTY;
        }
        cursorIndex = 1;
    }
}
