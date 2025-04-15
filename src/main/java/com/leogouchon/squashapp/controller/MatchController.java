package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.MatchRequestDTO;
import com.leogouchon.squashapp.enums.ServiceSide;
import com.leogouchon.squashapp.model.Matches;
import com.leogouchon.squashapp.model.Players;
import com.leogouchon.squashapp.service.MatchService;
import com.leogouchon.squashapp.service.interfaces.IMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final IMatchService matchService;

    @Autowired
    public MatchController(IMatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<Matches>> getMatches() {
        List<Matches> matches = matchService.getMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Matches> getMatch(@PathVariable Long id) {
        Optional<Matches> match = matchService.getMatch(id);
        return match.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Matches> createMatch(@RequestBody MatchRequestDTO matchRequest) {
        System.out.println("POST MAPPING CREATE MATCH");
        Matches createdMatch = matchService.createMatch(
                matchRequest.getPlayerAId(),
                matchRequest.getPlayerBId(),
                matchRequest.getPointsHistory(),
                matchRequest.getFinalScoreA(),
                matchRequest.getFinalScoreB()
        );
        return ResponseEntity.ok(createdMatch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/add-service")
    public ResponseEntity<String> addPoint(
            @PathVariable Long id,
            @RequestParam Players player,
            @RequestParam ServiceSide serviceSide) {
        String pointsHistory = matchService.addPoint(
                matchService.getMatch(id).get(),
                player,
                String.valueOf(serviceSide));
        return ResponseEntity.ok(pointsHistory);
    }

    @GetMapping("/{id}/is-finished")
    public ResponseEntity<Boolean> isFinished(@PathVariable Long id) {
        boolean isFinished = matchService.isFinished(matchService.getMatch(id).get());
        return ResponseEntity.ok(isFinished);
    }
}
