

import java.util.Random;

public class CRobot
{
    private static char symbol ='C';
    private int health=1000;
    private int x;
    private int y;
    private int CmoveCounter=0; // for moving in every four tours

    private Random rn = new Random();
    private Game game;

    //constructor
    public CRobot(Game game)
    {
        this.game=game;
        placeCRobot();
    }

    public CRobot(int x, int y)
    {
        this.x=x;
        this.y=y;
    }

    //for placing on the map
    public void placeCRobot()
    {
        while(true)
        {
            int tryY = rn.nextInt(1, Maze.getMapRowsNum() - 1);
            int tryX = rn.nextInt(1, Maze.getMapColumnsNum() - 1);

            if(game.getMaze().getMap()[tryY][tryX]==' ') // check whether the coordinates are empty
            {
                this.y=tryY;
                this.x= tryX;
                break;
            }
        }
    }

    public void CattackPlayer()
    {
        if((Math.abs(this.x-game.getPlayer().getX())==1 && this.y == game.getPlayer().getY() )||(Math.abs(this.y-game.getPlayer().getY())==1 && this.x==game.getPlayer().getX()))
        {
            game.getPlayer().setPLife(game.getPlayer().getPLife() - 50);
            if(game.getPlayer().getPLife()<0){
                game.getPlayer().setPLife(0);
            }
        }
    }

    public void Cmove()
    {
        CmoveCounter ++;
        if(CmoveCounter < 4){
            return;
        }
        CmoveCounter=0;
        // Manhattan distance algorithm
        // Nearest target information

        int nearestX = -1;
        int nearestY = -1;
        int minDistance = 99999;

        for(int mapY = 1; mapY < Maze.getMapRowsNum() - 1; mapY++)
        {
            for(int mapX = 1; mapX < Maze.getMapColumnsNum() - 1; mapX++)
            {
                //1 2 3 are exist?
                char scannedSquare = game.getMaze().getMap()[mapY][mapX];
                if (scannedSquare == '1' || scannedSquare == '2' || scannedSquare == '3') {
                    // Target found , calculate distance using Manhattan formula
                    int distance = Math.abs(mapX - this.x) + Math.abs(mapY - this.y);

                    // Determine which one is the closest
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestX = mapX;
                        nearestY = mapY;
                    }
                }
            }
        }
        //if treasure does not exist
        if (nearestX == -1 || nearestY == -1) {
            return;
        }
        int nextX = this.x;
        int nextY = this.y;

        // Movement logic:
        // If target X is greater than current X, move right (x++)
        // If target X is smaller, move left (x--)
        // Same logic applies to the Y axis
        if (this.x < nearestX) {
            nextX++;
        } else if (this.x > nearestX) {
            nextX--;
        } else if (this.y < nearestY) {
            nextY++;
        } else if (this.y > nearestY) {
            nextY--;
        }

        // Check if the next square is an obstacle
        char nextTile = game.getElementAt(nextX, nextY);
        if (nextTile == ' ' || nextTile == '1' || nextTile == '2' || nextTile == '3') {
            if (nextTile != ' ') {
                int collectedValue = Character.getNumericValue(nextTile);
                game.getMaze().getMap()[nextY][nextX] = ' '; // Clear the treasure from the map

                // Treasures are 3 times more valuable for computers [cite: 15]
                if (collectedValue == 1) {
                    game.setCScore(game.getCScore() + 9);
                } else if (collectedValue == 2) {
                    game.setCScore(game.getCScore() + 30);
                } else if (collectedValue == 3) {
                    game.setCScore(game.getCScore() + 90);
                }
            }
            this.x = nextX;
            this.y = nextY;
        }
    }

    public static char getSymbol() {
        return symbol;
    }

    public static void setSymbol(char symbol) {
        CRobot.symbol = symbol;
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

    public int getCmoveCounter() {
        return CmoveCounter;
    }

    public void setCmoveCounter(int CmoveCounter) {
        this.CmoveCounter = CmoveCounter;
    }

    public Random getRn() {
        return rn;
    }

    public void setRn(Random rn) {
        this.rn = rn;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}