import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

public class RawDataIterator implements Iterator<PacketEntry>, Closeable {
    CSVReaderHeaderAware csvReader;
    PacketEntry next;
    int firstTimestamp = Integer.MIN_VALUE;

    RawDataIterator(Reader reader) throws IOException {
        csvReader = new CSVReaderHeaderAware(reader);
        getNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public PacketEntry next() {
        PacketEntry ret = next;
        getNext();
        return ret;
    }

    private void getNext() {
        while (true) {
            Map<String, String> line;
            try {
                line = csvReader.readMap();
            } catch (IOException | CsvValidationException e) {
                throw new RuntimeException(e);
            }
            if (line == null) {
                next = null;
                break;
            }
            if (line.get("intercluster").equals("true")) {
                continue;
            }
            String srcRack = line.get("srcrack");
            String dstRack = line.get("dstrack");
            String srcPod = line.get("srcpod");
            String dstPod = line.get("dstpod");
            if (srcRack.equals("N") || dstRack.equals("N") || srcPod.equals("N") || dstPod.equals("N")) {
                continue;
            }
            int timestamp =  Integer.parseInt(line.get("timestamp"));
            if (firstTimestamp == Integer.MIN_VALUE) {
                firstTimestamp = timestamp;
            }
            timestamp -= firstTimestamp;
            int packetLength = Integer.parseInt(line.get("packetlength"));
            next = new PacketEntry(timestamp, packetLength, srcRack, dstRack, srcPod, dstPod);
            break;
        }
    }

    @Override
    public void close() throws IOException {
        csvReader.close();
    }
}
