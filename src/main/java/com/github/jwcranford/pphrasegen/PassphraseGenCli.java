package com.github.jwcranford.pphrasegen;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
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
@Command(name="pphrasegen", description="Generates passphrases based on a given word file.",
    mixinStandardHelpOptions = true, version="pphrasegen 0.4.0")
public final class PassphraseGenCli implements Callable<Integer> {

  private static final double DEFAULT_TARGET_ENTROPY = 75.0;
  private static final int DEFAULT_NUM_PASSPHRASES = 20;

  @Parameters(index = "0",
      description = "The file of words to use when generating a passphrase. The file contains one word per line.")
  private Path wordFile;

  @Option(names = { "-c", "--count"}, description = "Number of passphrases to generate (${DEFAULT-VALUE} by default)")
  private int count = DEFAULT_NUM_PASSPHRASES;

  @Option(names = { "-w", "--words"},
      description={
          "Number of words to include in each passphrase.",
          "By default, the number of words in each passphrase depends on the size of the input file.",
          "      # of words in file   # of words in passphrase",
          "      ------------------   ------------------------",
          "                    1024                          8",
          "                    2048                          7",
          "                    4096                          7",
          "                    8192                          6"
      })
  private int wordCount;



  @Override
  public Integer call() throws IOException {
    RandomWordGenerator primaryWordGenerator =
        new RandomWordGenerator(new SecureRandom(), Files.readAllLines(wordFile));
    final PassphraseGenerator dice = new PassphraseGenerator(primaryWordGenerator);
    if (wordCount == 0) {
      wordCount = (int) Math.ceil(primaryWordGenerator.calculateWordCount(DEFAULT_TARGET_ENTROPY));
    }
    dice.generatePassphrases(count, wordCount, System.out::println);
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new PassphraseGenCli()).execute(args);
    System.exit(exitCode);
  }

}


final class PassphraseGenerator {

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
