import java.io.*;

public class HighScoreList {

    // Doubly linked list node
    private static class Node {
        String name;
        int    score;
        Node   prev, next;
        Node(String name, int score) { this.name = name; this.score = score; }
    }

    private Node head = null;
    private Node tail = null;
    private int  size = 0;

    public void insert(String name, int score) {
        // TODO yeni skor için uygun pozisyonu bul, ekle, gerekirse tail'i güncelle
    }

    public void loadFromFile(String fileName) {
        // TODO: highscore.txt satır satır oku
    }

    public void saveToFile(String fileName) {
        // TODO: güncel listeyi yaz
    }

    @Override
    public String toString() {
        // TODO: listelenmiş skorları tek string olarak döndür
        return "";
    }
}
