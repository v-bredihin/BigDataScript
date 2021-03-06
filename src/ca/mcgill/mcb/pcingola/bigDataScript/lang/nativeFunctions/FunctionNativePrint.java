package ca.mcgill.mcb.pcingola.bigDataScript.lang.nativeFunctions;

import ca.mcgill.mcb.pcingola.bigDataScript.lang.Parameters;
import ca.mcgill.mcb.pcingola.bigDataScript.lang.Type;
import ca.mcgill.mcb.pcingola.bigDataScript.lang.TypeList;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;

/**
 * Native function "print"
 * 
 * @author pcingola
 */
public class FunctionNativePrint extends FunctionNative {

	public FunctionNativePrint() {
		super();
	}

	@Override
	protected void initFunction() {
		functionName = "print";
		returnType = TypeList.get(Type.STRING);

		String argNames[] = { "str" };
		Type argTypes[] = { Type.STRING };
		parameters = Parameters.get(argTypes, argNames);
		addNativeFunctionToScope();
	}

	@Override
	protected Object runFunctionNative(BigDataScriptThread csThread) {
		String str = csThread.getString("str");
		System.out.print(str);
		return str;
	}

}
