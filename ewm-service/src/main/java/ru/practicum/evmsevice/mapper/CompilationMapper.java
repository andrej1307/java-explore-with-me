package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.CompilationDto;
import ru.practicum.evmsevice.dto.EventShortDto;
import ru.practicum.evmsevice.dto.NewCompilationDto;
import ru.practicum.evmsevice.model.Compilation;

import java.util.List;

public class CompilationMapper {
    private CompilationMapper() {}

    public static Compilation toCompilation(NewCompilationDto dto) {
        Compilation c = new Compilation();
        c.setTitle(dto.getTitle());
        c.setPinned(false);
        if (dto.getPinned() != null) {
            c.setPinned(dto.getPinned());
        }
        return c;
    }

    public static CompilationDto toCompilationDto(Compilation c) {
        CompilationDto dto = new CompilationDto();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setPinned(c.getPinned());
        List<EventShortDto> eventDtos = c.getEvents()
                .stream()
                .map(EventMapper::toShortDto)
                .toList();
        dto.setEvents(eventDtos);
        return dto;
    }

}
