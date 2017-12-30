package com.github.jwcranford;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line application that generates a passphrase using the diceware
 * method described at http://www.diceware.com. See especially
 * http://world.std.com/%7Ereinhold/dicewarefaq.html#computer.
 * 
 * @author Jonathan W. Cranford
 */
public final class Diceware {

    private static final int MAX_DICEWARE_SIZE = 8192;
    private static final double DEFAULT_TARGET_ENTROPY = 75.0;
    private static final int DEFAULT_NUM_PASSPHRASES = 20;

    private final List<String> words = new ArrayList<>(MAX_DICEWARE_SIZE);

    // package private for unit tests
    final int effectiveWordListSize;
    final int defaultWordsInPassphrase;

    private final SecureRandom rand = new SecureRandom();

    public Diceware(final InputStream in, final double targetEntropy) throws IOException {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                words.add(line);
            }
        }
        final int entropyPerWord = (int) (Math.log(words.size()) / Math.log(2));
        this.effectiveWordListSize = 1 << entropyPerWord;
        this.defaultWordsInPassphrase = (int) Math.ceil(targetEntropy / entropyPerWord);
    }

    /** Generates next word at random from word list. */
    public String nextWord() {
        return words.get(rand.nextInt(effectiveWordListSize));
    }

    /** Generates next passphrase with given number of words. */
    public String nextPassphrase(int numberWords) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberWords; i++) {
            sb.append(nextWord()).append(' ');
        }
        return sb.toString();
    }

    /** Generates next passphrase with default number of words. */
    public String nextPassphrase() {
        return nextPassphrase(defaultWordsInPassphrase);
    }

    private static void usage(PrintStream out) {
        out.println("diceware-java -h | --help");
        out.println("   Prints this summary.");
        out.println("diceware-java file [w [p]]");
        out.println("   Generates p passphrases of w words each from the given file. ");
        out.format("   The default behavior is to generate %d passphrases with enough words each",
                DEFAULT_NUM_PASSPHRASES);
        out.println();
        out.format("   to get at least %.0f bits of entropy in each passphrase, according to",
                DEFAULT_TARGET_ENTROPY);
        out.println();
        out.println("   the size of the word list.");
        out.println();
        out.println("      # of words in list   # of words in passphrase");
        out.println("      ------------------   ------------------------");
        out.println("                    1024                          8");
        out.println("                    2048                          7");
        out.println("                    4096                          7");
        out.println("                    8192                          6");
    }

    // Takes an optional argument for the number of words in the passphrase.
    public static void main(String[] args) throws IOException {
        int numPhrases = DEFAULT_NUM_PASSPHRASES;

        if (args == null || args.length < 1) {
            System.err.println("Missing file argument!");
            usage(System.err);
            System.exit(2);
        }

        int nextArg=0;
        if ("-h".equals(args[nextArg]) || "--help".equals(args[nextArg])) {
            usage(System.err);
            System.exit(1);
        }
        final String file = args[nextArg++];
        final Diceware dice = new Diceware(new FileInputStream(file), DEFAULT_TARGET_ENTROPY);
        int numWords = dice.defaultWordsInPassphrase;
        if (nextArg < args.length) {
            numWords = Integer.parseInt(args[nextArg++]);
            if (nextArg < args.length) {
                numPhrases = Integer.parseInt(args[nextArg++]);
            }
        }

        for (int count = 0; count < numPhrases; count++) {
            System.out.print(dice.nextPassphrase(numWords));
            System.out.println();
        }
    }

}
