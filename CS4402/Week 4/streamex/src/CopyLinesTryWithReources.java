import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CopyLinesTryWithReources {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]));
                BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
                PrintWriter pw = new PrintWriter(bw); ) {
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(line); // copy one line at a time
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (ArrayIndexOutOfBoundsException aob) {
            System.err.println("You must pass 2 command-line arguments to program: <infile> <outfile>");
        }
        // br, bw and pw are all closed automatically
    }
}
