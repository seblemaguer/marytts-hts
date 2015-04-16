package marytts.modules.synthesis.hts;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URISyntaxException;

import marytts.datatypes.MaryDataType;

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
public class HTSUtils
{
    public static final MaryDataType HTSLABEL = new MaryDataType("HTSLABEL", true, true, MaryDataType.PLAIN_TEXT, null);
    public static final MaryDataType HTSLABEL_DEBUG = new MaryDataType("HTSLABEL_DEBUG", true, true, MaryDataType.PLAIN_TEXT, null);

    /**
     *  Loading a resource stream into a temp file in order to accessible from a shell command int the HTSSynthesizer
     *
     *  @param p : the property accessor which indicates the resource path
     *  @param prefix : the voice specific prefix
     *  @param key : the specific property we want to load
     *  @return a File which is the temp file
     *  @throws IOException or MaryConfigurationException in case of access or file creation error
     */
    public static File loadFile(PropertiesAccessor p, String prefix, String key)
        throws IOException, MaryConfigurationException
    {
        /* Create a temp file */
        File tmp = File.createTempFile(key, "");

        /* Add the data from the resource */
        OutputStream os = new FileOutputStream(tmp);
        InputStream is = p.getStream(prefix + "." + key);
        
        byte[] buffer = new byte[2048];
        int nbytes;
        while ((nbytes = is.read(buffer, 0, 2048)) > 0)
        {
            os.write(buffer, 0, nbytes);
        }
        os.close();

        /* Return a path to the temp file */
        return tmp;       
    }

    public static Path loadDirectory(String voiceName, String path, String prefix)
        throws IOException, MaryConfigurationException, URISyntaxException
    {
        Path directory = Files.createTempDirectory(prefix);
        URL url = MaryConfig.getVoiceConfig(voiceName).getClass().getResource(path.replace("jar:", ""));
        File dir = new File(url.toURI());
        for (File nextFile : dir.listFiles()) {
            OutputStream os = new FileOutputStream(directory.toString() + "/" + nextFile.getName());
            InputStream is = new FileInputStream(nextFile);

            byte[] buffer = new byte[2048];
            int nbytes;
            while ((nbytes = is.read(buffer, 0, 2048)) > 0)
            {
                os.write(buffer, 0, nbytes);
            }
            os.close();
        }

        return directory;
    }    
}
