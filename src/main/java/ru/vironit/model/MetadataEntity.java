package ru.vironit.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MetadataEntity {
    private long id;
    private String fileName;
    private String uploadTime;
    private String size;

    public MetadataEntity(long id, String fileName, String uploadTime, String size) {
        this.id = id;
        this.fileName = fileName;
        this.uploadTime = uploadTime;
        this.size = size;
    }

    public MetadataEntity() {

    }

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Basic
    @Column(name = "upload_time")
    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Basic
    @Column(name = "size")
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataEntity metadata = (MetadataEntity) o;

        if (id != metadata.id) return false;
        if (fileName != null ? !fileName.equals(metadata.fileName) : metadata.fileName != null) return false;
        if (uploadTime != null ? !uploadTime.equals(metadata.uploadTime) : metadata.uploadTime != null) return false;
        if (size != null ? !size.equals(metadata.size) : metadata.size != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (uploadTime != null ? uploadTime.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        return result;
    }
}
