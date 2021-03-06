package src.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import src.Model.Cell;
import src.Model.Grid;
import src.Model.SquareCell;
import src.controller.Simulation.Simulation;


public class XMLParser {
	private ArrayList<Integer> cellsList = new ArrayList<Integer>();
	private HashMap<Integer, Color> statesMap = new HashMap<Integer, Color>();
	private ArrayList<Double> paramsList = new ArrayList<Double>();
	private ArrayList<Integer> statesList = new ArrayList<Integer>();
	private String simType = "";
	private String sideLength = "";
	private String edgeType = "";
	private String shapeType = "";
	private double sideLen;
	private String[] simNames = {"Fire","WaTor","Life","Segregation", "Slime", "ForagingAnts"};
	private String[] shapeNames = {"Square", "Triangle", "Hexagon"};
	private String[] edgeNames = {"Finite", "Toroidal", "Infinite"};
	private HashMap<String, Simulation> simTypes = new HashMap<String, Simulation>();
	private boolean gridOutlined = true;
	PropertiesReader myReader;

	public XMLParser(File myFile, HashMap<String, Simulation> simTypes) throws ParserConfigurationException, SAXException, IOException, NoSuchFieldException, SecurityException, ClassNotFoundException, DOMException, IllegalArgumentException, IllegalAccessException{
		myReader = new PropertiesReader();
		try {
			myReader.load("english");
		} catch (IOException e) {
			System.out.println("Wrong file");
			System.exit(1);
		}
		this.simTypes = simTypes;
		fileCheck(myFile);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(myFile);
		doc.getDocumentElement().normalize();
		parseFile(doc);
	}

	public void fileCheck(File file) throws IOException{
		String fileExtension = file.getName();
		int pos = fileExtension.lastIndexOf(".");
		if(fileExtension.length() >= 4 && !(pos >= fileExtension.length()-3)){
			fileExtension = fileExtension.substring(pos+1);
		}
		if(!fileExtension.equals("xml")){
			String invalidFileMessage = myReader.getString("InvalidFile");
			popupErrorMessage(invalidFileMessage);
		}
	}

	public void popupErrorMessage(String message){
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(myReader.getString("ErrorAlert"));
		alert.setHeaderText(myReader.getString("RegisterError"));
		alert.setContentText(message);
		alert.showAndWait();
	}

	public Optional<String> textboxErrorMessage(String message){
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle(myReader.getString("RegisterError"));
		dialog.setHeaderText(message);
		dialog.setContentText("Desired value:");
		Optional<String> result = dialog.showAndWait();
		return result;
	}

	public boolean checkValid(String check, String[] list){
		for(int x=0; x<list.length; x++){
			if(list[x].toLowerCase().equals(check.toLowerCase()))
				return true;
		}
		return false;
	}

	public String setSimType(NodeList simNode){
		if(simNode.getLength() == 0 || !checkValid(simNode.item(0).getAttributes().getNamedItem("type").getTextContent(), simNames)){
			while(!checkValid(simType, simNames)){
				Optional<String> input = textboxErrorMessage(myReader.getString("ValidSim"));
				if(input.isPresent()){
					simType = input.get();
				}
			}
		}
		else
			simType = simNode.item(0).getAttributes().getNamedItem(myReader.getString("Type")).getTextContent();
		return simType;
	}

	public boolean setOutline(NodeList lineNode){
		if(lineNode.getLength() != 0){
			Node transparent = lineNode.item(0).getAttributes().getNamedItem(myReader.getString("Transparent"));
			String content = transparent.getTextContent();
			if(content.toLowerCase().equals("false")){
				return false;
			}
		}
		return true;
	}

	public void parseFile(Document doc) throws NoSuchFieldException, SecurityException, ClassNotFoundException, DOMException, IllegalArgumentException, IllegalAccessException{
		NodeList simNode = doc.getElementsByTagName(myReader.getString("Sim"));
		simType = setSimType(simNode);
		NodeList lineNode = doc.getElementsByTagName(myReader.getString("Lines"));
		gridOutlined = setOutline(lineNode);
		NodeList states = doc.getElementsByTagName(myReader.getString("State"));
		createStatesMap(states);
		NodeList cells = doc.getElementsByTagName(myReader.getString("Cells"));
		int NUM_STATES = states.getLength();
		createCellsProperties(cells, NUM_STATES);
		NodeList params = doc.getElementsByTagName(myReader.getString("Param"));
		createParamsList(params, simType);
	}

	public void createCellsList(Node listOfInitialStates){
		String unparsedStates = listOfInitialStates.getTextContent();
		String[] splitStates = unparsedStates.split(" ");
		Integer[] statesArr = new Integer[splitStates.length];
		for(int y=0; y<statesArr.length; y++){
			int state = Integer.parseInt(splitStates[y]);
			if(!statesMap.containsKey(state)){
				popupErrorMessage(myReader.getString(myReader.getString("InvalidState")));
				break;
			}
			cellsList.add(state);
		}
	}

	public void configFromNumTotalCells(Node numTotalCells, int numStates){
		String text = "";
		if(numTotalCells != null)
			text = numTotalCells.getTextContent();
		int totalCells = 0;
		String cellsStr = "";
		while(cellsStr.equals("")){
			try{
				totalCells = Integer.parseInt(text);
				cellsStr = text;
			}
			catch(NumberFormatException e){
				Optional<String> input = textboxErrorMessage(myReader.getString("EnterValidCells"));
				if(input.isPresent()){
					text = input.get();
				}
			}
		}
		for(int y=0; y<totalCells; y++){
			int state = (int) (Math.random()*numStates);
			cellsList.add(state);
		}
	}

