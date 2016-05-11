/**
 * Copyright 2000-2016 DFKI GmbH.
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

import java.util.List;

import marytts.datatypes.MaryData;

import marytts.modules.synthesis.Voice;

import javax.sound.sampled.AudioInputStream;

import marytts.exceptions.SynthesisException;

import marytts.modules.synthesis.WaveformSynthesizer;

/**
 * Provide a common interface for all waveform synthesizers, to be called from within the "wrapping"
 * Synthesis module.
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public interface HTSWaveformSynthesizer extends WaveformSynthesizer {
	/**
	 * Synthesize a given part of a MaryXML document. This method is expected to be thread-safe.
	 *
	 * @param d
     *            labels
	 * @param voice
	 *            the Voice to use for synthesis
	 * @param outputParams
	 *            any specified output parameters; may be null
	 * @return an AudioInputStream in synthesizer-native audio format.
	 * @throws IllegalArgumentException
	 *             if the voice requested for this section is incompatible with this WaveformSynthesizer.
	 */
	public AudioInputStream synthesize(MaryData d, Voice voice, String outputParams)
        throws SynthesisException;
}
