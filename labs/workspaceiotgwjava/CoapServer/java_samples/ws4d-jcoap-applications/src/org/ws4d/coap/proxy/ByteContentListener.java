/*
 * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work has been sponsored by Siemens Corporate Technology. 
 *
 */

package org.ws4d.coap.proxy;

import java.io.IOException;

import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ContentListener;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;

/**
 * This class is used to consume an entity and get the entity-data as byte-array.
 * The only other class which implements ContentListener is SkipContentListener.
 * SkipContentListener is ignoring all content.
 * Look at Apache HTTP Components Core NIO Framework -> Java-Documentation of SkipContentListener. 
 */

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

class ByteContentListener implements ContentListener {
    final SimpleInputBuffer input = new SimpleInputBuffer(2048, new HeapByteBufferAllocator());

    public void consumeContent(ContentDecoder decoder, IOControl ioctrl)
            throws IOException {
        input.consumeContent(decoder);
    }

    public void finish() {
        input.reset();
    }

    byte[] getContent() throws IOException {
        byte[] b = new byte[input.length()];
        input.read(b);
        return b;
    }

	@Override
	public void contentAvailable(ContentDecoder decoder, IOControl arg1)
			throws IOException {
		input.consumeContent(decoder);
		
	}

	@Override
	public void finished() {
		input.reset();					
	}
}
