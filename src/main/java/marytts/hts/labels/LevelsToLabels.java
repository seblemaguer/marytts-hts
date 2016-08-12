package marytts.hts.labels;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform levels to HTS compatible labels
 *
 * For now, the output alphabet is a modified arpabet one (including modifiers).
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class LevelsToLabels
{
    private final Logger logger = LoggerFactory.getLogger(LevelsToLabels.class);
    protected boolean has_duration;
    protected ArrayList<ArrayList<String>> matrice;
    protected ArrayList<Boolean> nss_mask;
    protected ArrayList<Integer> stressed_syl_indexes;
    protected Hashtable<String, ArrayList<Hashtable<String, String>>> levels;

    protected static final String UNDEF = "x";

    protected String cur_syl_vowel;
    protected int cur_pho_index;
    protected int cur_syl_index;
    protected int cur_wrd_index;
    protected int cur_phr_index;

    protected int pho_offset;
    protected int syl_wrd_offset;
    protected int syl_phr_offset;
    protected int wrd_offset;
    protected int phr_offset;
    protected int stress_prev;
    protected int stress_next;
    protected int cur_syl_size;
    protected int cur_wrd_size;
    protected int cur_phr_size_in_syls;
    protected int cur_phr_size_in_wrds;

    protected Hashtable<String, String> alphabet_converter;
    protected Hashtable<String, String> modifier;
    protected Hashtable<String, String> pos_converter;

    /* ==========================================================================================
     * # Constructors
     * ========================================================================================== */
    public LevelsToLabels(Hashtable<String, ArrayList<Hashtable<String, String>>> levels)
    {
        alphabet_converter = LevelsToLabels.initPhConverter();
        modifier = LevelsToLabels.initModifier();
        has_duration = false;
        initPOSConverter();
        this.levels = levels;
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
        alphabet_converter.put("U", "oo");
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
        alphabet_converter.put("s", "s");
        alphabet_converter.put("t", "t");
        alphabet_converter.put("tS", "ch");
        alphabet_converter.put("v", "v");
        alphabet_converter.put("w", "w");
        alphabet_converter.put("x", "xx"); // Double x in order to not get mistaken with undefined value
        alphabet_converter.put("z", "z");

        alphabet_converter.put("_", "pau");

        alphabet_converter.put("2", "eu");
        alphabet_converter.put("4", "dx");
        alphabet_converter.put("6", "er");
        alphabet_converter.put("9", "oe");
        alphabet_converter.put("?", "dt");

        return alphabet_converter;
    }

    public static Hashtable<String, String> initModifier()
    {
        Hashtable<String, String> modifier = new Hashtable<String, String>();

        modifier.put(":", "LONG");
        modifier.put("~", "NASAL");
        modifier.put("=", "SYLLABICITY");

        return modifier;
    }

    protected void initPOSConverter()
    {
        pos_converter = new Hashtable<String, String>();
        pos_converter.put("``", "STARTQUOTES");
        pos_converter.put("''", "ENDQUOTES");
        pos_converter.put("'", "QUOTE");
        pos_converter.put("$,", "COMMA");
        pos_converter.put("$(", "BRACKETS");
        pos_converter.put("$.", "DOT");
    }

    // FIXME: sampa to arpabet
    protected String convertLabel(String label)
    {
        String final_label = label;

        // Strip the modifier just to convert the label
        String tmp_label = label;
        for (String key: modifier.keySet())
        {
            tmp_label = tmp_label.replaceAll(key, "");
        }

        if (alphabet_converter.containsKey(tmp_label))
            final_label = final_label.replaceAll(Pattern.quote(tmp_label), alphabet_converter.get(tmp_label));
        else
            logger.debug("<{}> is not in the map", tmp_label);

        // Dealing with nasalisation
        for (String key: modifier.keySet())
        {
            final_label = final_label.replaceAll(key, modifier.get(key));
        }

        logger.debug("converting <{}> to <{}>", label, final_label);
        return final_label;
    }

    protected String convertPOS(String pos_label)
    {
        if (pos_converter.containsKey(pos_label))
            return pos_converter.get(pos_label);

        return pos_label;
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
        if (levels.get("phoneme").get(cur_pho_index).containsKey("duration"))
        {
            has_duration = true;
            matrice.get(seg_idx).add(levels.get("phoneme").get(cur_pho_index).get("duration"));
        }

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
            matrice.get(seg_idx).add(convertPOS(tmp_wrd.get("pos")));
            matrice.get(seg_idx).add(tmp_wrd.get("size"));
        }
        else
        {
            matrice.get(seg_idx).add(UNDEF);
            matrice.get(seg_idx).add(UNDEF);
        }

        // Current word part
        tmp_wrd = levels.get("word").get(cur_wrd_index);
        matrice.get(seg_idx).add(convertPOS(tmp_wrd.get("pos")));
        matrice.get(seg_idx).add(tmp_wrd.get("size"));

        // - position in phrase
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
            matrice.get(seg_idx).add(convertPOS(tmp_wrd.get("pos")));
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
        String format = "";
        if (has_duration)
            format = "%d\t%d\t"; // segment if duration

        // Phoneme format
        format = format + "%s^%s-%s+%s=%s@%s_%s";
        // Syllable format
        format += "/A:%s_%s_%s/B:%s-%s-%s@%s-%s&%s-%s#%s-%s$%s-%s!%s-%s;%s-%s|%s/C:%s+%s+%s";
        // Word format
        format += "/D:%s_%s/E:%s+%s@%s+%s&%s+%s#%s+%s/F:%s_%s";
        // Phrase format
        format += "/G:%s_%s/H:%s=%s^%s=%s|%s/I:%s_%s";
        // Utterance format
        format+= "/J:%s+%s-%s";

        ArrayList<String> labels = new ArrayList<String>();
        int start = 0;
        int end = 0;
        for (int i=0; i<matrice.size(); i++)
        {
            ArrayList<String> line = matrice.get(i);

            String cur_lab = "";

            if (has_duration)
            {
                String dur = line.remove(0);
                end = start + Integer.parseInt(dur) * 10000;
                if (nss_mask.get(i))
                {
                    cur_lab = String.format(format,
                                            start, end,
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
                                            start, end,
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

                start = end;
            }
            else
            {
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
