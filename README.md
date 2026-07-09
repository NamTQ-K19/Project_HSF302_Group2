# Project_HSF302_Group2


ROLE_ADMIN (Quản trị viên)→ Chuyển hướng đến /admin/dashboard
ROLE_MANAGER (Quản lý) → Chuyển hướng đến /dashboard
ROLE_CASHIER (Thu ngân) → Chuyển hướng đến /order/create (Màn hình tạo đơn hàng mới)
ROLE_BARISTA (Pha chế) → Chuyển hướng đến /barista/dashboard (Màn hình bảng điều khiển pha chế)
ROLE_CUSTOMER  ----> /home (Trang chủ chung)

## Danh sách các cập nhật (Tính năng Barista Dashboard)

**1. Giao diện (Front-end):**
- Thêm file `src/main/resources/templates/barista/barista-dashboard.html`: Giao diện chính của màn hình pha chế.
- Thêm file `src/main/resources/static/css/barista/style.css`: CSS thiết kế giao diện hiện đại, trực quan.
- Thêm file `src/main/resources/static/js/barista/app.js`: JS hiển thị đồng hồ thời gian thực và xử lý cửa sổ Báo lỗi (Modal).

**2. Mã nguồn Backend (Java):**
- Tạo `BaristaController.java`: Điều hướng trang và cung cấp API xử lý trạng thái món (`/barista/item/status`).
- Tạo DTO `BaristaOrderDTO.java` và `BaristaItemDTO.java`: Đóng gói dữ liệu tối ưu hiển thị.
- Sửa đổi `OrderRepository.java`: Thêm hàm JPQL `findBaristaOrders()` áp dụng nguyên tắc FIFO (Sắp xếp ưu tiên thời gian: Đơn đặt trước làm trước) và chỉ lấy các đơn có món đang ở trạng thái `PENDING` hoặc `PREPARING`.
- Cập nhật `CustomLoginSuccessHandler.java`: Đổi trang mặc định khi đăng nhập của `ROLE_BARISTA` về `/barista/dashboard`.