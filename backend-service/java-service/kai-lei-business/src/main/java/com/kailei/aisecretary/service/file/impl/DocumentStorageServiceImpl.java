package com.kailei.aisecretary.service.file.impl;

import com.kailei.aisecretary.entity.FileEntity;
import com.kailei.aisecretary.service.file.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentStorageServiceImpl implements DocumentStorageService {
    private static final String DEFAULT_PATH = "documents/";

    private final FileStorageService fileStorageService;

    @Override
    public FileEntity upload(MultipartFile file, Long uploaderUserId, String path) {
        String storagePath = Optional.ofNullable(path)
                .filter(value -> !value.isBlank())
                .orElse(DEFAULT_PATH);

        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(storagePath)
                .setObjectId(uploaderUserId == null ? null : uploaderUserId.toString())
                .setObjectType("document")
                .upload();

        FileEntity entity = new FileEntity();
        entity.setUploaderUserId(uploaderUserId);
        entity.setOriginalName(fileInfo.getOriginalFilename());
        entity.setStorageName(fileInfo.getFilename());
        entity.setFileUrl(fileInfo.getUrl());
        entity.setStoragePath(fileInfo.getPath());
        entity.setContentType(fileInfo.getContentType());
        entity.setFileSize(fileInfo.getSize());
        entity.setFileHash(fileInfo.getHashInfo() == null ? null : fileInfo.getHashInfo().getMd5());
        entity.setStatus(1);
        return entity;
    }

    @Override
    public boolean delete(String fileUrl) {
        return fileStorageService.delete(fileUrl);
    }
}
