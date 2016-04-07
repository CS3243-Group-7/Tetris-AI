package training;

import main.State;

import java.util.function.Function;

public class Feature {

    private final Function<int[][], Double> eval;
    private double value;

    public Feature(double defaultValue, Function<int[][], Double> eval) {
        value = defaultValue;
        this.eval = eval;
    }

    public Feature(Feature other) {
        this.value = other.getValue();
        this.eval = other.getEval();
    }

    public Function<int[][], Double> getEval() {
        return eval;
    }

    public double getScore(int[][] field) {
        return value * eval.apply(field);
    }

    public void setValue(double newValue) {
        value = newValue;
    }

    public double getValue() {
        return value;
    }

    /**
     * Common features
     */
    public static Feature getSumHeight(double defaultValue) {
        return new Feature(defaultValue, (field) -> {

            int[] maxHeight = getMaxHeight(field);

            int sumHeight = 0;

            for (int j = 0; j < State.COLS; j++) {
                for (int i = State.ROWS - 1; i >= 0; i--) {
                    if (field[i][j] > 0) {
                        sumHeight += maxHeight[j];
                        break;
                    }
                }
            }

            return (double) sumHeight;
        });
    }

    public static Feature getCompletedLines(double defaultValue) {
        return new Feature(defaultValue, (field) -> {

            int[] maxHeight = getMaxHeight(field);

            int completedLines = 0;
            int minHeight = State.ROWS + 1;
            for (int i = 0; i < maxHeight.length; i++)
                minHeight = Math.min(minHeight, maxHeight[i]);

            for (int i = 0; i < minHeight; i++) {
                boolean lineComplete = true;
                for (int j = 0; j < State.COLS; j++) {
                    if (field[i][j] == 0) {
                        lineComplete = false;
                        break;
                    }
                }

                if (lineComplete)
                    completedLines++;
            }

            return (double) completedLines;
        });
    }

