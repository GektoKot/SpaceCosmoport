package com.space.service;

import com.space.controller.ShipOrder;
import com.space.exceptions.BadRequestException;
import com.space.exceptions.ShipNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * TODO: add documentation
 */
@Service
@Transactional
public class ShipService {
    final ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public List<Ship> getAll(String name, String planet, ShipType shipType,
                             Long after, Long before, Boolean isUsed,
                             Double minSpeed, Double maxSpeed,
                             Integer minCrewSize, Integer maxCrewSize,
                             Double minRating, Double maxRating,
                             ShipOrder order, Integer pageNumber, Integer pageSize) {
        List<Ship> allShips = shipRepository.findAll();
        List<Ship> filteredShips = new ArrayList<>();

        if (pageSize.equals(Integer.MIN_VALUE)) {
            pageSize = allShips.size();
        }

        for (Ship ship : allShips) {
            boolean shipIsMatched = true;
            if (!Objects.isNull(name) && !ship.getName().contains(name)) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(planet) && !ship.getPlanet().contains(planet)) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(shipType) && ship.getShipType() != shipType) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(after) && ship.getProdDate().before(new Date(after))) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(before) && ship.getProdDate().after(new Date(before))) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(isUsed) && !ship.getUsed().equals(isUsed)) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(minSpeed) && ship.getSpeed() < minSpeed) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(maxSpeed) && ship.getSpeed() > maxSpeed) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(minCrewSize) && ship.getCrewSize() < minCrewSize) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(maxCrewSize) && ship.getCrewSize() > maxCrewSize) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(minRating) && ship.getRating() < minRating) {
                shipIsMatched = false;
            }
            if (!Objects.isNull(maxRating) && ship.getRating() > maxRating) {
                shipIsMatched = false;
            }
            ////////////////////
            if (shipIsMatched) {
                filteredShips.add(ship);
            }


        }

        filteredShips.sort(shipComparator(order));

        int start = pageNumber * pageSize;
        int end = Math.min((start + pageSize), filteredShips.size());

        return filteredShips.subList(start, end);
    }

    private Comparator<Ship> shipComparator(ShipOrder order) {
        Comparator<Ship> comparator;
        if (order.equals(ShipOrder.SPEED)) {
            comparator = Comparator.comparing(Ship::getSpeed);
        } else if (order.equals(ShipOrder.RATING)) {
            comparator = Comparator.comparing(Ship::getRating);
        } else if (order.equals(ShipOrder.DATE)) {
            comparator = Comparator.comparing(Ship::getProdDate);
        } else {
            comparator = Comparator.comparing(Ship::getId);
        }
        return comparator;
    }

    public Ship createShip(Ship ship) {
        validateParams(ship);
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        ship.setSpeed(Math.round(ship.getSpeed() * 100) / 100d);
        ship.setRating(calculateRating(ship.getSpeed(), ship.getProdDate(), ship.getUsed()));
        return shipRepository.save(ship);
    }

    private Double calculateRating(Double speed, Date prodDate, Boolean used) {
        double k = used ? 0.5 : 1;
        int shipAge = 3019 - (prodDate.getYear() + 1900);
        double rating = (80 * speed * k) / (shipAge + 1);
        return Math.round(rating * 100) / 100d;
    }

    private void validateParams(Ship ship) {
        checkParamsForNull(ship);
        checkParamsBounds(ship);
    }

    private void checkParamsBounds(Ship ship) {
        isNameValid(ship.getName());
        isPlanetValid(ship.getPlanet());
        isProdDateValid(ship.getProdDate());
        isSpeedInBound(ship.getSpeed());
        isCrewSizeInBound(ship.getCrewSize());
    }

    private void checkParamsForNull(Ship ship) {
        if (Objects.isNull(ship.getName()) ||
                Objects.isNull(ship.getPlanet()) ||
                Objects.isNull(ship.getShipType()) ||
                Objects.isNull(ship.getProdDate()) ||
                Objects.isNull(ship.getSpeed()) ||
                Objects.isNull(ship.getCrewSize())) {
            throw new BadRequestException();
        }
    }

    public Ship updateShip(Long id, Ship ship) {
        Ship shipForUpdate = checkAndGetExistingShip(id);
        boolean changed = false;
        if (ship.getName() != null && isNameValid(ship.getName())) {
            shipForUpdate.setName(ship.getName());
            changed = true;
        }
        if (ship.getPlanet() != null && isPlanetValid(ship.getPlanet())) {
            shipForUpdate.setPlanet(ship.getPlanet());
            changed = true;

        }
        if (ship.getSpeed() != null && isSpeedInBound(ship.getSpeed())) {
            shipForUpdate.setSpeed(ship.getSpeed());
            changed = true;

        }
        if (ship.getProdDate() != null && isProdDateValid(ship.getProdDate())) {
            shipForUpdate.setProdDate(ship.getProdDate());
            changed = true;

        }
        if (ship.getCrewSize() != null && isCrewSizeInBound(ship.getCrewSize())) {
            shipForUpdate.setCrewSize(ship.getCrewSize());
            changed = true;

        }
        if (ship.getUsed() != null) {
            shipForUpdate.setUsed(ship.getUsed());
            changed = true;

        }
        if (ship.getShipType() != null) {
            shipForUpdate.setShipType(ship.getShipType());
            changed = true;

        }
        if (changed) {
            shipForUpdate.setRating(calculateRating(shipForUpdate.getSpeed(), shipForUpdate.getProdDate(), shipForUpdate.getUsed()));
        }


        return shipRepository.save(shipForUpdate);
    }



    public Ship findById(Long id) {
        return checkAndGetExistingShip(id);
    }

    public void deleteById(Long id) {
        checkAndGetExistingShip(id);
        shipRepository.deleteById(id);
    }

    private Ship checkAndGetExistingShip(Long id) {
        Optional<Ship> ship = shipRepository.findById(id);
        if (!ship.isPresent()) {
            throw new ShipNotFoundException("!!!SHIP IS NOT EXIST!!!");
        }
        return ship.get();
    }
    private boolean isNameValid(String name) {
        if (name.length() > 50 || name.equals("")) {
            throw new BadRequestException();
        }
        return  true;
    }
    private boolean isPlanetValid(String planet) {
        if (planet.length() > 50 || planet.equals("")) {
            throw new BadRequestException();
        }
        return true;

    }
    private boolean isProdDateValid(Date prodDate) {
        int year = prodDate.getYear() + 1900;
        if (year < 2800 || year > 3019) {
            throw new BadRequestException();
        }
        return true;

    }
    private boolean isSpeedInBound(Double speed) {
        if (speed < 0.01 || speed > 0.99) {
            throw new BadRequestException();
        }
        return true;
    }
    private boolean isCrewSizeInBound(Integer crewSize) {
        if (crewSize < 1 || crewSize > 9999) {
            throw new BadRequestException();
        }
        return true;
    }

}
