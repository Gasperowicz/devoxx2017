package com.devmind.performance.web;

import static org.springframework.http.ResponseEntity.ok;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.devmind.performance.dto.SessionDto;
import com.devmind.performance.model.session.Session;
import com.devmind.performance.repository.SessionRepository;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * In the public API we expose only accepted sessions
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    SessionRepository sessionRepository;

    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> findOne(@PathVariable("id") Long id) {
        return ok().body(SessionDto.convert(sessionRepository.findOne(id)));
    }

    @GetMapping
    @JsonView(SessionDto.SessionList.class)
    public ResponseEntity<List<SessionDto>> findAll() {
        List<Session> sessions = sessionRepository.findAllSessions()
                .stream()
                .filter(session -> Objects.nonNull(session.getStart()))
                .sorted(Comparator.comparing(Session::getStart))
                .collect(Collectors.toList());

        return ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .eTag(String.valueOf(sessions.size() +
                        sessions.stream()
                                .collect(Collectors.summingInt(Session::getVersion))))
                .body(sessions.stream()
                        .map(SessionDto::convert)
                        .collect(Collectors.toList()));
    }

}
