package edu.cmu.inmind.multiuser.socialreasoner.control.bn;

import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * @author oromero
 */
public class BehaviorNetwork {
	private List<Behavior> modules = new Vector<>();
//	private List<String> states = new Vector <>();
    private CopyOnWriteArrayList<String> states = new CopyOnWriteArrayList<>();
	private List<String> goals = new Vector <>();
    private boolean removePrecond = false;
	private transient List<String> goalsResolved = new Vector <>();

	private double pi = 15; //20 the mean level of activation,
	private double theta = 15; //45 the threshold of activation, where is lowered 10% every time no module could be selected, and is reset to its initial value whenever a module becomes active.
	private double initialTheta = 15;//45
	private double phi = 70; //20 // 90 the amount of activation energy injected by the state per true proposition,
	private double gamma = 20; //70 the amount of activation energy injected by the goals per goal,
	private double delta = 50; // 90 not defined the amount of activation energy taken away by the protected goals per protected goal.

	private transient double[][] activationSuccesors;
	private transient double[][] activationPredeccesors;
	private transient double[][] activationConflicters;
	private transient double[] activationInputs;
	private transient double[] activationLinks;
	private transient boolean execution = false;
	private transient int indexBehActivated = -1;
	private transient int cont = 1;
	private transient Double[] activations;
    private transient boolean verbose = false;
    private transient List<String> previousStates;
    private transient List<Behavior> modulesCopy = new Vector<>();
    private transient String nameBehaviorActivated;
	private String output;
	private String matchesOutput;
	private String statesOutput;

	BehaviorNetwork(){}

	public List<Behavior> getModules() {
		return modules;
	}

	synchronized double[] getActivations() {
		int size = activations.length;
		double[] actvs = new double[ size ];
		for( int i = 0; i < size; i++){
			actvs[i] = activations[i];
		}
		return actvs;
	}

	int getIdxBehActivated() {
		return indexBehActivated;
	}

    public boolean isRemovePrecond() {
        return removePrecond;
    }

    public Behavior getBehaviorActivated(){
		if( indexBehActivated >= 0 ) {
			return modules.get(indexBehActivated);
		}
		return null;
	}

	public String getOutput() {
		return output;
	}

	public String getMatchesOutput() {
		return matchesOutput;
	}

	public String getStatesOutput() {
		return statesOutput;
	}

	public String getNameBehaviorActivated() {
        return nameBehaviorActivated;
	}

	public void setModules(List<Behavior> modules) {
		this.modules = modules;
		activationSuccesors = new double[modules.size()][modules.size()];
		activationPredeccesors = new double[modules.size()][modules.size()];
		activationConflicters = new double[modules.size()][modules.size()];
		activationInputs = new double[modules.size()];
		activationLinks = new double[modules.size()];
	}

	void setModules(List<Behavior> modules, int size) {
		this.modules = modules;
		for( int i = 0; i < modules.size(); i++ ){
			modules.get(i).setIdx(i);
		}

		activationSuccesors = new double[modules.size()][modules.size()];
		activationPredeccesors = new double[modules.size()][modules.size()];
		activationConflicters = new double[modules.size()][modules.size()];
		activationInputs = new double[modules.size()];
		activationLinks = new double[modules.size()];
		if( size < modules.size() ) {
			activations = new Double[modules.size()];
		}else{
			activations = new Double[size];
		}
	}

	public double getPi() {
		return pi;
	}

