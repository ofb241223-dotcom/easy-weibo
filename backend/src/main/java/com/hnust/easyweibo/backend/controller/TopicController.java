package com.hnust.easyweibo.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hnust.easyweibo.backend.domain.dto.topic.TopicResponse;
import com.hnust.easyweibo.backend.service.TopicService;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/trending")
    public List<TopicResponse> getTrendingTopics(@RequestParam(value = "limit", defaultValue = "6") int limit) {
        return topicService.getTrendingTopics(limit);
    }
}
