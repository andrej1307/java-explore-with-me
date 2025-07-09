package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.CompilationDto;
import ru.practicum.evmsevice.dto.NewCompilationDto;
import ru.practicum.evmsevice.dto.PatchCompilationDto;
import ru.practicum.evmsevice.service.CompilationService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/compilations")
public class AdminCompilationsController {
    private final CompilationService compilationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Validated @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Администратор создает подборку событий '{}'.", newCompilationDto.getTitle());
        return compilationService.createCompilation(newCompilationDto);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Integer compId,
                                            @Validated @RequestBody PatchCompilationDto compilationDto) {
        log.info("Администратор обновляет подборку событий '{}'.", compilationDto.getTitle());
        return compilationService.patchCompilation(compId, compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Integer compId) {
        log.info("Администратор удаляет подборку событий id={}.", compId);
        compilationService.deleteCompilation(compId);
    }
}
