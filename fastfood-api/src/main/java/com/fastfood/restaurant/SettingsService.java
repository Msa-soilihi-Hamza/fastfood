package com.fastfood.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final RestaurantSettingsRepository repository;

    @Transactional(readOnly = true)
    public RestaurantSettings get() {
        return repository.findById(RestaurantSettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Réglages absents : la migration V2 n'a pas été appliquée"));
    }

    @Transactional
    public SettingsDto update(SettingsDto dto) {
        RestaurantSettings settings = get();
        settings.setTakeawayEnabled(dto.takeawayEnabled());
        settings.setDineInEnabled(dto.dineInEnabled());
        settings.setPointsPerEuro(dto.pointsPerEuro());
        return SettingsDto.from(repository.save(settings));
    }
}
