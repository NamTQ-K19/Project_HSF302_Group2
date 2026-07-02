package hsf302.se2033jv.project_hsf302_group2.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class AvatarStorageUtil {

    @Value("${app.upload.avatar-dir}")
    private String avatarDir;

    @Value("${app.upload.avatar-url-prefix}")
    private String avatarUrlPrefix;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif"};

    public String store(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        boolean allowed = false;

        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new IllegalArgumentException("Loại tệp không hợp lệ");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước tệp vượt quá giới hạn");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;

        Path dir = Paths.get(avatarDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path dest = dir.resolve(fileName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        return avatarUrlPrefix + "/" + fileName;
    }

    public void delete(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith(avatarUrlPrefix)) return;

        String fileName = avatarUrl.replace(avatarUrlPrefix + "/", "");
        Path file = Paths.get(avatarDir).resolve(fileName);

        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Log lỗi nếu cần, không throw
        }
    }
}
