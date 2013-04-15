/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 2012-07-15
 *
 */

package org.biojava.core.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple buffered locking output stream for concurrent writing in a multithreaded environment.
 * Preserves in the stream the order in which data is added to the buffer.
 * @author dmyerstu
 *
 */
public class SynchronizedOutStream extends OutputStream implements Closeable {

	private byte[] buffer;

	private final int bufferSize;
	private int index = 0;
	private final Object mainLock = new Object();
	private OutputStream os;

	public SynchronizedOutStream(File f) throws IOException {
		this(new FileOutputStream(f));
	}

	public SynchronizedOutStream(OutputStream os) {
		this(os, 1024);
	}

	public SynchronizedOutStream(OutputStream os, int bufferSize) {
		this.os = os;
		this.bufferSize = bufferSize;
		buffer = new byte[bufferSize];
	}

	@Override
	public void close() throws IOException {
		os.close();
	}

	@Override
	public void flush() throws IOException {
		synchronized (mainLock) { // since this is a public method
			for (int i = 0; i < index; i++) { // don't use os.write(byte[]), since we don't want to write trailing 0s
				os.write(buffer[i]);
			}
			index = 0;
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		synchronized (mainLock) {
			for (byte element : b) {
				if (index >= bufferSize) flush();
				buffer[index] = element;
				index++;
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		byte[] by = new byte[1];
		by[0] = (byte) b;
		write(by);
	}

	public void print(Object o) throws IOException {
		if (o == null) return;
		if (o instanceof String) {
			write((String) o);
		} else if (o instanceof byte[]) {
			write((byte[]) o);
		} else {
			write(o.toString());
		}
	}

	public void write(String s) throws IOException {
		synchronized (mainLock) {
			os.write(s.getBytes());
		}
	}

}
