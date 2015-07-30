package marytts.modules.synthesis.hts;


import java.nio.file.Path;
import java.nio.file.Files;
import java.net.URL;
import java.net.URISyntaxException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import marytts.config.MaryConfig;
import marytts.exceptions.MaryConfigurationException;
import marytts.features.FeatureDefinition;
import marytts.server.MaryProperties;
import marytts.util.FeatureUtils;
import marytts.htsengine.HMMData.FeatureType;
import marytts.util.MaryUtils;
import marytts.util.io.PropertiesAccessor;

import org.apache.log4j.Logger;
/**
 * 
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class HTSData
{
	private Logger logger = MaryUtils.getLogger("HTSData");
    private int rate = 48000; /*< HTS Default sample rate */
    
    public boolean use_gv = false;

    /* Windows */
    public Path win_directory;
    
    /* Trees */
    public Path tree_directory;
    
    /* Models */
    public File dur_model_file;
    public File gv_model_file;
    public File cmp_model_file;

    /* Lists */
    public File full_list_file;
    public File gv_list_file;
    
    /* HTS Configuration */
    public File hts_configuration_file;


    public void init(String voiceName)
        throws IOException, MaryConfigurationException, URISyntaxException
    {
        // Get the config filename 
        PropertiesAccessor p = MaryConfig.getVoiceConfig(voiceName).getPropertiesAccessor(true);
        
		logger.debug("Initialisation of a new HTSData set for voice " + voiceName);
		String prefix = "voice." + voiceName;
		rate = p.getInteger(prefix + ".samplingRate", rate);

        /* Load windows */
		win_directory = HTSUtils.loadDirectory(voiceName, p.getProperty(prefix + ".win_dir"), "win");

        /* Load trees */
		tree_directory = HTSUtils.loadDirectory(voiceName, p.getProperty(prefix + ".tree_dir"), "trees");

        /* Load models */
		dur_model_file = HTSUtils.loadFile(p, prefix, "dur_mmf"); /* Model DUR */
		cmp_model_file = HTSUtils.loadFile(p, prefix, "cmp_mmf"); /* Model LF0 */

        /* Load list */
        full_list_file = HTSUtils.loadFile(p, prefix, "list");

            
        /* Load gv if necessary */
        use_gv = p.getBoolean(prefix + ".usegv"); /* Use Global Variance in parameter generation */
		if (use_gv) {
            // gv_directory = HTSUtils.loadDirectory(voiceName, p.getProperty(prefix + ".gv_dir"), "gv");
		}

        /* Load HTS configuration */
        hts_configuration_file = HTSUtils.loadFile(p, prefix, "configuration");
            
		logger.debug("init is complete");
    }
}
