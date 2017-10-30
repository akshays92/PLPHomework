package cop5556fa17.AST;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.Scanner.Token;

public abstract class Source extends ASTNode{
	
	Type Type;
	public Type getUtilType() {
		return Type;
	}
	public void setUtilType(Type type) {
		Type = type;
	}
	public Source(Token firstToken) {
		super(firstToken);
	}
	
	
}
