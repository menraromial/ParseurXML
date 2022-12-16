import com.aspose.words.Document;

public class Converter {

    public static void main(String[] args) throws Exception {
        Document doc = new Document("src/ModeleDeSupport.doc");
        doc.save("src/modelSup.rtf");
    }
}
