package com.hangman.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingClient {

	private Selector selector = null;
	public SocketChannel socketChannel = null;
	public String msgFromServer = "";
	public boolean stop = false;

	public NonBlockingClient() {

	}

	public void startNonBlockingClient(String ip, int port) {
		try {
			InetSocketAddress serverAddress = new InetSocketAddress(ip, port);
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(serverAddress);
			int keyOP = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
			socketChannel.register(selector, keyOP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connection() {

		while (!stop) {
			try {
				if (selector.select() > 0) {
					ReadWriteServer(selector.selectedKeys());
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean ReadWriteServer(Set<SelectionKey> readySet) throws Exception {
		Iterator<SelectionKey> iterator = readySet.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			iterator.remove();
			if (key.isConnectable()) {
				boolean connected = processConnect(key);
				if (!connected) {
					return true;
				}
			}
			if (key.isReadable()) {
				readFromServer(key);
			}
		}
		return false;
	}

	public boolean processConnect(SelectionKey key) throws Exception {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		while (socketChannel.isConnectionPending()) {
			socketChannel.finishConnect();
		}
		return true;
	}

	public void readFromServer(SelectionKey key) {
		try {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(2048);
			socketChannel.read(buffer);
			buffer.flip();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			synchronized (ClientSocket.objWait) {
				msgFromServer = new String(bytes);
				ClientSocket.objWait.notifyAll();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
