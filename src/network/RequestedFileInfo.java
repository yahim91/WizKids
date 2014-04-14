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

public class RequestedFileInfo {

	private static final int BUF_SIZE = 1024;
	private static final String INIT = "INIT";
	private static final String SENDING_SIZE = "SENDING_SIZE";
	private static final String SENDING_FILE = "SENDING_FILE";
	private static final String SENDING_COMPLETE = "SENDING_COMPLETE";
	private ByteBuffer buffer;
	private String state = INIT;
	private String fileName;
	private File sendingFile;
	private String path;
	private RandomAccessFile raf;
	private FileChannel fc;
	private MappedByteBuffer fileBuffer;
	private SocketChannel socketChannel;
	private Integer written_bytes;

	public RequestedFileInfo(String path, SocketChannel socketChannel) {
		buffer = ByteBuffer.allocateDirect(BUF_SIZE);
		this.path = path;
		this.socketChannel = socketChannel;
		this.written_bytes = 0;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void processBuffer(SelectionKey key) throws IOException {
		System.out.println("State is " + state);
		if (state.equals(INIT)) {
			if (readFromBuffer() <= 0)
				return;
			buffer.flip();
			byte[] cmd = new byte[buffer.getInt()];
			buffer.get(cmd);
			fileName = new String(cmd);
			sendingFile = new File(path + "/" + fileName);
			raf = new RandomAccessFile(sendingFile, "rw");
			fc = raf.getChannel();
			fileBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size());
			state = "SENDING_SIZE";
			System.out.println("file name is " + path + "/" + fileName);
			buffer.clear();
			key.interestOps(SelectionKey.OP_WRITE);
		} else if (state.equals(SENDING_SIZE)) {
			buffer.putInt((int) sendingFile.length());
			System.out.println("sending file size " + sendingFile.length());
			buffer.flip();
			writeToBuffer();
			state = SENDING_FILE;
		} else if (state.equals(SENDING_FILE)) {
			fileBuffer.flip();
			while(written_bytes < sendingFile.length()) {
				written_bytes += socketChannel.write(fileBuffer);
			}
			if (written_bytes >= sendingFile.length())
				state = SENDING_COMPLETE;
		} else if(state.equals(SENDING_COMPLETE)) {
			buffer.clear();
			socketChannel.close();
			//key.selector().close();
			fc.close();
			raf.close();
		}
	}

	private void writeToBuffer() throws IOException {
		while (socketChannel.write(buffer) > 0) {
			if (!buffer.hasRemaining()) {
				buffer.clear();
			}
		}
	}

	private int readFromBuffer() throws IOException {
		return socketChannel.read(buffer);

	}

}
