package ru.vironit.other;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.joda.time.DateTime;
import ru.vironit.model.MetadataEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileData {

    public static ObjectMetadata generateMetadata(File file) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

        Map<String, String> metadata = new HashMap<>();

        metadata.put("id", Long.toString(attributes.creationTime().to(TimeUnit.MILLISECONDS)));
        metadata.put("file-name", file.getName());
        metadata.put("upload-time", DateTime.now().toString());
        metadata.put("size", Long.toString(file.length()));

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setUserMetadata(metadata);
        return objectMetadata;
    }

    public static MetadataEntity writeMetadata(Map<String, String> metaMap) {
        return new MetadataEntity(
                Long.parseLong(metaMap.get("id")),
                metaMap.get("file-name"),
                metaMap.get("upload-time"),
                metaMap.get("size")
        );
    }
}
