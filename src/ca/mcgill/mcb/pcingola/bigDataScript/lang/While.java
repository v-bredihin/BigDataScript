package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessage.MessageType;
import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessages;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;
import ca.mcgill.mcb.pcingola.bigDataScript.run.RunState;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.Scope;

/**
 * While statement
 *
 * @author pcingola
 */
public class While extends Statement {

	Expression condition;
	Statement statement;

	public While(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	protected void parse(ParseTree tree) {
		int idx = 0;
		if (isTerminal(tree, idx, "while")) idx++; // 'while'
		if (isTerminal(tree, idx, "(")) idx++; // '('
		if (!isTerminal(tree, idx, ")")) condition = (Expression) factory(tree, idx++); // Is this a 'while:condition'? (could be empty)
		if (isTerminal(tree, idx, ")")) idx++; // ')'
		statement = (Statement) factory(tree, idx);
	}

	/**
	 * Evaluate condition
	 */
	boolean runCondition(BigDataScriptThread bdsThread) {
		if (condition == null) return true;

		condition.run(bdsThread);

		// If we are recovering from a checkpoint, we have to get
		// into the loop's statements to find the node where the
		// program created the checkpoint
		if (bdsThread.isCheckpointRecover()) return true;

		// Return value form 'condition'
		return popBool(bdsThread);
	}

	/**
	 * Run the program
	 */
	@Override
	protected void runStep(BigDataScriptThread bdsThread) {
		while (runCondition(bdsThread)) { // Loop condition
			statement.run(bdsThread);

			switch (bdsThread.getRunState()) {
			case OK: // OK continue
			case CHECKPOINT_RECOVER:
				break;

			case BREAK: // Break from loop
				bdsThread.setRunState(RunState.OK);
				return;

			case CONTINUE: // Continue: Nothing to do, just continue with the next iteration
				bdsThread.setRunState(RunState.OK);
				break;

			case FATAL_ERROR:
			case RETURN:
			case EXIT:
				return;

			default:
				throw new RuntimeException("Unhandled RunState: " + bdsThread.getRunState());
			}
		}
	}

	@Override
	protected void typeCheck(Scope scope, CompilerMessages compilerMessages) {
		if (condition != null) condition.returnType(scope);
		if ((condition != null) && !condition.isBool()) compilerMessages.add(this, "While loop condition must be a bool expression", MessageType.ERROR);
	}

}
