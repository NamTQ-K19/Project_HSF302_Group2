package hsf302.se2033jv.project_hsf302_group2.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelReservationRequest {

    @NotBlank(message = "Vui lòng nhập lý do hủy")
    private String cancellationReason;
}

