package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.Config;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;

/**
 * Expression: A statement that returns a value
 *
 * @author pcingola
 */
public class Expression extends Statement {

	public Expression(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	/**
	 * Run an expression: I.e. evaluate the expression
	 */
	@Override
	protected void runStep(BigDataScriptThread bdsThread) {
		try {
			run(bdsThread);
			bdsThread.pop();
		} catch (Throwable t) {
			if (Config.get().isDebug()) t.printStackTrace();
			bdsThread.fatalError(this, t);
		}
	}
}
