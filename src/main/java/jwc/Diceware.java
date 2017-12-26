package jwc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line application that generates a passphrase using the diceware
 * method described at http://world.std.com/%7Ereinhold/dicewarefaq.html#computer.
 * 
 * @author jwcranford
 */
public final class Diceware {

	private static final int DICEWARE_SIZE = 8192;

	private static final String DICEWARE_RESOURCE = "/diceware8k.txt";
	
	private final List<String> words = new ArrayList<>(DICEWARE_SIZE);
	private final SecureRandom rand = new SecureRandom();
	
	private Diceware() throws IOException {
		try (final BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(DICEWARE_RESOURCE)))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				words.add(line);
			}
		}
	}
	
	private String nextWord() {
		return words.get(rand.nextInt(DICEWARE_SIZE));
	}

	private static void usage(PrintStream out) {
		out.println("diceware -h | --help");
		out.println("	Prints this summary.");
		out.println("diceware [w [p]]");
		out.println("	Generates p phrases of w words each. ");
		out.println("	Default is 20 phrases of 5 words each.");
	}

	// Takes an optional argument for the number of words in the passphrase.
	public static void main(String[] args) throws IOException {
		int numWords = 5;
		int numPhrases = 20;
		
		if (args != null && args.length >= 1) {
			int argi=0;
			if ("-h".equals(args[argi]) || "--help".equals(args[argi])) {
				usage(System.err);
				System.exit(1);
			}
			numWords = Integer.parseInt(args[argi++]);
			if (args.length >= 2) {
				numPhrases = Integer.parseInt(args[argi++]);
			}
		}
		
		final Diceware dice = new Diceware();
		for (int count=0; count<numPhrases; count++) {
			for (int i=0; i<numWords; i++) {
				System.out.print(dice.nextWord());
				System.out.print(' ');
			}
			System.out.println();
		}
	}

}
