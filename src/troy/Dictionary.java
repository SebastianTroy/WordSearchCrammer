package troy;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;

/**
 * A Simple convenience class which bundles the loading of and referencing to a list of words from a user 'dictionary'.
 * 
 * @author Sebastian Troy
 */
public class Dictionary {
	private final RadixTree<VoidValue> validWords;

	/**
	 * Attempts to initialise this dictionary by loading a file with the specified name, if such a file doesn't exist, the user is prompted
	 * to choose one with a {@link FileChooser}.
	 * 
	 * @param dictionaryFileName
	 */
	Dictionary(String dictionaryFileName) {
		validWords = buildTreeFromDictionary(dictionaryFileName);
	}

	/**
	 * @param possibleWord {@link CharSequence} to check.
	 * @return <code>true</code> if the exact specified {@link CharSequence} is in this {@link UserDictionary}. The sequence must be identical per character, i.e.
	 *         an accented 'é' != a non-accented 'e'.
	 */
	public final boolean contains(CharSequence possibleWord) {
		return validWords.getValueForExactKey(possibleWord) != null;
	}

	/**
	 * @param stringToCheck {@link CharSequence} to check.
	 * @return <code>true</code> if any words in this {@link UserDictionary} begin with the specified {@link CharSequence}. The sequence must be identical per character, i.e.
	 *         an accented 'é' != a non-accented 'e'.
	 */
	public final boolean containsWordsStartingWith(CharSequence stringToCheck) {
		return validWords.getValuesForKeysStartingWith(stringToCheck) != null;
	}
	
	/**
	 * Attempts to load an existing file at "../resources/fileName" and resorts to showing the user a file picket if that fails. Once loaded
	 * it attempts to read in a list of valid words.
	 * 
	 * @param fileName - Name of the file to load the list of valid words from.
	 * @return <code>
	 */
	private final RadixTree<VoidValue> buildTreeFromDictionary(String fileName) {
		RadixTree<VoidValue> validWords = new ConcurrentRadixTree<VoidValue>(new DefaultCharArrayNodeFactory(), false);

		File dictionaryFile = null;

		try {
			dictionaryFile = new File(this.getClass().getResource("../resources/" + fileName).toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (dictionaryFile == null) {
			Alert fileNotFoundAlert = new Alert(AlertType.ERROR);
			fileNotFoundAlert.setTitle("Dictionary Not Found");
			fileNotFoundAlert
					.setContentText("Please select a file to use as the dictionary for this application.\nIt must consist of a list of words, with each word on a seperate line, with no punctiation or empty lines.");
			fileNotFoundAlert.showAndWait();

			FileChooser dictionarySelector = new FileChooser();

			boolean keepSearching = true;
			while (keepSearching) {
				dictionaryFile = dictionarySelector.showOpenDialog(null);

				if (dictionaryFile == null) {
					Alert cancelFileSelectDialog = new Alert(AlertType.CONFIRMATION);
					cancelFileSelectDialog.setTitle("Confirmation Dialog");
					cancelFileSelectDialog.setHeaderText("No File Selected");
					cancelFileSelectDialog.setContentText("Would you like to select a file?");

					Optional<ButtonType> result = cancelFileSelectDialog.showAndWait();
					if (result.get() != ButtonType.OK) {
						System.exit(1);
					}
				} else {
					keepSearching = false;
				}
			}
		}

		try {
			Files.lines(dictionaryFile.toPath(), Charset.defaultCharset()).forEach(line -> validWords.put(line.toUpperCase(), VoidValue.SINGLETON));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return validWords;
	}
}
