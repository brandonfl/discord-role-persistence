package com.brandonfl.demobot.db.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepositoryContainer {

  private final DiscordBotConfigRepository discordBotConfigRepository;

}
