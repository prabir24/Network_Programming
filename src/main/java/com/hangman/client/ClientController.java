package com.hangman.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ClientController {

	public static ClientModel clientModel = new ClientModel();

	@FXML
	private TextField textIp;

	@FXML
	private TextField textPort;

	@FXML
	private Button btnPlay;

	@FXML
	private ComboBox<String> cbPlayerName;

	@FXML
	void createPlayer(ActionEvent event) {
		btnPlay.setDisable(true);
		String PlayerName = cbPlayerName.getValue();
		String serverIP = textIp.getText();
		String serverPort = textPort.getText();

		if (PlayerName == null || serverIP == null || serverPort == null) {
			Alert alert = new Alert(AlertType.INFORMATION, "Fields cannot be NULL");
			alert.showAndWait();
			btnPlay.setDisable(false);
			return;
		}

		if (!validateIP(serverIP)) {
			Alert alert = new Alert(AlertType.INFORMATION, "Check SERVER IP Format");
			alert.showAndWait();
			btnPlay.setDisable(false);
			return;
		}

		int port = validatePort(serverPort);
		if (port == -1) {
			Alert alert = new Alert(AlertType.INFORMATION, "Check SERVER PORT Value");
			alert.showAndWait();
			btnPlay.setDisable(false);
			return;
		}

		if (PlayerName == null || PlayerName.isEmpty()) {
			Alert alert = new Alert(AlertType.INFORMATION,
					"Player Name cannot be Empty...Either enter a new Player Name or select from the drop down");
			alert.showAndWait();
		} else {
			boolean newPlayer = clientModel.createNewPlayerThread(PlayerName, serverIP, port);
			if (!newPlayer) {
				Alert alert = new Alert(AlertType.INFORMATION, "Duplicate Player Name or Socket Connection failed");
				alert.showAndWait();
			}
		}
		updatePlayersCB();
		btnPlay.setDisable(false);
	}

	public void updatePlayersCB() {
		ObservableList<String> cbPlayersList = FXCollections.observableArrayList(clientModel.getList());
		cbPlayerName.setItems(cbPlayersList);
	}

	public boolean validateIP(String ipv4) {
		try {
			if (ipv4 == null || ipv4.isEmpty()) {
				return false;
			}

			String[] parts = ipv4.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			if (ipv4.endsWith(".")) {
				return false;
			}

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	public int validatePort(String port) {
		try {
			int validPort = Integer.parseInt(port);
			if ((validPort < 0) || (validPort > 65535)) {
				return -1;
			}
			return validPort;
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}
}
