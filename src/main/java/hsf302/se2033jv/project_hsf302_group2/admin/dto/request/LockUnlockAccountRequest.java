package hsf302.se2033jv.project_hsf302_group2.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockUnlockAccountRequest {

    @NotNull(message = "User ID is required")
    private Integer userId;

    private Boolean lock; // true = lock, false = unlock
}