package io.github.grumpystuff.grumpyjson.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

public final class CloseShieldReader extends Reader {

    private final Reader in;

    public CloseShieldReader(Reader in) {
        this.in = in;
    }

    public int read(CharBuffer target) throws IOException {
        return in.read(target);
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(char[] cbuf) throws IOException {
        return in.read(cbuf);
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return in.read(cbuf, off, len);
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    public boolean ready() throws IOException {
        return in.ready();
    }

    public boolean markSupported() {
        return in.markSupported();
    }

    public void mark(int readAheadLimit) throws IOException {
        in.mark(readAheadLimit);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public void close() {
        // do nothing
    }

    public long transferTo(Writer out) throws IOException {
        return in.transferTo(out);
    }

}
