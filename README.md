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

## Cập nhật ngày hôm nay (Tính năng Thống kê, Báo lỗi & Cải thiện Giao diện Barista)

**1. Tính năng Thống kê (Barista Statistics):**
- Tạo mới file `src/main/resources/templates/barista/statistics.html`: Giao diện Dashboard thống kê trực quan bao gồm:
  - 4 Thẻ (Cards) tóm tắt: Tổng số món, số ly đã pha/hủy, và tỷ lệ hoàn thành (%).
  - Bảng xếp hạng **Top 5 sản phẩm pha thành công nhất** (kèm thanh progress bar).
  - Bảng phân tích **Lý do không pha được** (hiển thị số lần, số ly và % cho từng loại lý do).
  - Bảng **Lịch sử chi tiết** gộp chung các món hoàn thành & hủy (sắp xếp theo thời gian mới nhất, có đánh dấu màu trạng thái và ghi chú lý do hủy).
- Cập nhật `BaristaController.java`: Viết thêm endpoint `/barista/statistics`, xử lý logic tính toán, gom nhóm và sắp xếp dữ liệu thống kê từ database để truyền ra View.
- Bổ sung thuộc tính `cancelReason` (Lý do hủy) vào thực thể Java `OrderDetail.java`.
- Cập nhật `OrderDetailRepository.java`: Thêm hàm truy vấn `findBaristaStats()` để lấy toàn bộ danh sách các món đã hoàn thành hoặc bị hủy.

**2. Nâng cấp Tính năng Báo lỗi món (Barista Dashboard):**
- Cập nhật `barista-dashboard.html`: Thêm ô nhập liệu (Text Input) để Barista tự điền lý do khi nhấn vào lựa chọn báo lỗi **"Khác..."**.
- Cập nhật `app.js`: Thêm logic JS để hiển thị/ẩn ô nhập liệu linh hoạt khi người dùng thay đổi lựa chọn; đồng thời fix lỗi so sánh chuỗi (làm nút Khách hủy và Khác sáng lên cùng lúc).

**3. Cải thiện Hồ sơ cá nhân (Barista Profile):**
- Cập nhật `profile.html`: Bổ sung tính năng "Con mắt" (Eye Icon Toggle) để ẩn/hiện mật khẩu cho 3 ô nhập liệu trong form Đổi mật khẩu.

**4. Đồng bộ hóa Cơ sở dữ liệu (Database Script):**
- Cập nhật file `database/coffee_shop_sqlserver.sql`: Thêm thủ công cột `cancel_reason NVARCHAR(MAX) NULL` vào lệnh `CREATE TABLE order_details` để script CSDL luôn đồng bộ với các thay đổi bên Code Java (tránh lỗi khi người dùng tạo lại CSDL).