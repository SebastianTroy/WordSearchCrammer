package troy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 * TODO need to store the known words in a map, String (word) key, Struct value, start point to end point of the word. This way we can check
 * if a word already exists and not credit replicas and we can highlight words in our GUI when they are selected. This will require some
 * reworking of how we get "lines" and how we iterate through all affected areas. it also requires some checking if words cross over a new
 * change and removing them from the list (should be easy if our struct is a line defined by a start and end point which we can crawl
 * between to see if our point lies within it.)
 * 
 * TODO My current plan is to simple compare before/after scores for changes and if an improvement is made perform a slow and expensive
 * complete re-evaluation of current state (no need to be clever, doesn't happen often enough!)
 * 
 * @author Sebastian Troy
 *
 */
public class WordSearchCrammer extends Thread {

	private static final int MIN_WORD_LENGTH = 3;

	private final Dictionary validWords;
	private final Random rand = new Random();
	private boolean keepSearching = true;
	private final int size;
	private final char[][] wordSearchGrid;

	private final SimpleStringProperty numGenerationsStringProperty = new SimpleStringProperty("0");
	private final SimpleStringProperty numImprovmentsStringProperty = new SimpleStringProperty("0");
	private final SimpleStringProperty[][] wordSearchGridProperty;
	private final SimpleListProperty<String> wordsListProperty = new SimpleListProperty<String>();

	private final Map<String, ArrayList<Line>> knownWords = new HashMap<String, ArrayList<Line>>();

	public WordSearchCrammer(int size) {
		validWords = new Dictionary("dictionary.txt");
		this.size = size;

		wordSearchGridProperty = new SimpleStringProperty[size][size];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				wordSearchGridProperty[x][y] = new SimpleStringProperty(Character.toString(randChar()));
			}
		}

		wordSearchGrid = new char[size][size];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				wordSearchGrid[x][y] = randChar();
			}
		}
	}

	public Property<ObservableList<String>> getObservableWordListProperty() {
		return wordsListProperty;
	}

	public final ObservableValue<String>[][] getObservableCharacterGridStringProperties() {
		return wordSearchGridProperty;
	}

	public final ObservableValue<String> getNumberOfGenerationsStringProperty() {
		return numGenerationsStringProperty;
	}

	public final ObservableValue<String> getNumberOfImprovementsStringProperty() {
		return numImprovmentsStringProperty;
	}

	public final void stopSearching() {
		keepSearching = false;
	}

	@Override
	public final void run() {
		mapAllWordsInGrid();
		// TODO update of GUI

		int generations = 0;
		int improvements = 0;
		
		// Each loop is a 'generation'
		while (keepSearching) {
			// Setup mutations, calculate affected areas & recorde before/after state
			int numModifications = rand.nextInt(1) + 1;

			Point[] pointsToModify = new Point[numModifications];
			char[] replacementCharacters = new char[numModifications];
			char[] originalCharacters = new char[numModifications];
			Line[][] affectedLines = new Line[numModifications][4];

			for (int i = 0; i < numModifications; i++) {
				pointsToModify[i] = new Point(rand.nextInt(size), rand.nextInt(size));
				replacementCharacters[i] = randChar();
				originalCharacters[i] = wordSearchGrid[pointsToModify[i].x][pointsToModify[i].y];
			}

			for (int index = 0; index < numModifications; index++) {
				affectedLines[index] = getLines(pointsToModify[index]);
			}

			// Measure current state
			int currentFitness = 0;
			for (Line[] affectedLinesForPoint : affectedLines) { // TODO also accept backwards lines
				Set<Word> wordsInLine = getWordsInLines(affectedLinesForPoint);
				for (Word word : wordsInLine) {
					currentFitness += getFitnessOfWord(word);
				}
			}

			// Enact the mutations
			for (int i = 0; i < numModifications; i++) {
				wordSearchGrid[pointsToModify[i].x][pointsToModify[i].y] = replacementCharacters[i];
			}

			// Measure mutated state
			int mutatedFitness = 0;
			// TODO recalculate current fitness (now mutations have been applied... perhaps functionify the above!)

			// Modify current state as necessary
			if (mutatedFitness < currentFitness) {
				// Undo the mutations
				for (int i = 0; i < numModifications; i++) {
					wordSearchGrid[pointsToModify[i].x][pointsToModify[i].y] = originalCharacters[i];
				}
			} else {
				if (mutatedFitness > currentFitness) {
					improvements++;
				}
				mapAllWordsInGrid();
				// TODO update of GUI
			}

			generations++;
		}
	}

	private final int getFitnessOfWord(Word word) {
		int fitnessToReturn = 0;

		fitnessToReturn += word.line.length * (word.line.length - (MIN_WORD_LENGTH - 1));
		// TODO reward the use of interesting letters, score them like scrabble!

		return fitnessToReturn;
	}

	private Set<Word> getWordsInLines(Line[] lines) {
		HashSet<Word> wordsToReturn = new HashSet<Word>();

		for (Line line : lines) {
			if (line.length < MIN_WORD_LENGTH) {
				continue;
			}

			StringBuilder lineStringBuilder = new StringBuilder(line.length);
			line.forEachPoint(point -> {
				lineStringBuilder.append(this.wordSearchGridProperty[point.x][point.y].getValue());
			});

			for (int firstCharIndex = 0; firstCharIndex < line.length; firstCharIndex++) {
				for (int lastCharIndex = firstCharIndex + (MIN_WORD_LENGTH - 1); lastCharIndex < line.length; lastCharIndex++) {
					CharSequence possibleWord = lineStringBuilder.subSequence(firstCharIndex, lastCharIndex);

					// First check if the substring is a word in itself
					if (validWords.contains(possibleWord)) {
						wordsToReturn.add(new Word(line.getSubLine(firstCharIndex, lastCharIndex), lineStringBuilder.substring(firstCharIndex, lastCharIndex)));
					}

					// Optimisation, if the substring isn't even the start of any words, no need to keep checking longer substrings
					if (!validWords.containsWordsStartingWith(possibleWord)) {
						firstCharIndex++;
					}
				}
			}
		}

		return wordsToReturn;
	}

	/**
	 * Returns a list of all {@link Line}s passing through a specified {@link Point} in the grid.
	 * 
	 * @param point {@link Point} through which all lines pass through
	 * @return A list of {@link Line}s which pass through the specified {@link Point}.
	 */
	private final Line[] getLines(Point point) {
		Line[] linesToReturn = new Line[4];

		int smallestAxisValue = Math.min(point.x, point.y);
		Point lineStart, lineEnd;

		// TODO work out why some lines are too long (size might need to be (size - 1)...)
		for (Line.Direction direction : Line.Direction.values()) {
			switch (direction) {
				case Vertical: {
					lineStart = new Point(point.x, 0);
					lineEnd = new Point(point.x, size - 1);
					linesToReturn[0] = new Line(lineStart, lineEnd);
					break;
				}
				case Horizontal: {
					lineStart = new Point(0, point.y);
					lineEnd = new Point(size - 1, point.y);
					linesToReturn[1] = new Line(lineStart, lineEnd);
					break;
				}
				case DiagonalUp: {
					lineStart = new Point(Math.max(0, (point.x + point.y) - (size - 1)), Math.min(size - 1, point.x + point.y));
					lineEnd = new Point(lineStart.y, lineStart.x);
					linesToReturn[2] = new Line(lineStart, lineEnd);
					break;
				}
				case DiagonalDown: {
					lineStart = new Point(point.x - smallestAxisValue, point.y - smallestAxisValue);
					lineEnd = new Point((size - 1) - lineStart.y, (size - 1) - lineStart.x);
					linesToReturn[3] = new Line(lineStart, lineEnd);
					break;
				}
			}
		}

		return linesToReturn;
	}

	/**
	 * Clears the currently known set of words and checks the entire {@link #wordSearchGrid} for anything that is in {@link #validWords}.
	 */
	private final void mapAllWordsInGrid() {
		knownWords.clear();
		for (int i = 0; i < size; i++) {
			Point p = new Point(i, i);
			for (Line line : getLines(p)) {
				line.forEachSubline(subLine -> {
					if (subLine.length > MIN_WORD_LENGTH) {
						String stringForLine = getStringForLine(subLine);

						if (validWords.contains(stringForLine)) {
							if (!knownWords.containsKey(stringForLine)) {
								knownWords.put(stringForLine, new ArrayList<Line>());
							}

							ArrayList<Line> instancesOfWord = knownWords.get(stringForLine);

							// We are going to repeat a diagonal line repeatedly, so make sure we don't allow direct repeats!
							if (!instancesOfWord.contains(line)) {
								instancesOfWord.add(line);
							}
						}

					}
				});
			}
		}
	}

	/**
	 * @param line A {@link Line} within our {@link #wordSearchGrid}.
	 * @return A String representing the {@link Character}s at each point along the line.
	 */
	private final String getStringForLine(Line line) {
		final StringBuilder builder = new StringBuilder(line.length);
		line.forEachPoint(point -> {
			builder.append(wordSearchGrid[point.x][point.y]);
		});
		return builder.toString();
	}

	/**
	 * @return A random <code>char</code> between 'A' and 'Z' inclusive.
	 */
	private final char randChar() {
		return (char) (rand.nextInt(26) + 'A');
	}
}
