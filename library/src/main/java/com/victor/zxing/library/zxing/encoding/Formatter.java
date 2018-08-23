package com.victor.zxing.library.zxing.encoding;

/**
 * Encapsulates some simple formatting logic, to aid refactoring in {@link ContactEncoder}.
 */
interface Formatter {

    /**
     * @param value value to format
     * @param index index of value in a list of values to be formatted
     * @return formatted value
     */
    CharSequence format(CharSequence value, int index);

}
