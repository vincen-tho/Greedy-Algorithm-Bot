package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import javax.accessibility.AccessibleValue;

import static java.lang.Math.max;

public class Bot {

    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        if (myCar.damage >= 5) {
            return FIX;
        }

        if(myCar.speed < 3) {
            return ACCELERATE;
        }

        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);

        if (myCar.position.lane != 1) {
            List<Object> leftblocks = getBlocksInFrontLeft(myCar.position.lane, myCar.position.block);
            if (leftblocks.contains(PowerUps.BOOST) || leftblocks.contains(PowerUps.TWEET) || leftblocks.contains(PowerUps.EMP) || leftblocks.contains(PowerUps.OIL))  {
                return TURN_LEFT;
            }
        }

        if (myCar.position.lane != 4){
            List<Object> rightblocks = getBlocksInFrontRight(myCar.position.lane, myCar.position.block);
            if (rightblocks.contains(PowerUps.BOOST) || rightblocks.contains(PowerUps.TWEET) || rightblocks.contains(PowerUps.EMP) || rightblocks.contains(PowerUps.OIL))  {
                return TURN_RIGHT;
            }
        }
        

        if (myCar.powerups.length > 0) {
            if (isObstacle(blocks)) {
                if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                }
            }

            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }
    
            if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                return new TweetCommand(opponent.position.lane, opponent.position.block+1);
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


        if (isObstacle(blocks)) {

            if (myCar.position.lane == 1) {
                return TURN_RIGHT;
            }

            if (myCar.position.lane == 4) {
                return TURN_LEFT;
            }

            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }

        if (myCar.damage >= 2) {
            return FIX;
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
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }

        return blocks;
    }

    private List<Object> getBlocksInFrontRight(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
        }
        
        return blocks;
    }

    private List<Object> getBlocksInFrontLeft(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 2);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
        }

        
        return blocks;
    }

    private boolean isObstacle (List<Object> blocks) {

        return blocks.contains(Terrain.WALL) || blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL) || blocks.contains(Terrain.TWEET);
    
    } 
}
