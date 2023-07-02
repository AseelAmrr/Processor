package Main;

public class Memory {
	
	private int [] mem ;
	int pointer;
	
	
	public Memory() {
		setMem(new int [2048]);
		pointer = 0;
	}
	
	public int read(int address)
	{
		return getMem()[address];
	}
	
	public void write(int address, int data)
	{
		getMem()[address]= data;
	}

	public int [] getMem() {
		return mem;
	}

	public void setMem(int[] mem) {
		this.mem = mem;
	}
	
	
}
