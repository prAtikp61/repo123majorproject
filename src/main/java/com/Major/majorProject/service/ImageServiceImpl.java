package com.Major.majorProject.service;

import com.Major.majorProject.repository.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {

    public Cloudinary cloudinary;

    public ImageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile contactimage, String filename) {
        if (contactimage == null || contactimage.isEmpty()) {
            return null;
        }
        try {
            byte[] data = new byte[contactimage.getInputStream().available()];
            contactimage.getInputStream().read(data);
            cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "public_id",filename
            ));
            return this.getUrlFromPublicId(filename);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String getUrlFromPublicId(String publicId) {
        return cloudinary.url().transformation(new Transformation<>().width(500)
                .height(500).crop("fill")).generate(publicId);
    }
}
