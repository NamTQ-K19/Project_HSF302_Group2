package hsf302.se2033jv.project_hsf302_group2.manager.dto.response;

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
public class CoffeeTableResponse {
    private Integer id;
    private Integer capacity;
    private boolean isActive;
    private Integer floor;
}
