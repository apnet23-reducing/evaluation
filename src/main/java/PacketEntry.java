public record PacketEntry(
        int timestamp,
        int packetLength,
        String srcRack,
        String dstRack,
        String srcPod,
        String dstPod) {
    public int getTimestamp() {
        return timestamp;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public String getSrcRack() {
        return srcRack;
    }

    public String getDstRack() {
        return dstRack;
    }

    public String getSrcPod() {
        return srcPod;
    }

    public String getDstPod() {
        return dstPod;
    }
}
