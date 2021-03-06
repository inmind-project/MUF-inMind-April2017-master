package edu.cmu.inmind.multiuser.socialreasoner.control.bn;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;

/**
 * A competence module i can be described by a tuple (ci ai di zi). Where:
 * ci is a list of preconditions which have to be full-filled before the
 * competence module can become active. ai and di represent the expected
 * effects of the competence module's action in terms of an add-list and a
 * delete-list. In addition, each competence module has a level of activation zi
 *
 * @author oromero
 *
 */
public class Behavior implements Comparable<Behavior>{ // implements BehaviorInterface{
	private String name;
	private String id;
	private List<List<Premise>> preconditions = new Vector <>();
	private List<String> addList = new Vector <>();
	private String description;
	private List<String> addGoals = new Vector<>();
	private List<String> deleteList = new Vector <>();

	private transient double activation = 0;
	private transient int idx;
	private transient boolean executable = false, activated = false;
	private transient boolean verbose = false;
	private transient int numMatches;
	private transient String stateMatches;
	private transient int numPreconditions = -1;
	private transient double utility;

	public Behavior(String name){
		this.name = name;
	}

	public Behavior(String name, Premise[][] preconds, String[] addlist, String[] deletelist){
		this.name = name;
		addPreconditions(preconds);
		addList.addAll(Arrays.asList(addlist));
		deleteList.addAll(Arrays.asList(deletelist));
	}

	public Behavior(String name, String description, Premise[][] preconds, String[] addlist, String[] deletelist){
		this(name, preconds, addlist, deletelist);
		this.description = description;
	}

	public Behavior(String name, String description, Premise[][] preconds, String[] addlist, String[] deletelist, String[] addGoals){
		this(name, description, preconds, addlist, deletelist);
		this.description = description;
		this.addGoals.addAll(Arrays.asList(addGoals));

	}

	public void addPreconditions(Premise[][] preconds){
		for(int i = 0; preconds != null && i < preconds.length; i++) {
			List<Premise> precondList = new Vector<>();
			for(int j = 0; j < preconds[i].length; j++){
				precondList.add( preconds[i][j] );
			}
			preconditions.add( precondList );
		}
	}

	public String getId() {return id;}
	public void setId(String id) {this.id = id;}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getAddGoals() {
		return addGoals;
	}
	public List <String> getAddList() {
		return addList;
	}
	public void setAddList(List <String> addList) {
		this.addList = addList;
	}
	public void addAddList(List <String> addList) {
		if( this.addList == null ){
			addList = new Vector<>();
		}
		this.addList.addAll(addList);
	}
	public List <String> getDeleteList() {
		return deleteList;
	}
	public void setDeleteList(List <String> deleteList) {
		this.deleteList = deleteList;
	}
	public double getActivation() {
		return activation;
	}
	public void setActivation(double activation) {
		this.activation = activation;
	}
	public Collection<List<Premise>> getPreconditions() {
		if(preconditions == null){
			preconditions = new Vector<>();
		}
		return preconditions;
	}
	public int getIdx(){
		return this.idx;
	}
	public boolean getExecutable(){
		return executable;
	}
	public String getName(){
		return name;
	}
	public boolean getActivated(){
		return activated;
	}
	public void setActivated(boolean a){
		activated = a;
	}


	/**
	 * Determines if is into the add-list
	 * @param proposition
	 * @return
	 *
	 * Note: modified with weights.
	 */
	public boolean isSuccesor(String proposition){
		return isKindOfLink( addList, proposition );
	}

	/**
	 * Determines if is into the delete-list
	 * @param proposition
	 * @return
	 *
	 * Note: modified with weights.
	 */
	public boolean isInhibition(String proposition){
		return isKindOfLink( deleteList, proposition );
	}



