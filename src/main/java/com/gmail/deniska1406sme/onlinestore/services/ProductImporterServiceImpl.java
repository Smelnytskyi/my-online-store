package com.gmail.deniska1406sme.onlinestore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import com.gmail.deniska1406sme.onlinestore.model.Product;
import com.gmail.deniska1406sme.onlinestore.repositories.ProductRepository;
import com.gmail.deniska1406sme.onlinestore.utils.ImageUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ProductImporterServiceImpl implements ProductImporterService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final ImageUtil imageUtil = new ImageUtil();

    public ProductImporterServiceImpl(ProductRepository productRepository, ImageService imageService) {
        this.productRepository = productRepository;
        this.imageService = imageService;
    }

    @Override
    public void importProductsFromFile(String filePath){
        ObjectMapper mapper = new ObjectMapper();

        try {
            File file = new File(filePath);
            Product[] products = mapper.readValue(file, Product[].class);

            for (Product product : products) {
                String imagePath = "src/main/resources/static/photos/" + product.getImageUrl();
                String base64Image = imageUtil.convertImageToBase64(imagePath);
                ProductDTO productDTO = product.toProductDTO();
                boolean uploadSuccess = imageService.uploadImage(base64Image, productDTO);

                if (uploadSuccess) {
                    product.setImageUrl(productDTO.getImageUrl());
                    product.setDeleteImageUrl(productDTO.getDeleteImageUrl());
                }
                productRepository.save(product);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
