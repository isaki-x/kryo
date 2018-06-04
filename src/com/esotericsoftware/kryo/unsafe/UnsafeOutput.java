
package com.esotericsoftware.kryo.unsafe;

import static com.esotericsoftware.kryo.unsafe.UnsafeUtil.*;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

import java.io.OutputStream;

/** An {@link Output} that reads data using sun.misc.Unsafe. Multi-byte primitive types use native byte order, so the native byte
 * order on different computers which read and write the data must be the same.
 * @author Roman Levenstein <romixlev@gmail.com> */
public class UnsafeOutput extends Output {
	/** Creates an uninitialized Output, {@link #setBuffer(byte[], int)} must be called before the Output is used. */
	public UnsafeOutput () {
	}

	/** Creates a new Output for writing to a byte[].
	 * @param bufferSize The size of the buffer. An exception is thrown if more bytes than this are written and {@link #flush()}
	 *           does not empty the buffer. */
	public UnsafeOutput (int bufferSize) {
		this(bufferSize, bufferSize);
	}

	/** Creates a new Output for writing to a byte[].
	 * @param bufferSize The initial size of the buffer.
	 * @param maxBufferSize If {@link #flush()} does not empty the buffer, the buffer is doubled as needed until it exceeds
	 *           maxBufferSize and an exception is thrown. Can be -1 for no maximum. */
	public UnsafeOutput (int bufferSize, int maxBufferSize) {
		super(bufferSize, maxBufferSize);
	}

	/** Creates a new Output for writing to a byte[].
	 * @see #setBuffer(byte[]) */
	public UnsafeOutput (byte[] buffer) {
		this(buffer, buffer.length);
	}

	/** Creates a new Output for writing to a byte[].
	 * @see #setBuffer(byte[], int) */
	public UnsafeOutput (byte[] buffer, int maxBufferSize) {
		super(buffer, maxBufferSize);
	}

	/** Creates a new Output for writing to an OutputStream. A buffer size of 4096 is used. */
	public UnsafeOutput (OutputStream outputStream) {
		super(outputStream);
	}

	/** Creates a new Output for writing to an OutputStream with the specified buffer size. */
	public UnsafeOutput (OutputStream outputStream, int bufferSize) {
		super(outputStream, bufferSize);
	}

	public void writeInt (int value) throws KryoException {
		require(4);
		unsafe.putInt(buffer, byteArrayBaseOffset + position, value);
		position += 4;
	}

	public void writeLong (long value) throws KryoException {
		require(8);
		unsafe.putLong(buffer, byteArrayBaseOffset + position, value);
		position += 8;
	}

	public void writeFloat (float value) throws KryoException {
		require(4);
		unsafe.putFloat(buffer, byteArrayBaseOffset + position, value);
		position += 4;
	}

	public void writeDouble (double value) throws KryoException {
		require(8);
		unsafe.putDouble(buffer, byteArrayBaseOffset + position, value);
		position += 8;
	}

	public void writeShort (int value) throws KryoException {
		require(2);
		unsafe.putShort(buffer, byteArrayBaseOffset + position, (short)value);
		position += 2;
	}

	public void writeChar (char value) throws KryoException {
		require(2);
		unsafe.putChar(buffer, byteArrayBaseOffset + position, value);
		position += 2;
	}

	public void writeBoolean (boolean value) throws KryoException {
		require(1);
		unsafe.putByte(buffer, byteArrayBaseOffset + position, value ? (byte)1 : 0);
		position++;
	}

	public void writeInts (int[] array) throws KryoException {
		writeBytes(array, intArrayBaseOffset, array.length << 2);
	}

	public void writeLongs (long[] array) throws KryoException {
		writeBytes(array, longArrayBaseOffset, array.length << 3);
	}

	public void writeFloats (float[] array) throws KryoException {
		writeBytes(array, floatArrayBaseOffset, array.length << 2);
	}

	public void writeDoubles (double[] array) throws KryoException {
		writeBytes(array, doubleArrayBaseOffset, array.length << 3);
	}

	public void writeShorts (short[] array) throws KryoException {
		writeBytes(array, shortArrayBaseOffset, array.length << 1);
	}

	public void writeChars (char[] array) throws KryoException {
		writeBytes(array, charArrayBaseOffset, array.length << 1);
	}

	public void writeBooleans (boolean[] array, int offset, int count) throws KryoException {
		writeBytes(array, booleanArrayBaseOffset, array.length << 1);
	}

	public void writeBytes (byte[] array, int offset, int count) throws KryoException {
		writeBytes(array, byteArrayBaseOffset + offset, count);
	}

	/** Write count bytes to the byte buffer, reading from the given offset inside the in-memory representation of the object. */
	public void writeBytes (Object from, long offset, int count) throws KryoException {
		int copyCount = Math.min(capacity - position, count);
		while (true) {
			unsafe.copyMemory(from, offset, buffer, byteArrayBaseOffset + position, copyCount);
			position += copyCount;
			count -= copyCount;
			if (count == 0) break;
			offset += copyCount;
			copyCount = Math.min(capacity, count);
			require(copyCount);
		}
	}
}
