package com.kaboos.game;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class Pathfinding {

    private PathsGraph pathsGraph;
    private Room room;

    public Pathfinding(Room room){
        pathsGraph = new PathsGraph();
        this.room = room;
        createNodes();
        connectNodes();
    }

    public void createNodes(){//Creates nodes based on the tiles in the map.
        int widthInTiles = Constants.gameWidth/16;
        int heightInTiles = Constants.gameHeight/16;

        for (int y=0;y<heightInTiles;y++){
            for (int x=0;x<widthInTiles;x++){
                pathsGraph.addTile(new Tile(x,y));
            }
        }
    }

    //Connections are meant to be 4 directional (discluding edge cases), each 'if' brute forces a check to see if a tile is on the edge.
    public void connectNodes(){
        int widthInTiles = Constants.gameWidth/16;
        int heightInTiles = Constants.gameHeight/16;
        Tile targetTile;

        for (int y=0;y<heightInTiles;y++){
            for (int x=0;x<widthInTiles;x++){
                Tile currentTile = pathsGraph.getTile(x,y);

                if (x-1>-1 && connectable(x,y,x-1,y,"left")){//left
                    targetTile = pathsGraph.getTile(x-1,y);
                    pathsGraph.connectTiles(currentTile,targetTile);
                }if(x+1<widthInTiles && connectable(x,y,x+1,y,"right")){//right
                    targetTile = pathsGraph.getTile(x+1,y);
                    pathsGraph.connectTiles(currentTile,targetTile);
                }if(y-1>-1 && connectable(x,y,x,y-1,"bottom")){//bottom
                    targetTile = pathsGraph.getTile(x,y-1);
                    pathsGraph.connectTiles(currentTile,targetTile);
                }if(y+1<heightInTiles && connectable(x,y,x,y+1,"top")){//top
                    targetTile = pathsGraph.getTile(x,y+1);
                    pathsGraph.connectTiles(currentTile,targetTile);
                }
            }
        }
    }

    public boolean connectable(int xFrom, int yFrom, int xTo, int yTo, String direction){//Walls need to be checked in both tiles before a connection is made.
        TiledMapTileLayer walls = (TiledMapTileLayer) room.tiledMap.getLayers().get(1);
        String wallsFrom;
        String wallsTo;

        TiledMapTileLayer.Cell cellFrom = walls.getCell(xFrom,yFrom);
        TiledMapTileLayer.Cell cellTo = walls.getCell(xTo,yTo);

        if (cellFrom==null && cellTo==null){//If there is no wall in either of the two cells then a connection can be made.
            return true;
        }

        wallsFrom = checkTileWalls(xFrom,yFrom);
        wallsTo = checkTileWalls(xTo,yTo);

        if (wallsFrom.equals("all") || wallsTo.equals("all")){//One or both of the tiles is completely closed off.
            return false;
        }

        //The direction is relative from the 'from' tile to the 'to' tile.
        //'top' case does not exist in the tileset as a property used for walls but it exists as a connection.
        switch(direction){
            case "left":
                if (wallsFrom.equals("left") || wallsTo.equals("right")){
                    return false;
                }else{
                    return true;
                }
            case "right":
                if (wallsFrom.equals("right") || wallsTo.equals("left")){
                    return false;
                }else{
                    return true;
                }
            case "bottom":
                if (wallsFrom.equals("bottom")){
                    return false;
                }else{
                    return true;
                }
            case "top":
                if (wallsTo.equals("bottom")){
                    return false;
                }else{
                    return true;
                }
            default://no collidable walls
                return true;
        }
    }

    public String checkTileWalls(int x, int y){
        TiledMapTileLayer walls = (TiledMapTileLayer) room.tiledMap.getLayers().get(1);
        Object side;

        TiledMapTileLayer.Cell cell = walls.getCell(x,y);

        if (cell != null && cell.getTile()!=null) { //There is a wall in this cell.
            MapProperties properties = cell.getTile().getProperties();
            if (properties.containsKey("Collidable")) {
                side = properties.get("Collidable");
                return side.toString();
            }
        }
        return "none";//Not all walls are collidable.
    }

    public GraphPath<Tile> getPath(Tile start, Tile goal){
        GraphPath<Tile> path = pathsGraph.findPath(start,goal);
        return path;
    }

    public PathsGraph getGraph(){
        return pathsGraph;
    }
}

