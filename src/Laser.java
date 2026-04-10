

public class Laser
{
    private int[][] laserLifeMap = new int[Maze.getMapRowsNum()][Maze.getMapColumnsNum()];
    private boolean isLaserSpreading = false;
    private double startLX, startLY, targetLX, targetLY;
    private double currentStep, totalSteps;
    private Game game;

    public Laser(Game game) {
        this.game = game;
    }

    public void fire(int x1, int y1, int x2, int y2) {
        if (!isLaserSpreading && game.getPlayer().getLaserCount() > 0) {
            this.startLX = x1;
            this.startLY = y1;
            this.targetLX = x2;
            this.targetLY = y2;
            this.totalSteps = Math.max(Math.abs(targetLX - startLX), Math.abs(targetLY - startLY));

            if (totalSteps > 0) {
                isLaserSpreading = true;
                currentStep = 0;
                game.getPlayer().setLaserCount(game.getPlayer().getLaserCount() - 1);
            }
        }
    }

    public void update() {
        if (isLaserSpreading) {
            currentStep++;
            double ratio = currentStep / totalSteps;
            int curX = (int) Math.round(startLX + ratio * (targetLX - startLX));
            int curY = (int) Math.round(startLY + ratio * (targetLY - startLY));

            if (currentStep >= totalSteps) {
                isLaserSpreading = false;
            } else if (curY >= 0 && curY < Maze.getMapRowsNum() && curX >= 0 && curX < Maze.getMapColumnsNum()) {
                // Laser can go through any object
                laserLifeMap[curY][curX] = 100; // 100 timeunit life
            }
        }
        // Lifetime reduction and neighbor Damage Control
        for (int i = 0; i < Maze.getMapRowsNum(); i++) {
            for (int j = 0; j < Maze.getMapColumnsNum(); j++) {
                if (laserLifeMap[i][j] > 0) {
                    laserLifeMap[i][j]--;

                    // XRobot harm control
                    for (int k = 0; k < game.getXRobotCount(); k++) {
                        XRobot r = game.getXRobots()[k];
                        if ((Math.abs(r.getX() - j) == 1 && r.getY() == i) || (Math.abs(r.getY() - i) == 1 && r.getX() == j)) {
                            r.setHealth(r.getHealth() - 50);
                        }
                    }

                    // CRobot harm control
                    for (int k = 0; k < game.getCRobotCount(); k++) {
                        CRobot c = game.getCRobots()[k];
                        if ((Math.abs(c.getX() - j) == 1 && c.getY() == i) || (Math.abs(c.getY() - i) == 1 && c.getX() == j)) {
                            c.setHealth(c.getHealth() - 50);
                        }
                    }
                }
            }
        }
    }

    public void draw() {
        for (int i = 0; i < Maze.getMapRowsNum(); i++) {
            for (int j = 0; j < Maze.getMapColumnsNum(); j++) {
                if (laserLifeMap[i][j] > 0) {
                    if (game.getMaze().getMap()[i][j] == ' ') {
                        // due to pass walls
                        if (game.getElementAt(j, i) == ' ') {
                            Game.getCn().getTextWindow().output(j, i, '+', Game.getBlueColorText());
                        }
                    }
                } else if (laserLifeMap[i][j] == 0 && game.getMaze().getMap()[i][j] == ' ') {
                    if (game.getElementAt(j, i) == ' ') {
                        Game.getCn().getTextWindow().output(j, i, ' ');
                    }
                }
            }
        }
    }

    public int[][] getLaserLifeMap() {
        return laserLifeMap;
    }

    public void setLaserLifeMap(int[][] laserLifeMap) {
        this.laserLifeMap = laserLifeMap;
    }

    public boolean isLaserSpreading() {
        return isLaserSpreading;
    }

    public void setLaserSpreading(boolean laserSpreading) {
        this.isLaserSpreading = laserSpreading;
    }

    public double getStartLX() {
        return startLX;
    }

    public void setStartLX(double startLX) {
        this.startLX = startLX;
    }

    public double getStartLY() {
        return startLY;
    }

    public void setStartLY(double startLY) {
        this.startLY = startLY;
    }

    public double getTargetLX() {
        return targetLX;
    }

    public void setTargetLX(double targetLX) {
        this.targetLX = targetLX;
    }

    public double getTargetLY() {
        return targetLY;
    }

    public void setTargetLY(double targetLY) {
        this.targetLY = targetLY;
    }

    public double getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(double currentStep) {
        this.currentStep = currentStep;
    }

    public double getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(double totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}