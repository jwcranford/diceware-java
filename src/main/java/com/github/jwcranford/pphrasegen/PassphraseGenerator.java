package com.github.jwcranford.pphrasegen;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private final Supplier<String> primaryWordGenerator;

  PassphraseGenerator(Supplier<String> primaryWordGenerator) {
    this.primaryWordGenerator = primaryWordGenerator;
  }

  /** Generates next passphrase with given number of words. */
  private String nextPassphrase(int numberWords) {
    return Stream.generate(primaryWordGenerator).limit(numberWords).collect(Collectors.joining(" "));
  }

  void generatePassphrases(int numPhrases, int numWords, Consumer<String> passphraseCallback) {
    Stream.generate(() -> nextPassphrase(numWords)).limit(numPhrases)
        .forEach(passphraseCallback);
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
      usage(System.out);
      System.exit(0);
    }
    final String file = args[nextArg++];
    RandomWordGenerator primaryWordGenerator = new RandomWordGenerator(new SecureRandom(), Files.readAllLines(Paths.get(file)));
    final PassphraseGenerator dice = new PassphraseGenerator(primaryWordGenerator);
    int numWords = (int) Math.ceil(primaryWordGenerator.calculateWordCount(DEFAULT_TARGET_ENTROPY));
    if (nextArg < args.length) {
      numWords = Integer.parseInt(args[nextArg++]);
      if (nextArg < args.length) {
        numPhrases = Integer.parseInt(args[nextArg++]);
      }
    }

    dice.generatePassphrases(numPhrases, numWords, System.out::println);
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


class RandomWordGenerator implements Supplier<String> {
  private final List<String> words;
  private final Random random;
  private final EntropyCalculator entropyCalculator;

  RandomWordGenerator(Random random, List<String> words) {
    this.words = words;
    this.random = random;
    this.entropyCalculator = new EntropyCalculator(words.size());
  }

  /** Generates next word at random from word list. */
  public String get() {
    return words.get(random.nextInt(entropyCalculator.getEffectiveWordListSize()));
  }

  double calculateWordCount(double targetEntropy) {
    return targetEntropy / entropyCalculator.getEntropyPerWord();
  }
}
