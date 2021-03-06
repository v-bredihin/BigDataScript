package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessage.MessageType;
import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessages;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;
import ca.mcgill.mcb.pcingola.bigDataScript.run.RunState;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.Scope;

/**
 * for( ForInit ; ForCondition ; ForEnd ) Statements
 *
 * @author pcingola
 */
public class ForLoop extends StatementWithScope {

	// Note:	It is important that 'begin' node is type-checked before the others in order to
	//			add variables to the scope before ForCondition, ForEnd or Statement uses them.
	//			So the field name should be alphabetically sorted before the other (that's why
	//			I call it 'begin' and not 'init').
	//			Yes, it's a horrible hack.
	ForInit begin;
	ForCondition condition;
	ForEnd end;
	Statement statement;

	public ForLoop(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	protected void parse(ParseTree tree) {
		int idx = 0;

		if (isTerminal(tree, idx, "for")) idx++; // 'for'
		if (isTerminal(tree, idx, "(")) idx++; // '('
		if (!isTerminal(tree, idx, ";")) begin = (ForInit) factory(tree, idx++); // Is this a 'for:begin'? (could be empty)
		if (isTerminal(tree, idx, ";")) idx++; // ';'
		if (!isTerminal(tree, idx, ";")) condition = (ForCondition) factory(tree, idx++); // Is this a 'for:condition'? (could be empty)
		if (isTerminal(tree, idx, ";")) idx++; // ';'
		if (!isTerminal(tree, idx, ")")) end = (ForEnd) factory(tree, idx++); // Is this a 'for:end'? (could be empty)
		if (isTerminal(tree, idx, ")")) idx++; // ')'

		statement = (Statement) factory(tree, idx++);
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
	 * Run
	 */
	@Override
	protected void runStep(BigDataScriptThread bdsThread) {
		// Loop initialization
		if (begin != null) begin.run(bdsThread);

		while (runCondition(bdsThread)) { // Loop condition
			statement.run(bdsThread); // Loop statement

			switch (bdsThread.getRunState()) {
			case OK:
			case CHECKPOINT_RECOVER:
				break;

			case BREAK: // Break from loop
				bdsThread.setRunState(RunState.OK);
				return;

			case CONTINUE: // Continue: Nothing to do, just continue with the next iteration
				bdsThread.setRunState(RunState.OK);
				break;

			case FATAL_ERROR:
			case RETURN: // Return
			case EXIT: // Exit program
				return;

			default:
				throw new RuntimeException("Unhandled RunState: " + bdsThread.getRunState());
			}

			if (end != null) end.run(bdsThread); // End of loop
		}
	}

	@Override
	public void typeCheck(Scope scope, CompilerMessages compilerMessages) {
		super.typeCheck(scope, compilerMessages);
		if (statement == null) compilerMessages.add(this, "Empty for statement", MessageType.ERROR);
	}
}
