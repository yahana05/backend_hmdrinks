package com.hmdrinks.Service;
import com.hmdrinks.Entity.Post;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.UserVoucher;
import com.hmdrinks.Entity.Voucher;
import com.hmdrinks.Enum.Status_UserVoucher;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.UserVoucherRepository;
import com.hmdrinks.Repository.VoucherRepository;
import com.hmdrinks.Request.CreateVoucherReq;
import com.hmdrinks.Request.CrudVoucherReq;
import com.hmdrinks.Response.CRUDVoucherResponse;
import com.hmdrinks.Response.GetVoucherResponse;
import com.hmdrinks.Response.ListAllVoucherResponse;
import jakarta.transaction.Transactional;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserVoucherRepository userVoucherRepository;


    public boolean isVoucherValid(Voucher voucher) {
        if (voucher == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra nếu startDate hoặc endDate bị null
        if (voucher.getStartDate() == null || voucher.getEndDate() == null) {
            return false;
        }

        // Nếu voucher đã bị xóa hoặc hết hạn
        if (voucher.getIsDeleted() || voucher.getStatus() == Status_Voucher.EXPIRED) {
            return false;
        }

        // Kiểm tra thời gian hiệu lực
        return !voucher.getStartDate().isAfter(now) && !voucher.getEndDate().isBefore(now);
    }


    @Transactional
    public ResponseEntity<?> checkTimeVoucher() {
        List<Voucher> expiredVouchers = voucherRepository.findByIsDeletedFalse().stream()
                .filter(v -> !isVoucherValid(v))
                .peek(v -> v.setStatus(Status_Voucher.EXPIRED))
                .toList();

        if (!expiredVouchers.isEmpty()) {
            voucherRepository.saveAll(expiredVouchers);
            List<UserVoucher> userVouchers = expiredVouchers.stream()
                    .flatMap(v -> v.getUserVouchers().stream())
                    .peek(uv -> uv.setStatus(Status_UserVoucher.EXPIRED))
                    .toList();

            userVoucherRepository.saveAll(userVouchers);

        }

        return ResponseEntity.ok("Success");
    }



    public ResponseEntity<?> createVoucher(CreateVoucherReq req) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(req.getPostId());
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Post not found");
        }

        Voucher existingVoucher = voucherRepository.findByPostPostId(req.getPostId());
        if (existingVoucher != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Voucher Post already exists");
        }
        LocalDateTime createPostDate = post.getDateCreate();
        LocalDateTime currentDate = LocalDateTime.now();

        if (req.getStartDate().isBefore(currentDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date must be greater than or equal to the current date");
        }
        if (req.getEndDate().isBefore(createPostDate) || req.getEndDate().isBefore(currentDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("End date must be greater than or equal to post creation date and current date");
        }

        if (req.getStartDate().isBefore(createPostDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date must be greater than or equal to post creation date");
        }
        if(req.getNumber() <= 0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Number must be greater than 0");
        }

        Voucher voucher = new Voucher();
        voucher.setPost(post);
        voucher.setStartDate(req.getStartDate());
        voucher.setEndDate(req.getEndDate());
        voucher.setKey(req.getKeyVoucher());
        voucher.setIsDeleted(false);
        voucher.setNumber(req.getNumber());
        voucher.setDiscount(req.getDiscount());
        voucher.setStatus(Status_Voucher.ACTIVE);
        voucherRepository.save(voucher);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getKey(),
                voucher.getNumber(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        ));
    }

    public ResponseEntity<?> updateVoucher(CrudVoucherReq req){
        Voucher voucher = voucherRepository.findByVoucherIdAndIsDeletedFalse(req.getVoucherId());
        if(voucher == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Voucher not found");
        }
        if(voucher.getPost().getPostId() == req.getVoucherId())
        {
            Post post = postRepository.findByPostId(voucher.getPost().getPostId());
            if(post == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found");
            }
            LocalDateTime createPost = post.getDateCreate();
            LocalDateTime currentDate = voucher.getStartDate();

            if (req.getStartDate().isBefore(currentDate)) {
                return ResponseEntity.status(HttpStatus.valueOf(400))
                        .body("Start date new must be greater than or equal to start date old");
            }
            if (req.getEndDate().isBefore(currentDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than start date new");
            }
            if (req.getStartDate().isBefore(createPost)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date must be greater than or equal to post creation date");
            }
            if(req.getEndDate().isBefore(createPost)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than or equal to post creation date");
            }
            if (req.getNumber() < 0)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Number must be greater than 0");
            }

            List<UserVoucher> userVoucherList = userVoucherRepository.findByVoucherVoucherId(req.getVoucherId());
            int totalVoucherUserBefore = userVoucherList.size();
            int currentVoucher = req.getNumber() - totalVoucherUserBefore;
            if(currentVoucher < 0)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Number must be greater");
            }
            voucher.setPost(post);
            voucher.setStartDate(req.getStartDate());
            voucher.setEndDate(req.getEndDate());
            voucher.setNumber(currentVoucher);
            voucher.setDiscount(req.getDiscount());
            voucher.setKey(req.getKey());
            voucherRepository.save(voucher);
            return  ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getNumber(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ));
        }
        else
        {
            Voucher vou = voucherRepository.findByPostPostIdAndIsDeletedFalse(req.getPostId());
            if(vou == null){
                throw new BadRequestException("Voucher Not found");
            }
            Post post = postRepository.findByPostId(req.getPostId());
            if(post == null){
                throw new BadRequestException("Not found post");
            }
            LocalDateTime createPost = post.getDateCreate();
            LocalDateTime currentDate =vou.getStartDate();
            if (req.getStartDate().isBefore(currentDate)) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be greater than or equal to start date old");
            }
            if (req.getEndDate().isBefore(createPost) || req.getEndDate().isBefore(currentDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be greater than start date old");
            }
            if (req.getStartDate().isBefore(createPost)) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be greater than or equal to post creation date");
            }
            if(req.getEndDate().isBefore(createPost)){
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be greater than post creation date");
            }

            List<UserVoucher> userVoucherList = userVoucherRepository.findByVoucherVoucherId(req.getVoucherId());
            int totalVoucherUserBefore = userVoucherList.size();
            int currentVoucher = req.getNumber() - totalVoucherUserBefore;
            if(currentVoucher < 0)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Number must be greater");
            }
            voucher.setPost(post);
            voucher.setStartDate(req.getStartDate());
            voucher.setEndDate(req.getEndDate());
            voucher.setDiscount(req.getDiscount());
            voucher.setNumber(currentVoucher);
            voucher.setKey(req.getKey());
            voucherRepository.save(voucher);
            return  ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getNumber(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ));
        }
    }

    public ResponseEntity<?> getVoucherById(int voucherId){
        Voucher voucher = voucherRepository.findByVoucherIdAndIsDeletedFalse(voucherId);
        if(voucher == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getKey(),
                voucher.getNumber(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        ));
    }

    public ResponseEntity<?> getVoucherByKey(String voucherCode, int userId){
        Voucher voucher = voucherRepository.findByKeyAndIsDeletedFalse(voucherCode);
        if(voucher == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found");
        }
        UserVoucher userVoucher = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(userId,voucher.getVoucherId());
        if(userVoucher == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Voucher not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetVoucherResponse(
                userVoucher.getUserVoucherId(),
                userVoucher.getUser().getUserId(),
                userVoucher.getVoucher().getVoucherId(),
                userVoucher.getStatus().toString()
        ));
    }

    public interface SimpleVoucherProjection {
        Integer getVoucherId();
        String getKey();
        Integer getNumber();
        LocalDateTime getStartDate();
        LocalDateTime getEndDate();
        Double getDiscount();
        Status_Voucher getStatus();
        Integer getPostId();
    }

    public ResponseEntity<?> listAllVoucher() {
        List<SimpleVoucherProjection> projections = voucherRepository.findAllSimpleVoucher();

        List<CRUDVoucherResponse> responseList = projections.stream()
                .map(p -> new CRUDVoucherResponse(
                        p.getVoucherId(),
                        p.getKey(),
                        p.getNumber(),
                        p.getStartDate(),
                        p.getEndDate(),
                        p.getDiscount(),
                        p.getStatus(),
                        p.getPostId()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListAllVoucherResponse(responseList.size(), responseList));
    }


    public ResponseEntity<?> disableVoucher(int voucherId)
    {
        Voucher voucher = voucherRepository.findByVoucherId(voucherId);
        if(voucher == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found");
        }
        if(voucher.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher already disabled");
        }
        voucher.setIsDeleted(true);
        voucher.setDateDeleted(LocalDateTime.now());
        voucher.setStatus(Status_Voucher.EXPIRED);
        voucherRepository.save(voucher);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getKey(),
                voucher.getNumber(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        ));
    }

    public ResponseEntity<?> enableVoucher(int voucherId)
    {
        Voucher voucher = voucherRepository.findByVoucherId(voucherId);
        if(voucher == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found");
        }
        if(!voucher.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher already enable");
        }
        voucher.setIsDeleted(false);
        voucher.setDateDeleted(null);
        voucher.setStatus(Status_Voucher.ACTIVE);
        voucherRepository.save(voucher);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getKey(),
                voucher.getNumber(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        ));
    }

}