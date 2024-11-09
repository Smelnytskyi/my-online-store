package com.gmail.deniska1406sme.onlinestore.services;

import com.gmail.deniska1406sme.onlinestore.config.ImgbbConfig;
import com.gmail.deniska1406sme.onlinestore.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {
    private final ImgbbConfig imgbbConfig;

    @Autowired
    public ImageServiceImpl(ImgbbConfig imgbbConfig) {
        this.imgbbConfig = imgbbConfig;
    }

    @Override
    public boolean uploadImage(String base64Image, ProductDTO productDTO) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);
        body.add("key", imgbbConfig.getAltApiKey());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(imgbbConfig.getApiUrl(), HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            String imageUrl = (String) data.get("url");
            String deleteUrl = (String) data.get("delete_url");

            productDTO.setImageUrl(imageUrl);
            productDTO.setDeleteImageUrl(deleteUrl);

            return true;
        } else {
            throw new RuntimeException("Failed to upload image" + response.getBody());
        }
    }

    @Override
    public void deleteImage(String deleteUrl) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("key", imgbbConfig.getApiKey());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Deleted image successfully");
        } else {
            throw new RuntimeException("Failed to delete image" + response.getBody());
        }
    }
}
