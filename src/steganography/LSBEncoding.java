package steganography;

import java.awt.image.BufferedImage;
import utility.ImageUtility;

public class LSBEncoding {

    ImageUtility imageUtility;

    public LSBEncoding() {
        imageUtility = new ImageUtility();
    }

    // Method to encode text into the image
    public void encodeText(BufferedImage coverImage, String message, int bitArray[]) {
        // Append a null character '\0' to indicate the end of the message
        message += "\0";
        
        byte image[] = imageUtility.getByteData(coverImage);
        byte payload[] = message.getBytes();
        int offset = 0;
        int imageLength = image.length;
        
        // Convert the payload to bits (including the null character)
        boolean data[] = convertToBits(payload);
        int dataLength = data.length;
        int dataOverFlag = 0;

        // Iterate over the image data to embed the message bits
        for (int i = 0; i < imageLength && dataOverFlag == 0; i++) {
            for (int j = 7; j >= 0 && dataOverFlag == 0; j--) {
                if (bitArray[j] == 1) {
                    int mask = returnMask(j);
                    image[i] = (byte) (image[i] & mask);
                    
                    // Embed the next bit from the message
                    if (data[offset++]) {
                        image[i] = (byte) (image[i] | ~mask);
                    }
                    
                    // Check if we have embedded the entire message
                    if (offset >= dataLength) {
                        dataOverFlag = 1;
                    }
                }
            }
        }
    }

    // Method to generate the mask for a particular bit position
    private int returnMask(int bit) {
        int mask = 0xFF;
        switch (bit) {
            case 0:
                mask = 0xFE; break;
            case 1:
                mask = 0xFD; break;
            case 2:
                mask = 0xFB; break;
            case 3:
                mask = 0xF7; break;
            case 4:
                mask = 0xEF; break;
            case 5:
                mask = 0xDF; break;
            case 6:
                mask = 0xBF; break;
            case 7:
                mask = 0x7F; break;
        }
        return mask;
    }

    // Method to convert the payload bytes into a boolean array (representing bits)
    private boolean[] convertToBits(byte payload[]) {
        boolean result[] = new boolean[8 * payload.length];
        int offset = 0;
        for (byte b : payload) {
            for (int i = 7; i >= 0; i--) {
                int singleBit = (b >> i) & 1;
                result[offset++] = singleBit == 1;
            }
        }
        return result;
    }

    // Method to decode text from the image
    public String decodeText(BufferedImage coverImage, int bitArray[]) {
        byte image[] = imageUtility.getByteData(coverImage);
        int offset = 0;
        int imageLength = image.length;
        
        // Count the number of modified bits per byte
        int bitCount = 0;
        for (int i = 0; i < bitArray.length; i++) {
            if (bitArray[i] == 1) {
                bitCount++;
            }
        }

        // Prepare a boolean array to store the extracted bits
        boolean data[] = new boolean[imageLength * bitCount];
        for (int i = 0; i < imageLength; i++) {
            for (int j = 7; j >= 0; j--) {
                if (bitArray[j] == 1) {
                    int singleBit = (image[i] >> j) & 1;
                    data[offset++] = singleBit == 1;
                }
            }
        }

        // Convert the boolean array to a byte array (message)
        int secretMessageLength = (imageLength * bitCount) / 8;
        byte secretMessage[] = new byte[secretMessageLength];
        for (int i = 0; i < secretMessageLength; i++) {
            for (int bit = 0; bit < 8; bit++) {
                if (data[i * 8 + bit]) {
                    secretMessage[i] |= (128 >> bit);
                }
            }
        }

        try {
            String decodedMessage = new String(secretMessage, "ASCII");

            // Truncate the message at the null character '\0' to avoid extra symbols
            int nullCharIndex = decodedMessage.indexOf('\0');
            if (nullCharIndex != -1) {
                decodedMessage = decodedMessage.substring(0, nullCharIndex);
            }

            return decodedMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
