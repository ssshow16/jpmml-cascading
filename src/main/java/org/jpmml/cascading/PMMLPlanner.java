/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.cascading;

import java.util.*;

import cascading.flow.*;
import cascading.flow.planner.*;
import cascading.pipe.*;
import cascading.tuple.*;

import org.jpmml.evaluator.*;

public class PMMLPlanner implements AssemblyPlanner {

	private Evaluator evaluator = null;

	private String branchName = "pmml";

	private String headName = null;

	private String tailName = null;


	public PMMLPlanner(Evaluator evaluator){
		setEvaluator(evaluator);
	}

	@Override
	public List<Pipe> resolveTails(Context context){
		List<Pipe> tails = context.getTails();

		Pipe tail;

		String headName = getHeadName();

		if(tails.size() == 0){

			if(headName == null){
				headName = findHeadName(context);
			}

			tail = new Pipe(headName);
		} else

		if(tails.size() == 1){

			if(headName != null){
				throw new PlannerException("Cannot specify a head name when there is an incoming branch");
			}

			tail = tails.get(0);
		} else

		{
			throw new PlannerException("Too many incoming branches to choose from");
		}

		String tailName = getTailName();
		if(tailName == null){
			tailName = findTailName(context);
		}

		tail = new Pipe(tailName, resolveAssembly(tail));

		return Collections.singletonList(tail);
	}

	private Pipe resolveAssembly(Pipe pipe){
		Pipe tail;

		if(pipe == null){
			tail = new Pipe(getBranchName());
		} else

		{
			tail = new Pipe(getBranchName(), pipe);
		}

		Evaluator evaluator = getEvaluator();

		Fields argumentFields = FieldsUtil.getActiveFields(evaluator);
		Fields outputFields = (FieldsUtil.getPredictedFields(evaluator)).append(FieldsUtil.getOutputFields(evaluator));

		PMMLFunction function = new PMMLFunction(outputFields, evaluator);

		tail = new Each(tail, argumentFields, function, outputFields);

		return tail;
	}

	private String findHeadName(Context context){
		Flow<?> flow = context.getFlow();

		List<String> sourceNames = flow.getSourceNames();
		if(sourceNames.size() != 1){
			throw new PlannerException("Too many sources to choose from: " + sourceNames);
		}

		return sourceNames.get(0);
	}

	private String findTailName(Context context){
		Flow<?> flow = context.getFlow();

		List<String> sinkNames = flow.getSinkNames();
		if(sinkNames.size() != 1){
			throw new PlannerException("Too many sinks to choose from: " + sinkNames);
		}

		return sinkNames.get(0);
	}

	public Evaluator getEvaluator(){
		return this.evaluator;
	}

	private void setEvaluator(Evaluator evaluator){

		if(evaluator == null){
			throw new NullPointerException();
		}

		this.evaluator = evaluator;
	}

	public String getBranchName(){
		return this.branchName;
	}

	public void setBranchName(String branchName){

		if(branchName == null){
			throw new NullPointerException();
		}

		this.branchName = branchName;
	}

	public String getHeadName(){
		return this.headName;
	}

	/**
	 * Sets the name of the incoming source.
	 *
	 * This attribute is mandatory when more than one sources have been declared.
	 */
	public void setHeadName(String headName){
		this.headName = headName;
	}

	public String getTailName(){
		return this.tailName;
	}

	/**
	 * Sets the name of the outgoing sink.
	 *
	 * This attribute is mandatory when more than one sinks have been declared.
	 */
	public void setTailName(String tailName){
		this.tailName = tailName;
	}
}