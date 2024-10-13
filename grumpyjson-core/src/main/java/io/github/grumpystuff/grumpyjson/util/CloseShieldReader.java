package io.github.grumpystuff.grumpyjson.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * NOT PUBLIC API
 */
public final class CloseShieldReader extends Reader {

    private final Reader in;

    /**
     * NOT PUBLIC API
     *
     * @param in ...
     */
    public CloseShieldReader(Reader in) {
        this.in = in;
    }

    /**
     * NOT PUBLIC API
     *
     * @param target ...
     * @return ...
     * @throws IOException ...
     */
    public int read(CharBuffer target) throws IOException {
        return in.read(target);
    }

    /**
     * NOT PUBLIC API
     *
     * @return ...
     * @throws IOException ...
     */
    public int read() throws IOException {
        return in.read();
    }

    /**
     * NOT PUBLIC API
     *
     * @param cbuf ...
     * @return ...
     * @throws IOException ...
     */
    public int read(char[] cbuf) throws IOException {
        return in.read(cbuf);
    }

    /**
     * NOT PUBLIC API
     *
     * @param cbuf ...
     * @param off ...
     * @param len ...
     * @return ...
     * @throws IOException ...
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
        return in.read(cbuf, off, len);
    }

    /**
     * NOT PUBLIC API
     *
     * @param n ...
     * @return ...
     * @throws IOException ...
     */
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * NOT PUBLIC API
     *
     * @throws IOException ...
     */
    public boolean ready() throws IOException {
        return in.ready();
    }

    /**
     * NOT PUBLIC API
     *
     * @return ...
     */
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * NOT PUBLIC API
     *
     * @param readAheadLimit ...
     * @throws IOException ...
     */
    public void mark(int readAheadLimit) throws IOException {
        in.mark(readAheadLimit);
    }

    /**
     * NOT PUBLIC API
     *
     * @throws IOException ...
     */
    public void reset() throws IOException {
        in.reset();
    }

    /**
     * NOT PUBLIC API
     */
    public void close() {
        // do nothing
    }

    /**
     * NOT PUBLIC API
     *
     * @param out ...
     * @return ...
     * @throws IOException ...
     */
    public long transferTo(Writer out) throws IOException {
        return in.transferTo(out);
    }

}
