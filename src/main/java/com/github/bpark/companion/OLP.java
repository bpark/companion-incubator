package com.github.bpark.companion;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author ksr
 */
public class OLP {

    private TokenizerME tokenizerME;
    private NameFinderME nameFinderME;
    private POSTaggerME posTaggerME;
    private SentenceDetectorME sentenceDetectorME;
    private Parser parser;


    public static OLP createInstance() {
        return new OLP();
    }

    private OLP() {
    }

    public OLP withNameFinder() {
        try {
            nameFinderME = initNameFinder();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OLP withPosTagger() {
        try {
            posTaggerME = initPosTagger();
            if (tokenizerME == null) {
                tokenizerME = initTokenizer();
            }
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OLP withTokenizer() {
        try {
            if (tokenizerME == null) {
                tokenizerME = initTokenizer();
            }
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OLP withSentenceDetector() {
        try {
            sentenceDetectorME = initSentenceDetector();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OLP withParser() {
        try {
            parser = initParser();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> sentence(String text) {
        return Arrays.asList(sentenceDetectorME.sentDetect(text));
    }

    public List<String> tokenize(String sentence) {
        return Arrays.asList(tokenizerME.tokenize(sentence));
    }

    public List<String> tag(String sentence) {
        return Arrays.asList(posTaggerME.tag(tokenizerME.tokenize(sentence)));
    }

    public List<Parse> parse(String sentence) {
        Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);
        return Arrays.asList(topParses);
    }

    private TokenizerME initTokenizer() throws IOException {
        try (InputStream modelInToken = NWorkbench.class.getResourceAsStream("/bins/en-token.bin")) {
            TokenizerModel modelToken = new TokenizerModel(modelInToken);
            return new TokenizerME(modelToken);
        }
    }

    private NameFinderME initNameFinder() throws IOException {
        try (InputStream modelIn = NWorkbench.class.getResourceAsStream("/bins/en-ner-person.bin")) {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            return new NameFinderME(model);
        }
    }

    private POSTaggerME initPosTagger() throws IOException {
        try (InputStream modelIn = NWorkbench.class.getResourceAsStream("/bins/en-pos-maxent.bin")) {
            POSModel model = new POSModel(modelIn);
            return new POSTaggerME(model);
        }
    }

    private SentenceDetectorME initSentenceDetector() throws IOException {
        try (InputStream modelIn = NWorkbench.class.getResourceAsStream("/bins/en-sent.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        }
    }

    private Parser initParser() throws IOException {
        try (InputStream modelIn = NWorkbench.class.getResourceAsStream("/bins/en-parser-chunking.bin")) {
            ParserModel model = new ParserModel(modelIn);
            return ParserFactory.create(model);
        }
    }

}
