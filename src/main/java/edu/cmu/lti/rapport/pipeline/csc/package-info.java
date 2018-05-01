/**
 * a replacement for the MultiCSClassifier class/mess
 * 
 * enum ConversationalStrategy contains the known strategies.
 *   there seem to be two kinds: 
 *     toplevel strategies (which have their background classification via Recipes) and
 *     derived strategies (which base their results on other classifiers).
 *     The two derived strategies are ASN (which is 1-VSN) and QSD (which takes precedence over SD in case of a question).
 * 
 * ConversationalStrategyDistribution contains scores for all strategies (not actually a distribution as it need not sum to 1)
 * 
 * ConversationalStrategyClassifier is the definition of what classifiers in this package should be capable of doing:
 *   you can update the most recent features at any time
 *   you then query for the strategy distribution
 *   
 * There is at present just one implementation for ConversationalStrategyClassifier, which is the MultiClassifier.
 * 
 * MultiClassifier uses a scorer for each individual strategy. 
 *   The derived strategies are computed after the toplevel strategies, 
 *   because they need to have access to the toplevel scores for their own computation.
 * 
 * StrategyScorer is the interface for scoring an utterance, there are two implementations:
 *   SingleClassScorer computes scores for toplevel strategies. It uses OnlineParser to follow a Recipe
 *   DerivedClassScorer deals with the two derived strategies. This of course should be architectured differently, but oh well.
 *   
 */
package edu.cmu.lti.rapport.pipeline.csc;