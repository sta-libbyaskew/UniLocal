import java.io.*;

/**
 * ByteWriter - 2 different mechanisms
 *
 * Saleem Bhatti, http://www.cs.st-andrews.ac.uk/~saleem/ February 2007
 */
public class ByteWriter {

    /*
     ** This gives you access to the individual bytes and can be used
     *  for any kind of byte-oriented ouptut stream, including a
     *  network stream.
     *
     *  @param o   output stream
     *  @param b   buffer to write
     *  @param n   maximum number of bytes to write
     */
    static int writeBytes(OutputStream o, byte[] b, int n) {
        if ((o == null) || (b == null) || (n < 1)) return 0;

        int w = 0;

        try {
            if (n >= b.length) n = b.length;
            if (n > 0) w = n;

            o.write(b, 0, w);
        } catch (IOException e) {
            System.err.println("ByteWriter.writeBytes() - error: " + e.getMessage());
        }

        return w;
    }

    /*
     *  This is the preferred mechanism for Java screen output
     *  but can also be used to write to a network stream.
     *  Assumes s is a printable string.
     *
     *  @param o   ouput stream
     *  @param s   printable string
     */
    static void writeLine(OutputStream o, String s) {
        if ((s == null) || (o == null)) return;

        PrintWriter pr = new PrintWriter(o, true);
        pr.println(s);
        // pr.flush();

        return;
    }
}