	public boolean configureList(NamedNodeMap map, int numStates){
		Node listOfInitialStates = map.getNamedItem("list");
		Node numTotalCells = map.getNamedItem(myReader.getString("TotalCells"));
		if(listOfInitialStates != null){
			createCellsList(listOfInitialStates);
			return true;
		}
		else if(numTotalCells != null){
			configFromNumTotalCells(numTotalCells, numStates);
			return true;
		}
		return false;
	}

	public String configureMissingParam(String errorMessage, String[] paramType){
		String str = "";
		if(str.equals("") || !checkValid(str, paramType)){
			while(!checkValid(str, paramType)){
				Optional<String> input = textboxErrorMessage(errorMessage);
				if(input.isPresent()){
					str = input.get();
				}
			}
		}
		return str;
	}

	public void createCellsProperties(NodeList cells, int numStates){
		for(int x=0; x<cells.getLength(); x++){
			Node node = cells.item(x);
			NamedNodeMap map = node.getAttributes();
			configureList(map, numStates);
			Node lenNode = map.getNamedItem(myReader.getString("SideLength"));
			Node shapeNode = map.getNamedItem(myReader.getString("Shape"));
			Node edgeNode = map.getNamedItem(myReader.getString("Edge"));
			if(lenNode != null){
				sideLength = lenNode.getTextContent();
			}
			if(shapeNode != null){
				shapeType = shapeNode.getTextContent();
			}
			if(edgeNode != null){
				edgeType = edgeNode.getTextContent();
			}
		}
		listCheck(numStates);
		shapeCheck();
		edgeCheck();
		sideLenCheck();
	}

	public void listCheck(int numStates){
		if(cellsList.size() == 0){
			Node node = null;
			configFromNumTotalCells(node,numStates);
		}
	}

	public void shapeCheck(){
		if(shapeType.equals("") || !checkValid(shapeType, shapeNames))
			shapeType = configureMissingParam(myReader.getString("InvalidShape"), shapeNames);
	}

	public void edgeCheck(){
		if(edgeType.equals("") || !checkValid(edgeType, edgeNames))
			edgeType = configureMissingParam(myReader.getString("InvalidEdge"), edgeNames);
	}

	public void sideLenCheck(){
		if(isNum(sideLength))
			sideLen = Double.parseDouble(sideLength);
		if(sideLength == "" || !isNum(sideLength))
			sideLen = configureMissingLength(myReader.getString("InvalidLength"));
	}

	public boolean isNum(String str){
		try{
			Double.parseDouble(str);
			return true;
		}
		catch(NumberFormatException e){
			return false;
		}
	}

	public int configureMissingLength(String errorMessage){
		String str = "";
		while(str.equals("") || !isNum(str)){
			Optional<String> input = textboxErrorMessage(errorMessage);
			if(input.isPresent()){
				str = input.get();
			}
		}
		return Integer.parseInt(str);
	}

	public void createStatesMap(NodeList states) throws NoSuchFieldException, SecurityException, ClassNotFoundException, DOMException, IllegalArgumentException, IllegalAccessException{
		for(int x=0; x<states.getLength(); x++){
			Node node = states.item(x);
			NamedNodeMap map = node.getAttributes();
			Node value = map.getNamedItem(myReader.getString("Value"));
			Node color = map.getNamedItem(myReader.getString("Color"));
			Color myColor;
			Field field = Class.forName("javafx.scene.paint.Color").getField(color.getTextContent()); // toLowerCase because the color fields are RED or red, not Red
			myColor = (Color)field.get(null);
			int state = Integer.parseInt(value.getTextContent());
			statesMap.put(state, myColor);
		}
	}

	public void createParamsList(NodeList params, String simType){
		ArrayList<Double> paramValues = paramsErrorCheck(params, simType);
		for(int x=0; x<paramValues.size(); x++){
			paramsList.add(paramValues.get(x));
		}
	}

	public ArrayList<Double> paramsErrorCheck(NodeList params, String simType){
		ArrayList<Double> paramValues = new ArrayList<Double>();
		ArrayList<String> paramNames = simTypes.get(simType).getParameters();
		int numParams = 0;
		if(paramNames != null)
			numParams = paramNames.size();
		fillWithDefault(paramValues, numParams);
		for(int x=0; x<params.getLength(); x++){
			Node node = params.item(x);
			NamedNodeMap map = node.getAttributes();
			for(int y=0; y<paramNames.size(); y++){
				Node value = map.getNamedItem(paramNames.get(y));
				if(value != null){
					paramValues.set(paramNames.indexOf(paramNames.get(y)), Double.parseDouble(value.getTextContent()));
					break;
				}
			}
		}
		return paramValues;
	}

	public void fillWithDefault(ArrayList<Double> paramValues, int numParams){
		double DEFAULT_VALUE = -1/999;
		for(int x=0; x<numParams; x++){
			paramValues.add(DEFAULT_VALUE);
		}
	}

	public ArrayList<Integer> getCellsList(){
		return cellsList;
	}

	public HashMap<Integer, Color> getStatesMap(){
		return statesMap;
	}

	public ArrayList<Double> getParamsList(){
		return paramsList;
	}

	public String getSimType(){
		return simType;
	}

	public ArrayList<Integer> getStatesList(){
		return statesList;
	}

	public double getCellSideLength(){
		return sideLen;
	}

	public String getCellShape(){
		return shapeType;
	}

	public String getEdgeType(){
		return edgeType;
	}

	public Grid makeInitialGrid(ArrayList<Integer> cellList){
		Grid grid = new Grid(Integer.parseInt(myReader.getString("GridWidth")), Integer.parseInt(myReader.getString("GridHeight")));
		grid.setOutline(gridOutlined);
		grid.createGrid(cellList, shapeType, sideLen, edgeType);
		return grid;
	}
}
