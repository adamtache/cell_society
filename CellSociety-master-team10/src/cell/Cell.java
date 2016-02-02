/**
 * @Author: David Yan
 * @Date: Jan 31
 */
package cell;
import javafx.scene.shape.Shape;

public class Cell {

    protected int myX, myY;
    protected int currState;
    protected Shape myShape;

    public Cell(int state){
        currState = state;
        //myShape = new Shape(Rectangle);
    }

    public Cell(int x, int y, int state){
        myX = x;
        myY = y;
        currState = state;
    }

    public Cell(Cell toUse){
        myX = toUse.getX();
        myY = toUse.getY();
        currState = toUse.getState();
    }

    public Cell clone(){
        return new Cell(myX,myY,currState);
    }

    public void setX(int x){
        myX = x;
    }
    public void setY(int y){
        myY = y;
    }
    public void setState(int state){
        currState = state;
    }
    public int getX(){
        return myX;
    }
    public int getY(){
        return myY;
    }
    public int getState(){
        return currState;
    }


}