class Tile implements Location<Vector2> {
    private int x,y;
    private int index;
    private Vector2 position;//tile position in game coordinates
    private String name;

    public Tile(int x, int y){
        this.x = x;
        this.y = y;
        //The addition causes it to be the center of the tile
        position = new Vector2((this.x*16 + 16/2) / Constants.P2M,(this.y*16 + 16/2) / Constants.P2M);
        this.name = x + "," + y;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getIndex(){
        return index;
    }

    public String getName(){
        return name;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getOrientation() {
        return 0;
    }

    @Override
    public void setOrientation(float v) {

    }

    @Override
    public float vectorToAngle(Vector2 vector2) {
        return (float)Math.atan2(vector2.y,vector2.x);
    }

    @Override
    public Vector2 angleToVector(Vector2 vector2, float angle) {
        vector2.x = (float)Math.sin(angle);
        vector2.y = (float)Math.cos(angle);
        return vector2;
    }

    @Override
    public Location<Vector2> newLocation() {
        return null;
    }
}

class TilePath implements Connection<Tile> {
    private Tile ft, tt;
    private float cost;

    public TilePath(Tile fromTile, Tile toTile){
        ft = fromTile;
        tt = toTile;
        cost = Vector2.dst(ft.getX(),ft.getY(),tt.getX(),tt.getY());
    }

    @Override
    public float getCost(){
        return cost;
    }

    @Override
    public Tile getFromNode() {
        return ft;
    }

    @Override
    public Tile getToNode() {
        return tt;
    }
}

class PointHeuristic implements Heuristic<Tile> {

    @Override
    public float estimate(Tile currentTile, Tile target) {
        //return Math.abs(currentTile.getX() - target.getX()) + Math.abs(currentTile.getY() - target.getY());
        //Euclidian distance is better here because Manhatten distance causes enemies to hug walls to get the shortest distance,
        // which causes more needless collisions and makes the enemy movement too predictable
        return Vector2.dst(currentTile.getX(),currentTile.getY(),target.getX(),target.getY());
    }
}

class PathsGraph implements IndexedGraph<Tile> {

    private PointHeuristic pointHeuristic = new PointHeuristic();
    private Array<Tile> tiles = new Array<>();
    private Array<TilePath> paths = new Array<>();

    //The 'map' used for path finding, it will not be visible to the player.
    private ObjectMap<Tile, Array<Connection<Tile>>> pathsMap = new ObjectMap<>();

    private int lastIndex = 0;

    public void addTile(Tile p){
        p.setIndex(lastIndex);
        lastIndex++;//index is incremented after giving a point its index.

        tiles.add(p);
    }

    public void connectTiles(Tile ft, Tile tt){
        TilePath tilePath = new TilePath(ft,tt);

        if (!pathsMap.containsKey(ft)){
            pathsMap.put(ft, new Array<Connection<Tile>>());
        }
        pathsMap.get(ft).add(tilePath);
        paths.add(tilePath);
    }

    public GraphPath<Tile> findPath(Tile startTile, Tile target){
        GraphPath<Tile> pointPath = new DefaultGraphPath<>();
        new IndexedAStarPathFinder<>(this).searchNodePath(startTile, target, pointHeuristic, pointPath);
        return pointPath;
    }

    public Array<Tile> getTiles(){
        return tiles;
    }

    public Tile getTile(int x, int y){
        Tile tile = null;
        for (int i=0;i<tiles.size;i++){
            if (tiles.get(i).getX()==x && tiles.get(i).getY()==y){
                tile = tiles.get(i);
            }
        }
        return tile;
    }

    @Override
    public int getIndex(Tile tile) {
        return tile.getIndex();
    }

    @Override
    public int getNodeCount() {
        return lastIndex;
    }

    @Override
    public Array<Connection<Tile>> getConnections(Tile ft) {
        if(pathsMap.containsKey(ft)){
            return pathsMap.get(ft);
        }

        return new Array<>(0);
    }
}
