package com.hangman.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientSocket implements Runnable {

	private Socket cSocket = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private NonBlockingClient client = null;
	private String serverIp;
	private int serverPort;
	public static Object objWait = new Object();

	public ClientSocket() {

	}

	public void connectToServer(String ip, int port) throws Exception {
		serverIp = ip;
		serverPort = port;
		if (Main.blockingScoket) {
			cSocket = new Socket(ip, port);
			dis = new DataInputStream(cSocket.getInputStream());
			dos = new DataOutputStream(cSocket.getOutputStream());
		} else {
			Thread th = new Thread(this);
			th.start();
		}

	}

	public String readDataFromServer() {
		String readData = "";
		try {
			if (Main.blockingScoket) {
				readData = dis.readUTF();
			} else {
				synchronized (objWait) {
					try {
						System.out.println("PRABIR CHOUDHUYR");
						while (client.msgFromServer.compareTo("") == 0) {
							objWait.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					readData = client.msgFromServer;
					client.msgFromServer = "";
					System.out.println("DATA: " + readData);
				}
			}
			int len = retrieveLengthFromString(readData);
			int expectedLength = Integer.parseInt(readData.substring(0, len));
			int currentLength = readData.substring(len, readData.length()).length();
			if (expectedLength == currentLength) {
				return readData.substring(len, readData.length());
			} else {
				return "";
			}
			// return dis.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void writeDataToServer(String data) {
		try {
			String writeData = Integer.toString(data.length()).concat(data);
			if (Main.blockingScoket) {
				dos.writeUTF(writeData);
			} else {
				ByteBuffer buffer = ByteBuffer.wrap(writeData.getBytes());
				client.socketChannel.write(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (Main.blockingScoket) {
				dis.close();
				dos.close();
				cSocket.close();
			} else {
				client.stop = true;
				client.socketChannel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int retrieveLengthFromString(String str) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.substring(i, i + 1).matches("[0-9]"))
				count += 1;
			else
				break;
		}
		return count;
	}

	@Override
	public void run() {
		System.out.println("Waiting1");
		client = new NonBlockingClient();
		client.startNonBlockingClient(serverIp, serverPort);
		client.connection();
	}

}
