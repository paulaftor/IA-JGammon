
public class SingleCheckerModel {

    public static double[] data = new double[151];
    
    public static void main(String[] args) {
        calc(data.length-1);
        for (int i = 0; i < data.length; i++) {
            System.out.println(i + " " + data[i]);
        }
        
    }

    private static double calc(int p) {
        if(p <= 0) {
            return 0.0;
        }
        
        if(data[p] != 0.)
            return data[p];
        
        for (int i = 1; i <= 6; i++) {
            for (int j = i+1; j <= 6; j++) {
                data[p] += 2*(calc(p-i-j)+1);
            }
        }
        
        for (int i = 1; i <= 6; i++) {
            data[p] += calc(p-4*i)+1;
        }
        
        data[p] = data[p] / 36.;
        
        return data[p];
    }
    
}
