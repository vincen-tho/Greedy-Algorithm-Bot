package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.io.Console;
import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);


    // Greedy by speed / Boost
    
    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        // Basic fix logic
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0, 1);

        // Damage of 5 equals in a max speed of 0
        // Damage of 4 equals in a max speed of 3
        // Damage of 3 equals in a max speed of 6
        // Damage of 2 equals in a max speed of 8
        // Damage of 1 equals in a max speed of 9
        // Damage of 0 equals in a max speed of 15

        // Fix first if too damaged to move
        if (myCar.damage >= 3) {
            return FIX;
        }

        // Accelerate first if going to slow

        if (myCar.speed <= maxSpeed) {
            return ACCELERATE;
        }

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        // Basic avoidance logic

        if (isObstacle(blocks) || isObstacle(nextBlocks)) {

            if (myCar.position.lane != 4 && myCar.position.lane != 1) {

                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);
                List<Object> rightNextBlock = rightBlocks.subList(0, 0);

                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);
                List<Object> leftNextBlock = leftBlocks.subList(0, 0);

                if(leftNextBlock.contains(PowerUps.BOOST)){
                    return TURN_LEFT;
                }

                if(rightNextBlock.contains(PowerUps.BOOST)){
                    return TURN_RIGHT;
                }

                if ((isObstacle(rightBlocks) || isObstacle(rightNextBlock)) &&
                        !(isObstacle(leftBlocks) || isObstacle(leftNextBlock))) {

                    return TURN_LEFT;

                } else if ((isObstacle(leftBlocks) || isObstacle(leftNextBlock)) &&
                        !(isObstacle(rightBlocks) || isObstacle(rightNextBlock))) {

                    return TURN_RIGHT;

                } else if ((isObstacle(rightBlocks) || isObstacle(rightNextBlock)) &&
                        (isObstacle(leftBlocks) || isObstacle(leftNextBlock))) {

                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {

                        return LIZARD;

                    } else {
                        return ACCELERATE;
                    }

                } else if (!(isObstacle(rightBlocks) || isObstacle(rightNextBlock)) &&
                        !(isObstacle(leftBlocks) || isObstacle(leftNextBlock))) {

                    return TURN_RIGHT;

                }

            } else if (myCar.position.lane == 1) {
                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);
                List<Object> rightNextBlock = rightBlocks.subList(0, 0);

                if (isObstacle(rightBlocks) || isObstacle(rightNextBlock)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {

                        return LIZARD;

                    } else {
                        return ACCELERATE;
                    }
                }

                return TURN_RIGHT;

            } else if (myCar.position.lane == 4) {
                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);
                List<Object> leftNextBlock = leftBlocks.subList(0, 0);

                if (isObstacle(leftBlocks) || isObstacle(leftNextBlock)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {

                        return LIZARD;

                    } else {
                        return ACCELERATE;
                    }
                }
                return TURN_LEFT;

            }

        } else {

            return ACCELERATE;

        }

        // Basic improvement logic

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        // Basic aggression logic
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        return ACCELERATE;

    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp : available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/

    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Object> blocks = new ArrayList<>();
        if (lane != 4 || lane != 1) {
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane - 1);

            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);

            }
        }
        return blocks;
    }

    // Fungsi untuk memeriksa apakah sebuah block mengandung obstacle

    private boolean isObstacle(List<Object> blocks) {

        return blocks.contains(Terrain.WALL) || blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL);

    }

}
