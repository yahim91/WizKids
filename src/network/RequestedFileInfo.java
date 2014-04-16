package network;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import table.RowData;

public class RequestedFileInfo {

	private static final int BUFFER_SIZE = 1024;
	private static final String INIT = "INIT";
	private static final String SENDING_SIZE = "SENDING_SIZE";
	private static final String SENDING_FILE = "SENDING_FILE";
	private static final String SENDING_COMPLETE = "SENDING_COMPLETE";
	private ByteBuffer buffer;
	private String rqUserName;
	private String state = INIT;
	private String fileName;
	private File sendingFile;
	private String path;
	private RandomAccessFile raf;
	private FileChannel fc;
	private MappedByteBuffer fileBuffer;
	private SocketChannel socketChannel;
	private long fileSize;
	private Network network;
	private Integer rowIndex;

	public RequestedFileInfo(String path, SocketChannel socketChannel,
			Network net) {
		buffer = ByteBuffer.allocateDirect(BUFFER_SIZE + 8);
		this.path = path;
		this.socketChannel = socketChannel;
		this.network = net;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void processBuffer(SelectionKey key) throws IOException {
		System.out.println("State is " + state);
		if (state.equals(INIT)) {
			buffer.clear();
			int bytes;
			while ((bytes = socketChannel.read(buffer)) > 0) {
				if (!buffer.hasRemaining()) {
					break;
				}
			}
			buffer.flip();
			if (!buffer.hasRemaining()) {
				return;
			}

			byte[] file = new byte[buffer.getInt()];
			byte[] name = new byte[buffer.getInt()];
			buffer.get(file);
			buffer.get(name);
			fileName = new String(file);
			rqUserName = new String(name);
			sendingFile = new File(path + "/" + fileName);
			raf = new RandomAccessFile(sendingFile, "rw");
			fc = raf.getChannel();
			state = SENDING_SIZE;
			System.out.println("file name is " + path + "/" + fileName);
			buffer.clear();
			rowIndex = network.mediator.getTableModel().getRowCount();
			network.mediator.getTableModel().addRow(
					new RowData(network.mediator.getUserName(), rqUserName,
							fileName));
			key.interestOps(SelectionKey.OP_WRITE);
		} else if (state.equals(SENDING_SIZE)) {
			fileSize = sendingFile.length();
			buffer.putLong(fileSize);
			System.out.println("sending file size " + sendingFile.length());
			buffer.flip();
			writeToBuffer();
			state = SENDING_FILE;
		} else if (state.equals(SENDING_FILE)) {
			long bytes_read = 0;
			long length;
			while (bytes_read < fileSize) {
				length = fileSize - bytes_read < BUFFER_SIZE ? fileSize
						- bytes_read : BUFFER_SIZE;
				fileBuffer = fc.map(FileChannel.MapMode.READ_ONLY, bytes_read,
						length);
				buffer.putLong(length);
				buffer.put(fileBuffer);
				buffer.flip();

				bytes_read += length;
				while (socketChannel.write(buffer) > 0) {
					if (!buffer.hasRemaining()) {
						buffer.clear();
						break;
					}
				}
				System.out.println("Sent " + bytes_read + " bytes!");
				network.mediator.getTableModel().updateStatus(rowIndex,
						(int) (bytes_read * 100 / fileSize));
			}
			state = SENDING_COMPLETE;
		} else if (state.equals(SENDING_COMPLETE)) {
			buffer.clear();
			System.out.println("closing");
			socketChannel.close();
			fc.close();
			raf.close();
		}
	}

	private void writeToBuffer() throws IOException {
		while (socketChannel.write(buffer) > 0) {
			if (!buffer.hasRemaining()) {
				buffer.clear();
				break;
			}
		}
	}
}
