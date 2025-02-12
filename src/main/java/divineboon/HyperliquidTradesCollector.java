package divineboon;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.DecimalVector;
import org.apache.arrow.vector.TimeStampMicroVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.types.TimeUnit;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HyperliquidTradesCollector {
    private static final String WEBSOCKET_URL = "wss://api.hyperliquid.xyz/ws";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final BlockingQueue<JsonNode> messageQueue = new LinkedBlockingQueue<>();

    private static final int BATCH_SIZE = 1000;

    private static class TradesWebSocketClient extends WebSocketClient {
        public TradesWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            String subscribeMessage = "{\"method\":\"sub\",\"subscription\":{\"type\":\"trades\"}}";
            send(subscribeMessage);
            System.out.println("Connected to Hyperliquid WebSocket");
        }

        @Override
        public void onMessage(String message) {
            try {
                JsonNode node = objectMapper.readTree(message);
                if (node.has("data") && node.has("channel") && node.get("channel").asText().equals("trades")) {
                    messageQueue.put(node.get("data"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("WebSocket connection closed: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            System.err.println("WebSocket error: " + ex.getMessage());
        }
    }

    public static Schema createTradesSchema() {
        List<Field> fields = Arrays.asList(new Field("timestamp", FieldType
                .nullable(new ArrowType.Timestamp(TimeUnit.MICROSECOND, null)), null), new Field("coin", FieldType
                        .nullable(new ArrowType.Utf8()), null), new Field("side", FieldType
                                .nullable(new ArrowType.Utf8()), null), new Field("price", FieldType
                                        .nullable(new ArrowType.Decimal(20, 8)), null), new Field("size", FieldType
                                                .nullable(new ArrowType.Decimal(20, 8)), null), new Field("liquidation", FieldType
                                                        .nullable(new ArrowType.Bool()), null));
        return new Schema(fields);
    }

    private static class TradesWriter implements AutoCloseable {
        private final VectorSchemaRoot root;

        private final TimeStampMicroVector timestampVector;

        private final VarCharVector coinVector;

        private final VarCharVector sideVector;

        private final DecimalVector priceVector;

        private final DecimalVector sizeVector;

        private final BitVector liquidationVector;

        private int rowCount = 0;

        public TradesWriter(BufferAllocator allocator, Schema schema) {
            root = VectorSchemaRoot.create(schema, allocator);
            timestampVector = (TimeStampMicroVector) root.getVector("timestamp");
            coinVector = (VarCharVector) root.getVector("coin");
            sideVector = (VarCharVector) root.getVector("side");
            priceVector = (DecimalVector) root.getVector("price");
            sizeVector = (DecimalVector) root.getVector("size");
            liquidationVector = (BitVector) root.getVector("liquidation");

            root.allocateNew();
        }

        public void addTrade(JsonNode trade) {
            if (rowCount >= BATCH_SIZE) {
                throw new IllegalStateException("Batch is full");
            }

            timestampVector.set(rowCount, Instant.now().toEpochMilli() * 1000);
            coinVector.set(rowCount, trade.get("coin").asText().getBytes());
            sideVector.set(rowCount, trade.get("side").asText().getBytes());
            priceVector.set(rowCount, new BigDecimal(trade.get("price").asText()));
            sizeVector.set(rowCount, new BigDecimal(trade.get("size").asText()));
            liquidationVector.set(rowCount, trade.get("liquidation").asBoolean() ? 1 : 0);

            rowCount++;
        }

        public void writeBatch(ParquetWriter<VectorSchemaRoot> writer) throws Exception {
            if (rowCount > 0) {
                root.setRowCount(rowCount);
                writer.write(root);
                resetBatch();
            }
        }

        private void resetBatch() {
            rowCount = 0;
            root.allocateNew();
        }

        @Override
        public void close() {
            root.close();
        }
    }

    public static void main(String[] args) {
        try (BufferAllocator allocator = new RootAllocator()) {
            Schema schema = createTradesSchema();
            Path outputPath = Path.of("hyperliquid_trades.parquet");

            ParquetWriter<VectorSchemaRoot> writer = ArrowParquetWriter.builder(outputPath)
                    .withSchema(schema)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .build();

            TradesWebSocketClient client = new TradesWebSocketClient(new URI(WEBSOCKET_URL));
            client.connect();

            try (TradesWriter tradesWriter = new TradesWriter(allocator, schema)) {
                while (true) {
                    JsonNode trade = messageQueue.take();
                    tradesWriter.addTrade(trade);

                    if (tradesWriter.rowCount >= BATCH_SIZE) {
                        tradesWriter.writeBatch(writer);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                writer.close();
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}