package marytts.hts.labels.festival;

import java.util.ArrayList;
import java.util.Hashtable;
import marytts.hts.labels.LevelsToLabels;

/**
 * Transform levels to HTS compatible labels using the festival format. Therefore, only english is
 * supported for this generator.
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */

public class FestivalLevelsToLabels extends LevelsToLabels
{

    /* ==========================================================================================
     * # Constructors
     * ========================================================================================== */
    public FestivalLevelsToLabels(Hashtable<String, ArrayList<Hashtable<String, String>>> levels)
    {
        super(levels);
        alphabet_converter = FestivalLevelsToLabels.initPhConverter();
    }

    /* ==========================================================================================
     * # Conversion helpers
     * ========================================================================================== */
    public static Hashtable<String, String> initPhConverter()
    {
        Hashtable<String, String> alphabet_converter = new Hashtable<String, String>();

        // Vowels
        alphabet_converter.put("A", "aa");
        alphabet_converter.put("AI", "ay");
        alphabet_converter.put("E", "eh");
        alphabet_converter.put("EI", "ey");
        alphabet_converter.put("I", "ih");
        alphabet_converter.put("O", "ao");
        alphabet_converter.put("OI", "oy");
        alphabet_converter.put("U", "uh");
        alphabet_converter.put("aU", "aw");
        alphabet_converter.put("i", "iy");
        alphabet_converter.put("u", "uw");
        alphabet_converter.put("@", "ax");
        alphabet_converter.put("@U", "ow");
        alphabet_converter.put("V", "ah");
        alphabet_converter.put("{", "ae");

        alphabet_converter.put("j", "y");

        alphabet_converter.put("D", "dh");
        alphabet_converter.put("N", "ng");
        alphabet_converter.put("S", "sh");
        alphabet_converter.put("T", "th");
        alphabet_converter.put("Z", "zh");
        alphabet_converter.put("b", "b");
        alphabet_converter.put("d", "d");
        alphabet_converter.put("dZ", "jh"); // FIXME: what it is ?
        alphabet_converter.put("f", "f");
        alphabet_converter.put("g", "g");
        alphabet_converter.put("h", "hh");
        alphabet_converter.put("k", "k");
        alphabet_converter.put("l", "l");
        alphabet_converter.put("m", "m");
        alphabet_converter.put("n", "n");
        alphabet_converter.put("p", "p");
        alphabet_converter.put("r", "r");
        alphabet_converter.put("r=", "r"); // FIXME: sure ?
        alphabet_converter.put("s", "s");
        alphabet_converter.put("t", "t");
        alphabet_converter.put("tS", "ch");
        alphabet_converter.put("v", "v");
        alphabet_converter.put("w", "w");
        alphabet_converter.put("z", "z");

        alphabet_converter.put("_", "pau");

        alphabet_converter.put("2", "eu");
        alphabet_converter.put("4", "dx");
        alphabet_converter.put("6", "er");
        alphabet_converter.put("9", "oe");
        alphabet_converter.put("?", "dt");

        return alphabet_converter;
    }

