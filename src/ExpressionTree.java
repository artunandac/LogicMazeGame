//bu class, oyuncunun oluşturduğu logic ifadesini tutan ve process eden bir complete binary tree yapısıdır. Oyuncu sembolleri yerleştirirken, cursor hareket ettirirken, sembolleri kaldırırken ve ağacı doğrularken bu class'ın methodları kullanılır. Ayrıca infix/postfix gösterim ve değerlendirme (truth table için) işlemleri de burada yapılır. Görsel ağaç çizimi için char grid oluşturma methodu da içerir.
public class ExpressionTree
{

    // Complete Binary tree index 0 kullanılmaz 1-31 arasındaki indexli nodelar kullanılır
    
    private char[] nodes       = new char[32];
    private int    cursorIndex = 1;
    public  int    penalty     = 0;   

    //Operator ve Variable kontrolleri
    private boolean isOperator(char c)
    {
        // ~ (NOT), v (OR), ^ (AND), + (XOR), > (IMPLIES) = (IFF)
        return c == '~' || c == 'v' || c == '^' || c == '+' || c == '>'|| c == '=';
    }

    private boolean isVariable(char c)
    {
        char lower = Character.toLowerCase(c);
        return lower == 'a' || lower == 'b' || lower == 'c' || lower == 'd';
    }

    //ancestor'larda değişken var mı, varsa o slot'a sembol yerleştirilemez
    private boolean hasVariableAncestor(int i)
    {
        int p = i / 2;
        while (p >= 1)
        {
            if (isVariable(nodes[p])) return true;
            p = p / 2;
        }
        return false;
    }

    private boolean isSlotBlocked(int i)
    {
        if (i < 1 || i > 31) return true;
        if (hasVariableAncestor(i)) return true;
        // NOT (~) sadece sol çocuğa izin verir
        if (i > 1)
        {
            int parent = i / 2;
            if (nodes[parent] == '~' && i == parent * 2 + 1) return true;  // sağ çocuk NOT'un sağ çocuğu ise engellenir
        }
        return false;
    }

    public boolean placeSymbol(char c)
    {
        if (!isOperator(c) && !isVariable(c)) return false;
        if (cursorIndex < 1 || cursorIndex > 31) return false;
        if (isSlotBlocked(cursorIndex)) return false;
        nodes[cursorIndex] = c;
        advanceCursor();
        return true;
    }

    // Cursoru bir sonraki uygun boş slota ilerlet
    private void advanceCursor()
    {
        for (int i = cursorIndex + 1; i <= 31; i++)
        {
            if (nodes[i] == 0 && !isSlotBlocked(i)) { cursorIndex = i; return; }
        }
        for (int i = 1; i <= cursorIndex; i++)
        {
            if (nodes[i] == 0 && !isSlotBlocked(i)) { cursorIndex = i; return; }
        }
    }

    public boolean moveCursor(char dir)
    {
        int next = cursorIndex;
        if (dir == 'W' && cursorIndex > 1)
        {
            next = cursorIndex / 2;
        }
        else if (dir == 'A' && cursorIndex * 2 <= 31)
        {
            int target = cursorIndex * 2;
            if (isNodeVisible(target)) next = target;
        }
        else if (dir == 'D' && cursorIndex * 2 + 1 <= 31)
        {
            int target = cursorIndex * 2 + 1;
            if (isNodeVisible(target)) next = target;
        }

        if (next != cursorIndex)
        {
            cursorIndex = next;
            return true;
        }
        return false;
    }

    // Symbol Removing (R tuşu): cursordaki sembol kaldırılır yerine 0 konur
    public char removeAtCursor()
    {
        if (cursorIndex < 1 || cursorIndex > 31) return 0;
        char sym = nodes[cursorIndex];
        if (sym == 0) return 0;   
        nodes[cursorIndex] = 0;
        penalty += 2;   // her başarılı kaldırma işlemi için penaltye 2 puan eklenir
        return sym;
    }

    // Getters 

    public int getCursorIndex() { return cursorIndex; }

    public char getNode(int i)
    {
        if (i < 1 || i > 31) return 0;
        return nodes[i];
    }

    // Validation
    public boolean isValid()
    {
        // en az 3 variable olmalı 
        int varCount = 0;
        for (int i = 1; i <= 31; i++)
        {
            if (isVariable(nodes[i])) varCount++;
        }
        if (varCount < 3) return false;

        // depth en az 3 olmalı 
        // Level 3 düğümleri: index 8-15
        boolean hasDepth3 = false;
        for (int i = 8; i <= 31; i++)
        {
            if (nodes[i] != 0) { hasDepth3 = true; break; }
        }
        return hasDepth3;
    }

    // INFIX(INORDER)
    public String toInfix()
    {
        return buildInfix(1);
    }

    private String buildInfix(int i)
    {
        if (i > 31 || nodes[i] == 0) return "";

        int left  = i * 2;
        int right = i * 2 + 1;
        boolean hasLeft  = (left  <= 31 && nodes[left]  != 0);
        boolean hasRight = (right <= 31 && nodes[right] != 0);

        // LEAF NODE
        if (!hasLeft && !hasRight)
        {
            return "" + nodes[i];
        }

        // NOT (~) — tek çocuksa unary, iki çocuksa binary
        if (nodes[i] == '~')
        {
            if (hasLeft && !hasRight)
                return "(~" + buildInfix(left) + ")";
            if (!hasLeft && hasRight)
                return "(~" + buildInfix(right) + ")";
            // İki çocuk varsa binary olarak göster
        }

        // İkili operatörler: sol ve sağ çocukları parantez içinde göster
        String leftStr  = hasLeft  ? buildInfix(left)  : "";
        String rightStr = hasRight ? buildInfix(right) : "";
        return "(" + leftStr + nodes[i] + rightStr + ")";
    }

