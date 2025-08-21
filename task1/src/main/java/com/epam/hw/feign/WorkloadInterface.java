package com.epam.hw.feign;

import com.epam.hw.dto.GetWorkingHoursDTO;
import com.epam.hw.dto.UpdateWorkingHoursDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "WORKLOADMICROSERVICE", url = "http://localhost:8081", configuration = FeignConfig.class, fallback = WorkloadFallback.class)
public interface WorkloadInterface {

    @PostMapping("/trainer/{username}/{action}")
    ResponseEntity<String> updateWorkingHours(
            @PathVariable String username,
            @PathVariable String action,
            @RequestBody UpdateWorkingHoursDTO updateWorkingHoursDTO
    );

    @PostMapping("/trainer/{username}")
    ResponseEntity<String> getWorkingHours(
            @PathVariable String username,
            @RequestBody GetWorkingHoursDTO getWorkingHoursDTO
    );
}