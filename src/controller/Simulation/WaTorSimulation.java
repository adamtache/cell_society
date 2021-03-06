package src.controller.Simulation;
import java.util.*;

import src.Model.Actor;
import src.Model.Cell;
import src.Model.Grid;

public class WaTorSimulation extends Simulation{
	public String returnTitle(){
		return "WaTor Simulation";
	}
	public class Fish extends Actor{

		public Fish(double x, double y){
			super(x, y);
		}
	}
	public class Shark extends Actor{

		public Shark(double x, double y, double actorEnergy, double depletionRate) {
			super(x, y, actorEnergy, depletionRate);
		}
	}
	private List<Actor> actors;
	private double chronon;
	private double fishReproductionTime;
	private double sharkReproductionTime;
	private double sharkDepletionRate;
	private double eatFishRegenerationRate;
	private double sharkEnergy;
	private int emptyCell = 0;
	private int fishCell = 1;
	private int sharkCell = 2;

	public WaTorSimulation(){

	}
	
	public ArrayList<String> paramsList(){
		ArrayList<String> toRet = new ArrayList<String>();
		toRet.add("fishReproductionTime");
		return toRet;
		
	}


	public double getParameter(){
		return fishReproductionTime;
	}
	public void setParameter(double aval){
		fishReproductionTime = aval;
	}

	public void setChrononParameter(double time){
		chronon = time;
	}
	public void setFishReproductionTimeParameter(double time){
		fishReproductionTime = time;
	}
	public void setSharkReproductionTimeParameter(double time){
		sharkReproductionTime = time;
	}
	public void setSharkDepletionRateParameter(double rate){
		sharkDepletionRate = rate;
	}
	public void setEatFishRegenerationRateParameter(double rate){
		eatFishRegenerationRate = rate;
	}
	public void setSharkEnergyParamter(double energy){
		sharkEnergy = energy;
	}
	public void setEmptyCellParameter(int empty){
		emptyCell = empty;
	}
	public void setFishCellParameter(int fish){
		fishCell = fish;
	}
	public void setSharkCellParameter(int shark){
		sharkCell = shark;
	}

	public WaTorSimulation(Grid grid) {
		super(grid);
		actors = new ArrayList<Actor>();
		this.shouldNotUseGridClone();
	}

	public void createActors(Grid grid){
		for(Cell cell : grid.getCells()){
			if(cell.isState(fishCell)){
				Fish fish = new Fish(cell.getCenterX(), cell.getCenterY());
				actors.add(fish);
				cell.setActor(fish);
			}
			if(cell.isState(sharkCell)){
				Shark shark = new Shark(cell.getCenterX(), cell.getCenterY(), sharkEnergy, sharkDepletionRate);
				actors.add(shark);
				cell.setActor(shark);
			}
		}
	}
	public Cell updateCellState(Cell cell) {
		List<Cell> emptyCells = new ArrayList<Cell>();
		List<Fish> neighborFish = new ArrayList<Fish>();
		if(cell.getActors() == null){
			return cell;
		}
		List<Actor> actors = new ArrayList<Actor>();
		actors.addAll(cell.getActors());
		for(Actor actor : actors){
			if(actor instanceof Shark){
				Shark shark = (Shark) actor;
				if(sharkDidDie(cell, shark)){
					return cell;
				}
				reproduceIfAllowed(shark, false);
				sharkMovementRules(cell, shark, emptyCells, neighborFish);
			}
			if(actor instanceof Fish){
				Fish fish = (Fish) actor;
				reproduceIfAllowed(fish, true);
				for(Cell neighborCell : cell.getNonDiagonalNeighbors()){
					if(neighborCell.getActors() == null){
						emptyCells.add(neighborCell);
					}
				}
				fish.getCell().setState(emptyCell);
				fish.moveAtRandom(emptyCells);
				fish.getCell().setState(fishCell);
			}
		}
		return cell;
	}

