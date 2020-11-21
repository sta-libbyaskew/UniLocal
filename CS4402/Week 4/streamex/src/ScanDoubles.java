import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ScanDoubles {
    public static void main(String[] args) {
        Scanner s = null;
        double sum = 0;
        try {
            s = new Scanner(new File("src/numbers.txt"));
            while (s.hasNext()) {
                // we could instead get the next string s.next() and then
                // decide whether that string is a character op or a double
                if (s.hasNextDouble()) {
                    double d = s.nextDouble();
                    sum = sum + d;
                } else {
                    s.next(); // not a double - skip it
                }
            }
            System.out.println("The sum of all those doubles is " + sum);
            s.close();
        } catch (FileNotFoundException fnf) {
            System.err.println(fnf.getMessage());
        }
    }
}
