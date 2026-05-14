package com.vehical.maintence_scheduler.service;

import com.logger.middle.service.LoggerService;
import com.vehical.maintence_scheduler.dto.DepotDTO;
import com.vehical.maintence_scheduler.dto.DepotsResponse;
import com.vehical.maintence_scheduler.dto.VehicleSchedulingResult;
import com.vehical.maintence_scheduler.dto.VehicleTaskDTO;
import com.vehical.maintence_scheduler.dto.VehiclesResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleSchedulingService {

    private final EvaluationClient evaluationClient;
    private final LoggerService loggerService;

    public VehicleSchedulingService(EvaluationClient evaluationClient, LoggerService loggerService) {
        this.evaluationClient = evaluationClient;
        this.loggerService = loggerService;
    }

    public VehicleSchedulingResult schedule(Integer depotId) {
        loggerService.Log("backend", "info", "handler", "Vehicle scheduling request received for depot ID " + depotId);

        DepotsResponse depotsResponse = evaluationClient.fetchDepots();
        VehiclesResponse vehiclesResponse = evaluationClient.fetchVehicles();

        List<DepotDTO> depots = Optional.ofNullable(depotsResponse)
                .map(DepotsResponse::depots)
                .orElse(List.of());

        List<VehicleTaskDTO> allVehicles = Optional.ofNullable(vehiclesResponse)
                .map(VehiclesResponse::vehicles)
                .orElse(List.of());

        List<VehicleTaskDTO> vehicles = validVehicles(allVehicles);

        DepotDTO depot = depots.stream()
                .filter(currentDepot -> depotId.equals(currentDepot.ID()))
                .findFirst()
                .orElseThrow(() -> {
                    loggerService.Log("backend", "warn", "service", "Depot ID " + depotId + " was not present in evaluation-service depot response");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Depot not found");
                });

        VehicleSchedulingResult result = scheduleDepot(depot, vehicles);

        loggerService.Log(
                "backend",
                "info",
                "service",
                "Completed vehicle scheduling for depot ID " + depot.ID()
                        + " with " + result.selectedTaskIDs().size()
                        + " tasks, total duration " + result.totalDuration()
                        + ", and total impact " + result.totalImpact()
        );

        return result;
    }

    public List<VehicleSchedulingResult> scheduleAll() {
        loggerService.Log("backend", "info", "handler", "Vehicle scheduling request received for all depots");

        DepotsResponse depotsResponse = evaluationClient.fetchDepots();
        VehiclesResponse vehiclesResponse = evaluationClient.fetchVehicles();

        List<DepotDTO> depots = Optional.ofNullable(depotsResponse)
                .map(DepotsResponse::depots)
                .orElse(List.of());

        List<VehicleTaskDTO> vehicles = validVehicles(Optional.ofNullable(vehiclesResponse)
                .map(VehiclesResponse::vehicles)
                .orElse(List.of()));

        List<VehicleSchedulingResult> results = depots.stream()
                .filter(depot -> depot.ID() != null)
                .map(depot -> scheduleDepot(depot, vehicles))
                .toList();

        loggerService.Log(
                "backend",
                "info",
                "service",
                "Completed vehicle scheduling for " + results.size() + " depots using " + vehicles.size() + " valid vehicle tasks"
        );

        return results;
    }

    private VehicleSchedulingResult scheduleDepot(DepotDTO depot, List<VehicleTaskDTO> vehicles) {
        loggerService.Log(
                "backend",
                "info",
                "service",
                "Running knapsack scheduling for depot ID " + depot.ID()
                        + " with mechanic-hour capacity " + depot.MechanicHours()
                        + " and " + vehicles.size() + " valid vehicle tasks"
        );

        return solve(depot.ID(), depot.MechanicHours(), vehicles);
    }

    private List<VehicleTaskDTO> validVehicles(List<VehicleTaskDTO> allVehicles) {
        return allVehicles.stream()
                .filter(vehicle -> vehicle.TaskID() != null)
                .filter(vehicle -> vehicle.Duration() != null && vehicle.Duration() > 0)
                .filter(vehicle -> vehicle.Impact() != null && vehicle.Impact() >= 0)
                .toList();
    }

    VehicleSchedulingResult solve(Integer depotId, Integer capacity, List<VehicleTaskDTO> tasks) {
        if (capacity == null || capacity < 1 || tasks.isEmpty()) {
            return new VehicleSchedulingResult(depotId, List.of(), 0, 0);
        }

        int[] dp = new int[capacity + 1];
        BitSet[] selected = new BitSet[tasks.size()];

        for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
            VehicleTaskDTO task = tasks.get(taskIndex);
            int duration = task.Duration();
            int impact = task.Impact();
            selected[taskIndex] = new BitSet(capacity + 1);

            if (duration > capacity) {
                continue;
            }

            for (int hours = capacity; hours >= duration; hours--) {
                int candidateImpact = dp[hours - duration] + impact;
                if (candidateImpact > dp[hours]) {
                    dp[hours] = candidateImpact;
                    selected[taskIndex].set(hours);
                }
            }
        }

        List<String> selectedTaskIDs = new ArrayList<>();
        int totalDuration = 0;
        int remainingHours = capacity;

        for (int taskIndex = tasks.size() - 1; taskIndex >= 0; taskIndex--) {
            if (selected[taskIndex].get(remainingHours)) {
                VehicleTaskDTO task = tasks.get(taskIndex);
                selectedTaskIDs.add(task.TaskID());
                totalDuration += task.Duration();
                remainingHours -= task.Duration();
            }
        }

        Collections.reverse(selectedTaskIDs);

        return new VehicleSchedulingResult(depotId, selectedTaskIDs, totalDuration, dp[capacity]);
    }
}
