package com.marketplace.catalog.infrastructure.provider.ticketmaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketmasterEventDto(
        String id,
        String name,
        Dates dates,
        @JsonProperty("_embedded") EventEmbedded embedded
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dates(Start start) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Start(
                @JsonProperty("dateTime") String dateTime,
                String localDate
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventEmbedded(List<VenueDto> venues) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record VenueDto(String name, CityDto city) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record CityDto(String name) {}
        }
    }
}
