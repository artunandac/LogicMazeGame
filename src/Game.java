

import enigma.core.Enigma;
import enigma.event.TextMouseEvent;
import enigma.event.TextMouseListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import enigma.console.TextAttributes;

public class Game {
    private static enigma.console.Console cn = Enigma.getConsole("Twins Game");
    private TextMouseListener tmlis;
    private KeyListener klis;
    private int pScore = 0;
    private int cScore = 0;
    private int timeUnitCounter = 0;
    private boolean isHardMode;

    //COLORED TEXTS
    private static TextAttributes RED_COLOR_TEXT = new TextAttributes(Color.RED);
    private static TextAttributes GREEN_COLOR_TEXT = new TextAttributes(Color.GREEN);
    private static TextAttributes BLUE_COLOR_TEXT = new TextAttributes(Color.BLUE);
    private static TextAttributes YELLOW_COLOR_TEXT = new TextAttributes(Color.YELLOW);
    private static TextAttributes BLACK_COLOR_TEXT = new TextAttributes(Color.BLACK);
    private static TextAttributes CYAN_COLOR_TEXT = new TextAttributes(Color.CYAN);
    private static TextAttributes WHITE_RED_COLOR_TEXT = new TextAttributes(Color.WHITE, Color.RED);

    private Maze maze;
    private Player player;
    private Laser laser;
    private XRobot[] xRobots;
    private Item item;
    private int xRobotCount = 0;
    private CRobot[] cRobots;
    private int cRobotCount = 0;
    private GameInputSystem inputSystem;

    private int mousepr;
    private int mousex, mousey;
    private int keypr;
    private int rkey;

    public Game(int mapChoice, boolean isHardMode) throws Exception {
        this.isHardMode = isHardMode;
        maze = new Maze();
        if (mapChoice == 2) {
            maze.loadFromFile();
        }
        player = new Player(this);
        laser = new Laser(this);
        xRobots = new XRobot[20];
        cRobots = new CRobot[50];
        inputSystem = new GameInputSystem(this);

        {
            tmlis = new TextMouseListener() {
                public void mouseClicked(TextMouseEvent arg0) {}
                public void mousePressed(TextMouseEvent arg0) {
                    if (mousepr == 0) {
                        mousepr = 1;
                        mousex = arg0.getX();
                        mousey = arg0.getY();
                    }
                }
                public void mouseReleased(TextMouseEvent arg0) {}
            };
            cn.getTextWindow().addTextMouseListener(tmlis);

            klis = new KeyListener() {
                public void keyTyped(KeyEvent e) {}
                public void keyPressed(KeyEvent e) {
                    if (keypr == 0) {
                        keypr = 1;
                        rkey = e.getKeyCode();
                    }
                }
                public void keyReleased(KeyEvent e) {}
            };
            cn.getTextWindow().addKeyListener(klis);
        }

        keypr = 0;
        mousepr = 0;

        maze.drawMaze();
        player.print();
        clearScreen();
        maze.drawMaze();
        player.print();

        inputSystem.addInitialElements();

        while (true) {

            checkPlayerKeys();

            for (int i = 0; i < xRobotCount; i++) {
                XRobot r = xRobots[i];
                if (r.getHealth() <= 0) {
                    cn.getTextWindow().output(r.getX(), r.getY(), ' ');
                    pScore += 100;

                    for (int j = i; j < xRobotCount - 1; j++) {
                        xRobots[j] = xRobots[j + 1];
                    }
                    xRobots[xRobotCount - 1] = null;
                    xRobotCount--;
                    i--;
                    drawInfo();
                } else {
                    cn.getTextWindow().output(r.getX(), r.getY(), ' ');
                    r.attackPlayer();
                    r.move();
                    cn.getTextWindow().output(r.getX(), r.getY(), XRobot.getSymbol(), WHITE_RED_COLOR_TEXT);
                }
            }
            //Crobot
            for (int i = 0; i < cRobotCount; i++) {
                CRobot c = cRobots[i];
                if (c.getHealth() <= 0) {
                    cn.getTextWindow().output(c.getX(), c.getY(), ' ');
                    pScore += 100;

                    for (int j = i; j < cRobotCount - 1; j++) {
                        cRobots[j] = cRobots[j + 1];
                    }
                    cRobots[cRobotCount - 1] = null;
                    cRobotCount--;
                    i--;
                } else {
                    cn.getTextWindow().output(c.getX(), c.getY(), ' ');
                    c.CattackPlayer();
                    c.Cmove();
                    cn.getTextWindow().output(c.getX(), c.getY(), CRobot.getSymbol(), WHITE_RED_COLOR_TEXT);
                }
            }
            if (player.isDead()) {
                drawInfo();
                TextAttributes gameOverAttr = new TextAttributes(Color.WHITE, Color.RED);
                cn.getTextWindow().setCursorPosition(20, 11);
                cn.getTextWindow().output("******************************", gameOverAttr);
                cn.getTextWindow().setCursorPosition(20, 12);
                cn.getTextWindow().output("* GAME OVER!         *", gameOverAttr);
                cn.getTextWindow().setCursorPosition(20, 13);
                cn.getTextWindow().output("******************************", gameOverAttr);
                cn.getTextWindow().setCursorPosition(20, 15);
                cn.getTextWindow().output("Press ENTER to return to menu...", YELLOW_COLOR_TEXT);

                cn.readLine();
                break;
            }

            drawInfo();
            timeUnitCounter++;
            inputSystem.updateTime();

            applyFog();

            Thread.sleep(50);
        }
    }

