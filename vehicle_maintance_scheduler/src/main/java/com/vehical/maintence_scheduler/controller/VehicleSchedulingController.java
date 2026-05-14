package com.vehical.maintence_scheduler.controller;

import com.vehical.maintence_scheduler.dto.VehicleSchedulingResult;
import com.vehical.maintence_scheduler.service.VehicleSchedulingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicle-scheduling")
public class VehicleSchedulingController {

    private final VehicleSchedulingService vehicleSchedulingService;

    public VehicleSchedulingController(VehicleSchedulingService vehicleSchedulingService) {
        this.vehicleSchedulingService = vehicleSchedulingService;
    }

    @GetMapping("/result/{depotId}")
    public VehicleSchedulingResult getResult(@PathVariable Integer depotId) {
        return vehicleSchedulingService.schedule(depotId);
    }

    @GetMapping("/results")
    public List<VehicleSchedulingResult> getResultsForAllDepots() {
        return vehicleSchedulingService.scheduleAll();
    }
}
