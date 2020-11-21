import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CopyLines {
    public static void main(String[] args) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            br = new BufferedReader(new FileReader(args[0]));
            bw = new BufferedWriter(new FileWriter(args[1]));
            pw = new PrintWriter(bw);

            String line;
            while ((line = br.readLine()) != null) {
                pw.println(line); // copy one line at a time
            }
            br.close();
            bw.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (ArrayIndexOutOfBoundsException aob) {
            System.err.println("You must pass 2 command-line arguments to program: <infile> <outfile>");
        }
    }
}
