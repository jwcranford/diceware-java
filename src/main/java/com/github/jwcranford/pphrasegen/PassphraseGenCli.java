package com.github.jwcranford.pphrasegen;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
    mixinStandardHelpOptions = true, version="pphrasegen 0.6.0")
public final class PassphraseGenCli implements Callable<Integer> {

  private static final int DEFAULT_NUM_PASSPHRASES = 5;
  private static final String DEFAULT_WORDS_RESOURCE = "/diceware8k.txt";
  private static final int DEFAULT_WORD_LIST_SIZE_HINT = 8192;

  @Option(names = { "-f", "--file"},
      description = "The word file to use when generating a passphrase. The file should contain one word per line. If not specified, an internal copy of diceware8k.txt is used.")
  private Path wordFile = null;

  @Option(names = { "-c", "--count"}, description = "Number of passphrases to generate (${DEFAULT-VALUE} by default).")
  private int count = DEFAULT_NUM_PASSPHRASES;

  @Option(names = { "-s", "--special"}, description = "Number of special characters to insert at random locations in the generated passphrase.")
  private int specialChars = 0;

  private static final String SPECIAL = "!=$%-*./";

  @Option(names = { "-d", "--digits"}, description = "Number of digits to insert at random locations in the generated passphrase." )
  private int digits = 0;

  private static final String DIGITS = "23456789";

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
    final SecureRandom random = new SecureRandom();
    List<String> words = readWords();
    final RandomWordGenerator primaryWordGenerator = new RandomWordGenerator(random, words);
    final PassphraseGenerator dice = new PassphraseGenerator(primaryWordGenerator);
    if (wordCount == 0) {
      wordCount = (int) Math.ceil(primaryWordGenerator.defaultWordCount());
    }
    dice.generatePassphrases(count, wordCount,
        ss -> {
          addRandomCharRepeatedly(random, SPECIAL, specialChars, ss);
          addRandomCharRepeatedly(random, DIGITS, digits, ss);
          System.out.println(String.join(" ", ss));
        });
    return 0;
  }

  private List<String> readWords() throws IOException {
    if (wordFile != null) {
      return Files.readAllLines(wordFile);
    }
    try (InputStream resourceAsStream = getClass().getResourceAsStream(DEFAULT_WORDS_RESOURCE)) {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
      List<String> words = new ArrayList<>(DEFAULT_WORD_LIST_SIZE_HINT);
      String s = null;
      while ((s = bufferedReader.readLine()) != null) {
        words.add(s);
      }
      return words;
    }
  }

  static void addRandomCharRepeatedly(Random random, String chars, int times, List<String> pphrase) {
    if (times > 0) {
      RandomWordGenerator randomWordGenerator = new RandomWordGenerator(random, Arrays.asList(chars.split("")));
      for (int i = 0; i < times; i++) {
        pphrase.add(random.nextInt(pphrase.size() + 1), randomWordGenerator.get());
      }
    }
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

  List<String> nextPassphrase(int numberWords) {
    return Stream.generate(primaryWordGenerator).limit(numberWords).collect(Collectors.toList());
  }

  void generatePassphrases(int numPhrases, int numWords, Consumer<List<String>> callback) {
    Stream.generate(() -> nextPassphrase(numWords)).limit(numPhrases)
        .forEach(callback);
  }
}

/**
 * Calculates the entropy in a dictionary of a given size.
 */
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

  private static final double DEFAULT_TARGET_ENTROPY = 75.0;

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
    return words.get(randomWordIndex());
  }

  private int randomWordIndex() {
    return random.nextInt(entropyCalculator.getEffectiveWordListSize());
  }

  double defaultWordCount() {
    return DEFAULT_TARGET_ENTROPY / entropyCalculator.getEntropyPerWord();
  }

}
