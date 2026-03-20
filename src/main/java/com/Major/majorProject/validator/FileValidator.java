package com.Major.majorProject.validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;//2MB
    private static final String[] FILE_TYPE = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/svg"};

    @Override
    public boolean isValid(MultipartFile file,  ConstraintValidatorContext context) {

        //File empty???
        if(file == null || file.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File cannot be Empty").addConstraintViolation();
            return true;
        }

        //file size???
        if(file.getSize() > MAX_FILE_SIZE){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File Size Exceeded").addConstraintViolation();
            return false;
        }

        //file type???
        String contentType = file.getContentType();
        if (contentType == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Could not determine file type").addConstraintViolation();
            return false;
        }

        boolean isValidType = Arrays.stream(FILE_TYPE)
                .anyMatch(type -> type.equalsIgnoreCase(contentType));

        if (!isValidType) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Only PNG, JPG, or JPEG files are allowed").addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
