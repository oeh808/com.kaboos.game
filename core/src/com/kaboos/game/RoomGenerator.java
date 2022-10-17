package com.kaboos.game;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.*;

import java.util.ArrayList;

public class RoomGenerator {
    private TiledMap room;
    private TiledMapTileLayer[] layers = new TiledMapTileLayer[4];
    private TiledMapTileSet tileSet;
    private ArrayList<TiledMapTile> ground = new ArrayList<>();

    private ArrayList<TiledMapTile> hWallsTop = new ArrayList<>();
    private ArrayList<TiledMapTile> vWallsTop = new ArrayList<>();

    private ArrayList<TiledMapTile> leftWalls = new ArrayList<>();
    private ArrayList<TiledMapTile> midWalls = new ArrayList<>();
    private ArrayList<TiledMapTile> rightWalls = new ArrayList<>();

    private ArrayList<TiledMapTile> fountains = new ArrayList<>();
    private ArrayList<TiledMapTile> pillars = new ArrayList<>();

    private ArrayList<int[]> spawnPoints = new ArrayList();
    //ID's in tiled start at 0 but conversely start at 1 in code.


    public RoomGenerator(){
        createRoom();
        tileSet = new TmxMapLoader().load("map1.tmx").getTileSets().getTileSet(0);

        populateGroundTileset();
        populateHorizontalWallsTileset();
        populateVerticalWallsTileset();
        populateSpecialWallsTileset();

        generateRoom();
        populateSpawnPoints();
    }

    public void createRoom(){
        room = new TiledMap();

        for (int i=0;i<4;i++){
            layers[i] = new TiledMapTileLayer(Constants.gameWidth/16, Constants.gameHeight/16, 16, 16);
        }

        layers[0].setName("Ground");
        layers[1].setName("Middle");
        layers[2].setName("Collisions");
        layers[3].setName("Top");

        for (int i=0;i<4;i++){
            room.getLayers().add(layers[i]);
        }
    }

    public void populateGroundTileset(){
        //loops through the ground tiles in the tileset
        int index = 0;
        for (int i=129;i<225;i=i+32){
            for (int j=0;j<3;j++){
                ground.add(tileSet.getTile(i+j+1));
                index++;
                if (index==8){
                    break; //Only 8 tiles need to be extracted
                }
            }
        }
    }

    public void populateHorizontalWallsTileset(){
        ArrayList<TiledMapTile> hWalls = new ArrayList<>();

        int index = 0;
        for (int i=33;i<129;i=i+32) {
            for  (int j=0;j<3;j++){
                hWalls.add(tileSet.getTile(i+j+1));
            }
            if (index<3){
                hWallsTop.add(tileSet.getTile(index+2));
                index++;
            }
        }
        leftWalls.add(hWalls.get(0));
        hWalls.remove(0);

        rightWalls.add(hWalls.get(1));//one is used because removing the left wall reduces the array size
        hWalls.remove(1);

        midWalls = hWalls;//only middle walls left now
    }

    public void populateVerticalWallsTileset(){
        for (int i=256;i>192;i=i-32){
            for (int j=0;j<2;j++){
                vWallsTop.add(tileSet.getTile(i+j+1));
            }
        }
        leftWalls.add(tileSet.getTile(289));
        rightWalls.add(tileSet.getTile(290));

    }

    public void populateSpecialWallsTileset(){
        for (int i=4;i<228;i=i+32) {
            //first fountain tile is a top layer, the two pairs following are the two types of the fountain,
            // and the pair after that is a wall with ooze
            fountains.add(tileSet.getTile(i+1));
        }
        for (int i=165;i<261;i=i+32){
            for (int j=0;j<2;j++){
                pillars.add(tileSet.getTile(i+j+1));
            }
        }
    }

    public void generateRoom(){
        //formula for random number (inclusive) : (int)Math.floor(Math.random()*(max-min+1)+min);
        generateGroundLayer();
        generateMiddleLayer();
    }

    public void generateGroundLayer(){
        int min;
        int max;
        for (int row = 0; row < layers[0].getHeight(); row++) {
            for (int col = 0; col < layers[0].getWidth(); col++) {
                min = 1;
                max = 10;
                final TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                if ((int)Math.floor(Math.random()*(max-min+1)+min)>3){//Higher percentage of normal ground tile
                    cell.setTile(ground.get(0));
                }else{
                    max = ground.size()-1;
                    cell.setTile(ground.get((int)Math.floor(Math.random()*(max-min+1)+min)));
                }
                layers[0].setCell(col, row, cell);
            }
        }
    }

    public void generateMiddleLayer(){
        generateWalls();
        generatePillars();
    }

