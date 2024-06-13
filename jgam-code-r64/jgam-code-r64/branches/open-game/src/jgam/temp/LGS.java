/*
 * JGammon: A backgammon client written in Java
 * Copyright (C) 2005/06 Mattias Ulbrich
 *
 * JGammon includes: - playing over network
 *                   - plugin mechanism for graphical board implementations
 *                   - artificial intelligence player
 *                   - plugin mechanism for AI players
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */



package jgam.temp;

/**
 * LGS is German for linear equation system.
 *
 * Do LR-splitting and solve the easier problems then.
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class LGS {
    private LGS() {
    }

    public static class SingularException extends Exception {};

    /**
     * solve the equation A x = b.
     *
     * A must be square n*n and b of length n.
     *
     * The LGS must have a solution for this to work. Singular matrices
     * result in an exception.
     *
     * If there are more than one solution, choose one!
     *
     * @param A square matrix n*n
     * @param b A vector of length n
     * @return the solution
     * @throws jgam.ai.util.LGS.SingularException if the matrix is singular
     */
    public static double[] solve(double[][] A, double[] b) throws SingularException {

        int n = A.length;
        double B[][] = new double[n][n + 1];

        outMatrix(A);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                B[i][j] = A[i][j];
            }
            B[i][n] = b[i];
        }

        for (int i = 0; i < n; i++) {
            forwardElim(B, i);
        }

        outMatrix(B);

        return backSubs(B);
    }

    /**
     * backSubs
     *
     * @param B double[][]
     * @return double[]
     */
    private static double[] backSubs(double[][] M) throws SingularException {

        int n = M.length;

        double[] x = new double[n];

        for (int i = n - 1; i >= 0; i--) {
            x[i] = M[i][n];
            for (int j = i + 1; j < n; j++) {
                x[i] -= x[j] * M[i][j];
            }
            if (M[i][i] == 0) {
                if (x[i] != 0) {
                    throw new SingularException();
                }
            } else {
                x[i] = x[i] / M[i][i];
            }
        }

        return x;
    }

    /**
     * forwardElim
     *
     * @param B double[][]
     * @param i int
     */
    private static void forwardElim(double[][] B, int row) {
        int n = B.length;
        int maxrow = -1;

        for (int i = row; i < n; i++) {
            if (maxrow == -1 || B[i][row] > B[maxrow][row]) {
                maxrow = i;
            }
        }

        swapRow(B, row, maxrow);

        double pivot = B[row][row];
        if (pivot == 0) {
            return;
        }

        pivot = 1. / pivot;
        for (int i = row + 1; i < n; i++) {
            double rowpivot = B[i][row] * pivot;
            for (int j = row; j <= n; j++) {
                B[i][j] -= rowpivot * B[row][j];
            }

        }
    }

    /**
     * swapRow
     *
     * @param row int
     * @param maxrow int
     */
    private static void swapRow(double[][] M, int row1, int row2) {
        for (int i = 0; i < M[row1].length; i++) {
            double v = M[row1][i];
            M[row1][i] = M[row2][i];
            M[row2][i] = v;
        }
    }

    /**
     * print out a matrix
     *
     * @param LR double[][]
     */
    public static void outMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("[");
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(" " + matrix[i][j]);
            }
            System.out.println(" ]");
        }
    }

    public static void outVector(double[] vector) {
        System.out.print("[");
        for (int j = 0; j < vector.length; j++) {
            System.out.print(" " + vector[j]);
        }
        System.out.println(" ]");

    }

    public static void main(String[] args) throws SingularException {
        double A[][] = { {1, 2, 3}, {4, 5, 6}, {7, 8, 10}
        };
        double b[] = {5, 14, 24};
        outVector(solve(A, b));
    }

}
