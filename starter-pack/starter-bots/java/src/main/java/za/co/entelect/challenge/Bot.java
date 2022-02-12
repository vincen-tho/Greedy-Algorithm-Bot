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

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        //Basic fix logic
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0,1);
        
        //Fix first if too damaged to move
        if(myCar.damage == 5) {
            return FIX;
        }
        
        //Accelerate first if going to slow
        
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        //Basic fix logic
        
        if(myCar.damage >= 5) {
            return FIX;
        }

        //Basic avoidance logic
        
        if (blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL)) {
                
            if (myCar.position.lane != 4 && myCar.position.lane != 1) {
                
                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);
                List<Object> rightNextBlock = rightBlocks.subList(0, 1);

                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);
                List<Object> leftNextBlock = leftBlocks.subList(0, 1);
                
                if (rightBlocks.contains(Terrain.MUD) || rightBlocks.contains(Terrain.WALL) || rightNextBlock.contains(Terrain.MUD) || rightNextBlock.contains(Terrain.WALL) && 
                    !(leftBlocks.contains(Terrain.MUD) || leftBlocks.contains(Terrain.WALL) || leftNextBlock.contains(Terrain.MUD) || leftNextBlock.contains(Terrain.WALL))) {

                    return TURN_LEFT;
                
                } else if (leftBlocks.contains(Terrain.MUD) || leftBlocks.contains(Terrain.WALL) || leftNextBlock.contains(Terrain.MUD) || leftNextBlock.contains(Terrain.WALL) && 
                !(rightBlocks.contains(Terrain.MUD) || rightBlocks.contains(Terrain.WALL) || rightNextBlock.contains(Terrain.MUD) || rightNextBlock.contains(Terrain.WALL))) {

                    return TURN_RIGHT;

                } else {
                    
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
               
                        return LIZARD;
                    
                    }
                
                }

            } else if (myCar.position.lane == 1) {

                return TURN_RIGHT;

            } else if (myCar.position.lane == 4) {
                
                return TURN_LEFT;
            
            }

        }

        //Basic improvement logic
        
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        //Basic aggression logic
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
        for (PowerUps powerUp: available) {
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
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        
        
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }


}

