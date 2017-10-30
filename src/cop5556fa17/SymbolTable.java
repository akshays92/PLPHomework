package cop5556fa17;
import java.util.HashMap;

import cop5556fa17.AST.Declaration;

public class SymbolTable {
	static HashMap <String, Declaration> symbolTable ;
	
	public SymbolTable(){
		symbolTable = new HashMap<String,Declaration>();
	}
	
	public Declaration lookupDec(String s) {
		return symbolTable.get(s);
	}
	
	public void putSymbolTable(String s, Declaration d){
		 symbolTable.put(s, d);
	}
}
