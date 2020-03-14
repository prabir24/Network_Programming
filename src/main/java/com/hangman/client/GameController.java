package com.hangman.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

public class GameController {

	public GameModel gm = new GameModel();
	private int NoOfLetters = 0;

	@FXML
	private Button btnStart;

	@FXML
	private Button btnStop;

	@FXML
	private Button btnGuess;

	@FXML
	private TextField textWord;

	@FXML
	private TextField textRem;

	@FXML
	private TextField textScore;

	@FXML
	private TextField textGuess;

	@FXML
	void startGame(ActionEvent event) {
		btnStart.setDisable(true);
		GameModel.Message msg = gm.sendStartMessage();
		if (msg == null) {
			Alert alert = new Alert(AlertType.INFORMATION, "Message Vallidation Failed.. so closing the Window");
			alert.showAndWait();
			gm.sendStopMessage();
			Stage stage = (Stage) btnStart.getScene().getWindow();
			stage.close();
		} else {
			textWord.setText(msg.correctGuessedLetters);
			textRem.setText(Integer.toString(msg.NoOfAttemptsLeft));
			textScore.setText(Integer.toString(msg.Score));
			NoOfLetters = msg.NoOfLetters;
			btnGuess.setDisable(false);
		}
	}

	@FXML
	void stopGame(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION,
				"Are you sure you want to stop Playing.. You will lose the Score", ButtonType.YES, ButtonType.NO);
		alert.showAndWait();
		if (alert.getResult() == ButtonType.YES) {
			gm.sendStopMessage();
			Stage stage = (Stage) btnStop.getScene().getWindow();
			stage.close();
		}
	}

	@FXML
	void guessWord(ActionEvent event) {
		String guessStr = textGuess.getText();
		if (guessStr.length() == 1 || guessStr.length() == NoOfLetters) {
			GameModel.Message msg = gm.sendGuessMessage(guessStr);
			if (msg == null) {
				Alert alert = new Alert(AlertType.INFORMATION, "Message Vallidation Failed.. so closing the Window");
				alert.showAndWait();
				gm.sendStopMessage();
				Stage stage = (Stage) btnGuess.getScene().getWindow();
				stage.close();
			} else {
				textWord.setText(msg.correctGuessedLetters);
				textScore.setText(Integer.toString(msg.Score));
				if (msg.NoOfAttemptsLeft == -1) {
					textRem.setText("NA");
					btnStart.setDisable(false);
					btnGuess.setDisable(true);
				} else if (msg.NoOfAttemptsLeft == 0) {
					textRem.setText("0");
					btnStart.setDisable(false);
					btnGuess.setDisable(true);
				} else {
					textRem.setText(Integer.toString(msg.NoOfAttemptsLeft));
				}
			}
		} else {
			Alert alert = new Alert(AlertType.INFORMATION,
					"Guess can be letter or a word same size as the word to be guessed");
			alert.showAndWait();
		}
		textGuess.setText("");
	}

	public GameModel getGameModelObject() {
		return gm;
	}

}
