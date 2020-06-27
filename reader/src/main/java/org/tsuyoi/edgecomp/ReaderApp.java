package org.tsuyoi.edgecomp;

import org.tsuyoi.edgecomp.reader.CardReader;
import org.tsuyoi.edgecomp.reader.CardReaderTask;

public class ReaderApp {
    public static void main( String[] args ) {
        CardReaderTask task = new AppCardReaderTask();
        CardReader reader = new CardReader(0x0801, 0x01, 8, null, task);
        reader.start();
        while(true) {   // This is just to keep the application running while reading swipes

        }
    }
}