    public void applyFog() {
        if (!isHardMode) return;

        int sightRadius = 6;

        for (int y = 0; y < Maze.getMapRowsNum(); y++) {
            for (int x = 0; x < Maze.getMapColumnsNum(); x++) {

                double distA = Math.sqrt(Math.pow(x - player.getX(), 2) + Math.pow(y - player.getY(), 2));
                double distB = Math.sqrt(Math.pow(x - player.getBx(), 2) + Math.pow(y - player.getBy(), 2));

                if (distA > sightRadius && distB > sightRadius) {
                    Game.getCn().getTextWindow().output(x, y, ' ');
                } else {
                    char realElement = getElementAt(x, y);
                    if (realElement == maze.getWalltexture()) {
                        Game.getCn().getTextWindow().output(x, y, realElement);
                    } else if (realElement == 'H') {
                        Game.getCn().getTextWindow().output(x, y, realElement, Game.getGreenColorText());
                    } else if (realElement == XRobot.getSymbol() || realElement == CRobot.getSymbol()) {
                        Game.getCn().getTextWindow().output(x, y, realElement, Game.getWhiteRedColorText());
                    } else if (realElement != player.getPlayerA() && realElement != player.getPlayerB()) {
                        Game.getCn().getTextWindow().output(x, y, realElement);
                    }
                }
            }
        }
    }

    public char getElementAt(int checkX, int checkY) {
        if (maze.getMap()[checkY][checkX] != ' ') {
            return maze.getMap()[checkY][checkX];
        }

        if (player.getX() == checkX && player.getY() == checkY)
            return player.getPlayerA();

        if (player.getBx() == checkX && player.getBy() == checkY)
            return player.getPlayerB();

        for (int i = 0; i < xRobotCount; i++) {
            if (xRobots[i].getX() == checkX && xRobots[i].getY() == checkY) {
                return XRobot.getSymbol();
            }
        }

        for (int i = 0; i < cRobotCount; i++) {
            if (cRobots[i].getX() == checkX && cRobots[i].getY() == checkY) {
                return CRobot.getSymbol();
            }
        }
        return ' ';
    }

    public void drawInfo() {
        int seconds = timeUnitCounter / 20;
        cn.getTextWindow().setCursorPosition(60, 1);
        cn.getTextWindow().output("Time     : " + seconds + "  ");

        cn.getTextWindow().setCursorPosition(60, 2);
        cn.getTextWindow().output("P.Score  : " + pScore + "    ");

        cn.getTextWindow().setCursorPosition(60, 3);
        cn.getTextWindow().output("P.Life   : " + player.getPLife() + "    ");

        cn.getTextWindow().setCursorPosition(60, 4);
        cn.getTextWindow().output("P.Laser  : " + player.getLaserCount() + "    ");

        cn.getTextWindow().setCursorPosition(60, 6);
        cn.getTextWindow().output("X-Robots : " + xRobotCount + "  ");

        cn.getTextWindow().setCursorPosition(60,7);
        cn.getTextWindow().output("CRobots : " + cRobotCount+" ");
        cn.getTextWindow().setCursorPosition(60,8);
        cn.getTextWindow().output("CRobot skor : "+cScore+" ");
    }