    /*
    This method has a low chance of creating a wall and always creates left walls when there is no wall to its left, otherwise it has a low
    probability of creating a middle wall and a higher chance of making a right wall
    */
    public void generateWalls(){
        int min=1;
        int max=10;
        for (int row = 1; row < layers[1].getHeight()-3; row++) {
            for  (int col = 0; col < layers[1].getWidth(); col++){
                final TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                final TiledMapTileLayer.Cell cellTop = new TiledMapTileLayer.Cell();
                if (layers[1].getCell(col, row)==null || layers[1].getCell(col, row).getTile()==null){//If there is a wall in this cell
                    if (layers[1].getCell(col+1, row)==null){//If there is already a wall to the right, it must be a top wall
                        if (layers[1].getCell(col-1, row)==null && layers[1].getCell(col+2, row)==null){//If there is no wall on the left of this cell
                            if ((int)Math.floor(Math.random()*(max-min+1)+min)>9){//Left wall
                                generateLeftWalls(cell, cellTop, col,row);
                            }
                        }else{//There is a wall on the left
                            if (layers[1].getCell(col-1, row)!=null
                                    && !vWallsTop.contains(layers[1].getCell(col-1, row).getTile())
                                    && !hWallsTop.contains(layers[1].getCell(col-1, row).getTile())
                                    && layers[1].getCell(col-1, row).getTile()!= fountains.get(0)
                                    && layers[1].getCell(col-1, row).getTile()!= pillars.get(1)){//Checks that left wall is not a top wall
                                if (!rightWalls.contains(layers[1].getCell(col-1,row).getTile())){//Checks if the wall on the left is not a right wall
                                    if ((int)Math.floor(Math.random()*(max-min+1)+min)>6){//Creates Middle wall
                                        generateMidWalls(cell, cellTop,col, row);
                                    }else{//Creates Right wall
                                        generateRightWalls(cell, cellTop, col, row);
                                    }
                                }
                            }
                        }
                    }
                }else {
                    if (layers[1].getCell(col, row).getTile() == vWallsTop.get(0) || layers[1].getCell(col, row).getTile() == vWallsTop.get(1)) {
                        generateVerticalWallTop(col, row);
                    }
                }
            }
        }
    }

