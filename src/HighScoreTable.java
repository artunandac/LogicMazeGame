import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * HighScoreTable.java
 * ====================
 * VERİ YAPISI: Çift Bağlı Liste (Doubly Linked List)
 *
 * Oyunun yüksek skor tablosunu tutan sınıf.
 * Skorlar büyükten küçüğe (azalan) sıralı tutulur.
 *
 * Her düğüm (ScoreNode):
 *   - isim (String)
 *   - skor (int)
 *   - önceki düğüme işaretçi (prev)
 *   - sonraki düğüme işaretçi (next)
 *
 * Çift bağlı liste avantajı:
 *   Her iki yönde de gezinebilir (ileri/geri)
 *
 * Dosya formatı (highscore.txt):
 *   Her satır: "İsim Soyisim Skor"
 *   Örnek:
 *     Irmak Yol 412
 *     Tarkan Bulut 728
 */
public class HighScoreTable {

    // ─── İÇ SINIF: DÜĞÜM ─────────────────────────────────────────────────────────

    /**
     * Çift bağlı listenin bir düğümü.
     * Oyuncunun adını ve skorunu tutar.
     */
    private class ScoreNode {
        String name;     // Oyuncu adı
        int    score;    // Oyuncunun skoru
        ScoreNode prev;  // Önceki düğüme referans
        ScoreNode next;  // Sonraki düğüme referans

        /** Yeni bir skor düğümü oluşturur */
        ScoreNode(String name, int score) {
            this.name  = name;
            this.score = score;
            this.prev  = null;
            this.next  = null;
        }
    }

    // ─── ALANLAR ─────────────────────────────────────────────────────────────────

    /** Listenin başı (en yüksek skor) */
    private ScoreNode head;

    /** Listenin sonu (en düşük skor) */
    private ScoreNode tail;

    /** Listedeki toplam eleman sayısı */
    private int size;

    // ─── YAPICI METOD ────────────────────────────────────────────────────────────

    /** Boş bir skor tablosu oluşturur */
    public HighScoreTable() {
        head = null;
        tail = null;
        size = 0;
    }

    // ─── EKLEME (SIRALI) ─────────────────────────────────────────────────────────

    /**
     * Yeni bir skoru azalan sıraya göre listeye ekler.
     * Eşit skorlar varsa, yeni gelen sona eklenir.
     *
     * @param name  Oyuncu adı
     * @param score Oyuncu skoru
     */
    public void insert(String name, int score) {
        ScoreNode newNode = new ScoreNode(name, score);

        // Liste boşsa, ilk düğüm olarak ekle
        if (head == null) {
            head = newNode;
            tail = newNode;
            size++;
            return;
        }

        // Yeni skor tüm mevcut skorlardan büyükse → başa ekle
        if (score > head.score) {
            newNode.next = head;
            head.prev    = newNode;
            head         = newNode;
            size++;
            return;
        }

        // Doğru konumu bul (azalan sıra)
        ScoreNode current = head;
        while (current != null && current.score >= score) {
            current = current.next;
        }

        if (current == null) {
            // Listenin sonuna ekle (en küçük skor)
            tail.next    = newNode;
            newNode.prev = tail;
            tail         = newNode;
        } else {
            // current'dan önce ekle
            ScoreNode before = current.prev;
            before.next   = newNode;
            newNode.prev  = before;
            newNode.next  = current;
            current.prev  = newNode;
        }
        size++;
    }

    // ─── DOSYA İŞLEMLERİ ─────────────────────────────────────────────────────────

    /**
     * Skor tablosunu dosyadan yükler.
     * Dosya formatı: "İsim Soyisim Skor" (her satırda bir kayıt)
     *
     * @param filename Dosya yolu (örnek: "highscore.txt")
     */
    public void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Son boşluktan sonraki kısım skor, öncesi isim
                int lastSpace = line.lastIndexOf(' ');
                if (lastSpace == -1) continue; // Geçersiz satır

                String name      = line.substring(0, lastSpace).trim();
                String scoreStr  = line.substring(lastSpace + 1).trim();

                try {
                    int score = Integer.parseInt(scoreStr);
                    insert(name, score);
                } catch (NumberFormatException e) {
                    // Geçersiz skor satırı, atla
                }
            }
        } catch (IOException e) {
            // Dosya bulunamazsa varsayılan boş tablo kalır
            System.out.println("Uyarı: " + filename + " okunamadı, boş skor tablosu ile başlanıyor.");
        }
    }

    /**
     * Skor tablosunu dosyaya kaydeder.
     * Mevcut dosyanın üzerine yazar.
     *
     * @param filename Dosya yolu (örnek: "highscore.txt")
     */
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            ScoreNode current = head;
            while (current != null) {
                writer.println(current.name + " " + current.score);
                current = current.next;
            }
        } catch (IOException e) {
            System.out.println("Hata: " + filename + " kaydedilemedi.");
        }
    }

    // ─── ERİŞİM VE GÖRÜNTÜLEME ───────────────────────────────────────────────────

    /** Listedeki eleman sayısını döndürür */
    public int getSize() {
        return size;
    }

    /** Liste boş mu? */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Skor tablosunu standart çıktıya yazdırır.
     * Display.java bu metod yerine kendi çizimini kullanır.
     */
    public void printToConsole() {
        System.out.println("=== YÜKSEK SKOR TABLOSU ===");
        ScoreNode current = head;
        int rank = 1;
        while (current != null) {
            System.out.printf("%2d. %-20s %d%n", rank, current.name, current.score);
            current = current.next;
            rank++;
        }
    }

    /**
     * 'rank' sırasındaki (1'den başlar) ScoreNode'un ismini döndürür.
     * Display.java ekranda göstermek için kullanır.
     *
     * @param rank 1'den başlayan sıra numarası
     * @return İsim veya null (rank geçersizse)
     */
    public String getNameAt(int rank) {
        ScoreNode current = head;
        int i = 1;
        while (current != null) {
            if (i == rank) return current.name;
            current = current.next;
            i++;
        }
        return null;
    }

    /**
     * 'rank' sırasındaki (1'den başlar) ScoreNode'un skorunu döndürür.
     *
     * @param rank 1'den başlayan sıra numarası
     * @return Skor veya -1 (rank geçersizse)
     */
    public int getScoreAt(int rank) {
        ScoreNode current = head;
        int i = 1;
        while (current != null) {
            if (i == rank) return current.score;
            current = current.next;
            i++;
        }
        return -1;
    }
}
