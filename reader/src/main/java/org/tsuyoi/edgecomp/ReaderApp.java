package org.tsuyoi.edgecomp;

import org.tsuyoi.edgecomp.preader.CardReader;

public class ReaderApp {
    public static void main( String[] args ) {
        AppCardReaderTask task = new AppCardReaderTask();
        CardReader reader = new CardReader(0x0801, 0x01, 8, null, task);
        reader.start();
        while(true) {

        }
    }
}
