import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedReader;



public class IDE{
	static Map<Character,String> registers;
	static Map<Integer , String> memory;
	static Map<String , Integer> labels;
	static Set<Character> general_registers;
	static char hexa[];
	static BufferedReader scan;
	static String possible_codes[];
	static int address;//Always points to the address where the last instruction was stored
	static boolean AC,CS,Z,P,S;//STATUS FLAGS
	static int PC;
	static int SP;
	static boolean modified;
	/*
	 * NOT DONE ANYTHING ABOUT AC FLAG
	 */
	public static void main(String args[]) throws IOException{
		scan = new BufferedReader(new InputStreamReader(System.in));
		registers = new HashMap<Character,String>();
		memory = new HashMap<Integer , String>();//Memory location 0 will remain unused
		labels = new HashMap<String , Integer>();
		hexa = "0123456789ABCDEF".toCharArray();
		possible_codes = "DAA HLT RST MOV MVI LXI LDA STA LHLD SHLD LDAX STAX XCHG ADD ADC ADI ACI DAD SUB SBB SUI INR DCR INX DCX ANA ANI ORA ORI XRA XRI CMA CMC STC CMP CPI RLC RRC RAL RAR JMP JZ JNZ JC JNC JP JM JPE JPO CALL CZ CNZ CC CNC CP CM CPE CPO RET RZ RNZ RC RNC RP RM RPE RPO PCHL IN OUT PUSH POP XTHL SPHL".split(" ");
		general_registers = new HashSet<Character>();
		AC=CS=Z=P=S=false;
		SP = 65536;
		PC = 0;
		modified = false;
		add_the_general_registers();

		System.out.println("SIMULATOR FOR 8085 MICROPROCESSOR\n");
		//Take the input of code
		System.out.println("ENTER THE CODE :-\n");
		take_the_input();
		//Take the input of memory values
		System.out.println("ENTER THE MEMORY VALUES :-");
		System.out.println("IF NO VALUE HAS TO BE ENTERED PRESS N ELSE WRITE THE ADDRESS.\n");
		take_memory_values();
		//Execution starts
		System.out.println("\nNOW EXECUTION WILL BEGIN...");
		execute();
		//Providing the output to the user
		System.out.println("\nENTER THE MEMORY LOCATIONS AND REGISTER VALUES YOU WANT TO CHECK : ");
		System.out.println("(WHEN YOU ARE DONE CHECKING TYPE N)\n");
		output_process();
	}


	//Take the input code
	static void take_the_input() throws IOException{
		System.out.print("ENTER THE ADDRESS : ");

		address = hexa_to_deci(scan.readLine());
		System.out.println();

		while(true){
			print_the_address(address);
			String instruction = scan.readLine();
			int label_size = check_for_label(instruction);
			instruction = instruction.substring(label_size);
			memory.put(address, instruction);
			if(stop_instruction(instruction))
				break;
			address+=size_of_code(instruction);
		}
		System.out.println();
		System.out.print("DO YOU WANT TO ENTER ANY FURTHUR CODE (Y/N) : ");
		String choice = scan.readLine();
		System.out.println();
		if(choice.equals("Y"))
			take_the_input();
	}

	//Take the memory values
	static void take_memory_values() throws IOException{
		System.out.print("ENTER THE ADDRESS : ");
		String input = scan.readLine();
		if(input.equals("N"))
			return;
		address = hexa_to_deci(input);
		print_the_address(address);
		input = scan.readLine();
		if(input.length()==2)
			memory.put(address,input);
		else{
			memory.put(address,input.substring(2));
			memory.put(address+1,input.substring(0,2));
		}
		//System.out.println(address+" "+memory.get(address));
		take_memory_values();
	}

	//Execute the instructions
	static void execute() throws IOException{
		System.out.print("ENTER THE START ADDRESS : ");
		PC = hexa_to_deci(scan.readLine());
		
		while(true){
			modified = false;
			String instruct = memory.get(PC);
			//System.out.println(PC+" "+decimel_to_hexa(PC)+" "+instruct);
			perform_the_instruction(instruct);
			if(stop_instruction_for_exec(instruct))
				break;
			if(!modified)
				PC+=size_of_code(instruct);
		}
	}
	
	//Output process
	static void output_process() throws IOException{
		while(true){
			String check = scan.readLine();
			if(check.equals("N"))
				break;
			try{
				if(check.length()==1)
					System.out.println(check+" - "+registers.get(check.charAt(0)));
				else{
					int addre = hexa_to_deci(check);
					if(memory.get(addre)==null){
						System.out.println("NO VALUE PROVIDED !!");
						continue;
					}
					System.out.println(check+" - "+memory.get(addre));
				}
			}
			catch(Exception e){
				System.out.println(check+" - 00");
			}
		}
	}
	
	//Check if the instruction is used to stop the execution
	static boolean stop_instruction(String instruction){
		String code = instruction.split(" ")[0];
		switch(code){
		case "HLT":
		case "RST":
		case "RET":
			return true;
		default:
			return false;
		}
	}
	
	static boolean stop_instruction_for_exec(String instruction){
		String code = instruction.split(" ")[0];
		switch(code){
		case "HLT":
		case "RST":
			return true;
		default:
			return false;
		}
	}

	//Check if the code contains a label and return its length
	static int check_for_label(String instruction){
		if(instruction.length()<=4)//For memory values
			return 0;
		String first_code = instruction.split(" ")[0];
		int len = -1;
		for(int i=0;i<possible_codes.length;i++)
			if(possible_codes[i].equals(first_code))
				len = 0;
		if(len!=0){
			labels.put(first_code,address);
			len = first_code.length()+1;
		}
		return len;
	}

	//Add the general purpose registers
	static void add_the_general_registers(){
		general_registers.add('A');
		general_registers.add('B');
		general_registers.add('C');
		general_registers.add('D');
		general_registers.add('E');
		general_registers.add('H');
		general_registers.add('L');
	}

	//Convert from hexa to decimel
	static int hexa_to_deci(String hexa_address){
		int deci_val = 0;
		int multiply = 1;
		for(int i=hexa_address.length()-1;i>=0;i--){
			int j=0;
			for(;j<16;j++)
				if(hexa_address.charAt(i)==hexa[j])
					break;
			deci_val+=(multiply*j);
			multiply*=16;
		}
		return deci_val;
	}

	//Convert from decimal to hexa
	static String decimel_to_hexa(int deci_address){
		String disp = "";
		for(int i=0;i<4;i++){
			char hex = hexa[deci_address%16];
			disp = hex+disp;
			deci_address/=16;
		}
		return disp;
	}

	//COnvert from decimel to hexa for a 8 bit data
	static String decimel_to_hexa_8bit(int deci_address){
		String disp = "";
		for(int i=0;i<2;i++){
			char hex = hexa[deci_address%16];
			disp = hex+disp;
			deci_address/=16;
		}
		return disp;
	}

	//Print the address on the new line
	//to take as input the next instruction
	static void print_the_address(int print_address){
		System.out.print(decimel_to_hexa(print_address)+" : ");
	}

