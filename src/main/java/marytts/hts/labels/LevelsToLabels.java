package marytts.hts.labels;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Transform levels to HTS compatible labels
 *
 * For now, the output alphabet is a modified arpabet one (including modifiers).
 *
 * @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class LevelsToLabels
{
    private ArrayList<ArrayList<String>> matrice;
    private ArrayList<Boolean> nss_mask;
    private ArrayList<Integer> stressed_syl_indexes;
    private Hashtable<String, ArrayList<Hashtable<String, String>>> levels;

    private static final String UNDEF = "x";

    private String cur_syl_vowel;
    private int cur_pho_index;
    private int cur_syl_index;
    private int cur_wrd_index;
    private int cur_phr_index;

    private int pho_offset;
    private int syl_wrd_offset;
    private int syl_phr_offset;
    private int wrd_offset;
    private int phr_offset;
    private int stress_prev;
    private int stress_next;
    private int cur_syl_size;
    private int cur_wrd_size;
    private int cur_phr_size_in_syls;
    private int cur_phr_size_in_wrds;

    private Hashtable<String, String> sampa2arpabet;
    private Hashtable<String, String> modifier;
    private Hashtable<String, String> pos_converter;

    /* ==========================================================================================
     * # Constructors
     * ========================================================================================== */
    public LevelsToLabels(Hashtable<String, ArrayList<Hashtable<String, String>>> levels)
    {
        initPhConverter();
        initPOSConverter();
        initModifier();
        this.levels = levels;
    }

    /* ==========================================================================================
     * # Conversion helpers
     * ========================================================================================== */
    private void initPhConverter()
    {
        sampa2arpabet = new Hashtable<String, String>();

        // Vowels
        sampa2arpabet.put("A", "aa");
        sampa2arpabet.put("AI", "ay");
        sampa2arpabet.put("E", "eh");
        sampa2arpabet.put("EI", "ey");
        sampa2arpabet.put("I", "ih");
        sampa2arpabet.put("O", "ao");
        sampa2arpabet.put("OI", "oy");
        sampa2arpabet.put("U", "uh");
        sampa2arpabet.put("aU", "aw");
        sampa2arpabet.put("i", "iy");
        sampa2arpabet.put("u", "uw");
        sampa2arpabet.put("@", "ax");
        sampa2arpabet.put("@U", "ow");
        sampa2arpabet.put("V", "ah");
        sampa2arpabet.put("{", "ae");

        sampa2arpabet.put("j", "y");

        sampa2arpabet.put("D", "dh");
        sampa2arpabet.put("N", "ng");
        sampa2arpabet.put("S", "sh");
        sampa2arpabet.put("T", "th");
        sampa2arpabet.put("Z", "zh");
        sampa2arpabet.put("b", "b");
        sampa2arpabet.put("d", "d");
        sampa2arpabet.put("dZ", "jh"); // FIXME: what it is ?
        sampa2arpabet.put("f", "f");
        sampa2arpabet.put("g", "g");
        sampa2arpabet.put("h", "hh");
        sampa2arpabet.put("k", "k");
        sampa2arpabet.put("l", "l");
        sampa2arpabet.put("m", "m");
        sampa2arpabet.put("n", "n");
        sampa2arpabet.put("p", "p");
        sampa2arpabet.put("r", "r");
        sampa2arpabet.put("r=", "r"); // FIXME: sure ?
        sampa2arpabet.put("s", "s");
        sampa2arpabet.put("t", "t");
        sampa2arpabet.put("tS", "ch");
        sampa2arpabet.put("v", "v");
        sampa2arpabet.put("w", "w");
        sampa2arpabet.put("z", "z");

        sampa2arpabet.put("_", "pau");

        sampa2arpabet.put("4", "dx"); // FIXME: ?
    }

    private void initModifier()
    {
        modifier = new Hashtable<String, String>();
        modifier.put(":", "LONG");
        modifier.put("~", "NASAL");
    }

    private void initPOSConverter()
    {
        pos_converter = new Hashtable<String, String>();
        pos_converter.put("``", "STARTQUOTES");
        pos_converter.put("''", "ENDQUOTES");
        pos_converter.put("'", "QUOTE");
    }

    // FIXME: sampa to arpabet
    protected String convertLabel(String label)
    {
        String final_label = label;
        if (sampa2arpabet.containsKey(label))
            final_label = sampa2arpabet.get(label);

        // Dealing with nasalisation
        for (String key: modifier.keySet())
        {
            final_label = final_label.replaceAll(key, modifier.get(key));
        }

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

    private boolean isNSS(int index)
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
