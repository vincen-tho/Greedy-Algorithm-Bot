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

    private Car myCar;
    private Car opponent;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {

    }

    public Command run(GameState gameState) {

        
        myCar = gameState.player;
        opponent = gameState.opponent;

        int maxSpeed = GetMaxSpeed(myCar);

        //Basic fix logic
        List<Lane> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        
        //Fix first if too damaged to move
        if(myCar.damage >= 5) {
            return FIX;
        }
        
        //Accelerate first if going to slow
        
        if (myCar.speed < 3) {
            return ACCELERATE;
        }

        //Basic avoidance logic
        boolean isTruckFront = CheckTruck(blocks);
        int damageFront = calcDamage(blocks);


        // check obstacle
        if (isObstacle(blocks) || isTruckFront || CheckPlayer(blocks)) {
            if (myCar.position.lane != 4 && myCar.position.lane != 1) {
                
                List<Lane> rightBlocks = getBlocksSide(myCar.position.lane + 1, myCar.position.block, gameState);
                
                int damageRight = calcDamage(rightBlocks);


                List<Lane> leftBlocks = getBlocksSide(myCar.position.lane - 1, myCar.position.block, gameState);
                
                int damageLeft = calcDamage(leftBlocks);

                boolean isTruckRight = CheckTruck(rightBlocks);
                boolean isTruckLeft = CheckTruck(leftBlocks);
                
                if ( (isObstacle(rightBlocks) || isTruckRight) && !(isObstacle(leftBlocks)|| isTruckLeft) ) {

                    return TURN_LEFT;
                
                } else if ( (isObstacle(leftBlocks) || isTruckLeft)&& 
                !(isObstacle(rightBlocks) || isTruckRight) ) {

                    return TURN_RIGHT;

                } else if ( (isObstacle(rightBlocks) || isTruckRight) && 
                (isObstacle(leftBlocks) || isTruckLeft) ){
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {

                        if (calcLastDamage(blocks) < damageLeft && calcLastDamage(blocks) < damageRight){
                            return LIZARD;
                        }

                    }
                    
                    if (isTruckFront){
                        if(isTruckLeft && !isTruckRight){

                            return TURN_RIGHT;

                        } else if (isTruckRight && !isTruckLeft) {

                            return TURN_LEFT;

                        } else if (!isTruckLeft && !isTruckRight) {

                            if (damageLeft < damageRight) {

                                return TURN_LEFT;

                            } else {

                                return TURN_RIGHT;

                            }

                        }
                    } else {
                        if (isTruckRight){
                            if (!isTruckLeft){
                                if (damageLeft < damageFront) {
                                    return TURN_LEFT;
                                }
                            }
                        } else {
                            if (isTruckLeft) {
                                if (damageRight < damageFront) {
                                    return TURN_RIGHT;
                                }
                            } else {
                                if (damageFront > damageLeft || damageFront > damageRight) {
                                    if (damageFront > damageLeft && damageFront > damageRight) {
                                        if (damageRight < damageLeft) {
                                            return TURN_RIGHT;
                                        } else {
                                            return TURN_LEFT;
                                        }
                                    } else if (damageFront < damageLeft) {
                                        return TURN_RIGHT;
                                    } else {
                                        return TURN_LEFT;
                                    }
                                } 
                            }
                        }
                    }
                } else if  (!(isObstacle(rightBlocks) || isTruckRight) && 
                !(isObstacle(leftBlocks) || isTruckLeft) ) {

                    return TURN_RIGHT;

                }

            } else if (myCar.position.lane == 1) {
                List<Lane> rightBlocks = getBlocksSide(myCar.position.lane + 1, myCar.position.block, gameState);
                
                int damageRight = calcDamage(rightBlocks);
                boolean isTruckRight = CheckTruck(rightBlocks);

                if (isObstacle(rightBlocks) || isTruckRight) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
               
                        if (calcLastDamage(blocks) < damageRight){
                            return LIZARD;
                        }
                    
                    }

                    if (isTruckFront) {
                        return TURN_RIGHT;
                    } else {
                        if (!isTruckRight) {
                            if (damageFront > damageRight) {
                                return TURN_RIGHT;
                            }
                        }
                    }
                } else {
                    return TURN_RIGHT;
                }

            } else if (myCar.position.lane == 4) {
                List<Lane> leftBlocks = getBlocksSide(myCar.position.lane - 1, myCar.position.block, gameState);

                int damageLeft = calcDamage(leftBlocks);
                boolean isTruckLeft = CheckTruck(leftBlocks);

                if (isObstacle(leftBlocks) || isTruckLeft) {
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {        
                        
                        if (calcLastDamage(blocks) < damageLeft){
                            return LIZARD;
                        }

                    }

                    if (isTruckFront) {
                        return TURN_LEFT;
                    } else {
                        if (!isTruckLeft) {
                            if (damageFront > damageLeft) {
                                return TURN_LEFT;
                            }
                        }
                    }
                } else  {
                    return TURN_LEFT;
                }
            }
        } else if (!isBoost(blocks)) {
            if (myCar.position.lane != 4 && myCar.position.lane != 1) {
                List<Lane> leftBlocks = getBlocksSide(myCar.position.lane - 1, myCar.position.block, gameState);
                List<Lane> rightBlocks = getBlocksSide(myCar.position.lane + 1, myCar.position.block, gameState);
                if (!isObstacle(leftBlocks) && !CheckTruck(leftBlocks) && isBoost(leftBlocks)) {

                    return TURN_LEFT;
                    
                } else if (!isObstacle(rightBlocks) && !CheckTruck(rightBlocks) && isBoost(rightBlocks)) {

                    return TURN_RIGHT;

                }
            } else if (myCar.position.lane == 1) {
                List<Lane> rightBlocks = getBlocksSide(myCar.position.lane + 1, myCar.position.block, gameState);
                if (!isObstacle(rightBlocks) && !CheckTruck(rightBlocks) && isBoost(rightBlocks)) {

                    return TURN_RIGHT;

                }
            } else if (myCar.position.lane == 4) {
                List<Lane> leftBlocks = getBlocksSide(myCar.position.lane - 1, myCar.position.block, gameState);
                if (!isObstacle(leftBlocks) && !CheckTruck(leftBlocks) && isBoost(leftBlocks)) {

                    return TURN_LEFT;

                }
            }
        }