	//Find the size occupied by each address
	static int size_of_code(String instruction){
		int size = 0;
		String code = instruction.split(" ")[0];
		switch(code){
		case "MOV":
			size = size_of_mov(instruction);
			break;
		case "MVI":
			size = size_of_mvi(instruction);
			break;
		case "LXI":
			size = size_of_lxi(instruction);
			break;
		case "LDA":
			size = size_of_lda(instruction);
			break;
		case "STA":
			size = size_of_sta(instruction);
			break;
		case "LHLD":
			size = size_of_lhld(instruction);
			break;
		case "SHLD":
			size = size_of_shld(instruction);
			break;
		case "LDAX":
			size = size_of_ldax(instruction);
			break;
		case "STAX": 
			size = size_of_stax(instruction);
			break;
		case "XCHG":
			size = size_of_xchg(instruction);
			break;
		case "ADD":
			size = size_of_add(instruction);
			break;
		case "ADC":
			size = size_of_adc(instruction);
			break;
		case "ADI":
			size = size_of_adi(instruction);
			break;
		case "ACI":
			size = size_of_aci(instruction);
			break;
		case "DAD":
			size = size_of_dad(instruction);
			break;
		case "SUB":
			size = size_of_sub(instruction);
			break;
		case "SBB":
			size = size_of_sbb(instruction);
			break;
		case "SUI":
			size = size_of_sui(instruction);
			break;
		case "INR":
			size = size_of_inr(instruction);
			break;
		case "DCR":
			size = size_of_dcr(instruction);
			break;
		case "INX":
			size = size_of_inx(instruction);
			break;
		case "DCX":
			size = size_of_dcx(instruction);
			break;
		case "DAA":
			size = size_of_daa(instruction);
		case "ANA":
			size = size_of_ana(instruction);
			break;
		case "ANI":
			size = size_of_ani(instruction);
			break;
		case "ORA":
			size = size_of_ora(instruction);
			break;
		case "ORI":
			size = size_of_ori(instruction);
			break;
		case "XRA":
			size = size_of_xra(instruction);
			break;
		case "XRI":
			size = size_of_xri(instruction);
			break;
		case "CMA":
			size = size_of_cma(instruction);
			break;
		case "CMC":
			size = size_of_cmc(instruction);
			break;
		case "STC":
			size = size_of_stc(instruction);
			break;
		case "CMP":
			size = size_of_cmp(instruction);
			break;
		case "CPI":
			size = size_of_cpi(instruction);
			break;
		case "RLC":
			size = size_of_rlc(instruction);
			break;
		case "RRC":
			size = size_of_rrc(instruction);
			break;
		case "RAL":
			size = size_of_ral(instruction);
			break;
		case "RAR":
			size = size_of_rar(instruction);
			break;
		case "JMP":
			size = size_of_jmp(instruction);
			break;
		case "JZ":
			size = size_of_jz(instruction);
			break;
		case "JNZ":
			size = size_of_jnz(instruction);
			break;
		case "JC":
			size = size_of_jc(instruction);
			break;
		case "JNC":
			size = size_of_jnc(instruction);
			break;
		case "JP":
			size = size_of_jp(instruction);
			break;
		case "JM":
			size = size_of_jm(instruction);
			break;
		case "JPE":
			size = size_of_jpe(instruction);
			break;
		case "JPO":
			size = size_of_jpo(instruction);
			break;
		case "CALL":
			size = size_of_call(instruction);
			break;
		case "CZ":
			size = size_of_cz(instruction);
			break;
		case "CNZ":
			size = size_of_cnz(instruction);
			break;
		case "CC":
			size = size_of_cc(instruction);
			break;
		case "CNC":
			size = size_of_cnc(instruction);
			break;
		case "CP":
			size = size_of_cp(instruction);
			break;
		case "CM":
			size = size_of_cm(instruction);
			break;
		case "CPE":
			size = size_of_cpe(instruction);
			break;
		case "CPO":
			size = size_of_cpo(instruction);
			break;
		case "RET":
			size = size_of_ret(instruction);
			break;
		case "RZ":
			size = size_of_rz(instruction);
			break;
		case "RNZ":
			size = size_of_rnz(instruction);
			break;
		case "RC":
			size = size_of_rc(instruction);
			break;
		case "RNC":
			size = size_of_rnc(instruction);
			break;
		case "RP":
			size = size_of_rp(instruction);
			break;
		case "RM":
			size = size_of_rm(instruction);
			break;
		case "RPE":
			size = size_of_rpe(instruction);
			break;
		case "RPO":
			size = size_of_rpo(instruction);
			break;
		case "PCHL":
			size = size_of_pchl(instruction);
			break;
		case "IN":
			size = size_of_in(instruction);
			break;
		case "OUT":
			size = size_of_out(instruction);
			break;
		case "PUSH":
			size = size_of_push(instruction);
			break;
		case "POP":
			size = size_of_pop(instruction);
			break;
		case "XTHL":
			size = size_of_xthl(instruction);
			break;
		case "SPHL":
			size = size_of_sphl(instruction);
			break;
		default :
			size = 0;
			break;
		}
		return size;
	}

	
	//Perform the instruction
		static void perform_the_instruction(String instruction){
			String code = instruction.trim().split(" ")[0];
			switch(code){
			case "MOV":
				perform_mov(instruction);
				break;
			case "MVI":
				perform_mvi(instruction);
				break;
			case "LXI":
				perform_lxi(instruction);
				break;
			case "LDA":
				perform_lda(instruction);
				break;
			case "STA":
				perform_sta(instruction);
				break;
			case "LHLD":
				perform_lhld(instruction);
				break;
			case "SHLD":
				perform_shld(instruction);
				break;
			case "LDAX":
				perform_ldax(instruction);
				break;
			case "STAX": 
				perform_stax(instruction);
				break;
			case "XCHG":
				perform_xchg(instruction);
				break;
			case "ADD":
				perform_add(instruction);
				break;
			case "ADC":
				perform_adc(instruction);
				break;
			case "ADI":
				perform_adi(instruction);
				break;
			case "ACI":
				perform_aci(instruction);
				break;
			case "DAD":
				perform_dad(instruction);
				break;
			case "SUB":
				perform_sub(instruction);
				break;
			case "SBB":
				perform_sbb(instruction);
				break;
			case "SUI":
				perform_sui(instruction);
				break;
			case "INR":
				perform_inr(instruction);
				break;
			case "DCR":
				perform_dcr(instruction);
				break;
			case "INX":
				perform_inx(instruction);
				break;
			case "DCX":
				perform_dcx(instruction);
				break;
			case "DAA":
				perform_daa(instruction);
				break;
			case "ANA":
				perform_ana(instruction);
				break;
			case "ANI":
				perform_ani(instruction);
				break;
			case "ORA":
				perform_ora(instruction);
				break;
			case "ORI":
				perform_ori(instruction);
				break;
			case "XRA":
				perform_xra(instruction);
				break;
			case "XRI":
				perform_xri(instruction);
				break;
			case "CMA":
				perform_cma(instruction);
				break;
			case "CMC":
				perform_cmc(instruction);
				break;
			case "STC":
				perform_stc(instruction);
				break;
			case "CMP":
				perform_cmp(instruction);
				break;
			case "CPI":
				perform_cpi(instruction);
				break;
			case "RLC":
				perform_rlc(instruction);
				break;
			case "RRC":
				perform_rrc(instruction);
				break;
			case "RAL":
				perform_ral(instruction);
				break;
			case "RAR":
				perform_rar(instruction);
				break;
			case "JMP":
				perform_jmp(instruction);
				break;
			case "JZ":
				perform_jz(instruction);
				break;
			case "JNZ":
				perform_jnz(instruction);
				break;
			case "JC":
				perform_jc(instruction);
				break;
			case "JNC":
				perform_jnc(instruction);
				break;
			case "JP":
				perform_jp(instruction);
				break;
			case "JM":
				perform_jm(instruction);
				break;
			case "JPE":
				perform_jpe(instruction);
				break;
			case "JPO":
				perform_jpo(instruction);
				break;
			case "CALL":
				perform_call(instruction);
				break;
			case "CZ":
				perform_cz(instruction);
				break;
			case "CNZ":
				perform_cnz(instruction);
				break;
			case "CC":
				perform_cc(instruction);
				break;
			case "CNC":
				perform_cnc(instruction);
				break;
			case "CP":
				perform_cp(instruction);
				break;
			case "CM":
				perform_cm(instruction);
				break;
			case "CPE":
				perform_cpe(instruction);
				break;
			case "CPO":
				perform_cpo(instruction);
				break;
			case "RET":
				perform_ret(instruction);
				break;
			case "RZ":
				perform_rz(instruction);
				break;
			case "RNZ":
				perform_rnz(instruction);
				break;
			case "RC":
				perform_rc(instruction);
				break;
			case "RNC":
				perform_rnc(instruction);
				break;
			case "RP":
				perform_rp(instruction);
				break;
			case "RM":
				perform_rm(instruction);
				break;
			case "RPE":
				perform_rpe(instruction);
				break;
			case "RPO":
				perform_rpo(instruction);
				break;
			case "PCHL":
				perform_pchl(instruction);
				break;
			case "IN":
				perform_in(instruction);
				break;
			case "OUT":
				perform_out(instruction);
				break;
			case "PUSH":
				perform_push(instruction);
				break;
			case "POP":
				perform_pop(instruction);
				break;
			case "XTHL":
				perform_xthl(instruction);
				break;
			case "SPHL":
				perform_sphl(instruction);
				break;
			}
		}
	
	
	
	//Get memory location from HL pair
	static int memory_address_hl(){
		int hl_address = hexa_to_deci(registers.get('H')+registers.get('L'));
		return hl_address;
	}


	//                                                     DATA TRANSFER GROUP


	//MOV Command

	/*
	 Type 1 memory to reg
	 Type 2 reg to memory
	 Type 3 reg to reg
	 */

	static int size_of_mov(String passed){
		return 1;
	}
	//This will call the appropriate function for that MOV
	static void perform_mov(String passed){
		int type = type_of_mov(passed);
		if(type==1)
			mov_memory_to_reg(passed);
		else if(type==2)
			mov_reg_to_memory(passed);
		else if(type==3)
			mov_reg_to_reg(passed);
	}
	//This will find the type of MOV
	static int type_of_mov(String passed){
		if(passed.charAt(4)=='M' && general_registers.contains(passed.charAt(6)))
			return 2;
		else if(passed.charAt(6)=='M' && general_registers.contains(passed.charAt(4)))
			return 1;
		else if(general_registers.contains(passed.charAt(6)) && general_registers.contains(passed.charAt(4)))
			return 3;
		else
			return 0;
	}
	//Type 1 of MOV
	static void mov_memory_to_reg(String passed){
		int memory_address = memory_address_hl();
		registers.put(passed.charAt(4),memory.get(memory_address));
	}
	//Type 2 of MOV
	static void mov_reg_to_memory(String passed){
		int memory_address = memory_address_hl();
		memory.put(memory_address, registers.get(passed.charAt(6)));
	}
	//Type 3 of MOV
	static void mov_reg_to_reg(String passed){
		registers.put(passed.charAt(4),registers.get(passed.charAt(6)));
	}



