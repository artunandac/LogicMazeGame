public class TruthTable {

    private int[][] table;       // 16 satır × kolon sayısı
    private int[]   questionRow; // her kolon için boş satır indeksi

    public void compute(ExpressionTree tree) {
        // TODO: A,B,C,D tüm 16 kombinasyonu hesapla
    }

    // +3 doğru, -2 yanlış
    public int checkAnswer(int col, int row, int answer) {
        // TODO
        return 0;
    }

    public String[] getColumnNames() {
        // TODO: ara ifade isimlerini döndür
        return new String[0];
    }

    @Override
    public String toString() {
        // TODO: tabloyu string olarak formatla
        return "";
    }
}
