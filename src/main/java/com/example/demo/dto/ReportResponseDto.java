package com.example.demo.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ReportResponseDto {
    private int updatedCount;
    private List<Long> updatedUserIds;

    public ReportResponseDto(int updatedCount, List<Long> updatedUserIds) {
        this.updatedCount = updatedCount;
        this.updatedUserIds = updatedUserIds;
    }
}
