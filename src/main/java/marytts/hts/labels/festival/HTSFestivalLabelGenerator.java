package marytts.hts.labels.festival;

import java.util.Locale;
import java.util.ArrayList;
import java.util.Hashtable;

/* Marytts needed packages */
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.modules.MaryModule;
import marytts.util.MaryUtils;
import marytts.hts.labels.AcousticParamsToLevels;

/* Logger needed packages */
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/* MaryData needed packages */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Text;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* Debug */
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

/* HTS part */
import marytts.hts.synthesis.HTSUtils;

/**
 * HTS Label compatible generator following the festival standard (so useable by the hts-demo)
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class HTSFestivalLabelGenerator implements MaryModule
{
	protected Logger logger; /*< The logger */
    protected int state; /*< Module state */

    /* ========================================================================================================
     * ##
     * ======================================================================================================== */

    /**
     * Default constructor
     */
    public HTSFestivalLabelGenerator()
    {
        logger = MaryUtils.getLogger(name());
        state = MaryModule.MODULE_OFFLINE;
    }

    /* ========================================================================================================
     * ## MaryModule accessors
     * ======================================================================================================== */

    /**
     *  Get the module Name
     *
     *  @return the name of the module "HTSLabelGenerator"
     */
    public String name()
    {
        return "HTSFestivalLabelGenerator";
    }

    /**
     *  Get the input type of the module
     *
     *  @return ACOUSTPARAMS
     */
    public MaryDataType getInputType()
    {
        return MaryDataType.ACOUSTPARAMS;
    }

    @Deprecated
    public MaryDataType inputType()
    {
        return getInputType();
    }

    /**
     *  Get the output type of the module
     *
     *  @return a new type named HTSLABEL
     */
    public MaryDataType getOutputType()
    {
        return HTSUtils.HTSLABEL;
    }

    @Deprecated
    public MaryDataType outputType()
    {
        return getOutputType();
    }

    public Locale getLocale()
    {
        return Locale.ENGLISH;
    }

    public int getState()
    {
        return state;
    }

    /* ========================================================================================================
     * ##
     * ======================================================================================================== */
	/**
	 * Allow the module to start up, performing whatever is necessary to become operational. After successful completion,
	 * getState() should return MODULE_RUNNING.
	 */
	public void startup()
    {
		assert state == MODULE_OFFLINE;
		logger.info("Module started marytts.hts.labels.festival.HTSFestivalLabelGenerator (" + getInputType() +
                    " => " + getOutputType() + ", locale = en).");
        MaryDataType.registerDataType(HTSUtils.HTSLABEL);
        state = MaryModule.MODULE_RUNNING;
    }

	/**
	 * Perform a power-on self test by processing some example input data.
	 *
	 * @throws Error
	 *             if the module does not work properly.
	 */
	public void powerOnSelfTest()
        throws Error
    {
        return;
    }

	/**
	 * Allow the module to shut down cleanly. After this has successfully completed, getState() should return MODULE_OFFLINE.
	 */
	public void shutdown()
    {
        state = MaryModule.MODULE_OFFLINE;
    }



    /* ========================================================================================================
     * ##
     * ======================================================================================================== */

    /**
     *  Main process functions [TODO: terminate !]
     *
     *  @param d [TODO]
     *  @return XXXX [TODO]
     *  @throws Exception [TODO]
     */
	public MaryData process(MaryData d)
        throws Exception
    {
        // /* Debug */
        // TransformerFactory tf = TransformerFactory.newInstance();
        // Transformer transformer = tf.newTransformer();
        // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // StringWriter writer = new StringWriter();
        // transformer.transform(new DOMSource(acoustParams.getDocument()), new StreamResult(writer));
        // String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        // System.out.println(output);

        /* Generate levels information */
        Element root = d.getDocument().getDocumentElement();
        AcousticParamsToLevels transform = new AcousticParamsToLevels(d);
        Hashtable<String, ArrayList<Hashtable<String, String>>> levels = transform.getLevels();

        /* Generate labels */
        LevelsToLabels lvl = new LevelsToLabels(levels);
        String tree = lvl.toString();
        MaryData targetFeatures = new MaryData(getOutputType(), d.getLocale());
        targetFeatures.setPlainText(tree);
		return targetFeatures;
	}
}
