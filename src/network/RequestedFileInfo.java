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

import javax.xml.crypto.KeySelector;

import org.apache.log4j.Logger;

import table.RowData;

public class RequestedFileInfo {

	private static final int BUFFER_SIZE = 1024;
	private static final String INIT = "INIT";
	private static final String SENDING_SIZE = "SENDING_SIZE";
	private static final String SENDING_FILE = "SENDING_FILE";
	private static final String SENDING_COMPLETE = "SENDING_COMPLETE";
	private static final String RECEIVING_ACK = "RECEIVING_ACK";
	private static final String READ = "READ";
	private static final String WRITE = "WRITE";
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
	private long bytes_read;
	private Logger logger = Logger.getLogger("Main");

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

	public void processBuffer(SelectionKey key, String mode) throws IOException {
		if (state.equals(INIT) && mode.equals(READ)) {
			buffer.clear();
			readFromBuffer();
			buffer.flip();
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
			buffer.clear();
			rowIndex = network.mediator.getTableModel().getRowCount();
			network.mediator.getTableModel().addRow(
					new RowData(network.mediator.getUserName(), rqUserName,
							fileName));
			key.interestOps(SelectionKey.OP_WRITE);
			//logger.info("File requested is " + fileName);
			logger.info("File requested is " + path + "/" + fileName);
		} else if (state.equals(SENDING_SIZE) && mode.equals(WRITE)) {
			fileSize = sendingFile.length();
			buffer.clear();
			buffer.putLong(fileSize);
			buffer.flip();
			writeToBuffer();
			state = RECEIVING_ACK;
			key.interestOps(SelectionKey.OP_READ);
			logger.info("Sending file size " + sendingFile.length() + ".");
		} else if (state.equals(SENDING_FILE) && mode.equals(WRITE)) {
			long length;
			if (fileSize == 0) {
				network.mediator.getTableModel().updateStatus(rowIndex,100);
				state = SENDING_COMPLETE;
			} else if (bytes_read < fileSize) {
				length = fileSize - bytes_read < BUFFER_SIZE ? fileSize
						- bytes_read : BUFFER_SIZE;
				fileBuffer = fc.map(FileChannel.MapMode.READ_ONLY, bytes_read,
						length);
				buffer.putLong(length);
				buffer.put(fileBuffer);
				buffer.flip();
				bytes_read += length;
				writeToBuffer();
				logger.info("Sent " + bytes_read + " bytes!");
				network.mediator.getTableModel().updateStatus(rowIndex,
						(int) (bytes_read * 100 / fileSize));
				key.interestOps(SelectionKey.OP_READ);
				state = RECEIVING_ACK;
				System.out.println(rqUserName + " " + fileName);
				if (fileSize == bytes_read) {
					socketChannel.close();
					fc.close();
					raf.close();
					state = SENDING_COMPLETE;
				}
			} else {
				state = SENDING_COMPLETE;
			}
		} else if (state.equals(RECEIVING_ACK) && mode.equals(READ)) {
			long ack;
			buffer.clear();
			readFromBuffer();
			buffer.flip();
			if (!buffer.hasRemaining()) {
				return;
			}
			ack = buffer.getLong();
			if (ack == bytes_read) {
				logger.info("Received ack " + ack);
				state = SENDING_FILE;
				key.interestOps(SelectionKey.OP_WRITE);
			} else {
				logger.error("Error: Ack " + ack);
				state = SENDING_COMPLETE;
			}
			buffer.clear();
		} else if (state.equals(SENDING_COMPLETE)) {
			buffer.clear();
			logger.info("closing");
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
	
	private void readFromBuffer() throws IOException {
		while ((socketChannel.read(buffer)) > 0) {
			if (!buffer.hasRemaining()) {
				break;
			}
		}
	}
}
