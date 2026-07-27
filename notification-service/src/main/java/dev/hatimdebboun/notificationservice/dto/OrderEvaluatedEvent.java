package dev.hatimdebboun.notificationservice.dto;

public record OrderEvaluatedEvent(Long orderId, String status) {}