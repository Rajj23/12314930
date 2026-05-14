package com.vehical.maintence_scheduler.dto;

import java.util.List;

public record VehicleSchedulingResult(
        Integer depotId,
        List<String> selectedTaskIDs,
        Integer totalDuration,
        Integer totalImpact
) {
}
