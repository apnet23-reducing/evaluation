import com.google.gson.Gson;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AggregateData {
    private enum RackPod {
        RACK, POD
    }

    public static void main(String[] args) throws IOException {
        String inputCsvPath = args[0];
        String inputJsonPath = args[1];
        String outputPath = args[2];
        String rackPodString = args[3];
        String intervalString = args[4];

        RackPod rackPod;
        if (rackPodString.equals("rack")) {
            rackPod = RackPod.RACK;
        } else if (rackPodString.equals("pod")) {
            rackPod = RackPod.POD;
        } else {
            throw new RuntimeException();
        }
        int interval = Integer.parseInt(intervalString);

        Metadata metadata;
        try (FileReader fileReader = new FileReader(inputJsonPath)) {
            Gson gson = new Gson();
            metadata = gson.fromJson(fileReader, Metadata.class);
        }

        int n = (int) Math.ceil((metadata.getMaxTimestamp() + 1.0) / interval);
        int m = switch (rackPod) {
            case RACK -> metadata.getNumRacks();
            case POD -> metadata.getNumPods();
        };

        int[][][] result = new int[n][m][m];
        try (FileReader fileReader = new FileReader(inputCsvPath)) {
            for (ProcessedPacketEntry entry : new CsvToBeanBuilder<ProcessedPacketEntry>(fileReader)
                    .withType(ProcessedPacketEntry.class).build()) {
                int src = switch (rackPod) {
                    case RACK -> entry.getSrcRack();
                    case POD -> entry.getSrcPod();
                };
                int dst = switch (rackPod) {
                    case RACK -> entry.getDstRack();
                    case POD -> entry.getDstPod();
                };
                int i = entry.getTimestamp() / interval;
                result[i][src][dst] += entry.getPacketLength();
            }
        }

        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            Gson gson = new Gson();
            TrafficMatrix matrix = new TrafficMatrix();
            matrix.setData(result);
            gson.toJson(matrix, fileWriter);
        }
    }
}
