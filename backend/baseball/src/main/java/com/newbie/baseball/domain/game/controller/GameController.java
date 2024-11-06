package com.newbie.baseball.domain.game.controller;

import com.newbie.baseball.domain.game.dto.res.GameResponseDto;
import com.newbie.baseball.domain.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "경기정보 조회 API")
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @Operation(summary = "gameID로 경기조회")
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponseDto> getGameById(@PathVariable Integer gameId) {
        GameResponseDto game = gameService.getGameById(gameId);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @Operation(summary = "날짜(년-월)로 월별 경기목록 조회 (ex. 2024-09)")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<GameResponseDto>> getGameByYearAndMonth(@PathVariable String year, @PathVariable String month) {
        List<GameResponseDto> games = gameService.getGameByYearAndMonth(year + "-" + month);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @Operation(summary = "날짜로 해당일자 경기목록 조회 (ex. 2024-09-23)")
    @GetMapping("/{year}/{month}/{day}")
    public ResponseEntity<List<GameResponseDto>> getGameByDate(@PathVariable String year, @PathVariable String month, @PathVariable String day) {
        List<GameResponseDto> games = gameService.getGameByDate(year + "-" + month + "-" + day);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @Operation(summary = "날짜와 팀ID로 해당 날짜에 해당 팀 경기 조회")
    @GetMapping("/{year}/{month}/{day}/{teamId}")
    public ResponseEntity<List<GameResponseDto>> getGameByDateAndTeamId(@PathVariable String year, @PathVariable String month, @PathVariable String day, @PathVariable Integer teamId) {
        List<GameResponseDto> games = gameService.getGameByDateAndTeamId(year + "-" + month + "-" + day, teamId);
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
}