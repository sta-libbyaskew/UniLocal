import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyCharacters {
    public static void main(String[] args) {
        FileReader fr = null;
        FileWriter fw = null;
        try {
            fr = new FileReader(args[0]);
            fw = new FileWriter(args[1]);

            int c; // int representing unicode char
            while ((c = fr.read()) != -1) {
                fw.write(c); // copy one character at a time
            }
            fr.close();
            fw.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (ArrayIndexOutOfBoundsException aob) {
            System.err.println("You must pass 2 command-line arguments to program: <infile> <outfile>");
        }
    }
}
