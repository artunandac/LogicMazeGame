

import enigma.console.TextAttributes;
import java.awt.Color;

public class Main {

    public static void main(String[] args) throws Exception {
        showWelcomeScreen();

        boolean keepPlaying = true;
        while (keepPlaying) {
            int[] selections = showMenu(); // [0]: Maze Choice, [1]: Mode Choice (0=Classic, 1=Hard)

            if (selections[0] == 3) {
                keepPlaying = false;
                System.exit(0);
            }

            int mazeChoice = selections[0];
            boolean isHardMode = (selections[1] == 1);

            if (mazeChoice == 1) {
                clearScreen();
                Game.getCn().getTextWindow().setCursorPosition(20, 10);
                Game.getCn().getTextWindow().output("Generating maze ...");
                Thread.sleep(1500);
                Game myGame = new Game(1, isHardMode);
            } else if (mazeChoice == 2) {
                clearScreen();
                Game.getCn().getTextWindow().setCursorPosition(20, 10);
                Game.getCn().getTextWindow().output("Loading custom maze from file...");
                Thread.sleep(1500);
                Game myGame = new Game(2, isHardMode);
            }
        }
    }

    private static void showWelcomeScreen() throws Exception {
        String[] title = {
                "::::::::::: :::       ::: ::::::::::: ::::    :::  :::::::: ",
                "    :+:     :+:       :+:     :+:     :+:+:   :+: :+:    :+:",
                "    +:+     +:+       +:+     +:+     :+:+:+  +:+ +:+       ",
                "    +#+     +#+  +:+  +#+     +#+     +#+ +:+ +#+ +#++:++#++",
                "    +#+     +#+ +#+#+ +#+     +#+     +#+  +#+#+#        +#+",
                "    #+#      #+#+# #+#+#      #+#     #+#   #+#+# #+#    #+#",
                "    ###       ###   ###   ########### ###    ####  ######## ",
                "                                                            ",
                "       ::::::::      :::     ::::    ::::  ::::::::::       ",
                "      :+:    :+:   :+: :+:   +:+:+: :+:+:+ :+:              ",
                "      +:+         +:+   +:+  +:+ +:+:+ +:+ +:+              ",
                "      :#:        +#++:++#++: +#+  +:+  +#+ +#++:++#         ",
                "      +#+   +#+# +#+     +#+ +#+       +#+ +#+              ",
                "      #+#    #+# #+#     #+# #+#       #+# #+#              ",
                "       ########  ###     ### ###       ### ##########       "
        };

        TextAttributes colorHash = new TextAttributes(Color.RED, Color.BLACK);
        TextAttributes colorPlus = new TextAttributes(Color.GREEN, Color.BLACK);
        TextAttributes colorColon = new TextAttributes(Color.YELLOW, Color.BLACK);
        TextAttributes colorDefault = new TextAttributes(Color.WHITE, Color.BLACK);

        clearScreen();

        int startX = 10;
        int startY = 3;

        for (int i = 0; i < title.length; i++) {
            String row = title[i];
            for (int j = 0; j < row.length(); j++) {
                char c = row.charAt(j);
                TextAttributes currentColor = colorDefault;

                if (c == '#') currentColor = colorHash;
                else if (c == '+') currentColor = colorPlus;
                else if (c == ':') currentColor = colorColon;

                Game.getCn().getTextWindow().output(startX + j, startY + i, c, currentColor);
            }
            Thread.sleep(70);
        }

        Game.getCn().getTextWindow().setCursorPosition(28, startY + title.length + 2);
        Game.getCn().getTextWindow().output("Press ENTER to start...", colorDefault);
        Game.getCn().readLine();
    }

