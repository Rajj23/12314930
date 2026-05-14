package com.vehical.maintence_scheduler.service;

import com.vehical.maintence_scheduler.dto.VehicleSchedulingResult;
import com.vehical.maintence_scheduler.dto.VehicleTaskDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class VehicleSchedulingServiceTests {

    private final VehicleSchedulingService service = new VehicleSchedulingService(null, null);

    @Test
    void solveSelectsHighestImpactTasksWithinMechanicHourBudget() {
        List<VehicleTaskDTO> tasks = List.of(
                new VehicleTaskDTO("T1", 10, 60),
                new VehicleTaskDTO("T2", 20, 100),
                new VehicleTaskDTO("T3", 30, 120)
        );

        VehicleSchedulingResult result = service.solve(7, 50, tasks);

        assertEquals(7, result.depotId());
        assertIterableEquals(List.of("T2", "T3"), result.selectedTaskIDs());
        assertEquals(50, result.totalDuration());
        assertEquals(220, result.totalImpact());
    }

    @Test
    void solveReturnsEmptyResultWhenNoTaskFits() {
        List<VehicleTaskDTO> tasks = List.of(
                new VehicleTaskDTO("T1", 8, 40),
                new VehicleTaskDTO("T2", 9, 50)
        );

        VehicleSchedulingResult result = service.solve(3, 5, tasks);

        assertEquals(3, result.depotId());
        assertEquals(List.of(), result.selectedTaskIDs());
        assertEquals(0, result.totalDuration());
        assertEquals(0, result.totalImpact());
    }
}
