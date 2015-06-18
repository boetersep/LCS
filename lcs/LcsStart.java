package lcs;


public class LcsStart {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		args = new String[] {"config.txt"};
		
		LcsEngine e = new LcsEngine(args[0]);
		new Thread(e).start();
	}

}
