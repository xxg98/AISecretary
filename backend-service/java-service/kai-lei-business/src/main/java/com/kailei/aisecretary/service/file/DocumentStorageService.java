package com.kailei.aisecretary.service.file;

import com.kailei.aisecretary.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {
    FileEntity upload(MultipartFile file, Long uploaderUserId, String path);

    boolean delete(String fileUrl);
}
