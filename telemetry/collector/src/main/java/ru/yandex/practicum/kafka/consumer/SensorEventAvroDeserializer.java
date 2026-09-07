package ru.yandex.practicum.kafka.consumer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.exception.DeserializationException;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public class SensorEventAvroDeserializer implements Deserializer<SensorEventAvro> {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final DatumReader<SensorEventAvro> reader = new SpecificDatumReader<>(SensorEventAvro.getClassSchema());

    @Override
    public SensorEventAvro deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
                return this.reader.read(null, decoder);
            }
            return null;
        } catch (Exception e) {
            throw new DeserializationException("Ошибка десереализации данных из топика [" + topic + "]", e);
        }
    }
}
