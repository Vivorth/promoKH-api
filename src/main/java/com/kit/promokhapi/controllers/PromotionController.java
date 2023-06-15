package com.kit.promokhapi.controllers;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.kit.promokhapi.dto.AddPromotionDTO;
import com.kit.promokhapi.dto.ResponseDTO;

import com.kit.promokhapi.jwt.JwtHelper;
import com.kit.promokhapi.models.PostPromoReqModel;
import com.kit.promokhapi.models.Promotion;
import com.kit.promokhapi.models.PromotionDetail;
import com.kit.promokhapi.repository.PromotionDetailRepository;
import com.kit.promokhapi.repository.PromotionRepository;


import com.kit.promokhapi.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/promo_kh")
public class PromotionController {

    @Autowired
    PromotionRepository promotionRepository;
    @Autowired
    PromotionDetailRepository promotionDetailRepository;

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    PromotionService promotionService;
    @PostMapping("/promotion/add")
    public ResponseEntity<?> post(@Valid @RequestBody PostPromoReqModel reqModel) {

        Promotion promotion = new Promotion(
                reqModel.getCategoryId(),
                reqModel.getTitle(),
                reqModel.getOldPrice(),
                reqModel.getDiscountPrice(),
                reqModel.getDiscountPercentage(),
                reqModel.getStartDate(),
                reqModel.getEndDate(),
                reqModel.getFeatureImageUrl(),
                reqModel.getLocation(),
                LocalDateTime.now(),
                true
        );
        promotionRepository.save(promotion);

        PromotionDetail promotionDetail = new PromotionDetail(
                promotion.getId(),
                reqModel.getPromotionDetail(),
                reqModel.getImageUrlList(),
                reqModel.getContactNumber(),
                reqModel.getFacebookName(),
                reqModel.getPromotionUrl(),
                reqModel.getLongtitude(),
                reqModel.getLatitude(),
                LocalDateTime.now(),
                true
        );
        promotionDetailRepository.save(promotionDetail);

        AddPromotionDTO addPromotionDTO = new AddPromotionDTO(
                promotionDetail.getId(),
                reqModel.getCategoryId(),
                reqModel.getTitle(),
                reqModel.getOldPrice(),
                reqModel.getDiscountPrice(),
                reqModel.getDiscountPercentage(),
                reqModel.getStartDate(),
                reqModel.getEndDate(),
                reqModel.getFeatureImageUrl(),
                reqModel.getLocation()
        );

        return ResponseEntity.ok(new ResponseDTO<>(HttpStatus.OK.value(), "success", addPromotionDTO));
    }



@GetMapping("/promotion/get")
public ResponseEntity<?> getByCategory(@RequestParam String category_Id,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "25") int size) {

   if (category_Id == null || category_Id.isEmpty()) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionRepository.findAll(pageable);
        List<Promotion> promotionList = promotionPage.getContent();    
        ResponseDTO<List<Promotion>> responseDTO = new ResponseDTO<>(
                HttpStatus.OK.value(),
                "success",
                promotionList
        );
        return ResponseEntity.ok(responseDTO);
    }                                 
    
    Pageable pageable = PageRequest.of(page, size);
    Page<Promotion> promotionPage = promotionRepository.findByCategoryId(category_Id, pageable);
    
    List<Promotion> promotionList = promotionPage.getContent();
    
    ResponseDTO<List<Promotion>> responseDTO = new ResponseDTO<>(
            HttpStatus.OK.value(),
            "success",
            promotionList
    );
    
    return ResponseEntity.ok(responseDTO);
}
    @PatchMapping("/user/posted_promotion/update")
    public ResponseEntity<?> update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @RequestBody Map<Object, Object> payload, @RequestParam("promotion_id") String promotionId) {

        boolean isAuth = jwtHelper.validateAccessToken(authorization);
        if (isAuth) {
            promotionService.patch(payload, promotionId);
            return ResponseEntity.ok(new ResponseDTO<>(HttpStatus.OK.value(), "success", null));
        }
        else {
            return ResponseEntity.ok(new ResponseDTO<>(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", null));
        }
    }


    @PostMapping("/user/posted_promotion/delete")
    public  ResponseEntity<?> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                     @RequestBody  Map<String, Object> payload) {
        boolean isAuth = jwtHelper.validateAccessToken(authorization);
        if (isAuth) {
            try {
                Promotion deletePromotion = promotionService.deleteById((String) payload.get("promotion_id"));
                return ResponseEntity.ok(new ResponseDTO<>(HttpStatus.OK.value(), "Promotion has been deleted successfully.", deletePromotion));
            }
            catch (RuntimeException exc) {
                return ResponseEntity.ok(new ResponseDTO<>(HttpStatus.NOT_FOUND.value(), "Promotion not found", null));
            }
        }
        else {
            return ResponseEntity.ok(new ResponseDTO<>(HttpStatus.UNAUTHORIZED.value(), "unauthorized", null));
        }
    }
}

