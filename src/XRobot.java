
import java.util.Random;

public class XRobot {
    private static char symbol = 'X';
    private int health = 1000;
    private int x;
    private int y;
    private int moveCounter = 0;

    private int currentDirection;
    private Random random = new Random();
    private Game game;

    public XRobot(Game anaOyun) {
        this.game = anaOyun;
        placeRobot();
        currentDirection = random.nextInt(4);
    }

    public XRobot(int x, int y)
    {
        this.x=x;
        this.y=y;
    }

    public void placeRobot() {
        while (true) {
            int tryY = random.nextInt(1, Maze.getMapRowsNum() - 1);
            int tryX = random.nextInt(1, Maze.getMapColumnsNum() - 1);
            if (game.getMaze().getMap()[tryY][tryX] == ' ') {
                this.y = tryY;
                this.x = tryX;
                break;
            }
        }
    }

    public void move() {
        moveCounter++;
        if (moveCounter < 4) {
            return;
        }
        moveCounter = 0;

        // 25% probability of direction change at each step
        if (random.nextInt(100) < 25) {
            int newDir;
            do {
                newDir = random.nextInt(4);
            } while (newDir == currentDirection);
            currentDirection = newDir;
        }

        int nextX = this.x;
        int nextY = this.y;

        if (currentDirection == 0) nextY--;      //up
        else if (currentDirection == 1) nextY++; //down
        else if (currentDirection == 2) nextX--; //left
        else if (currentDirection == 3) nextX++; //right

        char targetElement = game.getElementAt(nextX, nextY);

        // NEW RULES:
        // if it's 1, 2 or 3 trace over it and remove it from the map (collect)
        if (targetElement == '1' || targetElement == '2' || targetElement == '3') {
            game.getMaze().getMap()[nextY][nextX] = ' ';
            this.x = nextX;
            this.y = nextY;
        }

        else if (targetElement == ' ') {
            this.x = nextX;
            this.y = nextY;
        }

        else {
            int newDir;
            do {
                newDir = random.nextInt(4);
            } while (newDir == currentDirection);
            currentDirection = newDir;
        }
    }

    public void attackPlayer() {
        if ((Math.abs(this.x - game.getPlayer().getX()) == 1 && this.y == game.getPlayer().getY()) ||
                (Math.abs(this.y - game.getPlayer().getY()) == 1 && this.x == game.getPlayer().getX())) {

            game.getPlayer().setPLife(game.getPlayer().getPLife() - 50); // every 50ms its damages 50

            if (game.getPlayer().getPLife() <= 0) {
                game.getPlayer().setPLife(0);
            }
        }
    }

    public static char getSymbol() {
        return symbol;
    }

    public static void setSymbol(char symbol) {
        XRobot.symbol = symbol;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getMoveCounter() {
        return moveCounter;
    }

    public void setMoveCounter(int moveCounter) {
        this.moveCounter = moveCounter;
    }

    public int getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(int currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}