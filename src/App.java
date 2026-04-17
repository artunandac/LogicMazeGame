/**
 * App.java
 * =========
 * Programın başlangıç noktası (entry point).
 * Sadece Game nesnesini oluşturur ve oyunu başlatır.
 */
public class App {

    public static void main(String[] args) {
        // Oyunu oluştur ve başlat
        Game game = new Game();
        game.start();
    }
}