	//MVI Command
	/*
	 Type 1 mvi to reg
	 Type 2 mvi to mem
	 */

	static int size_of_mvi(String passed){
		return 2;
	}
	//Call appropriate method acc to type
	static void perform_mvi(String passed){
		int type = type_of_mvi(passed);
		if(type==1)
			mvi_to_reg(passed);
		else if(type==2)
			mvi_to_mem(passed);
	}
	//Find type of mvi
	static int type_of_mvi(String passed){
		if(passed.charAt(4)!='M')
			return 1;
		else if(general_registers.contains(passed.charAt(4)))
			return 2;
		else
			return 0;
	}
	//Type 1 of MVI
	static void mvi_to_reg(String passed){
		registers.put(passed.charAt(4), passed.substring(6));
	}
	//Type 2 of MVi
	static void mvi_to_mem(String passed){
		int memory_address = memory_address_hl();
		memory.put(memory_address, passed.substring(6));
	}


	//LXI Command
	/*
	 Type 1 Load immediately the data into the register pair
	 */

	static int size_of_lxi(String passed){
		return 3;
	}
	//Call appropriate method acc to type
	static void perform_lxi(String passed){
		int type = type_of_lxi(passed);
		if(type==1)
			lxi_to_reg_pair(passed);
	}
	//Find type of LXI
	static int type_of_lxi(String passed){
		char fourth = passed.charAt(4);
		if(fourth=='B' || fourth=='D' || fourth=='H')
			return 1;
		else
			return 0;
	}
	//Type 1 of LXI
	static void lxi_to_reg_pair(String passed){
		char fourth = passed.charAt(4);
		if(fourth=='B'){
			registers.put('B', passed.substring(6,8));
			registers.put('C', passed.substring(8));
		}
		else if(fourth=='D'){
			registers.put('D', passed.substring(6,8));
			registers.put('E', passed.substring(8));
		}
		else if(fourth=='H'){
			registers.put('H', passed.substring(6,8));
			registers.put('L', passed.substring(8));
		}
	}



	//LDA Command
	/*
	 Type 1 Load the data from mem to A
	 */

	static int size_of_lda(String passed){
		return 3;
	}
	//Call appropriate method acc to type
	static void perform_lda(String passed){
		int type = type_of_lda(passed);
		if(type==1)
			lda_from_mem(passed);
	}
	//Find type of LDA
	static int type_of_lda(String passed){
		return 1;
	}
	//Type 1 of LDA
	static void lda_from_mem(String passed){
		registers.put('A', memory.get(hexa_to_deci(passed.substring(4))));
	}



	//STA Command
	/*
	 Type 1 Store the data from A to mem
	 */

	static int size_of_sta(String passed){
		return 3;
	}
	//Call appropriate method acc to type
	static void perform_sta(String passed){
		int type = type_of_sta(passed);
		if(type==1)
			sta_to_mem(passed);
	}
	//Find type of LDA
	static int type_of_sta(String passed){
		return 1;
	}
	//Type 1 of LDA
	static void sta_to_mem(String passed){
		memory.put(hexa_to_deci(passed.substring(4)),registers.get('A'));
		//System.out.println(memory.get(hexa_to_deci(passed.substring(4))));
	}


	//LHLD Command
	/*
	 Type 1 Load the data from consecutive memory to HL pair direct
	 */

	static int size_of_lhld(String passed){
		return 3;
	}
	//Call appropriate method acc to type
	static void perform_lhld(String passed){
		int type = type_of_lhld(passed);
		if(type==1)
			lhld_from_mem(passed);
	}
	//Find type of LHLD
	static int type_of_lhld(String passed){
		return 1;
	}
	//Type 1 of LHLD
	static void lhld_from_mem(String passed){
		registers.put('L',memory.get(hexa_to_deci(passed.substring(5))));
		int add_for_H = hexa_to_deci(passed.substring(5))+1;
		registers.put('H', memory.get(add_for_H));
	}



	//SHLD Command
	/*
	 Type 1 Store the data from HL pair to memory
	 */

	static int size_of_shld(String passed){
		return 3;
	}
	//Call appropriate method acc to type
	static void perform_shld(String passed){
		int type = type_of_shld(passed);
		if(type==1)
			shld_to_mem(passed);
	}
	//Find type of SHLD
	static int type_of_shld(String passed){
		return 1;
	}
	//Type 1 of SHLD
	static void shld_to_mem(String passed){
		int memo_address = hexa_to_deci(passed.substring(5));
		memory.put(memo_address, registers.get('L'));
		memo_address++;
		memory.put(memo_address, registers.get('H'));
	}


	//LDAX Command
	/*
	 * Type 1 Load the A with the data from the memory having address as register pair content
	 */

	static int size_of_ldax(String passed){
		return 1;
	}
	//Call appropriate method acc to type
	static void perform_ldax(String passed){
		int type = type_of_ldax(passed);
		if(type==1)
			ldax_from_mem(passed);
	}
	//Find type of LDAX
	static int type_of_ldax(String passed){
		return 1;
	}
	//Type 1 of LDAX
	static void ldax_from_mem(String passed){
		registers.put('A',memory.get(hexa_to_deci(registers.get('H')+registers.get('L'))));
	}


	//STAX Command
	/*
	 * Type 1 Store the content of A to the memory location specified by the content of the  HL pair
	 */

	static int size_of_stax(String passed){
		return 1;
	}
	//Call appropriate  method acc to type
	static void perform_stax(String passed){
		int type = type_of_stax(passed);
		if(type==1)
			stax_to_mem(passed);
	}
	//Find type of STAX
	static int type_of_stax(String passed){
		return 1;
	}
	//Type 1 of STAX
	static void stax_to_mem(String passed){
		memory.put(hexa_to_deci(registers.get('H')+registers.get('L')),registers.get('A'));
	}


	//XCHG Command
	/*
	 * Type 1 Exchange the contents of DE and HL reg pair
	 */

	static int size_of_xchg(String passed){
		return 1;
	}
	//Call appropriate method according to type
	static void perform_xchg(String passed){
		int type = type_of_xchg(passed);
		if(type==1)
			xchg_de_with_hl(passed);
	}
	//Find type of XCHG
	static int type_of_xchg(String passed){
		return 1;
	}
	//Type 1 of XCHG
	static void xchg_de_with_hl(String passed){
		String temporary = registers.get('D');
		registers.put('D', registers.get('H'));
		registers.put('H',temporary);
		temporary = registers.get('E');
		registers.put('E',registers.get('L'));
		registers.put('L',temporary);
	}


	//Modify the status flags
	static void modify_status(String content){
		String binary_A = Integer.toBinaryString(hexa_to_deci(content));
		int length = binary_A.length();
		if(length==32)
			binary_A = binary_A.substring(24);
		int ones = 0;
		for(int i=0;i<length;i++)
			if(binary_A.charAt(i)=='1')
				ones++;
		for(int i=length;i<8;i++)
			binary_A = "0"+binary_A;
		S = binary_A.charAt(0)=='1'?true:false;
		Z = ones==0?true:false;
		P = ones%2==0?true:false;
	}

	//                                                      ARITHEMATIC GROUP

	//ADD Command
	/*
	 * Type 1 Add the register to A
	 * Type 2 Add the memory content to A
	 */

	static int size_of_add(String passed){
		return 1;
	}
	//Call appropriate method acc to type
	static void perform_add(String passed){
		int type = type_of_add(passed);
		if(type==1)
			add_with_reg(passed);
		else if(type==2)
			add_with_mem(passed);
	}
	//Find type of ADD
	static int type_of_add(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}
	//Type 1 of ADD
	static void add_with_reg(String passed){
		int sum = hexa_to_deci(registers.get('A'));
		/*
		 * This will be valid only when sign arithematic is being performed
		 * if(S)
			sum*=-1;
		 */
		sum+=(hexa_to_deci(registers.get(passed.charAt(4))));
		CS = sum>255?true:false;
		registers.put('A', decimel_to_hexa_8bit(sum));
		modify_status(registers.get('A'));
	}
	//Type 2 of ADD
	static void add_with_mem(String passed){
		int sum = hexa_to_deci(registers.get('A'));
		/*
		 * This will be valid only when sign arithematic is being performed
		 * if(S)
			sum*=-1;
		 */
		sum+=hexa_to_deci(memory.get(memory_address_hl()));
		CS = sum>255?true:false;
		registers.put('A', decimel_to_hexa_8bit(sum));
		modify_status(registers.get('A'));
	}



	//ADC Command
	/*
	 * Type 1 Add register with carry to A
	 * Type 2 Add memory with carry to A
	 */