	public boolean sharkDidDie(Cell cell, Shark shark) {
		shark.depleteEnergy();
		if(shark.isDead()){
			cell.removeActor(shark);
			cell.setState(emptyCell);
			return true;
		}
		return false;
	}

	public void sharkMovementRules(Cell cell, Shark shark, List<Cell> emptyCells, List<Fish> neighborFish) {
		for(Cell neighborCell : cell.getNonDiagonalNeighbors()){
			List<Actor> neighborActors = neighborCell.getActors();
			if(neighborActors.size() > 0){
				Actor neighborActor = neighborCell.getActors().get(0);
				if(neighborActor instanceof Fish){
					neighborFish.add((Fish) neighborActor);
				}
			}
			else{
				emptyCells.add(neighborCell);
			}
		}
		if(neighborFish.size() == 0 && emptyCells.size() > 0){
			shark.getCell().setState(emptyCell);
			shark.moveAtRandom(emptyCells);
			shark.getCell().setState(sharkCell);
		}
		else if(neighborFish.size() > 0){
			eatFishAtRandom(cell, shark, neighborFish);
		}
	}

	public void eatFishAtRandom(Cell cell, Shark shark, List<Fish> neighborFish) {
		Random rand = new Random();
		int randomFishIndex = rand.nextInt(neighborFish.size());
		Fish randomFish = neighborFish.get(randomFishIndex);
		Cell randomFishCell = getGrid().getCell(randomFish.getX(), randomFish.getY());
		randomFishCell.removeActor(randomFish);
		shark.setEnergy(shark.getEnergy() + eatFishRegenerationRate);
		shark.move(randomFishCell);
		cell.removeActor(shark);
		cell.setState(emptyCell);
		Cell newSharkCell = getGrid().getCell(shark.getX(), shark.getY());
		newSharkCell.setState(sharkCell);
		newSharkCell.setActor(shark);
	}
	public void reproduceIfAllowed(Actor actor, boolean useFishReproductionTime){
		double reproductionTime = sharkReproductionTime;
		if(useFishReproductionTime){
			reproductionTime = fishReproductionTime;
		}
		if(actor.getTimeSinceReproduced() >= reproductionTime){
			reproduceAndReset(actor, useFishReproductionTime);
		}
		else{
			actor.updateTimeSinceReproduced(chronon);
		}
	}

	public void reproduceAndReset(Actor actor, boolean useFishReproductionTime) {
		Actor newActor;
		actor.resetTimeSinceReproduced();
		if(useFishReproductionTime){
			newActor = new Fish(actor.getX(), actor.getY());
		}
		else{
			newActor = new Shark(actor.getX(), actor.getY(), actor.getEnergy(), actor.getDepletionRate());
		}
		Cell newActorCell = getGrid().getCell(newActor.getX(), newActor.getY());
		newActorCell.setActor(newActor);
	}
	@Override
	public void createOrRemovePerStep() {
		// TODO Auto-generated method stub

	}
	public ArrayList<String> getParameters() {
		ArrayList<String> params = new ArrayList<String>();
		params.add("chronon");
		params.add("fishReproductionTime");
		params.add("sharkReproductionTime");
		params.add("sharkDepletionRate");
		params.add("eatFishRegenerationRate");
		params.add("sharkEnergy");
		return params;
	}
	public void setParameters(ArrayList<Double> params){
		double DEFAULT_VALUE = getDefaultVal();
		if(params.get(0) != DEFAULT_VALUE)
			chronon = params.get(0);
		if(params.get(1) != DEFAULT_VALUE)
			fishReproductionTime = params.get(1);
		if(params.get(2) != DEFAULT_VALUE)
			sharkReproductionTime = params.get(2);
		if(params.get(3) != DEFAULT_VALUE)
			sharkDepletionRate = params.get(3);
		if(params.get(4) != DEFAULT_VALUE)
			eatFishRegenerationRate = params.get(4);
		if(params.get(5) != DEFAULT_VALUE)
			sharkEnergy = params.get(5);
		createActors(getGrid());
	}
}
