package util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Util {	
	public static String simplifyMethodSignature(String signature){
		String simplifiedMethodSignature = "";
		String firstPart = "";
		String parameters= "";
		String lastPart  = "";
		String aux 		 = "";

		//signature = signature.replaceAll("\\s+","");

		for (int i = 0, n = signature.length(); i < n; i++) {
			char chr = signature.charAt(i);
			if (chr == '('){
				aux = signature.substring(i+1,signature.length());
				firstPart+="(";
				break;
			}else
				firstPart += chr;
		}
		for (int i = 0, n = aux.length(); i < n; i++) {
			char chr = aux.charAt(i);
			if (chr == ')'){
				lastPart = aux.substring(i,aux.length());
				break;
			}else
				parameters += chr;
		}

		simplifiedMethodSignature = firstPart + normalizeParameters(parameters) + lastPart;
		simplifiedMethodSignature = simplifiedMethodSignature.replace("{FormalParametersInternal}", "");
		return removeGenerics(simplifiedMethodSignature);
	}
	
	private static String normalizeParameters(String parameters){
		String normalizedParameters = "";
		String[] strs = parameters.split("-");
		for(int i = 0; i < strs.length; i++){
			if(i % 2 == 0){
				normalizedParameters+=(strs[i]+",");
			}
		}
		normalizedParameters = (normalizedParameters.substring(0,normalizedParameters.length()-1)) + "";
		return normalizedParameters;
	}
	
	public static List<String> getArgs(String signature){
		String parameters= "";
		String aux 		 = "";

		signature = signature.replaceAll("\\s+","");

		for (int i = 0, n = signature.length(); i < n; i++) {
			char chr = signature.charAt(i);
			if (chr == '('){
				aux = signature.substring(i+1,signature.length());
				break;
			}
		}
		for (int i = 0, n = aux.length(); i < n; i++) {
			char chr = aux.charAt(i);
			if (chr == ')'){
				break;
			}else
				parameters += chr;
		}
		return new ArrayList<String>(Arrays.asList(parameters.replace("{FormalParametersInternal}", "").split(",")));		
	}
		
	private static boolean isStatic(String str) {
		return str.equalsIgnoreCase("static");
	}

	private static boolean isAccessModifier(String str) {
		return str.equalsIgnoreCase("private")
				|| str.equalsIgnoreCase("public")
				|| str.equalsIgnoreCase("protected");
	}
	
	private static boolean isMethodModifier(String str)
	{
		return Util.isAccessModifier(str)
				|| isStatic(str)
				|| str.equalsIgnoreCase("abstract")
				|| str.equalsIgnoreCase("final")
				|| str.equalsIgnoreCase("native")
				|| str.equalsIgnoreCase("strictfp")
				|| str.equalsIgnoreCase("synchronized");
	}
	
	public static boolean isStaticMethod(List<String> modifiersList)
	{
		return modifiersList.contains("static");
	}
	
	public static boolean isPrivateMethod(List<String> modifiersList)
	{
		return modifiersList.contains("private");
	}
	
	private static List<String> getModifiersList(String str)
	{
		String[] strs = str.split("\\s+");
		List<String> modifiers = new ArrayList<String>();
		int i = 0;
		while(i < strs.length && isMethodModifier(strs[i]))
		{
			modifiers.add(strs[i]);
			i++;
		}
		return modifiers;
	}
		
	private static boolean isPrivate(String str)
	{
		return str.equalsIgnoreCase("private");
	}
	
	private static boolean isPublic(String str)
	{
		return str.equalsIgnoreCase("public");
	}
	
	private static String removeGenerics(String methodSignature) {
		String res = "";
		int count = 0;
		boolean canTake = true;
		for (int i = 0, n = methodSignature.length(); i < n; i++) {
			char chr = methodSignature.charAt(i);
			if (chr == '<') {
				if (canTake) {
					canTake = false;
					count = 1;
				} else {
					count++;
				}
			} else if (chr == '>') {
				count--;
				canTake = count == 0;
			}else if(canTake)
			{
				res += chr;
			}
		}
		return res;
	}
	
	private static String getFullType(String arg, List<String> imports)
	{
		String fullType = arg;
		if(!isPrimitiveType(arg))
		{
			String[] importSplit;
			String importStr;
			int j = 0;
			boolean found = false;
			while(j < imports.size() && !found)
			{
				importStr = imports.get(j);
				importSplit = importStr.split("\\.");
				String typeName = arg.replace("[]", "");
				found = importSplit[importSplit.length - 1].equals(typeName);
				
				if(found)
				{
					fullType = arg.replace(typeName, importStr);
				}
				j++;
			}
		}
		return fullType;
	}
	
	public static String includeFullArgsTypes(String signature, List<String> imports)
	{
		List<String> args = getArgs(signature);
		String oldArgsStr = String.join(",", args);
		int i = 0;
		for(String arg : args)
		{
			String fullType = getFullType(arg, imports);
			
			if(!fullType.equals(arg))
			{
				args.set(i, fullType);
			}
			i++;
		}
		String newArgsStr = String.join(",", args);
		return signature.replace(oldArgsStr, newArgsStr);
	}
		
	public static boolean isPrimitiveType(String typeStr)
	{
		return typeStr.equals("byte") ||
				typeStr.equals("short") ||
				typeStr.equals("int") ||
				typeStr.equals("long") ||
				typeStr.equals("float") ||
				typeStr.equals("double") ||
				typeStr.equals("char") ||
				typeStr.equals("boolean");
	}
	
	private static boolean isGeneric(String str) {
		return str.startsWith("<");
	}
		
	public static String getMethodReturnType(String methodSignature, List<String> imports) {
		String[] strs = methodSignature.split("\\s+");
		int i = 0;
		while(i < strs.length && strs[i].startsWith("@"))
		{
			i++;
		}
		while(i < strs.length && isMethodModifier(strs[i]))
		{
			i++;
		}
		String returnType = "";
		if(i < strs.length)
		{
			if (!isGeneric(strs[i])) {
				returnType = strs[i];
			} else {
				returnType = getMethodReturnType(removeGenerics(methodSignature), imports);
			}
		}		
		returnType = removeGenerics(returnType);
		return returnType.equals("void") ? returnType : getFullType(returnType, imports);
	}
		
	public static void main(String[] args) {
		System.out.println(getArgs("soma(int-int-boolean-boolean) throws Exception"));
		System.out.println(getArgs("int soma()"));
		System.out.println(getArgs("public static void int soma(List<Integer>-List<Integer>-int-int)"));
		System.out.println(simplifyMethodSignature("soma(List<Integer>-List<Integer>-int-int) throws Exeception"));
		System.out.println(simplifyMethodSignature("soma()"));
		System.out.println(getArgs(simplifyMethodSignature("soma(List<Integer>-List<Integer>-int-int) throws Exeception")));
		System.out.println(removeGenerics("public void soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}"));
		String str = "<T, S extends T> int copy(List<T> dest, List<S> src) {";
		System.out.println(removeGenerics("soma(List<Integer>-List<Integer>-int-int)"));
		System.out.println(removeGenerics(simplifyMethodSignature("soma(List<Integer>-List<Integer>-int-int) throws Exeception")));
		List<String> imports = new ArrayList<String>();
		imports.add("rx.Scheduler");
		imports.add("cin.ufpe.br.A");
		imports.add("java.util.List");
		System.out.println(includeFullArgsTypes(removeGenerics(simplifyMethodSignature(("soma(List<Integer>-List<Integer>-int-int) throws Exeception"))), imports));
		System.out.println(includeFullArgsTypes(removeGenerics(simplifyMethodSignature(("soma(List<Integer>-List<Integer>-Scheduler-Scheduler) throws Exeception"))), imports));
		System.out.println(includeFullArgsTypes(removeGenerics(simplifyMethodSignature(("soma(String-String-Scheduler-Scheduler-Object-Object) throws Exeception"))), imports));
		System.out.println(includeFullArgsTypes(removeGenerics(simplifyMethodSignature(("soma(String[]-String[]-Scheduler[]-Scheduler[]-Object-Object) throws Exeception"))), imports));
		System.out.println(includeFullArgsTypes(removeGenerics(simplifyMethodSignature(("soma(Character.Subset-Character.Subset) throws Exeception"))), imports));
		System.out.println(removeGenerics("public List<Integer> soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}"));
		System.out.println(getMethodReturnType("public List<Integer> soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public static List<Integer> soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public void soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public static void soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public static int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public static synchronized int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("public static native synchronized int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("static synchronized int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println(getMethodReturnType("synchronized int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
		System.out.println("Type: "+getMethodReturnType("@Override   "
				+ "synchronized int soma(List<Integer> a, List<Integer> b, int c, int d) throws Exeception {return 1;}", imports));
	}
}