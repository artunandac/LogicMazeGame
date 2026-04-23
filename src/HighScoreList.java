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

    // Azalan sıraya göre ekle
    public void insert(String name, int score) {
        // TODO
    }

    public void loadFromFile(String fileName) {
        // TODO: highscore.txt satır satır oku
    }

    public void saveToFile(String fileName) {
        // TODO: güncel listeyi yaz
    }

    @Override
    public String toString() {
        // TODO: listeleme
        return "";
    }
}
