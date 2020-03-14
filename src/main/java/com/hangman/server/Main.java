package com.hangman.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static ServSocket servSocket = null;
	public static NonBlockingSocket nonServSocket = null;
	public static Scanner userInput = new Scanner(System.in);
	public static List<String> ListOfWords = new ArrayList<String>();

	public static int userInput() {
		try {
			int inputInt = userInput.nextInt();
			return inputInt;
		} catch (InputMismatchException e) {
			System.out.println("Input should be an Integer.. Try Again");
			userInput.nextLine();
			int retVal = userInput();
			return retVal;
		}
	}

	public static int getPort() {
		System.out.println("Enter PORT No.");
		int port = userInput();
		if ((port < 0) || (port > 65535)) {
			System.out.println("Port No. is out of range");
			int retPort = getPort();
			return retPort;
		} else {
			return port;
		}
	}

	public static int getSocketType() {
		int socketType = userInput();
		if ((socketType != 1) && (socketType != 2)) {
			System.out.println("Please enter proper option");
			int retSocketType = getSocketType();
			return retSocketType;
		} else {
			return socketType;
		}
	}

	public static void loadFile(String file) {
		try {
			InputStream inputStream = Main.class.getResourceAsStream(file);
			InputStreamReader in = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(in);
			String line;
			while ((line = br.readLine()) != null) {
				ListOfWords.add(line);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public static void getIP() { String ip; try { Enumeration<NetworkInterface>
	 * interfaces = NetworkInterface.getNetworkInterfaces(); while
	 * (interfaces.hasMoreElements()) { NetworkInterface iface =
	 * interfaces.nextElement(); // filters out 127.0.0.1 and inactive interfaces if
	 * (iface.isLoopback() || !iface.isUp()) continue;
	 * 
	 * Enumeration<InetAddress> addresses = iface.; while
	 * (addresses.hasMoreElements()) { InetAddress addr = addresses.nextElement();
	 * ip = addr.getHostAddress(); System.out.println(iface.getDisplayName() + " " +
	 * ip); } } } catch (SocketException e) { throw new RuntimeException(e); } }
	 */

	public static void main(String[] args) {
		loadFile("/words.txt");
		System.out.println("Starting Server...");
		System.out.println("Enter 1 for Blocking Socket and 2 for Non-Blocking Socket");
		int option = getSocketType();
		int port = getPort();
		System.out.println("\nWaiting for a Client to join....");
		if (option == 1) {
			servSocket = new ServSocket(port);
			servSocket.createNewListnerThread();
		} else if (option == 2) {
			nonServSocket = new NonBlockingSocket(port);
			nonServSocket.createNonBlockingSocket();
		}
		while (true) {

		}

	}

}
