package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessage.MessageType;
import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessages;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.Scope;

/**
 * An "print" statement
 *
 * @author pcingola
 */
public class Print extends Exit {

	public Print(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;

		// Calculate expression's return type
		if (expr != null) expr.returnType(scope);

		// Program's return type is 'int' (exit code)
		returnType = Type.STRING;

		return returnType;
	}

	/**
	 * Run the program
	 */
	@Override
	protected void runStep(BigDataScriptThread bdsThread) {
		String msg = "";
		if (expr != null) {
			// Evaluate expression to show
			expr.run(bdsThread);
			msg = popString(bdsThread);
		}

		if (!msg.isEmpty()) System.out.print(msg);
	}

	@Override
	public String toString() {
		return "print " + expr + "\n";
	}

	@Override
	protected void typeCheck(Scope scope, CompilerMessages compilerMessages) {
		returnType(scope);
		if ((expr.getReturnType() != null) && (!expr.getReturnType().canCast(Type.STRING))) compilerMessages.add(this, "Cannot cast " + expr.getReturnType() + " to " + Type.STRING, MessageType.ERROR);
	}

}
