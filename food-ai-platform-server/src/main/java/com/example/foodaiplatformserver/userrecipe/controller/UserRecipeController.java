package com.example.foodaiplatformserver.userrecipe.controller;

import com.example.foodaiplatformserver.userrecipe.dto.UserRecipeCreateRequest;
import com.example.foodaiplatformserver.userrecipe.dto.UserRecipeResponse;
import com.example.foodaiplatformserver.userrecipe.service.UserRecipeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/user-recipes")
public class UserRecipeController {

    private final UserRecipeService userRecipeService;

    public UserRecipeController(UserRecipeService userRecipeService) {
        this.userRecipeService = userRecipeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRecipeResponse create(@Valid @RequestBody UserRecipeCreateRequest request) {
        return userRecipeService.create(request);
    }

    @GetMapping
    public List<UserRecipeResponse> getMyHistories(
            @RequestParam(name = "limit", required = false) @Min(value = 1, message = "limit는 1 이상이어야 합니다.") Integer limit
    ) {
        return userRecipeService.getMyHistories(limit);
    }
}
