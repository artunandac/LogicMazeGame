

import java.util.Random;
import java.io.InputStream;
import java.util.Scanner;

public class Maze {
    private static final int mapRowsNum = 23; //23
    private static final int mapColumnsNum = 53; //53
    private char[][] map = new char[mapRowsNum][mapColumnsNum];
    private char walltexture = '#'; //changing this will result in a different character used in walls

    public Maze() {
        boolean loadMaze = false;

        if(loadMaze) {
            loadFromFile();
        }
        else {
            do {
                initMaze();
                addWalls(4, 8); //4
                addWalls(6, 6); //6
                addWalls(20, 4); //20
                addWalls(5, 3); //5
            } while(!checkConnected());
        }
    }

    public void drawMaze() {
        for (int i = 0; i <map.length; i++) {
            for (int j = 0; j<map[i].length; j++) {
                if (map[i][j]==getWalltexture())
                    Game.getCn().getTextWindow().output(j, i,getWalltexture());
            }
        }
    }

    private void initMaze() {
        for(int i = 0; i < mapRowsNum; i++) {
            map[i][0] = walltexture;
            map[i][mapColumnsNum - 1] = walltexture;
        }

        for(int i = 0; i < mapColumnsNum; i++) {
            map[0][i] = walltexture;
            map[mapRowsNum - 1][i] = walltexture;
        }

        for(int i = 1; i < mapRowsNum - 1; i++) {
            for(int y = 1; y < mapColumnsNum - 1; y++) {
                map[i][y] = ' ';
            }
        }
    }

    private void addWalls(int wallNumber, int wallLength) {
        Random random = new Random();
        int wallCount = 0;

        while(wallCount != wallNumber) {
            boolean isPlaceAvailable = true;
            int wallX = random.nextInt(1, mapColumnsNum - 1);
            int wallY = random.nextInt(1, mapRowsNum - 1);
            int wallDirection = random.nextInt(1, 5);
            int lengthCount = 0;

            for(int i = 0; i < wallLength; i++) {
                if(wallDirection == 1) {
                    if(wallLength > wallY) {
                        isPlaceAvailable = false;
                        break;
                    }
                    if(map[wallY - lengthCount][wallX] != ' ') {
                        isPlaceAvailable = false;
                        break;
                    }
                    else {
                        lengthCount++;
                    }
                }
                else if(wallDirection == 2) {
                    if(wallLength + wallX > mapColumnsNum - 1) {
                        isPlaceAvailable = false;
                        break;
                    }
                    if(map[wallY][wallX + lengthCount] != ' ') {
                        isPlaceAvailable = false;
                        break;
                    }
                    else {
                        lengthCount++;
                    }
                }
                else if(wallDirection == 3) {
                    if(wallLength + wallY > mapRowsNum - 1) {
                        isPlaceAvailable = false;
                        break;
                    }
                    if(map[wallY + lengthCount][wallX] != ' ') {
                        isPlaceAvailable = false;
                        break;
                    }
                    else {
                        lengthCount++;
                    }
                }
                else {
                    if(wallLength > wallX) {
                        isPlaceAvailable = false;
                        break;
                    }
                    if(map[wallY][wallX - lengthCount] != ' ') {
                        isPlaceAvailable = false;
                        break;
                    }
                    else {
                        lengthCount++;
                    }
                }
            }

            if(!isPlaceAvailable) continue;
            else {
                for(int i = 0; i < wallLength; i ++) {
                    if(wallDirection == 1) map[wallY - i][wallX] = walltexture;
                    else if(wallDirection == 2) map[wallY][wallX + i] = walltexture;
                    else if(wallDirection == 3) map[wallY + i][wallX] = walltexture;
                    else map[wallY][wallX - i] = walltexture;
                }
                wallCount++;

                if(!(checkArea(2, 2, 3) == true && checkArea(3, 3, 5) == true && checkArea(4, 4, 7) == true && checkArea(6, 6, 15) == true)) {
                    for(int i = 0; i < wallLength; i ++) {
                        if(wallDirection == 1) map[wallY - i][wallX] = ' ';
                        else if(wallDirection == 2) map[wallY][wallX + i] = ' ';
                        else if(wallDirection == 3) map[wallY + i][wallX] = ' ';
                        else map[wallY][wallX - i] = ' ';
                    }
                    wallCount--;
                    continue;
                }
            }
        }
    }

    private boolean checkArea(int rows, int columns, int maxWallSquares) {
        for(int i = 0; i < ((mapRowsNum) - rows); i ++) {
            for(int y = 0; y < ((mapColumnsNum) - columns); y++) {
                int squareCount = 0;

                for(int z = 0; z < rows; z++) {
                    for(int k = 0; k < columns; k++) {
                        if(map[i + z][y + k] == walltexture) squareCount++;
                    }
                }
                if(squareCount > maxWallSquares) return false;
            }
        }
        return true;
    }

    private boolean checkConnected() {
        Random random = new Random();
        int mapX;
        int mapY;

        while(true) {
            mapX = random.nextInt(1, mapColumnsNum - 1);
            mapY = random.nextInt(1, mapRowsNum - 1);
            if (map[mapY][mapX] == ' ') break;
        }

        Stack stack = new Stack((mapRowsNum - 2)*(mapColumnsNum - 2) - 163);
        int[] current = {mapY, mapX};
        stack.push(current);
        map[mapY][mapX] = '*';

        int emptySquareCount = 1;

        while(!stack.isEmpty()) {
            current = (int[])stack.pop();
            mapX = current[1];
            mapY = current[0];

            if(mapY - 1 >= 0 && map[mapY - 1][mapX] == ' ') {
                stack.push(new int[] {mapY - 1, mapX});
                map[mapY - 1][mapX] = '*';
                emptySquareCount++;
            }
            if(mapX + 1 < mapColumnsNum && map[mapY][mapX + 1] == ' ') {
                stack.push(new int[] {mapY, mapX + 1});
                map[mapY][mapX + 1] = '*';
                emptySquareCount++;
            }
            if(mapY + 1 < mapRowsNum && map[mapY + 1][mapX] == ' ') {
                stack.push(new int[] {mapY + 1, mapX});
                map[mapY + 1][mapX] = '*';
                emptySquareCount++;
            }
            if(mapX - 1 >= 0 && map[mapY][mapX - 1] == ' ') {
                stack.push(new int[] {mapY, mapX - 1});
                map[mapY][mapX - 1] = '*';
                emptySquareCount++;
            }
        }

        for(int i = 1; i < mapRowsNum - 1; i++) {
            for(int y = 1; y < mapColumnsNum - 1; y++) {
                if(map[i][y] == '*') map[i][y] = ' ';
            }
        }
        return emptySquareCount == (mapRowsNum - 2)*(mapColumnsNum - 2) - 163;
    }

    public void loadFromFile() {
        InputStream inputStream = Main.class.getResourceAsStream("maze.txt");
        Scanner scanner = new Scanner(inputStream);
        String line;
        for(int i = 0; i < 23; i++) {
            line = scanner.nextLine();
            for(int j = 0; j < 53; j++) {
                map[i][j] = line.charAt(j);
            }
        }
        scanner.close();
    }

    public static int getMapRowsNum() {
        return mapRowsNum;
    }

    public static int getMapColumnsNum() {
        return mapColumnsNum;
    }

    public char[][] getMap() {
        return map;
    }

    public void setMap(char[][] map) {
        this.map = map;
    }

    public char getWalltexture() {
        return walltexture;
    }

    public void setWalltexture(char walltexture) {
        this.walltexture = walltexture;
    }
}