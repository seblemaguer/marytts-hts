package marytts.hts.labels;

import java.util.ArrayList;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/* testng part */
import org.testng.Assert;
import org.testng.annotations.*;

/* Marytts needed packages */
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.modules.MaryModule;
import marytts.util.MaryUtils;
import marytts.hts.synthesis.HTSUtils;
import marytts.hts.labels.HTSLabelGenerator;

/* MaryData needed packages */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;



/**
 * Test class for the main hts label generation module
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class HTSLabelGeneratorTest
{
    static final String text = "that's where you're going";
    MaryInterface mary;

    @BeforeSuite
	public void setupClass() throws Exception {
        mary = new LocalMaryInterface();
        mary.setOutputType("HTSLABEL");
        Assert.assertEquals("HTSLABEL", mary.getOutputType());
    }


	protected String loadResourceIntoString(String resourceName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourceName), "UTF-8"));
		StringBuilder buf = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			buf.append(line);
			buf.append("\n");
		}
		return buf.toString();
	}

    /**
     * Check baseline for german
     *
     */
	@Test
	public void convertTextToPhonemesDE() throws Exception {

        Locale loc = Locale.GERMAN;
        mary.setLocale(loc);
		Assert.assertEquals(loc, mary.getLocale());

		mary.setOutputType(HTSUtils.HTSLABEL.name());
        String text = loadResourceIntoString("utt1_de.txt");
        String generated_labels = mary.generateText(text);
        String original_labels = loadResourceIntoString("utt1_de.lab");

        Assert.assertEquals(generated_labels, original_labels);
    }

    /**
     * Check baseline for french
     *
     */
	@Test
	public void convertTextToPhonemesFR() throws Exception {

        Locale loc = Locale.FRENCH;
        mary.setLocale(loc);
		Assert.assertEquals(loc, mary.getLocale());

		mary.setOutputType(HTSUtils.HTSLABEL.name());
        String text = loadResourceIntoString("utt1_fr.txt");
        String generated_labels = mary.generateText(text);
        String original_labels = loadResourceIntoString("utt1_fr.lab");

        Assert.assertEquals(generated_labels, original_labels);
    }

    /**
     * Check when there is a recursive MTU (ex: phrase -&gt; [mtu -&gt;]* terms)
     *
     */
	@Test
	public void testRecursiveMTU() throws Exception {

        Locale loc = Locale.GERMAN;
        mary.setLocale(loc);
		Assert.assertEquals(loc, mary.getLocale());

		mary.setOutputType(HTSUtils.HTSLABEL.name());
        String text = loadResourceIntoString("utt2_de.txt");
        String generated_labels = mary.generateText(text);
        String original_labels = loadResourceIntoString("utt2_de.lab");

        Assert.assertEquals(generated_labels, original_labels);
    }

    /**
     * Check when there is embedded terms into a phrase (ex: phrase -&gt; phonology -&gt; [mtu -&gt;]* terms)
     *
     */
	@Test
	public void testEmbeddedTerms() throws Exception {

        Locale loc = Locale.GERMAN;
        mary.setLocale(loc);
		Assert.assertEquals(loc, mary.getLocale());

		mary.setOutputType(HTSUtils.HTSLABEL.name());
        String text = loadResourceIntoString("utt3_de.txt");
        String generated_labels = mary.generateText(text);
        String original_labels = loadResourceIntoString("utt3_de.lab");

        Assert.assertEquals(generated_labels, original_labels);
    }
}
