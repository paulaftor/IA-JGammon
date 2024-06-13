public class BoundedDoubleWindow {

    private double[] data;
    private boolean full;
    private int pos;

    public BoundedDoubleWindow(int size) {
        this.data = new double[size];
    }

    public synchronized void push(double value) {
        data[pos] = value;
        pos++;
        if(pos == data.length) {
            pos = 0;
            full = true;
        }
    }

    public int size() {
        if (full) {
            return data.length;
        } else {
            return pos;
        }
    }

    public double mean() {
        return sum()/size();
    }

    public double sqrmean() {
        return sqrsum()/size();
    }

    private double sqrsum() {
        int upper = full ? data.length : pos;
        double sum = 0.;
        for (int i = 0; i < upper; i++) {
            sum += data[i] * data[i];
        }
        return sum;
    }

    private double sum() {
        int upper = full ? data.length : pos;
        double sum = 0.;
        for (int i = 0; i < upper; i++) {
            sum += data[i];
        }
        return sum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BoundedDoubleWindow[");
        for (int i = 0; i < size(); i++) {
            sb.append(data[i]).append(i == size()-1 ? "]" :", ");
        }
        return sb.toString();
    }
}
