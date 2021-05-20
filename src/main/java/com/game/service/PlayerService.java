package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PlayerService {
    Page<Player> getPlayersList(Specification specification, Pageable sort);
    Player getPlayerById(Long id);
    Player createPlayer(Player player);
    Player updatePlayer(Long id, Player player);
    void deletePlayer(Long id);
    Integer getPlayerCount(Specification<Player> spec);

    Specification<Player> selectByName(String name);
    Specification<Player> selectByTitle(String title);
    Specification<Player> selectByRace(Race race);
    Specification<Player> selectByProfession(Profession profession);
    Specification<Player> selectByDate(Long after, Long before);
    Specification<Player> selectByBan(Boolean banned);
    Specification<Player> selectByExp(Integer minExperience, Integer maxExperience);
    Specification<Player> selectByLevel(Integer minLevel, Integer maxLevel);

    Long checkId(String id);

}
