package step2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ImportDeclaration;

public class ImportDeclarationVisitor extends ASTVisitor {
	List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();
	
	public boolean visit(ImportDeclaration node) {
		imports.add(node);
		return super.visit(node);
	}
	
	public List<ImportDeclaration> getImports() {
		return imports;
	}
}
