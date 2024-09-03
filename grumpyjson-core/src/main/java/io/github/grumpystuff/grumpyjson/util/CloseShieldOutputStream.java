package io.github.grumpystuff.grumpyjson.util;

import java.io.IOException;
import java.io.OutputStream;

public final class CloseShieldOutputStream extends OutputStream {

    private final OutputStream out;

    public CloseShieldOutputStream(OutputStream out) {
        this.out = out;
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() {
        // do nothing
    }

}
