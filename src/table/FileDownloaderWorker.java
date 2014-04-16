package table;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;
import javax.xml.crypto.KeySelector;

public class FileDownloaderWorker extends SwingWorker<Integer, Integer> {
	private static final int BUFFER_SIZE = 1024;
	private static final String RECEIVING_SIZE = "RECEIVING_SIZE";
	private static final String RECEIVING_READY = "RECEIVING_READY";
	private static final String RECEIVING_COMPLETE = "RECEIVING_COMPLETE";
	private static final String SENDING_ACK = "SENDING_ACK";
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

	public FileDownloaderWorker(String address, Integer port, String fileName,
			String userName) {
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(address, port));
			this.fileName = fileName;
			this.userName = userName;
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Integer doInBackground() {
		System.out.println("Crt th" + Thread.currentThread());

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
			System.err.println("An error ocurrred in connection!");
		} finally {
			try {
				socketChannel.close();
				System.out.println("closing");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		System.out.println("State is " + status);
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
			System.out.println("Sent ack" + bytes_received);
		}
		key.interestOps(SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		System.out.println("State is " + status);
		if (status.equals(RECEIVING_SIZE)) {
			while (socketChannel.read(buf) > 0) {
				if (!buf.hasRemaining()) {
					break;
				}
			}
			buf.flip();
			size = buf.getLong();
			System.out.println("Size of file is " + size);
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
				System.out.println("Limit buffer " + buf.limit());
				long rcvd = buf.getLong();
				System.out.println("RCVD is " + rcvd);
				fc.write(buf, bytes_received);
				buf.clear();
				bytes_received += rcvd;
				this.setProgress((int) ((bytes_received * 100) / size));
				System.out.println("bytes received" + bytes_received);
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
		System.out.println("connected");
		SocketChannel socketChannel = (SocketChannel) key.channel();
		socketChannel.finishConnect();
		ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE + 8);
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_WRITE, buf);
	}

	protected void process(List<Integer> chunks) {

		System.out.println("Crt th" + Thread.currentThread());

		System.out.println("Begin iteration");
		for (Integer i : chunks) {
			System.out.println(i);
		}
		System.out.println("End iteration");
		System.out.println(chunks);
	}

	@Override
	protected void done() {

		System.out.println("Crt th" + Thread.currentThread());

		if (isCancelled())
			System.out.println("Cancelled !");
		else
			System.out.println("Done !");
	}
}
