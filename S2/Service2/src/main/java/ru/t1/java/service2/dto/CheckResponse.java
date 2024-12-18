package ru.t1.java.service2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckResponse {
    private Boolean blocked;

    public boolean isBlocked() {
        return blocked;
    }
}

