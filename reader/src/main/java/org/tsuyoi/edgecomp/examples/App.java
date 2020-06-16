package org.tsuyoi.edgecomp.examples;

import org.tsuyoi.edgecomp.examples.reader.CardReader;

public class App {
    public static void main( String[] args ) {
        AppCardReaderTask task = new AppCardReaderTask();
        CardReader reader = new CardReader(0x0801, 0x01, 8, null, task);
        reader.start();
        while(true) {

        }
    }
}
