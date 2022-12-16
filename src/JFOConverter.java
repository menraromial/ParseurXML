import de.vc.jfo.Document;
import de.vc.jfo.rtf.RTFImporter;

import java.io.File;
import java.io.IOException;

public class JFOConverter {
    public static void main(String[] args) throws IOException {

        RTFImporter importer = new RTFImporter();
        Document doc = importer.importDocument( "src/modelSup.rtf" );
        doc.writeTo(new File("src/modelSup.fo"));

    }
}
