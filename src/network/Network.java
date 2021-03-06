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

import org.apache.log4j.Logger;

import app.IMediator;

public class Network {
	public static final int BUF_SIZE = 1024;
	private static final String WRITE = "WRITE";
	private static final String READ = "READ";
	private Integer port = 0;
	private String IP = "";
	private String filesPath = "";
	Selector selector = null;
	ServerSocketChannel serverSocketChannel = null;
	IMediator mediator;
	private Logger logger = Logger.getLogger("Main");

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
			System.out.println("Lissten for potential clients on: " + IP + " " + port);
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
			logger.error("Connection error!");
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
		RequestedFileInfo fileInfo = (RequestedFileInfo) key.attachment();
		fileInfo.processBuffer(key, WRITE);
	}

	private void read(SelectionKey key) {

		RequestedFileInfo fileInfo = (RequestedFileInfo) key.attachment();
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			fileInfo.processBuffer(key, READ);
		} catch (IOException e) {
			logger.error("Error occured while transmitting file!");
			
			try {
				socketChannel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	private void accept(SelectionKey key) throws IOException {

		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel(); // initialize from key
		SocketChannel socketChannel = serverSocketChannel.accept(); 
		RequestedFileInfo fileInfo = new RequestedFileInfo(filesPath,
				socketChannel, this);

		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_READ, fileInfo);
		
		logger.info("New connection accepted from " + socketChannel.socket().getRemoteSocketAddress() + "!");
	}

}
