package org.tsuyoi.edgecomp.examples.reader;

import org.hid4java.HidDevice;

public interface CardReaderTask {
    public void startLoop();
    public void startRead();
    public void readError(HidDevice device);
    public void readPiece(byte[] piece);
    public void readComplete();
}
