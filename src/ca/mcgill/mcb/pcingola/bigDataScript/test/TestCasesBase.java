package ca.mcgill.mcb.pcingola.bigDataScript.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.bigDataScript.BigDataScript;
import ca.mcgill.mcb.pcingola.bigDataScript.osCmd.TeeOutputStream;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.ScopeSymbol;
import ca.mcgill.mcb.pcingola.bigDataScript.util.Gpr;

/**
 * All methods shared amongst test cases
 *
 * @author pcingola
 */
public class TestCasesBase extends TestCase {

	public boolean debug = false;
	public boolean verbose = false;

	/**
	 * Compile a bds script
	 */
	BigDataScript bds(String fileName) {
		String args[] = { fileName };
		String argsv[] = { "-v", fileName };

		BigDataScript bigDataScript = new BigDataScript(verbose ? argsv : args);
		bigDataScript.setStackCheck(true);
		return bigDataScript;
	}

	BigDataScript bds(String[] args) {
		ArrayList<String> l = new ArrayList<String>();

		if (verbose) l.add("-v");
		for (String arg : args)
			l.add(arg);

		args = l.toArray(new String[0]);

		BigDataScript bigDataScript = new BigDataScript(args);
		bigDataScript.setStackCheck(true);
		return bigDataScript;
	}

	/**
	 * Check that a file compiles with expected errors
	 */
	void compileErrors(String fileName, String expectedErrors) {
		BigDataScript bigDataScript = bds(fileName);
		bigDataScript.compile();
		if (bigDataScript.getCompilerMessages().isEmpty()) fail("Expecting compilation errors in file '" + fileName + "', but none found!\n");
		Assert.assertEquals(expectedErrors.trim(), bigDataScript.getCompilerMessages().toString().trim());
	}

