package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;

/**
 * for( ForInit ; ForCondition ; ForEnd ) Statements
 *
 * @author pcingola
 */
public class ForEnd extends ExpressionList {

	public ForEnd(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	public void runStep(BigDataScriptThread bdsThread) {
		for (Expression expr : expressions) {
			expr.run(bdsThread);
			bdsThread.pop(); // Remove from stack, nobody is reading the results
		}
	}

}
