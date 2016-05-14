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


public class HTSLabelGeneratorTest
{
    static final String text = "that's where you're going";
    MaryInterface mary;

    @BeforeSuite
	public void setupClass() throws Exception {
        mary = new LocalMaryInterface();
        Locale loc = Locale.GERMAN;
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