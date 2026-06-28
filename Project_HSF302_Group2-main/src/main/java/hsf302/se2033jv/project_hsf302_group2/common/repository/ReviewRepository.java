package hsf302.se2033jv.project_hsf302_group2.common.repository;


import hsf302.se2033jv.project_hsf302_group2.common.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * Lấy danh sách đánh giá của một khách hàng (sắp xếp mới nhất)
     */
    List<Review> findByCustomer_UserIdOrderByCreatedAtDesc(Integer userId);

    /**
     * Lấy danh sách đánh giá theo đơn hàng
     */
    List<Review> findByOrder_OrderId(Integer orderId);

    /**
     * Lấy danh sách đánh giá theo sản phẩm
     */
    List<Review> findByProduct_ProductId(Integer productId);

    /**
     * Lấy danh sách đánh giá của khách hàng theo đơn hàng
     */
    List<Review> findByCustomer_UserIdAndOrder_OrderId(Integer userId, Integer orderId);

    /**
     * Kiểm tra khách hàng đã đánh giá sản phẩm trong đơn hàng chưa
     */
    boolean existsByCustomer_UserIdAndOrder_OrderIdAndProduct_ProductId(
            Integer userId, Integer orderId, Integer productId);

    /**
     * Lấy danh sách đánh giá hiển thị (is_visible = true)
     */
    List<Review> findByIsVisibleTrue();

    /**
     * Lấy danh sách đánh giá theo sản phẩm và hiển thị
     */
    List<Review> findByProduct_ProductIdAndIsVisibleTrue(Integer productId);

    /**
     * Lấy danh sách đánh giá theo rating
     */
    List<Review> findByRating(Integer rating);

    /**
     * Lấy đánh giá theo ID và khách hàng (kiểm tra quyền)
     */
    Optional<Review> findByReviewIdAndCustomer_UserId(Integer reviewId, Integer userId);

    /**
     * Cập nhật trạng thái hiển thị của đánh giá
     */
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.isVisible = :visible WHERE r.reviewId = :reviewId")
    void updateVisibility(@Param("reviewId") Integer reviewId, @Param("visible") Boolean visible);

    /**
     * Đếm số lượng đánh giá theo sản phẩm
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId")
    Long countByProductId(@Param("productId") Integer productId);

    /**
     * Tính điểm đánh giá trung bình của sản phẩm
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.productId = :productId AND r.isVisible = true")
    Double getAverageRatingByProductId(@Param("productId") Integer productId);

    /**
     * Lấy danh sách đánh giá của sản phẩm có phân trang
     */
    @Query("SELECT r FROM Review r WHERE r.product.productId = :productId AND r.isVisible = true ORDER BY r.createdAt DESC")
    List<Review> findVisibleByProductId(@Param("productId") Integer productId);
}