    // POSTFIX(POSTORDER)

    public String toPostfix()
    {
        return buildPostfix(1).trim();
    }

    private String buildPostfix(int i)
    {
        if (i > 31 || nodes[i] == 0) return "";

        int left  = i * 2;
        int right = i * 2 + 1;
        boolean hasLeft  = (left  <= 31 && nodes[left]  != 0);
        boolean hasRight = (right <= 31 && nodes[right] != 0);

        // NOT (~) — tek çocuksa unary
        if (nodes[i] == '~')
        {
            if (hasLeft && !hasRight)
                return buildPostfix(left) + "~ ";
            if (!hasLeft && hasRight)
                return buildPostfix(right) + "~ ";
            // İki çocuk varsa binary olarak devam et
        }

        String leftStr  = hasLeft  ? buildPostfix(left)  : "";
        String rightStr = hasRight ? buildPostfix(right) : "";
        return leftStr + rightStr + nodes[i] + " ";
    }

    // Node count (validation ve HUD için)
    public int getNodeCount()
    {
        int count = 0;
        for (int i = 1; i <= 31; i++)
        {
            if (nodes[i] != 0) count++;
        }
        return count;
    }

    // GRID BUILDING FOR VISUALIZATION
    private static final int GRID_ROWS = 9;
    private static final int GRID_COLS = 46;

    // Level 4 (nodes 16-31): 0,3,6,9,12,15,18,21, 24,27,30,33,36,39,42,45
    // Level 3 (nodes 8-15):  midpoints
    // Level 2 (nodes 4-7):   midpoints
    // Level 1 (nodes 2-3):   midpoints
    // Level 0 (node  1):     midpoint
    private static final int[] NODE_X = new int[32];
    private static final int[] NODE_Y = new int[32];

    static
    {
        // Level 4 (leaf): nodes 16-31
        for (int k = 0; k < 16; k++)
            NODE_X[16 + k] = k * 3;

        // Level 3: nodes 8-15
        for (int k = 0; k < 8; k++)
            NODE_X[8 + k] = (NODE_X[16 + k * 2] + NODE_X[17 + k * 2]) / 2;

        // Level 2: nodes 4-7
        for (int k = 0; k < 4; k++)
            NODE_X[4 + k] = (NODE_X[8 + k * 2] + NODE_X[9 + k * 2]) / 2;

        // Level 1: nodes 2-3
        NODE_X[2] = (NODE_X[4] + NODE_X[5]) / 2;
        NODE_X[3] = (NODE_X[6] + NODE_X[7]) / 2;

        // Level 0: node 1
        NODE_X[1] = (NODE_X[2] + NODE_X[3]) / 2;

        // Y coordinates = level * 2 (0, 2, 4, 6, 8)
        for (int i = 1; i <= 31; i++)
        {
            int level = 0;
            int tmp = i;
            while (tmp > 1) { tmp /= 2; level++; }
            NODE_Y[i] = level * 2;
        }
    }

    private boolean isNodeVisible(int i)
    {
        if (i == 1) return true; // root her zaman görünür
        if (i < 1 || i > 31) return false;
        if (nodes[i] != 0) return true;//ancestor dolmuşsa görünür
        // Boş slot kontrolleri
        if (isSlotBlocked(i)) return false;   
        int parent = i / 2;
        return nodes[parent] != 0; // parent dolu ve değişken değilse görünür
    }

    public char[][] buildGrid()
    {
        char[][] grid = new char[GRID_ROWS][GRID_COLS];

        for (int r = 0; r < GRID_ROWS; r++)
            for (int c = 0; c < GRID_COLS; c++)
                grid[r][c] = ' ';

        for (int i = 1; i <= 31; i++)
        {
            if (!isNodeVisible(i)) continue;

            int gx = NODE_X[i];
            int gy = NODE_Y[i];
            if (gy >= GRID_ROWS || gx >= GRID_COLS) continue;

            if (nodes[i] != 0)
                grid[gy][gx] = nodes[i];
            else
                grid[gy][gx] = '.';   
        }

        for (int i = 1; i <= 15; i++)
        {
            if (!isNodeVisible(i)) continue;

            int left  = i * 2;
            int right = i * 2 + 1;
            boolean leftVisible  = (left  <= 31 && isNodeVisible(left));
            boolean rightVisible = (right <= 31 && isNodeVisible(right));
            if (!leftVisible && !rightVisible) continue;

            int py = NODE_Y[i];
            int connY = py + 1;
            if (connY >= GRID_ROWS) continue;

            int px = NODE_X[i];

            if (leftVisible)
            {
                int lx = NODE_X[left];
                if (connY < GRID_ROWS && lx < GRID_COLS)
                    grid[connY][lx] = '/';
                for (int c = lx + 1; c < px; c++)
                {
                    if (c < GRID_COLS)
                        grid[connY][c] = '-';
                }
            }

            if (rightVisible)
            {
                int rx = NODE_X[right];
                if (connY < GRID_ROWS && rx < GRID_COLS)
                    grid[connY][rx] = '\\';
                for (int c = px + 1; c < rx; c++)
                {
                    if (c < GRID_COLS)
                        grid[connY][c] = '-';
                }
            }
        }

        return grid;
    }

    public int getGridRows() { return GRID_ROWS; }
    public int getGridCols() { return GRID_COLS; }
    public int getNodeX(int i) { return (i >= 1 && i <= 31) ? NODE_X[i] : -1; }
    public int getNodeY(int i) { return (i >= 1 && i <= 31) ? NODE_Y[i] : -1; }
}
