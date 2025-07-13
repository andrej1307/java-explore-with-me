package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.CompilationDto;
import ru.practicum.evmsevice.dto.NewCompilationDto;
import ru.practicum.evmsevice.dto.PatchCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto compilationDto);

    CompilationDto patchCompilation(Integer compId, PatchCompilationDto compilationDto);

    void deleteCompilation(Integer compId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilation(Integer compId);
}
