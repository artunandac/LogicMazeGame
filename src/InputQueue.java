import java.util.Random;

public class InputQueue {

    private static final int SIZE = 10;
    private char[] queue = new char[SIZE];
    private int    head  = 0;
    private int    size  = 0;

    private static final Random rand = new Random();

    public InputQueue() {
        // TODO: kuyruğu ilk 10 elemanla doldur
        for (int i = 0; i < SIZE; i++) queue[i] = generateElement();
        size = SIZE;
    }

    // Baştaki elemanı al, yerine yeni üret
    public char dequeue() {
        // TODO: circular queue implementasyonu
        return ' ';
    }

    public char peek() {
        // TODO: baştaki elemana bak
        return ' ';
    }

    // HUD gösterimi için tüm kuyruk
    public char[] getAll() {
        // TODO: mevcut sırayla döndür
        return new char[SIZE];
    }

    // 7/10 logic symbol, 2/10 @, 1/10 X
    private char generateElement() {
        // TODO: olasılık dağılımına göre eleman üret
        return ' ';
    }
}
