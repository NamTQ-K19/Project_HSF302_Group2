package hsf302.se2033jv.project_hsf302_group2.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockUnlockAccountRequest {

    @NotNull(message = "User ID is required")
    private Integer userId;

    private Boolean lock; // true = lock, false = unlock
}