    protected void initPOSConverter()
    {
        pos_converter = new Hashtable<String, String>();

        // aux
        pos_converter.put("is", "aux");
        pos_converter.put("am", "aux");
        pos_converter.put("are", "aux");
        pos_converter.put("was", "aux");
        pos_converter.put("were", "aux");
        pos_converter.put("has", "aux");
        pos_converter.put("have", "aux");
        pos_converter.put("had", "aux");
        pos_converter.put("be", "aux");

        // cc
        pos_converter.put("and", "cc");
        pos_converter.put("but", "cc");
        pos_converter.put("or", "cc");
        pos_converter.put("plus", "cc");
        pos_converter.put("yet", "cc");
        pos_converter.put("nor", "cc");

        // det
        pos_converter.put("the", "det");
        pos_converter.put("a", "det");
        pos_converter.put("an", "det");
        pos_converter.put("no", "det");
        pos_converter.put("some", "det");
        pos_converter.put("this", "det");
        pos_converter.put("that", "det");
        pos_converter.put("each", "det");
        pos_converter.put("another", "det");
        pos_converter.put("those", "det");
        pos_converter.put("every", "det");
        pos_converter.put("all", "det");
        pos_converter.put("any", "det");
        pos_converter.put("these", "det");
        pos_converter.put("both", "det");
        pos_converter.put("neither", "det");
        pos_converter.put("no", "det");
        pos_converter.put("many", "det");

        // in
        pos_converter.put("in", "in");

        // md
        pos_converter.put("will", "md");
        pos_converter.put("may", "md");
        pos_converter.put("would", "md");
        pos_converter.put("can", "md");
        pos_converter.put("could", "md");
        pos_converter.put("must", "md");
        pos_converter.put("ought", "md");
        pos_converter.put("might", "md");

        // pps
        pos_converter.put("her", "pps");
        pos_converter.put("his", "pps");
        pos_converter.put("their", "pps");
        pos_converter.put("its", "pps");
        pos_converter.put("our", "pps");
        pos_converter.put("their", "pps");
        pos_converter.put("mine", "pps");

        // to
        pos_converter.put("to", "to");

        // wp
        pos_converter.put("who", "wp");
        pos_converter.put("what", "wp");
        pos_converter.put("where", "wp");
        pos_converter.put("when", "wp");
        pos_converter.put("how", "wp");

        // punc
        pos_converter.put(".", "punc");
        pos_converter.put(",", "punc");
        pos_converter.put(":", "punc");
        pos_converter.put(";", "punc");
        pos_converter.put("\"", "punc");
        pos_converter.put("'", "punc");
        pos_converter.put("(", "punc");
        pos_converter.put("?", "punc");
        pos_converter.put(")", "punc");
        pos_converter.put("!", "punc");

        // content => default do nothing
    }

    protected String convertPOS(Hashtable<String, String> cur_wrd)
    {
        String fest_pos = pos_converter.get(cur_wrd.get("label"));

        if (fest_pos != null)
            return fest_pos;

        return "content";
    }

    protected String convertPOS(String pos)
    {
        String fest_pos = pos_converter.get(pos);
        if (fest_pos != null)
            return fest_pos;

        return "content";
    }

    /* ==========================================================================================
     * # Conversion helpers
     * ========================================================================================== */
    protected void listStressedPerUtterance()
    {
        stressed_syl_indexes = new ArrayList<Integer>();

        for (int i=0; i<levels.get("syllable").size(); i++)
        {
            if (levels.get("syllable").get(i).get("stress").equals("1"))
                stressed_syl_indexes.add(i);
        }
    }

    /* ==========================================================================================
     * # Matrice generation
     * ========================================================================================== */
    protected void generatePhonemePart(int seg_idx)
    {
        assert matrice != null;
        assert matrice.get(seg_idx) != null;

        // Get label list (FIXME: could be more time efficient)

        // Previous phonemes
        if (cur_pho_index > 1)
            matrice.get(seg_idx).add(convertLabel(levels.get("phoneme").get(cur_pho_index-2).get("label")));
        else
            matrice.get(seg_idx).add(UNDEF);

        if (cur_pho_index > 0)
            matrice.get(seg_idx).add(convertLabel(levels.get("phoneme").get(cur_pho_index-1).get("label")));
        else
            matrice.get(seg_idx).add(UNDEF);

        // Current phoneme
        matrice.get(seg_idx).add(convertLabel(levels.get("phoneme").get(cur_pho_index).get("label")));

        // Following phonemes
        if (cur_pho_index < (levels.get("phoneme").size()-1))
            matrice.get(seg_idx).add(convertLabel(levels.get("phoneme").get(cur_pho_index+1).get("label")));
        else
            matrice.get(seg_idx).add(UNDEF);

        if (cur_pho_index < (levels.get("phoneme").size()-2))
            matrice.get(seg_idx).add(convertLabel(levels.get("phoneme").get(cur_pho_index+2).get("label")));
        else
            matrice.get(seg_idx).add(UNDEF);

        // Positions
        if (!isNSS(cur_pho_index))
        {
            matrice.get(seg_idx).add(Integer.toString(cur_pho_index + 1 - pho_offset));
            matrice.get(seg_idx).add(Integer.toString(cur_syl_size - (cur_pho_index - pho_offset)));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }

    }

