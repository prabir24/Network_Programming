package com.hangman.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NonBlockingSocket {

	private ServerSocketChannel channel = null;
	private Selector selector = null;
	private int serverPort;
	private Map<SocketChannel, ListnerThread> channelMap = null;

	NonBlockingSocket() {

	}

	NonBlockingSocket(int port) {
		serverPort = port;
		channelMap = new HashMap<SocketChannel, ListnerThread>();
	}

	public void createNonBlockingSocket() {
		try {
			channel = ServerSocketChannel.open();
			channel.bind(new InetSocketAddress(serverPort));
			channel.configureBlocking(false);

			selector = Selector.open();
			SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				if (selector.select() > 0) {
					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						iterator.remove();
						if (!key.isValid()) {
							System.out.println("JOJO");
							continue;
						}
						if (key.isAcceptable()) {
							startHandler(key);
						}
						if (key.isReadable()) {
							recvFromClient(key);
						} /*
							 * else if (key.isWritable()) { sendToClient(key); }
							 */
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * server.connect(new InetSocketAddress(serverIP, serverPort)); Path path =
	 * Paths.get("C:/Test/temp.txt"); FileChannel fileChannel =
	 * FileChannel.open(path); ByteBuffer buffer = ByteBuffer.allocate(1024);
	 * while(fileChannel.read(buffer) > 0) { buffer.flip(); server.write(buffer);
	 * buffer.clear(); } fileChannel.close(); System.out.println("File Sent");
	 * server.close(); }
	 */

	private void startHandler(SelectionKey key) throws IOException {
		System.out.println("HERE");
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = serverSocketChannel.accept();
		clientChannel.configureBlocking(false);
		// Socket socket = clientChannel.socket();
		System.out.println("Connection has been established.\n");
		clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		selector.selectedKeys().remove(key);
		ListnerThread listThread = new ListnerThread(clientChannel);
		channelMap.put(clientChannel, listThread);
	}

	private void recvFromClient(SelectionKey key) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ListnerThread listThread = channelMap.get(socketChannel);
		buffer.clear();
		int numBytes = socketChannel.read(buffer);
		// Socket socket = socketChannel.socket();
		if (numBytes == -1) { // Closed connection or error --> -1
			// Remove client game session when connection is terminated
			channelMap.remove(socketChannel);
			key.cancel();
			System.out.println("Connection has been terminated.\n");
			closeSocket(socketChannel);
		} else {
			readMessage(buffer, listThread);
		}
		selector.selectedKeys().remove(key);
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

	// Converts the message received from the client to String (from bytes)
	// Then calls the clients current hangman session
	private void readMessage(ByteBuffer buffer, ListnerThread listThread) {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		listThread.nonBlockingRun(new String(bytes));
	}
}
