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

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.JarURLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;
/**
 * 
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class HTSUtils
{
	private static Logger logger = MaryUtils.getLogger("HTSUtils");
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

        String in_path = path.replace("jar:/", "");
        final File jarFile = new File(MaryConfig.getVoiceConfig(voiceName).getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        if(jarFile.isFile()) {  // Run with JAR file
            final JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            while(entries.hasMoreElements()) {
                
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) entries.nextElement();
                /* FIXME: For the future  ?
                if (file.isDirectory()) { // if its a directory, create it
                    f.mkdir();
                    continue;
                }
                */
                
                String name = file.getName();//filter according to the path
                if ((name.startsWith(in_path)) && (!name.endsWith("/")))
                { 
                    OutputStream fos = new FileOutputStream(directory.toString() + "/" + (new File(name)).getName());
                    java.io.InputStream is = jar.getInputStream(file); // get the input stream
                    while (is.available() > 0) {  // write contents of 'is' to 'fos'
                        fos.write(is.read());
                    }
                    fos.close();
                    is.close();
                    logger.debug("name is loaded : " + name);
                }
            }
            jar.close();
        }
        
        return directory;
    }    
}
