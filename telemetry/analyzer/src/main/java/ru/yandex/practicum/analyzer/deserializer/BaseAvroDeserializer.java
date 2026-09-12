package ru.yandex.practicum.analyzer.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.analyzer.exception.DeserializationException;

public abstract class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final Schema schema;
    private final DatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory != null ? decoderFactory : DecoderFactory.get();
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            }

            if (schema == null) {
                throw new IllegalStateException("Schema is not initialized!");
            }

            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            return this.reader.read(null, decoder);
        } catch (Exception e) {
            try {
                throw new DeserializationException("Ошибка десереализации данных из топика [" + topic + "]", e);
            } catch (DeserializationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
