package jgam.util;

import java.awt.Component;

/**
 * This is only the usual ProgressMonitor 
 *
 *Feature:
 *  you can read the current progress value too!
 *  
 * @author Mattias Ulbrich
 */
public class ProgressMonitor extends javax.swing.ProgressMonitor {

    public ProgressMonitor(Component parentComponent, Object message,
            String note, int min, int max) {
        super(parentComponent, message, note, min, max);
    }
    
    private int progress;
    
    public void setProgress(int val) {
        super.setProgress(val);
        progress = val;
    }
    
    public int getProgress() {
        return progress;
    }

}
