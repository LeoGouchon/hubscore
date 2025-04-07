package com.leogouchon.squashapp.controller;

import com.leogouchon.squashapp.dto.MatchRequestDTO;
import com.leogouchon.squashapp.enums.ServiceSide;
import com.leogouchon.squashapp.model.Match;
import com.leogouchon.squashapp.model.Player;
import com.leogouchon.squashapp.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @GetMapping
    public ResponseEntity<List<Match>> getMatches() {
        List<Match> matches = matchService.getMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatch(@PathVariable Long id) {
        Optional<Match> match = matchService.getMatch(id);
        return match.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody MatchRequestDTO matchRequest) {
        System.out.println("POST MAPPING CREATE MATCH");
        Match createdMatch = matchService.createMatch(
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
            @RequestParam Player player,
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
