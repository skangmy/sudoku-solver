package com.optimapp;

import net.sf.javailp.*;

import java.util.Arrays;

public class Main {
    public static String getVar(int row, int col, int value) {
        return row + "_" + col + "_" + value;
    }

    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        int[][] puzzle = new int[9][9];
        puzzle[0] = new int[]{5, -1, -1, 4, -1, -1, -1, -1, 1};
        puzzle[1] = new int[]{-1, 2, -1, -1, 3, -1, -1, 9, -1};
        puzzle[2] = new int[]{-1, -1, 8, -1, -1, 7, 6, -1, -1};
        puzzle[3] = new int[]{4, -1, -1, 9, -1, -1, 5, -1, -1};
        puzzle[4] = new int[]{-1, 1, -1, -1, -1, 2, -1, 7, -1};
        puzzle[5] = new int[]{-1, -1, 3, -1, 6, -1, -1, -1, -1};
        puzzle[6] = new int[]{6, -1, -1, 1, -1, -1, -1, -1, -1};
        puzzle[7] = new int[]{-1, -1, -1, -1, 5, -1, -1, 2, -1};
        puzzle[8] = new int[]{-1, 7, -1, -1, -1, 8, -1, -1, -1};

        SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//        factory.setParameter(Solver.VERBOSE,0);
        factory.setParameter(Solver.TIMEOUT,60); // set timeout to 100 seconds

        Problem problem = new Problem();

        // create variable for each cell and each possible value
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 1; k <= 9; k++) {
                    problem.setVarType(getVar(i, j, k), VarType.BOOL);

                    // fix value
                    if (puzzle[i][j] != -1) {
                        if (puzzle[i][j] == k) {
                            problem.setVarLowerBound(getVar(i, j, k), 1);
                        } else {
                            problem.setVarUpperBound(getVar(i, j, k), 0);
                        }
                    }
                }
            }
        }

        // Rule 1: each cell only one value
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Linear linear = new Linear();
                for (int k = 1; k <= 9; k++) {
                    linear.add(1, getVar(i, j, k));
                }
                problem.add(linear, "=", 1);
            }
        }

        // Rule 2: no duplicate value in each 3x3 block
        for (int blockx = 0; blockx < 9; blockx += 3) {
            for (int blocky = 0; blocky < 9; blocky += 3) {
                for (int k = 1; k <= 9; k++) {
                    Linear linear = new Linear();
                    for (int i = blockx; i < blockx + 3; i++) {
                        for (int j = blocky; j < blocky + 3; j++) {
                            linear.add(1, getVar(i, j, k));
                        }
                    }
                    problem.add(linear, "=", 1);
                }
            }
        }

        // Rule 3: no duplicate value in each row
        for (int k = 1; k <= 9; k++) {
            for (int i = 0; i < 9; i++) {
                Linear linear = new Linear();
                for (int j = 0; j < 9; j++) {
                    linear.add(1, getVar(i, j, k));
                }
                problem.add(linear, "=", 1);
            }

        }

        // Rule 4: no duplicate value in each column
        for (int k = 1; k <= 9; k++) {
            for (int j = 0; j < 9; j++) {
                Linear linear = new Linear();
                for (int i = 0; i < 9; i++) {
                    linear.add(1, getVar(i, j, k));
                }
                problem.add(linear, "=", 1);
            }
        }

        // Goal?
        // no goal - solve comply to all rules!
        Linear goal = new Linear();
        int delta = 1;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 1; k <= 9; k++) {
                    goal.add(delta, getVar(i, j, k));
                    delta += 0.001;
                }
            }
        }
        problem.setObjective(goal, OptType.MAX);

        Solver solver = factory.get(); // you should use this solver only once for one problem
        System.out.println("Running solver...");
        Result result = solver.solve(problem);

        if (result != null) {
            System.out.println("Feasible! Goal: " + result.getObjective());

            // apply result
            int[][] solution = new int[9][9];
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    for (int k = 1; k <= 9; k++) {
                        if (result.get(getVar(i, j, k)).intValue() == 1) {
                            solution[i][j] = k;
                        }
                    }
                }
            }
            System.out.println("Solution");
            System.out.println("========");
            for (int i = 0; i < 9; i++) {
                System.out.println(Arrays.toString(solution[i]));
            }

        } else {
            System.out.println("Infeasible!");
        }
    }
}
