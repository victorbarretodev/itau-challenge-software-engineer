package com.itau.insurance.domain.model;

import lombok.*;
import com.itau.insurance.domain.enums.RequestStatus;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {
    private RequestStatus status;
    private LocalDateTime timestamp;
}