    public void generateLeftWalls(TiledMapTileLayer.Cell cell, TiledMapTileLayer.Cell cellTop,int col, int row){
        int min=1;
        int max=10;
        if (col!=0 && (int)Math.floor(Math.random()*(max-min+1)+min)>8){//Probability of vertical left wall
            cell.setTile(leftWalls.get(1));
            cellTop.setTile(vWallsTop.get(0));
            if (row+1==layers[1].getHeight()-3){
                final TiledMapTileLayer.Cell cellTopTop = new TiledMapTileLayer.Cell();
                cellTopTop.setTile(vWallsTop.get(2));
                layers[1].setCell(col,row+2,cellTopTop);

                RectangleMapObject obj = (RectangleMapObject)cellTopTop.getTile().getObjects().get(0);
                layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row+2)*16f + obj.getRectangle().getY(),
                        obj.getRectangle().width,obj.getRectangle().height));
            }
        }else{
            cell.setTile(leftWalls.get(0));
            cellTop.setTile(hWallsTop.get(0));
        }

        layers[1].setCell(col, row, cell);
        layers[1].setCell(col,row+1,cellTop);
        RectangleMapObject obj = (RectangleMapObject)cellTop.getTile().getObjects().get(0);
        layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row+1)*16f + obj.getRectangle().getY(),
                obj.getRectangle().width,obj.getRectangle().height));
    }

    public void generateMidWalls(TiledMapTileLayer.Cell cell, TiledMapTileLayer.Cell cellTop,int col, int row){
        int min=1;
        int max=10;
        if ((int)Math.floor(Math.random()*(max-min+1)+min)>7){
            max = 4;
            int selection = (int)Math.floor(Math.random()*(max-min+1)+min);
            switch (selection){
                case 1://red fountain
                    cell.setTile(fountains.get(1));
                    cellTop.setTile(fountains.get(0));

                    layers[3].setCell(col,row+1,cellTop);
                    layers[0].setCell(col, row-1, new TiledMapTileLayer.Cell().setTile(fountains.get(2)));
                    break;
                case 2://blue fountain
                    cell.setTile(fountains.get(3));
                    cellTop.setTile(fountains.get(0));

                    layers[3].setCell(col,row+1,cellTop);
                    layers[0].setCell(col, row-1, new TiledMapTileLayer.Cell().setTile(fountains.get(4)));
                    break;
                case 3:
                    cell.setTile(fountains.get(5));
                    cellTop.setTile(hWallsTop.get(1));
                    layers[0].setCell(col, row-1, new TiledMapTileLayer.Cell().setTile(fountains.get(6)));
                    break;
                default://pillar
                    cell.setTile(pillars.get(3));
                    cellTop.setTile(pillars.get(1));

                    layers[3].setCell(col,row+1,cellTop);
                    layers[0].setCell(col, row-1, new TiledMapTileLayer.Cell().setTile(pillars.get(5)));
            }
        }else if((int)Math.floor(Math.random()*(max-min+1)+min)>5){//non-basic middle wall
            max = midWalls.size()-1;
            cell.setTile(midWalls.get((int)Math.floor(Math.random()*(max-min+1)+min)));
            cellTop.setTile(hWallsTop.get(1));
        }else{
            cell.setTile(midWalls.get(0));
            cellTop.setTile(hWallsTop.get(1));
        }

        layers[1].setCell(col, row, cell);
        layers[1].setCell(col,row+1,cellTop);
        RectangleMapObject obj = (RectangleMapObject)cellTop.getTile().getObjects().get(0);
        layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row+1)*16f + obj.getRectangle().getY(),
                obj.getRectangle().width,obj.getRectangle().height));
    }

    public void generateRightWalls(TiledMapTileLayer.Cell cell, TiledMapTileLayer.Cell cellTop,int col, int row){
        int min=1;
        int max=10;
        if (col != (layers[1].getWidth()-1) && (int)Math.floor(Math.random()*(max-min+1)+min)>8){//Probability of vertical right wall
            cell.setTile(rightWalls.get(1));
            cellTop.setTile(vWallsTop.get(1));
            if (row+1==layers[1].getHeight()-3){
                final TiledMapTileLayer.Cell cellTopTop = new TiledMapTileLayer.Cell();
                cellTopTop.setTile(vWallsTop.get(3));
                layers[1].setCell(col,row+2,cellTopTop);

                RectangleMapObject obj = (RectangleMapObject)cellTopTop.getTile().getObjects().get(0);
                layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row+2)*16f + obj.getRectangle().getY(),
                        obj.getRectangle().width,obj.getRectangle().height));
            }
        }else{
            cell.setTile(rightWalls.get(0));
            cellTop.setTile(hWallsTop.get(2));
        }
        layers[1].setCell(col, row, cell);
        layers[1].setCell(col,row+1,cellTop);
        RectangleMapObject obj = (RectangleMapObject)cellTop.getTile().getObjects().get(0);
        layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row+1)*16f + obj.getRectangle().getY(),
                obj.getRectangle().width,obj.getRectangle().height));
    }

    public void generateVerticalWallTop(int col, int row){
        //REMINDER: need to be fixed for top tile situations being closed off
        int min=1;
        int max=10;
        final TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        if (layers[1].getCell(col, row).getTile() == vWallsTop.get(0)){//left wall
            if (row+1!=layers[1].getHeight()-3 && (int)Math.floor(Math.random()*(max-min+1)+min)>6){//extend vertical wall
                cell.setTile(vWallsTop.get(0));
            }else{//end vertical wall
                cell.setTile(vWallsTop.get(2));
            }
        }else if (layers[1].getCell(col, row).getTile() == vWallsTop.get(1)){//right wall
            if (row+1!=layers[1].getHeight()-3 && (int)Math.floor(Math.random()*(max-min+1)+min)>6){
                cell.setTile(vWallsTop.get(1));
            }else{
                cell.setTile(vWallsTop.get(3));
            }
        }
        layers[1].setCell(col,row+1,cell);
        RectangleMapObject obj = (RectangleMapObject) cell.getTile().getObjects().get(0);
        layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row+1)*16f + obj.getRectangle().getY(),
                obj.getRectangle().width,obj.getRectangle().height));
    }

    public void populateSpawnPoints(){
        for (int row = 1; row < layers[1].getHeight()-1; row++) {
            for (int col = 0; col < layers[1].getWidth(); col++) {
                if (layers[1].getCell(col,row)==null || layers[1].getCell(col, row).getTile()==null){
                    int [] spawnPoint = new int[2];
                    spawnPoint[0] = col * 16;
                    spawnPoint[1] = row * 16;
                    spawnPoints.add(spawnPoint);
                }
            }
        }
    }

    public void generatePillars(){
        int min=1;
        int max=100;
        for (int row = 1; row < layers[1].getHeight()-3; row++) {
            for (int col = 1; col < layers[1].getWidth()-1; col++) {
                final TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                final TiledMapTileLayer.Cell cellTop = new TiledMapTileLayer.Cell();
                if (layers[1].getCell(col,row)==null && layers[1].getCell(col,row-1)==null && layers[1].getCell(col,row+1)==null
                && layers[1].getCell(col-1,row)==null && layers[1].getCell(col+1,row)==null
                && layers[1].getCell(col+1,row+1)==null && layers[1].getCell(col+1,row-1)==null
                && layers[1].getCell(col-1,row+1)==null && layers[1].getCell(col-1,row-1)==null){
                    if ((int)Math.floor(Math.random()*(max-min+1)+min)<5){
                        cell.setTile(pillars.get(2));
                        cellTop.setTile(pillars.get(0));

                        layers[3].setCell(col,row+1,cellTop);
                        layers[1].setCell(col,row+1,cellTop);
                        layers[1].setCell(col, row, cell);
                        layers[0].setCell(col, row-1, new TiledMapTileLayer.Cell().setTile(pillars.get(5)));

                        RectangleMapObject obj = (RectangleMapObject) cell.getTile().getObjects().get(0);
                        layers[2].getObjects().add(new RectangleMapObject(col*16f + obj.getRectangle().getX(),(row)*16f + obj.getRectangle().getY(),
                                obj.getRectangle().width,obj.getRectangle().height));
                    }
                }

            }
        }
    }

    public ArrayList<int[]> getSpawnPoints(){
        return spawnPoints;
    }

    public TiledMap getRoom(){
        return room;
    }
}
