/**
 * Symbol.java
 * ============
 * Oyundaki tüm karakter sabitleri ve yardımcı metodlar bu sınıfta tutulur.
 * Hiçbir zaman örneği (instance) oluşturulmaz; sadece static kullanım için tasarlanmıştır.
 *
 * Maze ekranında kullanılan karakterler (char):
 *   Değişkenler  : A B C D  (büyük harf = doğrudan değişken)
 *   NOT değişken : a b c d  (küçük harf = A', B', C', D')
 *   NOT operatörü: ~
 *   AND          : ^
 *   OR           : v
 *   XOR          : +
 *   IMPLIES      : >
 *   IFF          : =
 *   Ateş topu    : @ (paketlenmiş), o (aktif/ateşlenmiş)
 *   Robot        : X
 *   Oyuncu       : P
 *   Duvar        : #
 *   Boş          : (boşluk)
 */
public class Symbol {

    // ─── MANTIK SEMBOLLERİ ───────────────────────────────────────────────────────

    /** Değişken A */
    public static final char VAR_A    = 'A';
    /** Değişken B */
    public static final char VAR_B    = 'B';
    /** Değişken C */
    public static final char VAR_C    = 'C';
    /** Değişken D */
    public static final char VAR_D    = 'D';
    /** NOT A  (A') */
    public static final char NOT_A    = 'a';
    /** NOT B  (B') */
    public static final char NOT_B    = 'b';
    /** NOT C  (C') */
    public static final char NOT_C    = 'c';
    /** NOT D  (D') */
    public static final char NOT_D    = 'd';
    /** NOT operatörü  (~) */
    public static final char NOT      = '~';
    /** AND operatörü  (^) */
    public static final char AND      = '^';
    /** OR  operatörü  (v) */
    public static final char OR       = 'v';
    /** XOR operatörü  (+) */
    public static final char XOR      = '+';
    /** IMPLIES operatörü (>) */
    public static final char IMPLIES  = '>';
    /** IFF operatörü  (=) */
    public static final char IFF      = '=';

    // ─── DİĞER MAZE ELEMANLARI ───────────────────────────────────────────────────

    /** Oyuncu karakteri */
    public static final char PLAYER          = 'P';
    /** Düşman robot */
    public static final char ROBOT           = 'X';
    /** Paketlenmiş ateş topu (toplanabilir) */
    public static final char FIREBALL_PACKED = '@';
    /** Ateşlenmiş/aktif ateş tobu */
    public static final char FIREBALL_ACTIVE = 'o';
    /** Duvar */
    public static final char WALL            = '#';
    /** Boş kare */
    public static final char EMPTY           = ' ';

    // ─── YÖN SABİTLERİ ───────────────────────────────────────────────────────────

    public static final int UP    = 0;
    public static final int DOWN  = 1;
    public static final int LEFT  = 2;
    public static final int RIGHT = 3;

    // ─── YARDIMCI METODLAR ───────────────────────────────────────────────────────

    /**
     * Verilen karakterin bir mantık sembolü olup olmadığını kontrol eder.
     * (Değişken veya operatör)
     */
    public static boolean isLogicSymbol(char c) {
        return c == VAR_A || c == VAR_B || c == VAR_C || c == VAR_D
            || c == NOT_A || c == NOT_B || c == NOT_C || c == NOT_D
            || c == NOT   || c == AND   || c == OR    || c == XOR
            || c == IMPLIES || c == IFF;
    }

    /**
     * Verilen karakterin bir değişken olup olmadığını kontrol eder.
     * (A, B, C, D, a=A', b=B', c=C', d=D')
     */
    public static boolean isVariable(char c) {
        return c == VAR_A || c == VAR_B || c == VAR_C || c == VAR_D
            || c == NOT_A || c == NOT_B || c == NOT_C || c == NOT_D;
    }

    /**
     * Verilen karakterin bir operatör olup olmadığını kontrol eder.
     * (~, ^, v, +, >, =)
     */
    public static boolean isOperator(char c) {
        return c == NOT || c == AND || c == OR
            || c == XOR || c == IMPLIES || c == IFF;
    }

    /**
     * NOT operatörü tek operandlı (unary/tekli) bir operatördür.
     * Diğerleri iki operandlı (binary/ikili) operatörlerdir.
     */
    public static boolean isUnary(char c) {
        return c == NOT;
    }

    /**
     * Bir karakterin ekranda veya ifadede nasıl gösterileceğini döndürür.
     * Örnek: NOT_A → "A'",  AND → "^"
     */
    public static String toDisplayString(char c) {
        switch (c) {
            case VAR_A:   return "A";
            case VAR_B:   return "B";
            case VAR_C:   return "C";
            case VAR_D:   return "D";
            case NOT_A:   return "A'";
            case NOT_B:   return "B'";
            case NOT_C:   return "C'";
            case NOT_D:   return "D'";
            case NOT:     return "~";
            case AND:     return "^";
            case OR:      return "v";
            case XOR:     return "+";
            case IMPLIES: return ">";
            case IFF:     return "=";
            default:      return String.valueOf(c);
        }
    }

    /**
     * Değişken harfinden 0-3 arasında bir indeks döndürür (TruthTable için).
     * A→0, B→1, C→2, D→3
     * Değişken değilse -1 döndürür.
     */
    public static int getVariableIndex(char c) {
        switch (c) {
            case VAR_A: case NOT_A: return 0;
            case VAR_B: case NOT_B: return 1;
            case VAR_C: case NOT_C: return 2;
            case VAR_D: case NOT_D: return 3;
            default: return -1;
        }
    }

    // Symbol sınıfından nesne oluşturulmaması için private constructor
    private Symbol() {}
}
