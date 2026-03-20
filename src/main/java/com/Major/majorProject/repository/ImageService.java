package com.Major.majorProject.repository;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    public String uploadImage(MultipartFile contactImage, String filename);

    public String getUrlFromPublicId(String publicId);

}
