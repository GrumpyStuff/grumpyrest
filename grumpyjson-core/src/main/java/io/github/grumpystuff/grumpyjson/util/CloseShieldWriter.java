package io.github.grumpystuff.grumpyjson.util;

import java.io.IOException;
import java.io.Writer;

/**
 * NOT PUBLIC API
 */
public final class CloseShieldWriter extends Writer {

    private final Writer out;

    /**
     * NOT PUBLIC API
     *
     * @param out ...
     */
    public CloseShieldWriter(Writer out) {
        this.out = out;
    }

    /**
     * NOT PUBLIC API
     *
     * @param c ...
     * @throws IOException ...
     */
    public void write(int c) throws IOException {
        out.write(c);
    }

    /**
     * NOT PUBLIC API
     *
     * @param cbuf ...
     * @throws IOException ...
     */
    public void write(char[] cbuf) throws IOException {
        out.write(cbuf);
    }

    /**
     * NOT PUBLIC API
     *
     * @param cbuf ...
     * @param off ...
     * @param len ...
     * @throws IOException ...
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    /**
     * NOT PUBLIC API
     *
     * @param str ...
     * @throws IOException ...
     */
    public void write(String str) throws IOException {
        out.write(str);
    }

    /**
     * NOT PUBLIC API
     *
     * @param str ...
     * @param off ...
     * @param len ...
     * @throws IOException ...
     */
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    /**
     * NOT PUBLIC API
     *
     * @param csq ...
     * @throws IOException ...
     */
    public Writer append(CharSequence csq) throws IOException {
        return out.append(csq);
    }

    /**
     * NOT PUBLIC API
     *
     * @param csq ...
     * @param start ...
     * @param end ...
     * @throws IOException ...
     */
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return out.append(csq, start, end);
    }

    /**
     * NOT PUBLIC API
     *
     * @param c ...
     * @throws IOException ...
     */
    public Writer append(char c) throws IOException {
        return out.append(c);
    }

    /**
     * NOT PUBLIC API
     *
     * @throws IOException ...
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * NOT PUBLIC API
     */
    public void close() {
        // do nothing
    }

}
