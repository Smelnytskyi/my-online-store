package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;

public interface ImageService {

    boolean uploadImage(String base64Image, ProductDTO productDTO);
    void deleteImage(String deleteUrl);
}
