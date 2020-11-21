import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyBytes {
    public static void main(String[] args) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(args[0]);
            out = new FileOutputStream(args[1]);

            int c; // int representing byte
            while ((c = in.read()) != -1) {
                out.write(c); // copy one raw byte at a time
            }
            in.close();
            out.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (ArrayIndexOutOfBoundsException aob) {
            System.err.println("You must pass 2 command-line arguments to program: <infile> <outfile>");
        }
    }
}