    protected void generateSyllablePart(int seg_idx)
    {
        assert matrice != null;
        assert matrice.get(seg_idx) != null;

        Hashtable<String, String> tmp_syl; // Tmp syllable variable (previous => current => next)

        // Previous syllable part
        if (cur_syl_index > 0)
        {
            tmp_syl = levels.get("syllable").get(cur_syl_index-1);
            matrice.get(seg_idx).add(tmp_syl.get("stress"));
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(tmp_syl.get("size"));
        }
        else
        {

            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }

        // Current syllable part
        tmp_syl = levels.get("syllable").get(cur_syl_index);
        matrice.get(seg_idx).add(tmp_syl.get("stress"));
        matrice.get(seg_idx).add(UNDEF);
        matrice.get(seg_idx).add(tmp_syl.get("size"));

        // - Position in cur word
        matrice.get(seg_idx).add(Integer.toString(cur_syl_index+1 - syl_wrd_offset));
        matrice.get(seg_idx).add(Integer.toString(cur_wrd_size - (cur_syl_index - syl_wrd_offset)));

        // - Position in cur phrase
        matrice.get(seg_idx).add(Integer.toString(cur_syl_index+1 - syl_phr_offset));
        matrice.get(seg_idx).add(Integer.toString(cur_phr_size_in_syls - (cur_syl_index - syl_phr_offset)));

        // - stress syllables (phrase)
        matrice.get(seg_idx).add(UNDEF);
        matrice.get(seg_idx).add(UNDEF);

        // - accent syllables (phrase)
        matrice.get(seg_idx).add(UNDEF);
        matrice.get(seg_idx).add(UNDEF);

        // - stress syllables (utt)
        if (stress_prev < 0)
            matrice.get(seg_idx).add(UNDEF);
        else
            matrice.get(seg_idx).add(Integer.toString(cur_syl_index - stress_prev + 1)); // stressed_syl_indexes.get(stress_prev)));

        if (stress_next < 0)
            matrice.get(seg_idx).add(UNDEF);
        else
            matrice.get(seg_idx).add(Integer.toString(stressed_syl_indexes.get(stress_next) - cur_syl_index));

        // - accent syllables (utt)
        matrice.get(seg_idx).add(UNDEF);
        matrice.get(seg_idx).add(UNDEF);

        // - vowel
        matrice.get(seg_idx).add(cur_syl_vowel);

        // Next syllable part
        if (cur_syl_index < levels.get("syllable").size() - 1)
        {
            tmp_syl = levels.get("syllable").get(cur_syl_index+1);
            matrice.get(seg_idx).add(tmp_syl.get("stress"));
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(tmp_syl.get("size"));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }
    }

    protected void generateWordPart(int seg_idx)
    {
        assert matrice != null;
        assert matrice.get(seg_idx) != null;

        Hashtable<String, String> tmp_wrd; // Tmp word variable (previous => current => next)

        // Previous word part
        if (cur_wrd_index > 0)
        {
            tmp_wrd = levels.get("word").get(cur_wrd_index-1);
            matrice.get(seg_idx).add(convertPOS(tmp_wrd));
            matrice.get(seg_idx).add(tmp_wrd.get("size"));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }

        // Current word part
        tmp_wrd = levels.get("word").get(cur_wrd_index);
        matrice.get(seg_idx).add(convertPOS(tmp_wrd));
        matrice.get(seg_idx).add(tmp_wrd.get("size"));

        // - position in phrase (TODO)
        matrice.get(seg_idx).add(Integer.toString(cur_wrd_index - wrd_offset + 1));
        matrice.get(seg_idx).add(Integer.toString(cur_phr_size_in_wrds - (cur_wrd_index - wrd_offset)));

        // - Content word (phrase) (TODO)
        matrice.get(seg_idx).add(UNDEF);
        matrice.get(seg_idx).add(UNDEF);

        // - Content word (utt) (TODO)
        matrice.get(seg_idx).add(UNDEF);
        matrice.get(seg_idx).add(UNDEF);

        // Next word part
        if (cur_wrd_index < levels.get("word").size() - 1)
        {
            tmp_wrd = levels.get("word").get(cur_wrd_index+1);
            matrice.get(seg_idx).add(convertPOS(tmp_wrd));
            matrice.get(seg_idx).add(tmp_wrd.get("size"));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }
    }

