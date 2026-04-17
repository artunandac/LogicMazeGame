import java.util.Random;

/**
 * InputQueue.java
 * ================
 * VERİ YAPISI: Döngüsel Dizi Kuyruğu (Circular Array Queue)
 *
 * Oyuna yeni elemanlar eklemek için kullanılan giriş kuyruğu.
 * Kuyruk sabit 10 elemanlıdır ve her zaman dolu tutulur.
 * Her 20 oyun tıkında (2 saniyede) kuyruğun ilk elemanı
 * maze'e rastgele bir konuma yerleştirilir ve yerine yeni
 * bir eleman üretilir.
 *
 * Üretim olasılıkları:
 *   %70 → Mantık sembolü (14 farklı sembol arasından rastgele)
 *   %20 → Paketlenmiş ateş tobu  (@)
 *   %10 → Robot (X)
 *
 * Döngüsel kuyruk çalışma mantığı:
 *   [ e0 | e1 | e2 | ... | e9 ]
 *     ↑front                ↑rear
 *   enqueue: rear'a ekle, rear bir ilerle (mod 10)
 *   dequeue: front'tan al, front bir ilerle (mod 10)
 */
public class InputQueue {

    /** Kuyruğun sabit boyutu */
    private static final int SIZE = 10;

    /** Elemanların tutulduğu dizi */
    private char[] elements;

    /** Kuyruğun başı (dequeue yapılacak yer) */
    private int front;

    /** Kuyruğun sonu (enqueue yapılacak yer) */
    private int rear;

    /** Şu anki eleman sayısı */
    private int count;

    /** Rastgele üretim için */
    private Random random;

    /**
     * 14 mantık sembolünün listesi - rastgele seçim için kullanılır.
     * Sıra: A, B, C, D, a(=A'), b(=B'), c(=C'), d(=D'), ~, ^, v, +, >, =
     */
    private static final char[] LOGIC_SYMBOLS = {
        Symbol.VAR_A, Symbol.VAR_B, Symbol.VAR_C, Symbol.VAR_D,
        Symbol.NOT_A, Symbol.NOT_B, Symbol.NOT_C, Symbol.NOT_D,
        Symbol.NOT, Symbol.AND, Symbol.OR, Symbol.XOR,
        Symbol.IMPLIES, Symbol.IFF
    };

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /**
     * Boş bir kuyruk oluşturur.
     * Başlangıçta kuyruk boştur; fill() çağrısıyla doldurulur.
     */
    public InputQueue() {
        elements = new char[SIZE];
        front = 0;
        rear  = 0;
        count = 0;
        random = new Random();
    }

    // ─── TEMEL KUYRUK İŞLEMLERİ ──────────────────────────────────────────────────

    /**
     * Kuyruğun sonuna bir eleman ekler.
     * Kuyruk doluysa eleman eklenmez.
     *
     * @param element Eklenecek karakter
     */
    public void enqueue(char element) {
        if (isFull()) {
            return; // Kuyruk dolu, ekleme yapılamaz
        }
        elements[rear] = element;
        rear = (rear + 1) % SIZE; // Döngüsel: sondan başa döner
        count++;
    }

    /**
     * Kuyruğun başından bir eleman çıkarır ve döndürür.
     * Kuyruk boşsa EMPTY karakteri döndürür.
     *
     * @return Çıkarılan karakter veya Symbol.EMPTY
     */
    public char dequeue() {
        if (isEmpty()) {
            return Symbol.EMPTY;
        }
        char element = elements[front];
        front = (front + 1) % SIZE; // Döngüsel: sondan başa döner
        count--;
        return element;
    }

    /**
     * Kuyruğun başındaki elemana bakar fakat çıkarmaz.
     * Kuyruk boşsa EMPTY döndürür.
     *
     * @return Kuyruğun başındaki karakter
     */
    public char peek() {
        if (isEmpty()) {
            return Symbol.EMPTY;
        }
        return elements[front];
    }

    /**
     * Kuyruğun başından itibaren 'index' pozisyonundaki elemana bakar.
     * Ekranda kuyruğu göstermek için kullanılır.
     * (Örnek: index=0 → ilk eleman, index=9 → son eleman)
     *
     * @param index 0 ile SIZE-1 arasında pozisyon
     * @return O pozisyondaki karakter, geçersizse EMPTY
     */
    public char get(int index) {
        if (index < 0 || index >= count) {
            return Symbol.EMPTY;
        }
        // Döngüsel indeks hesabı: gerçek pozisyon = (front + index) mod SIZE
        return elements[(front + index) % SIZE];
    }

    // ─── DURUM KONTROLÜ ──────────────────────────────────────────────────────────

    /** Kuyruk tamamen dolu mu? */
    public boolean isFull() {
        return count == SIZE;
    }

    /** Kuyruk tamamen boş mu? */
    public boolean isEmpty() {
        return count == 0;
    }

    /** Kuyruktaki eleman sayısını döndürür */
    public int size() {
        return count;
    }

    // ─── RASTGELE ÜRETIM ─────────────────────────────────────────────────────────

    /**
     * Olasılıklara göre rastgele bir kuyruk elemanı üretir:
     *   %70 → Mantık sembolü
     *   %20 → Ateş tobu (@)
     *   %10 → Robot (X)
     *
     * @return Üretilen karakter
     */
    public char generateRandom() {
        int roll = random.nextInt(10); // 0-9 arası

        if (roll < 7) {
            // %70: 14 mantık sembolünden biri
            return LOGIC_SYMBOLS[random.nextInt(LOGIC_SYMBOLS.length)];
        } else if (roll < 9) {
            // %20: Paketlenmiş ateş tobu
            return Symbol.FIREBALL_PACKED;
        } else {
            // %10: Robot
            return Symbol.ROBOT;
        }
    }

    /**
     * Kuyruğu başlangıçta 10 rastgele elemanla doldurur.
     * Oyun başlangıcında bir kez çağrılır.
     */
    public void fill() {
        while (!isFull()) {
            enqueue(generateRandom());
        }
    }

    /**
     * Kuyruğun başından bir eleman alır ve yerine yeni rastgele bir eleman üretir.
     * Her 2 saniyede maze'e yerleştirme yapıldıktan sonra çağrılır.
     *
     * @return Maze'e yerleştirilecek karakter
     */
    public char dequeueAndRefill() {
        char element = dequeue();
        enqueue(generateRandom()); // Boşalan yere yeni eleman ekle
        return element;
    }
}
