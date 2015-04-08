package no.lau.vdvil.domain;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public interface FileRepresentation {
    URL remoteAddress();
    File localStorage(); //Confirmed stored locally
    String md5CheckSum();

    static FileRepresentation NULL = new FileRepresentation() {
        public URL remoteAddress() {return nullURL();}
        public File localStorage() {return new File("NULL");}
        public String md5CheckSum() {return "NULL";}
        public String toString() { return "NULL FileRepresentation";}

        private URL nullURL(){
            try { return new URL("NULL");
            } catch (MalformedURLException e) {throw new RuntimeException();}
        }
    };
}
