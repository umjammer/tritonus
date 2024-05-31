package org.tritonus.lowlevel.gsm;

public class BitDecoder {

    public enum AllocationMode {
        MSBitFirst, LSBitFirst
    }

    private final AllocationMode allocationMode;

    private byte[] codedFrame;
    private int codedFrameByteIndex;
    private int sr;
    private int currentBits;

    /**
     * Constructor.
     *
     * @param codedBytes
     * @param allocationMode
     */
    public BitDecoder(byte[] codedBytes, int bufferStartIndex, AllocationMode allocationMode) {
        super();
        this.allocationMode = allocationMode;
        codedFrame = codedBytes;
        codedFrameByteIndex = bufferStartIndex;
        sr = 0;
        currentBits = 0;
    }

    public void setCodedFrame(byte[] c, int bufferStartIndex) {
        codedFrame = c;
        codedFrameByteIndex = bufferStartIndex;
    }

    private void addNextCodedByteValue() {
        sr |= getNextCodedByteValue() << currentBits;
        currentBits += 8;
    }

    private int getNextCodedByteValue() {
        int value = codedFrame[codedFrameByteIndex];
        codedFrameByteIndex++;
        return value & 0xFF;
    }

    public final int getNextBits(int bits) {
        switch (allocationMode) {
        case LSBitFirst:
            while (currentBits < bits) {
                addNextCodedByteValue();
            }
            int value = sr & GsmDef.BITMASKS[bits];
            sr >>>= bits;
            currentBits -= bits;
            return value;
        case MSBitFirst:
        default:
            throw new RuntimeException("not supported");
        }
    }
}
