package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.crypto.KeySelector;

import app.IMediator;

public class Network {
	public static final int BUF_SIZE = 1024;
	private Integer port = 0;
	private String IP = "";
	private String filesPath = "";
	Selector selector = null;
	ServerSocketChannel serverSocketChannel = null;
	IMediator mediator;
	public static ExecutorService pool = Executors.newFixedThreadPool(5);

	public Network(String filesPath, IMediator med) {
		this.filesPath = filesPath;
		this.mediator = med;
	}

	public void setIP(String IP) {
		this.IP = IP;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void startListening() {

		try {
			selector = Selector.open();

			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(IP, port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				// wait for something to happen
				selector.select();

				// iterate over the events
				for (Iterator<SelectionKey> it = selector.selectedKeys()
						.iterator(); it.hasNext();) {
					// get current event and REMOVE it from the list!!!
					SelectionKey key = it.next();
					it.remove();

					if (key.isAcceptable())
						accept(key);
					else if (key.isReadable())
						read(key);
					else if (key.isWritable())
						write(key);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// cleanup

			if (selector != null)
				try {
					selector.close();
				} catch (IOException e) {
				}
			if (serverSocketChannel != null)
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
				}
		}

	}

	private void write(SelectionKey key) throws IOException {
		System.out.println("WRITE: ");

		RequestedFileInfo fileInfo = (RequestedFileInfo) key.attachment();
		fileInfo.processBuffer(key);
	}

	private void read(SelectionKey keyP) {
		final SelectionKey key = keyP;

		Runnable runObj = new Runnable() {

			@Override
			public void run() {
				
				RequestedFileInfo fileInfo = (RequestedFileInfo) key.attachment();
				try {
					fileInfo.processBuffer(key);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				key.selector().wakeup();
			}
		};
		key.interestOps(0);
		pool.execute(runObj);

	}

	private void accept(SelectionKey key) throws IOException {
		System.out.print("ACCEPT: ");

		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel(); // initialize from key
		SocketChannel socketChannel = serverSocketChannel.accept(); // initialize
																	// from
																	// accept
		RequestedFileInfo fileInfo = new RequestedFileInfo(filesPath, socketChannel, this);

		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_READ, fileInfo);

		// display remote client address
		System.out.println("Connection from: "
				+ socketChannel.socket().getRemoteSocketAddress());

	}

}
