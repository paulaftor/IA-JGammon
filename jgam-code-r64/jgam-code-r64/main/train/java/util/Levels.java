package util;


public class Levels {

    private int[] array;
    private int pos;
    
    private final int printLevel;

    public Levels(int size, int printLevel) {
        this.printLevel = printLevel;
        this.array = new int[size];
        this.pos = -1;
    }
    
    public void push(int t) {
        pos ++;
        array[pos] = t;
        if(pos < printLevel)
            System.err.println(this);
    }
    
    public void dec() {
        array[pos]--;
        assert array[pos] >= 0;
        if(pos < printLevel)
            System.err.println(this);

    }
    
    public void pop() {
        pos --;
        if(pos < printLevel)
            System.err.println(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i <= pos; i++) {
            if(i > 0) sb.append(", ");
            sb .append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
    
}
