package ca.mcgill.mcb.pcingola.bigDataScript.osCmd;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {

	OutputStream ostream1, ostream2;

	TeeOutputStream(OutputStream o1, OutputStream o2) throws IOException {
		ostream1 = o1;
		ostream2 = o2;
	}

	@Override
	public void close() throws IOException {
		// Do not close if they are STDOUT or STDERR
		if ((ostream1 != System.out) && (ostream1 != System.err)) ostream1.close();
		if ((ostream1 != System.out) && (ostream1 != System.err)) ostream2.close();
	}

	@Override
	public void flush() throws IOException {
		ostream1.flush();
		ostream2.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		ostream1.write(b, off, len);
		ostream2.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		byte[] buf = new byte[1];
		buf[0] = (byte) b;
		write(buf, 0, 1);
	}
}