package com.fastfood.restaurant;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SettingsDto(boolean takeawayEnabled, boolean dineInEnabled,
                          @Min(0) @Max(100) int pointsPerEuro) {

    @AssertTrue(message = "Au moins un mode (à emporter ou sur place) doit être proposé")
    public boolean isAtLeastOneModeEnabled() {
        return takeawayEnabled || dineInEnabled;
    }

    public static SettingsDto from(RestaurantSettings s) {
        return new SettingsDto(s.isTakeawayEnabled(), s.isDineInEnabled(), s.getPointsPerEuro());
    }
}
