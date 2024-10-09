package io.github.grumpystuff.grumpyjson.util;

import java.io.IOException;
import java.io.Writer;

public final class CloseShieldWriter extends Writer {

    private final Writer out;

    public CloseShieldWriter(Writer out) {
        this.out = out;
    }

    public void write(int c) throws IOException {
        out.write(c);
    }

    public void write(char[] cbuf) throws IOException {
        out.write(cbuf);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    public void write(String str) throws IOException {
        out.write(str);
    }

    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    public Writer append(CharSequence csq) throws IOException {
        return out.append(csq);
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return out.append(csq, start, end);
    }

    public Writer append(char c) throws IOException {
        return out.append(c);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() {
        // do nothing
    }

}
