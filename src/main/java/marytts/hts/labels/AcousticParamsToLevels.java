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


/**
 * Generate levels from the acoustparams output (xml => object oriented format)
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class AcousticParamsToLevels
{

    private Hashtable<String, ArrayList<Hashtable<String, String>>> levels;

    public AcousticParamsToLevels(MaryData acoustParams)
    {
        generateLevels(acoustParams.getDocument().getDocumentElement());
    }

    private Hashtable<String, String> generatePhrase(Element p)
    {
        Hashtable<String, String> phrase = new Hashtable<String, String>();

        int nb_wrds = 0;
        int nb_syls = 0;

        // Dealing with words (ignoring MTU, ..)
        NodeList wrd_nl = p.getElementsByTagName("t");
        for (int wrd_idx=0; wrd_idx<wrd_nl.getLength(); wrd_idx++)
        {
            NodeList syl = ((Element) wrd_nl.item(wrd_idx)).getElementsByTagName("syllable");
            for (int s=0; s<syl.getLength(); s++)
            {
                nb_syls++;
            }

            nb_wrds++;
        }

        NodeList bound_nl = p.getElementsByTagName("boundary");
        if (bound_nl.getLength() > 2)
        {
            // logging the problem

        }
        for (int i=0; i<bound_nl.getLength(); i++)
        {
            Element elt = (Element) bound_nl.item(i);
            NamedNodeMap attr = elt.getAttributes();
            phrase.put("tobi", ((Attr) attr.getNamedItem("tone")).getValue());
        }

        phrase.put("size_in_syls", Integer.toString(nb_syls));
        phrase.put("size_in_words", Integer.toString(nb_wrds));

        return phrase;
    }

    private Hashtable<String, String> generateWord(Element w)
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

    private Hashtable<String, String> generateSyllable(Element s)
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

    private Hashtable<String, String> generatePhoneme(Element p)
    {
        Hashtable<String, String> ph = new Hashtable<String, String>();
        NamedNodeMap attr = p.getAttributes();
        ph.put("label", ((Attr) attr.getNamedItem("p")).getValue());
        if (attr.getNamedItem("d") != null)
        {
            ph.put("duration", ((Attr) attr.getNamedItem("d")).getValue());
            levels.get("phoneme").get(0).put("duration", "400"); // FIXME: really patchy
        }
        return ph;
    }

    private void generateLevel(Element node)
    {
        if (node.getTagName().equals("ph"))
        {
            ArrayList<Hashtable<String, String>> level = new ArrayList<Hashtable<String, String>>();
            levels.get("phoneme").add(generatePhoneme(node));
        }
        else if (node.getTagName().equals("syllable")) // FIXME: syllable tag hardcoded
        {
            levels.get("syllable").add(generateSyllable(node));
        }
        else if (node.getTagName().equals("t")) // FIXME: word tag hardcoded
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
                // FIXME: really really patch!
                if (levels.get("phoneme").get(0).containsKey("duration"))
                    nss.put("duration", "400");

                levels.get("phoneme").add(nss);
            }

        }
        else if (node.getTagName().equals("phrase")) // FIXME: phrase tag hardcoded
        {
            levels.get("phrase").add(generatePhrase(node));
        }
    }

    private void generateLevelsRec(Node root)
    {
        if (root instanceof Element)
        {
            generateLevel((Element) root);

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
        // Check if the first phoneme have a duration
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
