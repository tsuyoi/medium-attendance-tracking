package org.tsuyoi.edgecomp;

import org.hid4java.HidDevice;
import org.tsuyoi.edgecomp.preader.CardReaderTask;

import java.util.HashMap;
import java.util.Map;

public class AppCardReaderTask implements CardReaderTask {
    private String data;

    public AppCardReaderTask() {
        data = "";
    }

    @Override
    public void startLoop() {
        System.out.println("Starting loop");
    }

    @Override
    public void startRead() {
        System.out.println("Starting read");
    }

    @Override
    public void readError(HidDevice device) {
        System.out.println("Handling error");
        System.err.println("Error: " + device.getLastErrorMessage());
    }

    @Override
    public void readPiece(byte[] piece) {
        String character = Translator.translate(piece);
        if (character != null) {
            if (character.equals("\n")) {
                int stripeOneStart = data.indexOf("%") + 1;
                int stripeOneEnd = data.indexOf("?");
                int stripeTwoStart = data.indexOf(";") + 1;
                int stripeTwoEnd = data.indexOf("=");
                int stripeThreeStart = stripeTwoEnd + 1;
                int stripeThreeEnd = data.lastIndexOf("?");
                boolean hasStripeOne = stripeOneStart > 0;
                int stripeOneLength = stripeOneEnd - stripeOneStart;
                boolean hasStripeTwo = (stripeTwoEnd - stripeTwoStart) > 0;
                int stripeTwoLength = stripeTwoEnd - stripeTwoStart;
                boolean hasStripeThree = stripeThreeStart > 0;
                int stripeThreeLength = stripeThreeEnd - stripeThreeStart;
                String id = null;
                if (hasStripeOne && stripeOneLength == 9)
                    id = data.substring(stripeOneStart, stripeOneEnd);
                else if (hasStripeTwo && stripeTwoLength == 9)
                    id = data.substring(stripeTwoStart, stripeTwoEnd);
                else if (hasStripeThree && stripeThreeLength == 9)
                    id = data.substring(stripeThreeStart, stripeThreeEnd);
                System.out.println("Data: " + data);
                System.out.println("ID: " + id);
                data = "";
            } else {
                data += character;
            }
        }
    }

    @Override
    public void readComplete() {
        System.out.println("Read complete");
    }

    public static class Translator {
        private static final Map<String, String> SpecialMap = new HashMap<String, String>() {{
            put( "8", "E");
            put("34", "%");
            put("46", "+");
            put("56", "?");
        }};
        private static final Map<String, String> CharacterMap = new HashMap<String, String>() {{
            put("30", "1");
            put("31", "2");
            put("32", "3");
            put("33", "4");
            put("34", "5");
            put("35", "6");
            put("36", "7");
            put("37", "8");
            put("38", "9");
            put("39", "0");
            put("46", "=");
            put("51", ";");
            put("88", "\n");
        }};

        public static String translate(byte[] toTranslate) {
            if (toTranslate == null || toTranslate.length < 3)
                return null;
            String specialFlag = Byte.toString(toTranslate[0]);
            String characterFlag = Byte.toString(toTranslate[2]);
            if (!characterFlag.equals("0")) {
                String character = null;
                if (specialFlag.equals("2")) {
                    if (SpecialMap.containsKey(characterFlag)) {
                        character = SpecialMap.get(characterFlag);
                    } else {
                        System.err.println("Failed to find special character: " + characterFlag);
                    }
                } else if (specialFlag.equals("0")) {
                    if (CharacterMap.containsKey(characterFlag)) {
                        character = CharacterMap.get(characterFlag);
                    } else {
                        System.err.println("Failed to find regular character: " + characterFlag);
                    }
                } else {
                    System.err.println("Found erroneous special flag: " + specialFlag);
                }
                return character;
            } else {
                System.out.println("characterFlag is '0'");
                return null;
            }
        }
    }
}
