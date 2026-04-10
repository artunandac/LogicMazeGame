
import java.util.Random;
import java.awt.event.KeyEvent;

public class Player
{
    private char playera = 'A';
    private char playerb = 'B';
    private int y; //y location of A
    private int x; //x location of A
    private int by; //y location of B
    private int bx; //x location of B
    private boolean inDirection = true; //B moves in the same direction as A if true / in the opposite if false
    private int laserCount = 0;
    private int pLife = 1000; // player health
    private Random random = new Random();
    private Game game;

    public Player(Game game) {
        this.game=game;
        place();
    }

    public void place() //places the mob on the map in a random spot initially
    {
        while (true) //tries until finding a valid spot and breaking
        {
            int tryy = random.nextInt(1, Maze.getMapRowsNum() - 1);
            int tryx = random.nextInt(1, Maze.getMapColumnsNum() - 1);
            if (game.getMaze().getMap()[tryy][tryx]!=game.getMaze().getWalltexture()) //checks if a wall is on the way, will have to improve to check for other entities also
            {
                y=tryy;
                x=tryx;

                by=tryy;
                bx=tryx;
                break;
            }
        }
    }

    public void collectItems() {
        // for A
        char itemA = game.getMaze().getMap()[y][x];
        if (itemA == '@') {
            laserCount++;
            game.getMaze().getMap()[y][x] = ' ';
        } else if (itemA == '1' || itemA == '2' || itemA == '3') {
            if (itemA == '1') game.setPScore(game.getPScore() + 9);
            else if (itemA == '2') game.setPScore(game.getPScore() + 30);
            else if (itemA == '3') game.setPScore(game.getPScore() + 90);
            game.getMaze().getMap()[y][x] = ' ';
        } else if (itemA == 'H') { // YENİ: İksiri iç (A)
            int newLife = Math.min(game.getPlayer().getPLife() + 200, 1000);
            game.getPlayer().setPLife(newLife);
            game.getMaze().getMap()[y][x] = ' ';
        }

        // for B
        char itemB = game.getMaze().getMap()[by][bx];
        if (itemB == '@') {
            laserCount++;
            game.getMaze().getMap()[by][bx] = ' ';
        } else if (itemB == '1' || itemB == '2' || itemB == '3') {
            if (itemB == '1') game.setPScore(game.getPScore() + 9);
            else if (itemB == '2') game.setPScore(game.getPScore() + 30);
            else if (itemB == '3') game.setPScore(game.getPScore() + 90);
            game.getMaze().getMap()[by][bx] = ' ';
        } else if (itemB == 'H') { // YENİ: İksiri iç (B)
            int newLife = Math.min(game.getPlayer().getPLife() + 200, 1000);
            game.getPlayer().setPLife(newLife);
            game.getMaze().getMap()[by][bx] = ' ';
        }
    }

    public boolean isDead() {
        if (pLife <= 0) {
            pLife = 0;
            return true;
        }
        return false;
    }

    public void print()
    {
        if (inDirection) {
            Game.getCn().getTextWindow().output(bx, by, playerb, Game.getGreenColorText());
            Game.getCn().getTextWindow().output(x, y, playera, Game.getGreenColorText());
        }
        else {
            Game.getCn().getTextWindow().output(bx, by, playerb, Game.getRedColorText());
            Game.getCn().getTextWindow().output(x, y, playera, Game.getRedColorText());
        }
    }

    public void move()
    {
        //Movement of A
        Game.getCn().getTextWindow().output(x, y, ' '); //needs improvement, can not delete in certain directions
        if ((game.getRkey() == KeyEvent.VK_LEFT) && (canMove(x-1,y))) {
            x--;
        }
        if ((game.getRkey() == KeyEvent.VK_RIGHT) && (canMove(x+1,y))) {
            x++;
        }
        if ((game.getRkey() == KeyEvent.VK_UP) && (canMove(x,y-1))) {
            y--;
        }
        if ((game.getRkey() == KeyEvent.VK_DOWN) && (canMove(x,y+1))) {
            y++;
        }

        //Movement B
        if (inDirection) {
            Game.getCn().getTextWindow().output(bx, by, ' '); //see above
            if ((game.getRkey() == KeyEvent.VK_LEFT) && (canMove(bx-1,by))) {
                bx--;
            }
            if ((game.getRkey() == KeyEvent.VK_RIGHT) && (canMove(bx+1,by))) {
                bx++;
            }
            if ((game.getRkey() == KeyEvent.VK_UP) && (canMove(bx,by-1))) {
                by--;
            }
            if ((game.getRkey() == KeyEvent.VK_DOWN) && (canMove(bx,by+1))) {
                by++;
            }
        }
        if (!inDirection) {
            Game.getCn().getTextWindow().output(bx, by, ' '); //see above
            if ((game.getRkey() == KeyEvent.VK_LEFT) && (canMove(bx+1,by))) {
                bx++;
            }
            if ((game.getRkey() == KeyEvent.VK_RIGHT) && (canMove(bx-1,by))) {
                bx--;
            }
            if ((game.getRkey() == KeyEvent.VK_UP) && (canMove(bx,by+1))) {
                by++;
            }
            if ((game.getRkey() == KeyEvent.VK_DOWN) && (canMove(bx,by-1))) {
                by--;
            }
        }
        collectItems();
        print();
    }

    void fireLaser() {
        if (game.getRkey() == KeyEvent.VK_SPACE) {
            // fire from a to b
            game.getLaser().fire(x, y, bx, by);
        }
    }

    void reverseRotation() {
        if (game.getRkey() == 'R') {
            inDirection = !inDirection;
        }
        print();
    }

    private boolean canMove(int x, int y)
    {
        if ((game.getElementAt(x, y)==(game.getMaze().getWalltexture()))||(game.getElementAt(x, y)==CRobot.getSymbol())||(game.getElementAt(x, y)==(XRobot.getSymbol())))
            return false;
        else
            return true;
    }

    public char getPlayerA() { return playera; }
    public void setPlayerA(char playera) { this.playera = playera; }
    public char getPlayerB() { return playerb; }
    public void setPlayerB(char playerb) { this.playerb = playerb; }
    public int getY() { return y; }
    public void setY(int inputY) { y = inputY; }
    public int getX() { return x; }
    public void setX(int inputX) { x = inputX; }
    public int getBy() { return by; }
    public void setBy(int by) { this.by = by; }
    public int getBx() { return bx; }
    public void setBx(int bx) { this.bx = bx; }
    public boolean getInDirection() { return inDirection; }
    public void setInDirection(boolean inDirection) { this.inDirection = inDirection; }
    public int getLaserCount() { return laserCount; }
    public void setLaserCount(int laserCount) { this.laserCount = laserCount; }
    public int getPLife() { return pLife; }
    public void setPLife(int pLife) { this.pLife = pLife; }
    public Random getRandom() { return random; }
    public void setRandom(Random random) { this.random = random; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
}