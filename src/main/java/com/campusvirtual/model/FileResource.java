package com.campusvirtual.model;

/**
 * Modelo que representa un fichero subido a una unidad por un profesor.
 */
public class FileResource {
    private final int id;
    private final int unitId;
    private final String fileName;
    private final String storagePath;
    private final String mimeType;
    private final long fileSize;

    public FileResource(int id, int unitId, String fileName, String storagePath,
                        String mimeType, long fileSize) {
        this.id = id;
        this.unitId = unitId;
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
    }

    public int getId() { return id; }
    public int getUnitId() { return unitId; }
    public String getFileName() { return fileName; }
    public String getStoragePath() { return storagePath; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }

    /** Devuelve el tamaño formateado (KB/MB) */
    public String getFileSizeText() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}
