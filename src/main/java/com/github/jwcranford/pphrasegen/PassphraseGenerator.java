package com.github.jwcranford.pphrasegen;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

/**
 * Command-line application that generates a passphrase using the diceware
 * method described at http://www.diceware.com. See especially
 * http://world.std.com/%7Ereinhold/dicewarefaq.html#computer.
 *
 * @author Jonathan W. Cranford
 */
public final class PassphraseGenerator {

  private static final double DEFAULT_TARGET_ENTROPY = 75.0;
  private static final int DEFAULT_NUM_PASSPHRASES = 20;

  private final SecureRandom rand = new SecureRandom();
  private final RandomWordGenerator primaryWordGenerator;

  private PassphraseGenerator(final String file) throws IOException {
    this(Files.readAllLines(Paths.get(file)));
  }

  private PassphraseGenerator(final List<String> words) {
    primaryWordGenerator = new RandomWordGenerator(rand, words);
  }

  /** Generates next passphrase with given number of words. */
  private String nextPassphrase(int numberWords) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numberWords; i++) {
      sb.append(primaryWordGenerator.nextWord()).append(' ');
    }
    return sb.toString();
  }

  private static void usage(PrintStream out) {
    out.println("pphrasegen -h | --help");
    out.println("   Prints this summary.");
    out.println("pphrasegen file [w [p]]");
    out.println("   Generates p passphrases of w words each from the given file. ");
    out.format("   The default behavior is to generate %d passphrases, with the number",
        DEFAULT_NUM_PASSPHRASES);
    out.println();
    out.println("   of words in each passphrase depending on the size of the input file.");
    out.println();
    out.println("      # of words in file   # of words in passphrase");
    out.println("      ------------------   ------------------------");
    out.println("                    1024                          8");
    out.println("                    2048                          7");
    out.println("                    4096                          7");
    out.println("                    8192                          6");
  }


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
    final PassphraseGenerator dice = new PassphraseGenerator(file);
    int numWords = (int) Math.ceil(dice.primaryWordGenerator.entropyCalculator.calculateWordCount(DEFAULT_TARGET_ENTROPY));
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


/**
 * Calculates the entropy in a dictionary of a given size.
 */
// visible for unit tests
class EntropyCalculator {

  private final int dictionarySize;

  EntropyCalculator(final int dictionarySize) {
    this.dictionarySize = dictionarySize;
  }

  double calculateWordCount(double targetEntropy) {
    return targetEntropy / getEntropyPerWord();
  }

  int getEntropyPerWord() {
    return (int) (Math.log(dictionarySize) / Math.log(2));
  }

  /**
   * @return the nearest power of two no bigger than the dictionary size
   */
  int getEffectiveWordListSize() {
    return 1 << getEntropyPerWord();
  }

}


class RandomWordGenerator {
  private final List<String> words;
  private final SecureRandom random;
  final EntropyCalculator entropyCalculator;

  RandomWordGenerator(SecureRandom random, List<String> words) {
    this.words = words;
    this.random = random;
    this.entropyCalculator = new EntropyCalculator(words.size());
  }

  /** Generates next word at random from word list. */
  String nextWord() {
    return words.get(random.nextInt(entropyCalculator.getEffectiveWordListSize()));
  }
}
