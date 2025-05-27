package com.authentication.demo.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service

public class ImageService {

  int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
  int MIN_WIDTH = 300; // Minimum width in pixels
  int MIN_HEIGHT = 300; // Minimum height in pixels


    public MultipartFile processImage(MultipartFile file) throws IOException, IllegalArgumentException {
        // Validate input file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Input file is empty or null");
        }

        // Extract file type from content type or filename
        String contentType = file.getContentType(); // e.g., "image/jpeg"
        String fileExtension = getFileExtension(file.getOriginalFilename()); // e.g., "jpg"

        // Validate file type
        if (!isSupportedFileType(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
        }

        // Read the original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Failed to read the image.");
        }

        if (originalImage.getWidth() < MIN_WIDTH || originalImage.getHeight() < MIN_HEIGHT) {
            throw new IllegalArgumentException("Image dimensions are too small.");
        }

        // Crop the image to a square
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        
        BufferedImage croppedImage = originalImage.getSubimage(x, y, size, size);

        // Convert the cropped image to a MultipartFile
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(croppedImage, fileExtension, outputStream); // Use dynamic file type
            return new MockMultipartFile(
                file.getName(),
                file.getOriginalFilename(),
                contentType,
                outputStream.toByteArray()
            );
        }
    }

    // Helper method to extract file extension
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name: " + filename);
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    // Helper method to validate supported file types
    private boolean isSupportedFileType(String fileExtension) {
        return fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png") || fileExtension.equals("gif");
    }
}