package main;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by zeulb on 3/19/16.
 */
public class GameEvaluator {
    public static final int CHECK_STATUS = 10000;

    public static void main(String[] args) {
        int play = 10;
        int totalRows = 0;
        int maxRows = Integer.MIN_VALUE;
        int minRows = Integer.MAX_VALUE;
        ArrayList<Integer> rows = new ArrayList<>();
        for(int i = 0; i < play; i++) {
            State s = new State();
            EMPlayer p = new EMPlayer();
            while (!s.hasLost()) {
                if (s.getTurnNumber()%CHECK_STATUS == 0) {
                    System.out.println("Game " + (i+1) + "-> Turn " + s.getTurnNumber() + ": " + s.getRowsCleared() + " rows cleared.");
                }
                s.makeMove(p.pickMove(s));
            }
            int rowsCleared = s.getRowsCleared();
            totalRows += rowsCleared;
            maxRows = Math.max(maxRows, rowsCleared);
            minRows = Math.min(minRows, rowsCleared);
            rows.add(rowsCleared);
            System.out.println("Game " + (i+1) + "-> Rows cleared: " + rowsCleared);
        }
        Collections.sort(rows);
        System.out.println("--------------------------------------");
        System.out.println("Total Games played: " + play);
        System.out.println("Average rows cleared : " + 1.0 * totalRows / play);
        System.out.println("Maximum rows cleared : " + maxRows);
        System.out.println("Minimum rows cleared : " + minRows);
        System.out.println("Median rows cleared : " + rows.get(play/2));
    }

}