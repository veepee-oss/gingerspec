/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.privalia.qa.cucumber.reporter;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * A nice appendable that doesn't throw checked exceptions
 */
final class NiceAppendable implements Appendable {

    private static final CharSequence NL = "\n";

    private final Appendable out;

    public NiceAppendable(Appendable out) {
        this.out = out;
    }

    public NiceAppendable println() {
        return append(NL);
    }

    public NiceAppendable append(CharSequence csq) {
        try {
            out.append(csq);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NiceAppendable append(CharSequence csq, int start, int end) {
        try {
            out.append(csq, start, end);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NiceAppendable append(char c) {
        try {
            out.append(c);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryFlush() {
        if (!(out instanceof Flushable)) {
            return;
        }

        try {
            ((Flushable) out).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NiceAppendable println(CharSequence csq) {
        try {
            out.append(csq).append(NL);
            tryFlush();
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            tryFlush();
            if (out instanceof Closeable) {
                ((Closeable) out).close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
