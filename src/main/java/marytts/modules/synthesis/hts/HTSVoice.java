/**
 * Copyright 2000-2006 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package marytts.modules.synthesis.hts;

import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import marytts.cart.DirectedGraph;
import marytts.cart.io.DirectedGraphReader;
import marytts.config.MaryConfig;
import marytts.config.VoiceConfig;
import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.util.MaryRuntimeUtils;
import marytts.util.MaryUtils;
import marytts.modules.synthesis.Voice;
import marytts.exceptions.SynthesisException;

import org.apache.log4j.Logger;

/**
 * A helper class for the synthesis module; each Voice object represents one available voice database.
 * 
 * @author S&eacute;bastien Le Maguer
 */

public class HTSVoice extends Voice {

    protected HTSWaveformSynthesizer synthesizer;
	private Logger logger = MaryUtils.getLogger("HTSVoice");
    protected HTSData data;
    
	public HTSVoice(String name, HTSWaveformSynthesizer synth)
        throws Exception
    {
        super(name, null);
        this.synthesizer = synth;
        this.data = new HTSData();
        this.data.init(name);
	}

	/**
	 * Synthesize a list of tokens and boundaries with the waveform synthesizer providing this voice.
	 * 
	 * @param outputParams
	 */
	public AudioInputStream synthesize(MaryData d, String outputParams) throws SynthesisException {
		return synthesizer.synthesize(d, this, outputParams);
	}


    /*****************************************************************************************************
     **
     *****************************************************************************************************/
    public File getHTSConfiguration()
    {
        return data.hts_configuration_file;
    }
    
    public File getCMPModel()
    {
        return data.cmp_model_file;
    }
    
    public File getDurationFile()
    {
        return data.dur_model_file;
    }
    
    public Path getTreeDirectory()
    {
        return data.tree_directory;
    }

    public Path getWinDirectory()
    {
        return data.win_directory;
    }
    
    public File getList()
    {
        return data.full_list_file;
    }
    
    public int getGenerationType()
    {
        return 0; // FIXME: add a choose technique in the configuration file
    }
}
