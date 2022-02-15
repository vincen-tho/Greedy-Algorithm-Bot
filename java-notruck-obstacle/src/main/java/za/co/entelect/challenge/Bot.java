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
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        myCar = gameState.player;
        opponent = gameState.opponent;

        //Basic fix logic
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0,1);
        
        //Fix first if too damaged to move
        if(myCar.damage >= 5) {
            return FIX;
        }
        
        //Accelerate first if going to slow
        
        if (myCar.speed < 3) {
            return ACCELERATE;
        }

        //Basic avoidance logic
        
        if (isObstacle(blocks) || isObstacle(nextBlocks)) {
                
            if (myCar.position.lane != 4 && myCar.position.lane != 1) {
                
                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);
                List<Object> rightNextBlock = rightBlocks.subList(0, 0);

                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);
                List<Object> leftNextBlock = leftBlocks.subList(0, 0);
                
                if ( (isObstacle(rightBlocks) || isObstacle(rightNextBlock)) && 
                    !(isObstacle(leftBlocks) || isObstacle(leftNextBlock)) ) {

                    return TURN_LEFT;
                
                } else if ( (isObstacle(leftBlocks) || isObstacle(leftNextBlock))&& 
                !(isObstacle(rightBlocks) || isObstacle(rightNextBlock)) ) {

                    return TURN_RIGHT;

                } else if ( (isObstacle(rightBlocks) || isObstacle(rightNextBlock)) && 
                (isObstacle(leftBlocks) || isObstacle(leftNextBlock)) ){
                    
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
               
                        return LIZARD;
                    
                    } else {
                        return ACCELERATE;
                    }
                
                } else if  (!(isObstacle(rightBlocks) || isObstacle(rightNextBlock)) && 
                !(isObstacle(leftBlocks) || isObstacle(leftNextBlock))) {

                    return TURN_RIGHT;

                }

            } else if (myCar.position.lane == 1) {
                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, gameState);
                List<Object> rightNextBlock = rightBlocks.subList(0, 0);

                if (isObstacle(rightBlocks) || isObstacle(rightNextBlock)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
               
                        return LIZARD;
                    
                    }
                }

                return TURN_RIGHT;

            } else if (myCar.position.lane == 4) {
                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, gameState);
                List<Object> leftNextBlock = leftBlocks.subList(0, 0);

                if (isObstacle(leftBlocks) || isObstacle(leftNextBlock)) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
               
                        return LIZARD;
                    
                    }
                }

                return TURN_LEFT;
            
            }

        }

        //Basic improvement logic
        
        if (myCar.powerups.length > 0) {

            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }
    
            if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                return new TweetCommand(opponent.position.lane, opponent.position.block+ opponent.speed + 1);
            }
    
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                if (opponent.position.lane == myCar.position.lane || opponent.position.lane == myCar.position.lane + 1 || opponent.position.lane == myCar.position.lane - 1){
                    if(opponent.position.block > myCar.position.block){
                        return EMP;
                    }
                }
            }
    
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                if(opponent.position.block < myCar.position.block){
                    return OIL;
                }
            }
        }


        if(myCar.damage >= 2) {
            if(myCar.speed <= 3) {
                return FIX;
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
        List<Object> blocks = new ArrayList<>();
        if (lane != 4 || lane != 1){
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane - 1);
            
            
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + 1; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);

            }
        }
        return blocks;
    }

    // Fungsi untuk memeriksa apakah sebuah block mengandung obstacle

    private boolean isObstacle (List<Object> blocks) {

        return blocks.contains(Terrain.WALL) || blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL);
    
    } 

    private boolean CheckTruck( int lane, int block, GameState gameState) {
        boolean isTruck = false;
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);

        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            if (laneList[i].isOccupiedByCyberTruck) {
                isTruck = true;
                break;
            }
        }

        return isTruck;
    }
}

