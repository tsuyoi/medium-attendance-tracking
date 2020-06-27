package org.tsuyoi.edgecomp.reader;

import org.hid4java.HidDevice;

public interface CardReaderTask {
    void startLoop();
    void startRead();
    void readError(HidDevice device);
    void readPiece(byte[] piece);
    void readComplete();
}
