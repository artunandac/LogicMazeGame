import java.util.Random;

public class InputQueue {

    private static final int SIZE = 10;
    private Queue queue;

    private static final Random rand = new Random();

    private static final char[] LOGIC_SYMBOLS = {
        'A', 'B', 'C', 'D',       // Variables
        'a', 'b', 'c', 'd',       // Negated variables 
        '~', '^', 'v', '+', '>', '='  // Operators
    };

    public InputQueue() {
        queue = new Queue(SIZE);
        for (int i = 0; i < SIZE; i++) {
            queue.enqueue(generateElement());
        }
    }

    public char dequeue() {
        char element = (char) queue.dequeue();
        queue.enqueue(generateElement());
        return element;
    }

    public char peek() {
        return (char) queue.peek();
    }
    
    //head'den başlar 
    public char[] getAll() {
        char[] result = new char[SIZE];
        // Queue'daki tüm elemanları almak için dequeue + enqueue döngüsü
        for (int i = 0; i < SIZE; i++) {
            char c = (char) queue.dequeue();
            result[i] = c;
            queue.enqueue(c);
        }
        return result;
    }

    // 7/10 logic symbol, 2/10 @, 1/10 X
    private char generateElement() {
        int roll = rand.nextInt(10);

        if (roll <= 6) {
            // 0-6 → Logic symbol (14 sembolden rastgele biri)
            return LOGIC_SYMBOLS[rand.nextInt(LOGIC_SYMBOLS.length)];
        } else if (roll <= 8) {
            // 7-8 → Packed fireball
            return '@';
        } else {
            // 9   → Robot
            return 'X';
        }
    }
}
