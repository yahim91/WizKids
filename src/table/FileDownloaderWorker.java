package table;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

public class FileDownloaderWorker extends SwingWorker<Integer, Integer> {
	private static final int BUF_SIZE = 1024;
	private static final String READY = "READY";
	private static final String RECEIVING_SIZE = "RECEIVING_SIZE";
	private static final String SENDING_READY = "SENDING_READY";
	private static final String SENDING_COMPLETE = "SENDING_COMPLETE";
	SocketChannel socketChannel;
	String fileName;
	Selector selector = null;
	private String REQUESTED_FILE = "FILE_REQUESTED";
	private String INIT = "FILE_INIT";
	private String status = INIT;
	private String userName;
	private RandomAccessFile raf;
	private FileChannel fc;
	private MappedByteBuffer fileBuffer;
	private long size;
	private File receivingFile;
	private int bytes_read;

	public FileDownloaderWorker(String address, Integer port, String fileName,
			String userName) {
		try {
			socketChannel = SocketChannel.open(/*
												 * new
												 * InetSocketAddress(address,
												 * port)
												 */);
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

		while (true) {
			try {
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

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} /*finally {
				try {
					socketChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
		}

	}

	private void write(SelectionKey key) throws IOException {
		int bytes_write;
		int bytes_written;
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		if (status.equals(INIT)) {
			String cmd = fileName;
			buf.putInt(cmd.length());
			buf.put(cmd.getBytes());
			buf.flip();
			bytes_written = 0;
			while ((bytes_write = socketChannel.write(buf)) > 0
					&& bytes_written < cmd.length()) {
				if (!buf.hasRemaining()) {
					buf.clear();
					break;
				}
				bytes_written += bytes_write;
			}
			status = RECEIVING_SIZE;
		}

		key.interestOps(SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		System.out.println("State is " + status);
		if (status.equals(RECEIVING_SIZE)) {
			if (socketChannel.read(buf) < 0)
				return;
			buf.flip();
			size = buf.getInt();
			System.out.println("Size of file is " + size);
			receivingFile = new File("users_folder" + "/" + userName + "/"
					+ fileName);
			if (!receivingFile.exists()) {
				receivingFile.createNewFile();
			}

			raf = new RandomAccessFile(receivingFile, "rw");
			fc = raf.getChannel();
			fileBuffer = fc.map(MapMode.READ_WRITE, 0, size);
			status = SENDING_READY;
			buf.clear();
		} else if (status.equals(SENDING_READY)) {
			int bytes_received;
			buf.clear();
			while ((bytes_received = socketChannel.read(buf)) > 0) {
				System.out.println("here pos " + buf.position());
				buf.flip();
				fc.write(buf);
				buf.clear();
				bytes_read += bytes_received;
			}
			System.out.println("read byte " + bytes_read);
			// if (bytes_read >= size)
			// status = SENDING_COMPLETE;
			// socketChannel.close();
		} else if (status.equals(SENDING_COMPLETE)) {
			socketChannel.close();
		}

	}

	private void connect(SelectionKey key) throws IOException {
		System.out.println("connected");
		SocketChannel socketChannel = (SocketChannel) key.channel();
		socketChannel.finishConnect();
		ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
		socketChannel.configureBlocking(false);
		socketChannel.register(key.selector(), SelectionKey.OP_WRITE, buf);
		key.selector().wakeup();

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
