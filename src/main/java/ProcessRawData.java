import com.google.gson.stream.JsonWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ProcessRawData {
    public static void main(String[] args) throws IOException {
        String inputCsvPath = args[0];
        String outputCsvPath = args[1];
        String outputJsonPath = args[2];

        Set<String> racks = new HashSet<>();
        Set<String> pods = new HashSet<>();
        int maxTimestamp = 0;
        int numPackets = 0;
        try (FileReader fileReader = new FileReader(inputCsvPath);
             RawDataIterator iterator = new RawDataIterator(fileReader)) {
            while (iterator.hasNext()) {
                PacketEntry entry = iterator.next();
                maxTimestamp = entry.getTimestamp();
                numPackets++;
                racks.add(entry.getSrcRack());
                racks.add(entry.getDstRack());
                pods.add(entry.getSrcPod());
                pods.add(entry.getDstPod());
            }
        }

        List<String> rackList = new ArrayList<>(racks);
        Collections.sort(rackList);
        List<String> podList = new ArrayList<>(pods);
        Collections.sort(podList);
        Map<String, Integer> rackToIndex = new HashMap<>();
        for (int i = 0; i < rackList.size(); ++i) {
            rackToIndex.put(rackList.get(i), i);
        }
        Map<String, Integer> podToIndex = new HashMap<>();
        for (int i = 0; i < podList.size(); ++i) {
            podToIndex.put(podList.get(i), i);
        }

        try (FileReader fileReader = new FileReader(inputCsvPath);
             RawDataIterator iterator = new RawDataIterator(fileReader);
             FileWriter fileWriter = new FileWriter(outputCsvPath);
             ICSVWriter writer = new CSVWriterBuilder(fileWriter).build()) {
            String[] headers = new String[]{"timestamp", "packetLength", "srcRack", "dstRack", "srcPod", "dstPod"};
            writer.writeNext(headers);
            while (iterator.hasNext()) {
                PacketEntry entry = iterator.next();
                int srcRack = rackToIndex.get(entry.getSrcRack());
                int dstRack = rackToIndex.get(entry.getDstRack());
                int srcPod = podToIndex.get(entry.getSrcPod());
                int dstPod = podToIndex.get(entry.getDstPod());
                int[] line = new int[]{entry.getTimestamp(), entry.getPacketLength(), srcRack, dstRack, srcPod, dstPod};
                String[] line2 = new String[line.length];
                for (int i = 0; i < line.length; i++) {
                    line2[i] = Integer.toString(line[i]);
                }
                writer.writeNext(line2);
            }
        }

        try (FileWriter fileWriter = new FileWriter(outputJsonPath);
             JsonWriter writer = new JsonWriter(fileWriter)) {
            writer.beginObject();
            writer.name("maxTimestamp").value(maxTimestamp);
            writer.name("numPackets").value(numPackets);
            writer.name("numRacks").value(racks.size());
            writer.name("numPods").value(pods.size());
            writer.endObject();
        }
    }
}
