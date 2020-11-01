import java.io.*;

/**
 * ByteReader - 2 different mechanisms
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class ByteReader {

    /*
     ** This gives you access to the individual bytes and can be used
     *  for any kind of byte-oriented input stream, including a
     *  network stream.
     *
     *  @param i   input stream
     *  @param n   maximum number of bytes to read
     */
    static byte[] readBytes(InputStream i, int n) {
        byte[] buffer = null;

        try {
            byte[] b;
            int r;

            if (i.available() > 0) // something to read?
            {
                b = new byte[n]; // a maximum of n bytes will be read
                r = i.read(b); // note that the value of r includes
                // the end of line (EOL) byte if present

                buffer = new byte[r]; // a buffer of just the rigth size
                for (--r; r >= 0; --r) buffer[r] = b[r];
            }
        } catch (IOException e) {
            System.err.println("ByteReader.readBytes() - error: " + e.getMessage());
        }

        return buffer;
    }

    /*
     *  This is the preferred mechanism for Java keyboard entry.
     *
     *  @param i   input stream
     */
    static String readLine(InputStream i) {
        String line = null;

        try {
            BufferedReader br;

            if (i.available() > 0) // something to read?
            {
                br = new BufferedReader(new InputStreamReader(i));
                line = br.readLine(); // this assumes the a EOL marker of
                // "\n, "\r" or "\r\n" is used, so is
                // not as general as read_1()
            }
        } catch (IOException e) {
            System.err.println("ByteReader.readLine() - error: " + e.getMessage());
        }

        return line;
    }
}
