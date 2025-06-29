package com.hmdrinks.Controller;
import com.hmdrinks.Enum.Language;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Request.*;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.*;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private  PaymentGroupService paymentGroupService;
    @Autowired
    private  ShipmentService shipmentService;
    @Autowired
    private UserVoucherService userVoucherService;
    @Autowired
    private  PaymentService paymentService;
    @Autowired
    private SupportFunction supportFunction;

    @GetMapping("/list-image/{proId}")
    public ResponseEntity<?> getListImage(@PathVariable Integer proId){
        ResponseEntity<?> validation = supportFunction.validatePositiveId("proId", proId);
        if (validation != null) return validation;
        return ResponseEntity.ok(adminService.getAllProductImages(proId));
    }
    @GetMapping(value = "/listUser")
    public ResponseEntity<?> listAllUser(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit
    ) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return userService.getListAllUser(page, limit);
    }

    @GetMapping(value = "/listUser-role")
    public ResponseEntity<?> listAllUserByRole(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit,
            @RequestParam(name = "role") Role role
    ) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return userService.getListAllUserByRole(page, limit, role);
    }


    @PostMapping(value = "/create-account")
    public ResponseEntity<?> createAccount(@RequestBody  CreateAccountUserReq req){
        return ResponseEntity.ok(adminService.createAccountUser(req));
    }

    @GetMapping(value = "/search-user")
    public ResponseEntity<?> searchByUser(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return ResponseEntity.ok(userService.totalSearchUser(keyword, page, limit));
    }
    @PutMapping(value = "/update-account")
    public ResponseEntity<?> updateAccount(@Valid @RequestBody UpdateAccountUserReq req) {
        return adminService.updateAccountUser(req);
    }

    @DeleteMapping(value = "/product/review/deleteOne")
    public ResponseEntity<?> deleteReview(@RequestBody @Valid  IdReq req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(adminService.deleteOneReview(req.getId()));
    }

    @DeleteMapping(value = "/product/review/deleteAll")
    public ResponseEntity<?> deleteAllReview(@RequestBody @Valid  IdReq req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(adminService.deleteALlReviewProduct(req.getId()));
    }

    @PostMapping("/filter-product")
    public ResponseEntity<?> filterProduct(
            @RequestBody FilterProductBox req
    ) {

        return ResponseEntity.ok(adminService.filterProduct(req));
    }

    @GetMapping(value = "/list-product")
    public ResponseEntity<?> listAllProduct(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit,
            @RequestParam(name = "language") Language language
    ) throws Exception {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;
        }
        return ResponseEntity.ok(adminService.listProduct(page, limit,language));
    }

    @GetMapping(value = "/search-product")
    public ResponseEntity<?> searchByCategoryName(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit, @RequestParam(name = "language")Language language) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return ResponseEntity.ok(adminService.totalSearchProduct(keyword,page,limit,language));
    }

    @GetMapping( value = "/product/variants/{id}")
    public ResponseEntity<?> viewProduct(@PathVariable Integer id){
        ResponseEntity<?> validation = supportFunction.validatePositiveId("id", id);
        if (validation != null) return validation;
        return ResponseEntity.ok(adminService.getAllProductVariantFromProduct(id));
    }

    @GetMapping(value ="/list-voucher/{userId}")
    public ResponseEntity<?> getAllVoucher(
            @PathVariable Integer userId
    ){
        ResponseEntity<?> validation = supportFunction.validatePositiveId("userId", userId);
        if (validation != null) return validation;
        return  userVoucherService.listAllVoucherUserId(userId);
    }

    @GetMapping("/product/view/{id}")
    public ResponseEntity<?> update( @PathVariable Integer id, @RequestParam Language language){
        ResponseEntity<?> validation = supportFunction.validatePositiveId("id", id);
        if (validation != null) return validation;
        return ResponseEntity.ok(adminService.getOneProduct(id,language));
    }

    @GetMapping("/cate/view/{id}/product")
    public ResponseEntity<?> getALLProductFromCategory(@PathVariable Integer id,@RequestParam(name = "page") String page, @RequestParam(name = "limit") String limit, @RequestParam(name = "language")Language language){
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);
        ResponseEntity<?> validation = supportFunction.validatePositiveId("id", id);
        if (validation != null) return validation;
        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return ResponseEntity.ok(adminService.getAllProductFromCategory(id,page,limit,language));
    }

    @GetMapping(value = "/post/view/all")
    public ResponseEntity<?> getAllPosts(@RequestParam(name = "page") String page,
                                                           @RequestParam(name = "limit") String limit,
                                                           @RequestParam(name = "language")Language language){
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return  ResponseEntity.ok(adminService.getAllPost(page,limit,language));
    }

    @GetMapping(value = "/post/view/type/all")
    public ResponseEntity<?> getAllPostsByTye(@RequestParam(name = "page") String page,
                                                                @RequestParam(name = "limit") String limit,
                                                                @RequestParam(name = "type") Type_Post typePost,
                                                                @RequestParam(name = "language")Language language){
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;
        }
        return  ResponseEntity.ok(adminService.getAllPostByType(page,limit,typePost,language));
    }

    @GetMapping(value = "/list-category")
    public ResponseEntity<?> listAllCategory(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit,
            @RequestParam(name = "language")Language language
    ) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return ResponseEntity.ok(adminService.listCategory(page, limit, language));
    }

    @PostMapping("/shipment/activate")
    public ResponseEntity<?> activeShipment(@RequestBody @Valid AdminActivateShipmentReq req) {
        return shipmentService.activate_Admin(req.getShipmentId(), req.getStatus());
    }

    @GetMapping("/list-payment-refund")
    public ResponseEntity<?> handleListPayment(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return  paymentService.listAllPaymentRefund(page, limit);
    }

    @PutMapping("/activate/refund")
    public ResponseEntity<?> handleRefund(
            @RequestBody @Valid IdReq idReq) {
        return  paymentService.activateRefund(idReq.getId());
    }

    @GetMapping("/group/list-payment-refund")
    public ResponseEntity<?> handleListPaymentGroupRefund(
            @RequestParam(name = "page") String page,
            @RequestParam(name = "limit") String limit) {
        ResponseEntity<?> validationResult = supportFunction.validatePaginationParams(page, limit);

        if (validationResult != null) {
            return validationResult;  // return luôn nếu có lỗi
        }
        return  paymentGroupService.listAllGroupPaymentRefund(page,limit);
    }


    @PutMapping("/group/activate/refund")
    public ResponseEntity<?> handleRefundGroup(
            @RequestBody IdReq idReq) {
        return  paymentGroupService.activateRefundGroup(idReq.getId());
    }

}