    protected void generatePhrasePart(int seg_idx)
    {
        assert matrice != null;
        assert matrice.get(seg_idx) != null;

        Hashtable<String, String> tmp_phr; // Tmp word variable (previous => current => next)


        // Previous phrase part
        if (cur_phr_index > 0)
        {
            tmp_phr = levels.get("phrase").get(cur_phr_index-1);
            matrice.get(seg_idx).add(tmp_phr.get("size_in_syls"));
            matrice.get(seg_idx).add(tmp_phr.get("size_in_words"));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }

        // Curren phrase part
        tmp_phr = levels.get("phrase").get(cur_phr_index);
        matrice.get(seg_idx).add(tmp_phr.get("size_in_syls"));
        matrice.get(seg_idx).add(tmp_phr.get("size_in_words"));

        // - position
        matrice.get(seg_idx).add(Integer.toString(cur_phr_index + 1));
        matrice.get(seg_idx).add(Integer.toString(levels.get("phrase").size() - cur_phr_index));

        // - tobi
        matrice.get(seg_idx).add(tmp_phr.get("tobi"));

        // Next phrase part
        if (cur_phr_index < levels.get("phrase").size() - 1)
        {
            tmp_phr = levels.get("phrase").get(cur_phr_index+1);
            matrice.get(seg_idx).add(tmp_phr.get("size_in_syls"));
            matrice.get(seg_idx).add(tmp_phr.get("size_in_words"));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }

    }

    protected void generateUtterancePart(int seg_idx)
    {
        assert matrice != null;
        assert matrice.get(seg_idx) != null;

        matrice.get(seg_idx).add(levels.get("utt").get(0).get("nb_syllables"));
        matrice.get(seg_idx).add(levels.get("utt").get(0).get("nb_words"));
        matrice.get(seg_idx).add(levels.get("utt").get(0).get("nb_phrases"));
    }

    protected boolean isNSS(int index)
    {
        if (levels.get("phoneme").get(index).get("label").equals("_"))
            return true;

        return false;
    }

