/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.system;

import com.android.ddmlib.IShellOutputReceiver;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Simple output-collecting command receiver
 */
public class StringBufferIShellOutputReceiver implements IShellOutputReceiver {
	private final StringBuilder sb = new StringBuilder();

	@Override
	public void addOutput(byte[] byteArray, int offset, int length) {
		Charset latin1Charset = Charset.forName("ISO-8859-1");
		CharBuffer charBuffer = latin1Charset.decode(ByteBuffer.wrap(byteArray));
		sb.append(charBuffer.toString(), offset, length);
	}

	@Override
	public void flush() {
		// Nothing special to do on command completion
	}

	@Override
	public boolean isCancelled() {
		return false; // No reason to cancel - perhaps
	}

	public StringBuilder getOutput() {
		return sb;
	}
}
