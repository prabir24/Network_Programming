package com.hangman.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController {

	public ClientLogin login = new ClientLogin();
	public Stage loginStage = null;
	public int count = 0;

	@FXML
	private TextField textUser;

	@FXML
	private TextField textPass;

	@FXML
	private Button btnLogin;

	@FXML
	void EventOnPassField(KeyEvent event) {

	}

	@FXML
	void validateLogin(ActionEvent event) {
		btnLogin.setDisable(true);
		String username = textUser.getText();
		String pswd = textPass.getText();
		if (username.isEmpty() || username == null || pswd.isEmpty() || pswd == null) {
			Alert alert = new Alert(AlertType.INFORMATION, "Username and Password Fields are mandatory to login");
			alert.showAndWait();
			btnLogin.setDisable(false);
		} else {
			int status = login.validateLogin(username, pswd);
			if (status == 1) {
				count += 1;
				Alert alert = new Alert(AlertType.INFORMATION,
						"Wrong Username or Password... No. of attempts left - " + (3 - count));
				alert.showAndWait();
				textUser.setText("");
				textPass.setText("");
				btnLogin.setDisable(false);
			} else if (status == -1) {
				Alert alert = new Alert(AlertType.INFORMATION,
						"Wrong Username or Password... No. of attempts left - 0  ... Closing Login WIndow");
				alert.showAndWait();
				loginStage.close();
				login.clientSocket.close();
				ClientModel.removeFromPlayersList(login.PlayerName);
			} else {
				loginStage.close();
				login.startGameUI();
			}
		}
	}

}