	/**
	 * Check that a file compiles without any errors
	 */
	void compileOk(String fileName) {
		BigDataScript bigDataScript = bds(fileName);
		bigDataScript.compile();
		if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());
	}

	/**
	 * Compile a file
	 */
	BigDataScript compileTest(String fileName) {
		BigDataScript bigDataScript = bds(fileName);
		bigDataScript.compile();
		if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());
		return bigDataScript;
	}

	/**
	 * Check that a file compiles without any errors, runs and a variable have its expected value
	 */
	void runAndCheck(String fileName, String varname, Object expectedValue) {
		String args[] = { fileName };
		runAndCheck(fileName, args, varname, expectedValue);
	}

	/**
	 * Check that a file compiles without any errors, runs and a variable have its expected value
	 */
	void runAndCheck(String fileName, String[] args, String varname, Object expectedValue) {
		// Compile, check for errors
		BigDataScript bigDataScript = bds(args);
		bigDataScript.run();
		if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());

		ScopeSymbol ssym = bigDataScript.getProgramUnit().getRunScope().getSymbol(varname);

		if (debug) Gpr.debug("Program: " + fileName + "\t" + ssym);
		Assert.assertTrue("Missing variable '" + varname + "'", ssym != null);
		Assert.assertEquals(expectedValue.toString(), ssym == null ? "" : ssym.getValue().toString());
	}

	void runAndCheckExit(String fileName, int expectedExitValue) {
		// Run
		BigDataScript bigDataScript = bds(fileName);
		int exitValue = bigDataScript.run();
		if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());
		Assert.assertEquals(expectedExitValue, exitValue);
	}

	void runAndCheckMultiple(String fileName, HashMap<String, Object> expectedValues) {
		runAndCheckMultiple(fileName, expectedValues, new ArrayList<String>());
	}

	/**
	 * Check that a file compiles without any errors, runs and all variables have their expected values
	 */
	void runAndCheckMultiple(String fileName, HashMap<String, Object> expectedValues, ArrayList<String> args) {
		// Prepare command line arguments
		args.add(0, fileName);
		if (verbose) args.add(0, "-v");
		String argsArray[] = args.toArray(new String[0]);

		// Compile & Run
		BigDataScript bigDataScript = new BigDataScript(argsArray);
		bigDataScript = new BigDataScript(argsArray);
		bigDataScript.setStackCheck(true);
		bigDataScript.run();
		if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());

		// Check all values
		for (String varName : expectedValues.keySet()) {
			Object expectedValue = expectedValues.get(varName);

			ScopeSymbol ssym = bigDataScript.getProgramUnit().getRunScope().getSymbol(varName);
			Assert.assertTrue("Missing variable '" + varName + "'", ssym != null);

			if (debug) Gpr.debug("Program: " + fileName + "\tvarName: '" + varName + "'\tssym: " + ssym);
			if (!expectedValue.toString().equals(ssym.getValue().toString())) throw new RuntimeException("Variable '" + varName + "' does not match:\n"//
					+ "\tExpected : '" + expectedValue.toString() + "'" //
					+ "\tActual   : '" + ssym.getValue().toString() + "'" //
					);
		}
	}

	/**
	 * Check that a file recovers from a checkpoint and runs without errors
	 */
	BigDataScript runAndCheckpoint(String fileName, String checkpointFileName, String varname, Object expectedValue) {
		return runAndCheckpoint(fileName, checkpointFileName, varname, expectedValue, null);
	}

	/**
	 * Check that a file recovers from a checkpoint and runs without errors
	 */
	BigDataScript runAndCheckpoint(String fileName, String checkpointFileName, String varname, Object expectedValue, Runnable runBeforeRecover) {
		// Compile
		BigDataScript bigDataScript = bds(fileName);
		bigDataScript.run();
		if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());

		// Run something before checkpoint recovery?
		if (runBeforeRecover != null) runBeforeRecover.run();
		else {
			// Check that values match
			ScopeSymbol ssym = bigDataScript.getProgramUnit().getRunScope().getSymbol(varname);
			Assert.assertTrue("Missing variable '" + varname + "'", ssym != null);

			if (debug) Gpr.debug("Program: " + fileName + "\t" + ssym);
			Assert.assertEquals(expectedValue, ssym.getValue().toString());
		}

		//---
		// Recover from checkpoint
		//---
		String chpFileName = checkpointFileName;
		if (checkpointFileName == null) chpFileName = fileName + ".chp";
		if (debug) Gpr.debug("CheckPoint file name : " + chpFileName);
		String args2[] = { "-r", chpFileName };
		String args2v[] = { "-v", "-r", chpFileName };
		BigDataScript bigDataScript2 = new BigDataScript(verbose ? args2v : args2);
		bigDataScript2.setStackCheck(true);
		bigDataScript2.run();

		// Check that values match
		ScopeSymbol ssym = bigDataScript2.getProgramUnit().getRunScope().getSymbol(varname);
		Assert.assertTrue("Missing variable '" + varname + "'", ssym != null);

		if (debug) Gpr.debug("Program: " + fileName + "\t" + ssym);
		Assert.assertEquals(expectedValue, ssym.getValue().toString());

		return bigDataScript2;
	}

	/**
	 * Check that StdErr has a string
	 */
	void runAndCheckStderr(String fileName, String expectedStderr) {
		// Compile
		BigDataScript bigDataScript = bds(fileName);

		// Capture STDERR
		PrintStream stderr = System.err;
		ByteArrayOutputStream captureStderr = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(captureStderr));

			// Run
			bigDataScript.run();
			if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// Restore STDERR
			System.setErr(stderr);
			System.err.println(captureStderr); // Show stderr
		}

		// Check that the expected string is in STDERR
		if (debug) Gpr.debug("Program's stderr: '" + captureStderr + "'");
		int index = captureStderr.toString().indexOf(expectedStderr);
		if (index < 0) throw new RuntimeException("Error: Expeted string '" + expectedStderr + "' in STDERR not found.\nSTDERR:\n" + captureStderr + "\n");
	}

	/**
	 * Check that StdOut has a string
	 */
	String runAndCheckStdout(String fileName, String expectedStdout) {
		return runAndCheckStdout(fileName, expectedStdout, false);
	}

	/**
	 * Check that StdOut has a string (or that the string is not present if 'reverse')
	 */
	String runAndCheckStdout(String fileName, String expectedStdout, boolean reverse) {
		// Run command and capture stdout
		String captureStdout = runAndReturnStdout(fileName);

		// Check that the expected string is in STDERR
		if (debug) Gpr.debug("Program's stdout: '" + captureStdout + "'");
		int index = captureStdout.toString().indexOf(expectedStdout);
		if (!reverse && index < 0) throw new RuntimeException("Error: Expeted string '" + expectedStdout + "' in STDOUT not found.\nSTDOUT:\n" + captureStdout + "\n");
		if (reverse && index >= 0) throw new RuntimeException("Error: Expeted string '" + expectedStdout + "' in absent from STDOUT, but it was found.\nSTDOUT:\n" + captureStdout + "\n");
		return captureStdout.toString();
	}

	/**
	 * Run a bds program and capture stdout (while still showing it)
	 */
	String runAndReturnStdout(String fileName) {
		BigDataScript bigDataScript = bds(fileName);

		// Capture STDOUT
		PrintStream stdout = System.out; // Store original stdout
		ByteArrayOutputStream captureStdout = new ByteArrayOutputStream();
		TeeOutputStream tee = new TeeOutputStream(stdout, captureStdout);
		try {
			System.setOut(new PrintStream(tee));

			// Run
			bigDataScript.run();
			if (!bigDataScript.getCompilerMessages().isEmpty()) fail("Compile errors in file '" + fileName + "':\n" + bigDataScript.getCompilerMessages());
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// Restore STDERR
			System.setOut(stdout);
		}

		return captureStdout.toString();
	}
}
