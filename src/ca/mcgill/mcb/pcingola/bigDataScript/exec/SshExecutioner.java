package ca.mcgill.mcb.pcingola.bigDataScript.exec;

import ca.mcgill.mcb.pcingola.bigDataScript.cluster.Cluster;
import ca.mcgill.mcb.pcingola.bigDataScript.util.Gpr;

/**
 * Execute tasks in a remote computer, using ssh
 * 
 * It is implemented by running a local queue (OsCmdQueue)
 * 
 * @author pcingola
 */
public class SshExecutioner extends LocalQueueExecutioner {

	Cluster cluster;

	public SshExecutioner(Cluster cluster) {
		super(null);
		this.cluster = cluster;
	}

	@Override
	public synchronized void kill() {
		cluster.stopHostInfoUpdaters();
		super.kill();
	}

	@Override
	public void run() {
		Gpr.debug("SshExecutioner: Starting.");
		cluster.startHostInfoUpdaters();
		super.run();
		cluster.stopHostInfoUpdaters();
	}

}
