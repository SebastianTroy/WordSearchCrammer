package troy;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

public class UserInterface {
	@FXML
	ListView<String> wordList;
	@FXML
	Slider sizeSlider;
	@FXML
	Button startButton;
	@FXML
	Button stopButton;
	@FXML
	GridPane resultsTable;
	@FXML
	Label generationsLabel;
	@FXML
	Label improvementsLabel;

	Label[][] wordSearchContent = new Label[20][20];

	WordSearchCrammer wordSearchCrammerAlgorith = null;

	public UserInterface() {
		for (int x = 0; x < 20; x++)
			for (int y = 0; y < 20; y++) {
				wordSearchContent[x][y] = new Label("X");
			}
	}

	@FXML
	public void initialize() {
		sizeSlider.setValue(12);
		onSizeSliderUsed();
	}
	
	private void bindKnownWordsToList(Property<ObservableList<String>> knownWords) {
		wordList.itemsProperty().bind(knownWords);
	}

	private void bindTextPropertiesToLabels(ObservableValue<String>[][] observableStrings) {
		if (wordSearchContent.length > 0) {
			if (observableStrings.length <= wordSearchContent.length && observableStrings[0].length <= wordSearchContent[0].length) {
				int xLength = observableStrings.length;
				int yLength = observableStrings[0].length;
				for (int x = 0; x < xLength; x++) {
					for (int y = 0; y < yLength; y++) {
						wordSearchContent[x][y].textProperty().bind(observableStrings[y][x]);
					}
				}
			}
			else {
				System.out.println("bindTextPropertiesToLabels: Size mismatch between wordSearchContent and observableStrings");
			}
		}
		else {
			System.out.println("bindTextPropertiesToLabels: wordSearchContent length too small" + wordSearchContent.length);
		}
	}

	@FXML
	public void onSizeSliderUsed() {
		resultsTable.getChildren().clear();

		int tableSize = (int) sizeSlider.getValue();
		for (int x = 0; x < tableSize; x++)
			for (int y = 0; y < tableSize; y++) {
				resultsTable.add(wordSearchContent[x][y], x, y);
			}

		resultsTable.setGridLinesVisible(true);
	}

	@FXML
	public void onEndPressed() {
		wordSearchCrammerAlgorith.stopSearching();
		startButton.setDisable(false);
		stopButton.setDisable(true);
	}

	@FXML
	public void onBeginPressed() {
		int size = (int) sizeSlider.getValue();
		wordSearchCrammerAlgorith = new WordSearchCrammer(size);

		bindKnownWordsToList(wordSearchCrammerAlgorith.getObservableWordListProperty());
		bindTextPropertiesToLabels(wordSearchCrammerAlgorith.getObservableCharacterGridStringProperties());
		generationsLabel.textProperty().bind(wordSearchCrammerAlgorith.getNumberOfGenerationsStringProperty());
		improvementsLabel.textProperty().bind(wordSearchCrammerAlgorith.getNumberOfImprovementsStringProperty());

		wordSearchCrammerAlgorith.start();

		startButton.getScene().getWindow().setOnCloseRequest(handler -> {
			if (wordSearchCrammerAlgorith != null) {
				wordSearchCrammerAlgorith.stopSearching();
			}
		});

		startButton.setDisable(true);
		stopButton.setDisable(false);
	}
}
