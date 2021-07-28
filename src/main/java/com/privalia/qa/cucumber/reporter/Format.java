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

interface Format {

    String text(String text);

    static Format color(AnsiEscapes... escapes) {
        return new Color(escapes);
    }

    static Format monochrome() {
        return new Monochrome();
    }

    final class Color implements Format {

        private final AnsiEscapes[] escapes;

        private Color(AnsiEscapes... escapes) {
            this.escapes = escapes;
        }

        public String text(String text) {
            StringBuilder sb = new StringBuilder();
            for (AnsiEscapes escape : escapes) {
                escape.appendTo(sb);
            }
            sb.append(text);
            if (escapes.length > 0) {
                AnsiEscapes.RESET.appendTo(sb);
            }
            return sb.toString();
        }

    }

    class Monochrome implements Format {

        private Monochrome() {

        }

        @Override
        public String text(String text) {
            return text;
        }

    }

}
