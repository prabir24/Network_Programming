package com.hangman.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServSocket {

	private ServerSocket serverSocket = null;
	private ListnerThread lnThread = null;

	ServSocket(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServSocket() {

	}

	public void createNewListnerThread() {
		lnThread = new ListnerThread(serverSocket);
		System.out.println("New listner thread created");
		createNewListnerThread();
	}

}
