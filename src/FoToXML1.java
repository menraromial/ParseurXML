import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class FoToXML1 {

    static  Element parti = null;
    static Element chap = null;
    static Element para = null;
    static Element notion = null;
    static Element racine = null;
    static int partIter = 1;
    static int chapCount = 1;
    static int paraCount = 1;

    public static void main(String[] args) {
        System.out.println("En cours de Conversion...");
       converter();
        System.out.println("Conversion terminee");
    }

    public static void converter(){
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setIgnoringElementContentWhitespace(true);
            Document documentSource = builder.build(new File("src/modelSup.fo"));

            racine = new Element("cours");
            Document document = new Document(racine);
            int countNotion = 1;



            Namespace fo = Namespace.getNamespace("http://www.w3.org/1999/XSL/Format");
            Element elementRacine = documentSource.getRootElement().getChild("page-sequence", fo).getChild("flow", fo);
            Iterator iterator = elementRacine.getContent().iterator();
            while (iterator.hasNext()) {
                Object o = iterator.next();

                if (o instanceof Element) {
                    Element child = (Element) o;
                    if("list-block".equals(child.getName())){

                        if (child.getChildren().size()==1){
                            //
                            //System.out.println(child.getChildren().size());
                            String lab = child.getChild("list-item",fo).getChild("list-item-label", fo).getChild("block", fo).getChild("inline",fo).getText();
                            Element tit = (Element) child.getChild("list-item",fo).getChild("list-item-body", fo).getChild("block", fo).getChildren().get(0);
                            String title = tit.getText();
                            if (title==""){
                                tit = (Element) child.getChild("list-item",fo).getChild("list-item-body", fo).getChild("block", fo).getChildren().get(1);
                                title = tit.getText();
                            }

                            if (Pattern.matches("\\d.",lab)){
                                parti = new Element("partie");
                                parti.setAttribute("label", String.valueOf(partIter));
                                //parti.addContent(new Element("label").setText(lab));
                                parti.addContent(new Element("titre").setText(title));
                                racine.addContent(parti);
                                chap=null;
                                para=null;
                                notion = null;
                                partIter++;
                                chapCount=1;
                            }
                            else if (Pattern.matches("\\d.\\d.",lab)){
                                chap = new Element("chapitre");
                                String part ="";
                                if(parti!=null){
                                    part = parti.getAttributeValue("label");
                                }
                                chap.setAttribute("label",part + "." + String.valueOf(chapCount));
                                //chap.addContent(new Element("label").setText(lab));
                                chap.addContent(new Element("titre").setText(title));
                                parti.addContent(chap);

                                para=null;
                                notion=null;
                                chapCount++;
                                paraCount=1;
                            }
                            else if (Pattern.matches("\\d.\\d.\\d.",lab)){
                                String part = "";
                                if (chap!=null){
                                    part = chap.getAttributeValue("label");
                                }
                                para = new Element("paragraphe").setAttribute("label",part + "."+ String.valueOf(paraCount));
                                //para.addContent(new Element("label").setText(lab));
                                para.addContent(new Element("titre").setText(title));
                                chap.addContent(para);
                                notion = new Element("notion").setAttribute("label","notion par defaut");
                                notion.addContent(new Element("titre").setText("titre par defaut"));
                                notion.addContent(new Element("contenu"));
                                para.addContent(notion);
                                paraCount++;
                                countNotion= 1;
                            }
                        }

                        else {
                            //Creer un élément liste
                            String part = conteneur(para, chap, parti);
                            Element list = new Element("ul");
                            insert(list);
                            //racine.addContent(list);
                            Iterator listIterator = child.getContent().iterator();
                            while (listIterator.hasNext()) {
                                Object lt = listIterator.next();
                                if (lt instanceof Element) {
                                    Element listItem = (Element) lt;
                                    String content = listItem.getChild("list-item-body", fo).getChild("block", fo).getText();
                                    list.addContent(new Element("li").setText(content));
                                    //list.addContent(item);
                                }
                            }
                        }
                    }
                    else if ("block".equals(child.getName())){
                        String part =conteneur(para, chap, parti);

                        if (child.getChildren().isEmpty() ){
                            String label = part +"_"+ countNotion;
                            String content = child.getText();
                            Element text = new Element("texte");
                            if (!isBlank(content)) {
                                text.setText(content);
                                insert(text);
                            }
                            //racine.addContent(notion);
                            countNotion++;

                        }
                        else if ( child.getChild("inline",fo).getChild("external-graphic",fo)!=null){
                            ///
                            String url = child.getChild("inline",fo).getChild("external-graphic",fo).getAttributeValue("src");
                            String with = child.getChild("inline",fo).getChild("external-graphic",fo).getAttributeValue("width");
                            String height = child.getChild("inline",fo).getChild("external-graphic",fo).getAttributeValue("height");
                            Element img = new Element("image");
                            img.setAttribute("height",height);
                            img.setAttribute("width",with);
                            img.setText(url);
                            insert(img);
                            //racine.addContent(img);

                        }
                        else {


                            String label = part +"_"+ countNotion;
                            String content = child.getChild("inline",fo).getText();
                            if (!isBlank(content)) {
                                Element text = new Element("texte").setText(content);
                                insert(text);
                            }
                            //racine.addContent(notion);
                            countNotion++;
                        }
                    }
                    else if ("table".equals(child.getName())){
                        //
                        //table-body
                        String part = conteneur(para, chap, parti);
                        Element table = new Element("table");
                        insert(table);
                        //racine.addContent(table);
                        insertTable(table, child,fo);
                    }


                    //System.out.println(child.getName());
                    //afficherTitre(child);
                }
            }

            XMLOutputter xml = new XMLOutputter();
            xml.setFormat(Format.getPrettyFormat());
            xml.output(document, new FileWriter("src/modelSup.xml"));


        } catch (JDOMException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public static void afficherTitre(Element element) {
        ElementFilter filtre = new ElementFilter("titre");

        List children = element.getContent(filtre);
        Iterator iterator = children.iterator();
        while (iterator.hasNext()) {
            Element fils = (Element) iterator.next();
            System.out.println(fils.getText());
        }
    }
    //cette fonction permet de trouver le parent d'un element (Notion, tableau, liste, image)
    public static String conteneur(Element paragraphe, Element Chapitre, Element parti){
        String part ="";
        if (paragraphe!=null){
            part = paragraphe.getAttributeValue("label");
        }
        else if (Chapitre!=null){
            part = Chapitre.getAttributeValue("label");
        }
        else if (parti!=null){
            part = parti.getAttributeValue("label");
        }
        return part;
    }
    //Trouver une partie
    public static boolean isPart(String label){
        return true;
    }

    //Trouver un chapitre
    public static boolean isChap(String label){
        return true;
    }

    //Trouver un paragraphe
    public static boolean isParagraph(String label){
        return true;
    }

    //Trouver une lste
    public static boolean isList(String balise){
        return "list-block".equals(balise);
    }

    //Inserer une notion, une image, un tableau
    public static void insert(Element element){
        if (notion!=null) {
            Element contenu = notion.getChild("contenu");
            contenu.addContent(element);
        }
        else if(parti==null) {
            parti = new Element("partie").setAttribute("label","1");
            parti.addContent(new Element("titre").setText("Préliminaire sur le document"));
            racine.addContent(parti);
            chap = new Element("chapitre").setAttribute("label","chapitre par defaut");
            chap.addContent(new Element("titre").setText("titre par defaut"));
            parti.addContent(chap);
            para = new Element("paragraphe").setAttribute("label","paragraphe par defaut");
            para.addContent(new Element("titre").setText("titre par defaut"));
            chap.addContent(para);
            notion = new Element("notion").setAttribute("label","notion par defaut");
            notion.addContent(new Element("titre").setText("titre par defaut"));
            notion.addContent(new Element("contenu"));
            notion.getChild("contenu").addContent(element);
            para.addContent(notion);
            partIter++;

        }
        else if(chap==null){
            chap = new Element("chapitre").setAttribute("label","chapitre par defaut");
            chap.addContent(new Element("titre").setText("titre par defaut"));
            parti.addContent(chap);
            para = new Element("paragraphe").setAttribute("label","paragraphe par defaut");
            para.addContent(new Element("titre").setText("titre par defaut"));
            chap.addContent(para);
            notion = new Element("notion").setAttribute("label","notion par defaut");
            notion.addContent(new Element("titre").setText("titre par defaut"));
            notion.addContent(new Element("contenu"));
            notion.getChild("contenu").addContent(element);
            para.addContent(notion);
        }
        else if (para==null){
            para = new Element("paragraphe").setAttribute("label","paragraphe par defaut");
            para.addContent(new Element("titre").setText("titre par defaut"));
            chap.addContent(para);
            notion = new Element("notion").setAttribute("label","notion par defaut");
            notion.addContent(new Element("titre").setText("titre par defaut"));
            notion.addContent(new Element("contenu"));
            notion.getChild("contenu").addContent(element);
            para.addContent(notion);
        }
        else if(notion==null){
            notion = new Element("notion").setAttribute("label","notion par defaut");
            notion.addContent(new Element("titre").setText("titre par defaut"));
            notion.addContent(new Element("contenu"));
            notion.getChild("contenu").addContent(element);
            para.addContent(notion);
        }
    }

    public static void insertTable(Element table, Element child, Namespace fo){
        Iterator tabIterator = child.getChild("table-body",fo).getContent().iterator();
        Element tbody = new Element("tbody");
        table.addContent(tbody);
        while (tabIterator.hasNext()) {
            Object tabrow = tabIterator.next();
            if (tabrow instanceof Element) {
                Element row = (Element) tabrow;

                Element tr = new Element("tr");
                tbody.addContent(tr);

                Iterator cellIterator = row.getContent().iterator();
                while (cellIterator.hasNext()){
                    Object cell = cellIterator.next();
                    if (cell instanceof Element){
                        //
                        String content ="";
                        //Les attributs
                        String bgColor = ((Element) cell).getAttributeValue("background-color");
                        String dAlign = ((Element) cell).getAttributeValue("display-align");
                        String pLeft = ((Element) cell).getAttributeValue("padding-left");
                        String bWidth = ((Element) cell).getAttributeValue("border-width");
                        String bStyle = ((Element) cell).getAttributeValue("border-style");
                        String bColor = ((Element) cell).getAttributeValue("border-color");
                        String pRight = ((Element) cell).getAttributeValue("padding-right");
                        String border = bWidth + " " + bStyle + " " + bColor;

                        //String content = ((Element) cell).getChild("block",fo).getText();
                        if (((Element) cell).getChild("block",fo).getChildren().isEmpty()){
                            content = ((Element) cell).getChild("block",fo).getText();
                        }
                        else {
                            content = ((Element) cell).getChild("block",fo).getChild("inline",fo).getText();
                        }
                        //content = ((Element) cell).getChildText("block",fo);
                        Element td = new Element("td");
                        if (bgColor!=null) td.setAttribute("background-color", bgColor);
                        if (border!=null) td.setAttribute("border", border);
                        if (dAlign!=null) td.setAttribute("display-align",dAlign);
                        if (pLeft!=null) td.setAttribute("padding-left",pLeft);
                        if (pRight!=null) td.setAttribute("padding-right",pRight);
                        td.setText(content);
                        tr.addContent(td);
                    }
                }
            }
        }
    }

    //Inserer un paragraphe

    //Inserer un chapitre

    //Eliminer les notions vides et même les caractères
    public static boolean isBlank(String text){
        return text == null | text.trim().isEmpty()|" ".equals(text.trim());
    }
}
