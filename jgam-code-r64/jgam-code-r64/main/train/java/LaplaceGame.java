import java.util.HashMap;
import java.util.Map;


public class LaplaceGame {

    static int L = 20;
    
    static final Double THROW = new Double(1);
    static final Double DOUBLE1 = new Double(2);
    static final Double DROPPED_DOUBLE1 = new Double(3);
    static final Double DOUBLE2 = new Double(4);
    static final Double DROPPED_DOUBLE2 = new Double(5);
    
    static Map<String, Double> cache = new HashMap<String, Double>();
    
    static double decision(int n, int k, int cubeOwner) {
        String key = "dec ev " + n + " " + k + " " + cubeOwner;
        if(cache.containsKey(key)) {
            return cache.get(key);
        }
        
        eval(n, k, cubeOwner);
        
        return cache.get(key);
    }
    
    static double eval(int n, int k, int cubeOwner) {

        String key = "ev " + n + " " + k + " " + cubeOwner;
        if(cache.containsKey(key)) {
            return cache.get(key);
        }
        
        if(k > L || n-k > L) {
            throw new RuntimeException(key);
        }
        
        if(k == L) {
            cache.put(key, 1.);
            return 1.;
        }
        
        if(n-k == L) {
            cache.put(key, -1.);
            return -1.;
        }
        
        double result = .5 * (eval(n + 1, k, cubeOwner) + eval(n + 1, k + 1,
                cubeOwner));
        Double decision = THROW;
        
        if(cubeOwner == -1 || cubeOwner == 0) {
            // actually "2 * .5 * ..."
            double doubled = eval(n + 1, k, 1)
                    + eval(n + 1, k + 1, 1);
            if(doubled >= result) {
                if(doubled > 1.) {
                    decision = DROPPED_DOUBLE1;
                    result = 1.; 
                } else {
                    decision = DOUBLE1;
                    result = doubled;
                }
            }
        }
        
        if(cubeOwner == -1 || cubeOwner == 1) {
            // actually "2 * .5 * ..."
            double doubled = eval(n + 1, k, 0)
                    + eval(n + 1, k + 1, 0);
            if(doubled <= result) {
                if(doubled < -1.) {
                    decision = DROPPED_DOUBLE2;
                    result = -1.; 
                } else {
                    decision = DOUBLE2;
                    result = doubled;
                }
            }
        }

        cache.put(key, result);
        cache.put("dec " + key, decision);
        
        return result;
    }
    
    public static void main(String[] args) {
        run(-1);
    }

    private static void run(int cube) {
        for (int k = 0; k <= L; k++) {
            System.out.printf("%3d: ", k);
            for (int n = 0; n < k; n++) {
                System.out.print("      /      /    ");
            }
            for (int n = k; n <= k+L; n++) {
                System.out.printf("%6.3f/%6.3f/%s  ",
                        eval(n, k, 100),
                        eval(n, k, cube), 
                        toChar(cache.get("dec ev " + n + " " + k + " " + cube), n));
                
            }
            System.out.println();
        }
        
        System.out.println(eval(2,1,1));
        System.out.println(eval(2,1,0));
        System.out.println(eval(2,1,-1));
        System.out.println(eval(2,2,1));
    }

    private static String toChar(Double d, int n) {
        if(d == DOUBLE1) {
            return "t1";
        }
        
        if(d == DROPPED_DOUBLE1) {
            return "d1";
        }
        
        if(d == DOUBLE2) {
            return "t2";
        }
        
        if(d == DROPPED_DOUBLE2) {
            return "d2";
        }
        
        return "  ";
    }

}