    public static Feature getHoleCount(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int[] maxHeight = getMaxHeight(field);

            int holes = 0;
            for (int j = 0; j < State.COLS; j++) {
                for (int i = 0; i < maxHeight[j] - 1; i++) {
                    if (field[i][j] == 0)
                        holes++;
                }
            }

            return (double) holes;
        });
    }

    public static Feature getBumpiness(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int[] maxHeight = getMaxHeight(field);

            int bumpiness = 0;
            for (int i = 0; i + 1 < State.COLS; i++) {
                bumpiness += Math.abs(maxHeight[i + 1] - maxHeight[i]);
            }

            return (double) bumpiness;
        });
    }

    private static int[] getMaxHeight(int[][] field) {
        int[] maxHeight = new int[State.COLS];

        for (int j = 0; j < State.COLS; j++) {
            for (int i = State.ROWS - 1; i >= 0; i--) {
                if (field[i][j] > 0) {
                    maxHeight[j] = (i + 1);
                    break;
                }
                if (i == 0)
                    maxHeight[j] = 0;
            }
        }

        return maxHeight;
    }

    /**
     * LHOOOOOOOOOOOOOOO
     */

    /**
     * Return weighted filled cells. cells at row-i costs i.
     */
    public static Feature getWeightedFilledCells(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int filledCells = 0;
            ;
            for (int i = 0; i < State.ROWS; ++i) {
                for (int j = 0; j < State.COLS; ++j) {
                    if (field[i][j] != 0) {
                        filledCells += (i + 1);
                    }
                }
            }
            return (double) filledCells;
        });
    }

    /**
     *  The depth of the deepest hole (a width-1 hole with filled spots on both sides)
     */
    public static Feature getDeepestOneHole(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            for (int i = 0; i < State.ROWS; ++i) {
                for (int j = 0; j < State.COLS; ++j) {
                    int left = (j - 1 < 0 ? 1 : field[i][j-1]);
                    int right = (j + 1 >= State.COLS ? 1 : field[i][j+1]);
                    if (field[i][j] == 0 && left == 1 && right == 1) {
                        return (double) State.ROWS - i;
                    }
                }
            }
            return 0.0;
        });
    }

    /**
     *  The total depth of all the holes on the game board
     */
    public static Feature getSumOfAllHoles(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int holesSum = 0;
            int[] maxHeight = getMaxHeight(field);
            for (int j = 0; j < State.COLS; ++j) {
                for (int i = 0; i < maxHeight[j]; ++i) {
                    if (field[i][j] == 0) {
                        holesSum += (State.ROWS - i);
                    }
                }
            }
            return (double) holesSum;
        });
    }

    /**
     * Horizontal Roughness - The number of times a spot alternates between an empty and a filled status, going by rows
     */
    public static Feature getHorizontalRoughness(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int horizontalRoughness = 0;
            for (int i = 0; i < State.ROWS; ++i) {
                for (int j = 1; j < State.COLS; ++j) {
                    if (field[i][j] != field[i][j - 1]) {
                        ++horizontalRoughness;
                    }
                }
            }
            return (double) horizontalRoughness;
        });
    }

    /**
     * Vertical Roughness - The number of times a spot alternates between an empty and a filled status, going by columns
     */
    public static Feature getVerticalRoughness(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int verticalRoughness = 0;
            for (int j = 0; j < State.COLS; ++j) {
                for (int i = 1; i < State.ROWS; ++i) {
                    if (field[i][j] != field[i-1][j]) {
                        ++verticalRoughness;
                    }
                }
            }
            return (double) verticalRoughness;
        });
    }

    /**
     * The number of holes that are 3 or more blocks deep
     */
    public static Feature getWellCount(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int numberOfWells = 0;
            int[] maxHeight = getMaxHeight(field);
            for (int j = 0; j < State.COLS; ++j) {
                for (int i = 0; i + 2 < maxHeight[j]; ++i) {
                    if (field[i][j] == 0 && (i == 0 || field[i-1][j] == 1)) {
                        if (field[i+1][j] == 0 && field[i+2][j] == 0) {
                            ++numberOfWells;
                        }
                    }
                }
            }
            return (double) numberOfWells;
        });
    }

    /**
     * Weighted empty cells
     */
    public static Feature getWeightedEmptyCells(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int emptyCells = 0;
            int[] maxHeight = getMaxHeight(field);
            for (int j = 0; j < State.COLS; ++j) {
                for (int i = 0; i < maxHeight[j]; ++i) {
                    if (field[i][j] == 0) {
                        emptyCells += State.ROWS - i;
                    }
                }
            }
            return (double) emptyCells;
        });
    }

    /**
     * The height of the highest hole on the board
     */
    public static Feature getHighestHole(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int[] maxHeight = getMaxHeight(field);
            for (int i = State.ROWS - 1; i >= 0; --i) {
                for (int j = 0; j < State.COLS; ++j) {
                    if (field[i][j] == 0 && i < maxHeight[j]) {
                        return (double) i;
                    }
                }
            }
            return 0.0;
        });
    }

    /**
     * (h2 – h1) + |h2 – h3| + |h3 – h4| + |h4 – h5| + |h5 – h6| + |h6 – h7| + |h7 – h8| + |h8 – h9| + (h9 – h10)
     */
    public static Feature getSurfaceSmoothness(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int surfaceSmoothness = 0;
            int[] maxHeight = getMaxHeight(field);
            surfaceSmoothness += maxHeight[1] - maxHeight[0];
            surfaceSmoothness += maxHeight[State.COLS-1] - maxHeight[State.COLS-2];
            for (int i = 1; i < State.COLS - 2; ++i) {
                surfaceSmoothness += Math.abs(maxHeight[i] - maxHeight[i+1]);
            }
            return (double) surfaceSmoothness;
        });
    }

    /**
     * for every well of depth w, sum 1 + 2 + 3 + ... + w
     */
    public static Feature getSumSquareWells(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int sum = 0;
            int[] maxHeight = getMaxHeight(field);
            for (int j = 0; j < State.COLS; ++j) {
                int wellDepth = 0;
                for (int i = 0; i < maxHeight[j]; ++i) {
                    if (field[i][j] == 0) {
                        ++wellDepth;
                        sum += wellDepth;
                    } else {
                        wellDepth = 0;
                    }
                }
            }
            return (double) sum;
        });
    }

    public static Feature getSumWellDepths(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int sum = 0;
            for (int j = 0; j < State.COLS; ++j) {
                int wellDepth = 0;
                for (int i = 0; i < State.ROWS; ++i) {
                    if (field[i][j] == 0) {
                        ++wellDepth;
                        boolean wallLeft = (j == 0 || field[i][j-1] != 0);
                        boolean wallRight = (j == State.COLS-1 || field[i][j+1] != 0);
                        if (wallLeft && wallRight) {
                            sum += wellDepth;
                        }
                    } else {
                        wellDepth = 0;
                    }
                }
            }
            return (double) sum;
        });
    }

    /**
     * compute the lowest and highest position of new piece. compute the total
     */
    /*
    public static Feature gettetrominoHeight(double defaultValue) {
        int fieldBeforeCleared[][] = nextState.fieldBeforeCleared;
        if (nextState.lost) {
            return State.ROWS;
        }
        int minHeightPiece = Integer.MAX_VALUE;
        int maxHeightPiece = Integer.MIN_VALUE;
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (fieldBeforeCleared[i][j] == nextState.turn) {
                    minHeightPiece = Math.min(minHeightPiece,i);
                    maxHeightPiece = Math.max(maxHeightPiece,i);
                }
            }
        }
        return (float)(maxHeightPiece + minHeightPiece) / 2f;
    }
    */

    /**
     * return the landing height of the new piece
     */
    /*
    public static Feature getlandingHeight(double defaultValue) {
        int fieldBeforeCleared[][] = nextState.fieldBeforeCleared;
        if (nextState.lost) {
            return State.ROWS;
        }
        for (int i = 0; i < State.ROWS; ++i) {
            for (int j = 0; j < State.COLS; ++j) {
                if (fieldBeforeCleared[i][j] == nextState.turn) {
                    return i;
                }
            }
        }
        return 0;
    }
    */

    public static Feature getRowTransitions(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int sum = 0;
            for (int i = 0; i < State.ROWS; ++i) {
                for (int j = 0; j < State.COLS; ++j) {
                    boolean wallLeft = (j == 0 || field[i][j-1] != 0);
                    boolean wallNow = (field[i][j] != 0);
                    if (wallLeft ^ wallNow) {
                        ++sum;
                    }
                }
                if (field[i][State.COLS - 1] == 0) {
                    ++sum;
                }
            }
            return (double) sum;
        });
    }

    public static Feature getColumnTransitions(double defaultValue) {
        return new Feature(defaultValue, (field) -> {
            int sum = 0;
            for (int j = 0; j < State.COLS; ++j) {
                for (int i = 0; i < State.ROWS; ++i) {
                    boolean wallDown = (i == 0 || field[i-1][j] != 0);
                    boolean wallNow = (field[i][j] != 0);
                    if (wallDown ^ wallNow) {
                        ++sum;
                    }
                }
            }
            return (double) sum;
        });
    }
}