	private boolean isKindOfLink(List<String> list, String proposition){
		if( list.contains(proposition) ) {
			return true;
		}else{
			for( String premise : list ){
				if( premise.contains("*") && Pattern.compile(premise.replace("*", "[a-zA-Z0-9_]*"))
						.matcher(proposition).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	public void setAddList(String proposition){
		addList.add(proposition);
	}

	/**
	 * Determines whether proposition is into the preconditions set
	 * @param proposition
	 * Note: modified with weights.
	 * @return
	 */
	public boolean hasPrecondition(String proposition){
		for(List<Premise> precondList : preconditions ){
			for( Premise precond : precondList ){
				if( precond.getLabel().contains("*")){
					if ( Pattern.compile( precond.getLabel().replace("*", "[a-zA-Z0-9_]*") )
							.matcher( proposition ).matches() ){
						return true;
					}
				}else if( precond.getLabel().equals(proposition) ){
					return true;
				}

			}
		}
		return false;
	}

	/**
	 * the input of activation to module x from the state at time t is
	 * @param states
	 * @param matchedStates
	 * @param phi
	 * Note: modified with weights.
	 * @return
	 */
	public double calculateInputFromState(List<String> states, int[] matchedStates, double phi){
		double activation = 0;
		for(List<Premise> condList : preconditions ){
			for(Premise cond : condList ) {
				int index = findPremise( states, cond.getLabel() );
				if (index != -1) {
					double temp = phi * (1.0d / (double) matchedStates[index]) * (1.0d / (double) preconditions.size()) * cond.getWeight();
					activation += temp;
					if(verbose) {
						System.out.println("state gives " + this.name + " an extra activation of " + temp + " for " + cond);
					}
				}
			}
		}
		return activation;
	}

	private int findPremise(List<String> states, String condition){
		for( int i = 0; i < states.size(); i++ ){
			if( Pattern.compile(condition.replace("*", "[a-zA-Z0-9_]*"))
					.matcher(states.get(i)).matches() ){
				return i;
			}
		}
		return -1;
	}

	/**
	 * The input of activation to competence module x from the goals at time t is
	 * @param goals
	 * @param achievedPropositions
	 * @param gamma
	 * Note: modified with weights.
	 * @return
	 */
	public double calculateInputFromGoals(List<String> goals, int[] achievedPropositions, double gamma){
		double activation = 0;
		for(int i = 0; i < addList.size(); i++){
			int index = findPremise(goals, addList.get(i));
			if(index != -1){
				double temp = gamma * (1.0d / (double) achievedPropositions[index]) * (1.0d / (double) addList.size());
				activation += temp;
				if(verbose) {
					System.out.println("goals give " + this.name + " an extra activation of " + temp);
				}
			}
		}
		return activation;
	}

	/**
	 * The removal of activation from competence module x by the goals that are protected
	 * at time t is.
	 * @param goalsR
	 * @param undoPropositions
	 * @param delta
	 * Note: modified with weights.
	 * @return
	 */
	public double calculateTakeAwayByProtectedGoals(List<String> goalsR, int[] undoPropositions, double delta){
		double activation = 0;
		for(int i = 0; i < deleteList.size(); i++){ //ojrl addlist
			int index = findPremise(goalsR, deleteList.get(i)); //ojrl addList
			if(index != -1){
				double temp = delta * (1.0d / (double) undoPropositions[index]) * (1.0d / (double) deleteList.size());
				activation += temp;
				if(verbose) {
					System.out.println("goalsR give " + this.name + " an extra activation of " + temp);
				}
			}
		}
		return activation;
	}

	/**
	 * A function executable(i t), which returns 1 (true) if competence module i is executable
	 * at time t (i.e., if all of the preconditions of competence module i are members
	 * of S (t)), and 0 (false) otherwise.
	 * Note: modified with weights.
	 */
	public boolean isExecutable (List <String> states){
		Collection<List<Premise>> preconds = new Vector<> (this.getPreconditions());
		for(List<Premise> precondRow : preconds ){
			for(Premise precond : precondRow ){
				if(states.contains(precond.getLabel())){
					//TODO: remove this. This is just for WEF Demo
					return executable = true;
				}
			}
			return executable = false;
		}
		return executable = true;
	}

	public boolean isExecutable (int maximum){
		return executable = numMatches >= maximum;
	}

	public boolean isExecutable (double maximum){
		return executable = utility >= (maximum * .8);
	}

	/**
	 * Note: modified with weights.
	 * @return
	 */
	public double computeUtility() {
		return utility = this.getActivation() + (this.getNumMatches() * 5);
	}

	public void resetActivation(boolean reset){
		if(reset){
//			activation = 0;
			activation = activation/2;
		}
		executable = false;
		activated = false;
	}

	/**
	 * Note: modified with weights.
	 * @param act
	 */
	public void updateActivation(double act){
		activation += act;
		if(activation < 0)
			activation = 1;
	}

	public void decay(double factor){
		activation *= factor;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	@Override
	public int compareTo(Behavior other) {
		double thisUtility = this.computeUtility();
		double otherUtility = other.computeUtility();
		return Double.compare( thisUtility, otherUtility);
	}

	@Override
	public Behavior clone(){
		return Utils.clone(this);
	}

	public void reset() {
		activation = 0;
		executable = false;
		activated = false;
		numMatches = 0;
	}

	/**
	 * Note: modified with weights.
	 * @param states
	 * @return
	 */
	public int calculateMatchPreconditions(CopyOnWriteArrayList<String> states) {
		numMatches = 0;
		stateMatches = "";
		for( List<Premise> precondList : preconditions ){
			for( Premise precond : precondList ){
				if( states.contains(precond.getLabel()) ){
					stateMatches += String.format("[(%s, %s)] ",precond.getLabel(), precond.getWeight());
					numMatches++;
				}
			}
		}
		return numMatches;
	}

	public int getNumMatches() {
		return numMatches;
	}

	public void setNumMatches(int numMatches) {
		this.numMatches = numMatches;
	}

	public String getStateMatches() {
		return stateMatches;
	}
}