	public void setPi(double pi) {
		this.pi = pi;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public double getInitialTheta() {
		return initialTheta;
	}

	public void setInitialTheta(double itheta) {
		this.initialTheta = itheta;
	}

	public double getPhi() {
		return phi;
	}

	public void setPhi(double phi) {
		this.phi = phi;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	/**
	 * a function S (t) returning the propositions that are observed to be true at time t
	 * (the state of the environment as perceived by the agent) S being implemented
	 * by an independent process (or the real world),
	 */
	CopyOnWriteArrayList<String> getState (){
		if(states != null)
			return states;
		return new CopyOnWriteArrayList<>();
	}

	public String getStateString(){
		String result = "";
		if(states != null) {
			for( String state : states ){
				result += state + ", ";
			}
			result = result.substring(0, result.length()-2);
		}
		return result;
	}

    public String getPreviousStateString(){
        String result = "";
        if(previousStates != null) {
            for( String state : previousStates ){
                result += state + ", ";
            }
            result = result.substring(0, result.length()-2);
        }
        return result;
    }

	public synchronized void setState(CopyOnWriteArrayList<String> states){
        try {
            this.states = states; //new CopyOnWriteArrayList<>(states);
        }catch (Exception e){
            e.printStackTrace();
        }
	}

//	public void addState(List<String> states){
//		if(states == null)
//			this.states = null;
//		else
//			for(int i = 0; i < states.size(); i++) {
//				if( !this.states.contains(states.get(i)) ) {
//					this.states.add(states.get(i));
//				}
//			}
//	}

	/**
	 * a function G(t) returning the propositions that are a goal of the agent at time
	 * t G being implemented by an independent process,
	 */
	public List<String> getGoals (){
		if(goals != null)
			return goals;
		return new Vector<>();
	}

	public void setGoals(List<String> goals){
		if(goals == null)
			this.goals = null;
		else
			this.goals.addAll(goals);
	}

	/**
	 * a function R(t) returning the propositions that are a goal of the agent that
	 * has already been achieved at time t R being implemented by an independent
	 * process (e.g. some internal or external goal creator),
	 */
	private Collection<String> getGoalsR (){
		if(goalsResolved != null)
			return goalsResolved;
		return new Vector<>();
	}

	private void setGoalsR(List<String> goalsR){
		if(goalsR == null)
			this.goalsResolved = null;
		else
			this.goalsResolved.addAll(goalsR);
	}

	/**
	 * A function executable(i t), which returns 1 if competence module i is executable
	 * at time t (i.e., if all of the preconditions of competence module i are members
	 * of S (t)), and 0 otherwise.
	 */
	public boolean executable (int i){
		try {
			return modules.get(i).isExecutable(states);
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

    public boolean executable (int idx, int maximum){
        try {
            return modules.get(idx).isExecutable(maximum);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

	private boolean executable (int idx, double maximum){
		try {
			return modules.get(idx).isExecutable(maximum);
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * a function M (j), which returns the set of modules that match proposition j ,
	 * i.e., the modules x for which j E cx,
	 * Note: modified with weights.
	 */
	private List<Behavior> matchProposition(String proposition){
		Vector<Behavior> behaviors = new Vector<>();
		for(Behavior behavior : modules){
			if(behavior.hasPrecondition(proposition))
				behaviors.add(behavior);
		}
		return behaviors;
	}

	/**
	 * a function A(j ), which returns the set of modules that achieve proposition j ,
	 * i.e., the modules x for which j E ax,
	 * Note: modified with weights.
	 */
	private Vector<Behavior> achieveProposition(String proposition){
		Vector<Behavior> behaviors = new Vector<>();
		for(Behavior beh : modules){
			if( beh.isSuccesor(proposition) )
				behaviors.add( beh );
		}
		return behaviors;
	}

	/**
	 * a function U (j ), which returns the set of modules that undo proposition j , i.e.,
	 * the modules x for which j E dx,
	 */
	private Vector<Behavior> undoProposition(String proposition){
		Vector<Behavior> behaviors = new Vector<>();
		for(Behavior beh : modules){
			if( beh.isInhibition(proposition) )
				behaviors.add( beh );
		}
		return behaviors;
	}

	/**
	 * a function U (j ), which returns the set of modules that undo proposition j , i.e.,
	 * the modules x for which j E dx, and j E S(t)
	 *
	 * Note: modified with weights.
	 */
	private Vector<Behavior> undoPropositionState(String proposition, int indexBehx){
		Vector<Behavior> behaviors = new Vector<>();
		for(Behavior behx : modules){
			if(behx.getIdx() != indexBehx){
				if( behx.isInhibition(proposition) && states.contains(proposition) )
					behaviors.add(behx);
			}
		}
		return behaviors;
	}

	/**
	 * The impact of the state, goals and protected goals on the activation level of a module is computed.
	 Note: modified with weights.
	 */
	private void computeActivation (){
		Vector<String> statesList = new Vector<>(getState());
		Vector<String> goalsList = new Vector<>(getGoals());
		Vector<String> goalsRList = new Vector<>(getGoalsR());
		int[] matchesByState = new int[statesList.size()];
		int[] matchesByGoal = new int[goals.size()];
		int[] matchesByGoalsResolved = new int[states.size()];//new int[goals.size()];

		//#M(j)
		for(int i = 0; i < statesList.size(); i++){
			matchesByState[i] = matchProposition(statesList.get(i)).size();
		}
		//#A(j)
		for(int i = 0; i < goalsList.size(); i++){
			matchesByGoal[i] = achieveProposition(goalsList.get(i)).size();
		}
		//#U(j)
		//ojrl for(int i = 0; i < statesList.size(); i++){
		for(int i = 0; i < goalsRList.size(); i++){
			//ojrl matchesByGoalsResolved[i] = undoProposition(statesList.get(i)).size();
			matchesByGoalsResolved[i] = undoProposition(goalsRList.get(i)).size();
		}

		for(int i = 0; i < modules.size(); i++){
			double actState = modules.get(i).calculateInputFromState(statesList, matchesByState, phi);
			double actGoal = modules.get(i).calculateInputFromGoals(goalsList, matchesByGoal, gamma);
			double actGoalR = modules.get(i).calculateTakeAwayByProtectedGoals(goalsRList, matchesByGoalsResolved, delta); //ojrl states
			activationInputs[i] += actState + actGoal - actGoalR;
		}
	}

	/**
	 * The way the competence module activates and inhibits related modules through
	 * its successor links, predecessor links and conflicter links is computed.
	 *
	 * Note: modified with weights.
	 */
	private void computeLinks (){
        //int highest = highestNumPreconditions();
		double highest = highestUtility();
		for(int i = 0; i < modules.size(); i++){
			//boolean isExecutable = executable(i);
            boolean isExecutable = executable(i, highest);
			activationSuccesors[i] = spreadsForward(modules.get(i).getIdx(), isExecutable);
			activationPredeccesors[i] = spreadsBackward(modules.get(i).getIdx(), isExecutable);
			activationConflicters[i] = takesAway(modules.get(i).getIdx());
		}
	}

	/**
	 * Note: modified with weights.
	 * @return
	 */
	private double highestUtility() {
		double maximum = 0;
		for (Behavior module : modules) {
			module.calculateMatchPreconditions(states);
			double utility = module.computeUtility();
			if (utility > maximum) {
				maximum = utility;
			}
		}
		return maximum;
	}

	/**
     * This method returns the behavior with the highest number of preconditions present at the current state.
     * @return
     */
    private int highestNumPreconditions() {
        int maximum = 0;
		for (Behavior module : modules) {
			int numMatched = module.calculateMatchPreconditions(states);
			if (numMatched > maximum) {
				maximum = numMatched;
			}
		}
        return maximum;
    }

    /**
	 * An executable competence module x spreads activation forward. It increases
	 * (by a fraction of its own activation level) the activation level of those
	 * successors y for which the shared proposition p E ax n cy is not true.
	 * Intuitively, we want these successor modules to become more activated because
	 * they are 'almost executable', since more of their preconditions will be fulfilled
	 * after the competence module has become active.
	 * Formally, given that competence module x = (cx ax dx zx) is executable,
	 * it spreads forward through those successor links for which the proposition
	 * that defines them p E ax is false.
	 *
	 * Note: modified with weights.
	 */
	private double[] spreadsForward(int indexBehavior, boolean executable){
		double[] activation = new double[modules.size()];
		if(executable){
			Vector<String> addList = new Vector<> ( modules.get(indexBehavior).getAddList() );
			for(String addPremise : addList ){
				if( !states.contains(addPremise) ){ //p E ax is false
                    List<Behavior> sparseBehaviors = matchProposition(addPremise); //j E ax n cy
					for(Behavior beh : sparseBehaviors ){
						int cardinalityMj = sparseBehaviors.size();
						int cardinalityCy = beh.getPreconditions().size();
						double temp = beh.getActivation() * (phi/gamma) * (1d / cardinalityMj) * (1d / cardinalityCy);
                        activation[beh.getIdx()] += temp;
                        if( verbose ) {
                            System.out.println(modules.get(indexBehavior).getName() + " spreads " + temp + " forward to " +
                                    beh.getName() + " for " + addPremise);
                        }
					}
				}
			}
		}
		return activation;
	}

	/**
	 * A competence module x that is NOT executable spreads activation backward.
	 * It increases (by a fraction of its own activation level) the activation level of
	 * those predecessors y for which the shared proposition p E cx n ay is not true.
	 * Intuitively, a non-executable competence module spreads to the modules that
	 * `promise' to fulfill its preconditions that are not yet true, so that the competence
	 * module may become executable afterwards. Formally, given that competence
	 * module x = (cx ax dx zx) is not executable, it spreads backward through those
	 * predecessor links for which the proposition that defined them p E cx is false.
	 * @param indexBehavior
	 * @param executable
	 * @return
	 *
	 * Note: modified with weights.
	 */
	private double[] spreadsBackward(int indexBehavior, boolean executable){
		double[] activation = new double[modules.size()];
		if(!executable){
			Behavior beh = modules.get(indexBehavior);
			Collection<List<Premise>> condList = new Vector<> (beh.getPreconditions());
			for(List<Premise> condRow : condList ){
                for(Premise cond : condRow ){
                    if( !states.contains( cond.getLabel() ) ) { //p E cx is false
                        Vector<Behavior> sparseBehaviors = achieveProposition(cond.getLabel()); //j E cx n ay
                        for (int j = 0; j < sparseBehaviors.size(); j++) {
							int cardinalityAj = sparseBehaviors.size();
                            int cardinalityAy = sparseBehaviors.get(j).getAddList().size();
                            double temp = beh.getActivation() * (1d / cardinalityAj) * (1d / cardinalityAy);
                            activation[sparseBehaviors.get(j).getIdx()] += temp;
                            if( verbose ) {
                                System.out.println(beh.getName() + " spreads " + temp + " backward to " +
                                        sparseBehaviors.get(j).getName() + " for " + cond);
                            }
                        }

                    }
                }
			}
		}
		return activation;
	}

	/**
	 * Inhibition of Conflicters
	 * Every competence module x (executable or not) decreases (by a fraction of its
	 * own activation level) the activation level of those conflicters y for which the
	 * shared proposition p E cx n dy is true. Intuitively, a module tries to prevent a
	 * module that undoes its true preconditions from becoming active. Notice that
	 * we do not allow a module to inhibit itself (while it may activate itself). In
	 * case of mutual conflict of modules, only the one with the highest activation
	 * level inhibits the other. This prevents the phenomenon that the most relevant
	 * modules eliminate each other. Formally, competence module x = (cx ax dx zx)
	 * takes away activation energy through all of its conflicter links for which the
	 * proposition that defines them p E cx is true, except those links for which there
	 * exists an inverse conflicter link that is stronger.
	 *
	 * Note: modified with weights.
	 */
	private double[] takesAway(int indexBehavior){
		double[] activation = new double[modules.size()];
		Behavior behx = modules.get(indexBehavior);
        Collection<List<Premise>> condListX = new Vector<>(behx.getPreconditions());

		for( List<Premise> condRow : condListX ){
            for( Premise cond : condRow ) {
				Vector<Behavior> sparseBehaviors = undoPropositionState(cond.getLabel(), indexBehavior); //j E cx n dy
                for ( Behavior behy : sparseBehaviors) {
                    double temp = 0;
                    if ((behx.getActivation() <= behy.getActivation()) && inverseTakesAway(indexBehavior, behy.getIdx())) {
                        activation[behy.getIdx()] = 0;
                    } else {
                        int cardinalityUj = sparseBehaviors.size();
                        int cardinalityDy = behy.getDeleteList().size();
                        temp = behx.getActivation() * (delta / gamma) * (1d / cardinalityUj) * (1d / cardinalityDy);
                        activation[behy.getIdx()] += temp;//Math.max(activationTemp, sparseBehaviors.get(j).getActivationPrior());
                    }
                    if( verbose ) {
                        System.out.println(behx.getName() + " decreases (inhibits)" + behy.getName() + " with " + temp +
                                " for " + cond);
                    }
                }
            }
		}
		return activation;
	}

	/**
	 * Finds the intersection set S(t) n cy n dx
	 * @param indexBehx
	 * @param indexBehy
	 * @return
	 *
	 * Note: modified with weights.
	 */
	private boolean inverseTakesAway(int indexBehx, int indexBehy){
        Collection<List<Premise>> condsListy = new Vector<> (modules.get(indexBehy).getPreconditions());
		Behavior behx = modules.get(indexBehx);

		for(List<Premise> condRow : condsListy ){
            for( Premise cond : condRow ) {
                if (behx.getDeleteList().contains(cond.getLabel()) ) //cy n dx
                    if (states.contains( cond.getLabel() )) //cy n S(t)
                        return true;
            }
		}
		return false;
	}

	/**
	 * A decay function ensures that the overall activation level remains constant.
	 */
	private void applyDecayFx (){
		double sum = 0, factor, mayor = 0;
		int indexMayor = 0;

		for(int i = 0; i < modules.size(); i++){
			sum += modules.get(i).getActivation();
			if(modules.get(i).getActivation() > mayor){
				mayor = modules.get(i).getActivation();
				indexMayor = i;
			}
		}
		if(sum > pi * modules.size()){
			factor = pi * modules.size() / sum;
			for (Behavior module : modules) {
				module.decay(factor);
			}
		}
		//ojrl decay bahavior which increments its activation continously and is not activated
		if(modules.get(indexMayor).getActivation() > (theta * 1.5) && !modules.get(indexMayor).getActivated()){
			modules.get(indexMayor).decay(0.5);
			for (Behavior module : modules) {
				if (module.getExecutable() && module.getIdx() != modules.get(indexMayor).getIdx()
						&& !module.getActivated() && indexBehActivated != -1) {
					if (indexBehActivated != module.getIdx())
						module.decay(1.5);
					else
						module.decay(0.9);
				}
			}
		}

		for(int i = 0; i < modules.size(); i++){
            if( verbose ) {
//                System.out.println("activation-level " + modules.get(i).getName() + ": " + modules.get(i).getActivation());
            }
		}
	}

	/**
	 * The competence module that fulfills the following three conditions becomes
	 * active: (i) It has to be executable, (ii) Its level of activation has to surpass a
	 * certain threshold and (iii) It must have a higher activation level than all other
	 * competence modules that fulfill conditions (i) and (ii). When two competence
	 * modules fulfilll these conditions (i.e., they are equally strong), one of them is
	 * chosen randomly. The activation level of the module that has become active is
	 * reinitialized to 0 2. If none of the modules fulfills conditions (i) and (ii), the
	 * threshold is lowered by 10%.
	 *
	 * Note: modified with weights.
	 */
	private int activateBehavior (){
		double act = 0;
		indexBehActivated = -1;
		for( Behavior beh : modules ){
            if( beh.getActivation() >= theta && beh.getExecutable() && beh.getActivation() > act){
                indexBehActivated = modules.indexOf(beh);
                act = beh.getActivation();
                nameBehaviorActivated = beh.getId(); //beh.getName();
            }
        }
        execution = false;
		String values = "";
		matchesOutput = "";
		for( Behavior beh : modules ){
			String matches = "$ " + beh.getName() + ": " + beh.getStateMatches();
			int number = beh.getNumMatches();
			if( SocialReasonerController.verbose ) {
				System.err.println(matches.replace("$ ", "(" + number + "-" + beh.getActivation() + ") "));
			}
			matchesOutput += matches.replace("$ ", "(" + number + ") ") + ", ";
			values += "["+beh.getName() + ": " + beh.getActivation() + "], ";
		}
		if( SocialReasonerController.verbose ) {
			System.out.println(values + ", theta: " + theta);
		}
        if( indexBehActivated >= 0 ){
			statesOutput = Arrays.toString(states.toArray());
			if(SocialReasonerController.verbose ) {
				System.err.println("Executing Behavior: " + modules.get(indexBehActivated).getName());
				System.err.println("States: " + statesOutput );
			}
            execution = true;
            modules.get(indexBehActivated).setActivated(true);
            protectGoals(modules.get(indexBehActivated));
        }
		return indexBehActivated;
	}

	/**
	 *
	 * @param behIndex
	 */
	private synchronized void triggerPostconditions(int behIndex){
		if( behIndex >= 0 && behIndex < modules.size() ) {
			Behavior beh = modules.get(behIndex);
			Vector<String> addList = new Vector<>(beh.getAddList());
			Vector<String> deleteList = new Vector<>(beh.getDeleteList());

			try {
				previousStates = new Vector<>(states);
				//we do not need to add preconditions to state since it is done by DMMain
				//                for (String anAddList : addList) {
				//                    if (!anAddList.contains("_history")) {
				//                        states.remove(anAddList);
				//                        states.add(anAddList);
				//                    }
				//                }
				if (removePrecond) {
					if( beh.getPreconditions() != null ) {
						for (List<Premise> preconds : beh.getPreconditions()) {
							for (Premise precond : preconds) {
								states.remove(precond.getLabel());
							}
						}
					}
				} else {
					List<String> toRemove = new ArrayList<>();
					for (String premise : deleteList) {
						for (String st : states) {
							if (premise.contains("*") && Pattern.compile(premise.replace("*", "[a-zA-Z0-9]*"))
									.matcher(st).matches()) {
								toRemove.add(st);
							}
						}
						states.removeAll(toRemove);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (String goal : beh.getAddGoals()) {
				goals.remove(goal);
				goals.add(goal);
			}
		}
	}

	/**
	 * Protect the goals achieved
	 * @param beh behavior
	 * Note: modified with weights.
	 */
	private void protectGoals(Behavior beh){
		for (String goalTemp : beh.getAddList()) {
			if (goals.contains(goalTemp)) {
				goalsResolved.add(goalTemp);
				goals.remove(goalTemp);
			}
		}
	}

	/**
	 * updates the activation of each behavior
	 * Note: modified with weights.
	 */
	private void updateActivation(){
		//activation level of a competence module y at time t
		for(int i = 0; i < modules.size(); i++){
			for(int j = 0; j < modules.size(); j++){
				activationLinks[i] += activationSuccesors[j][i] + activationPredeccesors[j][i] - activationConflicters[j][i];
			}
			modules.get(i).updateActivation(activationInputs[i] + activationLinks[i]);
			activationInputs[i] = 0;
			activationLinks[i] = 0;
		}
	}

	/**
	 * Reset the activation of the behaviors
	 */
	private void reset(){
		//reseting
		if(execution)
			theta = initialTheta;
		for (Behavior module : modules) {
			module.resetActivation(module.getActivated());
		}
	}

	/**
	 * Note: modified with weights.
	 * @return
	 */
    public int selectBehavior() {
        if (activations == null) {
            setModules(modules, modules.size() + 1);
        }
        activations[modules.size()] = theta;
        if (verbose) {
            System.out.println("\nStep " + (cont + 1) + " goals achieved: " + goalsResolved.size() + " goals: " + goals.size());
        }
        computeActivation();

        computeLinks();

        updateActivation();

        activateBehavior();

        recordActivations();

        applyDecayFx();

        cont++;
        return indexBehActivated;
    }

	/**
	 * Note: modified with weights.
	 */
	private void recordActivations(){
        for (int i = 0; i < modules.size(); i++) {
            Behavior b = modules.get(i);
            if (verbose) {
                System.out.println("Behavior: " + b.getName() + "  activation: " + b.getActivation());
            }
            activations[i] = modules.get(i).getActivation();
        }
        if (indexBehActivated != -1) {
            activations[modules.size()] = modules.get(indexBehActivated).getActivation();
        }
    }

    public void execute(int cycle){
        if( cycle > 0) {
            triggerPostconditions(indexBehActivated);
        }
        if( !execution ){
            theta *= 0.9;
            if( verbose ) {
                System.err.println("None of the executable modules has accumulated enough activation to become active");
                System.out.println("Theta: " + theta);
            }
        }

        if( cycle > 0 ) {
            reset();
        }
    }

    public int getHighestActivation() {
        int idx = 5; //ASN
        double max = 0;
        for(int i = 0; i< modules.size(); i++){
            Behavior beh = modules.get(i);
            if( beh.getActivation() > theta && beh.getActivation() > max ){
                idx = i;
            }
        }
        modules.get(idx).setActivation(max * 1.15);
        nameBehaviorActivated = modules.get(idx).getId(); //.getName();
        return indexBehActivated = idx;
    }

	public int getHighestActivationUsingNone(){
		int idx = 7, maxPrecTrue = 0;
		double max = 0;
		for(int i = 0; i< modules.size(); i++){
			Behavior beh = modules.get(i);
			if( beh.getName().equals("NONE")){
				idx = i;
			}
			if( beh.getActivation() > theta && beh.getActivation() > max ){
				max = beh.getActivation();
			}
			if( beh.getNumMatches() > maxPrecTrue ){
				maxPrecTrue = beh.getNumMatches();
			}
		}
        if( max == 0){
            max = theta;
        }
        indexBehActivated = idx;
        nameBehaviorActivated = modules.get(idx).getId(); //getName();
        modules.get(idx).setActivation(max * 1.30);
		modules.get(idx).setNumMatches(maxPrecTrue);
        modules.get(idx).setActivated(true);
        execution = true;
        recordActivations();
		return idx;
	}

    public String[] getModuleNamesByHighestActivation() {
        getModulesCopy();
        Collections.sort( modulesCopy );
        Collections.reverse( modulesCopy );
        String[] results = new String[modulesCopy.size()];
		DecimalFormat formatter = new DecimalFormat("#0.00");
		output = "";
        for( int i = 0; i < modulesCopy.size(); i++ ){
			Behavior bp = modulesCopy.get(i);
            results[i] = bp.getId();
			output += "["+bp.getName()+": ("+formatter.format( bp.getActivation())+") - ("+bp.getNumMatches()+")], ";
        }
		output += "theta: " + theta;
        return results;
    }

    private void getModulesCopy() {
        modulesCopy.clear();
        for( Behavior beh : modules ){
            modulesCopy.add( beh.clone() );
        }
    }

	public String[] getModuleNames() {
		String[] names = new String[ modules.size() ];
		for(int i = 0; i < names.length; i++){
			names[i] = modules.get(i).getName();
		}
		return names;
	}

	public double[] getOnlyActivations() {
		int size = modules.size();
		double[] actvs = new double[ size ];
		for( int i = 0; i < size; i++){
			actvs[i] = activations[i];
		}
		return actvs;
	}

	public void resetAll() {
		for( Behavior beh : modules ){
			beh.reset();
		}
		theta = initialTheta;
	}
}
