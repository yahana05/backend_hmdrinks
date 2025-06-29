package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Product;
import com.hmdrinks.Entity.ProductVariants;
import com.hmdrinks.Enum.Size;
import com.hmdrinks.Service.ProductService;
import com.hmdrinks.Service.ProductVariantProjection1;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantsRepository extends JpaRepository<ProductVariants,Integer> {
    ProductVariants findByVarId(Integer varId);
    ProductVariants findByVarIdAndSize(Integer varId,Size size);
    ProductVariants findBySizeAndProduct_ProId(Size size, Integer productId);
    ProductVariants findBySizeAndProduct_ProIdAndIsDeletedFalse(Size size, Integer productId);

    ProductVariants findBySizeAndProduct_ProIdAndVarIdNot(Size size, Integer productId, Integer variantId);

    List<ProductVariants> findByProduct_ProId(Integer proId);

    @Query("""
    SELECT new com.hmdrinks.Service.ProductVariantProjection1(
        v.varId, v.size, v.price, v.stock, v.isDeleted,
        v.dateCreated, v.dateUpdated, v.dateDeleted
    )
    FROM ProductVariants v
    WHERE v.product.proId = :proId
""")
    List<ProductVariantProjection1> findVariantByProductId(@Param("proId") Integer proId);


    /**
     * Finds ProductVariants by Category ID and a list of Product IDs with dynamic sorting.
     *
     * @param categoryId The ID of the category.
     * @param productIds The list of product IDs.
     * @param sort       The sorting criteria.
     * @return A list of filtered and sorted ProductVariants.
     */
    List<ProductVariants> findByProduct_Category_CateIdAndProduct_ProIdIn(int categoryId, List<Integer> productIds, Sort sort);


    List<ProductVariants> findByProduct_Category_CateIdAndProduct_ProIdInAndIsDeletedFalse(
            int categoryId,
            List<Integer> productIds,
            Sort sort
    );




    Page<ProductVariants> findAll(Pageable pageable);

    @Query(value = "SELECT COUNT(var_id) FROM product_variants", nativeQuery = true)
    int TotalNumberOfProductVariants();

    List<ProductVariants> findByProduct_ProIdIn(List<Integer> productIds);

    List<ProductVariants> findByProduct_ProIdInAndIsDeletedFalse(List<Integer> productIds);
}
