package com.github.bpark.companion;

import com.opencsv.CSVReader;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ksr
 */
public class SynonymDemo {

    private IDictionary dictionary;

    private List<PopularWord> popularWords;

    public SynonymDemo() {
        try {
            createPopularWordsIndex();
            initDictionary();

            OLP olp = OLP.createInstance().withPosTagger().withTokenizer();
            List<String> tokens = olp.tokenize("Beyond more popular stops are strange and exciting attractions with stories to tell.");
            List<String> tagged = olp.tag("Beyond more popular stops are strange and exciting attractions with stories to tell.");

            int index = 0;

            for (String token : tokens) {

                String tag = tagged.get(index);

                Map<String, Integer> synonymsMap = buildPopularitySynonyms(token, PosType.byPennTag(tag));

                Map<String, Double> weightsMap = calculateWeights(synonymsMap);

                weightsMap.forEach((k,v) -> {
                    System.out.println(k + " - " + v);
                });

                System.out.println();

                index++;
            }

            close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createPopularWordsIndex() throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(SynonymDemo.class.getResourceAsStream("/bins/20knorm.txt")));

        popularWords = reader.readAll().stream().map(line -> new PopularWord(line[0], StringUtils.isNotEmpty(line[1]) ? POS.valueOf(line[1]) : null)).collect(Collectors.toList());
    }

    private void initDictionary() throws Exception {
        String path = "dict";
        dictionary = new Dictionary(new File(path));
        dictionary.open();
    }

    private void close() {
        dictionary.close();
    }

    private Map<String, Integer> buildPopularitySynonyms(String word, POS pos) {

        WordnetStemmer stemmer = new WordnetStemmer(dictionary);
        List<String> stems = stemmer.findStems(word, pos);
        String stem = stems != null && stems.size() > 0 ? stems.get(0) : word;
        System.out.println("Stem: " + stem + ", pos: " + pos);

        Map<String, Integer> synonymMap = new HashMap<>();

        if (pos != null) {

            IIndexWord idxWord = dictionary.getIndexWord(stem, pos);

            if (idxWord != null && idxWord.getWordIDs() != null && idxWord.getWordIDs().size() > 0) {

                IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
                IWord dictionaryWord = dictionary.getWord(wordID);
                ISynset synset = dictionaryWord.getSynset();

                List<String> synonyms = synset.getWords().stream().map(IWord::getLemma).collect(Collectors.toList());

                synonyms.forEach(syn -> {
                    int popularityIndex = popularWords.indexOf(new PopularWord(syn, pos));
                    if (popularityIndex == -1) {
                        popularityIndex = popularWords.indexOf(new PopularWord(syn, null));
                    }
                    if (popularityIndex >= 0) {
                        synonymMap.put(syn, popularityIndex);
                    }
                });
            }
        }

        return synonymMap;
    }

    private Map<String, Double> calculateWeights(Map<String, Integer> priorityMap) {
        double total = priorityMap.values().stream().mapToDouble(i -> i).sum();
        Map<String, Double> weightedMap = new HashMap<>();
        priorityMap.forEach((key, value) -> weightedMap.put(key, total / value));

        double totalWeight = weightedMap.values().stream().mapToDouble(i -> i).sum();
        weightedMap.forEach((key, value) -> weightedMap.put(key, value / totalWeight));

        return weightedMap;
    }

    public static void main(String[] args) throws Exception {
        new SynonymDemo();

    }
}