	static int size_of_adc(String passed){
		return 1;
	}
	//Call appropriate method acc to type
	static void perform_adc(String passed){
		int type = type_of_adc(passed);
		if(type==1)
			adc_with_reg(passed);
		else if(type==2)
			adc_with_mem(passed);
	}
	//Find the type of ADC
	static int type_of_adc(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}
	//Type 1 of ADC 
	static void adc_with_reg(String passed){
		int sum = hexa_to_deci(registers.get('A'));
		/*
		 * This will be valid only when sign arithematic is being performed
		 * if(S)
			sum*=-1;
		 */
		sum+=(CS?1:0);
		sum+=hexa_to_deci(registers.get(passed.charAt(4)));
		CS = sum>255?true:false;
		registers.put('A',decimel_to_hexa_8bit(sum));
		modify_status(registers.get('A'));
	}
	//Type 2 of ADC
	static void adc_with_mem(String passed){
		int sum = hexa_to_deci(registers.get('A'));
		/*
		 * This will be valid only when sign arithematic is being performed
		 * if(S)
			sum*=-1;
		 */
		sum+=(CS?1:0);
		sum+=hexa_to_deci(memory.get(memory_address_hl()));
		CS = sum>255?true:false;
		registers.put('A', decimel_to_hexa_8bit(sum));
		modify_status(registers.get('A'));
	}



	//ADI Command
	/*
	 * Type 1 Add the immediate data to the A
	 */

	static int size_of_adi(String passed){
		return 2;
	}
	//Call appropriate method acc to type
	static void perform_adi(String passed){
		int type = type_of_adi(passed);
		switch(type){
		case 1:
			adi_data_with_acc(passed);
			break;
		}
	}

	//Find type of ADI
	static int type_of_adi(String passed){
		return 1;
	}

	//Type 1 of ADI
	static void adi_data_with_acc(String passed){
		int sum = hexa_to_deci(registers.get('A'));
		sum+=hexa_to_deci(passed.substring(4));
		CS = sum>255?true:false;
		registers.put('A',decimel_to_hexa_8bit(sum));
		modify_status(registers.get('A'));
	}


	//ACI Command
	/*
	 * Type 1 Add the immediate data with the accumulator with carry
	 */

	static int size_of_aci(String passed){
		return 2;
	}

	//Call appropriate method acc to type
	static void perform_aci(String passed){
		int type = type_of_aci(passed);
		switch(type){
		case 1:
			aci_data_with_acc(passed);
			break;
		}
	}

	//Find type of ACI
	static int type_of_aci(String passed){
		return 1;
	}

	//Type 1 of ACI
	static void aci_data_with_acc(String passed){
		int sum = hexa_to_deci(registers.get('A'));
		sum+=(CS?1:0);
		sum+=hexa_to_deci(passed.substring(4));
		CS = sum>255?true:false;
		registers.put('A',decimel_to_hexa_8bit(sum));
		modify_status(registers.get('A'));
	}


	//DAD Command
	/*
	 * Type 1 Add the contents of the HL pair to the mentioned register pair
	 */

	static int size_of_dad(String passed){
		return 1;
	}

	//Call appropriate method acc to type
	static void perform_dad(String passed){
		int type = type_of_dad(passed);
		switch(type){
		case 1:
			dad_with_hl(passed);
			break;
		}
	}

	//Find type of DAD
	static int type_of_dad(String passed){
		return 1;
	}

	//Type 1 of DAD
	static void dad_with_hl(String passed){
		switch(passed.charAt(4)){
		case 'B':
			dad_with_hl_internal(hexa_to_deci(registers.get('B')),hexa_to_deci(registers.get('C')));
			break;
		case 'D':
			dad_with_hl_internal(hexa_to_deci(registers.get('D')),hexa_to_deci(registers.get('E')));
		}
	}

	//Function to implement dad_with_hl_using a function
	static void dad_with_hl_internal(int h,int l){
		l+=hexa_to_deci(registers.get('L'));
		int carry = l>255?1:0;
		registers.put('L',decimel_to_hexa_8bit(l));
		h+=hexa_to_deci(registers.get('H'));
		h+=carry;
		CS = h>255?true:false;
		registers.put('H',decimel_to_hexa_8bit(h));
	}



	//SUB Command
	/*
	 * Type 1 Subtract register from A
	 * Type 2 Subtract the memory data from A
	 */

	static int size_of_sub(String passed){
		return 1;
	}

	//Call the app method acc to type
	static void perform_sub(String passed){
		int type = type_of_sub(passed);
		switch(type){
		case 1:
			sub_with_reg(passed);
			break;
		case 2:
			sub_with_mem(passed);
			break;
		}
	}

