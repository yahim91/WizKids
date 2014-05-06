package table;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

import javax.print.DocFlavor.BYTE_ARRAY;
import javax.sound.midi.Receiver;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import app.IMediator;

public class FileDownloaderWorker extends SwingWorker<Boolean, String> {
	private static final int BUFFER_SIZE = 1024;
	private static final String RECEIVING_SIZE = "RECEIVING_SIZE";
	private static final String RECEIVING_READY = "RECEIVING_READY";
	private static final String RECEIVING_COMPLETE = "RECEIVING_COMPLETE";
	private static final String SENDING_ACK = "SENDING_ACK";
	private static final String ERROR_RECEIVING = "ERROR_RECEIVING";
	SocketChannel socketChannel;
	String fileName;
	Selector selector = null;
	private String INIT = "FILE_INIT";
	private String status = INIT;
	private String userName;
	private RandomAccessFile raf;
	private FileChannel fc;
	private long size;
	private File receivingFile;
	private long bytes_received = 0;
	private Logger logger;
	private String address;
	private Integer port;
	private IMediator mediator;
	private Integer index;

	public FileDownloaderWorker(String address, Integer port, String fileName,
			String userName, IMediator mediator, Integer index) {
		logger = Logger.getLogger("Main");
		this.mediator = mediator;
		this.index = index;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			this.address = address;
			this.port = port;
			socketChannel.connect(new InetSocketAddress(address, port));
			this.fileName = fileName;
			this.userName = userName;
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("Connecting to " + address + ":" + port + " failed!");
		}
	}

	@Override
	protected Boolean doInBackground() {
		try {
			while (true) {
				selector.select();
				for (Iterator<SelectionKey> it = selector.selectedKeys()
						.iterator(); it.hasNext();) {
					SelectionKey key = it.next();
					it.remove();

					if (key.isConnectable())
						connect(key);
					else if (key.isReadable())
						read(key);
					else if (key.isWritable())
						write(key);
				}
			}

		} catch (IOException e) {
			logger.error("Connection to " + address + ":" + port);
			status = ERROR_RECEIVING;
		} finally {
			try {
				socketChannel.close();
				logger.info("closing socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes_received == size;
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		if (status.equals(INIT)) {
			String cmd = fileName;
			buf.putInt(cmd.length());
			buf.putInt(userName.length());

			buf.put(cmd.getBytes());
			buf.put(userName.getBytes());
			buf.flip();
			while (socketChannel.write(buf) > 0) {
				if (!buf.hasRemaining()) {
					buf.clear();
					break;
				}
			}
			status = RECEIVING_SIZE;
		} else if (status.equals(SENDING_ACK)) {
			buf.clear();
			buf.putLong(bytes_received);
			buf.flip();
			while (socketChannel.write(buf) > 0) {
				if (!buf.hasRemaining()) {
					buf.clear();
					break;
				}
			}
			status = RECEIVING_READY;
			logger.info("Sent ack" + bytes_received + "!");
		}
		key.interestOps(SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		logger.info("State is " + status);
		if (status.equals(RECEIVING_SIZE)) {
			while (socketChannel.read(buf) > 0) {
				if (!buf.hasRemaining()) {
					break;
				}
			}
			buf.flip();
			size = buf.getLong();
			logger.info("Size of file is " + size);
			receivingFile = new File("users_folder" + "/" + userName + "/"
					+ fileName);
			if (!receivingFile.exists()) {
				receivingFile.createNewFile();
			}

			raf = new RandomAccessFile(receivingFile, "rw");
			fc = raf.getChannel();
			status = SENDING_ACK;
			key.interestOps(SelectionKey.OP_WRITE);
			buf.clear();
		} else if (status.equals(RECEIVING_READY)) {
			if (size == 0) {
				this.setProgress(100);
				status = RECEIVING_COMPLETE;
			} else if (bytes_received < size) {
				buf.clear();
				while (socketChannel.read(buf) > 0) {
					if (!buf.hasRemaining()) {
						break;
					}
				}
				buf.flip();
				if (!buf.hasRemaining()) {
					return;
				}
				long rcvd = buf.getLong();
				fc.write(buf, bytes_received);
				buf.clear();
				bytes_received += rcvd;
				this.setProgress((int) ((bytes_received * 100) / size));
				logger.info("bytes received " + bytes_received);
				status = SENDING_ACK;
				key.interestOps(SelectionKey.OP_WRITE);
				if (bytes_received == size) {
					selector.close();
					socketChannel.close();
					fc.close();
					raf.close();
				}
			} else {
				status = RECEIVING_COMPLETE;
			}
		} else if (status.equals(RECEIVING_COMPLETE)) {
			selector.close();
			socketChannel.close();
			fc.close();
			raf.close();
		}

	}

	private void connect(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		socketChannel.finishConnect();
		ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE + 8);
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_WRITE, buf);
		logger.info("Connected");
		
	}

	protected void process(List<String> chunks) {
		
	}

	@Override
	protected void done() {

		if (isCancelled())
			logger.info("Cancelled !");
		else {
			if (bytes_received == size && !status.equals(ERROR_RECEIVING)) {
				logger.info("Receiving complete!");
				// TODO: may be removed
				// mediator.getUserFiles(mediator.getUserName()).add(fileName);
				mediator.addNewOwnFile(fileName);
				mediator.sendUpdateFiles();
			} else {
				logger.info("Receiving failed!");
				mediator.getTableModel().updateMessage(index, "Error");
				mediator.displayMessage("Error downloading file");
			}
			
			
		}
	}
}
