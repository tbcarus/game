package com.game.service;

import com.game.BadRequestException;
import com.game.NotFoundException;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {
    @Autowired
    PlayerRepository playerRepository;

    @Override
    public Page<Player> getPlayersList(Specification specification, Pageable sort) {

        return playerRepository.findAll(specification, sort);
    }

    @Override
    public Player getPlayerById(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }

        return playerRepository.findById(id).get();
    }

    @Override
    public Player createPlayer(Player player) {
        if (player.getName() == null || player.getTitle() == null || player.getRace() == null || player.getProfession() == null ||
                player.getBirthday() == null || player.getExperience() == null) {
            throw new BadRequestException();
        }
        checkPlayerName(player);
        checkPlayerTitle(player);
        checkPlayerExperience(player);
        checkPlayerBirthday(player);

        if (player.getBanned() == null) {
            player.setBanned(false);
        }


        int level = calcLevel(player.getExperience());
        int untilNextLevel = calcuntilNextLevel(level, player.getExperience());
        player.setLevel(level);
        player.setUntilNextLevel(untilNextLevel);

        return playerRepository.save(player);
    }

    public Long checkId(String id) {
        if (id == null) {
            throw new BadRequestException();
        }
        if(id.contains(".") || id.contains(",")) {
            throw new BadRequestException();
        }

        long checkedId;

        try {
            checkedId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException();
        }

        if (checkedId <= 0) {
            throw new BadRequestException();
        }

        return checkedId;
    }

    private void checkPlayerName(Player player) {
        if (player.getName().length() == 0 || player.getName().length() > 12) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerTitle(Player player) {
        if (player.getTitle().length() > 30) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerExperience(Player player) {
        if (player.getExperience() < 0 || player.getExperience() > 10000000) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerBirthday(Player player) {
        if (player.getBirthday().getTime() < 0 || player.getBirthday().getYear() + 1900 < 2000 || player.getBirthday().getYear() + 1900 > 3000) {
            throw new BadRequestException();
        }
    }

    private int calcLevel(int exp) {
        return (int) ((Math.sqrt(2500 + 200 * exp) - 50) / 100);
    }

    private int calcuntilNextLevel(int level, int exp) {
        return 50 * (level + 1) * (level + 2) - exp;
    }

    @Override
    public Player updatePlayer(Long id, Player player) {

        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        Player updatedPlayer = getPlayerById(id);

        if (player.getName() != null) {
            checkPlayerName(player);
            updatedPlayer.setName(player.getName());
        }

        if (player.getTitle() != null) {
            checkPlayerTitle(player);
            updatedPlayer.setTitle(player.getTitle());
        }

        if (player.getRace() != null) {
            updatedPlayer.setRace(player.getRace());
        }

        if (player.getProfession() != null) {
            updatedPlayer.setProfession(player.getProfession());
        }

        if (player.getBirthday() != null) {
            checkPlayerBirthday(player);
            updatedPlayer.setBirthday(player.getBirthday());
        }

        if (player.getBanned() != null) {
            updatedPlayer.setBanned(player.getBanned());
        }

        if (player.getExperience() != null) {
            checkPlayerExperience(player);
            int level = calcLevel(player.getExperience());
            int untilNextLevel = calcuntilNextLevel(level, player.getExperience());
            updatedPlayer.setExperience(player.getExperience());
            updatedPlayer.setLevel(level);
            updatedPlayer.setUntilNextLevel(untilNextLevel);
        }

        return playerRepository.save(updatedPlayer);
    }

    @Override
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        playerRepository.deleteById(id);
    }

    @Override
    public Integer getPlayerCount(Specification<Player> spec) {
        return (int) playerRepository.count(spec);
    }

    @Override
    public Specification<Player> selectByName(String name) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (name == null) {
                    return null;
                }
                return criteriaBuilder.like(root.get("name"), "%" + name + "%");
            }
        };
    }

    @Override
    public Specification<Player> selectByTitle(String title) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (title == null) {
                    return null;
                }
                return criteriaBuilder.like(root.get("title"), "%" + title + "%");
            }
        };
    }

    @Override
    public Specification<Player> selectByRace(Race race) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (race == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("race"), race);
            }
        };
    }

    @Override
    public Specification<Player> selectByProfession(Profession profession) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (profession == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("profession"), profession);
            }
        };
    }

    @Override
    public Specification<Player> selectByDate(Long after, Long before) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (after == null && before == null) {
                    return null;
                }

                if (after == null) {
                    Date tempBefore = new Date(before);
                    return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), tempBefore);
                }

                if (before == null) {
                    Date tempAfter = new Date(after);
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), tempAfter);
                }

                Calendar beforeCalendar = new GregorianCalendar();
                beforeCalendar.setTime(new Date(before));
                beforeCalendar.set(Calendar.HOUR, 0);
                beforeCalendar.add(Calendar.MILLISECOND, -1);

                Date tempAfter = new Date(after);
                Date tempBefore = beforeCalendar.getTime();

                return criteriaBuilder.between(root.get("birthday"), tempAfter, tempBefore);
            }
        };
    }

    @Override
    public Specification<Player> selectByBan(Boolean banned) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (banned == null) {
                    return null;
                }
                if (banned) {
                    return criteriaBuilder.isTrue(root.get("banned"));
                } else {
                    return criteriaBuilder.isFalse(root.get("banned"));
                }
            }
        };
    }

    @Override
    public Specification<Player> selectByExp(Integer minExperience, Integer maxExperience) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minExperience == null && maxExperience == null) {
                    return null;
                }
                if (minExperience == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience);
                }
                if (maxExperience == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience);
                }
                return criteriaBuilder.between(root.get("experience"), minExperience, maxExperience);
            }
        };
    }

    @Override
    public Specification<Player> selectByLevel(Integer minLevel, Integer maxLevel) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minLevel == null && maxLevel == null) {
                    return null;
                }
                if (minLevel == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel);
                }
                if (maxLevel == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel);
                }
                return criteriaBuilder.between(root.get("level"), minLevel, maxLevel);
            }
        };
    }
}

