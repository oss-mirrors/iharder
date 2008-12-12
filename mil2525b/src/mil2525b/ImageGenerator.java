package mil2525b;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Student
 */
public class ImageGenerator {

    private char affiliation = '-'; // Friendly, hostile, etc
    private char domain = '-';      // Air, ground, etc
    private char level3 = '-';      // Military, civilian, equipment, etc
    private char level4 = '-';

    private StringBuilder append = new StringBuilder();

    public static void main(String[] args){
        JFrame f = new JFrame("ImageGenerator Test");
        f.getContentPane().setLayout(new BorderLayout());
        ImageGenerator ig = ImageGenerator.getInstance().hostile().groundWeapon();
        System.out.println(CotHelper.cotTypes(ig.toCotString()));
        f.getContentPane().add( new JLabel( ig.toCotString(), ig.getImage(), JLabel.CENTER), BorderLayout.CENTER );
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }

    private ImageGenerator(){
    }


    public static ImageGenerator getInstance(){
        return new ImageGenerator();
    }

    public ImageGenerator friendly(){
        this.affiliation = 'f';
        return this;
    }

    public ImageGenerator hostile(){
        this.affiliation = 'h';
        return this;
    }

    public ImageGenerator unknown(){
        this.affiliation = 'u';
        return this;
    }

    public ImageGenerator pending(){
        this.affiliation = 'p';
        return this;
    }

    public ImageGenerator assumed(){
        this.affiliation = 'a';
        return this;
    }

    public ImageGenerator neutral(){
        this.affiliation = 'n';
        return this;
    }

    public ImageGenerator suspect(){
        this.affiliation = 's';
        return this;
    }

    public ImageGenerator joker(){
        this.affiliation = 'j';
        return this;
    }

    public ImageGenerator faker(){
        this.affiliation = 'k';
        return this;
    }


    public ImageGenerator air(){
        this.domain = 'A';
        return this;
    }
    public ImageGenerator ground(){
        this.domain = 'G';
        return this;
    }
    public ImageGenerator surface(){
        this.domain = 'S';
        return this;
    }
    public ImageGenerator underwater(){
        this.domain = 'U';
        return this;
    }


    public ImageGenerator military(){
        this.level3 = 'M';
        return this;
    }
    public ImageGenerator civilian(){
        this.level3 = 'C';
        return this;
    }
    public ImageGenerator equipment(){
        this.level3 = 'E';
        return this;
    }
    public ImageGenerator structure(){
        this.level3 = 'I';
        return this;
    }
    public ImageGenerator unit(){
        this.level3 = 'U';
        return this;
    }



    public ImageGenerator airWeapon(){
        this.domain = 'A';
        this.level3 = 'W';
        return this;
    }
    public ImageGenerator groundWeapon(){
        this.domain = 'G';
        this.level3 = 'E';
        this.level4 = 'W';
        return this;
    }

    public ImageGenerator missile(){
        this.level4 = 'M';
        return this;
    }

    


    public ImageGenerator append( String cotSequence ){
        append.append(cotSequence);
        return this;
    }


    public String toCotString(){
        return String.format("%c-%c-%c-%c-%c", 'a', affiliation, domain, level3, level4 ) + append;
    }

    public String toString(){
        return toCotString();
    }

    public ImageIcon getImage(){
        byte[] data = null;
        String cot = toCotString();
        while( (data = Mil2525b.getBytesFromCotType( cot )) == null && cot.length() > 2 ){
            cot = cot.substring(0,cot.length()-2);
        }
        return data == null ? null : new ImageIcon(data);
    }


}
