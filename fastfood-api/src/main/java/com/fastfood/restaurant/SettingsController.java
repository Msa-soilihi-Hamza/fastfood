package com.fastfood.restaurant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /** Public : le client doit savoir quels modes le restaurant propose. */
    @GetMapping("/api/settings")
    public SettingsDto get() {
        return SettingsDto.from(settingsService.get());
    }

    @PutMapping("/api/admin/settings")
    public SettingsDto update(@Valid @RequestBody SettingsDto dto) {
        return settingsService.update(dto);
    }
}
