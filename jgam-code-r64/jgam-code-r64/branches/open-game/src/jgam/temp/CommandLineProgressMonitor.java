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

import java.io.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class CommandLineProgressMonitor {

    private int min;
    private int max;
    int dots = 0;
    private int cur;

    private boolean running = false;

    public CommandLineProgressMonitor(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void start() {
        System.out.println("[        10%       20%       30%       40%       50%       60%       70%       80%       90%         ]");
        System.out.print("[");
        running = true;
        dots = 0;
        setProgress(cur);
    }

    public void stop() {
        System.out.println("]");
        running = false;
    }

    public void setProgress(int newval) {
        this.cur = newval;

        if (!running) {
            return;
        }

        int percent;

        if (cur >= max) {
            percent = 100;
        } else {
            percent = (cur - min) * 100 / (max - min);
        }

        for (int i = dots; i < percent; i++) {
            System.out.print(".");
        }

        if (percent == 100) {
            stop();
        }

        dots = percent;
    }

    public static void main(String[] args) throws InterruptedException {
        CommandLineProgressMonitor mon = new CommandLineProgressMonitor(60, 200);

        mon.setProgress(80);
        mon.start();
        Thread.sleep(1000);
        for (int i = 45; i <= 200; i++) {
            mon.setProgress(i);
            if (i % 50 == 0) {
                mon.stop();
            }
            if (i % 50 == 20) {
                mon.start();
            }
            Thread.sleep(50);
        }
    }
}