    private static int[] showMenu() throws Exception {
        while (true) {
            clearScreen();

            int startX = 30;
            int startY = 8;

            Game.getCn().getTextWindow().setCursorPosition(startX, startY);
            Game.getCn().getTextWindow().output("=== MENU ===");

            Game.getCn().getTextWindow().setCursorPosition(startX - 2, startY + 3);
            Game.getCn().getTextWindow().output("[ 1 ] START TWINS GAME");

            Game.getCn().getTextWindow().setCursorPosition(startX - 2, startY + 5);
            Game.getCn().getTextWindow().output("[ 2 ] HOW TO PLAY THE GAME?");

            Game.getCn().getTextWindow().setCursorPosition(startX - 2, startY + 7);
            Game.getCn().getTextWindow().output("[ 3 ] EXIT");

            Game.getCn().getTextWindow().setCursorPosition(startX - 2, startY + 10);
            Game.getCn().getTextWindow().output("Please select an option (1-2-3): ");

            String choice = Game.getCn().readLine();

            if (choice.equals("1")) {
                int selectedMode = 0; // 0: Classic, 1: Hard

                // --- MODE SELECTION ---
                while (true) {
                    clearScreen();
                    int mX = 28;
                    int mY = 8;

                    Game.getCn().getTextWindow().setCursorPosition(mX, mY);
                    Game.getCn().getTextWindow().output("=== MODE SELECTION ===");

                    Game.getCn().getTextWindow().setCursorPosition(mX - 2, mY + 3);
                    Game.getCn().getTextWindow().output("[ 1 ] CLASSIC MODE (Original)");

                    Game.getCn().getTextWindow().setCursorPosition(mX - 2, mY + 5);
                    Game.getCn().getTextWindow().output("[ 2 ] HARD MODE (With Fog And Healing Potions)");

                    Game.getCn().getTextWindow().setCursorPosition(mX - 2, mY + 8);
                    Game.getCn().getTextWindow().output("Select difficulty (1-2): ");

                    String modeChoice = Game.getCn().readLine();
                    if (modeChoice.equals("1")) {
                        selectedMode = 0;
                        break;
                    } else if (modeChoice.equals("2")) {
                        selectedMode = 1;
                        break;
                    }
                }

                // --- MAZE SELECTION ---
                while (true) {
                    clearScreen();
                    int mX = 28;
                    int mY = 8;

                    Game.getCn().getTextWindow().setCursorPosition(mX, mY);
                    Game.getCn().getTextWindow().output("=== MAZE SELECTION ===");

                    Game.getCn().getTextWindow().setCursorPosition(mX - 2, mY + 3);
                    Game.getCn().getTextWindow().output("[ 1 ] PLAY DEFAULT MAZE");

                    Game.getCn().getTextWindow().setCursorPosition(mX - 2, mY + 5);
                    Game.getCn().getTextWindow().output("[ 2 ] LOAD A MAZE");

                    Game.getCn().getTextWindow().setCursorPosition(mX - 2, mY + 8);
                    Game.getCn().getTextWindow().output("Select maze type (1-2): ");

                    String mazeChoice = Game.getCn().readLine();

                    if (mazeChoice.equals("1")) {
                        return new int[]{1, selectedMode};
                    } else if (mazeChoice.equals("2")) {
                        return new int[]{2, selectedMode};
                    }
                }
            } else if (choice.equals("2")) {
                clearScreen();
                Game.getCn().getTextWindow().setCursorPosition(25, 8);
                Game.getCn().getTextWindow().output("--- HOW TO PLAY THE GAME? ---");

                Game.getCn().getTextWindow().setCursorPosition(15, 10);
                Game.getCn().getTextWindow().output("* Use arrow keys to move characters A and B.");

                Game.getCn().getTextWindow().setCursorPosition(15, 12);
                Game.getCn().getTextWindow().output("* Press 'R' to toggle Same/Opposite direction modes.");

                Game.getCn().getTextWindow().setCursorPosition(15, 14);
                Game.getCn().getTextWindow().output("* Press SPACE to shoot lasers using your '@' package laser.");

                Game.getCn().getTextWindow().setCursorPosition(20, 18);
                Game.getCn().getTextWindow().output("Press ENTER to return to the menu...");
                Game.getCn().readLine();
            } else if (choice.equals("3")) {
                return new int[]{3, 0}; // exit
            }
        }
    }

    private static void clearScreen() {
        for (int y = 0; y < 25; y++) {
            for (int x = 0; x < 80; x++) {
                Game.getCn().getTextWindow().setCursorPosition(x, y);
                Game.getCn().getTextWindow().output(' ');
            }
        }
    }
}