package util;

/**
 * This class can be used to maintain a fixed size list of double values. Adding
 * new values to the list removes the oldest entries from the list.
 * 
 * It supports operations such as average value.
 * 
 * @author mattias
 * 
 */
public class AverageBuffer {

    private int start;
    private int level;
    private final double[] buffer;

    public AverageBuffer(int size) {
        this.buffer = new double[size];
    }
    
    public void add(double d) {
        if(level < buffer.length) {
            buffer[level] = d;
            level++;
        } else {
            buffer[start] = d;
            start = (start + 1) % buffer.length;
        }
    }
    
    public double average() {
        if(level == 0)
            return Double.NaN;
        
        double sum = 0.;
        for (int i = 0; i < level; i++) {
            sum += buffer[i];
        }
        return sum / level;
    }
    
    public double sqAverage() {
        if(level == 0)
            return Double.NaN;
        
        double sum = 0.;
        for (int i = 0; i < level; i++) {
            sum += buffer[i] * buffer[i];
        }
        return sum / level;
    }
    
    public double absAverage() {
        if(level == 0)
            return Double.NaN;
        
        double sum = 0.;
        for (int i = 0; i < level; i++) {
            sum += Math.abs(buffer[i]);
        }
        return sum / level;
    }
    
    @Override
    public String toString() {
        return "[#" + level + " av=" + average() + " sqav=" + sqAverage()
                + " absav=" + absAverage() + "]";
    }

    public void printAtZero() {
        if(start == 1) {
            System.out.println(toString());
        }
    }
}
