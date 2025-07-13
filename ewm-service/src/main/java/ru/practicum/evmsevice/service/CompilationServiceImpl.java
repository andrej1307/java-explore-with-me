package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.CompilationDto;
import ru.practicum.evmsevice.dto.NewCompilationDto;
import ru.practicum.evmsevice.dto.PatchCompilationDto;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.mapper.CompilationMapper;
import ru.practicum.evmsevice.model.Compilation;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.repository.CompilationRepository;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        List<Event> events = eventService.findEventsByIdIn(compilationDto.getEvents());
        compilation.setEvents(new HashSet<>(events));
        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto savedCompilationDto = CompilationMapper.toCompilationDto(savedCompilation);
        return savedCompilationDto;
    }

    @Override
    public CompilationDto patchCompilation(Integer compId, PatchCompilationDto compilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Не найдена подборка id=" + compId));
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getEvents() != null) {
            if (!compilationDto.getEvents().isEmpty()) {
                List<Event> events = eventService.findEventsByIdIn(compilationDto.getEvents());
                compilation.setEvents(new HashSet<>(events));
            }
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto savedCompilationDto = CompilationMapper.toCompilationDto(savedCompilation);
        return savedCompilationDto;
    }

    @Override
    public void deleteCompilation(Integer compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Не найдена подборка id=" + compId));
        compilation.getEvents().clear();
        compilationRepository.delete(compilation);
    }

    @Override
    public CompilationDto getCompilation(Integer compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Не найдена подборка id=" + compId));
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinnedEquals(pinned);
        } else {
            compilations = compilationRepository.findAll();
        }
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .skip(from)
                .limit(size)
                .toList();
    }
}
