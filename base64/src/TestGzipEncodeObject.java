
import java.io.IOException;
import javax.swing.JLabel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @version $Id$
 * @author $Author$
 */
public class TestGzipEncodeObject {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        JLabel label = new JLabel("hello world");
        String enc = Base64.encodeObject(label, Base64.NO_OPTIONS);
        String encZ = Base64.encodeObject(label, Base64.GZIP);
        System.out.printf("Uncompressed: %d\nCompressed: %d\n", enc.length(),encZ.length());
        JLabel encLabel = (JLabel) Base64.decodeToObject(enc);
        System.out.println("Recovered from uncompressed: " + encLabel );
        JLabel encZLabel = (JLabel) Base64.decodeToObject(encZ);
        System.out.println("Recovered from compressed: " + encZLabel );
    }

}
