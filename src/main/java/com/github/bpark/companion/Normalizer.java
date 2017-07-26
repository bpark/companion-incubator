package com.github.bpark.companion;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ksr
 */
public class Normalizer {

    public static void main(String[] args) throws Exception {

        String path = "dict";

        IDictionary dict = new Dictionary(new File(path));
        dict.open();

        OLP olp = OLP.createInstance().withTokenizer().withPosTagger();

        List<String> popularity = IOUtils.readLines(SynonymDemo.class.getResourceAsStream("/bins/20k.txt"), StandardCharsets.UTF_8);

        List<String> normalized = new ArrayList<>();

		for (String word : popularity) {
			List<String> tags = olp.tag(word);
			String tag = tags.get(0);

			POS pos = PosType.byPennTag(tag);

			if (pos != null) {

				WordnetStemmer stemmer = new WordnetStemmer(dict);
				List<String> stems = stemmer.findStems(word, pos);
				String stem = stems != null && stems.size() > 0 ? stems.get(0) : word;
				if (!normalized.contains(stem + "," + pos.name())) {
					normalized.add(stem + "," + pos.name());
				}
			} else {
				if (!normalized.contains(word + ",")) {
					normalized.add(word + ",");
				}
			}
		}

        normalized.forEach(System.out::println);
    }
}
