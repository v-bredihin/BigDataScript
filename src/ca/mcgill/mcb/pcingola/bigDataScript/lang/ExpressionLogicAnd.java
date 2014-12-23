package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;

/**
 * Boolean AND
 *
 * @author pcingola
 */
public class ExpressionLogicAnd extends ExpressionLogic {

	public ExpressionLogicAnd(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	/**
	 * Evaluate an expression
	 */
	@Override
	public void eval(BigDataScriptThread bdsThread) {
		left.eval(bdsThread);
		if (!((Boolean) bdsThread.peek())) return; // Already false? No need to evaluate the other expression
		right.eval(bdsThread); // 'AND' only depends on this result (left was true)
	}

	@Override
	protected String op() {
		return "&&";
	}

}