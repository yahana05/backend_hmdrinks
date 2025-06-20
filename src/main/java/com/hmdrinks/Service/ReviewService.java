package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Enum.Status_Cart;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public ResponseEntity<?> createReview(CreateNewReview req)
    {
        Product product = productRepository.findByProIdAndIsDeletedFalse(req.getProId());
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not fount product");
        }
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is deleted");
        }
        Review review = new Review();
        review.setContent(req.getContent());
        review.setRatingStar(req.getRatingStart());
        review.setUser(user);
        review.setDateCreated(LocalDateTime.now());
        review.setProduct(product);
        review.setIsDeleted(false);
        reviewRepository.save(review);

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDReviewResponse (
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getProduct().getProId(),
                review.getUser().getFullName(),
                review.getContent(),
                review.getRatingStar(),
                review.getIsDeleted(),
                review.getDateDeleted(),
                review.getDateUpdated(),
                review.getDateCreated()
        ));
    }

    public ResponseEntity<?> updateReview(CRUDReviewReq req) {
        Review review = reviewRepository.findByReviewIdAndUser_UserIdAndIsDeletedFalse(req.getReviewId(), req.getUserId());
        if (review == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Review not found");
        }
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is deleted");
        }
        review.setContent(req.getContent());
        review.setRatingStar(req.getRatingStart());
        review.setDateUpdated(LocalDateTime.now());
        review.setDateCreated(LocalDateTime.now());
        reviewRepository.save(review);
        CRUDReviewResponse response = new CRUDReviewResponse(
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getProduct().getProId(),
                review.getUser().getFullName(),
                review.getContent(),
                review.getRatingStar(),
                review.getIsDeleted(),
                review.getDateDeleted(),
                review.getDateUpdated(),
                review.getDateCreated()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<String> deleteOneReview(DeleteReviewReq req) {
        Review review = reviewRepository.findByReviewIdAndUser_UserIdAndIsDeletedFalse(req.getReviewId(), req.getUserId());
        if (review == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Review not found");
        }
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is deleted");
        }
        review.setIsDeleted(true);
        review.setDateDeleted(LocalDateTime.now());
        reviewRepository.save(review);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Review deleted successfully");
    }



    public interface ReviewProjection {
        Integer getReviewId();
        Integer getUserId();
        Integer getProId();
        String getFullName();
        String getContent();
        Integer getRatingStar();
        Boolean getIsDeleted();
        LocalDateTime getDateDeleted();
        LocalDateTime getDateUpdated();
        LocalDateTime getDateCreated();
    }


    public ResponseEntity<?> getAllReview(String pageFromParam, String limitFromParam, int proId) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Math.min(Integer.parseInt(limitFromParam), 100);

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("dateCreated").descending());

        Page<ReviewProjection> reviewPage = reviewRepository.findReviewProjectionsByProductId(proId, pageable);
        int totalReviews = reviewRepository.countAllReviewsByProductId(proId);

        List<CRUDReviewResponse> responses = reviewPage.getContent().stream()
                .map(r -> new CRUDReviewResponse(
                        r.getReviewId(),
                        r.getUserId(),
                        r.getProId(),
                        r.getFullName(),
                        r.getContent(),
                        r.getRatingStar(),
                        r.getIsDeleted(),
                        r.getDateDeleted(),
                        r.getDateUpdated(),
                        r.getDateCreated()
                )).toList();

        return ResponseEntity.ok(new ListAllReviewProductResponse(
                page,
                reviewPage.getTotalPages(),
                limit,
                totalReviews,
                responses
        ));
    }



//    public ResponseEntity<ListAllReviewProductResponse> getAllReview(String pageFromParam, String limitFromParam,int proId)
//    {
//        int page = Integer.parseInt(pageFromParam);
//        int limit = Integer.parseInt(limitFromParam);
//        if (limit >= 100) limit = 100;
//        Pageable pageable = PageRequest.of(page - 1, limit);
//        Page<Review> reviews = reviewRepository.findByProduct_ProId(proId,pageable);
//        List<CRUDReviewResponse> crudReviewResponseList = new ArrayList<>();
//        for(Review review : reviews)
//        {
//            crudReviewResponseList.add(new CRUDReviewResponse (
//                    review.getReviewId(),
//                    review.getUser().getUserId(),
//                    review.getProduct().getProId(),
//                    review.getUser().getFullName(),
//                    review.getContent(),
//                    review.getRatingStar(),
//                    review.getIsDeleted(),
//                    review.getDateDeleted(),
//                    review.getDateUpdated(),
//                    review.getDateCreated()
//            ));
//        }
//        return ResponseEntity.status(HttpStatus.OK).body(new ListAllReviewProductResponse (page,
//                reviews.getTotalPages(),
//                limit,
//                crudReviewResponseList));
//    }
}
