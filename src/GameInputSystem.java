
import java.util.Random;

public class GameInputSystem {
    private int timeUnitCounter;
    private Random random;
    private Game game; // O

    public GameInputSystem(Game game) {
        this.timeUnitCounter = 0;
        this.random = new Random();
        this.game = game;
    }

    public int getTimeUnitCounter() {
        return timeUnitCounter;
    }

    public void setTimeUnitCounter(int timeUnitCounter) {
        this.timeUnitCounter = timeUnitCounter;
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

    public void addInitialElements() {
        for (int i = 0; i < 10; i++) {
            generateElement();
        }
    }

    public void updateTime() {              //20 =1second 40=2second 60=3second
        timeUnitCounter++;
        if (timeUnitCounter >= 20) {
            generateElement();
            timeUnitCounter = 0;
        }
        game.getLaser().update();
        game.getLaser().draw();
    }

    private void generateElement() {
        int x, y;
        do {
            x = random.nextInt(Maze.getMapColumnsNum() - 2) + 1;
            y = random.nextInt(Maze.getMapRowsNum() - 2) + 1;
        } while (game.getMaze().getMap()[y][x] != ' ' || game.getElementAt(x, y) != ' ');

        int maxDice = -1;
        if(game.isHardMode()){
            maxDice = 12;
        }
        else{
            maxDice = 11;
        }

        int dice = random.nextInt(maxDice) + 1;
        char symbol = ' ';

        if (dice <= 2) { symbol = '1'; }
        else if (dice <= 4) { symbol = '2'; }
        else if (dice <= 6) { symbol = '3'; }
        else if (dice <= 9) { symbol = '@'; }
        else if (dice == 10) {
            //symbol = 'C';
            addCRobot(x, y); // array control for c robot still in use
        }
        else if (dice == 11) {
            //symbol = 'X';
            addXRobot(x, y); // array control for x robot still in use
        }
        else if (dice == 12) {
            symbol = 'H';
        }

        // Chosen symbol directly typed to the board
        game.getMaze().getMap()[y][x] = symbol;
        if (Game.getCn() != null && symbol != ' ') {
            Game.getCn().getTextWindow().setCursorPosition(x, y);
            if(symbol == 'H') {
                Game.getCn().getTextWindow().output(symbol, Game.getGreenColorText());
            } else {
                Game.getCn().getTextWindow().output(symbol, Game.getYellowColorText());
            }
        }
    }

    // robots continue to be held in an array since they are moving objects
    private void addCRobot(int x, int y) {
        for (int i = 0; i < game.getCRobots().length; i++) {
            if (game.getCRobots()[i] == null) {
                CRobot c = new CRobot(game);
                /*
                c.setX(x);
                c.setY(y);
                 */
                game.getCRobots()[i] = c;
                game.setCRobotCount(game.getCRobotCount() + 1);
                break;
            }
        }
    }

    private void addXRobot(int x, int y) {
        for (int i = 0; i < game.getXRobots().length; i++) {
            if (game.getXRobots()[i] == null) {
                XRobot r = new XRobot(game);
                /*
                r.setX(x);
                r.setY(y);
                 */
                game.getXRobots()[i] = r;
                game.setXRobotCount(game.getXRobotCount() + 1);
                break;
            }
        }
    }
}