//        Basic improvement logic

                
        if (myCar.powerups.length > 0) {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                List<Lane> blockFar = getBlocksInFrontFar(myCar.position.lane, myCar.position.block, gameState);
                if (myCar.damage == 0 && !myCar.boosting && !isObstacle(blockFar) && !CheckTruck(blockFar)) {
                    return BOOST;     
                }
            }

            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                if(opponent.position.block > myCar.position.block){
                    if (opponent.position.lane == myCar.position.lane || opponent.position.lane == myCar.position.lane + 1 || opponent.position.lane == myCar.position.lane - 1){

                        return EMP;
                    }
                }
            }
            

            if(opponent.position.block < myCar.position.block){
                if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                    return new TweetCommand(opponent.position.lane, opponent.position.block + GetSpeedAfterAccel(opponent) + 1);
                }

                if (myCar.speed == maxSpeed) {
                    if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {

                            return OIL;

                    }
                } 
            }
        } 

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) || !myCar.boosting) {
            if(myCar.damage >= 1) {
                return FIX;
            }
        } else {
            if(myCar.damage >= 2) {
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

    
    private List<Lane> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane> blocks = new ArrayList<>();
        if (lane != 4 || lane != 1){
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane - 1);
            
            
            for (int i = max(block - startBlock, 0); i <= block - startBlock + GetSpeedAfterAccel(myCar); i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i]);

            }
        }

        if (!blocks.isEmpty()) {
            blocks.remove(0);
        }
        return blocks;
    }

    private List<Lane> getBlocksInFrontFar(int lane, int block, GameState gameState) {
        List<Lane> blocks = new ArrayList<>();
        if (lane != 4 || lane != 1){
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane - 1);
            
            
            for (int i = max(block - startBlock, 0); i <= block - startBlock + 15; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i]);

            }
        }
        if (!blocks.isEmpty()) {
            blocks.remove(0);
        }
        return blocks;
    }


    private List<Lane> getBlocksSide(int lane, int block, GameState gameState) {
        List<Lane> blocks = new ArrayList<>();
        if (lane != 4 || lane != 1){
            List<Lane[]> map = gameState.lanes;
            int startBlock = map.get(0)[0].position.block;

            Lane[] laneList = map.get(lane - 1);
            
            
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed-1; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i]);

            }
        }
        return blocks;
    }


    // Fungsi untuk memeriksa apakah sebuah block mengandung obstacle

    private boolean isObstacle (List<Lane> blocks) {
        Lane[] block = new Lane[blocks.size()];
        blocks.toArray(block);
        for (int i = 0; i < block.length; i++) {
            if (block[i].terrain == Terrain.WALL || block[i].terrain == Terrain.MUD || block[i].terrain == Terrain.OIL_SPILL) {
                 return true;
            }
         }

        return false;
    } 
    
    private boolean isBoost (List<Lane> blocks) {
        Lane[] block = new Lane[blocks.size()];
        blocks.toArray(block);
        for (int i = 0; i < block.length; i++) {
            if (block[i].terrain == Terrain.BOOST) {
                 return true;
            }
         }

        return false;
    } 

    private int calcDamage (List<Lane> blocks) {
        Lane[] block = new Lane[blocks.size()];
        blocks.toArray(block);
        int damage = 0;
        for (int i = 0; i < block.length; i++) {
            if(block[i].terrain == Terrain.WALL){
                damage += 2;
            }
            else if (block[i].terrain == Terrain.MUD || block[i].terrain == Terrain.OIL_SPILL) {
                damage += 1;
            }
        }

        return damage;
    }

    private int calcLastDamage (List<Lane> blocks) {
        Lane[] block = new Lane[blocks.size()];
        blocks.toArray(block);
        
        if (block[block.length -1].isOccupiedByCyberTruck) {
            return 999;
        } else if (block[block.length -1].terrain == Terrain.WALL) {
            return 2;
        } else if (block[block.length -1].terrain == Terrain.MUD || block[block.length -1].terrain == Terrain.OIL_SPILL) {
            return 1;
        } else  {
            return 0;
        }
    }

    private boolean CheckTruck(List<Lane> blocks) {
        boolean isTruck = false;
        Lane[] block = new Lane[blocks.size()];
        blocks.toArray(block);
        for (int i = 0; i < block.length; i++) {
           if (block[i].isOccupiedByCyberTruck) {
                isTruck = true;
                break;
           }
        }

        return isTruck;
    }
    private boolean CheckPlayer(List<Lane> blocks) {
        boolean isPlayer = false;
        int playernext = 999;
        Lane[] block = new Lane[blocks.size()];
        blocks.toArray(block);
        for (int i = 0; i < block.length; i++) {
           if (block[i].occupiedByPlayerId == opponent.id) {
                playernext = opponent.position.block - myCar.position.block + opponent.speed;
                break;
           }
        }

        if (playernext <= block.length) {
            isPlayer = true;
        }

        return isPlayer;
    }

    private int GetMaxSpeed(Car player) {
        if (!player.boosting) {
            switch(player.damage) {
                case 1:
                    return 9;
                case 2:
                    return 8;
                case 3:
                    return 6;
                case 4:
                    return 3;
                case 5:
                    return 0;
                default:
                    return 9;
            }
        } else {
            switch(player.damage) {
                case 1:
                    return 9;
                case 2:
                    return 8;
                case 3:
                    return 6;
                case 4:
                    return 3;
                case 5:
                    return 0;
                default:
                    return 15;
            }
        }
    }

    private int GetSpeedAfterAccel(Car player) {
            switch(player.speed) {
                case 3:
                    return 6;
                case 5:
                    return 6;
                case 6:
                    return 8;
                case 8:
                    return 9;
                default:
                    return player.speed;
            }    
    }  
}

