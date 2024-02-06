package com.github.terasscriptnest;

import java.awt.EventQueue;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;

/*
 * 	****	DYNAMIC CLASS 	****
 */
public class JComponentOutputStream extends OutputStream {

	/* Attributes */
	private byte[] oneByte; // array for write(int val);
	private Appender appender; // most recent action
	private Lock jcosLock = new ReentrantLock();

	/* Constructor */
	public JComponentOutputStream(JComponent txtara, JComponentHandler handler) {
		this(txtara, 1000, handler);
	}

	public JComponentOutputStream(JComponent txtara, int maxlin, JComponentHandler handler) {
		if (maxlin < 1) {
			throw new IllegalArgumentException(
					"JComponentOutputStream maximum lines must be positive (value=" + maxlin + ")");
		}
		oneByte = new byte[1];
		appender = new Appender(txtara, maxlin, handler);
	}

	/* Methods */
	/** Clear the current console text area. */
	public void clear() {
		jcosLock.lock();
		try {
			if (appender != null) {
				appender.clear();
			}
		} finally {
			jcosLock.unlock();
		}
	}

	public void close() {
		jcosLock.lock();
		try {
			appender = null;
		} finally {
			jcosLock.unlock();
		}
	}

	public void flush() {
		// sstosLock.lock();
		// try {
		// // TODO: Add necessary code here...
		// } finally {
		// sstosLock.unlock();
		// }
	}

	public void write(int val) {
		jcosLock.lock();
		try {
			oneByte[0] = (byte) val;
			write(oneByte, 0, 1);
		} finally {
			jcosLock.unlock();
		}
	}

	public void write(byte[] ba) {
		jcosLock.lock();
		try {
			write(ba, 0, ba.length);
		} finally {
			jcosLock.unlock();
		}
	}

	public void write(byte[] ba, int str, int len) {
		jcosLock.lock();
		try {
			if (appender != null) {
				appender.append(bytesToString(ba, str, len));
			}
		} finally {
			jcosLock.unlock();
		}
	}

	static private String bytesToString(byte[] ba, int str, int len) {
		try {
			return new String(ba, str, len, "UTF-8");
		} catch (UnsupportedEncodingException thr) {
			return new String(ba, str, len);
		} // all JVMs are required to support UTF-8
	}

	/*
	 * **** STATIC CLASS ****
	 */
	static class Appender implements Runnable {
		/* Attributes */
		private final JComponent swingComponent;
		private final int maxLines; // Max lines allowed in text area
		private final LinkedList<Integer> lengths; // Length of lines in text area
		private final List<String> values; // Values waiting to be appended
		private int curLength; // Length of current line
		private boolean clear, queue;
		private Lock appenderLock;
		private JComponentHandler handler;

		/* Constructor */
		Appender(JComponent cpt, int maxlin, JComponentHandler hndlr) {
			appenderLock = new ReentrantLock();

			swingComponent = cpt;
			maxLines = maxlin;
			lengths = new LinkedList<Integer>();
			values = new ArrayList<String>();
			curLength = 0;
			clear = false;
			queue = true;

			handler = hndlr;
		}

		/* Methods */
		void append(String val) {
			appenderLock.lock();
			try {
				values.add(val);
				if (queue) {
					queue = false;
					EventQueue.invokeLater(this);
				}
			} finally {
				appenderLock.unlock();
			}
		}

		void clear() {
			appenderLock.lock();
			try {
				clear = true;
				curLength = 0;
				lengths.clear();
				values.clear();
				if (queue) {
					queue = false;
					EventQueue.invokeLater(this);
				}
			} finally {
				appenderLock.unlock();
			}
		}

		// MUST BE THE ONLY METHOD THAT TOUCHES the JComponent!
		public void run() {
			appenderLock.lock();
			try {
				if (clear) {
					handler.setText(swingComponent, "");
				}
				for (String val : values) {
					curLength += val.length();
					if (val.endsWith(EOL1) || val.endsWith(EOL2)) {
						if (lengths.size() >= maxLines) {
							handler.replaceRange(swingComponent, "", 0, lengths.removeFirst());
						}
						lengths.addLast(curLength);
						curLength = 0;
					}
					handler.append(swingComponent, val);
				}
				values.clear();
				clear = false;
				queue = true;
			} finally {
				appenderLock.unlock();
			}
		}

		static private final String EOL1 = "\n";
		static private final String EOL2 = System.getProperty("line.separator", EOL1);
	}

	/*
	 * **** INTERFACE ****
	 */
	public interface JComponentHandler {
		void setText(JComponent swingComponent, String text);

		void replaceRange(JComponent swingComponent, String text, int start, int end);

		void append(JComponent swingComponent, String text);
	}

}
