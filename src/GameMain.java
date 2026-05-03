import enigma.console.Console;
import enigma.core.Enigma;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class GameMain {

    private static final int CONSOLE_W = 80;
    private static final int CONSOLE_H = 24;
    private static final int FONT_SIZE = 20;
    private static final int HUD_X     = 53;

    private static final int MAZE  = 1;
    private static final int TREE  = 2;
    private static final int TABLE = 3;
    private int currentScreen = MAZE;

    private Console cn;

    private Board          board;
    private Player         player;
    private Robot[]        robots;
    private int            robotCount;
    private LogicSymbol[]  symbols;
    private int            symbolCount;
    private Fireball       fireball;
    private InputQueue     inputQueue;
    private ExpressionTree tree;
    private TruthTable     truthTable;
    private HighScoreList  highScoreList;

    private long startTime;
    private long pausedMs       = 0;
    private long pauseStart     = 0;
    private long screenEnterTime = 0;
    private int  robotTimer;
    private int  inputTimer;

    private volatile int rkey  = 0;
    private volatile int keypr = 0;

    private boolean paused   = false;
    private boolean gameOver = false;
    private String  treeMessage = "";

    private Random rand = new Random();

    public GameMain() {
        cn = Enigma.getConsole("Logic Maze Game", CONSOLE_W, CONSOLE_H, FONT_SIZE);

        cn.getTextWindow().addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e)    {}
            public void keyReleased(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {
                if (keypr == 0) { keypr = 1; rkey = e.getKeyCode(); }
            }
        });

        board = new Board();
        if (!board.loadMazeFromFile("maze.txt"))
            System.out.println("ERROR: maze.txt yüklenemedi!");
            // board.generateMaze();
        player        = new Player();
        robots        = new Robot[100];
        robotCount    = 0;
        symbols       = new LogicSymbol[300];
        symbolCount   = 0;
        fireball      = new Fireball();
        inputQueue    = new InputQueue();
        tree          = new ExpressionTree();
        truthTable    = new TruthTable();
        highScoreList = new HighScoreList();
        highScoreList.loadFromFile("highscore.txt");


        int[] pos = randomFreeCell();
        player.x = pos[0];
        player.y = pos[1];

        for (int i = 0; i < 10; i++)
            spawnFromQueue();
        System.out.println("DEBUG init: symbols=" + symbolCount + " robots=" + robotCount);

        startTime  = System.currentTimeMillis();
        robotTimer = 0;
        inputTimer = 0;
    }


    public void run() {
        while (!gameOver) {
            long t0 = System.currentTimeMillis();

            handleInput();

            if (!paused && currentScreen == MAZE) {
                robotTimer++;
                inputTimer++;

                for (int i = 0; i < robotCount; i++)
                    robots[i].tickModeSwitch();

                if (fireball.isActive())   updateFireball();
                if (robotTimer >= 4)       { moveAllRobots();  robotTimer = 0; }
                if (inputTimer >= 20)      { spawnFromQueue(); inputTimer = 0; }
                checkNeighborHarm();

                if (!player.isAlive()) { gameOver = true; handleGameOver(); break; }
            }

            render();

            long sleep = 100 - (System.currentTimeMillis() - t0);
            if (sleep > 0) try { Thread.sleep(sleep); } catch (InterruptedException ignored) {}
        }
    }

    // INPUT HANDLING
    private void handleInput() {
        if (keypr != 1) return;
        keypr = 0;

        if (rkey == KeyEvent.VK_ESCAPE) { gameOver = true; return; }

        // Screen geçişi (Key 1/2/3 maze/tree/table)
        if      (rkey == KeyEvent.VK_1) { paused = false; switchScreen(MAZE);  return; }
        else if (rkey == KeyEvent.VK_2) { paused = true;  switchScreen(TREE);  return; }
        else if (rkey == KeyEvent.VK_3) { paused = true;  switchScreen(TABLE); return; }

        if (currentScreen == MAZE) {
            if (rkey == KeyEvent.VK_P) { paused = !paused; return; }
        }

        if (currentScreen == MAZE && !paused) {
            int dx = 0, dy = 0;
            if      (rkey == KeyEvent.VK_LEFT)   dx = -1;
            else if (rkey == KeyEvent.VK_RIGHT)  dx =  1;
            else if (rkey == KeyEvent.VK_UP)     dy = -1;
            else if (rkey == KeyEvent.VK_DOWN)   dy =  1;
            else if (rkey == KeyEvent.VK_SPACE)  tryFireFireball();
            else if (rkey == KeyEvent.VK_M)     player.toggleStorageMode();

            if (dx != 0 || dy != 0) {
                int nx = player.x + dx, ny = player.y + dy;
                checkAndCollectSymbol(nx, ny);
                player.move(dx, dy, board, robots, robotCount);
            }
            return;
        }

        if (currentScreen == TREE) {
            if      (rkey == KeyEvent.VK_W) { if (tree.moveCursor('W')) player.score--; }
            else if (rkey == KeyEvent.VK_A) { if (tree.moveCursor('A')) player.score--; }
            else if (rkey == KeyEvent.VK_D) { if (tree.moveCursor('D')) player.score--; }
            else if (rkey == KeyEvent.VK_T) {
                if (player.getBackpackSize() > 0) {
                    char sym = player.removeFromBackpack(0);
                    tree.placeSymbol(sym);
                    treeMessage = "Placed '" + sym + "' on tree.";
                } else {
                    treeMessage = "Backpack is empty!";
                }
            }
            else if (rkey == KeyEvent.VK_R) {
                char sym = tree.removeAtCursor();
                if (sym != 0) {
                    if (player.addToBackpack(sym)) {
                        player.score -= 2;
                        treeMessage = "Removed '" + sym + "' to backpack. (-2)";
                    } else {
                        tree.placeSymbol(sym);
                        treeMessage = "Backpack is full!";
                    }
                } else {
                    treeMessage = "Slot is empty!";
                }
            }
            else if (rkey == KeyEvent.VK_F) {
                if (tree.isValid()) {
                    int bonus = 10 * tree.getNodeCount();
                    player.score += bonus;
                    treeMessage = "Tree OK! +" + bonus + " points!";
                    switchScreen(TABLE);
                } else {
                    player.score -= 10;
                    treeMessage = "Invalid tree! Min 3 vars & depth 3. (-10)";
                }
                return;
            }
            return;
        }

    }

    private void checkAndCollectSymbol(int nx, int ny) {
        for (int i = 0; i < symbolCount; i++) {
            if (symbols[i] != null && symbols[i].x == nx && symbols[i].y == ny) {
                collectSymbol(i);
                return;
            }
        }
    }

    private void collectSymbol(int idx) {
        char sym = symbols[idx].symbol;
        board.setCell(symbols[idx].y, symbols[idx].x, ' ');
        removeSymbol(idx);


        if (sym == '@') {
            player.fireballCount++;
            player.score += 5;
            return;
        }

        if (player.storageTree) {
            if (!tree.placeSymbol(sym))       
                player.addToBackpack(sym);     
        } else {
            if (!player.addToBackpack(sym))    
                tree.placeSymbol(sym);         
        }
        player.score += 5;
    }

    // FIREBALL
    private void tryFireFireball() {
        if (player.fireballCount > 0 && !fireball.isActive()) {
            player.fireballCount--;
            fireball.launch(player.x, player.y, player.lastDx, player.lastDy);
        }
    }

    private void updateFireball() {
        fireball.move(board);
        if (!fireball.isActive()) return;
        for (int i = robotCount - 1; i >= 0; i--) {
            if (robots[i].x == fireball.x && robots[i].y == fireball.y) {
                player.score += 50;
                removeRobot(i);
            }
        }
    }

    // ROBOT
    private void removeRobot(int idx) {
        robots[idx] = robots[robotCount - 1];
        robots[robotCount - 1] = null;
        robotCount--;
    }

    private void removeSymbol(int idx) {
        symbols[idx] = symbols[symbolCount - 1];
        symbols[symbolCount - 1] = null;
        symbolCount--;
    }

    private void moveAllRobots() {
        for (int i = 0; i < robotCount; i++) {
            int tx = player.x, ty = player.y;

            if (robots[i].targeted) {
                // En yakın LogicSymbol'ü hedefle, yoksa player konumunu hedefle
                int best = Integer.MAX_VALUE;
                for (int s = 0; s < symbolCount; s++) {
                    if (symbols[s] == null) continue;
                    int d = Math.abs(symbols[s].x - robots[i].x)
                          + Math.abs(symbols[s].y - robots[i].y);
                    if (d < best) { best = d; tx = symbols[s].x; ty = symbols[s].y; }
                }
                robots[i].stepTargeted(board, robots, robotCount, player.x, player.y, tx, ty);
            } else {
                robots[i].step(board, robots, robotCount, player.x, player.y);
            }
            for (int s = symbolCount - 1; s >= 0; s--) {
                if (symbols[s] != null
                        && symbols[s].x == robots[i].x
                        && symbols[s].y == robots[i].y) {
                    board.setCell(symbols[s].y, symbols[s].x, ' ');
                    removeSymbol(s);
                }
            }
        }
    }

    // NEIGHBOR HARM (her tick -5 HP)
    private void checkNeighborHarm() {
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};
        for (int i = 0; i < robotCount; i++)
            for (int d = 0; d < 4; d++)
                if (robots[i].x == player.x + dx[d]
                 && robots[i].y == player.y + dy[d]) {
                    player.hp -= 5;
                    return;
                }
    }

    // INPUT QUEUE
    private void spawnFromQueue() {
        char elem = inputQueue.dequeue();
        int[] pos = randomFreeCell();
        if (pos == null) return;

        if (elem == 'X') {
            if (robotCount < robots.length)
                robots[robotCount++] = new Robot(pos[0], pos[1]);
        } else if (elem == '@') {
            // Fireball paketini maze'e koy, oyuncu yürüyüp toplasın
            if (symbolCount < symbols.length) {
                symbols[symbolCount++] = new LogicSymbol(pos[0], pos[1], '@');
                board.setCell(pos[1], pos[0], '@');
            }
        } else if (elem != ' ') {
            if (symbolCount < symbols.length) {
                symbols[symbolCount++] = new LogicSymbol(pos[0], pos[1], elem);
                board.setCell(pos[1], pos[0], elem);
            }
        }
    }

    // GAME OVER
    private void handleGameOver() {
        clearScreen();
        writeColored(28, 10, "GAME  OVER", Color.RED);
        writeText(22, 12, "Final Score : " + player.score);
        highScoreList.insert("Player", player.score);
        highScoreList.saveToFile("highscore.txt");
        String[] lines = highScoreList.toString().split("\n");
        for (int i = 0; i < lines.length && i < 8; i++)
            writeText(22, 14 + i, lines[i]);
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
    }


    // RENDER
    private void render() {
        switch (currentScreen) {
            case MAZE:  renderMaze();  break;
            case TREE:  renderTree();  break;
            case TABLE: renderTable(); break;
        }
    }

    private void renderMaze() {
        for (int r = 0; r < Board.ROWS; r++)
            for (int c = 0; c < Board.COLS; c++) {
                cn.getTextWindow().setCursorPosition(c, r);
                cn.getTextWindow().output(board.grid[r][c]);
            }

        for (int i = 0; i < symbolCount; i++) {
            if (symbols[i] == null) continue;
            writeColored(symbols[i].x, symbols[i].y, "" + symbols[i].symbol, Color.ORANGE);
        }

        for (int i = 0; i < robotCount; i++) {
            Color col = robots[i].targeted ? Color.RED : Color.GREEN;
            writeColored(robots[i].x, robots[i].y, "X", col);
        }

        if (fireball.isActive())
            writeColored(fireball.x, fireball.y, "o", Color.CYAN);
        writeColored(player.x, player.y, "P", Color.GREEN);
        renderHUD();
    }

    private void renderHUD() {
        long offMs = pausedMs + (currentScreen != MAZE && pauseStart > 0 ? System.currentTimeMillis() - pauseStart : 0);
        long sec = (System.currentTimeMillis() - startTime - offMs) / 1000;

        writeText(HUD_X, 0, "Input");
        writeColored(HUD_X, 1, "<<<<<<<<<<<", Color.YELLOW);
        char[] q = inputQueue.getAll();
        StringBuilder sb = new StringBuilder();
        for (char ch : q) sb.append(ch == 0 ? ' ' : ch);
        writeText(HUD_X, 2, sb.toString());
        writeColored(HUD_X, 3, "<<<<<<<<<<<", Color.YELLOW);

        writeText(HUD_X, 5, "Time    : " + sec + "   ");
        writeText(HUD_X, 6, "Score   : " + player.score + "   ");
        writeText(HUD_X, 7, "Fireball: " + player.fireballCount + "   ");
        writeText(HUD_X, 8, "Life    : " + player.hp + "   ");
        writeText(HUD_X, 9, "Storage : " + (player.storageTree ? "Tree    " : "Backpack"));

        if (paused) writeColored(HUD_X, 11, "-- PAUSED --", Color.YELLOW);
        else        writeText(HUD_X, 11, "            ");
        writeColored(HUD_X, 12, "DBG sym=" + symbolCount + " rob=" + robotCount + "   ", Color.GRAY);

        writeText(HUD_X, 13, "+------+");
        String[] bp = player.printBackpack();
        for (int i = 0; i < 8; i++)
            writeText(HUD_X, 14 + i, bp[i]);
        writeText(HUD_X, 22, "+------+");
        writeText(HUD_X, 23, "Backpack");
    }

    private void renderTree() {
        clearScreen();

        char[][] grid = tree.buildGrid();
        int curIdx = tree.getCursorIndex();
        int curGX  = tree.getNodeX(curIdx);
        int curGY  = tree.getNodeY(curIdx);

        for (int r = 0; r < tree.getGridRows(); r++) {
            for (int c = 0; c < tree.getGridCols() && c < HUD_X - 1; c++) {
                char ch = grid[r][c];
                if (r == curGY && c == curGX) {
                    writeColored(c, r, "" + ch, Color.GREEN);
                } else if (ch == '/' || ch == '\\' || ch == '-') {
                    writeColored(c, r, "" + ch, Color.DARK_GRAY);
                } else if (ch != ' ' && ch != '.') {
                    writeColored(c, r, "" + ch, Color.ORANGE);
                } else if (ch == '.') {
                    writeColored(c, r, ".", Color.DARK_GRAY);
                }
            }
        }

        int infoY = tree.getGridRows() + 1;   // row 10
        long treeSec = screenEnterTime > 0 ? (System.currentTimeMillis() - screenEnterTime) / 1000 : 0;
        writeText(0, infoY,     "W=Up  A=Left  D=Right  T=Place  R=Remove  F=Finish");
        writeText(0, infoY + 1, "Cursor: node " + curIdx + "   ");

        writeText(0, infoY + 3, "Expression");
        writeText(0, infoY + 4, "Infix  : " + tree.toInfix() + "   ");
        writeText(0, infoY + 5, "Postfix: " + tree.toPostfix() + "   ");

        if (treeMessage.length() > 0)
            writeColored(0, infoY + 7, treeMessage + "   ", Color.YELLOW);

        writeColored(0, 22, "Time in Tree: " + treeSec + "s   ", Color.CYAN);

        renderHUD();
    }

    private void renderTable() {
        clearScreen();
        long tableSec = screenEnterTime > 0 ? (System.currentTimeMillis() - screenEnterTime) / 1000 : 0;
        writeText(0, 0, "--- TABLE SCREEN ---  Key 1: maze");
        writeColored(0, 22, "Time in Table: " + tableSec + "s   ", Color.CYAN);
        writeText(0, 2, truthTable.toString());
        renderHUD();
    }

    // SCREEN MANAGEMENT
    private void switchScreen(int screen) {
        if (screen != MAZE && currentScreen == MAZE) {
            pauseStart = System.currentTimeMillis();
            screenEnterTime = pauseStart;
        } else if (screen == MAZE && currentScreen != MAZE && pauseStart > 0) {
            pausedMs += System.currentTimeMillis() - pauseStart;
            screenEnterTime = 0;
        } else if (screen != MAZE) {
            screenEnterTime = System.currentTimeMillis();
        }
        currentScreen = screen;
        clearScreen();
    }

    private void clearScreen() {
        for (int r = 0; r < CONSOLE_H; r++)
            for (int c = 0; c < CONSOLE_W; c++) {
                cn.getTextWindow().setCursorPosition(c, r);
                cn.getTextWindow().output(' ');
            }
    }

    private void writeText(int x, int y, String s) {
        for (int i = 0; i < s.length() && x + i < CONSOLE_W; i++) {
            cn.getTextWindow().setCursorPosition(x + i, y);
            cn.getTextWindow().output(s.charAt(i));
        }
    }

    private void writeColored(int x, int y, String s, Color fg) {
        enigma.console.TextAttributes attr =
                new enigma.console.TextAttributes(fg, Color.BLACK);
        for (int i = 0; i < s.length() && x + i < CONSOLE_W; i++) {
            cn.getTextWindow().setCursorPosition(x + i, y);
            cn.getTextWindow().output(s.charAt(i), attr);
        }
    }

    private int[] randomFreeCell() {
        for (int attempt = 0; attempt < 500; attempt++) {
            int c = 1 + rand.nextInt(Board.COLS - 2);
            int r = 1 + rand.nextInt(Board.ROWS - 2);
            if (board.grid[r][c] != ' ') continue;
            if (c == player.x && r == player.y) continue;
            boolean blocked = false;
            for (int i = 0; i < robotCount; i++)
                if (robots[i].x == c && robots[i].y == r) { blocked = true; break; }
            if (!blocked) return new int[]{c, r};
        }
        return new int[]{1, 1};
    }
}
