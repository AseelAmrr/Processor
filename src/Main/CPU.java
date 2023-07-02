package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CPU {
	int PC = 0;
	static int[] registerFile;
	Memory memory;
	ArrayList<Instruction> instructions = new ArrayList<Instruction>();
	int instCount;
	int flagJump =0;

	public CPU() {
		registerFile = new int[32];
		memory = new Memory();
		encode();
	}

	private void encode() {

		File file = new File("src/CA.txt");
		BufferedReader br;
		String s = "";
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			String[] words;

			while ((line = br.readLine()) != null) {
				instCount++;
				words = line.split(" "); // Split the word using space

				if (words[0].equals("ADD")) {
					s = "0000" + regR(words[1]) + regR(words[2]) + regR(words[3]) + "0000000000000";
				} else if (words[0].equals("SUB")) {
					s = "0001" + regR(words[1]) + regR(words[2]) + regR(words[3]) + "0000000000000";
				} else if (words[0].equals("MUL")) {
					s = "0010" + regR(words[1]) + regR(words[2]) + regR(words[3]) + "0000000000000";
				} else if (words[0].equals("MOVI")) {
					s = "0011" + regR(words[1]) + "00000" +  immToBin(words[2]);
				} else if (words[0].equals("JEQ")) {
					s = "0100" + regR(words[1]) + regR(words[2]) + immToBin(words[3]);
				} else if (words[0].equals("AND")) {
					s = "0101" + regR(words[1]) + regR(words[2]) + regR(words[3]) + "0000000000000";
				} else if (words[0].equals("XORI")) {
					s = "0110" + regR(words[1]) + regR(words[2]) + immToBin(words[3]);
				} else if (words[0].equals("JMP")) {
					s = "0111" + addToBin(words[1]); // unsigned
				} else if (words[0].equals("LSL")) {
					s = "1000" + regR(words[1]) + regR(words[2]) + "00000" + shamtToBin(words[3]);
				} else if (words[0].equals("LSR")) {
					s = "1001" + regR(words[1]) + regR(words[2]) + "00000" + shamtToBin(words[3]);
				} else if (words[0].equals("MOVR")) {
					s = "1010" + regR(words[1]) + regR(words[2]) + immToBin(words[3]);
				} else if (words[0].equals("MOVM")) {
					s = "1011" + regR(words[1]) + regR(words[2]) + immToBin(words[3]);
				}
				long temp = Long.parseLong(s,2);
				int temp2 = (int) temp;
				this.memory.write(memory.pointer, temp2);
				this.memory.pointer++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String regR(String reg) {
		reg = reg.substring(1);
		int intVal = Integer.parseInt(reg);
		String bin = Integer.toBinaryString(intVal);
		return extend(bin, 5);

	}

	public static String immToBin(String num) // just use
	{
		String extended = null;
		int intVal = Integer.parseInt(num);
		int posNum = Math.abs(intVal);
		if(intVal>=0) {
			
			String temp = Integer.toBinaryString(posNum);
			String temp2 =extend(temp, 18);
			System.out.println("temp2 "+ temp2);
			extended =twosComplement(temp2);
		}
		else 
		{
			String bin = Integer.toBinaryString(posNum);
			extended = extend(bin, 18);
//			if (extended.charAt(0) == '1') {
//				return removeExtra(extended, 18);
//			}
			
		}
		return extended;
	}

	private static String twosComplement(String bin) {
		boolean firstOne = false;
		String twos="";
		
		for(int i =bin.length()-1 ; i>=0;i--) {
			if (firstOne)
			{
				if (bin.charAt(i)=='0')
					twos = '1' + twos;
				else 
					twos = '0' + twos;
			}
			else
			{
				twos =bin.charAt(i) + twos;
			}
			if (bin.charAt(i)=='1' && !firstOne)
				firstOne= true;
		}
		System.out.println("twos "+twos);
		return twos;
		
		}


	public static String shamtToBin(String num) // just use
	{
		int intVal = Integer.parseInt(num);
		String bin = Integer.toBinaryString(intVal);
		return extend(bin, 13);
	}

	public static String addToBin(String num) {
		int intVal = Integer.parseInt(num);
		String bin = Integer.toBinaryString(intVal);
		return extend(bin, 28);
	}

	public static String extend(String bin, int count) {
		for (int i = 0; bin.length() < count; i++) {
			bin = "0" + bin;
		}
		return bin;
	}

	public static String signExtend(String bin, int count) {
		char sign = bin.charAt(0);
		for (int i = 0; bin.length() < count; i++) {
			bin = sign + bin;
		}
		return bin;
	}

	private Instruction fetch() {
		System.out.println("The input of fetch Stage: PC = " + PC);
		if (memory.read(PC) != 0) {
			Instruction inst = new Instruction(memory.read(PC));
			inst.id = PC + 1;
			PC++;
			inst.status = 2;
			System.out.println("We Fetched: Instruction " + inst.id);
			System.out.println("The output of fetch Stage: Instruction = " + inst.instValue);
			return inst;
		}
		return null;
	}

	private void decode(Instruction inst) {
		System.out.println("The input of decode Stage: Instruction ID = " + inst.id + " ");
		System.out.println("The input of decode Stage: Instruction Value = " + inst.instValue + " ");
		int opTemp = inst.instValue & 0b11110000000000000000000000000000;
		inst.op = (byte) (opTemp >>> 28);
		byte op = inst.op;
		int val = inst.instValue;

		if (op == 0 || op == 1 || op == 2 || op == 5 || op == 8 || op == 9) // R-Type
		{
			int rsTemp = (val & 0b00001111100000000000000000000000);
			inst.r1 = (byte) (rsTemp >>> 23);

			int rtTemp = val & 0b00000000011111000000000000000000;
			inst.r2 = (byte) (rtTemp >>> 18);

			int rdTemp = val & 0b00000000000000111110000000000000;
			inst.r3 = (byte) (rdTemp >>> 13);

			inst.shamt = (short) (val & 0b00000000000000000001111111111111);

			inst.r2Val = registerFile[inst.r2];
			inst.r3Val = registerFile[inst.r3];

		}

		else if (op == 3 || op == 4 || op == 6 || op == 10 || op == 11) { // I_Type
			int rsTemp = (val & 0b00001111100000000000000000000000);
			inst.r1 = (byte) (rsTemp >>> 23);

			int rtTemp = val & 0b00000000011111000000000000000000;
			inst.r2 = (byte) (rtTemp >>> 18);

			String imm = Integer.toBinaryString( val & 0b00000000000000111111111111111111);
			int immidiate ;
			int intImm = val & 0b00000000000000111111111111111111;
			if(Integer.toBinaryString( intImm ).equals("1000000000000000000"))
				immidiate= -131072;
			else if(Integer.toBinaryString( intImm ).equals("100000000000000001"))
				immidiate= 131071;
			
			else if (Integer.toBinaryString( val ).charAt(17)=='0')
			{
				immidiate = val & 0b00000000000000111111111111111111;
				immidiate = immidiate * -1;
			}
			else
			{
				imm = twosComplement(imm);
				immidiate = Integer.parseInt(imm, 2);
				
			}

			inst.imm = immidiate;
			inst.r2Val = registerFile[inst.r2];
			inst.r1Val = registerFile[inst.r1];
		} else if (op == 7) {// J-Type
			inst.address = val & 0b00001111111111111111111111111111;

		}
		inst.status++;
		System.out.println("We decoded: Instruction: " + inst.id);
	}

	public void execute(Instruction inst) {		
		System.out.println("The input of execute Stage: Instruction = " + inst.id);
		System.out.println("The input of execute Stage: Instruction Value = " + inst.instValue);
		System.out.println("The input of execute Stage: Instruction Opcode = " + inst.op);
		switch (inst.op) {
		// add
		case 0:
			inst.aluRes = inst.r2Val + inst.r3Val;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  R3 = " + inst.r3Val);
			System.out.println("The input of execute Stage:  shamt = " + inst.shamt);
			break;
//			inst.aluRes = registerFile[inst.r2] + registerFile[inst.r3];break;
		// sub
		case 1:
			inst.aluRes = inst.r2Val - inst.r3Val;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  R3 = " + inst.r3Val);
			System.out.println("The input of execute Stage:  shamt = " + inst.shamt);
			break;
		// mul
		case 2:
			inst.aluRes = inst.r2Val * inst.r3Val;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  R3 = " + inst.r3Val);
			System.out.println("The input of execute Stage:  shamt = " + inst.shamt);
			break;
		// move imm
		case 3:
			inst.aluRes = inst.imm;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  immidiate = " + inst.imm);
			break;
		// jumpIfEqual
		case 4:
			if (inst.r1Val == inst.r2Val) {
				PC = PC -1+ inst.imm; // -1 as we already incremented before
				for (int i = 0; i < instructions.size(); i++) {
					if (instructions.get(i) == inst) {
						for (int j = i + 1; j < instructions.size(); j++) {
							instructions.remove(instructions.get(j));
							j--;
						}

					}
				}
				flagJump =2;
			}
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  immidiate = " + inst.imm);
			break;
		// AND
		case 5:
			inst.aluRes = inst.r2Val & inst.r3Val;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  R3 = " + inst.r3Val);
			System.out.println("The input of execute Stage:  shamt = " + inst.shamt);
			break;
		// Exclusive Or Immediate
		case 6:
			inst.aluRes = inst.r2Val ^ inst.imm;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  immidiate = " + inst.imm);
			break;
		// jump
		case 7:
			int PCtemp = PC & 0b11110000000000000000000000000000;
			PCtemp = PCtemp >>> 28;
			String temp = Integer.toBinaryString(PCtemp) + complete28Bit(Integer.toBinaryString(inst.address));
			PC = Integer.parseInt(temp, 2);
			for (int i = 0; i < instructions.size(); i++) {
				if (instructions.get(i) == inst) {
					for (int j = i + 1; j < instructions.size(); j++) {
						instructions.remove(instructions.get(j));
						j--;
					}
				}

			}
			flagJump =2;
			System.out.println("The input of execute Stage:  Address = " + inst.address);
			break;
		// Logical Shift Left
		case 8:
			inst.aluRes = inst.r2Val << inst.shamt;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  R3 = " + inst.r3Val);
			System.out.println("The input of execute Stage:  shamt = " + inst.shamt);
			break;
		// Logical Shift Right
		case 9:
			inst.aluRes = inst.r2Val >>> inst.shamt;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  R3 = " + inst.r3Val);
			System.out.println("The input of execute Stage:  shamt = " + inst.shamt);
			break;
		// Move to Register
		case 10:
			inst.aluRes = inst.r2Val + inst.imm ;
			//+ 1024;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  immidiate = " + inst.imm);
			break;
		// Move to Memory
		case 11:
			inst.aluRes = inst.r2Val + inst.imm ;
			//+ 1024;
			System.out.println("The input of execute Stage:  R1 = " + inst.r1Val);
			System.out.println("The input of execute Stage:  R2 = " + inst.r2Val);
			System.out.println("The input of execute Stage:  immidiate = " + inst.imm);
			break;
		}
		inst.status++;
		System.out.println("We executed: Instruction: " + inst.id);
		

	}

	public void memory(Instruction inst) {
		System.out.println("The input of Memory Stage: Instruction = " + inst.id);
		if (inst.op == 11) {
			memory.write(inst.aluRes, inst.r1Val);
			System.out.println("Memory at:"+ inst.aluRes + " Changed to: "+ inst.r1Val);
		} else if (inst.op == 10) {
			inst.wb = memory.read(inst.aluRes);
			System.out.println("Memory Read at :"+ inst.aluRes + "is "+ inst.wb);
		}
		inst.status++;
		System.out.println("Memory Stage of : Instruction: " + inst.id);


	}

	public void writeBack(Instruction inst) {
		System.out.println(" In the writeBack Stage" + inst.id  );

		if (inst.r1==0)
		{
			inst.status++;
			System.out.println("We wrote Back: Instruction: " + inst.id+" And the value of R0 did not change");
			printArray(registerFile);
			return ;

		}
		switch (inst.op) {
		// add
		case 0:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// sub
		case 1:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// mul
		case 2:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// move imm
		case 3:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// AND
		case 5:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// Exclusive Or Immediate
		case 6:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// Logical Shift Left
		case 8:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// Logical Shift Right
		case 9:
			registerFile[inst.r1] = inst.aluRes;
			printArray(registerFile);
			break;
		// Move to Register
		case 10:
			registerFile[inst.r1] = inst.wb;
			printArray(registerFile);
			break;

		}
		inst.status++;
		
	}

	private static String complete28Bit(String address2) {
		while (address2.length() < 28) {
			address2 = "0" + address2;
		}
		return address2;
	}

	public void pipline() {

		int cycleCount = 7 + ((instCount - 1) * 2);

		for (int i = 1; i<= cycleCount || instructions.size()>0 ; i++) {
			System.out.println("-------------" + i + "-------------");
			for (int j = 0; j < instructions.size(); j++) {
				Instruction inst = instructions.get(j);
				switch (inst.status) {
				case 2:
					inst.status++;
					System.out.println("We are decoding1 instruction: " + inst.id);
					break;
				case 3:
					decode(inst);
					break;
				case 4:
					inst.status++;
					System.out.println("We are executing1 instruction: " + inst.id);
					break;
				case 5:
					execute(inst);
					break;
				case 6:
					memory(inst);
					break;
				case 7:
					writeBack(inst);
					instructions.remove(inst);
					j--;
					break;
				}
			}
			if(flagJump !=0) {
				flagJump--;
			}
			else
		 if (i % 2 != 0 && instructions.size() < 4 && flagJump ==0 ) {
				Instruction fetched = fetch();
				if (fetched != null) {
					instructions.add(fetched);
				}
			}

		}
	}

	public static void printArray(int[] array) {
		if (array.length == 0) {
			System.out.println("Empty array");
			return;
		}

		if (array.length > 32) {
			int[] trimmedArray = new int[32];
			System.arraycopy(array, 0, trimmedArray, 0, 32); // Trim the array if it has more than 32 elements
			array = trimmedArray;
		}

		System.out.println("Register File R0 -> R31:");
		for (int element : array) {
			System.out.print(element + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) {

		CPU cpu = new CPU();
		cpu.pipline();
		printArray(registerFile);
		for (int i=0 ; i<cpu.memory.getMem().length ; i++)
		{
			if (cpu.memory.read(i)!=0)
				System.out.println("Memory["+i + "] = " + cpu.memory.read(i) );
		}

	}

}
