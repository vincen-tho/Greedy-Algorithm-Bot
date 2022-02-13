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
    private final static Command TWEET = new TweetCommand(4,76);
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

        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> leftblocks = getBlocksInFrontLeft(myCar.position.lane, myCar.position.block);
        List<Object> rightblocks = getBlocksInFrontRight(myCar.position.lane, myCar.position.block);
        

        if(myCar.speed < 3) {
            return ACCELERATE;
        }


        if (blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            if (myCar.position.lane == 1) {
                return TURN_RIGHT;
            }

            if (myCar.position.lane == 4) {
                return TURN_LEFT;
            }

            if (leftblocks.contains(Terrain.MUD) || leftblocks.contains(Terrain.WALL)) {
                return TURN_RIGHT;
            }

            if (rightblocks.contains(Terrain.MUD) || rightblocks.contains(Terrain.WALL)) {
                return TURN_LEFT;
            }

            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
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
        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed+1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }

        return blocks;
    }

    private List<Object> getBlocksInFrontRight(int lane, int block) {
        List<Object> blocks = new ArrayList<>();
        if (lane != 4){
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed+1; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);
            }
        }
        
        return blocks;
    }

    private List<Object> getBlocksInFrontLeft(int lane, int block) {
        List<Object> blocks = new ArrayList<>();
        if (lane != 1){
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane - 2);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed+1; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);

            }
        }
        
        return blocks;
    }
}
