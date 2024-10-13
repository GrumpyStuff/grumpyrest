package io.github.grumpystuff.grumpyjson.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * NOT PUBLIC API
 */
public final class CloseShieldOutputStream extends OutputStream {

    private final OutputStream out;

    /**
     * NOT PUBLIC API
     *
     * @param out ...
     */
    public CloseShieldOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * NOT PUBLIC API
     *
     * @param b ...
     * @throws IOException ...
     */
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * NOT PUBLIC API
     *
     * @param b ...
     * @throws IOException ...
     */
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * NOT PUBLIC API
     *
     * @param b ...
     * @param off ...
     * @param len ...
     * @throws IOException ...
     */
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
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
