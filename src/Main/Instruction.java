package Main;

public class Instruction {
	int id;
	int instValue ;
	byte op;
	byte r1;
	byte r2;
	byte r3;
	int r1Val;
	int r2Val;
	int r3Val;
	int aluRes;
	int imm;
    short shamt;
    int wb;
	int address;
	int status;
	
	public Instruction(int l) {
		instValue=l;
	}
}
