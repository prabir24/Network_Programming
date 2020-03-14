package com.hangman.client;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameModel {

	public static class Message {
		int NoOfLetters;
		String correctGuessedLetters;
		int NoOfAttemptsLeft;
		int Score;

		@JsonCreator
		public Message(@JsonProperty("NoOfLetters") int NoOfLetters,
				@JsonProperty("correctGuessedLetters") String correctGuessedLetters,
				@JsonProperty("NoOfAttemptsLeft") int NoOfAttemptsLeft, @JsonProperty("Score") int Score) {

			this.NoOfLetters = NoOfLetters;
			this.correctGuessedLetters = correctGuessedLetters;
			this.NoOfAttemptsLeft = NoOfAttemptsLeft;
			this.Score = Score;
		}

	}

	private class ClientMessage {
		String type;
		String Key;
		String player;
		String guess;
	}

	public String PlayerName;
	public ClientSocket clientSocket = null;
	private ObjectMapper Obj = null;
	private ClientMessage clientMsg = null;
	public String jwtKey;

	GameModel() {
		PlayerName = "";
		// clientSocket = ClientController.clientModel.getSocketObject(PlayerName);
		Obj = new ObjectMapper();
		Obj.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		clientMsg = new ClientMessage();
	}

	/*
	 * public void startGameUI(String player) { try { PlayerName = player;
	 * FXMLLoader fxmlLoader = new
	 * FXMLLoader(getClass().getResource("GameUI.fxml")); Parent root = (Parent)
	 * fxmlLoader.load(); Stage stage = new Stage();
	 * stage.setTitle("Haugman Game : " + player); stage.setScene(new Scene(root));
	 * stage.show();
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } }
	 */

	public Message sendStartMessage() {
		clientMsg.type = "Start";
		clientMsg.player = PlayerName;
		clientMsg.guess = "";
		clientMsg.Key = jwtKey;
		clientSocket.writeDataToServer(objToJSONString(clientMsg));
		String serverData = clientSocket.readDataFromServer();
		if (serverData.compareTo("failed") == 0) {
			return null;
		} else {
			Message msg = JSONStringToObject(serverData);
			return msg;
		}
	}

	public Message sendGuessMessage(String guessStr) {
		clientMsg.type = "Guess";
		clientMsg.player = PlayerName;
		clientMsg.guess = guessStr;
		clientMsg.Key = jwtKey;
		clientSocket.writeDataToServer(objToJSONString(clientMsg));
		String serverData = clientSocket.readDataFromServer();
		if (serverData.compareTo("failed") == 0) {
			return null;
		} else {
			Message msg = JSONStringToObject(serverData);
			return msg;
		}
	}

	public void sendStopMessage() {
		clientMsg.type = "Stop";
		clientMsg.player = PlayerName;
		clientMsg.guess = "";
		clientMsg.Key = jwtKey;
		clientSocket.writeDataToServer(objToJSONString(clientMsg));
		clientSocket.close();
		ClientModel.removeFromPlayersList(PlayerName);
	}

	public Message JSONStringToObject(String jsonString) {

		try {
			return Obj.readValue(jsonString, Message.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String objToJSONString(ClientMessage msg) {
		try {
			return Obj.writeValueAsString(msg);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}