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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.datatypes.MaryXML;
import marytts.exceptions.SynthesisException;
import marytts.modules.InternalModule;
import marytts.modules.synthesis.Voice;
import marytts.modules.synthesis.WaveformSynthesizer;
import marytts.server.MaryProperties;
import marytts.signalproc.effects.EffectsApplier;
import marytts.util.data.audio.AppendableSequenceAudioInputStream;
import marytts.util.dom.MaryDomUtils;
import marytts.util.dom.NameNodeFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * The synthesis module.
 *
 * @author Marc Schr&ouml;der
 */

public class HTSSynthesis extends InternalModule {
	private List<HTSWaveformSynthesizer> waveformSynthesizers;
	private EffectsApplier effects;

	public HTSSynthesis() {
		super("Synthesis", HTSUtils.HTSLABEL, MaryDataType.AUDIO, null);
	}

	public void startup() throws Exception {
		startupSynthesizers();
		super.startup();
	}

	private void startupSynthesizers() throws ClassNotFoundException, InstantiationException, Exception {
		waveformSynthesizers = new ArrayList<HTSWaveformSynthesizer>();
		for (String synthClassName : MaryProperties.synthesizerClasses()) {
            WaveformSynthesizer ws = (WaveformSynthesizer) Class.forName(synthClassName).newInstance();
            if (ws instanceof HTSWaveformSynthesizer)
            {
                ws.startup();
                waveformSynthesizers.add((HTSWaveformSynthesizer) ws);
            }
		}
	}

	/**
	 * Perform a power-on self test by processing some example input data.
	 * 
	 * @throws Error
	 *             if the module does not work properly.
	 */
	public synchronized void powerOnSelfTest() throws Error {
		for (Iterator<HTSWaveformSynthesizer> it = waveformSynthesizers.iterator(); it.hasNext();) {
			HTSWaveformSynthesizer ws = it.next();
			ws.powerOnSelfTest();
		}
	}

    // FIXME: voice loading management !
	public MaryData process(MaryData d) throws Exception {
		// We produce audio data, so we expect some helpers in our input:
		assert d.getAudioFileFormat() != null : "Audio file format is not set!";
		Document doc = d.getDocument();

		AudioFormat targetFormat = d.getAudioFileFormat().getFormat();
		HTSVoice defaultVoice = (HTSVoice) d.getDefaultVoice();
        
		Locale locale = d.getLocale();
		String outputParams = d.getOutputParams();

		if (defaultVoice == null) {
			defaultVoice = (HTSVoice) Voice.getDefaultVoice(locale);
			if (defaultVoice == null) {
				throw new SynthesisException("No voice available for locale '" + locale + "'");
			}
			logger.info("No default voice associated with data. Assuming global default " + defaultVoice.getName());
		}

		MaryData result = new MaryData(outputType(), d.getLocale());
        
		// Also remember XML document in "AUDIO" output data, to keep track of phone durations:
		result.setDocument(doc);
		result.setAudioFileFormat(d.getAudioFileFormat());
		if (d.getAudio() != null) {
			// This (empty) AppendableSequenceAudioInputStream object allows a
			// thread reading the audio data on the other "end" to get to our data as we are producing it.
			assert d.getAudio() instanceof AppendableSequenceAudioInputStream;
			result.setAudio(d.getAudio());
		}

		HTSVoice currentVoice = defaultVoice;

        
		AudioInputStream ais = null;
		ais = currentVoice.synthesize(d, outputParams);
        
        if (ais != null) {
            // Conversion to targetFormat required?
            if (!ais.getFormat().matches(targetFormat)) {
                // Attempt conversion; if not supported, log a warning
                // and provide the non-converted stream.
                logger.info("Audio format conversion required for voice " + currentVoice.getName());
                try {
                    AudioInputStream intermedStream = AudioSystem.getAudioInputStream(targetFormat, ais);
                    ais = intermedStream;
                } catch (IllegalArgumentException iae) { // conversion not supported
                    boolean solved = false;
                    // try again with intermediate sample rate conversion
                    if (!targetFormat.getEncoding().equals(ais.getFormat())
						&& targetFormat.getSampleRate() != ais.getFormat().getSampleRate()) {
                        AudioFormat sampleRateConvFormat = new AudioFormat(ais.getFormat().getEncoding(),
                                                                           targetFormat.getSampleRate(), ais.getFormat().getSampleSizeInBits(), ais.getFormat().getChannels(),
                                                                           ais.getFormat().getFrameSize(), ais.getFormat().getFrameRate(), ais.getFormat().isBigEndian());
                        try {
                            AudioInputStream intermedStream = AudioSystem.getAudioInputStream(sampleRateConvFormat, ais);
                            ais = AudioSystem.getAudioInputStream(targetFormat, intermedStream);
                            // No exception thrown, i.e. success
                            solved = true;
                        } catch (IllegalArgumentException iae1) {
                        }
                    }
                    if (!solved)
                        throw new UnsupportedAudioFileException("Conversion from audio format " + ais.getFormat()
                                                                + " to requested audio format " + targetFormat + " not supported.\n" + iae.getMessage());
                }
            }
            
            result.appendAudio(ais);
        }
        
		return result;
	}
}
