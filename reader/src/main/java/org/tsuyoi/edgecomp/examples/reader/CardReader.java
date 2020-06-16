package org.tsuyoi.edgecomp.examples.reader;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hid4java.*;

public class CardReader {
    private final Integer vendorId;
    private final Integer productId;
    private final int packetLength;
    public String serialNumber;
    private final CardReaderTask task;
    private CardReaderWorker cardReaderWorker = null;

    public CardReader(Integer vendorId, Integer productId, int packetLength, String serialNumber,
                      CardReaderTask task) {
        this.vendorId = vendorId;
        this.productId = productId;
        this.packetLength = packetLength;
        this.serialNumber = serialNumber;
        this.task = task;
    }

    public void start() {
        if (cardReaderWorker == null) {
            cardReaderWorker = new CardReaderWorker(vendorId, productId, packetLength, serialNumber);
            new Thread(cardReaderWorker).start();
        } else {
            System.err.println("Card reader is already active");
        }
    }

    public void stop() {
        if (cardReaderWorker != null) {
            cardReaderWorker.stop();
            cardReaderWorker = null;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Shutdown sleep interrupted");
            }
        } else {
            System.err.println("Card reader is not running");
        }
    }

    private class CardReaderWorker implements Runnable {
        private final Integer vendorId;
        private final Integer productId;
        private final int packetLength;
        public String serialNumber;
        private boolean running = true;

        public CardReaderWorker(Integer vendorId, Integer productId, int packetLength, String serialNumber) {
            this.vendorId = vendorId;
            this.productId = productId;
            this.packetLength = packetLength;
            this.serialNumber = serialNumber;
        }

        public void stop() {
            this.running = false;
        }

        @Override
        public void run() {
            byte[] piece = new byte[packetLength];
            try {
                HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
                hidServicesSpecification.setAutoShutdown(true);
                hidServicesSpecification.setScanInterval(500);
                hidServicesSpecification.setPauseInterval(5000);
                hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);

                // Get HID services using custom specification
                HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);

                for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
                    System.out.println(hidDevice.toString());
                }

                // Open the device device by Vendor ID and Product ID with wildcard serial number
                HidDevice hidDevice = hidServices.getHidDevice(vendorId, productId, serialNumber);
                if (hidDevice == null) {
                    System.err.println("Magnetic card reader not found, aborting...");
                    return;
                }
                System.out.println("Now reading...");
                while (running) {
                    boolean moreData = true;
                    while (moreData) {
                        int val = hidDevice.read(piece, 500);
                        switch (val) {
                            case -1:
                                task.readError(hidDevice);
                                break;
                            case 0:
                                task.readComplete();
                                moreData = false;
                                break;
                            default:
                                task.readPiece(piece);
                                break;
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.err.println("Null pointer exception: " + e.getMessage());
                System.err.println("Null pointer exception:\n" + ExceptionUtils.getStackTrace(e));
            } catch (HidException e) {
                System.err.println("HID exception: " + e.getMessage());
                System.err.println("HID exception:\n" + ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
