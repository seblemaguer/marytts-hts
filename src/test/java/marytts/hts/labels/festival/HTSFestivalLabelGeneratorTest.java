package marytts.hts.labels.festival;

import java.util.ArrayList;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

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
import marytts.hts.labels.festival.HTSFestivalLabelGenerator;

/* MaryData needed packages */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Test class for the festival hts label generation module
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class HTSFestivalLabelGeneratorTest
{
    MaryInterface mary;

    @BeforeSuite
	public void setupClass() throws Exception {
        // log4j.logger.marytts = DEBUG, stderr
        Properties props = System.getProperties();
        props.setProperty("voice.cmu-slt-hsmm.preferredModules",
                          "marytts.hts.labels.festival.HTSFestivalLabelGenerator");
        mary = new LocalMaryInterface();
        Locale loc = Locale.US;
        mary.setLocale(loc);

        mary.setOutputType("HTSLABEL");
		Assert.assertEquals(loc, mary.getLocale());
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

	@Test
	public void convertTextToPhonemes() throws Exception {
		mary.setOutputType(HTSUtils.HTSLABEL.name());
        String text = loadResourceIntoString("utt1.txt");
        String generated_labels = mary.generateText(text);
        String original_labels = loadResourceIntoString("utt1.lab");

        Assert.assertEquals(generated_labels, original_labels);
    }
}
