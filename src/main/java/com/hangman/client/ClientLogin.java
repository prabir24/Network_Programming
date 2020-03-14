package com.hangman.client;

import java.io.IOException;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientLogin {

	private class ClientCredentials {
		
		String username;
		String password;
	}

	public ClientSocket clientSocket = null;
	private ObjectMapper Obj = null;
	public String jwtKey = "";
	public String PlayerName = "";

	public ClientLogin() {
		Obj = new ObjectMapper();
		Obj.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

	}

	public int validateLogin(String username, String pswd) {

		ClientCredentials cred = new ClientCredentials();
		cred.username = username;
		cred.password = pswd;
		clientSocket.writeDataToServer(objToJSONString(cred));
		jwtKey = clientSocket.readDataFromServer();
		if (jwtKey.compareTo("failed") == 0)
			return -1;
		else if (jwtKey.compareTo("try") == 0)
			return 1;
		else
			return 0;
	}

	public void startGameUI() {
		Main.startGameUI(PlayerName, clientSocket, jwtKey);
	}

	public ClientCredentials JSONStringToObject(String jsonString) {

		try {
			return Obj.readValue(jsonString, ClientCredentials.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String objToJSONString(ClientCredentials msg) {
		try {
			return Obj.writeValueAsString(msg);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
