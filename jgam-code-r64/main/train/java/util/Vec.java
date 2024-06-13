package util;

import java.util.Arrays;
import java.util.function.BiFunction;

public class Vec {

    private final double[] values;

    public Vec(double... values) {
        this.values = values;
    }

    public double[] toArray() {
        return values;
    }

    public double get(int i) {
        return values[i];
    }

    public Vec sub(Vec v) {
        return componentWise(v, (x,y) -> (x - y));
    }

    public double sqrSum() {
        double result = 0.;
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            result += value * value;
        }
        return result;
    }

    public  interface DoubleBiFun {
        double apply(double a, double b);
    }

    public interface DoubleFun {
        double apply(double a);

    }

    public Vec componentWise(Vec v, DoubleBiFun f) {
        if(values.length != v.values.length)
            throw new IllegalArgumentException();
        double[] result = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = f.apply(values[i], v.values[i]);
        }
        return new Vec(result);
    }

    public Vec map(DoubleFun f) {
        double[] result = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = f.apply(values[i]);
        }
        return new Vec(result);
    }

    @Override
    public String toString() {
        return "[" + Arrays.toString(values) + ']';
    }

    public String toString(String format) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            result.append(i == 0 ? "[" : ", ");
            result.append(String.format(format, value));
        }
        return result.append("]").toString();
    }
}
