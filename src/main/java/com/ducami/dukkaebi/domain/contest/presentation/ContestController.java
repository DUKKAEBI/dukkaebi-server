package com.ducami.dukkaebi.domain.contest.presentation;

import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contest")
public class ContestController {

    @PostMapping("/create")
    public void createContest(@RequestBody ContestReq req) {
        return;
    }
}
