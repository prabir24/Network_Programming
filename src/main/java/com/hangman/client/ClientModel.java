package com.hangman.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

public class ClientModel implements Runnable {

	private String PlayerName;
	private static Map<String, ClientSocket> PlayersMap;
	private List<String> PlayersList;
	public ClientSocket cSocket = null;
	private String ServerIp;
	private int ServerPort;

	ClientModel() {
		PlayerName = "";
		ServerIp = "";
		ServerPort = 0;
		PlayersMap = new HashMap<String, ClientSocket>();
		PlayersList = new ArrayList<String>();
	}

	public boolean createNewPlayerThread(String player, String ip, int port) {
		PlayerName = player;
		ServerIp = ip;
		ServerPort = port;

		if (!PlayersMap.containsKey(player)) {
			if (socketConnection()) {
				addToPlayersList(player);
				Thread t = new Thread(this, PlayerName);
				t.start();
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void run() {

		Platform.runLater(() -> {
			// Main.startGameUI(PlayerName, getSocketObject( PlayerName));
			Main.startLoginUI(PlayerName, getSocketObject(PlayerName));
		});
	}

	public boolean socketConnection() {
		try {
			cSocket = new ClientSocket();
			cSocket.connectToServer(ServerIp, ServerPort);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void addToPlayersList(String player) {
		PlayersMap.put(player, cSocket);
	}

	public static void removeFromPlayersList(String player) {
		if (PlayersMap.containsKey(player)) {
			PlayersMap.remove(player);
		}
	}

	public List<String> getList() {
		PlayersList.clear();
		PlayersList.addAll(PlayersMap.keySet());
		return PlayersList;
	}

	public ClientSocket getSocketObject(String player) {
		return PlayersMap.get(player);
	}
}