    /* ==========================================================================================
     * # Formating
     * ========================================================================================== */
    public ArrayList<String> format()
    {
        assert matrice != null;
        String format ="%s^%s-%s+%s=%s@%s_%s"; // Phoneme format
        // Syllable format
        format += "/A:%s_%s_%s/B:%s-%s-%s@%s-%s&%s-%s#%s-%s$%s-%s!%s-%s;%s-%s|%s/C:%s+%s+%s";
        // Word format
        format += "/D:%s_%s/E:%s+%s@%s+%s&%s+%s#%s+%s/F:%s_%s";
        // Phrase format
        format += "/G:%s_%s/H:%s=%s^%s=%s|%s/I:%s_%s";
        // Utterance format
        format+= "/J:%s+%s-%s";

        ArrayList<String> labels = new ArrayList<String>();
        for (int i=0; i<matrice.size(); i++)
        {
            ArrayList<String> line = matrice.get(i);

            String cur_lab = "";

            if (nss_mask.get(i))
            {
                cur_lab = String.format(format,
                                        // Phoneme
                                        line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0),
                                        UNDEF, UNDEF,
                                        // Syllable
                                        UNDEF, UNDEF, UNDEF,
                                        UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF,
                                        UNDEF, UNDEF, UNDEF,
                                        // Word
                                        UNDEF, UNDEF,
                                        UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF,
                                        UNDEF, UNDEF,
                                        // Phrase
                                        UNDEF, UNDEF,
                                        UNDEF, UNDEF, UNDEF, UNDEF, UNDEF, UNDEF,
                                        UNDEF, UNDEF,
                                        // Utterance
                                        UNDEF, UNDEF, UNDEF);
            }
            else
            {
                cur_lab = String.format(format,
                                        // Phoneme
                                        line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0),
                                        // Syllable
                                        line.remove(0), line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0), line.remove(0),
                                        // Word
                                        line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0),
                                        // Phrase
                                        line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0), line.remove(0), line.remove(0), line.remove(0),
                                        line.remove(0), line.remove(0),
                                        // Utterance
                                        line.remove(0), line.remove(0), line.remove(0));
            }
            labels.add(cur_lab);
        }

        return labels;
    }

    /* ==========================================================================================
     * # Interface
     * ========================================================================================== */
    public void generateMatrice()
    {
        ArrayList<String> labels = new ArrayList<String>();
        cur_syl_index = 0;
        cur_wrd_index = 0;
        cur_phr_index = 0;

        pho_offset = 0;
        syl_wrd_offset = 0;
        syl_phr_offset = 0;
        wrd_offset = 0;

        // FIXME: suppose everything is ok in term of levels
        cur_syl_size = Integer.parseInt(levels.get("syllable").get(0).get("size"));
        cur_wrd_size = Integer.parseInt(levels.get("word").get(0).get("size"));
        cur_phr_size_in_wrds = Integer.parseInt(levels.get("phrase").get(0).get("size_in_words"));
        cur_phr_size_in_syls = Integer.parseInt(levels.get("phrase").get(0).get("size_in_syls"));

        listStressedPerUtterance();
        stress_prev = -1;
        stress_next = 0;
        if (stressed_syl_indexes.get(0) == 0)
            stress_next = 1;
        if (stressed_syl_indexes.size() <= stress_next)
            stress_next = -1;

        cur_syl_vowel = levels.get("syllable").get(0).get("vowel");

        matrice = new ArrayList<ArrayList<String>>();
        nss_mask = new ArrayList<Boolean>();
        for(int i=0; i<levels.get("phoneme").size(); i++) //FIXME: segments instead of phonemes
        {
            matrice.add(new ArrayList<String>());

            // Update phoneme indexes
            cur_pho_index = i;

            // Generte phonme informations
            generatePhonemePart(i);

            if (!isNSS(cur_pho_index))
            {
                // Label generation
                generateSyllablePart(i);
                generateWordPart(i);
                generatePhrasePart(i);
                nss_mask.add(false);


                // Adapt phoneme offsets + syllable indexes
                if ((cur_pho_index - pho_offset + 1) >= cur_syl_size)
                {
                    // Update current index
                    cur_syl_index++;

                    if (cur_syl_index < levels.get("syllable").size())
                    {
                        pho_offset += cur_syl_size;
                        cur_syl_size = Integer.parseInt(levels.get("syllable").get(cur_syl_index).get("size"));

                        // Update stress syllables indexes
                        if ((stress_prev < 0) ||
                            (levels.get("syllable").get(cur_syl_index-1).get("stress").equals("1")))
                        {
                            stress_prev = cur_syl_index;
                        }

                        if ((stress_next >= 0) && (cur_syl_index >= stressed_syl_indexes.get(stress_next)))
                        {
                            stress_next++;

                            if (stress_next >= stressed_syl_indexes.size())
                                stress_next = -1;
                        }
                    }

                }

                // Adapt syllable offsets + word indexes
                if ((cur_syl_index - syl_wrd_offset + 1) > cur_wrd_size)
                {
                    cur_wrd_index++;
                    if (cur_wrd_index < levels.get("word").size())
                    {
                        syl_wrd_offset += cur_wrd_size;
                        cur_wrd_size = Integer.parseInt(levels.get("word").get(cur_wrd_index).get("size"));
                    }
                }

                // Adapt word offsets + phrase indexes
                if ((cur_wrd_index - wrd_offset + 1) > cur_phr_size_in_wrds)
                {
                    cur_phr_index++;
                    if (cur_phr_index < levels.get("phrase").size())
                    {
                        wrd_offset += cur_phr_size_in_wrds;
                        syl_phr_offset += cur_phr_size_in_syls;
                        cur_phr_size_in_syls = Integer.parseInt(levels.get("phrase").get(cur_phr_index).get("size_in_syls"));
                        cur_phr_size_in_wrds = Integer.parseInt(levels.get("phrase").get(cur_phr_index).get("size_in_words"));
                    }
                }

            }
            else
            {
                pho_offset += 1;
                nss_mask.add(true);
            }

            generateUtterancePart(i);
        }
    }

    public ArrayList<String> generateLabels()
    {
        generateMatrice();
        return format();
    }

    public ArrayList<String> getLabels()
    {
        assert matrice != null;
        return format();
    }

    public String toString()
    {
        String result = "";
        ArrayList<String> labels = generateLabels();
        for (String label: labels) {
            result += label + "\n";
        }
        return result;
    }
}
