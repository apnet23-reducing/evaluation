import com.opencsv.bean.CsvBindByName;

public class ProcessedPacketEntry {
    @CsvBindByName private int timestamp;
    @CsvBindByName private int packetLength;
    @CsvBindByName private int srcRack;
    @CsvBindByName private int dstRack;
    @CsvBindByName private int srcPod;
    @CsvBindByName private int dstPod;

    public int getTimestamp() {
        return timestamp;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public int getSrcRack() {
        return srcRack;
    }

    public int getDstRack() {
        return dstRack;
    }

    public int getSrcPod() {
        return srcPod;
    }

    public int getDstPod() {
        return dstPod;
    }
}
