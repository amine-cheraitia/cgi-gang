package com.marketplace.catalog.infrastructure.provider.ticketmaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketmasterSearchResponseDto(
        @JsonProperty("_embedded") Embedded embedded
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Embedded(List<TicketmasterEventDto> events) {}
}