	//Find the type of SUB
	static int type_of_sub(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of SUB
	static void sub_with_reg(String passed){
		int subt = hexa_to_deci(registers.get('A'));
		int minu = hexa_to_deci(registers.get(passed.charAt(4)));
		minu = 256-minu;
		minu%=256;
		subt+=minu;
		CS = subt>255?true:false;
		registers.put('A',decimel_to_hexa_8bit(subt));
		modify_status(registers.get('A'));
	}

	//Type 2 of SUB
	static void sub_with_mem(String passed){
		int subt = hexa_to_deci(registers.get('A'));
		int minu = hexa_to_deci(memory.get(memory_address_hl()));
		minu = 256-minu;
		minu%=256;
		subt+=minu;
		CS = subt>255?true:false;
		registers.put('A',decimel_to_hexa_8bit(subt));
		modify_status(registers.get('A'));
	}



	//SBB Command
	/*
	 * Type 1 Subtract register and carry from A
	 * Type 2 Subtract memory content and carry from A
	 */

	static int size_of_sbb(String passed){
		return 1;
	}

	//Call the appropriate method acc to type
	static void perform_sbb(String passed){
		int type = type_of_sbb(passed);
		switch(type){
		case 1:
			sbb_with_reg(passed);
			break;
		case 2:
			sbb_with_mem(passed);
			break;
		}
	}

	//Find the type of SBB
	static int type_of_sbb(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of SBB
	static void sbb_with_reg(String passed){
		int subt = hexa_to_deci(registers.get('A'));
		int mult = hexa_to_deci(registers.get(passed.charAt(4)));
		mult++;
		mult%=256;
		mult = 256-mult;
		mult%=256;
		subt+=mult;
		CS = subt>255?true:false;
		registers.put('A', decimel_to_hexa_8bit(subt));
		modify_status(registers.get('A'));
	}

	//Type 2 of SBB
	static void sbb_with_mem(String passed){
		int subt = hexa_to_deci(registers.get('A'));
		int mult = hexa_to_deci(memory.get(memory_address_hl()));
		mult++;
		mult%=256;
		mult = 256-mult;
		subt+=mult;
		CS = subt>255?true:false;
		registers.put('A', decimel_to_hexa_8bit(subt));
		modify_status(registers.get('A'));
	}



	//SUI Command
	/*
	 * Type 1 Subtract immediate data from A
	 */

	static int size_of_sui(String passed){
		return 2;
	}

	//Call the appropriate method according to type
	static void perform_sui(String passed){
		int type = type_of_sui(passed);
		switch(type){
		case 1:
			sui_with_acc(passed);
			break;
		}
	}

	//Find the type of SUI
	static int type_of_sui(String passed){
		return 1;
	}

	//Type 1 of SUI
	static void sui_with_acc(String passed){
		int subt = hexa_to_deci(registers.get('A'));
		int minu = hexa_to_deci(passed.substring(4));
		minu = 256-minu;
		minu%=256;
		subt+=minu;
		CS = subt>255?true:false;
		registers.put('A', decimel_to_hexa_8bit(subt));
		modify_status(registers.get('A'));
	}



	//INR Command
	/*
	 * Type 1 Increment the value of registers
	 * Type 2 Increment the value of the memory location
	 */

	static int size_of_inr(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_inr(String passed){
		int type = type_of_inr(passed);
		switch(type){
		case 1:
			inr_reg(passed);
			break;
		case 2:
			inr_mem(passed);
			break;
		}
	}

	//Find the type of INR
	static int type_of_inr(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of INR
	static void inr_reg(String passed){
		int val = hexa_to_deci(registers.get(passed.charAt(4)));
		val++;
		registers.put(passed.charAt(4), decimel_to_hexa_8bit(val));
		modify_status(registers.get(passed.charAt(4)));
	}

	//Type 2 of INR
	static void inr_mem(String passed){
		int val = hexa_to_deci(memory.get(memory_address_hl()));
		val++;
		memory.put(memory_address_hl(), decimel_to_hexa_8bit(val));
		modify_status(memory.get(memory_address_hl()));
	}



	//DCR Command
	/*
	 * Type 1 Decrement the value of registers
	 * Type 2 Decrement the value of the memory location
	 */

	static int size_of_dcr(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_dcr(String passed){
		int type = type_of_dcr(passed);
		switch(type){
		case 1:
			dcr_reg(passed);
			break;
		case 2:
			dcr_mem(passed);
			break;
		}
	}

	//Find the type of INR
	static int type_of_dcr(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of DCR
	static void dcr_reg(String passed){
		int val = hexa_to_deci(registers.get(passed.charAt(4)));
		val--;
		registers.put(passed.charAt(4), decimel_to_hexa_8bit(val));
		modify_status(registers.get(passed.charAt(4)));
	}

	//Type 2 of DCR
	static void dcr_mem(String passed){
		int val = hexa_to_deci(memory.get(memory_address_hl()));
		val--;
		memory.put(memory_address_hl(), decimel_to_hexa_8bit(val));
		modify_status(memory.get(memory_address_hl()));
	}



	//INX Command
	/*
	 * Type 1 Increment the data contained in the register pair
	 */

	static int size_of_inx(String passed){
		return 1;
	}

	//Call the appropriate method acc to type
	static void perform_inx(String passed){
		int type = type_of_inx(passed);
		switch(type){
		case 1:
			inx_rp(passed);
			break;
		}
	}

	//Find the type of INX
	static int type_of_inx(String passed){
		return 1;
	}

	//Type 1 of INX
	static void inx_rp(String passed){
		int h,l;
		switch(passed.charAt(4)){
		case 'B':
			h = hexa_to_deci(registers.get('B'));
			l = hexa_to_deci(registers.get('C'));
			l++;
			h+=(l>255?1:0);
			registers.put('C',decimel_to_hexa_8bit(l));
			registers.put('B',decimel_to_hexa_8bit(h));
			break;
		case 'D':
			h = hexa_to_deci(registers.get('D'));
			l = hexa_to_deci(registers.get('E'));
			l++;
			h+=(l>255?1:0);
			registers.put('E',decimel_to_hexa_8bit(l));
			registers.put('D',decimel_to_hexa_8bit(h));
			break;
		case 'H':
			h = hexa_to_deci(registers.get('H'));
			l = hexa_to_deci(registers.get('L'));
			l++;
			h+=(l>255?1:0);
			registers.put('L',decimel_to_hexa_8bit(l));
			registers.put('H',decimel_to_hexa_8bit(h));
			break;
		}
	}



	//DCX Command
	/*
	 * Type 1 Decrement the data contained in the register pair
	 */

	static int size_of_dcx(String passed){
		return 1;
	}

	//Call the appropriate method acc to type
	static void perform_dcx(String passed){
		int type = type_of_inx(passed);
		switch(type){
		case 1:
			dcx_rp(passed);
			break;
		}
	}

	//Find the type of INX
	static int type_of_dcx(String passed){
		return 1;
	}

	//Type 1 of INX
	static void dcx_rp(String passed){
		int h,l;
		switch(passed.charAt(4)){
		case 'B':
			h = hexa_to_deci(registers.get('B'));
			l = hexa_to_deci(registers.get('C'));
			l--;
			if(l==-1)
				h--;
			if(l==-1)
				l=255;
			if(h==-1)
				h=255;
			registers.put('C',decimel_to_hexa_8bit(l));
			registers.put('B',decimel_to_hexa_8bit(h));
			break;
		case 'D':
			h = hexa_to_deci(registers.get('D'));
			l = hexa_to_deci(registers.get('E'));
			l--;
			if(l==-1)
				h--;
			if(l==-1)
				l=255;
			if(h==-1)
				h=255;
			h+=(l>255?1:0);
			registers.put('E',decimel_to_hexa_8bit(l));
			registers.put('D',decimel_to_hexa_8bit(h));
			break;
		case 'H':
			h = hexa_to_deci(registers.get('H'));
			l = hexa_to_deci(registers.get('L'));
			l--;
			if(l==-1)
				h--;
			if(l==-1)
				l=255;
			if(h==-1)
				h=255;
			h+=(l>255?1:0);
			registers.put('L',decimel_to_hexa_8bit(l));
			registers.put('H',decimel_to_hexa_8bit(h));
			break;
		}
	}



	//DAA Command
	/*
	 * Type 1 Adjust the content of the A to represent decimel value
	 */

	static int size_of_daa(String passed){
		return 1;
	}
	
	//Perform DAA
	static void perform_daa(String passed){
		int val = hexa_to_deci(registers.get('A'));
		String ans = "";
		for(int i=0;i<8;i++){
			ans = Integer.toString(val%10)+ans;
			val/=10;
		}
		if(val==0)
			CS = false;
		else
			CS = true;
		modify_status(registers.get('A'));
	}
	
	


	//LOGICAL GROUP


	//ANA Command
	/*
	 * Type 1 And the A with register
	 * Type 2 And the A with memory
	 */

	static int size_of_ana(String passed){
		return 1;
	}

	//Call the appropriate method acc to type
	static void perform_ana(String passed){
		int type = type_of_ana(passed);
		switch(type){
		case 1:
			ana_with_reg(passed);
			break;
		case 2:
			ana_with_mem(passed);
			break;
		}
	}

	//Find the type of ANA
	static int type_of_ana(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of ANA
	static void ana_with_reg(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(registers.get(passed.charAt(4)));
		val1 = val1&val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}

	//Type 2 of ANA
	static void ana_with_mem(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(memory.get(memory_address_hl()));
		val1 = val1&val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}



	//ANI Command
	/*
	 * Type 1 And the immediate data to the A
	 */

	static int size_of_ani(String passed){
		return 2;
	}

	//Call the appropriate method acc to type of ANI
	static void perform_ani(String passed){
		int type = type_of_ani(passed);
		if(type==1)
			ani_with_acc(passed);
	}

	//Find the type of ANI
	static int type_of_ani(String passed){
		return 1;
	}

	//Type 1 of ANI
	static void ani_with_acc(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(passed.substring(4));
		val1 = val1&val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}



	//ORA Command
	/*
	 * Type 1 Or the A with register
	 * Type 2 Or the A with memory
	 */

	static int size_of_ora(String passed){
		return 1;
	}

	//Call the appropriate method acc to type
	static void perform_ora(String passed){
		int type = type_of_ora(passed);
		switch(type){
		case 1:
			ora_with_reg(passed);
			break;
		case 2:
			ora_with_mem(passed);
			break;
		}
	}

	//Find the type of ORA
	static int type_of_ora(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of ORA
	static void ora_with_reg(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(registers.get(passed.charAt(4)));
		val1 = val1|val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}

	//Type 2 of ORA
	static void ora_with_mem(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(memory.get(memory_address_hl()));
		val1 = val1|val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}



	//ORI Command
	/*
	 * Type 1 Or the immediate data to the A
	 */

	static int size_of_ori(String passed){
		return 2;
	}

	//Call the appropriate method acc to type of ORI
	static void perform_ori(String passed){
		int type = type_of_ori(passed);
		if(type==1)
			ori_with_acc(passed);
	}

	//Find the type of ORI
	static int type_of_ori(String passed){
		return 1;
	}

	//Type 1 of ORI
	static void ori_with_acc(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(passed.substring(4));
		val1 = val1|val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}



	//XRA Command
	/*
	 * Type 1 Xor the A with register
	 * Type 2 Xor the A with memory
	 */

	static int size_of_xra(String passed){
		return 1;
	}

	//Call the appropriate method acc to type
	static void perform_xra(String passed){
		int type = type_of_xra(passed);
		switch(type){
		case 1:
			xra_with_reg(passed);
			break;
		case 2:
			xra_with_mem(passed);
			break;
		}
	}

	//Find the type of XRA
	static int type_of_xra(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of XRA
	static void xra_with_reg(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(registers.get(passed.charAt(4)));
		val1 = val1&val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}

	//Type 2 of XRA
	static void xra_with_mem(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(memory.get(memory_address_hl()));
		val1 = val1&val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}



	//XRI Command
	/*
	 * Type 1 Xor the immediate data to the A
	 */

	static int size_of_xri(String passed){
		return 2;
	}

	//Call the appropriate method acc to type of XRI
	static void perform_xri(String passed){
		int type = type_of_xri(passed);
		if(type==1)
			xri_with_acc(passed);
	}

	//Find the type of XRI
	static int type_of_xri(String passed){
		return 1;
	}

	//Type 1 of XRI
	static void xri_with_acc(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(passed.substring(4));
		val1 = val1&val2;
		registers.put('A',decimel_to_hexa_8bit(val1));
		modify_status(registers.get('A'));
	}



	//CMA Command
	/*
	 * Type 1 Complement the A
	 */

	static int size_of_cma(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_cma(String passed){
		int type = type_of_cma(passed);
		switch(type){
		case 1:
			cma_with_acc(passed);
			break;
		}
	}

	//Find the type of CMA
	static int type_of_cma(String passed){
		return 1;
	}

	//Type 1 of CMA
	static void cma_with_acc(String passed){
		int val = hexa_to_deci(registers.get('A'));
		String bin = Integer.toBinaryString(val);
		for(int i=bin.length();i<8;i++)
			bin = "0"+bin;
		val=0;
		for(int i=7;i>=0;i--)
			if(bin.charAt(i)=='0')
				val+=(Math.pow(2,7-i));
		registers.put('A',decimel_to_hexa_8bit(val));
	}



	//CMC Command
	/*
	 * Type 1 Complement the carry status
	 */

	static int size_of_cmc(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_cmc(String passed){
		int type = type_of_cmc(passed);
		switch(type){
		case 1:
			cmc_with_car(passed);
			break;
		}
	}

	//Find the type of CMC
	static int type_of_cmc(String passed){
		return 1;
	}

	//Type 1 of CMC
	static void cmc_with_car(String passed){
		CS = !CS;
	}



	//STC Command
	/*
	 * Type 1 Set the CS
	 */

	static int size_of_stc(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_stc(String passed){
		int type = type_of_stc(passed);
		switch(type){
		case 1:
			stc_with_car(passed);
			break;
		}
	}

	//Find the type of STC
	static int type_of_stc(String passed){
		return 1;
	}

	//Type 1 of STC
	static void stc_with_car(String passed){
		CS = true;
	}



	//CMP Command
	/*
	 * Type 1 Compare the register with A
	 * Type 2 Compare the memory with A
	 */

	static int size_of_cmp(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_cmp(String passed){
		int type = type_of_cmp(passed);
		switch(type){
		case 1:
			cmp_with_reg(passed);
			break;
		case 2:
			cmp_with_mem(passed);
			break;
		}
	}

	//Find the type of CMP
	static int type_of_cmp(String passed){
		if(general_registers.contains(passed.charAt(4)))
			return 1;
		else if(passed.charAt(4)=='M')
			return 2;
		else
			return 0;
	}

	//Type 1 of CMP
	static void cmp_with_reg(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(registers.get(passed.charAt(4)));
		S = val1>=val2?false:true;
		val2 = 256-val2;
		val2%=256;
		val1+=val2;
		CS = val1>255?true:false;
		String b = Integer.toBinaryString(val1);
		int ones=0;
		for(int i=0;i<b.length();i++)
			if(b.charAt(i)=='1')
				ones++;
		Z = ones==0?true:false;
		P = ones%2==0?true:false;
	}

	//Type 2 of CMP
	static void cmp_with_mem(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(memory.get(memory_address_hl()));
		S = val1>=val2?false:true;
		val2 = 256-val2;
		val2%=256;
		val1+=val2;
		CS = val1>255?true:false;
		String b = Integer.toBinaryString(val1);
		int ones=0;
		for(int i=0;i<b.length();i++)
			if(b.charAt(i)=='1')
				ones++;
		Z = ones==0?true:false;
		P = ones%2==0?true:false;
	}



	//CPI Command
	/*
	 * Type 1 Compare the data with the content of A
	 */

	static int size_of_cpi(String passed){
		return 2;
	}

	//Call the appropriate method according to type
	static void perform_cpi(String passed){
		int type = type_of_cpi(passed);
		switch(type){
		case 1:
			cpi_with_acc(passed);
			break;
		}
	}

	//Find the type of CPI
	static int type_of_cpi(String passed){
		return 1;
	}

	//Type 1 of CPI
	static void cpi_with_acc(String passed){
		int val1 = hexa_to_deci(registers.get('A'));
		int val2 = hexa_to_deci(passed.substring(4));
		S = val1>=val2?false:true;
		val2 = 256-val2;
		val2%=256;
		val1+=val2;
		CS = val1>255?true:false;
		String b = Integer.toBinaryString(val1);
		int ones=0;
		for(int i=0;i<b.length();i++)
			if(b.charAt(i)=='1')
				ones++;
		Z = ones==0?true:false;
		P = ones%2==0?true:false;
	}



	//RLC Command
	/*
	 * Type 1 Rotate the accumulator content left without CS
	 */

	static int size_of_rlc(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rlc(String passed){
		int type = type_of_rlc(passed);
		switch(type){
		case 1:
			rlc_without_carry(passed);
			break;
		}
	}

	//Find the type of RLC
	static int type_of_rlc(String passed){
		return 1;
	}

	//Type 1 of RLC
	static void rlc_without_carry(String passed){
		int val = hexa_to_deci(registers.get('A'));
		int bits[] = new int[8];
		for(int i=0;i<8;i++){
			bits[7-i] = val%2;
			val/=2;
		}
		CS = bits[0]==1?true:false;
		for(int i=0;i<7;i++)
			bits[i] = bits[i+1];
		bits[7] = CS?1:0;
		int dec = 0;
		for(int i=0;i<8;i++)
			dec+=(bits[7-i]*Math.pow(2,i));
		registers.put('A',decimel_to_hexa_8bit(dec));
	}



	//RRC Command
	/*
	 * Type 1 Rotate the A right without CS
	 */

	static int size_of_rrc(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rrc(String passed){
		int type = type_of_rrc(passed);
		switch(type){
		case 1:
			rrc_without_carry(passed);
			break;
		}
	}

	//Find the type of RRC
	static int type_of_rrc(String passed){
		return 1;
	}

	//Type 1 of RRC
	static void rrc_without_carry(String passed){
		int val = hexa_to_deci(registers.get('A'));
		int bits[] = new int[8];
		for(int i=0;i<8;i++){
			bits[7-i] = val%2;
			val/=2;
		}
		CS = bits[7]==1?true:false;
		for(int i=7;i>0;i--)
			bits[i] = bits[i-1];
		bits[0] = CS?1:0;
		int dec = 0;
		for(int i=0;i<8;i++)
			dec+=(bits[7-i]*Math.pow(2,i));
		registers.put('A',decimel_to_hexa_8bit(dec));
	}



	//RAL Command
	/*
	 * Type 1 Rotate the A content left through CS
	 */

	static int size_of_ral(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_ral(String passed){
		int type = type_of_ral(passed);
		switch(type){
		case 1:
			ral_with_car(passed);
			break;
		}
	}

	//Find the type of RAL
	static int type_of_ral(String passed){
		return 1;
	}

	//Type 1 of RAL
	static void ral_with_car(String passed){
		int val = hexa_to_deci(registers.get('A'));
		int bits[] = new int[9];
		for(int i=0;i<8;i++){
			bits[8-i] = val%2;
			val/=2;
		}
		for(int i=0;i<8;i++)
			bits[i] = bits[i+1];
		bits[8] = CS?1:0;
		CS = bits[0]==1?true:false;
		int dec = 0;
		for(int i=0;i<8;i++)
			dec+=(bits[8-i]*Math.pow(2,i));
		registers.put('A',decimel_to_hexa_8bit(dec));
	}



	//RAR Command
	/*
	 * Type 1 Rotate the A content right through CS
	 */

	static int size_of_rar(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rar(String passed){
		int type = type_of_rar(passed);
		switch(type){
		case 1:
			rar_with_car(passed);
			break;
		}
	}

	//Find the type of RAR
	static int type_of_rar(String passed){
		return 1;
	}

	//Type 1 of RAR
	static void rar_with_car(String passed){
		int val = hexa_to_deci(registers.get('A'));
		int bits[] = new int[9];
		for(int i=0;i<8;i++){
			bits[8-i] = val%2;
			val/=2;
		}
		bits[0] = CS?1:0;
		CS = bits[8]==1?true:false;
		for(int i=8;i>0;i--)
			bits[i] = bits[i-1];
		int dec = 0;
		for(int i=0;i<8;i++)
			dec+=(bits[8-i]*Math.pow(2,i));
		registers.put('A',decimel_to_hexa_8bit(dec));
	}




	//BRANCH GROUP

	//Get the Program Status Word
	static int psw(){
		int psw = 0;
		psw+=(CS?1:0);
		psw+=(P?4:0);
		psw+=(AC?16:0);
		psw+=(Z?64:0);
		psw+=(S?128:0);
		return psw;
	}


	//Fill the stack
	static void fill_the_stack(String h,String l){
		memory.put(--SP,h);
		memory.put(--SP,l);
	}


	//Complete the jump requirements
	static void complete_jump_req(String passed){
		//		String program_counter = decimel_to_hexa(PC);
		//		fill_the_stack(program_counter.substring(0,2),program_counter.substring(2,4),psw());
		if(labels.get(passed)==null)
			PC = hexa_to_deci(passed);
		else
			PC = labels.get(passed);
		modified =true;
	}





	//JMP Command
	/*
	 * Type 1 Unconditional jump
	 */

	static int size_of_jmp(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jmp(String passed){
		int type = type_of_jmp(passed);
		switch(type){
		case 1:
			jump_without_condition(passed);
			break;
		}
	}

	//Find the type of JMP
	static int type_of_jmp(String passed){
		return 1;
	}

	//Type 1 of JMP
	static void jump_without_condition(String passed){
		complete_jump_req(passed.substring(4));
	}




	//JZ Command
	/*
	 * Type 1 Jump if zero is set
	 */

	static int size_of_jz(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jz(String passed){
		int type = type_of_jz(passed);
		switch(type){
		case 1:
			jump_when_zero(passed);
			break;
		}
	}

	//Find the type of JZ
	static int type_of_jz(String passed){
		return 1;
	}

	//Type 1 of JZ
	static void jump_when_zero(String passed){
		if(Z)
			complete_jump_req(passed.substring(3));
	}



	//JNZ Command
	/*
	 * Type 1 Jump if zero is not set
	 */

	static int size_of_jnz(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jnz(String passed){
		int type = type_of_jnz(passed);
		switch(type){
		case 1:
			jump_when_not_zero(passed);
			break;
		}
	}

	//Find the type of JNZ
	static int type_of_jnz(String passed){
		return 1;
	}

	//Type 1 of JNZ
	static void jump_when_not_zero(String passed){
		if(!Z)
			complete_jump_req(passed.substring(4));
	}



	//JC Command
	/*
	 * Type 1 Jump if carry is set
	 */

	static int size_of_jc(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jc(String passed){
		int type = type_of_jc(passed);
		switch(type){
		case 1:
			jump_when_carry(passed);
			break;
		}
	}

	//Find the type of JC
	static int type_of_jc(String passed){
		return 1;
	}

	//Type 1 of JC
	static void jump_when_carry(String passed){
		if(CS)
			complete_jump_req(passed.substring(3));
	}



	//JNC Command
	/*
	 * Type 1 Jump if carry is not set
	 */

	static int size_of_jnc(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jnc(String passed){
		int type = type_of_jnc(passed);
		switch(type){
		case 1:
			jump_when_carry_not(passed);
			break;
		}
	}

	//Find the type of JNC
	static int type_of_jnc(String passed){
		return 1;
	}

	//Type 1 of JNC
	static void jump_when_carry_not(String passed){
		if(!CS)
			complete_jump_req(passed.substring(4));
	}



	//JP Command
	/*
	 * Type 1 Jump if sign is not set
	 */

	static int size_of_jp(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jp(String passed){
		int type = type_of_jp(passed);
		switch(type){
		case 1:
			jump_when_sign_not(passed);
			break;
		}
	}

	//Find the type of JP
	static int type_of_jp(String passed){
		return 1;
	}

	//Type 1 of JZ
	static void jump_when_sign_not(String passed){
		if(!S)
			complete_jump_req(passed.substring(3));
	}



	//JM Command
	/*
	 * Type 1 Jump if sign is set
	 */

	static int size_of_jm(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jm(String passed){
		int type = type_of_jm(passed);
		switch(type){
		case 1:
			jump_when_sign(passed);
			break;
		}
	}

	//Find the type of JM
	static int type_of_jm(String passed){
		return 1;
	}

	//Type 1 of JZ
	static void jump_when_sign(String passed){
		if(S)
			complete_jump_req(passed.substring(3));
	}



	//JPE Command
	/*
	 * Type 1 Jump if parity is set i.e. even parity
	 */

	static int size_of_jpe(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jpe(String passed){
		int type = type_of_jpe(passed);
		switch(type){
		case 1:
			jump_when_parity(passed);
			break;
		}
	}

	//Find the type of JPE
	static int type_of_jpe(String passed){
		return 1;
	}

	//Type 1 of JPE
	static void jump_when_parity(String passed){
		if(P)
			complete_jump_req(passed.substring(4));
	}



	//JPO Command
	/*
	 * Type 1 Jump if parity is not set
	 */

	static int size_of_jpo(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_jpo(String passed){
		int type = type_of_jpo(passed);
		switch(type){
		case 1:
			jump_when_parity_not(passed);
			break;
		}
	}

	//Find the type of JPO
	static int type_of_jpo(String passed){
		return 1;
	}

	//Type 1 of JPO
	static void jump_when_parity_not(String passed){
		if(!P)
			complete_jump_req(passed.substring(4));
	}



	//Complete CAll requirements
	static void complete_call_req(String passed){
		String program_counter = decimel_to_hexa(PC);
		fill_the_stack(program_counter.substring(0,2),program_counter.substring(2,4));
		if(labels.get(passed)==null)
			PC = hexa_to_deci(passed);
		else
			PC = labels.get(passed);	
		modified =true;
	}



	//CALL Command
	/*
	 * Type 1 Call the function unconditionally
	 */

	static int size_of_call(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_call(String passed){
		int type = type_of_call(passed);
		switch(type){
		case 1:
			call_without_condition(passed);
			break;
		}
	}

	//Find type of CALL
	static int type_of_call(String passed){
		return 1;
	}

	//Type 1 of CALL
	static void call_without_condition(String passed){
		complete_call_req(passed.substring(5));
	}




	//CC Command
	/*
	 * Type 1 Call if carry is set
	 */

	static int size_of_cc(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cc(String passed){
		int type = type_of_cc(passed);
		switch(type){
		case 1:
			call_when_carry(passed);
			break;
		}
	}

	//Find type of CC
	static int type_of_cc(String passed){
		return 1;
	}

	//Type 1 of CC
	static void call_when_carry(String passed){
		if(CS)
			complete_call_req(passed.substring(3));
	}




	//CNC Command
	/*
	 * Type 1 Call when carry is not set
	 */

	static int size_of_cnc(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cnc(String passed){
		int type = type_of_cnc(passed);
		switch(type){
		case 1:
			call_when_no_carry(passed);
			break;
		}
	}

	//Find type of CNC
	static int type_of_cnc(String passed){
		return 1;
	}

	//Type 1 of CNC
	static void call_when_no_carry(String passed){
		if(!CS)
			complete_call_req(passed.substring(4));
	}




	//CZ Command
	/*
	 * Type 1 Call when zero is set
	 */

	static int size_of_cz(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cz(String passed){
		int type = type_of_cz(passed);
		switch(type){
		case 1:
			call_when_zero(passed);
			break;
		}
	}

	//Find type of CZ
	static int type_of_cz(String passed){
		return 1;
	}

	//Type 1 of CZ
	static void call_when_zero(String passed){
		if(Z)
			complete_call_req(passed.substring(3));
	}




	//CNZ Command
	/*
	 * Type 1 Call when zero is not set
	 */

	static int size_of_cnz(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cnz(String passed){
		int type = type_of_cnz(passed);
		switch(type){
		case 1:
			call_when_not_zero(passed);
			break;
		}
	}

	//Find type of CNZ
	static int type_of_cnz(String passed){
		return 1;
	}

	//Type 1 of CNZ
	static void call_when_not_zero(String passed){
		if(!Z)
			complete_call_req(passed.substring(4));
	}



	//CP Command
	/*
	 * Type 1 Call when sign bit is not set
	 */

	static int size_of_cp(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cp(String passed){
		int type = type_of_cp(passed);
		switch(type){
		case 1:
			call_when_sign_not(passed);
			break;
		}
	}

	//Find type of CP
	static int type_of_cp(String passed){
		return 1;
	}

	//Type 1 of CP
	static void call_when_sign_not(String passed){
		if(!S)
			complete_call_req(passed.substring(3));
	}




	//CM Command
	/*
	 * Type 1 Call when sign is set
	 */

	static int size_of_cm(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cm(String passed){
		int type = type_of_cm(passed);
		switch(type){
		case 1:
			call_when_sign(passed);
			break;
		}
	}

	//Find type of CM
	static int type_of_cm(String passed){
		return 1;
	}

	//Type 1 of CM
	static void call_when_sign(String passed){
		if(S)
			complete_call_req(passed.substring(3));
	}



	//CPE Command
	/*
	 * Type 1 Call when parity is set
	 */

	static int size_of_cpe(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cpe(String passed){
		int type = type_of_cpe(passed);
		switch(type){
		case 1:
			call_when_parity(passed);
			break;
		}
	}

	//Find type of CPE
	static int type_of_cpe(String passed){
		return 1;
	}

	//Type 1 of CPE
	static void call_when_parity(String passed){
		if(P)
			complete_call_req(passed.substring(4));
	}



	//CPO Command
	/*
	 * Type 1 Call when parity is not set
	 */

	static int size_of_cpo(String passed){
		return 3;
	}

	//Call the appropriate method according to type
	static void perform_cpo(String passed){
		int type = type_of_cpo(passed);
		switch(type){
		case 1:
			call_when_parity_not(passed);
			break;
		}
	}

	//Find type of CPO
	static int type_of_cpo(String passed){
		return 1;
	}

	//Type 1 of CPO
	static void call_when_parity_not(String passed){
		if(!P)
			complete_call_req(passed.substring(4));
	}




	//Complete the return requirements
	static void complete_return_requirements(){
		String ad = memory.get(SP+1)+memory.get(SP);
		SP+=2;
		PC = hexa_to_deci(ad);
		modified =true;
	}



	//RET Command
	/*
	 * Type 1 Return from subroutine to the main program unconditionally
	 */

	static int size_of_ret(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_ret(String passed){
		int type = type_of_ret(passed);
		switch(type){
		case 1:
			return_unconditionally(passed);
			break;
		}
	}

	//Find the type of RET
	static int type_of_ret(String passed){
		return 1;
	}

	//Type 1 of RET
	static void return_unconditionally(String passed){
		complete_return_requirements();
	}




	//RC Command
	/*
	 * Type 1 Return if carry is set
	 */

	static int size_of_rc(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rc(String passed){
		int type = type_of_rc(passed);
		switch(type){
		case 1:
			return_when_carry(passed);
			break;
		}
	}

	//Find the type of RC
	static int type_of_rc(String passed){
		return 1;
	}

	//Type 1 of RC
	static void return_when_carry(String passed){
		if(CS)
			complete_return_requirements();
	}




	//RNC Command
	/*
	 * Type 1 Return if carry is not set
	 */

	static int size_of_rnc(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rnc(String passed){
		int type = type_of_rnc(passed);
		switch(type){
		case 1:
			return_not_carry(passed);
			break;
		}
	}

	//Find the type of RNC
	static int type_of_rnc(String passed){
		return 1;
	}

	//Type 1 of RNC
	static void return_not_carry(String passed){
		if(!CS)
			complete_return_requirements();
	}




	//RZ Command
	/*
	 * Type 1 Return if zero is set
	 */

	static int size_of_rz(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rz(String passed){
		int type = type_of_rz(passed);
		switch(type){
		case 1:
			return_when_zero(passed);
			break;
		}
	}

	//Find the type of RZ
	static int type_of_rz(String passed){
		return 1;
	}

	//Type 1 of RZ
	static void return_when_zero(String passed){
		if(Z)
			complete_return_requirements();
	}




	//RNZ Command
	/*
	 * Type 1 Return if zero is not set
	 */

	static int size_of_rnz(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rnz(String passed){
		int type = type_of_rnz(passed);
		switch(type){
		case 1:
			return_not_zero(passed);
			break;
		}
	}

	//Find the type of RNZ
	static int type_of_rnz(String passed){
		return 1;
	}

	//Type 1 of RNZ
	static void return_not_zero(String passed){
		if(!Z)
			complete_return_requirements();
	}




	//RP Command
	/*
	 * Type 1 Return if sign is not set
	 */

	static int size_of_rp(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rp(String passed){
		int type = type_of_rp(passed);
		switch(type){
		case 1:
			return_not_sign(passed);
			break;
		}
	}

	//Find the type of RP
	static int type_of_rp(String passed){
		return 1;
	}

	//Type 1 of RP
	static void return_not_sign(String passed){
		if(!S)
			complete_return_requirements();
	}




	//RM Command
	/*
	 * Type 1 Return if sign is set
	 */

	static int size_of_rm(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rm(String passed){
		int type = type_of_rm(passed);
		switch(type){
		case 1:
			return_when_sign(passed);
			break;
		}
	}

	//Find the type of RM
	static int type_of_rm(String passed){
		return 1;
	}

	//Type 1 of RM
	static void return_when_sign(String passed){
		if(S)
			complete_return_requirements();
	}




	//RPE Command
	/*
	 * Type 1 Return if parity is set
	 */

	static int size_of_rpe(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rpe(String passed){
		int type = type_of_rpe(passed);
		switch(type){
		case 1:
			return_when_parity(passed);
			break;
		}
	}

	//Find the type of RPE
	static int type_of_rpe(String passed){
		return 1;
	}

	//Type 1 of RPE
	static void return_when_parity(String passed){
		if(P)
			complete_return_requirements();
	}




	//RPO Command
	/*
	 * Type 1 Return if parity is not set
	 */

	static int size_of_rpo(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_rpo(String passed){
		int type = type_of_rpo(passed);
		switch(type){
		case 1:
			return_not_parity(passed);
			break;
		}
	}

	//Find the type of RPO
	static int type_of_rpo(String passed){
		return 1;
	}

	//Type 1 of RPO
	static void return_not_parity(String passed){
		if(!P)
			complete_return_requirements();
	}





	//RST COMMAND
	//Restart Commands should be written here




	//PCHL Command
	/*
	 * Type 1 Jump to address given by HL pair
	 */

	static int size_of_pchl(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_pchl(String passed){
		int type = type_of_pchl(passed);
		switch(type){
		case 1:
			jump_to_pchl(passed);
			break;
		}
	}

	//Find the type of PCHL
	static int type_of_pchl(String passed){
		return 1;
	}

	//Type 1 of PCHL
	static void jump_to_pchl(String passed){
		String ad = registers.get('H')+registers.get('L');
		PC = hexa_to_deci(ad);
		modified = true;
	}



	//IN Command
	/*
	 * Type 1 Get the input from the input port and store it in the A
	 */

	static int size_of_in(String passed){
		return 2;
	}

	//Call the appropriate method according to type
	static void perform_in(String passed){
		int type = type_of_in(passed);
		switch(type){
		case 1:
			try {
				in_to_acc(passed);
			} catch (IOException e) {
			}
			break;
		}
	}

	//Find the type of IN
	static int type_of_in(String passed){
		return 1;
	}

	//Type 1 of IN
	static void in_to_acc(String passed) throws IOException{
		System.out.print("Input to port "+hexa_to_deci(passed.substring(3))+" : ");
		String enter = scan.readLine();
		registers.put('A',enter);
	}



	//OUT Command
	/*
	 * Type 1 Output the A to the output port
	 */

	static int size_of_out(String passed){
		return 2;
	}

	//Call the appropriate method according to type
	static void perform_out(String passed){
		int type = type_of_out(passed);
		switch(type){
		case 1:
			out_from_acc(passed);
			break;
		}
	}

	//Find the type of OUT
	static int type_of_out(String passed){
		return 1;
	}

	//Type 1 of OUT
	static void out_from_acc(String passed){
		System.out.println("Output to port "+hexa_to_deci(passed.substring(3))+" : "+registers.get('A'));
	}



	//PUSH Command
	/*
	 * Type 1 Push the register pair data on to the stack
	 */

	static int size_of_push(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_push(String passed){
		int type = type_of_push(passed);
		switch(type){
		case 1:
			push_to_stack(passed);
			break;
		}
	}

	//Find the type of PUSH
	static int type_of_push(String passed){
		return 1;
	}

	//Type 1 of PUSH
	static void push_to_stack(String passed){
		char r = passed.charAt(5);
		switch(r){
		case 'B':
			fill_the_stack(registers.get('B'),registers.get('C'));
			break;
		case 'D':
			fill_the_stack(registers.get('D'),registers.get('E'));
			break;
		case 'H':
			fill_the_stack(registers.get('H'),registers.get('L'));
			break;
		case 'P':
			fill_the_stack(registers.get('A'),decimel_to_hexa_8bit(psw()));
			break;
		}
	}




	//POP Command
	/*
	 * Type 1 Pop the contents of the stack
	 */

	static int size_of_pop(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_pop(String passed){
		int type = type_of_pop(passed);
		switch(type){
		case 1:
			pop_from_stack(passed);
			break;
		}
	}

	//Find the type of POP
	static int type_of_pop(String passed){
		return 1;
	}

	//Type 1 of POP
	static void pop_from_stack(String passed){
		char r = passed.charAt(4);
		switch(r){
		case 'B':
			registers.put('C',memory.get(SP++));
			registers.put('B',memory.get(SP++));
			break;
		case 'D':
			registers.put('E',memory.get(SP++));
			registers.put('D',memory.get(SP++));
			break;
		case 'H':
			registers.put('L',memory.get(SP++));
			registers.put('H',memory.get(SP++));
			break;
		case 'P':
			String w = memory.get(SP++);
			registers.put('A',memory.get(SP++));
			w = Integer.toBinaryString(hexa_to_deci(w));
			CS = w.charAt(0)=='1'?true:false;
			P = w.charAt(2)=='1'?true:false;
			AC = w.charAt(4)=='1'?true:false;
			Z = w.charAt(6)=='1'?true:false;
			S = w.charAt(7)=='1'?true:false;
		}
	}




	//HLT Command



	//Implement the HLT Command




	//XTHL Command
	/*
	 * Type 1 Exchange the content of stack with HL pair
	 */

	static int size_of_xthl(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_xthl(String passed){
		int type = type_of_xthl(passed);
		switch(type){
		case 1:
			exchange_with_stack(passed);
			break;
		}
	}

	//Find the type of XTHL
	static int type_of_xthl(String passed){
		return 1;
	}

	//Type 1 of XTHL
	static void exchange_with_stack(String passed){
		String h = registers.get('H');
		String l = registers.get('L');
		int al = hexa_to_deci(memory.get(SP));
		int ah = hexa_to_deci(memory.get(SP+1));
		registers.put('L',memory.get(al));
		registers.put('H',memory.get(ah));
		memory.put(al,l);
		memory.put(ah,h);
	}




	//SPHL Command
	/*
	 * Type 1 Exchange the content of stack with HL pair
	 */

	static int size_of_sphl(String passed){
		return 1;
	}

	//Call the appropriate method according to type
	static void perform_sphl(String passed){
		int type = type_of_sphl(passed);
		switch(type){
		case 1:
			transfer_hl_to_sp(passed);
			break;
		}
	}

	//Find the type of SPHL
	static int type_of_sphl(String passed){
		return 1;
	}

	//Type 1 of SPHL
	static void transfer_hl_to_sp(String passed){
		SP = hexa_to_deci(registers.get('H')+registers.get('L'));
	}


//Some other instructions that are for interrupt masks or enabling or disabling them
	
}
