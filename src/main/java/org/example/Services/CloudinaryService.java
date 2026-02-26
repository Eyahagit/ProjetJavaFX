package org.example.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Service to upload images/media to Cloudinary and get back a URL.
 * Credentials are set in this service.
 */
public class CloudinaryService {

    private static final String CLOUD_NAME = "dcfb7nedc";
    private static final String API_KEY = "381561793828754";
    private static final String API_SECRET = "-i2VdylLRnptwGPo2Ve2x_TV0GI";

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET
        );
        this.cloudinary = new Cloudinary(config);
    }

    /**
     * Uploads a file to Cloudinary and returns the secure URL.
     * @param file image or video file to upload
     * @return the public URL of the uploaded asset, or null on failure
     */
    public String upload(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
            Object url = result != null ? result.get("secure_url") : null;
            return url != null ? url.toString() : null;
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a file by path. Convenience for FileChooser path.
     */
    public String upload(String filePath) {
        if (filePath == null || filePath.isBlank()) return null;
        return upload(new File(filePath));
    }
}
