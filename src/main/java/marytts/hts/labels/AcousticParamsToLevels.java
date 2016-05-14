package marytts.hts.labels;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.*;

/* MaryData needed packages */
import marytts.datatypes.MaryData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Text;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AcousticParamsToLevels
{

    private Hashtable<String, ArrayList<Hashtable<String, String>>> levels;

    public AcousticParamsToLevels(MaryData acoustParams)
    {
        generateLevels(acoustParams.getDocument().getDocumentElement());
    }

    private Hashtable<String, String> generatePhrase(Node p)
    {
        Hashtable<String, String> phrase = new Hashtable<String, String>();

        int nb_wrds = 0;
        int nb_syls = 0;

        // Child
        NodeList nl = p.getChildNodes();
        for (int i=0; i<nl.getLength(); i++)
        {
            try {
                Element elt = (Element) nl.item(i);
                if (elt.getTagName().equals("boundary")) {
                    NamedNodeMap attr = elt.getAttributes();
                    phrase.put("tobi", ((Attr) attr.getNamedItem("tone")).getValue());
                }

                else if ((nl.item(i) instanceof Element) &&
                         (((Element) nl.item(i)).getTagName().equals("t"))) // FIXME: hardcoded
                {
                    NodeList syl = elt.getChildNodes();
                    for (int s=0; s<syl.getLength(); s++)
                    {
                        if ((syl.item(s) instanceof Element) &&
                            (((Element) syl.item(s)).getTagName().equals("syllable"))) // FIXME: hardcoded
                        {
                            nb_syls++;
                        }
                    }
                    nb_wrds++;
                }

                // FIXME: mtu here ?!
                else if ((nl.item(i) instanceof Element) &&
                         (((Element) nl.item(i)).getTagName().equals("mtu"))) // FIXME: hardcoded
                {
                    NodeList wrds = elt.getChildNodes();
                    for (int w=0; w<wrds.getLength(); w++)
                    {
                        if ((wrds.item(w) instanceof Element) &&
                            (((Element) wrds.item(w)).getTagName().equals("t"))) // FIXME: hardcoded
                        {
                            NodeList syl = ((Element) wrds.item(w)).getChildNodes();
                            for (int s=0; s<syl.getLength(); s++)
                            {
                                if ((syl.item(s) instanceof Element) &&
                                    (((Element) syl.item(s)).getTagName().equals("syllable"))) // FIXME: hardcoded
                                {
                                    nb_syls++;
                                }
                            }

                            nb_wrds++;
                        }
                    }
                }
            }
            catch (Exception ex) // FIXME: be more precise !
            {
            }
        }

        phrase.put("size_in_syls", Integer.toString(nb_syls));
        phrase.put("size_in_words", Integer.toString(nb_wrds));

        return phrase;
    }

    private Hashtable<String, String> generateWord(Node w)
    {
        Hashtable<String, String> word = new Hashtable<String, String>();
        NamedNodeMap attr = w.getAttributes();
        for (int i=0; i<attr.getLength(); i++) {
            word.put(((Attr) attr.item(i)).getName(), ((Attr) attr.item(i)).getValue());
        }

        // Compute size
        NodeList nl = w.getChildNodes();
        int size = 0;
        for (int i=0; i<nl.getLength(); i++)
        {
            if ((nl.item(i) instanceof Element) &&
                (((Element) nl.item(i)).getTagName().equals("syllable"))) // FIXME: hardcoded
            {
                size++;
            }
            else if (nl.item(i) instanceof Text)
            {
                word.put("label", ((Text) nl.item(i)).getWholeText());
            }
        }
        word.put("size", Integer.toString(size));

        return word;
    }

    private Hashtable<String, String> generateSyllable(Node s)
    {
        Hashtable<String, String> syl = new Hashtable<String, String>();
        NamedNodeMap attr = s.getAttributes();

        // Get stress information
        try
        {
            syl.put("stress", ((Attr) attr.getNamedItem("stress")).getValue());
        }
        catch (Exception ex) // FIXME: DOMException
        {
            syl.put("stress", "0");
        }

        // Compute size
        NodeList nl = s.getChildNodes();
        int size = 0;
        for (int i=0; i<nl.getLength(); i++)
        {
            if ((nl.item(i) instanceof Element) &&
                (((Element) nl.item(i)).getTagName().equals("ph"))) // FIXME: hardcoded
            {
                size++;
            }
        }
        syl.put("size", Integer.toString(size));

        // TODO:
        syl.put("vowel", "x");

        return syl;
    }

    private Hashtable<String, String> generatePhoneme(Node p)
    {
        Hashtable<String, String> ph = new Hashtable<String, String>();
        NamedNodeMap attr = p.getAttributes();
        ph.put("label", ((Attr) attr.getNamedItem("p")).getValue());
        return ph;
    }

    private void generateLevel(Node node)
    {
        if (((Element) node).getTagName().equals("ph"))
        {
            ArrayList<Hashtable<String, String>> level = new ArrayList<Hashtable<String, String>>();
            levels.get("phoneme").add(generatePhoneme(node));
        }
        else if (((Element) node).getTagName().equals("syllable")) // FIXME: syllable tag hardcoded
        {
            levels.get("syllable").add(generateSyllable(node));
        }
        else if (((Element) node).getTagName().equals("t")) // FIXME: word tag hardcoded
        {
            NamedNodeMap attr = node.getAttributes();
            if (attr.getNamedItem("ph") != null) // FIXME: deal with double quotes
            {
                levels.get("word").add(generateWord(node));
            }
            else
            {
                // FIXME: which kind of pause !
                Hashtable<String, String> nss = new Hashtable<String, String>();
                nss.put("label", "_");
                levels.get("phoneme").add(nss);
            }

        }
        else if (((Element) node).getTagName().equals("phrase")) // FIXME: phrase tag hardcoded
        {
            levels.get("phrase").add(generatePhrase(node));
        }
    }

    private void generateLevelsRec(Node root)
    {
        if (root instanceof Element)
        {
            generateLevel(root);

            // Child
            NodeList nl = root.getChildNodes();
            for (int i=0; i<nl.getLength(); i++)
            {
                if (nl.item(i) instanceof Element) // FIXME: Stupid patch to ignore the text
                {
                    generateLevelsRec(nl.item(i));
                }
            }
        }
    }


    private void generateLevels(Node root)
    {
        levels = new Hashtable<String, ArrayList<Hashtable<String, String>>>();

        levels.put("utt", new ArrayList<Hashtable<String, String>>());
        levels.put("phrase", new ArrayList<Hashtable<String, String>>());
        levels.put("word", new ArrayList<Hashtable<String, String>>());
        levels.put("syllable", new ArrayList<Hashtable<String, String>>());
        levels.put("phoneme", new ArrayList<Hashtable<String, String>>());

        // FIXME: Adding artificially a start pause
        Hashtable<String, String> nss = new Hashtable<String, String>();
        nss.put("label", "_");
        levels.get("phoneme").add(nss);

        // Now generate levels information
        generateLevelsRec(root);

        // Adding counting information for utterance level
        Hashtable<String, String> utt = new Hashtable<String, String>();
        utt.put("nb_syllables", Integer.toString(levels.get("syllable").size()));
        utt.put("nb_words", Integer.toString(levels.get("word").size()));
        utt.put("nb_phrases", Integer.toString(levels.get("phrase").size()));
        levels.get("utt").add(utt);
    }

    public Hashtable<String, ArrayList<Hashtable<String, String>>> getLevels()
    {
        return levels;
    }
}
