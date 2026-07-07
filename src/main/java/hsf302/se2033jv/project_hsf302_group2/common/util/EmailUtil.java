package hsf302.se2033jv.project_hsf302_group2.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailUtil {
    private static final String COMPANY_NAME = "BrewMaster Coffee Shop";
    private static final String COMPANY_EMAIL = "support@brewmaster.com";
    private static final String COMPANY_PHONE = "1900-1234";
    private static final String COMPANY_ADDRESS = "Hoà Lạc - Hà Nội";

    // ==================== ICONS SVG ====================

    private static final String ICON_LOCK = """
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C8.68629 2 6 4.68629 6 8V10H4C3.44772 10 3 10.4477 3 11V21C3 21.5523 3.44772 22 4 22H20C20.5523 22 21 21.5523 21 21V11C21 10.4477 20.5523 10 20 10H18V8C18 4.68629 15.3137 2 12 2Z" 
                stroke="#dc2626" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 14V17" stroke="#dc2626" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="19" r="0.5" fill="#dc2626" stroke="#dc2626" stroke-width="0.5"/>
            <path d="M8 10V8C8 5.79086 9.79086 4 12 4C14.2091 4 16 5.79086 16 8V10" 
                stroke="#dc2626" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
    """;

    private static final String ICON_UNLOCK = """
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 10H4C3.44772 10 3 10.4477 3 11V21C3 21.5523 3.44772 22 4 22H20C20.5523 22 21 21.5523 21 21V11C21 10.4477 20.5523 10 20 10H18Z" 
                stroke="#22c55e" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M8 10V8C8 5.79086 9.79086 4 12 4C14.2091 4 16 5.79086 16 8" 
                stroke="#22c55e" stroke-width="1.8" stroke-linecap="round"/>
            <path d="M12 14V17" stroke="#22c55e" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="19" r="0.5" fill="#22c55e" stroke="#22c55e" stroke-width="0.5"/>
        </svg>
    """;

    private static final String ICON_WELCOME = """
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" 
                stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M8 9.5L12 13.5L16 9.5" stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 2V22" stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="12" r="9" stroke="#c8a882" stroke-width="1.2" stroke-dasharray="2 2"/>
        </svg>
    """;

    private static final String ICON_OTP = """
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 4H20C21.1046 4 22 4.89543 22 6V18C22 19.1046 21.1046 20 20 20H4C2.89543 20 2 19.1046 2 18V6C2 4.89543 2.89543 4 4 4Z" 
                stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M8 8H16" stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round"/>
            <path d="M8 12H14" stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round"/>
            <path d="M8 16H12" stroke="#4a2c1b" stroke-width="1.8" stroke-linecap="round"/>
            <rect x="16" y="12" width="4" height="4" rx="1" stroke="#c8a882" stroke-width="1.5"/>
            <text x="17.5" y="15.5" font-family="monospace" font-size="12" font-weight="bold" fill="#c8a882">OTP</text>
        </svg>
    """;

    private static final String ICON_COFFEE = """
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 8H18L19 18C19.5 21 20 22 12 22C4 22 4.5 21 5 18L6 8Z" 
                stroke="#f5ede4" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M17 8V6C17 4.89543 16.1046 4 15 4H9C7.89543 4 7 4.89543 7 6V8" 
                stroke="#f5ede4" stroke-width="1.8" stroke-linecap="round"/>
            <path d="M18 8H20C21.1046 8 22 8.89543 22 10V12C22 13.1046 21.1046 14 20 14H18" 
                stroke="#f5ede4" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="15" cy="14" r="1" fill="#f5ede4" opacity="0.5"/>
            <circle cx="12" cy="14" r="1" fill="#f5ede4" opacity="0.5"/>
            <circle cx="9" cy="14" r="1" fill="#f5ede4" opacity="0.5"/>
        </svg>
    """;

    private static final String ICON_INFO = """
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" 
                stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 16V12" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="12" cy="8" r="0.5" fill="#3b82f6" stroke="#3b82f6" stroke-width="0.5"/>
        </svg>
    """;

    // ==================== PUBLIC METHODS ====================

    // ── USER EMAILS ──

    public static String getAccountLockedContent(String userName, String reason) {
        String body = buildAccountLockedBody(userName, reason);
        return buildFullEmail(body, ICON_LOCK, "Tài khoản đã bị khóa");
    }

    public static String getAccountUnlockedContent(String userName, String reason) {
        String body = buildAccountUnlockedBody(userName, reason);
        return buildFullEmail(body, ICON_UNLOCK, "Tài khoản đã được mở khóa");
    }

    public static String getAccountCreatedContent(String userName, String username, String password) {
        String body = buildAccountCreatedBody(userName, username, password);
        return buildFullEmail(body, ICON_WELCOME, "Tài khoản đã được tạo");
    }

    public static String getOtpContent(String userName, String otp) {
        String body = buildOtpBody(userName, otp);
        return buildFullEmail(body, ICON_OTP, "Mã xác thực OTP");
    }

    // ── ADMIN EMAILS ──

    public static String getOtpContentForAdmin(String userName, String otp, String userEmail, String username) {
        String body = buildOtpForAdminBody(userName, otp, userEmail, username);
        return buildFullEmail(body, ICON_OTP, "Xác nhận tạo tài khoản (Admin)");
    }

    public static String getAccountCreationRequestContent(String userName, String username, String email, String phone, String role) {
        String body = buildAccountCreationRequestBody(userName, username, email, phone, role);
        return buildFullEmail(body, ICON_INFO, "Yêu cầu tạo tài khoản mới");
    }

    public static String getAccountCreationSuccessContent(String userName, String username, String email, String phone, String role) {
        String body = buildAccountCreationSuccessBody(userName, username, email, phone, role);
        return buildFullEmail(body, ICON_WELCOME, "Tài khoản mới đã được tạo");
    }

    public static String getAccountLockedAdminContent(String userName, String email, String reason) {
        String body = buildAccountLockedAdminBody(userName, email, reason);
        return buildFullEmail(body, ICON_LOCK, "Đã khóa tài khoản người dùng");
    }

    public static String getAccountUnlockedAdminContent(String userName, String email, String reason) {
        String body = buildAccountUnlockedAdminBody(userName, email, reason);
        return buildFullEmail(body, ICON_UNLOCK, "Đã mở khóa tài khoản người dùng");
    }

    // ==================== BUILD BODY CONTENT ====================

    // ── USER: Account Locked ──
    private static String buildAccountLockedBody(String userName, String reason) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #dc2626; margin: 0; font-size: 24px;">Tài khoản đã bị khóa</h2>
                <p style="color: #64748b; margin-top: 6px;">Tài khoản của bạn đã bị tạm ngưng</p>
            </div>
            
            <div style="background: #fef2f2; border-left: 4px solid #dc2626; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #991b1b; font-size: 15px;">
                    <strong>⚠️ Tài khoản của bạn đã bị khóa</strong>
                </p>
            </div>
            
            <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                <tr>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 120px; font-size: 14px;">
                        <strong>Người dùng</strong>
                    </td>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                        %s
                    </td>
                </tr>
                <tr>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                        <strong>Lý do</strong>
                    </td>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                        %s
                    </td>
                </tr>
                <tr>
                    <td style="padding: 10px 0; color: #64748b; font-size: 14px;">
                        <strong>Thời gian</strong>
                    </td>
                    <td style="padding: 10px 0; color: #1e293b; font-size: 14px;">
                        %s
                    </td>
                </tr>
            </table>
            
            <div style="background: #f8fafc; padding: 16px 20px; border-radius: 8px; margin: 16px 0;">
                <p style="margin: 0; color: #475569; font-size: 14px;">
                    <strong>📌 Lưu ý:</strong> Nếu bạn cho rằng đây là sai sót, vui lòng liên hệ bộ phận hỗ trợ để được giải quyết.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_LOCK,
                escapeHtml(userName),
                escapeHtml(reason != null ? reason : "Không có lý do cụ thể"),
                getCurrentDateTime()
        );
    }

    // ── USER: Account Unlocked ──
    private static String buildAccountUnlockedBody(String userName, String reason) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #22c55e; margin: 0; font-size: 24px;">Tài khoản đã được mở khóa</h2>
                <p style="color: #64748b; margin-top: 6px;">Bạn đã có thể đăng nhập lại</p>
            </div>
            
            <div style="background: #f0fdf4; border-left: 4px solid #22c55e; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #166534; font-size: 15px;">
                    <strong>✅ Tài khoản của bạn đã được mở khóa thành công</strong>
                </p>
            </div>
            
            <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                <tr>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 120px; font-size: 14px;">
                        <strong>Người dùng</strong>
                    </td>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                        %s
                    </td>
                </tr>
                <tr>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                        <strong>Lý do mở khóa</strong>
                    </td>
                    <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                        %s
                    </td>
                </tr>
                <tr>
                    <td style="padding: 10px 0; color: #64748b; font-size: 14px;">
                        <strong>Thời gian</strong>
                    </td>
                    <td style="padding: 10px 0; color: #1e293b; font-size: 14px;">
                        %s
                    </td>
                </tr>
            </table>
            
            <div style="background: #f0fdf4; padding: 16px 20px; border-radius: 8px; margin: 16px 0;">
                <p style="margin: 0; color: #166534; font-size: 14px;">
                    <strong>✅ Bạn đã có thể đăng nhập lại vào hệ thống.</strong>
                </p>
            </div>
            """;

        return template.formatted(
                ICON_UNLOCK,
                escapeHtml(userName),
                escapeHtml(reason != null ? reason : "Không có lý do cụ thể"),
                getCurrentDateTime()
        );
    }

    // ── USER: Account Created ──
    private static String buildAccountCreatedBody(String userName, String username, String password) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #4a2c1b; margin: 0; font-size: 24px;">Chào mừng đến với BrewMaster!</h2>
                <p style="color: #64748b; margin-top: 6px;">Tài khoản của bạn đã được tạo thành công</p>
            </div>
            
            <div style="background: #f8f5f0; border-left: 4px solid #c8a882; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #4a2c1b; font-size: 15px;">
                    <strong>Xin chào %s,</strong><br>
                    <span style="color: #64748b;">Tài khoản của bạn đã được tạo trên hệ thống BrewMaster. Dưới đây là thông tin đăng nhập của bạn.</span>
                </p>
            </div>
            
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; margin: 16px 0;">
                <h3 style="color: #4a2c1b; margin: 0 0 16px 0; font-size: 16px;">📋 Thông tin đăng nhập</h3>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 130px; font-size: 14px;">
                            <strong>Tên đăng nhập</strong>
                        </td>
                        <td style="padding: 10px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-weight: 600; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 10px 0; color: #64748b; font-size: 14px;">
                            <strong>Mật khẩu</strong>
                        </td>
                        <td style="padding: 10px 0; color: #1e293b; font-weight: 600; font-size: 14px; padding: 8px 12px; border-radius: 4px; font-family: monospace; background: #f8f5f0;">
                            %s
                        </td>
                    </tr>
                </table>
            </div>
            
            <div style="background: #fef3c7; border: 1px solid #fcd34d; border-radius: 8px; padding: 16px 20px; margin: 16px 0;">
                <p style="margin: 0; color: #92400e; font-size: 14px;">
                    <strong>⚠️ Lưu ý bảo mật:</strong>
                </p>
                <ul style="margin: 6px 0 0 20px; color: #92400e; font-size: 13px;">
                    <li>Vui lòng <strong>đổi mật khẩu</strong> ngay sau khi đăng nhập lần đầu</li>
                    <li>Không chia sẻ mật khẩu với bất kỳ ai</li>
                    <li>Liên hệ hỗ trợ nếu bạn không yêu cầu tạo tài khoản này</li>
                </ul>
            </div>
            """;

        return template.formatted(
                ICON_WELCOME,
                escapeHtml(userName),
                escapeHtml(username),
                escapeHtml(password)
        );
    }

    // ── USER: OTP ──
    private static String buildOtpBody(String userName, String otp) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #4a2c1b; margin: 0; font-size: 24px;">Mã xác thực OTP</h2>
                <p style="color: #64748b; margin-top: 6px;">Nhập mã này để hoàn tất tạo tài khoản</p>
            </div>
            
            <div style="background: #f8f5f0; border-left: 4px solid #c8a882; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #4a2c1b; font-size: 15px;">
                    <strong>Xin chào %s,</strong><br>
                    <span style="color: #64748b;">Bạn đang tạo tài khoản mới trên hệ thống BrewMaster. Nhập mã OTP dưới đây để xác thực.</span>
                </p>
            </div>
            
            <div style="text-align: center; background: #ffffff; border: 2px dashed #c8a882; border-radius: 16px; padding: 30px 20px; margin: 20px 0;">
                <p style="color: #64748b; font-size: 14px; margin-bottom: 12px;">Mã OTP của bạn</p>
                <div style="font-size: 40px; font-weight: 700; letter-spacing: 14px; color: #4a2c1b; background: #f8f5f0; padding: 16px 24px; border-radius: 12px; display: inline-block; font-family: 'Courier New', monospace;">
                    %s
                </div>
                <p style="color: #dc2626; font-size: 13px; margin-top: 14px;">
                    ⏰ Mã có hiệu lực trong <strong>5 phút</strong>
                </p>
            </div>
            
            <div style="background: #fef3c7; border: 1px solid #fcd34d; border-radius: 8px; padding: 16px 20px; margin: 16px 0;">
                <p style="margin: 0; color: #92400e; font-size: 14px;">
                    <strong>⚠️ Lưu ý:</strong> Tuyệt đối không chia sẻ mã OTP với bất kỳ ai. Nếu bạn không yêu cầu tạo tài khoản, vui lòng bỏ qua email này.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_OTP,
                escapeHtml(userName),
                otp
        );
    }

    // ── ADMIN: OTP for Admin ──
    private static String buildOtpForAdminBody(String userName, String otp, String userEmail, String username) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #4a2c1b; margin: 0; font-size: 24px;">Xác nhận tạo tài khoản</h2>
                <p style="color: #64748b; margin-top: 6px;">Vui lòng xác nhận tạo tài khoản mới</p>
            </div>
            
            <div style="background: #f8f5f0; border-left: 4px solid #c8a882; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #4a2c1b; font-size: 15px;">
                    <strong>Xin chào Admin,</strong><br>
                    <span style="color: #64748b;">Bạn đang xác nhận tạo tài khoản mới cho người dùng dưới đây.</span>
                </p>
            </div>
            
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin: 16px 0;">
                <h3 style="color: #4a2c1b; margin: 0 0 12px 0; font-size: 16px;">👤 Thông tin người dùng</h3>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 130px; font-size: 14px;">
                            <strong>Họ tên</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Tên đăng nhập</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #64748b; font-size: 14px;">
                            <strong>Email</strong>
                        </td>
                        <td style="padding: 8px 0; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                </table>
            </div>
            
            <div style="text-align: center; background: #ffffff; border: 2px dashed #c8a882; border-radius: 16px; padding: 24px 20px; margin: 20px 0;">
                <p style="color: #64748b; font-size: 14px; margin-bottom: 12px;">Mã OTP xác nhận</p>
                <div style="font-size: 40px; font-weight: 700; letter-spacing: 14px; color: #4a2c1b; background: #f8f5f0; padding: 16px 24px; border-radius: 12px; display: inline-block; font-family: 'Courier New', monospace;">
                    %s
                </div>
                <p style="color: #dc2626; font-size: 13px; margin-top: 14px;">
                    ⏰ Mã có hiệu lực trong <strong>5 phút</strong>
                </p>
            </div>
            
            <div style="background: #fef3c7; border: 1px solid #fcd34d; border-radius: 8px; padding: 16px 20px; margin: 16px 0;">
                <p style="margin: 0; color: #92400e; font-size: 14px;">
                    <strong>⚠️ Lưu ý:</strong> Vui lòng kiểm tra kỹ thông tin trước khi xác nhận tạo tài khoản.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_OTP,
                escapeHtml(userName),
                escapeHtml(username),
                escapeHtml(userEmail),
                otp
        );
    }

    // ── ADMIN: Account Creation Request ──
    private static String buildAccountCreationRequestBody(String userName, String username, String email, String phone, String role) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #3b82f6; margin: 0; font-size: 24px;">Yêu cầu tạo tài khoản mới</h2>
                <p style="color: #64748b; margin-top: 6px;">Có yêu cầu tạo tài khoản mới trên hệ thống</p>
            </div>
            
            <div style="background: #eff6ff; border-left: 4px solid #3b82f6; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #1e40af; font-size: 15px;">
                    <strong>📌 Thông báo:</strong> Một yêu cầu tạo tài khoản mới đã được gửi.
                </p>
            </div>
            
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin: 16px 0;">
                <h3 style="color: #4a2c1b; margin: 0 0 12px 0; font-size: 16px;">📋 Thông tin chi tiết</h3>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 140px; font-size: 14px;">
                            <strong>Họ tên</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Tên đăng nhập</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Email</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Số điện thoại</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #64748b; font-size: 14px;">
                            <strong>Vai trò</strong>
                        </td>
                        <td style="padding: 8px 0; color: #1e293b; font-size: 14px;">
                            <span style="background: #f8f5f0; padding: 2px 12px; border-radius: 4px;">%s</span>
                        </td>
                    </tr>
                </table>
            </div>
            
            <div style="background: #f0fdf4; padding: 16px 20px; border-radius: 8px; margin: 16px 0;">
                <p style="margin: 0; color: #166534; font-size: 14px;">
                    <strong>✅ Hành động:</strong> Vui lòng kiểm tra OTP trong email để xác nhận tạo tài khoản.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_INFO,
                escapeHtml(userName),
                escapeHtml(username),
                escapeHtml(email),
                escapeHtml(phone),
                escapeHtml(role)
        );
    }

    // ── ADMIN: Account Creation Success ──
    private static String buildAccountCreationSuccessBody(String userName, String username, String email, String phone, String role) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #22c55e; margin: 0; font-size: 24px;">Tài khoản mới đã được tạo thành công</h2>
                <p style="color: #64748b; margin-top: 6px;">Tài khoản đã được tạo và thông báo đến người dùng</p>
            </div>
            
            <div style="background: #f0fdf4; border-left: 4px solid #22c55e; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #166534; font-size: 15px;">
                    <strong>✅ Tài khoản mới đã được tạo thành công</strong>
                </p>
            </div>
            
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin: 16px 0;">
                <h3 style="color: #4a2c1b; margin: 0 0 12px 0; font-size: 16px;">📋 Thông tin tài khoản đã tạo</h3>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 140px; font-size: 14px;">
                            <strong>Họ tên</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Tên đăng nhập</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Email</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Số điện thoại</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #64748b; font-size: 14px;">
                            <strong>Vai trò</strong>
                        </td>
                        <td style="padding: 8px 0; color: #1e293b; font-size: 14px;">
                            <span style="background: #f8f5f0; padding: 2px 12px; border-radius: 4px;">%s</span>
                        </td>
                    </tr>
                </table>
            </div>
            
            <div style="background: #f0fdf4; padding: 16px 20px; border-radius: 8px; margin: 16px 0;">
                <p style="margin: 0; color: #166534; font-size: 14px;">
                    <strong>✅ Hoàn tất:</strong> Người dùng đã được gửi email thông báo với thông tin đăng nhập.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_WELCOME,
                escapeHtml(userName),
                escapeHtml(username),
                escapeHtml(email),
                escapeHtml(phone),
                escapeHtml(role)
        );
    }

    // ── ADMIN: Account Locked Admin ──
    private static String buildAccountLockedAdminBody(String userName, String email, String reason) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #dc2626; margin: 0; font-size: 24px;">Đã khóa tài khoản người dùng</h2>
                <p style="color: #64748b; margin-top: 6px;">Tài khoản đã bị khóa thành công</p>
            </div>
            
            <div style="background: #fef2f2; border-left: 4px solid #dc2626; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #991b1b; font-size: 15px;">
                    <strong>⚠️ Đã khóa tài khoản người dùng</strong>
                </p>
            </div>
            
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin: 16px 0;">
                <h3 style="color: #4a2c1b; margin: 0 0 12px 0; font-size: 16px;">📋 Thông tin tài khoản bị khóa</h3>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 130px; font-size: 14px;">
                            <strong>Người dùng</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Email</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #64748b; font-size: 14px;">
                            <strong>Lý do</strong>
                        </td>
                        <td style="padding: 8px 0; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                </table>
            </div>
            
            <div style="background: #f8fafc; padding: 16px 20px; border-radius: 8px; margin: 16px 0;">
                <p style="margin: 0; color: #475569; font-size: 14px;">
                    <strong>📌 Thông báo:</strong> Người dùng đã được gửi email thông báo về việc khóa tài khoản.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_LOCK,
                escapeHtml(userName),
                escapeHtml(email),
                escapeHtml(reason != null ? reason : "Không có lý do cụ thể")
        );
    }

    // ── ADMIN: Account Unlocked Admin ──
    private static String buildAccountUnlockedAdminBody(String userName, String email, String reason) {
        String template = """
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="margin-bottom: 10px;">%s</div>
                <h2 style="color: #22c55e; margin: 0; font-size: 24px;">Đã mở khóa tài khoản người dùng</h2>
                <p style="color: #64748b; margin-top: 6px;">Tài khoản đã được mở khóa thành công</p>
            </div>
            
            <div style="background: #f0fdf4; border-left: 4px solid #22c55e; padding: 16px 20px; border-radius: 8px; margin-bottom: 24px;">
                <p style="margin: 0; color: #166534; font-size: 15px;">
                    <strong>✅ Đã mở khóa tài khoản người dùng</strong>
                </p>
            </div>
            
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin: 16px 0;">
                <h3 style="color: #4a2c1b; margin: 0 0 12px 0; font-size: 16px;">📋 Thông tin tài khoản được mở khóa</h3>
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; width: 130px; font-size: 14px;">
                            <strong>Người dùng</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #64748b; font-size: 14px;">
                            <strong>Email</strong>
                        </td>
                        <td style="padding: 8px 0; border-bottom: 1px solid #f1f0ed; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px 0; color: #64748b; font-size: 14px;">
                            <strong>Lý do mở khóa</strong>
                        </td>
                        <td style="padding: 8px 0; color: #1e293b; font-size: 14px;">
                            %s
                        </td>
                    </tr>
                </table>
            </div>
            
            <div style="background: #f8fafc; padding: 16px 20px; border-radius: 8px; margin: 16px 0;">
                <p style="margin: 0; color: #475569; font-size: 14px;">
                    <strong>📌 Thông báo:</strong> Người dùng đã được gửi email thông báo về việc mở khóa tài khoản.
                </p>
            </div>
            """;

        return template.formatted(
                ICON_UNLOCK,
                escapeHtml(userName),
                escapeHtml(email),
                escapeHtml(reason != null ? reason : "Không có lý do cụ thể")
        );
    }

    // ==================== FULL EMAIL TEMPLATE ====================

    private static String buildFullEmail(String bodyContent, String icon, String subject) {
        String template = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body, table, td, p, div, h1, h2, h3, h4, h5, h6 {
                        margin: 0;
                        padding: 0;
                        border: 0;
                        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    }
                    
                    body {
                        background-color: #f8f5f0;
                        padding: 20px;
                        line-height: 1.6;
                    }
                    
                    .email-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: #ffffff;
                        border-radius: 16px;
                        box-shadow: 0 4px 24px rgba(74, 44, 27, 0.10);
                        overflow: hidden;
                    }
                    
                    .email-header {
                        background: linear-gradient(135deg, #4a2c1b, #6d4c2a);
                        padding: 28px 40px;
                        text-align: center;
                    }
                    
                    .email-header .logo {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        gap: 12px;
                        font-size: 26px;
                        font-weight: 700;
                        color: #f5ede4;
                        letter-spacing: -0.5px;
                    }
                    
                    .email-header .logo-icon {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        width: 44px;
                        height: 44px;
                    }
                    
                    .email-header .sub {
                        color: #c8a882;
                        font-size: 13px;
                        margin-top: 4px;
                        letter-spacing: 1px;
                    }
                    
                    .email-body {
                        padding: 32px 40px;
                    }
                    
                    .email-footer {
                        background: #f8f5f0;
                        padding: 20px 40px;
                        text-align: center;
                        border-top: 1px solid #e8ddd0;
                    }
                    
                    .email-footer p {
                        font-size: 12px;
                        color: #94a3b8;
                        margin: 2px 0;
                    }
                    
                    .email-footer a {
                        color: #c8a882;
                        text-decoration: none;
                    }
                    
                    .email-footer a:hover {
                        text-decoration: underline;
                    }
                    
                    @media (max-width: 480px) {
                        .email-header {
                            padding: 20px;
                        }
                        .email-body {
                            padding: 20px;
                        }
                        .email-footer {
                            padding: 16px 20px;
                        }
                        .email-header .logo {
                            font-size: 20px;
                            flex-wrap: wrap;
                        }
                        .email-header .logo-icon svg {
                            width: 32px;
                            height: 32px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-header">
                        <div class="logo">
                            <span class="logo-icon">%s</span>
                            <span>BrewMaster</span>
                        </div>
                        <div class="sub">☕ Premium Coffee Experience</div>
                    </div>
                    
                    <div class="email-body">
                        %s
                    </div>
                    
                    <div class="email-footer">
                        <p>© %s <strong>%s</strong> - Tất cả quyền được bảo lưu.</p>
                        <p>📧 %s | 📞 %s | 📍 %s</p>
                        <p style="margin-top: 6px; font-size: 11px; color: #cbd5e1;">
                            Email này được gửi tự động từ hệ thống. Vui lòng không trả lời email này.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;

        return template.formatted(
                subject,
                ICON_COFFEE,
                bodyContent,
                String.valueOf(LocalDateTime.now().getYear()),
                COMPANY_NAME,
                COMPANY_EMAIL,
                COMPANY_PHONE,
                COMPANY_ADDRESS
        );
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }

    private static String getCurrentDateTime() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        );
    }
}