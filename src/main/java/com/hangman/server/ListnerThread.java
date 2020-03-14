package com.hangman.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class ListnerThread implements Runnable {

	public static class ClientMessage {
		String type;
		String Key;
		String player;
		String guess;

		@JsonCreator
		public ClientMessage(@JsonProperty("type") String type, @JsonProperty("Key") String Key,
				@JsonProperty("player") String player, @JsonProperty("guess") String guess) {

			this.type = type;
			this.Key = Key;
			this.player = player;
			this.guess = guess;
		}
	}

	public static class ClientCredentials {
		String username;
		String password;

		@JsonCreator
		public ClientCredentials(@JsonProperty("username") String username, @JsonProperty("password") String password) {

			this.username = username;
			this.password = password;
		}
	}

	private class Message {
		int NoOfLetters;
		String correctGuessedLetters;
		int NoOfAttemptsLeft;
		int Score;
	}

	private String username = "rezaul";
	private String password = "1234";
	private Key JWTKey = null;

	private SocketChannel socketChannel = null;
	private Socket socket = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private ObjectMapper Obj = null;
	private HangmanGame game = null;
	private Message msg = null;
	private boolean stop = false;
	private boolean blockingType = true;
	private String nonBlockingMessage = "";
	private boolean Loginstatus = false;
	private int LoginCount = 0;

	ListnerThread() {

	}

	ListnerThread(SocketChannel channel) {
		game = new HangmanGame();
		msg = new Message();
		blockingType = false;
		socketChannel = channel;
		Obj = new ObjectMapper();
		Obj.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}

	ListnerThread(ServerSocket servSock) {
		try {
			socket = servSock.accept();
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			Obj = new ObjectMapper();
			Obj.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

			Thread t = new Thread(this);
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String readData() {
		try {
			String readData = "";
			if (blockingType) {
				readData = dis.readUTF();
			} else {
				readData = nonBlockingMessage;
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
			if (e.getMessage() != "Connection reset") {
				e.printStackTrace();
			} else {
				System.out.println("Connection reset");
			}
		}
		return null;
	}

	public void writeData(String data) {
		try {
			String writeData = Integer.toString(data.length()).concat(data);
			System.out.println(writeData);
			if (blockingType) {
				dos.writeUTF(writeData);
			} else {
				ByteBuffer buffer = ByteBuffer.wrap(writeData.getBytes());
				try {
					socketChannel.write(buffer);
				} catch (IOException e) {
					System.out.println("Connection has been terminated.\n");
					closeSocket(socketChannel);
				}
			}

			// dos.writeUTF(data);
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

	public void close() {
		try {
			dis.close();
			dos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeSocket(SocketChannel socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			System.out.println("Unable to close socket!");
		}
	}

	public ClientMessage JSONStringToObject(String jsonString) {

		try {
			return Obj.readValue(jsonString, ClientMessage.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ClientCredentials JSONStringToLoginObject(String jsonString) {

		try {
			return Obj.readValue(jsonString, ClientCredentials.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String objToJSONString(Message msg) {
		try {
			return Obj.writeValueAsString(msg);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void ActionOnCmd(String cmd, String str) {
		if (cmd.compareTo("Start") == 0) {
			game.selectWord();
			updateMessage();
		} else if (cmd.compareTo("Stop") == 0) {
			stop = true;
		} else if (cmd.compareTo("Guess") == 0) {
			if (str.length() == 1) {
				game.matchLetter(str);
				updateMessage();
			} else {
				game.matchWord(str);
				updateMessage();
			}
		}
	}

	public void updateMessage() {
		msg.correctGuessedLetters = game.msg.correctGuessedLetters;
		msg.NoOfAttemptsLeft = game.msg.NoOfAttemptsLeft;
		msg.NoOfLetters = game.msg.NoOfLetters;
		msg.Score = game.msg.Score;
	}

	public boolean validateUser(int count) {
		String jsonString = readData();
		ClientCredentials cred = JSONStringToLoginObject(jsonString);
		if ((cred.username.compareTo(username) == 0) && (cred.password.compareTo(password) == 0)) {
			String JWTKey = generateJWTKey(jsonString);
			writeData(JWTKey);
			return true;
		} else if (count < 2) {
			writeData("try");
			return false;
		} else {
			writeData("failed");
			return false;
		}
	}

	public String generateJWTKey(String jsonString) {
		JWTKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		return Jwts.builder().setSubject(jsonString).signWith(JWTKey).compact();
	}

	public boolean validateJWTKey(String key) {
		try {
			Jwts.parser().setSigningKey(JWTKey).parseClaimsJws(key);
			return true;

		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public void run() {
		boolean status = false;
		for (int i = 0; i < 3; i++) {
			status = validateUser(i);
			if (status)
				break;
		}
		if (status) {
			game = new HangmanGame();
			msg = new Message();
			while (!stop) {
				System.out.println("Waiting to read data");
				ClientMessage cmsg = JSONStringToObject(readData());
				System.out.println("Data read success");
				if (validateJWTKey(cmsg.Key)) {
					System.out.println("Key Succesfully Validated");
					ActionOnCmd(cmsg.type, cmsg.guess);
					if (cmsg.type.compareTo("Stop") != 0) {
						writeData(objToJSONString(msg));
						System.out.println("Data Write success");
					}
				} else {
					System.out.println("Key Validation Failed");
					writeData("failed");
				}
			}
		}
	}

	public void nonBlockingRun(String message) {
		nonBlockingMessage = message;
		if (Loginstatus) {
			System.out.println("Waiting to read data");
			ClientMessage cmsg = JSONStringToObject(readData());
			System.out.println("Data read success");
			if (validateJWTKey(cmsg.Key)) {
				System.out.println("Key Succesfully Validated");
				ActionOnCmd(cmsg.type, cmsg.guess);
				if (cmsg.type.compareTo("Stop") != 0) {
					writeData(objToJSONString(msg));
					System.out.println("Data Write success");
				}
			} else {
				System.out.println("Key Validation Failed");
				writeData("failed");
			}
		} else {
			Loginstatus = validateUser(LoginCount);
			LoginCount += 1;
		}
	}

}
