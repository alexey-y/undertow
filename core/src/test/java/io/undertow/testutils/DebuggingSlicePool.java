package io.undertow.testutils;

import org.xnio.Pool;
import org.xnio.Pooled;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stuart Douglas
 */
public class DebuggingSlicePool implements Pool<ByteBuffer>{

    static final Set<DebuggingBuffer> BUFFERS = Collections.newSetFromMap(new ConcurrentHashMap<DebuggingBuffer, Boolean>());
    static volatile String currentLabel;

    private final Pool<ByteBuffer> delegate;

    public DebuggingSlicePool(Pool<ByteBuffer> delegate) {
        this.delegate = delegate;
    }


    @Override
    public Pooled<ByteBuffer> allocate() {
        final Pooled<ByteBuffer> delegate = this.delegate.allocate();
        return new DebuggingBuffer(delegate, currentLabel);
    }

    static class DebuggingBuffer implements Pooled<ByteBuffer> {

        private final RuntimeException allocationPoint;
        private final Pooled<ByteBuffer> delegate;
        private final String label;

        public DebuggingBuffer(Pooled<ByteBuffer> delegate, String label) {
            this.delegate = delegate;
            this.label = label;
            allocationPoint = new RuntimeException();
            BUFFERS.add(this);
        }

        @Override
        public void discard() {
            BUFFERS.remove(this);
            delegate.discard();
        }

        @Override
        public void free() {
            BUFFERS.remove(this);
            delegate.free();
        }

        @Override
        public ByteBuffer getResource() throws IllegalStateException {
            return delegate.getResource();
        }

        @Override
        public void close() {
        }

        RuntimeException getAllocationPoint() {
            return allocationPoint;
        }

        String getLabel() {
            return label;
        }
    }
}