    public void checkPlayerKeys()
    {
        if (keypr == 1) {
            {
                player.move();
                player.fireLaser();
                player.reverseRotation();
            }
            keypr = 0;
        }
    }

    private void clearScreen() {
        for (int y = 0; y < 25; y++) {
            for (int x = 0; x < 80; x++) {
                cn.getTextWindow().setCursorPosition(x, y);
                cn.getTextWindow().output(' ');
            }
        }
    }

    public boolean isHardMode() { return isHardMode; }
    public void setHardMode(boolean hardMode) { isHardMode = hardMode; }
    public static enigma.console.Console getCn() { return cn; }
    public static void setCn(enigma.console.Console cn) { Game.cn = cn; }
    public TextMouseListener getTmlis() { return tmlis; }
    public void setTmlis(TextMouseListener tmlis) { this.tmlis = tmlis; }
    public KeyListener getKlis() { return klis; }
    public void setKlis(KeyListener klis) { this.klis = klis; }
    public int getPScore() { return pScore; }
    public void setPScore(int pScore) { this.pScore = pScore; }
    public int getCScore() { return cScore; }
    public void setCScore(int cScore) { this.cScore = cScore; }
    public int getTimeUnitCounter() { return timeUnitCounter; }
    public void setTimeUnitCounter(int timeUnitCounter) { this.timeUnitCounter = timeUnitCounter; }
    public static TextAttributes getRedColorText() { return RED_COLOR_TEXT; }
    public static void setRedColorText(TextAttributes redColorText) { RED_COLOR_TEXT = redColorText; }
    public static TextAttributes getGreenColorText() { return GREEN_COLOR_TEXT; }
    public static void setGreenColorText(TextAttributes greenColorText) { GREEN_COLOR_TEXT = greenColorText; }
    public static TextAttributes getBlueColorText() { return BLUE_COLOR_TEXT; }
    public static void setBlueColorText(TextAttributes blueColorText) { BLUE_COLOR_TEXT = blueColorText; }
    public static TextAttributes getYellowColorText() { return YELLOW_COLOR_TEXT; }
    public static void setYellowColorText(TextAttributes yellowColorText) { YELLOW_COLOR_TEXT = yellowColorText; }
    public static TextAttributes getBlackColorText() { return BLACK_COLOR_TEXT; }
    public static void setBlackColorText(TextAttributes blackColorText) { BLACK_COLOR_TEXT = blackColorText; }
    public static TextAttributes getCyanColorText() { return CYAN_COLOR_TEXT; }
    public static void setCyanColorText(TextAttributes cyanColorText) { CYAN_COLOR_TEXT = cyanColorText; }
    public static TextAttributes getWhiteRedColorText() { return WHITE_RED_COLOR_TEXT; }
    public static void setWhiteRedColorText(TextAttributes whiteRedColorText) { WHITE_RED_COLOR_TEXT = whiteRedColorText; }
    public Maze getMaze() { return maze; }
    public void setMaze(Maze maze) { this.maze = maze; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Laser getLaser() { return laser; }
    public void setLaser(Laser laser) { this.laser = laser; }
    public XRobot[] getXRobots() { return xRobots; }
    public void setXRobots(XRobot[] xRobots) { this.xRobots = xRobots; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public int getXRobotCount() { return xRobotCount; }
    public void setXRobotCount(int xRobotCount) { this.xRobotCount = xRobotCount; }
    public CRobot[] getCRobots() { return cRobots; }
    public void setCRobots(CRobot[] cRobots) { this.cRobots = cRobots; }
    public int getCRobotCount() { return cRobotCount; }
    public void setCRobotCount(int cRobotCount) { this.cRobotCount = cRobotCount; }
    public GameInputSystem getInputSystem() { return inputSystem; }
    public void setInputSystem(GameInputSystem inputSystem) { this.inputSystem = inputSystem; }
    public int getMousepr() { return mousepr; }
    public void setMousepr(int mousepr) { this.mousepr = mousepr; }
    public int getMousex() { return mousex; }
    public void setMousex(int mousex) { this.mousex = mousex; }
    public int getMousey() { return mousey; }
    public void setMousey(int mousey) { this.mousey = mousey; }
    public int Keypr() { return keypr; }
    public void setKeypr(int keypr) { this.keypr = keypr; }
    public int getRkey() { return rkey; }
    public void setRkey(int rkey) { this.rkey = rkey; }
}